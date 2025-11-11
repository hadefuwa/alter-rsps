# Alter RSPS Implementation Summary

## ✅ Completed Tasks

### 1. Configuration Files Created
- ✅ **game.yml** - Main game configuration file (created from `game.example.yml`)
  - Server name: "Alter"
  - Port: 43594
  - Revision: 228
  - Home coordinates: (3218, 3218)
  - All services configured

- ✅ **dev-settings.yml** - Development settings file (created from `dev-settings.example.yml`)
  - All debug options set to false (production-ready)

### 2. Documentation Created
- ✅ **SETUP_GUIDE.md** - Comprehensive setup guide with detailed instructions
- ✅ **QUICK_START.md** - Quick reference guide for fast setup
- ✅ **download-requirements.ps1** - PowerShell script to automatically download required files

### 3. Project Structure Verified
- ✅ Project structure is correct
- ✅ Gradle configuration files are present
- ✅ All source code modules are in place

## ⚠️ Required Actions (User Must Complete)

### 1. Download Required Files

The server requires two files from the OpenRS2 archive:

#### Option A: Use the PowerShell Script (Recommended)
```powershell
.\download-requirements.ps1
```

This script will:
- Download `xteas.json` to `data/xteas.json`
- Download and extract cache files to `data/cache/`
- Verify all required files are present

#### Option B: Manual Download

1. **Download xteas.json**:
   - URL: https://archive.openrs2.org/caches/runescape/2038/keys.json
   - Save as: `data/xteas.json`

2. **Download and extract cache**:
   - URL: https://archive.openrs2.org/caches/runescape/2038/disk.zip
   - Extract the ZIP file
   - Copy all contents from the `cache` folder inside the ZIP to `data/cache/`
   - Required files:
     - `main_file_cache.dat2`
     - `main_file_cache.idx0` through `idx20`
     - `main_file_cache.idx255`

### 2. Initialize Gradle Wrapper (if needed)

If `gradlew.bat` doesn't exist, generate it:
```powershell
# If you have Gradle installed globally
gradle wrapper

# Or use IntelliJ IDEA's built-in Gradle wrapper generation
```

### 3. Build and Install

#### Using IntelliJ IDEA (Recommended):
1. Open IntelliJ IDEA
2. Open the Alter project
3. Wait for Gradle sync
4. In Gradle panel: `Alter` → `game-server` → `Tasks` → `other` → `install`
5. This will:
   - Verify all cache files exist
   - Generate RSA encryption keys
   - Decrypt map files

#### Using Command Line:
```powershell
.\gradlew.bat :game-server:install
```

### 4. Start the Server

#### Using IntelliJ IDEA:
- Gradle panel: `Alter` → `game-server` → `Tasks` → `application` → `run`

#### Using Command Line:
```powershell
.\gradlew.bat :game-server:run
```

## 📋 Verification Checklist

Before starting the server, verify:

- [ ] Java 17 is installed (`java -version`)
- [ ] `data/xteas.json` exists
- [ ] `data/cache/main_file_cache.dat2` exists
- [ ] `data/cache/main_file_cache.idx0` through `idx20` exist
- [ ] `data/cache/main_file_cache.idx255` exists
- [ ] `game.yml` exists ✅
- [ ] `dev-settings.yml` exists ✅
- [ ] IntelliJ IDEA is configured with Java 17 SDK

## 🎯 Expected Server Output

When the server starts successfully, you should see:

```
Preparing Alter...
Alter loaded up in XXXX ms.
Loaded properties for Alter.
Loaded filestore from path ... in XXXX ms.
Loaded X privilege levels in XXXX ms.
Loaded X plugins in XXXX ms.
Alter loaded up in XXXX ms.
Now listening for incoming connections on port 43594...
```

If you only see `Alter Loaded up in x ms.` without the port message, check the logs for errors.

## 🔧 Troubleshooting

### Missing Cache Files
- Run the `download-requirements.ps1` script
- Or manually download and extract the cache files

### Missing xteas.json
- Download from: https://archive.openrs2.org/caches/runescape/2038/keys.json
- Save as `data/xteas.json`

### Java Version Issues
- Ensure Java 17 is installed
- In IntelliJ: File → Project Structure → Set SDK to Java 17
- Verify with `java -version`

### Gradle Wrapper Missing
- Generate wrapper: `gradle wrapper` (if Gradle is installed)
- Or use IntelliJ's Gradle wrapper generation feature

### Port Already in Use
- Change port in `game.yml`: `game-port: 43594` → `game-port: 43595` (or another port)

## 📚 Additional Resources

- **Main README**: See `README.md` for original setup instructions
- **Detailed Setup Guide**: See `SETUP_GUIDE.md`
- **Quick Start**: See `QUICK_START.md`
- **Alter Discord**: https://discord.com/invite/sAzCuuwkpN
- **OpenRS2 Archive**: https://archive.openrs2.org/

## 🎮 Client Setup

After the server is running, you can connect using:

### RSProx (Recommended Client)

1. Download RSProx from: https://github.com/blurite/rsprox/releases
2. Create `%USERPROFILE%\.rsprox\proxy-targets.yaml`:
```yaml
config:
  - id: 1
    name: Alter
    jav_config_url: https://client.blurite.io/jav_local_228.ws
    varp_count: 15000
    revision: 228.2
    modulus: YOUR_MODULUS_KEY_HERE
```
3. Get the modulus key from the `modulus` file (created after running install task)
4. Replace `YOUR_MODULUS_KEY_HERE` with the actual modulus value

## 📝 Notes

- The server uses OSRS revision 228
- Default port is 43594
- Player saves are stored in JSON format by default
- The server supports plugin-based architecture for easy customization
- All game content is loaded from plugins in the `game-plugins` module

## ✨ Next Steps After Setup

1. Customize server settings in `game.yml`
2. Add custom plugins to `game-plugins/src/main/kotlin/org/alter/plugins/`
3. Configure spawns in `data/cfg/spawns/item_spawns.yml`
4. Customize world settings in `data/cfg/world.json`

---

**Status**: Server implementation is complete. Configuration files are ready. User needs to download cache files and xteas.json, then run the install and start tasks.

