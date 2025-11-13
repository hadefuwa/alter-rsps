# RSProx RuneLite Plugin Persistence Quick Fix
# This script ONLY affects the RSProx/Alter profile, not your other RuneLite installations
# Run this script as Administrator for best results

Write-Host "RSProx RuneLite Plugin Persistence Quick Fix" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "This script ONLY affects RSProx/Alter profile settings" -ForegroundColor Yellow
Write-Host ""

$runeliteDir = Join-Path $env:USERPROFILE ".runelite"
$profilesDir = Join-Path $runeliteDir "profiles"

# RSProx/Alter profile name - adjust if your profile has a different name
$rsproxProfileNames = @("Alter", "rsprox", "RSProx", "Private Server")

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

# Find RSProx profile
Write-Host "Looking for RSProx/Alter profile..." -ForegroundColor Yellow
$rsproxProfileFound = $false
$rsproxProfilePath = $null

if (Test-Path $profilesDir) {
    $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
    foreach ($profileDir in $profileDirs) {
        foreach ($profileName in $rsproxProfileNames) {
            if ($profileDir.Name -eq $profileName) {
                $rsproxProfilePath = $profileDir.FullName
                $rsproxProfileFound = $true
                Write-Host "Found RSProx profile: $($profileDir.Name)" -ForegroundColor Green
                break
            }
        }
        if ($rsproxProfileFound) { break }
    }
}

if (-not $rsproxProfileFound) {
    Write-Host "WARNING: RSProx profile not found!" -ForegroundColor Yellow
    Write-Host "Available profiles:" -ForegroundColor Yellow
    if (Test-Path $profilesDir) {
        $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
        foreach ($profileDir in $profileDirs) {
            Write-Host "  - $($profileDir.Name)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  No profiles directory found (using default profile)" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "To create an RSProx profile:" -ForegroundColor Cyan
    Write-Host "  1. Launch RuneLite" -ForegroundColor Cyan
    Write-Host "  2. Go to Configuration > Profiles" -ForegroundColor Cyan
    Write-Host "  3. Create a new profile named 'Alter' or 'rsprox'" -ForegroundColor Cyan
    Write-Host "  4. Switch to that profile when using RSProx" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "This script will now exit. Please create the profile first." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit
}

# Fix RSProx profile settings
Write-Host "Fixing RSProx profile settings..." -ForegroundColor Yellow
$profileSettings = Join-Path $rsproxProfilePath "settings.properties"

if (Test-Path $profileSettings) {
    # Backup
    Write-Host "Backing up profile settings..." -ForegroundColor Yellow
    $backupFile = "$profileSettings.backup.$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Copy-Item $profileSettings $backupFile -ErrorAction SilentlyContinue
    Write-Host "Backup created: $backupFile" -ForegroundColor Green
    
    # Remove read-only attribute
    Set-ItemProperty -Path $profileSettings -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
    Write-Host "Removed read-only attribute from profile settings" -ForegroundColor Green
    
    # Ensure directory is writable
    try {
        $acl = Get-Acl $rsproxProfilePath
        $user = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
        $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($user, "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
        $acl.SetAccessRule($accessRule)
        Set-Acl -Path $rsproxProfilePath -AclObject $acl
        Write-Host "Fixed profile directory permissions" -ForegroundColor Green
    } catch {
        Write-Host "Could not fix profile directory permissions: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "Profile settings file doesn't exist yet (will be created when you launch RuneLite with this profile)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "RSProx profile fix completed!" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: Make sure you're using the RSProx profile when launching RuneLite:" -ForegroundColor Yellow
Write-Host "  1. Launch RuneLite" -ForegroundColor Yellow
Write-Host "  2. Go to Configuration > Profiles" -ForegroundColor Yellow
Write-Host "  3. Select the '$([System.IO.Path]::GetFileName($rsproxProfilePath))' profile" -ForegroundColor Yellow
Write-Host "  4. Configure your plugins" -ForegroundColor Yellow
Write-Host "  5. Restart RuneLite and verify settings persist" -ForegroundColor Yellow
Write-Host ""
Write-Host "NOTE: Your other RuneLite installations and profiles are NOT affected by this script." -ForegroundColor Green
Write-Host ""
Read-Host "Press Enter to exit"


