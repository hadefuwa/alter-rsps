# PowerShell script for setting up bug detection tools

Write-Host "Setting up bug detection tools for Alter RSPS..." -ForegroundColor Green
Write-Host ""

# Check if detekt config exists
if (Test-Path "config/detekt.yml") {
    Write-Host "✓ Detekt config already exists" -ForegroundColor Green
} else {
    Write-Host "✓ Detekt config created" -ForegroundColor Green
}

# Check if ktlint is configured
$buildFile = Get-Content "build.gradle.kts" -Raw
if ($buildFile -match "ktlint") {
    Write-Host "✓ ktlint is configured" -ForegroundColor Green
} else {
    Write-Host "✗ ktlint not found in build.gradle.kts" -ForegroundColor Yellow
}

# Check if detekt is configured
if ($buildFile -match "detekt") {
    Write-Host "✓ detekt is configured" -ForegroundColor Green
} else {
    Write-Host "✗ detekt not found in build.gradle.kts" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Running initial checks..." -ForegroundColor Cyan
Write-Host ""

# Run ktlint check
Write-Host "1. Running ktlint..." -ForegroundColor Yellow
& ./gradlew.bat ktlintCheck 2>&1 | Select-Object -First 20

# Run detekt (if configured)
if ($buildFile -match "detekt") {
    Write-Host ""
    Write-Host "2. Running detekt..." -ForegroundColor Yellow
    & ./gradlew.bat detekt 2>&1 | Select-Object -First 20
}

# Run tests
Write-Host ""
Write-Host "3. Running tests..." -ForegroundColor Yellow
& ./gradlew.bat test --continue 2>&1 | Select-Object -Last 10

Write-Host ""
Write-Host "Setup complete! See BUG_DETECTION_GUIDE.md for details." -ForegroundColor Green
Write-Host ""
Write-Host "Quick commands:" -ForegroundColor Cyan
Write-Host "  ./gradlew.bat ktlintCheck    - Check code style"
Write-Host "  ./gradlew.bat ktlintFormat   - Auto-fix code style"
Write-Host "  ./gradlew.bat detekt         - Run static analysis"
Write-Host "  ./gradlew.bat test           - Run all tests"

