# RSProx RuneLite Plugin Persistence Diagnostic
# This script ONLY checks the RSProx/Alter profile, not your other RuneLite installations

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RSProx RuneLite Plugin Persistence Diagnostic" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "This script ONLY checks RSProx/Alter profile settings" -ForegroundColor Yellow
Write-Host ""

$runeliteDir = Join-Path $env:USERPROFILE ".runelite"
$profilesDir = Join-Path $runeliteDir "profiles"

# RSProx/Alter profile name - adjust if your profile has a different name
$rsproxProfileNames = @("Alter", "rsprox", "RSProx", "Private Server")

$issuesFound = @()
$fixesApplied = @()

# Find RSProx profile
Write-Host "[1/5] Looking for RSProx/Alter profile..." -ForegroundColor Yellow
$rsproxProfileFound = $false
$rsproxProfilePath = $null
$rsproxProfileName = $null

if (Test-Path $profilesDir) {
    $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
    foreach ($profileDir in $profileDirs) {
        foreach ($profileName in $rsproxProfileNames) {
            if ($profileDir.Name -eq $profileName) {
                $rsproxProfilePath = $profileDir.FullName
                $rsproxProfileName = $profileDir.Name
                $rsproxProfileFound = $true
                Write-Host "  OK: Found RSProx profile: $rsproxProfileName" -ForegroundColor Green
                break
            }
        }
        if ($rsproxProfileFound) { break }
    }
}

if (-not $rsproxProfileFound) {
    Write-Host "  ISSUE: RSProx profile not found!" -ForegroundColor Red
    $issuesFound += "RSProx profile not found"
    Write-Host ""
    Write-Host "Available profiles:" -ForegroundColor Yellow
    if (Test-Path $profilesDir) {
        $profileDirs = Get-ChildItem -Path $profilesDir -Directory -ErrorAction SilentlyContinue
        foreach ($profileDir in $profileDirs) {
            Write-Host "  - $($profileDir.Name)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  No profiles directory found" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "RECOMMENDATION: Create an RSProx profile in RuneLite:" -ForegroundColor Cyan
    Write-Host "  1. Launch RuneLite" -ForegroundColor Cyan
    Write-Host "  2. Go to Configuration > Profiles" -ForegroundColor Cyan
    Write-Host "  3. Create a new profile named 'Alter' or 'rsprox'" -ForegroundColor Cyan
    Write-Host "  4. Switch to that profile when using RSProx" -ForegroundColor Cyan
} else {
    # Check profile directory permissions
    Write-Host "[2/5] Checking profile directory permissions..." -ForegroundColor Yellow
    try {
        $testFile = Join-Path $rsproxProfilePath "test_write_permission.tmp"
        "test" | Out-File -FilePath $testFile -ErrorAction Stop
        Remove-Item $testFile -ErrorAction SilentlyContinue
        Write-Host "  OK: Profile directory is writable" -ForegroundColor Green
    } catch {
        Write-Host "  ISSUE: Cannot write to profile directory" -ForegroundColor Red
        $issuesFound += "Profile directory not writable"
        
        try {
            $acl = Get-Acl $rsproxProfilePath
            $user = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
            $accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule($user, "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
            $acl.SetAccessRule($accessRule)
            Set-Acl -Path $rsproxProfilePath -AclObject $acl
            Write-Host "  SUCCESS: Fixed profile directory permissions" -ForegroundColor Green
            $fixesApplied += "Fixed profile directory permissions"
        } catch {
            Write-Host "  FAILED: Could not fix permissions. Please run as administrator." -ForegroundColor Red
        }
    }
    
    # Check profile settings file
    Write-Host "[3/5] Checking profile settings file..." -ForegroundColor Yellow
    $profileSettings = Join-Path $rsproxProfilePath "settings.properties"
    
    if (-not (Test-Path $profileSettings)) {
        Write-Host "  INFO: Profile settings file doesn't exist yet (will be created when you launch RuneLite)" -ForegroundColor Yellow
    } else {
        Write-Host "  OK: Profile settings file exists" -ForegroundColor Green
        
        # Check if readable
        try {
            $content = Get-Content $profileSettings -ErrorAction Stop
            Write-Host "  OK: File is readable" -ForegroundColor Green
            
            if ($content.Count -eq 0) {
                Write-Host "  WARNING: File is empty (may be corrupted)" -ForegroundColor Yellow
                $issuesFound += "Profile settings file is empty"
            } else {
                $hasValidEntries = $false
                foreach ($line in $content) {
                    if ($line -match '^[^#].*=') {
                        $hasValidEntries = $true
                        break
                    }
                }
                if (-not $hasValidEntries) {
                    Write-Host "  WARNING: File appears to have no valid entries" -ForegroundColor Yellow
                    $issuesFound += "Profile settings file appears corrupted"
                }
            }
        } catch {
            Write-Host "  ISSUE: Cannot read profile settings file" -ForegroundColor Red
            $issuesFound += "Cannot read profile settings file"
        }
        
        # Check if writable
        try {
            Set-ItemProperty -Path $profileSettings -Name IsReadOnly -Value $false -ErrorAction Stop
            Write-Host "  OK: File is writable" -ForegroundColor Green
        } catch {
            Write-Host "  ISSUE: File may be read-only or locked" -ForegroundColor Red
            $issuesFound += "Profile settings file not writable"
        }
    }
    
    # Check bootstrap.json (for private servers)
    Write-Host "[4/5] Checking bootstrap.json..." -ForegroundColor Yellow
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
                    Write-Host "  OK: Bootstrap.json appears valid" -ForegroundColor Green
                }
            } catch {
                Write-Host "  WARNING: bootstrap.json may be invalid or corrupted" -ForegroundColor Yellow
                $issuesFound += "bootstrap.json may be corrupted"
            }
            break
        }
    }
    
    if (-not $bootstrapFound) {
        Write-Host "  INFO: No bootstrap.json found (this is OK if not using private server)" -ForegroundColor Yellow
    }
    
    # Check if RuneLite is running
    Write-Host "[5/5] Checking if RuneLite is running..." -ForegroundColor Yellow
    $runeliteProcesses = Get-Process -Name "RuneLite" -ErrorAction SilentlyContinue
    if ($runeliteProcesses) {
        Write-Host "  WARNING: RuneLite is currently running. Please close it before applying fixes." -ForegroundColor Yellow
        $issuesFound += "RuneLite is running (close before fixing)"
    } else {
        Write-Host "  OK: RuneLite is not running" -ForegroundColor Green
    }
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostic Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($issuesFound.Count -eq 0 -and $rsproxProfileFound) {
    Write-Host "No issues detected! Your RSProx profile configuration appears to be healthy." -ForegroundColor Green
    Write-Host ""
    Write-Host "IMPORTANT: Make sure you're using the '$rsproxProfileName' profile when launching RuneLite:" -ForegroundColor Yellow
    Write-Host "  1. Launch RuneLite" -ForegroundColor Yellow
    Write-Host "  2. Go to Configuration > Profiles" -ForegroundColor Yellow
    Write-Host "  3. Select the '$rsproxProfileName' profile" -ForegroundColor Yellow
    Write-Host "  4. Configure your plugins" -ForegroundColor Yellow
    Write-Host "  5. Restart RuneLite and verify settings persist" -ForegroundColor Yellow
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
        Write-Host "  3. Ensure you're using the '$rsproxProfileName' profile when launching RuneLite" -ForegroundColor Yellow
        Write-Host "  4. Check for duplicate RSProfiles at: https://runelite.net/account/home" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "NOTE: This script ONLY checked the RSProx profile. Your other RuneLite installations are NOT affected." -ForegroundColor Green
Write-Host "RSProx profile path: $rsproxProfilePath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")


