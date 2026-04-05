$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$jar = ".\lib\mysql-connector-j-8.4.0.jar"
$src = ".\gamersync\api\GamerSyncHttpServer.java"

if (-not (Test-Path $jar)) {
    Write-Host "Missing JDBC jar: $jar" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $src)) {
    Write-Host "Missing source file: $src" -ForegroundColor Red
    exit 1
}

Write-Host "Compiling GamerSyncHttpServer..." -ForegroundColor Cyan
javac -cp ".;lib/mysql-connector-j-8.4.0.jar" $src
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Starting API on http://localhost:8080 ..." -ForegroundColor Green
java -cp ".;lib/mysql-connector-j-8.4.0.jar" gamersync.api.GamerSyncHttpServer
