. "$PSScriptRoot/dev-env.ps1"
if (!(Test-LocalPort 3306)) { throw 'Start the MySQL91 service on port 3306 first.' }
if (!$Secrets.PSObject.Properties['FLASHGO_DEV_PASSWORD']) {
    $Secrets | Add-Member -NotePropertyName FLASHGO_DEV_PASSWORD -NotePropertyValue ([guid]::NewGuid().ToString('N'))
    $Secrets | ConvertTo-Json | Set-Content "$LocalRoot/env.json"
}
$env:FLASHGO_DEV_PASSWORD = $Secrets.FLASHGO_DEV_PASSWORD
$cryptoJar = Get-ChildItem "$env:USERPROFILE/.m2/repository/org/springframework/security/spring-security-crypto/6.3.0" -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName
if (!$cryptoJar) { throw 'Build the project first to download BCrypt.' }
'class HashPassword { public static void main(String[] args) { System.out.print(org.springframework.security.crypto.bcrypt.BCrypt.hashpw(System.getenv("FLASHGO_DEV_PASSWORD"), org.springframework.security.crypto.bcrypt.BCrypt.gensalt())); } }' | Set-Content "$LocalRoot/HashPassword.java" -Encoding ascii
$hash = & java --class-path $cryptoJar "$LocalRoot/HashPassword.java"
Assert-Exit 'BCrypt password hashing'
if ($hash -notmatch '^\$2[aby]\$') { throw 'Invalid BCrypt output' }
$sql = @"
USE flashgo;
START TRANSACTION;
INSERT INTO app_user(user_no,username,nickname,mobile,password_hash) VALUES ('LOCAL_USER_V1','13800000001','Local User','13800000001','$hash') ON DUPLICATE KEY UPDATE user_no=user_no;
INSERT INTO merchant(merchant_no,username,name,contact_name,contact_mobile,password_hash) VALUES ('LOCAL_MERCHANT_V1','13800000002','Local Merchant','Local Contact','13800000002','$hash') ON DUPLICATE KEY UPDATE merchant_no=merchant_no;
INSERT INTO rider(rider_no,username,name,mobile,password_hash) VALUES ('LOCAL_RIDER_V1','13800000003','Local Rider','13800000003','$hash') ON DUPLICATE KEY UPDATE rider_no=rider_no;
INSERT INTO platform_account(username,password_hash,display_name) VALUES ('local_admin','$hash','Local Admin') ON DUPLICATE KEY UPDATE username=username;
COMMIT;
"@
$env:MYSQL_PWD = $Secrets.FLASHGO_DB_PASSWORD
try {
    $sql | & "$MysqlBin/mysql.exe" --no-defaults -h 127.0.0.1 -P 3306 -u $env:FLASHGO_DB_USER --default-character-set=utf8mb4
    Assert-Exit 'Local seed data'
} finally { Remove-Item Env:MYSQL_PWD }
Write-Host 'Local accounts ready. Password is FLASHGO_DEV_PASSWORD in .local/env.json. Existing accounts are not overwritten.'
