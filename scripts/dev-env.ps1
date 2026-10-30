$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path $PSScriptRoot -Parent
$LocalRoot = Join-Path $ProjectRoot '.local'
New-Item -ItemType Directory -Force $LocalRoot, "$LocalRoot/logs" | Out-Null
$secretFile = Join-Path $LocalRoot 'env.json'
if (!(Test-Path $secretFile)) {
    @{
        FLASHGO_DB_PASSWORD = [guid]::NewGuid().ToString('N')
        FLASHGO_REDIS_PASSWORD = [guid]::NewGuid().ToString('N')
        FLASHGO_RABBIT_PASSWORD = [guid]::NewGuid().ToString('N')
        FLASHGO_JWT_SECRET = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
    } | ConvertTo-Json | Set-Content $secretFile
}
$Secrets = Get-Content $secretFile -Raw | ConvertFrom-Json
$javaProperty = (& java -XshowSettings:properties -version 2>&1 | Select-String '^\s*java.home\s*=').ToString()
$javaRuntime = $javaProperty -replace '^\s*java.home\s*=\s*', ''
$JavaExecutable = Join-Path $javaRuntime 'bin/java.exe'
$Secrets.PSObject.Properties | Where-Object Name -like 'FLASHGO_*' | ForEach-Object {
    [Environment]::SetEnvironmentVariable($_.Name, [string]$_.Value, 'Process')
}
if (!$env:FLASHGO_DB_USER) { $env:FLASHGO_DB_USER = 'root' }
# Keep IDE startup and script startup on the same ignored local configuration.
$localConfig = @{
    spring = @{
        datasource = @{username=$env:FLASHGO_DB_USER;password=$Secrets.FLASHGO_DB_PASSWORD}
        data = @{redis=@{password=$Secrets.FLASHGO_REDIS_PASSWORD}}
        rabbitmq = @{password=$Secrets.FLASHGO_RABBIT_PASSWORD}
    }
    flashgo = @{auth=@{'jwt-secret'=$Secrets.FLASHGO_JWT_SECRET}}
}
# JSON is valid YAML and safely quotes passwords containing special characters.
$localConfig | ConvertTo-Json -Depth 6 | Set-Content "$LocalRoot/application-local.yml" -Encoding utf8
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:ERLANG_HOME = "$LocalRoot/tools/erlang"
$env:RABBITMQ_BASE = "$LocalRoot/rabbitmq"
$env:RABBITMQ_MNESIA_BASE = "$LocalRoot/rabbitmq/db"
$env:RABBITMQ_LOG_BASE = "$LocalRoot/logs"
$env:RABBITMQ_CONFIG_FILE = "$LocalRoot/rabbitmq/rabbitmq"
$env:RABBITMQ_ENABLED_PLUGINS_FILE = "$LocalRoot/rabbitmq/enabled_plugins"
$env:RABBITMQ_NODENAME = 'flashgo@localhost'
$env:RABBITMQ_ERLANG_COOKIE = $Secrets.FLASHGO_RABBIT_PASSWORD
$env:ERL_EPMD_ADDRESS = '127.0.0.1'
$env:RABBITMQ_DIST_PORT = '25673'
$env:RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS = '-kernel inet_dist_use_interface {127,0,0,1}'
$MysqlBin = if ($env:FLASHGO_MYSQL_BIN) { $env:FLASHGO_MYSQL_BIN } else { 'D:/DevelopTools/mysql/bin' }
$RabbitSbin = "$LocalRoot/tools/rabbitmq_server-4.4.0/sbin"
$Memurai = Get-ChildItem "$LocalRoot/tools/memurai" -Filter memurai.exe -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName

function Test-LocalPort([int]$Port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try { $client.Connect('127.0.0.1', $Port); return $true } catch { return $false } finally { $client.Dispose() }
}
function Wait-LocalPort([int]$Port, [int]$Seconds = 60) {
    $deadline = (Get-Date).AddSeconds($Seconds)
    do {
        if (Test-LocalPort $Port) { return }
        Start-Sleep -Milliseconds 500
    } while ((Get-Date) -lt $deadline)
    throw "Port $Port did not become ready. Check .local/logs."
}
function Assert-Exit([string]$Task) {
    if ($LASTEXITCODE -ne 0) { throw "$Task failed (exit $LASTEXITCODE)" }
}
