param([string]$MysqlBin = 'D:/DevelopTools/mysql/bin')
$env:FLASHGO_MYSQL_BIN = $MysqlBin
. "$PSScriptRoot/dev-env.ps1"
Push-Location $ProjectRoot
try {
    New-Item -ItemType Directory -Force "$LocalRoot/downloads", "$LocalRoot/tools", "$LocalRoot/redis", "$LocalRoot/rabbitmq" | Out-Null
    function Get-Package([string]$Name, [string]$Url) {
        $destination = "$LocalRoot/downloads/$Name"
        if (!(Test-Path $destination)) {
            & curl.exe --fail --location --continue-at - --retry 3 --retry-all-errors --connect-timeout 20 --output "$destination.partial" $Url
            Assert-Exit "Download $Name"
            Move-Item -LiteralPath "$destination.partial" -Destination $destination
        }
        return $destination
    }
    if (!(Test-Path "$env:ERLANG_HOME/bin/erl.exe")) {
        $archive = Get-Package 'otp.zip' 'https://github.com/erlang/otp/releases/download/OTP-27.3.4.19/otp_win64_27.3.4.19.zip'
        Expand-Archive -LiteralPath $archive -DestinationPath $env:ERLANG_HOME -Force
    }
    & "$env:ERLANG_HOME/bin/erl.exe" -noshell -eval 'halt().'
    Assert-Exit 'Erlang runtime (see INSTALL.txt if the Visual C++ runtime is missing)'
    if (!(Test-Path "$RabbitSbin/rabbitmq-server.bat")) {
        $archive = Get-Package 'rabbitmq.zip' 'https://github.com/rabbitmq/rabbitmq-server/releases/download/v4.4.0/rabbitmq-server-windows-4.4.0.zip'
        Expand-Archive -LiteralPath $archive -DestinationPath "$LocalRoot/tools" -Force
    }
    if (!$Memurai) {
        $installer = Get-Package 'memurai.msi' 'https://dist.memurai.com/releases/Memurai-Developer/4.1.2/Memurai-Developer-v4.1.2.msi'
        if ((Get-AuthenticodeSignature $installer).Status -ne 'Valid') { throw 'Memurai installer signature is not valid.' }
        # Administrative extraction only: no machine-wide service, PATH or firewall changes.
        $process = Start-Process msiexec.exe -ArgumentList @('/a', "`"$installer`"", '/qn', "TARGETDIR=`"$LocalRoot/tools/memurai`"", '/l*v', "`"$LocalRoot/logs/memurai-extract.log`"") -WindowStyle Hidden -PassThru -Wait
        if ($process.ExitCode -ne 0) { throw "Memurai extraction failed: $($process.ExitCode)" }
    }
    @"
bind 127.0.0.1
port 16379
requirepass $($Secrets.FLASHGO_REDIS_PASSWORD)
dir $($LocalRoot.Replace('\','/'))/redis
appendonly yes
"@ | Set-Content "$LocalRoot/redis/redis.conf" -Encoding ascii
    @"
listeners.tcp.1 = 127.0.0.1:15672
management.tcp.ip = 127.0.0.1
management.tcp.port = 15673
default_user = flashgo
default_pass = $($Secrets.FLASHGO_RABBIT_PASSWORD)
default_vhost = /flashgo
"@ | Set-Content "$LocalRoot/rabbitmq/rabbitmq.conf" -Encoding ascii
    '[rabbitmq_management].' | Set-Content "$LocalRoot/rabbitmq/enabled_plugins" -Encoding ascii
    if (!(Test-LocalPort 3306)) { throw 'Start the existing MySQL91 service on port 3306 first.' }
    $env:MYSQL_PWD = $Secrets.FLASHGO_DB_PASSWORD
    try {
        'CREATE DATABASE IF NOT EXISTS flashgo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' | & "$MysqlBin/mysql.exe" --no-defaults -h 127.0.0.1 -P 3306 -u $env:FLASHGO_DB_USER
        Assert-Exit 'Local database connection and creation'
    } finally { Remove-Item Env:MYSQL_PWD }
    & "$PSScriptRoot/dev.ps1" Start
    Get-ChildItem "$LocalRoot/downloads" -File | Where-Object Extension -in '.zip','.msi' | Get-FileHash -Algorithm SHA256 | Select-Object Path,Hash | ConvertTo-Json | Set-Content "$LocalRoot/downloads/checksums.json"
} finally { Pop-Location }
