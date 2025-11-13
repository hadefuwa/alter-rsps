# RuneLite Plugin Persistence Diagnostic and Fix Script
# WARNING: This script affects ALL RuneLite profiles and installations
# For RSProx-specific diagnostics, use: fix-rsprox-profile.ps1
# This script diagnoses and fixes common issues with RuneLite plugins not persisting settings

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RuneLite Plugin Persistence Diagnostic" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "WARNING: This script affects ALL RuneLite profiles!" -ForegroundColor Yellow
Write-Host "For RSProx-only diagnostics, use: fix-rsprox-profile.ps1" -ForegroundColor Yellow
Write-Host ""

$runeliteDir = Join-Path $env:USERPROFILE ".runelite"
$settingsFile = Join-Path $runeliteDir "settings.properties"
$issuesFound = @()
$fixesApplied = @()

# Function to check if a path is writable
function Test-PathWritable {
    param([string]$Path)
    
    try {
        if (-not (Test-Path $Path)) {
            return $false
        }
        
        $testFile = Join-Path $Path "test_write_permission.tmp"
        try {
            "test" | Out-File -FilePath $testFile -ErrorAction Stop
            Remove-Item $testFile -ErrorAction SilentlyContinue
            return $true
        } catch {
            return $false
        }
    } catch {
        return $false
    }
}

# Function to fix directory permissions
function Fix-DirectoryPermissions {
    param([string]$Path)
    
    try {
        if (-not (Test-Path $Path)) {
            New-Item -ItemType Directory -Path $Path -Force | Out-Null
            $fixesApplied += "Created directory: $Path"
            return $true
        }
        
        $acl = Get-Acl $Path
        $user = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
        $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($user, "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
        $acl.SetAccessRule($accessRule)
        Set-Acl -Path $Path -AclObject $acl
        $fixesApplied += "Fixed permissions for: $Path"
        return $true
    } catch {
        Write-Host "  ERROR: Could not fix permissions: $_" -ForegroundColor Red
        return $false
    }
}

# Check 1: Verify .runelite directory exists
Write-Host "[1/6] Checking .runelite directory..." -ForegroundColor Yellow
if (-not (Test-Path $runeliteDir)) {
    Write-Host "  ISSUE: .runelite directory does not exist at: $runeliteDir" -ForegroundColor Red
    $issuesFound += ".runelite directory missing"
    
    Write-Host "  Attempting to create directory..." -ForegroundColor Yellow
    if (Fix-DirectoryPermissions -Path $runeliteDir) {
        Write-Host "  SUCCESS: Created .runelite directory" -ForegroundColor Green
    } else {
        Write-Host "  FAILED: Could not create directory. Please run as administrator." -ForegroundColor Red
    }
} else {
    Write-Host "  OK: .runelite directory exists" -ForegroundColor Green
}

# Check 2: Verify directory permissions
Write-Host "[2/6] Checking directory permissions..." -ForegroundColor Yellow
if (Test-Path $runeliteDir) {
    if (-not (Test-PathWritable -Path $runeliteDir)) {
        Write-Host "  ISSUE: Cannot write to .runelite directory" -ForegroundColor Red
        $issuesFound += "Directory not writable"
        
        Write-Host "  Attempting to fix permissions..." -ForegroundColor Yellow
        if (Fix-DirectoryPermissions -Path $runeliteDir) {
            Write-Host "  SUCCESS: Fixed directory permissions" -ForegroundColor Green
        } else {
            Write-Host "  FAILED: Could not fix permissions. Please run as administrator." -ForegroundColor Red
        }
    } else {
        Write-Host "  OK: Directory is writable" -ForegroundColor Green
    }
}

# Check 3: Check settings.properties file
Write-Host "[3/6] Checking settings.properties file..." -ForegroundColor Yellow
if (-not (Test-Path $settingsFile)) {
    Write-Host "  ISSUE: settings.properties file does not exist" -ForegroundColor Red
    $issuesFound += "settings.properties missing"
    
    Write-Host "  This file will be created automatically when RuneLite starts." -ForegroundColor Yellow
    Write-Host "  If it doesn't appear, there may be a permission issue." -ForegroundColor Yellow
} else {
    Write-Host "  OK: settings.properties exists" -ForegroundColor Green
    
    # Check if file is readable
    try {
        $content = Get-Content $settingsFile -ErrorAction Stop
        Write-Host "  OK: File is readable" -ForegroundColor Green
        
        # Check if file appears corrupted (empty or invalid format)
        if ($content.Count -eq 0) {
            Write-Host "  WARNING: File is empty (may be corrupted)" -ForegroundColor Yellow
            $issuesFound += "settings.properties is empty"
        } else {
            # Check for common corruption patterns
            $hasValidEntries = $false
            foreach ($line in $content) {
                if ($line -match '^[^#].*=') {
                    $hasValidEntries = $true
                    break
                }
            }
            
            if (-not $hasValidEntries) {
                Write-Host "  WARNING: File appears to have no valid entries" -ForegroundColor Yellow
                $issuesFound += "settings.properties appears corrupted"
            }
        }
    } catch {
        Write-Host "  ISSUE: Cannot read settings.properties: $_" -ForegroundColor Red
        $issuesFound += "Cannot read settings.properties"
    }
    
    # Check if file is writable
    try {
        $fileInfo = Get-Item $settingsFile
        $fileInfo.IsReadOnly = $false
        Set-ItemProperty -Path $settingsFile -Name IsReadOnly -Value $false -ErrorAction Stop
        Write-Host "  OK: File is writable" -ForegroundColor Green
    } catch {
        Write-Host "  ISSUE: File may be read-only or locked" -ForegroundColor Red
        $issuesFound += "settings.properties not writable"
    }
}

# Check 4: Check for multiple RuneLite installations
Write-Host "[4/6] Checking for multiple RuneLite installations..." -ForegroundColor Yellow
$runeliteProcesses = Get-Process -Name "RuneLite" -ErrorAction SilentlyContinue
$runeliteInstalls = @()

# Check common installation locations
$commonPaths = @(
    "$env:LOCALAPPDATA\RuneLite",
    "$env:APPDATA\RuneLite",
    "$env:ProgramFiles\RuneLite",
    "$env:ProgramFiles(x86)\RuneLite",
    "$env:USERPROFILE\AppData\Local\RuneLite"
)

foreach ($path in $commonPaths) {
    if (Test-Path $path) {
        $runeliteInstalls += $path
    }
}

if ($runeliteInstalls.Count -gt 1) {
    Write-Host "  WARNING: Found multiple RuneLite installation directories:" -ForegroundColor Yellow
    foreach ($install in $runeliteInstalls) {
        Write-Host "    - $install" -ForegroundColor Yellow
    }
    $issuesFound += "Multiple RuneLite installations detected"
    Write-Host "  RECOMMENDATION: Uninstall duplicate installations and keep only one." -ForegroundColor Yellow
} else {
    if ($runeliteInstalls.Count -eq 1) {
        Write-Host "  OK: Found single installation at: $($runeliteInstalls[0])" -ForegroundColor Green
    } else {
        Write-Host "  INFO: Could not detect RuneLite installation directory (this is OK if using portable version)" -ForegroundColor Yellow
    }
}

# Check 5: Check if RuneLite is currently running
Write-Host "[5/6] Checking if RuneLite is running..." -ForegroundColor Yellow
if ($runeliteProcesses) {
    Write-Host "  WARNING: RuneLite is currently running. Please close it before applying fixes." -ForegroundColor Yellow
    Write-Host "  Running processes: $($runeliteProcesses.Count)" -ForegroundColor Yellow
    $issuesFound += "RuneLite is running (close before fixing)"
} else {
    Write-Host "  OK: RuneLite is not running" -ForegroundColor Green
}

# Check 6: Check for profile-related issues
Write-Host "[6/8] Checking RuneLite profiles..." -ForegroundColor Yellow
$profilesDir = Join-Path $runeliteDir "profiles"
$profilesFile = Join-Path $runeliteDir "profiles.properties"

if (Test-Path $profilesDir) {
    $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
    if ($profileDirs) {
        Write-Host "  INFO: Found $($profileDirs.Count) profile(s)" -ForegroundColor Yellow
        foreach ($profileDir in $profileDirs) {
            $profileSettings = Join-Path $profileDir.FullName "settings.properties"
            if (Test-Path $profileSettings) {
                try {
                    $profileContent = Get-Content $profileSettings -ErrorAction Stop
                    if ($profileContent.Count -eq 0) {
                        Write-Host "  WARNING: Profile '$($profileDir.Name)' has empty settings file" -ForegroundColor Yellow
                        $issuesFound += "Profile '$($profileDir.Name)' settings file is empty"
                    }
                } catch {
                    Write-Host "  WARNING: Cannot read profile '$($profileDir.Name)' settings" -ForegroundColor Yellow
                }
            }
        }
    }
} else {
    Write-Host "  INFO: No profiles directory found (this is OK for default profile)" -ForegroundColor Yellow
}

# Check for profile properties file
if (Test-Path $profilesFile) {
    try {
        $null = Get-Content $profilesFile -ErrorAction Stop
        Write-Host "  OK: profiles.properties exists" -ForegroundColor Green
    } catch {
        Write-Host "  WARNING: Cannot read profiles.properties" -ForegroundColor Yellow
        $issuesFound += "Cannot read profiles.properties"
    }
}

# Check 7: Check for bootstrap.json related issues (for private servers)
Write-Host "[7/8] Checking for bootstrap.json configuration issues..." -ForegroundColor Yellow
$bootstrapPaths = @(
    "bootstrap.json",
    (Join-Path (Get-Location) "bootstrap.json"),
    (Join-Path $env:USERPROFILE "bootstrap.json")
)

$bootstrapFound = $false
foreach ($bootstrapPath in $bootstrapPaths) {
    if (Test-Path $bootstrapPath) {
        Write-Host "  INFO: Found bootstrap.json at: $bootstrapPath" -ForegroundColor Yellow
        $bootstrapFound = $true
        try {
            $bootstrapContent = Get-Content $bootstrapPath -Raw -ErrorAction Stop | ConvertFrom-Json
            if ($bootstrapContent.artifacts) {
                Write-Host "  INFO: Bootstrap.json appears valid" -ForegroundColor Green
            }
        } catch {
            Write-Host "  WARNING: bootstrap.json may be invalid or corrupted" -ForegroundColor Yellow
            $issuesFound += "bootstrap.json may be corrupted"
        }
        break
    }
}

if (-not $bootstrapFound) {
    Write-Host "  INFO: No bootstrap.json found (this is OK for standard RuneLite)" -ForegroundColor Yellow
}

# Check 8: Check for antivirus/firewall interference
Write-Host "[8/8] Checking for potential interference..." -ForegroundColor Yellow
$windowsDefender = Get-MpPreference -ErrorAction SilentlyContinue
if ($windowsDefender) {
    Write-Host "  INFO: Windows Defender is active. Consider adding RuneLite to exclusions if issues persist." -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostic Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($issuesFound.Count -eq 0) {
    Write-Host "No issues detected! Your RuneLite configuration appears to be healthy." -ForegroundColor Green
    Write-Host ""
    Write-Host "If plugins still don't persist, try:" -ForegroundColor Yellow
    Write-Host "  1. Check for duplicate RSProfiles at: https://runelite.net/account/home" -ForegroundColor Yellow
    Write-Host "  2. Run RuneLite in safe mode: RuneLite (safe mode) shortcut" -ForegroundColor Yellow
    Write-Host "  3. Disable third-party plugins one by one to identify conflicts" -ForegroundColor Yellow
    Write-Host "  4. Check RuneLite logs at: $runeliteDir\logs" -ForegroundColor Yellow
    Write-Host "  5. Verify you're using the correct profile in RuneLite (Configuration > Profiles)" -ForegroundColor Yellow
} else {
    Write-Host "Issues found: $($issuesFound.Count)" -ForegroundColor Red
    foreach ($issue in $issuesFound) {
        Write-Host "  - $issue" -ForegroundColor Red
    }
    Write-Host ""
    
    if ($fixesApplied.Count -gt 0) {
        Write-Host "Fixes applied:" -ForegroundColor Green
        foreach ($fix in $fixesApplied) {
            Write-Host "  + $fix" -ForegroundColor Green
        }
        Write-Host ""
        Write-Host "Please restart RuneLite and check if the issue is resolved." -ForegroundColor Yellow
    } else {
        Write-Host "RECOMMENDED ACTIONS:" -ForegroundColor Yellow
        Write-Host "  1. Close RuneLite if it's running" -ForegroundColor Yellow
        Write-Host "  2. Run this script as Administrator to fix permissions" -ForegroundColor Yellow
        Write-Host "  3. Check for duplicate RSProfiles at: https://runelite.net/account/home" -ForegroundColor Yellow
        Write-Host "  4. If settings.properties is corrupted, delete it and let RuneLite recreate it" -ForegroundColor Yellow
        Write-Host "  5. Check Windows Defender/Firewall exclusions for RuneLite" -ForegroundColor Yellow
        Write-Host "  6. Verify profile selection in RuneLite (Configuration > Profiles)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "To fix settings.properties corruption:" -ForegroundColor Cyan
        Write-Host "  Backup: Copy $settingsFile to $settingsFile.backup" -ForegroundColor Cyan
        Write-Host "  Delete: Remove $settingsFile" -ForegroundColor Cyan
        Write-Host "  Restart: Launch RuneLite to recreate the file" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "For private server (rsprox) users:" -ForegroundColor Cyan
        Write-Host "  - Ensure bootstrap.json is accessible and valid" -ForegroundColor Cyan
        Write-Host "  - Check that RuneLite is connecting to the correct server" -ForegroundColor Cyan
        Write-Host "  - Verify profile settings are not being reset by server configuration" -ForegroundColor Cyan
    }
}

Write-Host ""
Write-Host "Configuration directory: $runeliteDir" -ForegroundColor Cyan
Write-Host "Settings file: $settingsFile" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

