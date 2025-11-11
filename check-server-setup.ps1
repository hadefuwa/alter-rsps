# Server Setup Diagnostic Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Alter RSPS Setup Diagnostic" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$errors = @()
$warnings = @()

# Check required files
Write-Host "Checking required files..." -ForegroundColor Yellow

if (-not (Test-Path "data\xteas.json")) {
    $errors += "Missing: data\xteas.json"
    Write-Host "[ERROR] Missing: data\xteas.json" -ForegroundColor Red
} else {
    Write-Host "[OK] data\xteas.json exists" -ForegroundColor Green
}

if (-not (Test-Path "data\cache\main_file_cache.dat2")) {
    $errors += "Missing: data\cache\main_file_cache.dat2"
    Write-Host "[ERROR] Missing: data\cache\main_file_cache.dat2" -ForegroundColor Red
} else {
    Write-Host "[OK] Cache dat2 file exists" -ForegroundColor Green
}

if (-not (Test-Path "game.yml")) {
    $errors += "Missing: game.yml"
    Write-Host "[ERROR] Missing: game.yml" -ForegroundColor Red
} else {
    Write-Host "[OK] game.yml exists" -ForegroundColor Green
}

if (-not (Test-Path "dev-settings.yml")) {
    $warnings += "Missing: dev-settings.yml (optional)"
    Write-Host "[WARNING] Missing: dev-settings.yml (optional)" -ForegroundColor Yellow
} else {
    Write-Host "[OK] dev-settings.yml exists" -ForegroundColor Green
}

# Check RSA keys
Write-Host ""
Write-Host "Checking RSA keys..." -ForegroundColor Yellow

if (-not (Test-Path "data\rsa\key.pem")) {
    $warnings += "Missing: data\rsa\key.pem (will be generated on first run)"
    Write-Host "[WARNING] Missing: data\rsa\key.pem" -ForegroundColor Yellow
    Write-Host "  This will be generated automatically when you run the install task" -ForegroundColor Gray
} else {
    Write-Host "[OK] RSA key exists" -ForegroundColor Green
}

# Check Java version
Write-Host ""
Write-Host "Checking Java version..." -ForegroundColor Yellow

try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match "17") {
        Write-Host "[OK] Java 17 detected" -ForegroundColor Green
    } else {
        $warnings += "Java version may not be 17"
        Write-Host "[WARNING] Java version: $javaVersion" -ForegroundColor Yellow
        Write-Host "  Recommended: Java 17" -ForegroundColor Gray
    }
} catch {
    $errors += "Java not found in PATH"
    Write-Host "[ERROR] Java not found in PATH" -ForegroundColor Red
}

# Check port availability
Write-Host ""
Write-Host "Checking port 43594..." -ForegroundColor Yellow

$portInUse = Get-NetTCPConnection -LocalPort 43594 -ErrorAction SilentlyContinue
if ($portInUse) {
    $warnings += "Port 43594 is already in use"
    Write-Host "[WARNING] Port 43594 is already in use" -ForegroundColor Yellow
    Write-Host "  You may need to stop another application or change the port in game.yml" -ForegroundColor Gray
} else {
    Write-Host "[OK] Port 43594 is available" -ForegroundColor Green
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($errors.Count -eq 0) {
    Write-Host "Setup looks good!" -ForegroundColor Green
    if ($warnings.Count -gt 0) {
        Write-Host ""
        Write-Host "Warnings:" -ForegroundColor Yellow
        foreach ($warning in $warnings) {
            Write-Host "  - $warning" -ForegroundColor Yellow
        }
    }
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Run the install task in IntelliJ:" -ForegroundColor White
    Write-Host "   Gradle -> Alter -> game-server -> Tasks -> other -> install" -ForegroundColor Gray
    Write-Host "2. Then run the server:" -ForegroundColor White
    Write-Host "   Gradle -> Alter -> game-server -> Tasks -> application -> run" -ForegroundColor Gray
} else {
    Write-Host "Found $($errors.Count) error(s):" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Please fix these errors before running the server." -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Cyan

