# Alter RSPS Setup Guide

This guide will help you set up the Alter RuneScape Private Server.

## Prerequisites

1. **Java JDK 17** - Make sure you have Java 17 installed. You can verify by running:
   ```bash
   java -version
   ```
   If you don't have it, download from: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

2. **IntelliJ IDEA** (Recommended) - Download from: https://www.jetbrains.com/idea/download/

## Step 1: Download Required Files

You need to download two files from the OpenRS2 archive:

### 1.1 Download XTEAs File
- **URL**: https://archive.openrs2.org/caches/runescape/2038/keys.json
- **Save as**: `xteas.json` inside the `/data/` directory
- **Location**: `data/xteas.json`

### 1.2 Download Cache Files
- **URL**: https://archive.openrs2.org/caches/runescape/2038/disk.zip
- **Extract**: Extract the ZIP file and copy the `cache` folder contents to `/data/cache/`
- **Required files**: The cache folder should contain files like:
  - `main_file_cache.dat2`
  - `main_file_cache.idx0` through `main_file_cache.idx20`
  - `main_file_cache.idx255`

## Step 2: Verify Configuration Files

The following configuration files should already be created:
- ✅ `game.yml` - Main game configuration
- ✅ `dev-settings.yml` - Development settings

If these are missing, they have been created from the example files.

## Step 3: Build and Install

### Using IntelliJ IDEA:

1. Open IntelliJ IDEA
2. Open the Alter project (File -> Open -> Select the Alter folder)
3. Wait for Gradle to sync and download dependencies
4. Open the Gradle panel (usually on the right side)
5. Navigate to: `Alter` -> `game-server` -> `Tasks` -> `other` -> `install`
6. Double-click `install` to run it

This will:
- Verify all required cache files exist
- Generate RSA keys for encryption
- Decrypt the map files

### Using Command Line:

```bash
# Windows (PowerShell)
.\gradlew.bat :game-server:install

# Linux/Mac
./gradlew :game-server:install
```

## Step 4: Run the Server

### Using IntelliJ IDEA:

1. In the Gradle panel, navigate to: `Alter` -> `game-server` -> `Tasks` -> `application` -> `run`
2. Double-click `run` to start the server

### Using Command Line:

```bash
# Windows (PowerShell)
.\gradlew.bat :game-server:run

# Linux/Mac
./gradlew :game-server:run
```

## Step 5: Verify Server is Running

When the server starts successfully, you should see output like:
```
Alter loaded up in XXXX ms.
Now listening for incoming connections on port 43594...
```

If you only see `Alter Loaded up in x ms.` without the port message, something went wrong.

## Step 6: Connect with Client

### Recommended Client: RSProx

1. Download RSProx from: https://github.com/blurite/rsprox/releases
2. For Windows users:
   - Press `Win + R` and type: `%USERPROFILE%`
   - Locate or create folder named `.rsprox`
   - Create a file named `proxy-targets.yaml` inside `.rsprox`
   - Paste the following content (replace `YOUR_MODULUS_KEY_HERE` with the modulus key from the `modulus` file generated during install):

```yaml
config:
  - id: 1
    name: Alter
    jav_config_url: https://client.blurite.io/jav_local_228.ws
    varp_count: 15000
    revision: 228.2
    modulus: YOUR_MODULUS_KEY_HERE
```

The modulus key can be found in the `modulus` file in the project root directory after running the install task.

## Troubleshooting

### Missing Cache Files Error
If you get an error about missing cache files:
- Make sure you downloaded the cache ZIP from the OpenRS2 archive
- Extract all files from the cache folder to `data/cache/`
- Verify all required `.idx` files and `.dat2` file are present

### Missing xteas.json Error
- Download `keys.json` from the OpenRS2 archive
- Rename it to `xteas.json`
- Place it in the `data/` directory (same level as `api.yml`)

### Java Version Error
- Make sure you have Java 17 installed
- In IntelliJ: File -> Project Structure -> Set SDK to Java 17
- Verify with `java -version` in terminal

### Port Already in Use
- Change the port in `game.yml`: `game-port: 43594` to a different port
- Or stop any other application using port 43594

## Project Structure

```
Alter/
├── data/
│   ├── api.yml              # API configuration
│   ├── xteas.json          # XTEA keys (you need to download this)
│   └── cache/              # Game cache files (you need to download this)
│       ├── main_file_cache.dat2
│       ├── main_file_cache.idx0
│       └── ... (other idx files)
├── game.yml                # Game configuration (created)
├── dev-settings.yml        # Dev settings (created)
└── ... (other project files)
```

## Additional Resources

- Alter Discord: https://discord.com/invite/sAzCuuwkpN
- RSMod Documentation: https://github.com/Tomm0017/rsmod
- OpenRS2 Archive: https://archive.openrs2.org/

## Notes

- The server uses revision 228 (OSRS client revision)
- Default port is 43594
- Default home location is coordinates (3218, 3218)
- Player saves are stored in JSON format by default

