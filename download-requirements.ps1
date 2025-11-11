# Alter RSPS - Download Requirements Script
# This script downloads the required cache files and xteas.json

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Alter RSPS - Download Requirements" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "https://archive.openrs2.org/caches/runescape/2038"
$dataDir = "data"
$cacheDir = "$dataDir\cache"

# Create directories if they don't exist
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir | Out-Null
    Write-Host "Created directory: $dataDir" -ForegroundColor Green
}

if (-not (Test-Path $cacheDir)) {
    New-Item -ItemType Directory -Path $cacheDir | Out-Null
    Write-Host "Created directory: $cacheDir" -ForegroundColor Green
}

# Download xteas.json
Write-Host "Downloading xteas.json..." -ForegroundColor Yellow
$xteasUrl = "$baseUrl/keys.json"
$xteasPath = "$dataDir\xteas.json"

try {
    Invoke-WebRequest -Uri $xteasUrl -OutFile $xteasPath -ErrorAction Stop
    Write-Host "[OK] Downloaded xteas.json successfully" -ForegroundColor Green
}
catch {
    Write-Host "[ERROR] Failed to download xteas.json: $_" -ForegroundColor Red
    exit 1
}

# Download cache ZIP
Write-Host ""
Write-Host "Downloading cache files (this may take a while)..." -ForegroundColor Yellow
$cacheZipUrl = "$baseUrl/disk.zip"
$tempZip = "cache-temp.zip"

try {
    Invoke-WebRequest -Uri $cacheZipUrl -OutFile $tempZip -ErrorAction Stop
    Write-Host "[OK] Downloaded cache ZIP successfully" -ForegroundColor Green
}
catch {
    Write-Host "[ERROR] Failed to download cache ZIP: $_" -ForegroundColor Red
    exit 1
}

# Extract cache files
Write-Host ""
Write-Host "Extracting cache files..." -ForegroundColor Yellow
$tempExtract = "temp-cache-extract"
$extractionSuccess = $false

try {
    Expand-Archive -Path $tempZip -DestinationPath $tempExtract -Force -ErrorAction Stop
    
    # Find the cache folder in the extracted files
    $extractedCache = Get-ChildItem -Path $tempExtract -Recurse -Directory -Filter "cache" | Select-Object -First 1
    
    if ($null -eq $extractedCache) {
        Write-Host "[ERROR] Could not find cache folder in extracted ZIP" -ForegroundColor Red
        Write-Host "Please manually extract the cache folder from: $tempZip" -ForegroundColor Yellow
    }
    else {
        Write-Host "Found cache folder at: $($extractedCache.FullName)" -ForegroundColor Cyan
        
        # Copy all cache files
        Copy-Item -Path "$($extractedCache.FullName)\*" -Destination $cacheDir -Recurse -Force -ErrorAction Stop
        Write-Host "[OK] Extracted cache files successfully" -ForegroundColor Green
        $extractionSuccess = $true
        
        # Verify required files exist
        $requiredFiles = @(
            "main_file_cache.dat2",
            "main_file_cache.idx0",
            "main_file_cache.idx1",
            "main_file_cache.idx2",
            "main_file_cache.idx3",
            "main_file_cache.idx4",
            "main_file_cache.idx5",
            "main_file_cache.idx7",
            "main_file_cache.idx8",
            "main_file_cache.idx9",
            "main_file_cache.idx10",
            "main_file_cache.idx11",
            "main_file_cache.idx12",
            "main_file_cache.idx13",
            "main_file_cache.idx14",
            "main_file_cache.idx15",
            "main_file_cache.idx17",
            "main_file_cache.idx18",
            "main_file_cache.idx19",
            "main_file_cache.idx20",
            "main_file_cache.idx255"
        )
        
        $missingFiles = @()
        foreach ($file in $requiredFiles) {
            if (-not (Test-Path "$cacheDir\$file")) {
                $missingFiles += $file
            }
        }
        
        if ($missingFiles.Count -eq 0) {
            Write-Host "[OK] All required cache files are present" -ForegroundColor Green
        }
        else {
            Write-Host "[WARNING] Some required files are missing:" -ForegroundColor Yellow
            foreach ($file in $missingFiles) {
                Write-Host "  - $file" -ForegroundColor Yellow
            }
        }
    }
}
catch {
    Write-Host "[ERROR] Failed to extract cache files: $_" -ForegroundColor Red
    Write-Host "Please manually extract $tempZip to $cacheDir" -ForegroundColor Yellow
}
finally {
    # Cleanup temp files
    if (Test-Path $tempZip) {
        Remove-Item $tempZip -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $tempExtract) {
        Remove-Item $tempExtract -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Download Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Build the project: .\gradlew.bat :game-server:build" -ForegroundColor White
Write-Host "2. Run install: .\gradlew.bat :game-server:install" -ForegroundColor White
Write-Host "3. Start server: .\gradlew.bat :game-server:run" -ForegroundColor White
Write-Host ""
