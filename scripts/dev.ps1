param([ValidateSet('Start','Stop','Status','Run','StartApp','StopApp','Test','Build')][string]$Action = 'Status')
. "$PSScriptRoot/dev-env.ps1"
Push-Location $ProjectRoot
try {
    switch ($Action) {
        'Start' {
            if (!(Test-Path "$MysqlBin/mysqld.exe") -or !$Memurai -or !(Test-Path "$RabbitSbin/rabbitmq-server.bat")) {
                throw 'Missing native dependencies. Run scripts/setup-native.ps1 first.'
            }
            if (!(Test-Path "$LocalRoot/mysql/data/mysql")) { throw 'MySQL data is not initialized. Run setup-native.ps1.' }
            if (!(Test-LocalPort 13306)) {
                $mysqlArgs = @('--no-defaults', "--datadir=$LocalRoot/mysql/data", '--port=13306', '--bind-address=127.0.0.1', '--mysqlx=OFF', '--console')
                $process = Start-Process "$MysqlBin/mysqld.exe" -ArgumentList $mysqlArgs -WindowStyle Hidden -PassThru -RedirectStandardOutput "$LocalRoot/logs/mysql.out.log" -RedirectStandardError "$LocalRoot/logs/mysql.err.log"
                $process.Id | Set-Content "$LocalRoot/mysql.pid"
                Wait-LocalPort 13306
            }
            if (!(Test-LocalPort 16379)) {
                $process = Start-Process $Memurai -ArgumentList "$LocalRoot/redis/redis.conf" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$LocalRoot/logs/redis.out.log" -RedirectStandardError "$LocalRoot/logs/redis.err.log"
                $process.Id | Set-Content "$LocalRoot/redis.pid"
                Wait-LocalPort 16379
            }
            if (!(Test-LocalPort 15672)) {
                & "$RabbitSbin/rabbitmq-server.bat" -detached
                Assert-Exit 'RabbitMQ startup'
                Wait-LocalPort 15672
            }
            Write-Host 'Native dependencies ready: MySQL 13306, Redis 16379, RabbitMQ 15672 (management 15673).'
        }
        'Stop' {
            if (Test-LocalPort 8080) { Write-Host 'Stop the foreground application with Ctrl+C before stopping dependencies.' }
            if (Test-LocalPort 15672) { & "$RabbitSbin/rabbitmqctl.bat" shutdown; Assert-Exit 'RabbitMQ shutdown' }
            if (Test-LocalPort 16379) {
                $env:REDISCLI_AUTH = $Secrets.FLASHGO_REDIS_PASSWORD
                try { & (Join-Path (Split-Path $Memurai) 'memurai-cli.exe') -p 16379 shutdown; Assert-Exit 'Redis shutdown' } finally { Remove-Item Env:REDISCLI_AUTH -ErrorAction SilentlyContinue }
            }
            if (Test-LocalPort 13306) {
                $env:MYSQL_PWD = $Secrets.MYSQL_ROOT_PASSWORD
                try { & "$MysqlBin/mysqladmin.exe" --no-defaults -h 127.0.0.1 -P 13306 -u root shutdown; Assert-Exit 'MySQL shutdown' } finally { Remove-Item Env:MYSQL_PWD }
            }
        }
        'Status' {
            foreach ($entry in @(@('MySQL',13306),@('Redis',16379),@('RabbitMQ',15672),@('RabbitMQ UI',15673),@('FlashGo',8080))) {
                Write-Host ("{0,-12} {1}: {2}" -f $entry[0],$entry[1],(Test-LocalPort $entry[1]))
            }
        }
        'Run' { & ./mvnw.cmd spring-boot:run; Assert-Exit 'Application' }
        'StartApp' {
            if (Test-LocalPort 8080) { throw 'Port 8080 is already in use.' }
            $jar = Join-Path $ProjectRoot 'target/flashgo-0.0.1-SNAPSHOT.jar'
            if (!(Test-Path $jar)) { throw 'Build the project first.' }
            $process = Start-Process $JavaExecutable -ArgumentList @('-Dfile.encoding=UTF-8','-jar',$jar) -WorkingDirectory $ProjectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput "$LocalRoot/logs/app.out.log" -RedirectStandardError "$LocalRoot/logs/app.err.log"
            @{Id=$process.Id;StartTime=$process.StartTime.ToUniversalTime().ToString('o')} | ConvertTo-Json | Set-Content "$LocalRoot/app-process.json"
            Wait-LocalPort 8080
            Write-Host 'FlashGo started. Use StopApp to stop this background process.'
        }
        'StopApp' {
            if (Test-Path "$LocalRoot/app-process.json") {
                $record = Get-Content "$LocalRoot/app-process.json" -Raw | ConvertFrom-Json
                $process = Get-Process -Id $record.Id -ErrorAction SilentlyContinue
                $expectedStart = ([datetime]$record.StartTime).ToUniversalTime()
                if ($process -and $process.ProcessName -eq 'java' -and $process.StartTime.ToUniversalTime().Ticks -eq $expectedStart.Ticks) {
                    Stop-Process -Id $process.Id
                    $process.WaitForExit(10000) | Out-Null
                    Write-Host 'FlashGo stopped.'
                }
            }
        }
        'Test' { & ./mvnw.cmd -B verify; Assert-Exit 'Verification' }
        'Build' { & ./mvnw.cmd -B '-DskipTests' package; Assert-Exit 'Build' }
    }
} finally { Pop-Location }
