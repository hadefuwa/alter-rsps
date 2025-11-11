# Quick Start Guide

## ✅ What's Already Done

1. ✅ Created `game.yml` configuration file
2. ✅ Created `dev-settings.yml` configuration file
3. ✅ Verified project structure

## ⚠️ What You Need to Do

### 1. Download XTEAs File

**Download**: https://archive.openrs2.org/caches/runescape/2038/keys.json

**Save as**: `data/xteas.json`

**Quick PowerShell command** (run from project root):
```powershell
Invoke-WebRequest -Uri "https://archive.openrs2.org/caches/runescape/2038/keys.json" -OutFile "data\xteas.json"
```

### 2. Download and Extract Cache Files

**Download**: https://archive.openrs2.org/caches/runescape/2038/disk.zip

**Extract** the ZIP file and copy all contents from the `cache` folder inside the ZIP to `data/cache/`

**Quick PowerShell commands** (run from project root):
```powershell
# Download the cache ZIP
Invoke-WebRequest -Uri "https://archive.openrs2.org/caches/runescape/2038/disk.zip" -OutFile "cache-temp.zip"

# Extract to temp location
Expand-Archive -Path "cache-temp.zip" -DestinationPath "temp-cache" -Force

# Copy cache files to data/cache/
Copy-Item -Path "temp-cache\cache\*" -Destination "data\cache\" -Recurse -Force

# Cleanup
Remove-Item "cache-temp.zip" -Force
Remove-Item "temp-cache" -Recurse -Force
```

## 🚀 Next Steps

After downloading the required files:

1. **Build the project**:
   ```powershell
   .\gradlew.bat :game-server:build
   ```

2. **Run the install task** (generates RSA keys and decrypts maps):
   ```powershell
   .\gradlew.bat :game-server:install
   ```

3. **Start the server**:
   ```powershell
   .\gradlew.bat :game-server:run
   ```

Or use IntelliJ IDEA:
- Open Gradle panel → `Alter` → `game-server` → `Tasks` → `other` → `install`
- Then: `Alter` → `game-server` → `Tasks` → `application` → `run`

## 📋 Verification Checklist

Before running the server, verify:

- [ ] `data/xteas.json` exists
- [ ] `data/cache/main_file_cache.dat2` exists
- [ ] `data/cache/main_file_cache.idx0` through `idx20` and `idx255` exist
- [ ] `game.yml` exists (✅ Already created)
- [ ] `dev-settings.yml` exists (✅ Already created)
- [ ] Java 17 is installed (`java -version`)

## 🐛 Troubleshooting

See `SETUP_GUIDE.md` for detailed troubleshooting information.

