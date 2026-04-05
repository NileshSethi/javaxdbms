$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$port = 5500

if (Get-Command py -ErrorAction SilentlyContinue) {
    Write-Host "Starting UI server on http://localhost:$port ..." -ForegroundColor Green
    py -m http.server $port
    exit $LASTEXITCODE
}

if (Get-Command python -ErrorAction SilentlyContinue) {
    Write-Host "Starting UI server on http://localhost:$port ..." -ForegroundColor Green
    python -m http.server $port
    exit $LASTEXITCODE
}

Write-Host "Python not found. Install Python 3 and retry." -ForegroundColor Red
exit 1
