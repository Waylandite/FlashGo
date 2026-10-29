. "$PSScriptRoot/dev-env.ps1"
$base = 'http://127.0.0.1:8080/api/v1'
$health = Invoke-RestMethod "$base/health"
if ($health.data.status -ne 'UP') { throw 'Health check failed' }
$spec = Invoke-RestMethod 'http://127.0.0.1:8080/v3/api-docs'
if (!$spec.paths.PSObject.Properties['/api/v1/auth/login']) { throw 'OpenAPI login endpoint missing' }
foreach ($account in @(@('APP_USER','13800000001'),@('MERCHANT','13800000002'),@('RIDER','13800000003'),@('PLATFORM','local_admin'))) {
    $body = @{type=$account[0];username=$account[1];password=$Secrets.FLASHGO_DEV_PASSWORD} | ConvertTo-Json
    $login = Invoke-RestMethod "$base/auth/login" -Method Post -ContentType 'application/json' -Body $body
    if (!$login.data.accessToken) { throw "Login failed for $($account[0])" }
    $headers = @{Authorization="Bearer $($login.data.accessToken)"}
    $me = Invoke-RestMethod "$base/auth/me" -Headers $headers
    if ($me.data.identityType -ne $account[0]) { throw 'Identity mismatch' }
    $null = Invoke-RestMethod "$base/auth/logout" -Method Post -Headers $headers
    $rejected = $false
    try { $null = Invoke-RestMethod "$base/auth/me" -Headers $headers } catch {
        if ([int]$_.Exception.Response.StatusCode -eq 401) { $rejected=$true } else { throw }
    }
    if (!$rejected) { throw 'Logged-out token was accepted' }
    Write-Host "$($account[0]): login / me / logout / revoked token passed"
}
$rabbitAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("flashgo:$($Secrets.FLASHGO_RABBIT_PASSWORD)"))
$rabbitHeaders = @{Authorization="Basic $rabbitAuth"}
$rabbitBase = 'http://127.0.0.1:15673/api'
$overview = Invoke-RestMethod "$rabbitBase/overview" -Headers $rabbitHeaders
$queue = 'flashgo.dev.smoke.' + [guid]::NewGuid().ToString('N')
$queueUrl = "$rabbitBase/queues/%2Fflashgo/$queue"
$queueCreated = $false
try {
    $null = Invoke-RestMethod $queueUrl -Method Put -Headers $rabbitHeaders -ContentType 'application/json' -Body '{"durable":true,"auto_delete":false,"arguments":{}}'
    $queueCreated = $true
    $body = @{properties=@{};routing_key=$queue;payload='flashgo-smoke';payload_encoding='string'} | ConvertTo-Json
    $published = Invoke-RestMethod "$rabbitBase/exchanges/%2Fflashgo/amq.default/publish" -Method Post -Headers $rabbitHeaders -ContentType 'application/json' -Body $body
    if (!$published.routed) { throw 'RabbitMQ did not route the test message' }
    $messages = Invoke-RestMethod "$queueUrl/get" -Method Post -Headers $rabbitHeaders -ContentType 'application/json' -Body '{"count":1,"ackmode":"ack_requeue_false","encoding":"auto","truncate":50000}'
    if ($messages[0].payload -ne 'flashgo-smoke') { throw 'RabbitMQ message mismatch' }
    Write-Host "RabbitMQ $($overview.rabbitmq_version): authenticated publish / consume passed"
} finally {
    if ($queueCreated) { $null = Invoke-RestMethod $queueUrl -Method Delete -Headers $rabbitHeaders }
}
