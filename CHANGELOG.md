# Changelog - Server Setup and Path Fixes

## Date: 2025-01-XX (Critical Bug Fixes and Code Quality Improvements)

### Summary
Fixed multiple critical bugs and code quality issues discovered during code review:
1. Fixed broken logic in Social.kt that always evaluated to true
2. Fixed array index bug in PawnList.kt that was wasting one slot
3. Fixed unsafe null pointer exceptions that could cause server crashes
4. Removed debug println statements from production code
5. Improved error handling and documentation throughout

---

## Bug Fixes

### 1. Social.kt - Broken Logic Fix
**File**: `game-server/src/main/kotlin/org/alter/game/model/social/Social.kt`

**Issue**: The `pushFriends()` method had a condition `if (friends.isEmpty() || true)` which always evaluated to true, making the else branch unreachable.

**Root Cause**: Debugging code left in production with `|| true` condition.

**Fix Applied**:
- Removed the `|| true` condition
- Cleaned up temporary fix entry handling
- Documented incomplete friend/ignore list functionality with clear TODOs
- Added notes about missing protocol messages needed for full implementation

**Impact**: Friend list logic now works correctly. Full functionality pending protocol message implementation.

---

### 2. PawnList.kt - Array Index Bug Fix
**File**: `game-server/src/main/kotlin/org/alter/game/model/PawnList.kt`

**Issue**: The `add()` method started iterating from index 1 instead of 0, wasting the first slot in the array.

**Root Cause**: Loop initialization error - `for (i in 1 until pawns.size)` instead of `for (i in 0 until pawns.size)`.

**Fix Applied**:
- Changed loop to start from index 0
- Added documentation explaining the bug fix

**Impact**: Now uses full array capacity. Previously wasted one player/NPC slot.

---

### 3. PawnPathAction.kt - Null Pointer Exception Fix
**File**: `game-server/src/main/kotlin/org/alter/game/model/move/PawnPathAction.kt`

**Issue**: Unsafe null assertions (`!!`) on nullable attributes could cause NPE if both `INTERACTING_NPC_ATTR` and `INTERACTING_PLAYER_ATTR` were null.

**Root Cause**: Missing null checks before using force-unwrap operator.

**Fix Applied**:
- Replaced unsafe `!!` operators with safe null handling
- Added early returns when both attributes are null
- Added null check for `INTERACTING_OPT_ATTR`

**Impact**: Prevents server crashes when interaction attributes are missing.

---

### 4. LootTableBuilder.kt - Null Safety Improvements
**File**: `game-server/src/main/kotlin/org/alter/game/model/weightedTableBuilder/LootTableBuilder.kt`

**Issue**: 
- Unsafe null assertions (`!!`) on `loot.weight` could cause NPE
- Empty catch block with only `printStackTrace()` provided no useful error information

**Root Cause**: Missing null checks and poor error handling.

**Fix Applied**:
- Replaced `loot.weight!!` with safe null handling using `?: continue`
- Improved error handling in catch block with error message logging
- Added TODO for proper logging service integration

**Impact**: Prevents crashes from null weights and provides better error visibility.

---

### 5. OpNpcTHandler.kt - Debug Code Removal
**File**: `game-server/src/main/kotlin/org/alter/game/message/handler/OpNpcTHandler.kt`

**Issue**: Debug `println` statement left in production code.

**Root Cause**: Debugging code not removed before commit.

**Fix Applied**:
- Removed `println` statement
- Added logger import and companion object
- Replaced with commented debug logging for future use

**Impact**: Cleaner production code, no unnecessary console output.

---

### 6. Code Quality Improvements

#### Pawn.kt - Debug Statement Removal
**File**: `game-server/src/main/kotlin/org/alter/game/model/entity/Pawn.kt`
- Removed debug `println` statement from `isRouteBlocked()` method
- Replaced with commented debug logging

#### RSCM.kt - Logging Improvement
**File**: `plugins/rscm/src/main/kotlin/org/alter/rscm/RSCM.kt`
- Replaced `println` with proper `logger.warn` for error messages

#### CacheManager.kt - Documentation
**File**: `plugins/filestore/src/main/kotlin/dev/openrune/cache/CacheManager.kt`
- Documented `println` usage (logger not available in this context)
- Added TODO for potential logger integration

#### WaterPlugin.kt - Documentation
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/water/WaterPlugin.kt`
- Added comprehensive documentation explaining RSCM migration requirement
- Documented required functionality for future implementation

#### KeptOnDeath.kt - Documentation
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/worn_equipment/kod/KeptOnDeath.kt`
- Added documentation for risk value calculation implementation
- Documented TODO items with clear implementation guidance

#### EventMouseClickHandler.kt - Documentation
**File**: `game-server/src/main/kotlin/org/alter/game/message/handler/EventMouseClickHandler.kt`
- Added comprehensive documentation explaining placeholder status
- Documented when and how to implement if needed

#### ItemMetadataService.kt - Enhanced TODOs
**File**: `game-server/src/main/kotlin/org/alter/game/service/game/ItemMetadataService.kt`
- Expanded TODO comments with detailed implementation guidance
- Documented attack sounds and equip sound support requirements
- Improved error handling documentation

---

## Date: 2025-01-11 (Bug Fixes)

### Summary
Fixed two critical UI bugs that prevented players from using essential game features:
1. Settings window close button was non-functional
2. Home teleport button was not working

---

## Bug Fixes

### 1. Settings Window Close Button Fix
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/settings/options/tabs/OptionsTabFirstPlugin.kt`

**Issue**: The settings window (interface 134) could not be closed by clicking the close button (component 4).

**Root Cause**: Missing button click handler for the close button on interface 134.

**Fix Applied**:
- Added import for `Settings` class to use the `SETTINGS_CLOSE_BUTTON_ID` constant
- Added `onButton` handler for interface 134, component 4 that closes the window
- Handler calls `player.closeComponent(parent = 161, child = 18)` to properly close the interface

**Code Added**:
```kotlin
import org.alter.plugins.content.interfaces.options.Settings

/**
 * Close button handler for the settings window (interface 134).
 */
onButton(interfaceId = OptionsTab.ALL_SETTINGS_INTERFACE_ID, component = Settings.SETTINGS_CLOSE_BUTTON_ID) {
    player.closeComponent(parent = 161, child = 18)
}
```

**Impact**: Players can now properly close the settings window by clicking the close button.

---

### 2. Home Teleport Button Fix
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/commands/commands/all/TeleportsPlugin.kt`

**Issue**: The home teleport button (interface 218, component 7) was not functioning.

**Root Cause**: Missing button click handler for the home teleport spell button.

**Fix Applied**:
- Added imports for `TaskPriority` and `prepareForTeleport` extension function
- Implemented complete home teleport handler with proper animation sequence
- Added teleport validation to check if player can teleport
- Implemented 5-stage animation sequence matching OSRS home teleport behavior:
  - Animation stages: LUMBRIDGE_HOME_TELEPORT_1 through LUMBRIDGE_HOME_TELEPORT_5
  - Graphics: LUMBRIDGE_HOME_TELEPORT_1 through LUMBRIDGE_HOME_TELEPORT_4
  - Proper timing with `wait()` cycles between animation stages
- Teleports player to home location defined in `world.gameContext.home`
- Properly locks/unlocks player during teleport process

**Code Added**:
```kotlin
import org.alter.game.model.queue.TaskPriority
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Home teleport button handler (interface 218, component 7).
 */
onButton(interfaceId = 218, component = 7) {
    if (!player.lock.canTeleport()) {
        return@onButton
    }

    val home = world.gameContext.home
    player.queue(TaskPriority.STRONG) {
        player.prepareForTeleport()
        player.lock = LockState.FULL_WITH_DAMAGE_IMMUNITY
        
        // Home teleport animation sequence
        player.animate(Animation.LUMBRIDGE_HOME_TELEPORT_1)
        player.graphic(Graphic.LUMBRIDGE_HOME_TELEPORT_1)
        wait(cycles = 2)
        
        player.animate(Animation.LUMBRIDGE_HOME_TELEPORT_2)
        player.graphic(Graphic.LUMBRIDGE_HOME_TELEPORT_2)
        wait(cycles = 2)
        
        player.animate(Animation.LUMBRIDGE_HOME_TELEPORT_3)
        player.graphic(Graphic.LUMBRIDGE_HOME_TELEPORT_3)
        wait(cycles = 2)
        
        player.animate(Animation.LUMBRIDGE_HOME_TELEPORT_4)
        player.graphic(Graphic.LUMBRIDGE_HOME_TELEPORT_4)
        wait(cycles = 2)
        
        player.animate(Animation.LUMBRIDGE_HOME_TELEPORT_5)
        wait(cycles = 1)
        
        player.moveTo(home)
        player.animate(-1)
        player.unlock()
    }
}
```

**Impact**: Players can now use the home teleport spell button to teleport to their home location with proper animations.

---

## Date: 2025-01-11

### Summary
Fixed all relative path issues to allow the Alter RSPS server to run correctly from the project root directory. The server was originally configured to run from a subdirectory, causing file path resolution failures.

---

## Configuration Files Created

### 1. `game.yml`
- **Created from**: `game.example.yml`
- **Purpose**: Main game configuration file
- **Contents**: Server name, port (43594), revision (228), home coordinates, privileges, and services configuration
- **Location**: Project root directory

### 2. `dev-settings.yml`
- **Created from**: `dev-settings.example.yml`
- **Purpose**: Development/debugging settings
- **Contents**: All debug options set to `false` (production-ready)
- **Location**: Project root directory

---

## Build Configuration Changes

### 3. `game-server/build.gradle.kts`
- **Change**: Added working directory configuration for the `run` task
- **Details**: 
  ```kotlin
  tasks.named<JavaExec>("run") {
      workingDir = rootProject.projectDir
  }
  ```
- **Reason**: Ensures the server runs from the project root directory, allowing relative paths to resolve correctly

---

## Core Server Path Fixes

### 4. `game-server/src/main/kotlin/org/alter/game/Launcher.kt`
- **Changed paths**:
  - `../data/api.yml` → `data/api.yml`
  - `../data/cache` → `data/cache`
  - `../game.yml` → `game.yml`
  - `../dev-settings.yml` → `dev-settings.yml`
- **Reason**: Working directory is now project root, so `../` was going up one level incorrectly

---

## Service Configuration Fixes

### 5. `game.yml` - Service Paths
Updated service configurations to use correct paths:

- **XteaKeyService**:
  - Added: `path: "data/"` (was defaulting to `../data/`)

- **NpcMetadataService**:
  - Added: `path: "data/cfg/npcs.csv"` (was defaulting to `../data/cfg/npcs.csv`)

- **ObjectMetadataService**:
  - Added: `path: "data/cfg/locs.csv"` (was defaulting to `../data/cfg/locs.csv`)

- **RsaService**:
  - Added: `path: "data/rsa/key.pem"` (was defaulting to `../data/rsa/key.pem`)

- **DumpEntityIdService**:
  - Changed: `cache-path: "../data/cache/"` → `cache-path: "data/cache/"`
  - Changed: `output-path: "../game-api/..."` → `output-path: "game-api/..."`

---

## Service Code Path Fixes

### 6. `game-server/src/main/kotlin/org/alter/game/service/game/ItemMetadataService.kt`
Fixed hardcoded paths:
- `../data/cfg/items` → `data/cfg/items`
- `../data/cfg/objs.csv` → `data/cfg/objs.csv`
- `../data/cfg/items/renderAnimations/bas_mappings.json` → `data/cfg/items/renderAnimations/bas_mappings.json`
- `../data/cfg/items/renderAnimations/item_bas.json` → `data/cfg/items/renderAnimations/item_bas.json`

### 7. `plugins/rscm/src/main/kotlin/org/alter/rscm/RSCM.kt`
- Changed: `Path.of("../data/cfg/rscm/")` → `Path.of("data/cfg/rscm/")`
- **Impact**: Fixed "RSCM List is empty" error that was preventing plugins from loading

### 8. `game-plugins/src/main/kotlin/org/alter/plugins/content/objects/door/DoorService.kt`
- Changed: `"../data/cfg/doors/single-doors.json"` → `"data/cfg/doors/single-doors.json"`
- Changed: `"../data/cfg/doors/double-doors.json"` → `"data/cfg/doors/double-doors.json"`

### 9. `game-plugins/src/main/kotlin/org/alter/plugins/content/objects/gates/GateService.kt`
- Changed: `"../data/cfg/gates/gates.json"` → `"data/cfg/gates/gates.json"`

### 10. `game-plugins/src/main/kotlin/org/alter/plugins/content/skills/thieving/stall/StallThievingService.kt`
- Changed: `"../data/cfg/thieving/stalls.json"` → `"data/cfg/thieving/stalls.json"`

### 11. `game-plugins/src/main/kotlin/org/alter/plugins/content/skills/thieving/pickpocket/PickpocketService.kt`
- Changed: `"../data/cfg/thieving/pickpockets.json"` → `"data/cfg/thieving/pickpockets.json"`

### 12. `game-plugins/src/main/kotlin/org/alter/plugins/content/skills/thieving/chest/ChestThievingService.kt`
- Changed: `"../data/cfg/thieving/chests.json"` → `"data/cfg/thieving/chests.json"`

### 13. `game-plugins/src/main/kotlin/org/alter/plugins/service/worldlist/WorldListService.kt`
- Changed: `"../data/cfg/world.json"` → `"data/cfg/world.json"`

### 14. `game-server/src/main/kotlin/org/alter/game/saving/formats/impl/Json.kt`
- Changed: `"../data/saves/${collectionName}/"` → `"data/saves/${collectionName}/"`
- **Impact**: Fixed player save file paths

### 15. `game-server/src/main/kotlin/org/alter/game/model/social/Social.kt`
- Changed: `"../data/saves/"` → `"data/saves/"` (2 occurrences)
- **Impact**: Fixed friend/ignore list save paths

---

## Helper Scripts Created

### 16. `download-requirements.ps1`
- **Purpose**: Automated script to download required cache files and xteas.json
- **Features**:
  - Downloads `xteas.json` from OpenRS2 archive
  - Downloads cache ZIP file
  - Extracts cache files to `data/cache/`
  - Verifies all required cache files are present
  - Cleans up temporary files

### 17. `check-server-setup.ps1`
- **Purpose**: Diagnostic script to verify server setup
- **Checks**:
  - Required files exist (xteas.json, cache files, config files)
  - RSA keys exist
  - Java version
  - Port availability

---

## Documentation Created

### 18. `SETUP_GUIDE.md`
- Comprehensive setup guide with detailed instructions
- Troubleshooting section
- Client setup instructions

### 19. `QUICK_START.md`
- Quick reference guide for fast setup
- PowerShell commands for downloading files

### 20. `IMPLEMENTATION_SUMMARY.md`
- Summary of all completed tasks
- Verification checklist
- Next steps after setup

### 21. `TROUBLESHOOTING.md`
- Common issues and solutions
- How to get detailed error information
- Java version configuration guide

---

## Files Downloaded

### 22. Required Cache Files
- **xteas.json**: Downloaded from `https://archive.openrs2.org/caches/runescape/2038/keys.json`
  - Location: `data/xteas.json`
  
- **Cache files**: Downloaded from `https://archive.openrs2.org/caches/runescape/2038/disk.zip`
  - Extracted to: `data/cache/`
  - Includes: `main_file_cache.dat2` and all `.idx` files (idx0-idx20, idx255)

---

## Issues Resolved

1. ✅ **FileNotFoundException: ../data/api.yml** - Fixed Launcher paths
2. ✅ **FileNotFoundException: Missing xteas.json** - Fixed XteaKeyService path
3. ✅ **FileNotFoundException: ../data/cfg/objs.csv** - Fixed ItemMetadataService paths
4. ✅ **FileNotFoundException: ../data/cfg/npcs.csv** - Fixed NpcMetadataService path
5. ✅ **IllegalStateException: RSCM List is empty** - Fixed RSCM initialization path
6. ✅ **NoSuchFileException: ../data/cfg/doors/single-doors.json** - Fixed DoorService paths
7. ✅ **Multiple plugin load failures** - Resolved after RSCM fix
8. ✅ **Working directory issues** - Fixed by setting workingDir in build.gradle.kts

---

## Server Status

### Final Result
✅ **Server successfully running on port 43594**

**Server Output:**
```
[INFO] NetworkService - Bound to ports: 43594
[INFO] NetworkService - Revision: 228
[INFO] NetworkService - Supported client types: Desktop
[INFO] Server - Now listening for incoming connections on port 43594...
[INFO] GameService - [Cycle time: 2-4ms] [Entities: 0p / 115n] [Map: 77c / 2r / 0i]
```

**Server Statistics:**
- Port: 43594
- Revision: 228
- NPCs loaded: 115
- Map chunks: 77
- Memory usage: ~869MB used, 1380MB reserved
- Cycle time: 2-4ms (excellent performance)

---

## Next Steps for Users

1. **Connect with Client**: Use RSProx client (see README.md for setup)
2. **Get Modulus Key**: Check `modulus` file in project root for RSA modulus key
3. **Configure RSProx**: Create `%USERPROFILE%\.rsprox\proxy-targets.yaml` with modulus key
4. **Connect**: Use `127.0.0.1:43594` to connect to local server

---

## Technical Notes

### Path Resolution Strategy
All paths were changed from `../data/...` to `data/...` because:
- The Gradle `run` task now sets `workingDir = rootProject.projectDir`
- This means the working directory is the project root (`C:\Users\Hamed\Documents\alter-pnda-rsps\Alter`)
- Relative paths like `data/...` resolve correctly from the project root
- The `../` prefix was going up one directory level incorrectly

### Files Modified
- **Configuration files**: 1 (`game.yml`)
- **Kotlin source files**: 12 files across multiple modules
- **Build files**: 1 (`game-server/build.gradle.kts`)
- **Total changes**: 15+ path corrections

### Testing
- ✅ Server starts successfully
- ✅ All services initialize correctly
- ✅ RSCM loads properly
- ✅ Plugins load without errors
- ✅ Network service binds to port 43594
- ✅ Game cycles running smoothly

---

## Credits

All path fixes were applied to make the server compatible with running from the project root directory, following the standard Gradle application plugin behavior.

