# Quick Fix Script for RuneLite Plugin Persistence Issues
# WARNING: This script affects ALL RuneLite profiles and installations
# For RSProx-specific fixes, use: quick-fix-rsprox-profile.ps1
# Run this script as Administrator for best results

Write-Host "RuneLite Plugin Persistence Quick Fix" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "WARNING: This script affects ALL RuneLite profiles!" -ForegroundColor Yellow
Write-Host "For RSProx-only fixes, use: quick-fix-rsprox-profile.ps1" -ForegroundColor Yellow
Write-Host ""

$runeliteDir = Join-Path $env:USERPROFILE ".runelite"
$settingsFile = Join-Path $runeliteDir "settings.properties"

# Check if running as administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "WARNING: Not running as Administrator. Some fixes may not work." -ForegroundColor Yellow
    Write-Host "Right-click PowerShell and select 'Run as Administrator' for full fix." -ForegroundColor Yellow
    Write-Host ""
}

# Close RuneLite if running
Write-Host "Checking if RuneLite is running..." -ForegroundColor Yellow
$runeliteProcesses = Get-Process -Name "RuneLite" -ErrorAction SilentlyContinue
if ($runeliteProcesses) {
    Write-Host "RuneLite is running. Closing it..." -ForegroundColor Yellow
    Stop-Process -Name "RuneLite" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "RuneLite closed." -ForegroundColor Green
}

# Ensure .runelite directory exists and has correct permissions
Write-Host "Fixing .runelite directory..." -ForegroundColor Yellow
if (-not (Test-Path $runeliteDir)) {
    New-Item -ItemType Directory -Path $runeliteDir -Force | Out-Null
    Write-Host "Created .runelite directory" -ForegroundColor Green
}

# Fix directory permissions
try {
    $acl = Get-Acl $runeliteDir
    $user = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($user, "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
    $acl.SetAccessRule($accessRule)
    Set-Acl -Path $runeliteDir -AclObject $acl
    Write-Host "Fixed directory permissions" -ForegroundColor Green
} catch {
    Write-Host "Could not fix permissions: $_" -ForegroundColor Red
}

# Backup and fix settings.properties if it exists
if (Test-Path $settingsFile) {
    Write-Host "Backing up settings.properties..." -ForegroundColor Yellow
    $backupFile = "$settingsFile.backup.$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Copy-Item $settingsFile $backupFile -ErrorAction SilentlyContinue
    Write-Host "Backup created: $backupFile" -ForegroundColor Green
    
    # Remove read-only attribute
    Set-ItemProperty -Path $settingsFile -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
    Write-Host "Removed read-only attribute" -ForegroundColor Green
}

# Fix profile-related issues
Write-Host "Checking profiles..." -ForegroundColor Yellow
$profilesDir = Join-Path $runeliteDir "profiles"
if (Test-Path $profilesDir) {
    $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
    foreach ($profileDir in $profileDirs) {
        $profileSettings = Join-Path $profileDir.FullName "settings.properties"
        if (Test-Path $profileSettings) {
            try {
                Set-ItemProperty -Path $profileSettings -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
                Write-Host "Fixed permissions for profile: $($profileDir.Name)" -ForegroundColor Green
            } catch {
                Write-Host "Could not fix profile '$($profileDir.Name)': $_" -ForegroundColor Yellow
            }
        }
    }
}

# Check profiles.properties
$profilesFile = Join-Path $runeliteDir "profiles.properties"
if (Test-Path $profilesFile) {
    Set-ItemProperty -Path $profilesFile -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
    Write-Host "Fixed profiles.properties permissions" -ForegroundColor Green
}

Write-Host ""
Write-Host "Quick fix completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Launch RuneLite" -ForegroundColor Yellow
Write-Host "  2. Check for duplicate RSProfiles at: https://runelite.net/account/home" -ForegroundColor Yellow
Write-Host "  3. Verify profile selection in RuneLite (Configuration > Profiles)" -ForegroundColor Yellow
Write-Host "  4. Configure your plugins" -ForegroundColor Yellow
Write-Host "  5. Restart RuneLite to verify settings persist" -ForegroundColor Yellow
Write-Host ""
Write-Host "If issues persist, run the full diagnostic script: .\fix-runelite-plugins.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "For private server (rsprox) users:" -ForegroundColor Cyan
Write-Host "  - Ensure bootstrap.json is accessible and valid" -ForegroundColor Cyan
Write-Host "  - Check that RuneLite is connecting to the correct server" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit"

