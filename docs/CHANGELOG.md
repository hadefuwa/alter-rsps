# Changelog - Server Setup and Path Fixes

## Date: 2025-01-XX (Crazy Archaeologist Combat Improvements & Loot System Updates)

### Summary
Enhanced the Crazy Archaeologist boss with new combat mechanics, improved loot distribution, and quality-of-life fixes:
1. Added unequip attack that removes player equipment
2. Fixed loot drops to appear at player locations instead of NPC location
3. Reduced attack frequency for better combat pacing
4. Added automatic unlock system for players stuck in combat
5. Increased frequency of book rain AOE attack
6. Fixed loot drop conflicts between shared loot and default loot systems

### Key Changes:

#### 1. Crazy Archaeologist Unequip Attack
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt)

**Feature**: Added new special attack that removes a random equipped item from players and places it in their inventory (if space available).

**Implementation**:
- Triggers after 5+ attacks with 25% chance (1 in 4)
- Priority 3 special attack (after teleport and book rain)
- Fires special unequip projectile (graphic ID 1576)
- Finds all equipped items and randomly selects one to unequip
- Only works if player has inventory space
- Shows disruption graphic and messages to player
- NPC taunts "Your equipment is mine!" above head when successful

**Impact**: Adds another challenging mechanic to the fight, forcing players to manage inventory space.

---

#### 2. Loot Drops at Player Locations
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/death/SharedLootDropPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/death/SharedLootDropPlugin.kt)

**Issue**: Loot was dropping at the NPC's location, but players can't stand on the same tile as the Crazy Archaeologist due to tile blocking system.

**Fix Applied**:
- Added `dropAtPlayerLocationNpcIds` set to track NPCs that should drop loot at player locations
- Modified `handleSharedLootDrop` to check if NPC should drop at player location
- Updated `dropCoinsForPlayer` and `dropRandomItemForPlayer` to accept `dropTile` parameter
- All loot (regular drops, 250k coins, random items) now drops at each player's tile

**Impact**: Players can now easily collect their loot without being pushed away from the drop location.

---

#### 3. Reduced Attack Frequency
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistConfigsPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistConfigsPlugin.kt)

**Change**: Increased `attackSpeed` from 4 cycles (2.4 seconds) to 6 cycles (3.6 seconds).

**Impact**: Reduces attack frequency by 50%, giving players more time to react and manage combat.

---

#### 4. Automatic Player Unlock System
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt)

**Issue**: Players could get locked/stuck during combat with the Crazy Archaeologist, especially due to tile blocking interactions.

**Fix Applied**:
- Enhanced tile blocking timer to detect locked players in combat
- Checks if player is locked, can't move, within combat range, and has dealt damage
- Finds safe adjacent tile (not NPC's tile) to knock player back to
- Resets interactions and unlocks player
- Moves player to safe tile with message "The Crazy Archaeologist's magic knocks you back!"
- If no safe tile found, still unlocks player with message "You break free from the lock!"

**Impact**: Prevents players from getting permanently stuck during combat, improving combat experience.

---

#### 5. Increased Book Rain Attack Frequency
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/crazyarchaeologist/CrazyArchaeologistCombatPlugin.kt)

**Changes**:
- Reduced `BOOK_RAIN_ATTACK_MIN_COUNT` from 6 to 3 attacks
- Increased `BOOK_RAIN_ATTACK_CHANCE_DENOMINATOR` from 4 to 2 (chance increased from 25% to 50%)

**Impact**: Book rain AOE attack now triggers approximately 4x more frequently (from ~1 in 24 attacks to ~1 in 6 attacks).

---

#### 6. Fixed Loot Drop System Conflicts
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt)

**Issue**: Both `NpcLootDropPlugin` and `SharedLootDropPlugin` were handling Crazy Archaeologist deaths, causing loot to drop at NPC location.

**Fix Applied**:
- Added check in `NpcLootDropPlugin` to skip NPCs with multiple players who dealt damage
- Added specific check to skip Crazy Archaeologist (handled by SharedLootDropPlugin)
- Prevents default loot handler from interfering with shared loot system

**Impact**: Ensures shared loot system works correctly without conflicts from default loot handler.

---

## Date: 2025-11-17 (Guard Pickpocketing Fix, Royal Seed Pod Teleporter & Teleport Guide)

### Summary
Fixed guard pickpocketing system and added custom teleportation features:
1. Fixed guard pickpocketing - guards were not pickpocketable due to NPC ID mismatch
2. Created Royal Seed Pod custom coordinate teleporter (Pnda character only)
3. Created comprehensive teleport locations guide in markdown format
4. Removed character save data from gitignore for git tracking

### Key Changes:

#### 1. Guard Pickpocketing Fix
**Issue**: Guards in Varrock were not pickpocketable despite having pickpocket configuration.

**Root Cause**:
- Varrock spawn plugin was spawning guards 11912-11915
- Pickpocket configuration was set for guards 397-400
- The spawned guard NPCs (11912-11915) don't have "pickpocket" option in OSRS cache
- Guards 3010-3011 are confirmed to have the pickpocket option in cache

**Fix Applied**:
- Updated [data/cfg/thieving/pickpockets.json](../data/cfg/thieving/pickpockets.json):
  - Changed NPC list from guards 397-400 to guards 3010-3011
  - Kept level 40 requirement, 46.8 XP, and coin loot (30-60)
- Updated [game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/spawns/SpawnPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/spawns/SpawnPlugin.kt):
  - Changed all guard spawns to use guard_3010 and guard_3011
  - Updated Varrock Square guards (3 spawns)
  - Updated Castle Entrance guards (4 spawns)
  - Updated Castle Courtyard guards (4 spawns)
  - Updated Castle Interior guards (2 spawns)
  - Updated Upper Floor guards (2 spawns)

**Impact**: Guards in Varrock can now be pickpocketed successfully by players with 40+ Thieving.

---

#### 2. Royal Seed Pod Custom Coordinate Teleporter
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/items/teleport/RoyalSeedPodPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/teleport/RoyalSeedPodPlugin.kt) (NEW)

**Feature**: Created custom teleportation system using Royal Seed Pod (item ID 19564) that allows the player "Pnda" to teleport to any coordinates by entering X, Y, and Height values.

**Implementation**:
- Item restriction: Only works for username "pnda" (case-insensitive)
- Prompts player to enter X coordinate via chatbox input
- Prompts player to enter Y coordinate via chatbox input
- Prompts player to enter height/plane (0-3, defaults to 0)
- Validates coordinates (X/Y must be 0-16383, height must be 0-3)
- Uses TeleportType.MODERN for teleportation animation
- Provides feedback messages during each step

**Usage**:
1. Get Royal Seed Pod: `::item 19564`
2. Click "Teleport" option on the item
3. Enter X coordinate when prompted
4. Enter Y coordinate when prompted
5. Enter height (0-3) or leave blank for ground level
6. Player teleports to the specified location

**Security**: Other players receive message "The Royal Seed Pod glows briefly, but nothing happens..." when trying to use it.

**Impact**: Provides Pnda character with admin-level teleportation capabilities for server management and testing.

---

#### 3. Teleport Locations Reference Guide
**File**: [guides/TELEPORT_LOCATIONS.md](../guides/TELEPORT_LOCATIONS.md) (NEW)

**Feature**: Created comprehensive markdown reference guide for all teleport locations in the server.

**Content**:
- **Main Cities** (8 locations): Varrock, Lumbridge, Falador, Edgeville, Yanille, Gnome Stronghold, Camelot, Ardougne
- **Wilderness & PvP Areas** (8 locations): Mage Bank, Lava Dragon Isle, Wilderness Volcano, Graveyard of Shadows, Dark Warriors' Fortress, Chaos Temple, Bandit Camp, Resource Area
- **Dungeons & Caves** (8 locations): Taverley Dungeon, Brimhaven Dungeon, Ancient Cavern, God Wars Dungeon, Slayer Tower, Stronghold of Security, TzHaar City
- **Skilling Locations** (8 locations): Seers' Village, Catherby, Fishing Guild, Mining Guild, Crafting Guild, Rimmington, Port Sarim, Draynor Village
- **Desert Cities & Towns** (6 locations): Al Kharid, Duel Arena, Shantay Pass, Pollnivneach, Nardah, Sophanem
- **Special & Island Locations** (4 locations): Karamja, Ape Atoll, TzHaar Fight Cave, TzHaar Fight Pit
- **Northern Locations** (6 locations): Barbarian Outpost, Barbarian Village, Burthorpe, Taverley, Rellekka, Jatizso
- **Test & Special Areas** (1 location): Thieving Test Area
- **Wilderness Obelisk Locations** (6 obelisk zones with coordinate ranges)

**Additional Features**:
- Usage instructions for Royal Seed Pod coordinate teleporter
- Quick command reference for admin teleport commands
- Coordinate system explanation (X/Y range, height levels)
- All coordinates organized in markdown tables with X, Y, Height, and Notes columns

**Total Locations**: 50+ teleport destinations documented

**Impact**: Provides quick reference for all available teleport coordinates, useful for both admins and the Royal Seed Pod custom teleporter.

---

#### 4. Character Save Data Git Tracking
**File**: [.gitignore](../.gitignore)

**Change**: Removed `/data/saves/` from .gitignore to enable git tracking of player save data.

**Reason**: User requested character save data to be tracked by git going forward to prevent data loss.

**Impact**: Future character saves will be committed to git repository. Previous save data was already lost (not recoverable as it was never tracked).

---

#### 5. Wilderness Gate Compilation Fix
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/WildernessGate1728Plugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/WildernessGate1728Plugin.kt)

**Issue**: Compilation errors in WildernessGate1728Plugin.kt preventing build.

**Errors Fixed**:
- Added missing import: `import dev.openrune.cache.CacheManager.getObject`
- Changed `.lowercase()` to `.toLowerCase()` (Kotlin compatibility)
- Added explicit type annotations to resolve overload ambiguity
- Fixed lambda context by changing `openGate()` to `openGate(this)` and `closeGate()` to `closeGate(this)`

**Impact**: Wilderness gate (object 1728) now compiles and functions correctly.

---

### Git Changes
- Committed all fixes with message: "Fix guard pickpocketing and add Royal Seed Pod teleporter"
- Created tag: **v1.0.1**
- Pushed to remote repository

---

### Files Modified
- `.gitignore` - Removed /data/saves/ entry
- `data/cfg/thieving/pickpockets.json` - Changed guard NPCs to 3010-3011
- `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/spawns/SpawnPlugin.kt` - Changed all guard spawns to 3010-3011
- `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/WildernessGate1728Plugin.kt` - Fixed compilation errors

### Files Created
- `game-plugins/src/main/kotlin/org/alter/plugins/content/items/teleport/RoyalSeedPodPlugin.kt` - Royal Seed Pod custom teleporter
- `guides/TELEPORT_LOCATIONS.md` - Comprehensive teleport locations guide

---

## Date: 2025-11-16 (Clue Casket Opening System)

### Summary
Implemented complete clue casket opening functionality for all clue casket tiers:
1. Created ClueCasketPlugin to handle opening all clue casket types
2. Fixed item option registration to properly detect and bind to casket options
3. All clue casket tiers now work: beginner, easy, medium, hard, elite, and master
4. Added comprehensive reward system with tier-based loot tables
5. Implemented inventory space validation (requires 10 free slots)

### Technical Details
**Issue**: Clue caskets could not be opened - clicking on them did nothing.

**Root Cause**:
- No plugin handler was registered for clue casket items
- Item option detection needed to handle multiple option indices
- Required proper slot detection and item verification

**Solution**:
- Created new `ClueCasketPlugin` in `game-plugins/src/main/kotlin/org/alter/plugins/content/items/clue_casket/`
- Registered handlers for all casket types by RSCM name and item ID
- Implemented robust option detection (tries "open", "use", and option indices 1-4)
- Added direct item ID registration (2724 for hard casket) as fallback
- Implemented tier-based reward generation with weighted loot tables
- Added inventory space check (10 free slots required)
- Rewards drop on ground if inventory is full

**Files Created**:
- [ClueCasketPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/clue_casket/ClueCasketPlugin.kt) - Complete plugin implementation

**Features**:
- **All Casket Tiers Supported**: Beginner, Easy, Medium, Hard, Elite, Master
- **Reward System**: Tier-appropriate rewards with weighted drop tables
- **Inventory Management**: Validates free space, drops excess rewards on ground
- **Sound Effects**: Plays casket opening sound (Sound.CASKET_OPEN)
- **Error Handling**: Graceful fallback to coins if reward items don't exist
- **Debug Logging**: Comprehensive logging for troubleshooting registration issues

**Casket Types Registered**:
- `item.casket_easy` (ID: 2714)
- `item.casket_medium` (ID: 2802)
- `item.casket_hard` (ID: 2724)
- `item.casket_elite` (ID: 12084)
- `item.reward_casket_master` (ID: 19836)
- `item.reward_casket_beginner` (ID: 23245)
- All `item.reward_casket_*` variants

**Impact**: Players can now successfully open clue caskets and receive rewards. All clue casket tiers are fully functional.

---

## Date: 2025-11-16 (Food Eating System)

### Summary
Fixed and implemented comprehensive food eating system for all food items:
1. Completely rewrote EatingPlugin to properly bind food items
2. Fixed item option binding to use correct option number (option 2)
3. All 51 food types now work correctly including anglerfish with overheal mechanics
4. Players can eat at any HP level (including full HP)
5. Food delay timers properly prevent spam-eating

### Technical Details
**Issue**: Original EatingPlugin attempted to dynamically scan all 30,000+ cache items which caused initialization failures and prevented the plugin from loading.

**Solution**:
- Rewrote plugin to only bind the 51 foods defined in the Food enum
- Fixed option binding: Client sends option=2 for "Eat" even though it's at index 0 in interfaceOptions
- Used direct integer option binding (`onItemOption(food.item, 2)`) instead of string-based binding
- Removed complex dynamic scanning and caching logic

**Files Modified**:
- [EatingPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/EatingPlugin.kt) - Complete rewrite
- [Food.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/Food.kt) - No changes (already correct)
- [Foods.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/Foods.kt) - No changes (already correct)

**Working Food Types** (51 total):
- All fish: shrimps, sardine, herring, mackerel, trout, cod, pike, salmon, tuna, rainbow fish, cave eel, lobster, bass, swordfish, monkfish, karambwan, shark, sea turtle, manta ray, dark crab, anglerfish
- All meats: chicken, meat, roast beast meat, kebab
- All baked goods: bread, cakes, pies, pizzas
- All vegetables/fruits: potato, cabbage, onion, banana, strawberry, watermelon, pineapple, stew, curry, cheese, tomato

---

## Date: 2025-11-16 (HP Regeneration System)

### Summary
Implemented automatic HP regeneration system that restores 1 HP every 30 seconds for all players:
1. Added HP regeneration timer system
2. Players automatically regain 1 HP every 30 seconds when not at full health
3. Regeneration only occurs when players are alive and below maximum HP

---

## HP Regeneration System

### Feature
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/hpregeneration/HpRegenerationPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/hpregeneration/HpRegenerationPlugin.kt)

**Feature**: Automatic HP regeneration that restores 1 HP every 30 seconds for all players.

**Implementation**:
- Created new `HpRegenerationPlugin` that extends `KotlinPlugin`
- Added `HP_REGENERATION_TIMER` to `Timers.kt` for timer management
- Timer interval: 50 cycles (30 seconds, since 1 cycle = 0.6 seconds)
- Timer initialized on player login
- Timer handler regenerates 1 HP and resets timer every 30 seconds

**Functionality**:
- **Login Initialization**: Timer starts at 50 cycles when player logs in
- **Automatic Regeneration**: Every 30 seconds, players regain 1 HP if:
  - Player is alive (not dead)
  - Current HP is below maximum HP
- **Safety Checks**:
  - Skips regeneration if player is dead
  - Only regenerates if not at full health
  - Caps HP at maximum (prevents overheal)
- **Timer Management**: Timer automatically resets after each regeneration cycle

**Timer Key Added**:
**File**: [game-server/src/main/kotlin/org/alter/game/model/timer/Timers.kt](../game-server/src/main/kotlin/org/alter/game/model/timer/Timers.kt)
- Added `HP_REGENERATION_TIMER = TimerKey()` for HP regeneration system

**Impact**:
- Players now passively regenerate health over time
- Reduces reliance on food for minor health recovery
- Makes gameplay more forgiving for exploration and non-combat activities
- Regeneration is slow enough (1 HP per 30 seconds) to not trivialize combat

---

## Date: 2025-11-16 (Food Eating & Bone Burying Systems)

### Summary
Fixed and enhanced food eating and bone burying systems:
1. Fixed food eating functionality with proper item handling
2. Added 30+ additional food types with correct healing values
3. Implemented comprehensive bone burying for prayer training
4. Added "Bury" and "Bury-all" options for bones

---

## Food Eating System Fix

### Issue
Players were unable to eat food items in their inventory.

### Root Cause
- Item option binding issues similar to bone burying
- Missing proper queue handling for food consumption
- Inconsistent item removal logic

### Fixes Applied

#### Food Eating Implementation
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/EatingPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/EatingPlugin.kt)

**Changes**:
- Refactored food eating logic to use helper function `eatFood()`
- Added proper item verification before consumption
- Implemented queue-based eating with animation and sound
- Fixed item removal to use item ID instead of string
- Added extensive debug logging for troubleshooting

**Functionality**:
- Proper cooldown handling (prevents spam eating)
- Animation (829) and sound (2393) effects
- Health restoration based on food type
- Support for overheal (Anglerfish)
- Support for combo food (Karambwan)
- Replacement item handling (e.g., empty vials)
- Attack delay timer after eating

#### Food Types Expansion
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/Food.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/items/consumables/food/Food.kt)

**Added Food Categories**:
- **Seafood** (15 types): Shrimp, Tuna, Lobster, Swordfish, Shark, Manta Ray, Dark Crab, etc.
- **Meat** (4 types): Cooked Chicken, Cooked Meat, Roast Beast Meat, Ugthanki Kebab
- **Pastries & Baked Goods** (8 types): Bread, Cakes, Pies, Pizzas (Plain, Meat, Anchovy, Pineapple)
- **Vegetables & Fruits** (11 types): Potatoes (various preparations), Cabbage, Onion, Banana, Strawberry, Watermelon, Pineapple, etc.
- **Stews & Soups** (2 types): Stew, Curry
- **Other** (4 types): Egg, Cheese, Tomato, Sweetcorn

**Total Food Types**: 44+ items with proper OSRS healing values

---

## Date: 2025-11-16 (Bone Burying System)

### Summary
Implemented complete bone burying functionality for prayer training:
1. Added bone burying for 30+ bone types with proper prayer XP rewards
2. Implemented "Bury" and "Bury-all" options for inventory bones
3. Added support for noted bones
4. Fixed burying animation, sound effects, and cooldown timers

### Changes

#### Bone Burying Implementation
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/prayer/PrayersPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/prayer/PrayersPlugin.kt)

**Features Added**:
- Bone burying for all standard OSRS bone types:
  - Regular bones (4.5 XP)
  - Big bones (15 XP)
  - Dragon bones (72 XP)
  - Baby dragon bones (30 XP)
  - Superior dragon bones (150 XP)
  - Lava dragon bones (85 XP)
  - Wyvern bones (72 XP)
  - Dagannoth bones (125 XP)
  - Ourg/Fayrg/Raurg bones (140/84/96 XP)
  - Hydra/Wyrm/Drake bones (110/50/80 XP)
  - Monkey bones variants (5-18 XP)
  - And 20+ more bone types

**Functionality**:
- Option 1: "Bury" - Buries a single bone with animation and sound
- Option 2: "Bury-all" - Buries all bones of that type in inventory (for select valuable bones)
- Noted bone support - Can bury noted bones (converts and buries one at a time)
- Cooldown timer (3 ticks) to prevent spam
- Proper animation (827) and sound (2738) effects
- XP rewards match OSRS values

**Helper Functions**:
- `buryBone()` - Handles single bone burial
- `buryNotedBone()` - Handles noted bone burial
- `buryAllBones()` - Handles mass bone burial

---

## Date: 2025-01-XX (Drop Visibility Timer System)

### Summary
Restored timer-based drop visibility system where only the killer sees drops initially, then everyone can see them after a delay:
1. Fixed drop visibility to show only to killer initially (1 minute)
2. Implemented public visibility timer (3 minutes after initial delay)
3. Updated default despawn delay to 4 minutes total (1 min private + 3 min public)

---

## Drop Visibility Timer System

### Issue
All NPC drops were immediately visible to all players, removing the original timer-based visibility system where:
- Only the killer could see drops for 1 minute
- Then everyone could see drops for 3 minutes
- Then drops would despawn

### Root Cause
- `gItemPublicDelay` was set to `0` in `Server.kt`, making all items public immediately
- NPC drops in `NpcLootDropPlugin.kt` didn't set `timeUntilPublic` or `timeUntilDespawn` timers
- Default despawn delay was 300 cycles (3 minutes) instead of 400 cycles (4 minutes total)

### Fixes Applied

#### 1. NPC Drop Timer Configuration
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt)

**Changes**:
- Added `TimeConstants` import for cycle calculations
- Set `timeUntilPublic = 100` cycles (1 minute) for all NPC drops
- Set `timeUntilDespawn = 400` cycles (4 minutes total) for all NPC drops
- Set `ownerShipType = 1` to mark items as owned by the killer
- Applied to both regular loot table drops and random bonus drops

**Code Added**:
```kotlin
// Set timers: killer sees for 1 minute, then everyone for 3 minutes
newGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE // 100 cycles = 1 minute
newGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4 // 400 cycles = 4 minutes total
newGroundItem.ownerShipType = 1 // Set ownership type to "Self Player"
```

#### 2. Default Public Delay Configuration
**File**: [game-server/src/main/kotlin/org/alter/game/Server.kt](../game-server/src/main/kotlin/org/alter/game/Server.kt)

**Changes**:
- Changed default `gItemPublicDelay` from `0` to `GroundItem.DEFAULT_PUBLIC_SPAWN_CYCLES` (100 cycles = 1 minute)
- Prevents items from becoming public immediately when spawned

**Before**:
```kotlin
gItemPublicDelay = gameProperties.getOrDefault("gitem-public-spawn-delay", 0)
```

**After**:
```kotlin
gItemPublicDelay = gameProperties.getOrDefault("gitem-public-spawn-delay", GroundItem.DEFAULT_PUBLIC_SPAWN_CYCLES)
```

#### 3. Default Despawn Delay Update
**File**: [game-server/src/main/kotlin/org/alter/game/model/entity/GroundItem.kt](../game-server/src/main/kotlin/org/alter/game/model/entity/GroundItem.kt)

**Changes**:
- Updated `DEFAULT_DESPAWN_CYCLES` from `300` to `400` cycles (4 minutes total)
- Matches the intended behavior: 1 minute private + 3 minutes public

**Before**:
```kotlin
const val DEFAULT_DESPAWN_CYCLES = 300
```

**After**:
```kotlin
const val DEFAULT_DESPAWN_CYCLES = 400 // 4 minutes: 1 min private + 3 min public
```

### How It Works

**Timeline**:
- **Cycles 0-99 (1 minute)**: Item is private - only the killer can see it
- **Cycles 100-399 (3 minutes)**: Item becomes public - everyone can see it
- **Cycle 400+**: Item despawns

**Mechanics**:
- Items spawn with `owner = killer`, making them private initially
- `World.kt` cycle logic checks `currentCycle >= gItemPublicDelay` (100) to make items public
- When item becomes public, `removeOwner()` is called and `ownerShipType` is set to 0
- Despawn check uses `currentCycle >= gItemDespawnDelay` (400) when item is public

### Impact
- ✅ NPC drops now have proper timer-based visibility
- ✅ Killers get exclusive access to their drops for 1 minute
- ✅ Other players can see drops after 1 minute
- ✅ Drops despawn after 4 minutes total (1 min private + 3 min public)
- ✅ Restores original intended behavior for drop visibility

---

## Date: 2025-01-XX (NPC Loot System Improvements)

### Summary
Fixed NPC loot drop system with 4 major improvements:
1. Added missing loot drops to NPCs (cows, KBD, Barrows brothers)
2. Fixed String item identifier handling in loot system
3. Fixed ground item stacking for stackable items
4. Fixed coin ID issue (617 -> 995) in all loot tables

---

## NPC Loot System Improvements

### 1. Added Missing NPC Loot Drops
**Files**:
- [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/CowPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/CowPlugin.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/kbd/KbdConfigsPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/kbd/KbdConfigsPlugin.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/barrows/*.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/barrows/)

**Issue**: Several NPCs had combat definitions but no loot drops configured, causing them to drop nothing when killed.

**NPCs Fixed**:
- **Cows**: Added bones, raw beef, and cowhide drops
- **King Black Dragon**: Uncommented and fixed loot table with dragon bones, black dragonhide, and high-tier items
- **Barrows Brothers** (Ahrim, Dharok, Guthan, Karil, Torag, Verac): Added bones, coins, and rune drops

**Impact**:
- All NPCs with combat definitions now have proper loot drops
- Players receive rewards when killing NPCs
- Consistent loot system across all NPCs

---

### 2. String Item Identifier Support in Loot System
**File**: [game-server/src/main/kotlin/org/alter/game/model/weightedTableBuilder/LootTableBuilder.kt](../game-server/src/main/kotlin/org/alter/game/model/weightedTableBuilder/LootTableBuilder.kt)

**Issue**: Loot system threw `Unhandled drop type: class java.lang.String` when using string item identifiers like `"item.bones"` in loot tables.

**Root Cause**:
- `Loot.handleToItem()` only handled `Int`, `LootTable`, and `KFunction<*>` types
- String identifiers (e.g., `"item.bones"`) were not converted to Int IDs

**Fix Applied**:
- Added `String` case in `handleToItem()` function (lines 122-126)
- Converts string identifiers to Int IDs using `RSCM.getRSCM()`
- Allows loot tables to use readable string identifiers instead of hardcoded IDs

**Code Change**:
```kotlin
is String -> {
    // Convert string item identifier (e.g., "item.bones") to Int ID using RSCM
    val itemId = getRSCM(item)
    items.add(GroundItem(itemId, amount = randomStep(min, max, steepness), tile = tile))
}
```

**Impact**:
- Loot tables can now use string identifiers (`"item.bones"`, `"item.coins_995"`, etc.)
- More maintainable and readable loot configurations
- No more crashes when processing string-based loot drops

---

### 3. Ground Item Stacking Fix
**File**: [game-server/src/main/kotlin/org/alter/game/model/World.kt](../game-server/src/main/kotlin/org/alter/game/model/World.kt)

**Issue**: Stackable items (like coins) dropped by NPCs weren't combining on the ground, causing multiple separate stacks that didn't stack in inventory.

**Root Cause**:
- `World.spawn()` only combined stackable items with the same `ownerUID`
- When items became public (owner removed), they couldn't combine with newly dropped items that still had an owner
- This caused coins to drop as separate stacks that didn't stack in inventory

**Fix Applied**:
- Modified ground item combination logic (lines 441-465)
- Stackable items now combine when:
  - They have the same owner, OR
  - At least one is public (no owner), OR
  - Both have no owner
- Ensures public items combine with any matching stackable items

**Code Change**:
```kotlin
val oldItem =
    chunk.getEntities<GroundItem>(tile, EntityType.GROUND_ITEM).firstOrNull {
        it.item == item.item && (
            it.ownerUID == item.ownerUID || 
            it.isPublic() || 
            item.isPublic() ||
            (it.ownerUID == null && item.ownerUID == null)
        )
    }
```

**Impact**:
- Stackable items (coins, runes, etc.) properly combine on the ground
- Items stack correctly in inventory when picked up
- Better loot management and inventory space usage

---

### 4. Coin ID Fix (617 -> 995)
**Files**:
- [game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/CombatConfigPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/areas/varrock/CombatConfigPlugin.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/CombatConfigPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/CombatConfigPlugin.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/barrows/*.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/barrows/)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/dragons/DragonConfigsPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/dragons/DragonConfigsPlugin.kt)

**Issue**: NPCs were dropping coin ID 617 instead of the standard coin ID 995, causing coins not to stack with normal coins in inventory.

**Root Cause**:
- RSCM file has two coin entries:
  - `coins:617` (wrong/old coin type)
  - `coins_995:995` (correct standard coins)
- Loot tables were using `"item.coins"` which resolved to ID 617
- Should have been using `"item.coins_995"` for ID 995

**Fix Applied**:
- Replaced all `"item.coins"` with `"item.coins_995"` in all loot tables:
  - Varrock guards
  - Wilderness NPCs (skeletons, bandits, dark wizards, etc.)
  - All Barrows brothers
  - All dragon types

**Impact**:
- NPCs now drop standard coins (ID 995) that stack properly
- Coins stack correctly with existing coins in inventory
- Consistent coin system across the game

---

## Date: 2025-11-15 (Critical Bug Fixes - Round 6)

### Summary
Fixed 7+ critical bugs in door/gate mechanics, shop systems, and deposit boxes:
1. Fixed DoorPlugin and GatePlugin attribute copying crashes (2 fixes)
2. Fixed ItemCurrency shop slot access crashes (3 fixes)
3. Fixed DepositBoxPlugin item swap crashes (2 fixes)

---

## Bug Fixes - Round 6

### 1. DoorPlugin and GatePlugin Attribute Safety
**Files**:
- [game-plugins/src/main/kotlin/org/alter/plugins/content/objects/door/DoorPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/objects/door/DoorPlugin.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/objects/gates/GatePlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/objects/gates/GatePlugin.kt)

**Issue**: Server would crash when opening/closing doors or gates with "sticky" states.

**Root Causes**:
- DoorPlugin line 110 & GatePlugin line 52: `from.attr[STICK_STATE]!!` unsafe
- Assumes STICK_STATE attribute always exists after has() check
- Race condition if attribute removed between has() and access

**Fixes Applied**:
- **DoorPlugin lines 108-113**: Safe attribute copying
  ```kotlin
  fun copyStickVars(from: GameObject, to: GameObject) {
      val stickState = from.attr[STICK_STATE]
      if (stickState != null) {
          to.attr[STICK_STATE] = stickState
      }
  }
  ```
- **GatePlugin lines 50-55**: Same pattern for gate objects

**Impact**:
- Door and gate interactions never crash from null attributes
- Sticky state preserved correctly when doors/gates are opened/closed
- Safe attribute copying for all game objects

---

### 2. ItemCurrency Shop Slot Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/shops/ItemCurrency.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/shops/ItemCurrency.kt)

**Issue**: Shop transactions would crash when updating item quantities.

**Root Causes**:
- Line 149: `shop.items[slot]!!.currentAmount` assumes slot never becomes null
- Lines 198, 203: Same pattern when selling to shop
- Shop item could be removed by another player mid-transaction
- Race conditions in multi-player shop access

**Fixes Applied**:
- **Lines 148-162**: Safe shop item access when buying
  ```kotlin
  if (add.completed > 0 && shopItem.amount != Int.MAX_VALUE) {
      val currentShopItem = shop.items[slot]
      if (currentShopItem != null) {
          currentShopItem.currentAmount -= add.completed

          if (currentShopItem.amount == 0 && currentShopItem.isTemporary == true) {
              shop.items[slot] = null
          }

          shop.refresh(p.world)
      }
  }
  ```
- **Lines 199-212**: Safe shop item access when selling
  ```kotlin
  if (shopSlot != -1) {
      val existingShopItem = shop.items[shopSlot]
      if (existingShopItem != null) {
          existingShopItem.currentAmount += amount
      }
  } else {
      val freeSlot = shop.items.indexOfFirst { it == null }
      check(freeSlot != -1)
      val newShopItem = ShopItem(unnoted, amount = 0)
      shop.items[freeSlot] = newShopItem
      newShopItem.currentAmount = amount
  }
  ```

**Impact**:
- Shop transactions never crash from concurrent access
- Safe multi-player shop interactions
- Proper handling of temporary items being removed

---

### 3. DepositBoxPlugin Item Swap Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/objects/depositbox/DepositBoxPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/objects/depositbox/DepositBoxPlugin.kt)

**Issue**: Players would crash when swapping items in deposit box inventory.

**Root Causes**:
- Lines 90-91: `player.attr[INTERACTING_ITEM_SLOT]!!` and `player.attr[OTHER_ITEM_SLOT_ATTR]!!` unsafe
- Attributes could be null during rapid drag operations
- Similar to InventoryPlugin but in deposit box context

**Fixes Applied**:
- **Lines 90-97**: Safe slot attribute access
  ```kotlin
  val srcSlot = player.attr[INTERACTING_ITEM_SLOT] ?: run {
      player.message("Invalid source slot.")
      return@onComponentToComponentItemSwap
  }
  val dstSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: run {
      player.message("Invalid destination slot.")
      return@onComponentToComponentItemSwap
  }
  ```

**Impact**:
- Deposit box item swapping never crashes
- Clear error messages when drag operations fail
- Consistent behavior with regular inventory swapping

---

## Date: 2025-11-15 (Critical Bug Fixes - Round 5)

### Summary
Fixed 6+ critical bugs in player interaction systems:
1. Fixed InventoryPlugin item swapping crashes (2 fixes)
2. Fixed TradeSession null pointer exception (1 fix)
3. Fixed AttackTabPlugin special attack crashes (2 fixes)

---

## Bug Fixes - Round 5

### 1. InventoryPlugin Item Swap Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/inventory/InventoryPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/inventory/InventoryPlugin.kt)

**Issue**: Players would crash when swapping inventory items.

**Root Causes**:
- Lines 86-87: `player.attr[INTERACTING_ITEM_SLOT]!!` and `player.attr[OTHER_ITEM_SLOT_ATTR]!!` unsafe
- Attributes could be null during rapid drag operations
- Race conditions when swapping items quickly

**Fixes Applied**:
- **Lines 86-93**: Replaced double `!!` with safe null checks
  ```kotlin
  val srcSlot = player.attr[INTERACTING_ITEM_SLOT] ?: run {
      player.writeMessage("Invalid source slot.")
      return@onComponentToComponentItemSwap
  }
  val dstSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: run {
      player.writeMessage("Invalid destination slot.")
      return@onComponentToComponentItemSwap
  }
  ```

**Impact**:
- Inventory item swapping never crashes from null attributes
- Players receive clear error messages instead of disconnecting
- Drag-and-drop operations are now fully crash-safe

---

### 2. TradeSession Partner Session Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/trading/impl/TradeSession.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/trading/impl/TradeSession.kt)

**Issue**: Trading would crash when checking inventory space requirements.

**Root Cause**:
- Line 298: `partner.getTradeSession()!!` assumes partner always has active trade session
- Partner could log out or cancel trade between checks
- Race condition during trade progression

**Fix Applied**:
- **Lines 298-310**: Safe trade session retrieval with validation
  ```kotlin
  val partnerSession = partner.getTradeSession()
  if (partnerSession == null) {
      player.message("Trading partner is no longer in a trade.")
      decline(forced = true)
      return
  }

  if (player.inventory.freeSlotCount < partnerSession.container.occupiedSlotCount) {
      // ... inventory space check
  }
  ```

**Impact**:
- Trading system never crashes from missing partner session
- Graceful handling when partner logs out mid-trade
- Clear error messages for players when trades fail

---

### 3. AttackTabPlugin Special Attack Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/combat_options/AttackTabPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/combat_options/AttackTabPlugin.kt)

**Issues**: Players would crash when trying to use special attacks without a weapon equipped.

**Root Causes**:
- Lines 76, 90: `player.equipment[EquipmentType.WEAPON.id]!!.id` assumes weapon always equipped
- Players could click special attack button with no weapon
- Weapon could be unequipped after interface opens

**Fixes Applied**:
- **Lines 76-88**: Safe weapon retrieval for first button
  ```kotlin
  val weapon = player.equipment[EquipmentType.WEAPON.id]
  if (weapon == null) {
      player.message("You need a weapon to use special attacks.")
      return@onButton
  }

  if (SpecialAttacks.executeOnEnable(weapon.id)) {
      // ... execute special attack
  }
  ```

- **Lines 95-107**: Same pattern for second special attack button (component 36)

**Impact**:
- Special attack button never crashes when no weapon equipped
- Clear feedback to players: "You need a weapon to use special attacks."
- Unarmed combat doesn't cause interface crashes

---

## Date: 2025-11-15 (Critical Bug Fixes - Round 4)

### Summary
Fixed an additional 10+ critical bugs in core server systems:
1. Fixed BankTabsPlugin tab swapping crashes (3 fixes)
2. Fixed World.kt service loading null pointer exceptions (3 fixes)
3. Fixed Chunk.kt zone update crashes (4 fixes)

---

## Bug Fixes - Round 4

### 1. BankTabsPlugin Tab Management Safety
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/bank/BankTabsPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/bank/BankTabsPlugin.kt)

**Issues**:
- Bank tab swapping and item movement could crash with null pointer exceptions
- No validation when dragging items between tabs
- Missing attribute checks for component interactions

**Root Causes**:
- Lines 110, 114: `player.attr[INTERACTING_COMPONENT_CHILD]!!` and `player.attr[OTHER_ITEM_SLOT_ATTR]!!` unsafe
- Lines 129-130: `player.attr[INTERACTING_ITEM_SLOT]!!` and `player.attr[OTHER_ITEM_SLOT_ATTR]!!` unsafe
- Attributes could be null if interaction state is cleared or corrupted
- Race conditions during rapid bank operations

**Fixes Applied**:
- **Lines 110-120**: Replaced double `!!` with safe null checks and early returns
  - Added "Invalid bank operation" message for missing component child
  - Added "Invalid destination slot" message for missing destination

- **Lines 135-142**: Safe tab attribute access with validation
  - Added "Invalid source tab" message
  - Added "Invalid destination tab" message
  - Prevents crashes when tab indices are corrupted

**Impact**:
- Bank tab operations never crash from null attributes
- Players receive clear error messages instead of disconnecting
- Tab swapping, insertion, and item movement are now crash-safe

---

### 2. World Service Loading Robustness
**File**: [game-server/src/main/kotlin/org/alter/game/model/World.kt](../game-server/src/main/kotlin/org/alter/game/model/World.kt)

**Issues**:
- Server startup would crash if service configuration was malformed
- No error handling for missing or invalid service classes
- Unsafe null assertions and type casts during service initialization

**Root Causes**:
- Line 671: `gameProperties.get<ArrayList<Any>>("services")!!` crashes if property missing
- Line 675: `Class.forName(className).asSubclass(Service::class.java)!!` crashes on invalid class
- No try-catch around Class.forName() for ClassNotFoundException
- No validation that class implements Service interface

**Fixes Applied**:
- **Lines 671-673**: Safe service list retrieval
  ```kotlin
  val foundServices = gameProperties.get<ArrayList<Any>>("services") ?: run {
      logger.warn { "No services defined in game configuration." }
      return
  }
  ```

- **Lines 676-683**: Safe service configuration parsing
  - Added safe cast `s as? LinkedHashMap<*, *>`
  - Added validation for 'class' field existence
  - Error logged and service skipped if invalid

- **Lines 684-692**: Comprehensive class loading error handling
  ```kotlin
  val clazz = try {
      Class.forName(className).asSubclass(Service::class.java)
  } catch (e: ClassNotFoundException) {
      logger.error(e) { "Service class not found: $className" }
      return@forEach
  } catch (e: ClassCastException) {
      logger.error(e) { "Class $className does not implement Service interface" }
      return@forEach
  }
  ```

- **Lines 538-542**: Fixed getObject() chunk retrieval
  - Replaced `chunks.get(tile, createIfNeeded = true)!!` with safe null check
  - Returns null gracefully if chunk cannot be created

**Impact**:
- Server starts successfully even with malformed service configuration
- Individual service failures don't crash entire server startup
- Detailed error logging helps identify configuration problems
- Object lookups never crash from chunk failures

---

### 3. Chunk Zone Update Client Safety
**File**: [game-server/src/main/kotlin/org/alter/game/model/region/Chunk.kt](../game-server/src/main/kotlin/org/alter/game/model/region/Chunk.kt)

**Issues**:
- Zone updates could crash when sending to clients
- Unsafe null assertions on client region base and payload data
- No validation before creating or sending updates

**Root Causes**:
- Line 170: `createUpdateFor(item, spawn = true)!!` assumes update creation always succeeds
- Line 194: `computed[OldSchoolClientType.DESKTOP]!!` assumes payload always exists
- Line 286: `client.lastKnownRegionBase!!` crashes if client region not initialized
- Line 292: Another `computed[OldSchoolClientType.DESKTOP]!!` unsafe access

**Fixes Applied**:
- **Lines 170-174**: Safe update creation for ground items
  ```kotlin
  val newUpdate = createUpdateFor(item, spawn = true)
  if (newUpdate != null) {
      updates.add(newUpdate)
  }
  ```

- **Lines 197-200**: Safe payload retrieval for single client
  ```kotlin
  val payload = computed[OldSchoolClientType.DESKTOP]
  if (payload != null) {
      p.write(UpdateZonePartialEnclosed(..., payload = payload))
  }
  ```

- **Lines 286-296**: Safe region base and payload for broadcast
  ```kotlin
  val regionBase = client.lastKnownRegionBase ?: continue
  val local = regionBase.toLocal(this.coords.toTile())
  // ... compute messages ...
  val payload = computed[OldSchoolClientType.DESKTOP]
  if (payload != null) {
      client.write(UpdateZonePartialEnclosed(..., payload = payload))
  }
  ```

**Impact**:
- Zone updates (ground items, objects, projectiles) never crash clients
- Clients without initialized regions are skipped safely
- Missing payload data doesn't disconnect nearby players
- Ground item count updates are fully protected

---

## Date: 2025-11-15 (NPC Loot Drop System Implementation)

### Summary
**MAJOR FEATURE**: Implemented complete NPC loot drop system - NPCs now drop items when killed!

Fixed the core issue where NPCs would die but never drop any loot, making combat unrewarding. This was a fundamental missing feature that affected the entire gameplay experience.

### Key Changes:
1. **NPC Loot Drop System**: Created universal death handler for all NPCs
2. **Wilderness Loot Tables**: Added loot drops for all 63+ wilderness monsters  
3. **Lumbridge Combat Config**: Added combat definitions and loot for 30+ creatures
4. **Compilation Fix**: Fixed Kotlin return statement error in GroundItemRouteAction

**Impact**: Combat is now rewarding with proper loot drops. Over 90+ NPCs now drop appropriate items including coins, weapons, armor, runes, and rare drops.

---

## Features

### 1. Universal NPC Loot Drop Handler
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt`

**Feature**: Created universal death handler that processes loot drops for any NPC with configured loot tables.

**Implementation**:
- Hooks into `onAnyNpcDeath` to catch all NPC deaths
- Retrieves killer (player who dealt most damage) from NPC attributes
- Extracts loot tables from NPC's combat definition (`npc.combatDef.LootTables`)
- Uses existing `roll(player, lootTables)` function to generate drops
- Spawns ground items at NPC death location with proper ownership
- Includes error handling for misconfigured loot tables
- Optional messaging for high-value drops

**Root Cause**: The `NpcDeathAction.kt` imported the `roll` function but never called it, so loot tables were never processed.

**Impact**: All NPCs with configured loot tables now properly drop items when killed.

---

### 2. Wilderness Monster Loot Tables
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/CombatConfigPlugin.kt`

**Feature**: Added comprehensive loot tables for all wilderness monsters (63+ NPCs).

**Monsters Configured**:
- **Dark Wizards** (9 spawns): Bones + runes, wizard gear (rare)
- **Skeletons** (12 spawns): Bones + bronze/iron weapons and arrows  
- **Bandits** (8 spawns): Bones + coins, weapons, lockpicks
- **Chaos Druids** (5 spawns): Bones + herbs, nature/chaos/law runes
- **Wolves** (9 spawns): Bones + raw beef, cowhide, wolf bones
- **Dark Warriors** (6 spawns): Bones + higher-tier weapons and runes
- **Green Dragons** (8 spawns): Dragon bones/hide + runes, weapons, rare drops (dragon med helm 1/128, shield left half 1/256)
- **Hellhounds** (6 spawns): Bones + high-value runes, gems, rare equipment (dragon spear 1/512, rune platebody 1/384)

**Loot Table Structure**:
- **Always**: Guaranteed drops (bones for all monsters)
- **Main**: Weighted drop table with common items
- **Tertiary**: Independent rare drop rolls

**Impact**: Wilderness combat is now rewarding with appropriate risk/reward balance.

---

### 3. Lumbridge Area Combat System
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/lumbridge/LumbridgeCombatConfigPlugin.kt`

**Feature**: Created complete combat definitions and loot systems for all Lumbridge creatures (30+ NPCs).

**Creatures Configured**:
- **Rats** (6+ spawns): 5 HP, bones + coins, raw rat meat
- **Giant Spiders** (7+ spawns): 15 HP, bones + spider silk, spider legs  
- **Goblins** (8+ spawns): 18 HP, aggressive, bones + bronze weapons, goblin mail
- **Imps** (1 spawn): 12 HP, fast attacks, bones + beads, bread, imp jar (rare)
- **Sheep** (5+ spawns): 8 HP, peaceful, bones + wool, raw mutton
- **Rams** (3+ spawns): 12 HP, mildly aggressive, bones + better wool rewards
- **Zombie Rats** (2 spawns): 8 HP, bones + coins, rotten food

**Combat Characteristics**:
- Appropriate HP levels for new player progression
- Aggression settings (some passive, some aggressive)
- Proper attack speeds and respawn delays
- Thematic loot drops matching creature types

**Impact**: New players now have proper progression with rewarding low-level combat.

---

## Bug Fixes

### 1. GroundItemRouteAction Compilation Error
**File**: `game-server/src/main/kotlin/org/alter/game/model/move/GroundItemRouteAction.kt`

**Issue**: Kotlin compilation error - `'return' is prohibited here` on lines 32 and 36.

**Root Cause**: `return@walkPlugin` statements inside `run` blocks within `player.queue` lambda caused context conflicts.

**Fix Applied**: Added explicit lambda label `walkPlugin@{...}` to resolve return statement ambiguity.

**Impact**: Project now compiles successfully without Kotlin errors.

---

## Date: 2025-11-15 (Critical Bug Fixes - Round 3)

### Summary
Fixed an additional 14+ critical bugs across tournament supplies, combat formulas, and item sets:
1. Fixed TournamentSuppliesPlugin null-safety and array bounds issues (7+ fixes)
2. Fixed unsafe NPC species type casts in combat formulas (6 fixes)
3. Fixed ItemsetsPlugin null-safety and exception handling (4+ fixes)

---

## Bug Fixes - Round 3

### 1. TournamentSuppliesPlugin Safety Improvements
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/tournament_supplies/TournamentSuppliesPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/tournament_supplies/TournamentSuppliesPlugin.kt)

**Issues**:
- Unsafe null assertions could crash tournament interface
- Empty catch block silently swallowed inventory errors
- No array bounds validation on inventory slots

**Root Causes**:
- Line 35: `player.attr[INTERACTING_ITEM_ID]!!` crashes if attribute missing
- Lines 71-84: Multiple `player.inventory[slot]!!` without bounds checking
- Lines 85-87: Empty catch block with only `printStackTrace()`, no user feedback

**Fixes Applied**:
- **Line 35**: Replaced `player.attr[INTERACTING_ITEM_ID]!!` with safe `?: return@onButton`
- **Lines 70-84**: Complete rewrite with safe null handling:
  - Added null check for slot attribute with early return
  - Added bounds validation: `slot !in 0 until player.inventory.capacity`
  - Extracted inventory item once, stored in `item` variable
  - Removed all `!!` operators from inventory access
  - Added user-friendly error messages
- **Removed empty catch block**: Replaced with proper error handling throughout

**Impact**:
- Tournament supplies interface never crashes from null attributes
- Players receive clear error messages instead of silent failures
- Array bounds violations prevented before accessing inventory

---

### 2. Combat Formula Type-Safety Improvements
**Files**:
- [game-plugins/src/main/kotlin/org/alter/plugins/content/combat/formula/MeleeCombatFormula.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/combat/formula/MeleeCombatFormula.kt)
- [game-plugins/src/main/kotlin/org/alter/plugins/content/combat/formula/RangedCombatFormula.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/combat/formula/RangedCombatFormula.kt)

**Issue**: NPC species checks used unsafe type casts that could cause ClassCastException.

**Root Causes**:
- Lines used pattern `pawn.entityType.isNpc` check followed by `pawn as Npc` cast
- Smart casting not triggered by entity type property checks
- Risk if entity type flags become inconsistent with actual type

**Fixes Applied** (MeleeCombatFormula.kt):
- **isDemon() (Lines 296-302)**: Changed from `pawn as Npc` to `pawn is Npc` with smart casting
- **isShade() (Lines 304-310)**: Same pattern fix for shade species check
- **isKalphite() (Lines 312-318)**: Same pattern fix for kalphite species check
- **isScarab() (Lines 320-326)**: Same pattern fix for scarab species check
- **isWearingDharok() (Lines 328-337)**: Changed from `pawn as Player` to `pawn is Player`
- **isWearingVerac() (Lines 339-348)**: Same pattern fix for Verac's set check

**Fixes Applied** (RangedCombatFormula.kt):
- **isDragon() (Lines 438-444)**: Changed from `pawn as Npc` to `pawn is Npc` with smart casting
- **isFiery() (Lines 446-452)**: Same pattern fix for fiery species check

**Impact**:
- Combat formulas never crash from type mismatches
- Smart casting eliminates ClassCastException risk
- Bonus damage calculations (Darklight vs demons, Keris vs kalphites, dragonbane vs dragons) are now crash-safe

---

### 3. ItemsetsPlugin Null-Safety and Exception Handling
**File**: [game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/itemsets/ItemsetsPlugin.kt](../game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/itemsets/ItemsetsPlugin.kt)

**Issues**:
- Unsafe null assertions could crash item set interface
- Empty catch blocks silently swallowed errors with no user feedback
- No proper logging of failures

**Root Causes**:
- Lines 35-36: Double `!!` operators on item ID and option attributes
- Line 76: Another `!!` on item ID attribute
- Line 82: Unsafe `!!` on slot attribute
- Lines 68-70, 85-87, 95-97, 102-104: Empty catch blocks with only `printStackTrace()`

**Fixes Applied**:
- **Lines 35-38**: Replaced double `!!` with safe early returns:
  ```kotlin
  val itemId = player.attr[INTERACTING_ITEM_ID] ?: return@onButton
  val option = player.attr[INTERACTING_OPT_ATTR] ?: return@onButton
  ```

- **Lines 70-73**: Improved exception handling for item set creation:
  ```kotlin
  catch (e: Exception) {
      player.writeMessage("Error creating item set: ${e.message}")
      logger.error(e) { "Failed to create item set for player ${player.username}" }
  }
  ```

- **Lines 84-94**: Added comprehensive null checks for inventory operations:
  - Safe slot attribute access with error message
  - Null check on inventory item with user feedback
  - Removed all `!!` operators

- **Lines 105-115**: Improved exception handling for unpacking:
  ```kotlin
  catch (e: Exception) {
      player.writeMessage("Error unpacking item set: ${e.message}")
      logger.error(e) { "Failed to unpack item set for player ${player.username}" }
  }
  ```

- **Added proper logging**: Imported KotlinLogging and added logger instance

**Impact**:
- Item set interface never crashes from null attributes
- Players receive clear error messages when operations fail
- Server logs capture detailed error information for debugging
- Inventory operations are fully protected with bounds checks

---

## Date: 2025-11-15 (Critical Null-Safety Bug Fixes - Round 2)

### Summary
Fixed an additional 8+ critical type-safety and array bounds bugs:
1. Fixed unsafe type casts in combat strategies (5 fixes)
2. Fixed array index out of bounds in bank tab system (2 fixes)
3. Fixed NPC block sound crash when attacker is not a player

---

## Bug Fixes - Round 2

### 1. Combat Strategy Type-Safety Improvements
**Files**:
- `game-plugins/src/main/kotlin/org/alter/plugins/content/combat/strategy/MeleeCombatStrategy.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/combat/strategy/RangedCombatStrategy.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/combat/strategy/MagicCombatStrategy.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/combat/Combat.kt`

**Issue**: Combat system used unsafe type casts that could cause ClassCastException.

**Root Cause**:
- Lines used pattern `pawn.entityType.isPlayer` check followed by `pawn as Player` cast
- Kotlin smart casting wasn't triggered by entity type checks
- Risk if entity type flags became inconsistent

**Fixes Applied**:
- **MeleeCombatStrategy.kt (Line 69-70)**: Replaced `pawn.entityType.isPlayer && pawn as Player` with `pawn is Player`
- **RangedCombatStrategy.kt (Line 177-178)**: Same pattern fix for ranged combat XP
- **MagicCombatStrategy.kt (Line 78-79)**: Same pattern fix for magic combat XP
- **Combat.kt (Line 99-100)**: Changed unsafe `pawn as Player` to safe `pawn is Player` check with early return
- **Combat.kt (Line 107)**: Replaced `target.attr[COMBAT_TARGET_FOCUS_ATTR]!!.get()` with safe nullable access

**Impact**:
- Combat XP calculation no longer crashes if entity type is mismatched
- NPC vs NPC combat won't crash when trying to play block sounds
- Smart casting eliminates risk of ClassCastException

---

### 2. Bank Tab Array Bounds Protection
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/bank/BankTabs.kt`

**Issue**: Bank tab indexing could cause ArrayIndexOutOfBoundsException.

**Root Cause**:
- Lines 199, 224: `player.bank[dex - 1]` accessed without bounds checking
- If `dex` becomes 0, accessing `bank[-1]` causes crash
- Tab varbit corruption could cause `dex` to exceed bank capacity

**Fixes Applied**:
- **getPlaceholderInsertionPoint()** (Line 199-202):
  - Added bounds check: `dex > 0 && dex <= player.bank.capacity`
  - Added `coerceIn(0, player.bank.capacity)` to clamp result

- **newInsertionPoint()** (Line 224-230):
  - Same bounds checking pattern
  - Additional check before accessing `player.bank[dex]`
  - Prevents index -1 and index >= capacity

**Impact**: Bank tab operations never crash from out-of-bounds access, even with corrupted varbits.

---

## Date: 2025-11-15 (Critical Null-Safety Bug Fixes - Round 1)

### Summary
Fixed 11+ critical null pointer exceptions that could crash the server or players:
1. Fixed XteaKeyService crashes when loading regions without XTEA keys
2. Fixed PluginRepository core plugin null assertions (4 fixes)
3. Fixed ObjectPathAction double null assertions (5 fixes)
4. Fixed GroundItemRouteAction crashes when picking up items
5. Fixed BankPlugin incinerator crash on empty slots
6. Added comprehensive error logging system

---

## Bug Fixes

### 1. XteaKeyService Null-Safety Improvements
**File**: `game-server/src/main/kotlin/org/alter/game/service/xtea/XteaKeyService.kt`

**Issue**: Server could crash when loading map regions that didn't have XTEA keys defined.

**Root Cause**:
- Lines 54 and 149 used `!!` operator after null check
- If the null check failed due to race conditions, server would crash

**Fix Applied**:
- Replaced `keys[region]!!` with `keys[region] ?: EMPTY_KEYS`
- Added safe fallback to ensure method never returns null
- Provides empty keys (all zeros) for unmapped regions

**Impact**: Server no longer crashes when players enter regions without defined XTEA keys. Map loading continues gracefully.

---

### 2. PluginRepository Core Plugin Safety
**File**: `game-server/src/main/kotlin/org/alter/game/plugin/PluginRepository.kt`

**Issue**: Server could crash if core plugins (combat, window status, modal close, menu) weren't registered.

**Root Cause**:
- Lines 587, 746, 762, 776 used `!!` operator after null checks
- Plugin binding could fail during server startup, causing crashes in core gameplay

**Fixes Applied**:
- **executeCombat()** (Line 587): Replaced `combatPlugin!!` with safe `.let{}` call
- **executeWindowStatus()** (Line 746): Replaced `windowStatusPlugin!!` with safe `.let{}` and warning log
- **executeModalClose()** (Line 762): Replaced `closeModalPlugin!!` with safe `.let{}` and warning log
- **isMenuOpened()** (Line 776): Replaced `isMenuOpenedPlugin!!` with safe `.let{}` returning false default

**Impact**: Core gameplay systems no longer crash if plugins fail to load. Server logs warnings instead of crashing.

---

### 3. ObjectPathAction Player Interaction Fixes
**File**: `game-server/src/main/kotlin/org/alter/game/model/move/ObjectPathAction.kt`

**Issue**: Players would crash when interacting with objects or using items on objects.

**Root Cause**:
- Lines 66-67: Double null assertions `player.attr[INTERACTING_ITEM]!!.get()!!`
- Lines 85, 90: Similar pattern for object interactions
- WeakReferences could be garbage collected between checks
- Attributes might not be set in all code paths

**Fixes Applied**:
- **itemOnObjectPlugin** (Lines 66-73):
  - Replaced double `!!` with safe `?.get() ?: run { return }`
  - Added user-friendly error message
  - Early return prevents crash

- **objectInteractPlugin** (Lines 85-98):
  - Replaced `obj!!.get()!!` with safe nullable checks
  - Replaced `opt!!` with null check and early return
  - Added proper error handling

**Impact**: Players no longer crash when:
- Using items on objects (trees, rocks, etc.)
- Clicking on objects
- Objects despawn during interaction
- Memory pressure causes WeakReference cleanup

---

### 4. GroundItemRouteAction Pickup Safety
**File**: `game-server/src/main/kotlin/org/alter/game/model/move/GroundItemRouteAction.kt`

**Issue**: Players would crash when attempting to pick up ground items.

**Root Cause**:
- Lines 30-31: Double null assertions `player.attr[INTERACTING_GROUNDITEM_ATTR]!!.get()!!`
- Items could despawn before player reaches them
- WeakReferences vulnerable to garbage collection

**Fix Applied**:
- Replaced `item!!.get()!!` with `?.get() ?: run { return }`
- Replaced `opt!!` with null check and early return
- Added graceful error message for missing items

**Impact**: Players no longer crash when:
- Items despawn while walking to them
- Another player picks up the item first
- Server is under memory pressure

---

### 5. BankPlugin Incinerator Safety
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/bank/BankPlugin.kt`

**Issue**: Players would crash when using incinerator on empty bank slots.

**Root Cause**:
- Line 106: `player.bank[slot]!!` assumed slot always contains an item
- Players could click incinerator on empty slots
- Slot index could be out of sync with actual items

**Fix Applied**:
- Replaced `player.bank[slot]!!` with `player.bank[slot] ?: run { return }`
- Added user message: "There is no item in that slot."
- Prevents removal of non-existent items

**Impact**: Bank incinerator now safely handles clicks on empty slots without crashing.

---

### 6. Logging System Enhancements
**File**: `game-server/src/main/resources/logback.xml`

**Feature**: Added comprehensive file logging for debugging and monitoring.

**Changes**:
- Added `logs/server.log` - All server logs (rotates daily, 7 day history)
- Added `logs/errors.log` - Only errors/warnings (rotates daily, 30 day history)
- Maintains console output for development
- Full stack traces in error log

**Impact**: Developers can now analyze crashes and errors via log files. Makes bug hunting significantly easier.

---

## Technical Details

### Null-Safety Pattern Used

**Before (Unsafe)**:
```kotlin
val item = player.attr[ATTR]!!.get()!!
```

**After (Safe)**:
```kotlin
val item = player.attr[ATTR]?.get() ?: run {
    player.writeMessage("Error message")
    return@plugin
}
```

### Benefits
1. **No crashes**: Null cases handled gracefully
2. **User feedback**: Players see helpful messages instead of disconnecting
3. **Server stability**: Individual player errors don't crash entire server
4. **Debugging**: Logs provide context for issues

---

## Date: 2025-11-15 (Auto-Save System and Teleport Menu Improvements)

### Summary
Implemented automatic periodic player saving and improved teleport menu navigation:
1. Added automatic player save system to prevent data loss
2. Redesigned teleport menu pagination for better navigation
3. Made auto-save interval configurable via game.yml

---

## Features

### 1. Automatic Player Save System
**Files**:
- `game-server/src/main/kotlin/org/alter/game/task/AutoSaveTask.kt` (NEW)
- `game-server/src/main/kotlin/org/alter/game/service/GameService.kt`
- `game-server/src/main/kotlin/org/alter/game/GameContext.kt`
- `game-server/src/main/kotlin/org/alter/game/Server.kt`
- `game.yml`

**Feature**: Implemented periodic auto-save system to save all online players automatically.

**Details**:
- Created new `AutoSaveTask` game task that runs every game cycle
- Auto-saves all online players at configurable intervals (default: every 100 cycles / 1 minute)
- Prevents data loss from server crashes or unexpected disconnections
- Saves complete player state: position, skills, inventory, bank, equipment, appearance, etc.
- Provides logging of save operations with success/error counts
- Can be disabled by setting interval to 0 (not recommended)

**Configuration** (`game.yml`):
```yaml
# Auto-save configuration
# How often to automatically save all online players, in game cycles.
# At 600ms per cycle (default): 100 cycles = 1 minute, 200 cycles = 2 minutes
# Set to 0 to disable auto-save (not recommended - only saves on logout)
auto-save-interval: 100
```

**Impact**: Players no longer lose progress if the server crashes or they disconnect unexpectedly. Maximum data loss is now limited to the auto-save interval (default: 1 minute of gameplay).

---

### 2. Teleport Menu Pagination Redesign
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/commands/commands/all/TeleportsPlugin.kt`

**Feature**: Redesigned teleport menu to show only 5 options per page with simplified navigation.

**Details**:
- Changed from 8 locations per page to 3 locations per page
- Fixed pagination structure:
  - Option 1: "Previous Page" (1st option)
  - Options 2-4: Teleport locations (3 locations)
  - Option 5: "Next Page" (5th option)
- Total menu options: Always 5 (consistent UI)
- Navigation buttons always visible for ease of use
- Clicking Previous/Next on first/last page re-shows the same page (no action)
- Total pages increased from 7 to 18 (52 locations ÷ 3 per page)

**Impact**: Simpler, more consistent teleport menu navigation with exactly 5 clickable options per page.

---

## Technical Details

### AutoSaveTask Implementation
- **Execution**: Runs on every game cycle via `GameService.populateTasks()`
- **Interval Tracking**: Tracks cycles since last save with internal counter
- **Player Filtering**: Only saves fully initialized players (skips players still logging in)
- **Error Handling**: Try-catch around each player save with detailed error logging
- **Performance**: Minimal overhead - only executes save logic every N cycles
- **Logging**: Info-level logging on successful saves, error-level on failures

### Auto-Save Data Coverage
Everything saved by the auto-save system (via `PlayerSaving.savePlayer()`):
1. **Login credentials**: Username hash, password hash, XTEA keys
2. **Player details**: Position (tile x/z/height), privilege level, run energy, display mode
3. **Appearance**: Character looks, colors, gender
4. **Skills**: All skill levels and experience values (23 skills)
5. **Attributes**: Custom player attributes and flags
6. **Timers**: Active timers with offline tick calculations
7. **Containers**: Inventory, bank, equipment, and other item containers
8. **Varps**: Variable player states (interface states, settings, etc.)

### Configuration System
- `GameContext.autoSaveInterval`: Added to game context data class
- `Server.kt`: Loads interval from game.yml with default value of 100
- `AutoSaveTask(interval)`: Constructor parameter for configuration injection
- Can be changed at runtime by modifying game.yml and restarting server

---

## Date: 2025-01-XX (UI Navigation, Follow, and Trading Fixes)

### Summary
Fixed critical gameplay issues affecting user interface navigation, player following, and trading functionality:
1. Fixed display settings button (116:68) incorrectly navigating to audio settings
2. Implemented missing character follow functionality
3. Fixed trading system crash when interacting with players

---

## Bug Fixes

### 1. Display Settings Button Navigation Fix
**Files**: 
- `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/settings/options/tabs/OptionsTabFirstPlugin.kt`

**Issue**: Clicking the display settings button (interface 116, component 68) immediately redirected users back to audio settings (component 67) instead of opening display settings.

**Root Cause**: 
- Component 67 (audio settings) was missing a handler to set the `SETTINGS_TAB_FOCUS` varbit
- Component 68 (display settings) was incorrectly setting focus to 1 instead of 2

**Fix Applied**:
- Added missing handler for component 67 to set `SETTINGS_TAB_FOCUS` to 1 (audio settings)
- Corrected component 68 to set `SETTINGS_TAB_FOCUS` to 2 (display settings) instead of 1

**Impact**: Display settings button now correctly navigates to the display settings tab instead of redirecting to audio settings.

---

### 2. Character Follow Functionality Implementation
**Files**: 
- `game-server/src/main/kotlin/org/alter/game/model/attr/Attributes.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/OSRSPlugin.kt`
- `game-server/src/main/kotlin/org/alter/game/model/entity/Pawn.kt`

**Issue**: The "Follow" player option was sent to clients but had no handler implementation, causing the feature to be non-functional.

**Root Cause**: Missing `onPlayerOption("Follow")` handler and no attribute to track follow targets.

**Fix Applied**:
- Added `FOLLOWING_TARGET_ATTR` attribute to track which player is being followed
- Implemented `onPlayerOption("Follow")` handler with continuous following logic
- Created follow loop that:
  - Maintains 1-tile distance from target when possible
  - Automatically stops if target moves >15 tiles away or changes levels
  - Stops when player manually moves or interacts with other objects
- Added cleanup in `resetInteractions()` to clear follow state when interactions are reset

**Impact**: Players can now successfully follow other players. The system handles edge cases like target logout, distance limits, and manual movement cancellation.

---

### 3. Trading System Crash Fix
**Files**: 
- `game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/trading/TradingPlugin.kt`

**Issue**: Trading system would crash with NullPointerException when attempting to trade with another player.

**Root Cause**: The trading handler used unsafe `getInteractingPlayer()` method with non-null assertion (`!!`), which would throw exceptions if:
- `INTERACTING_PLAYER_ATTR` was null
- WeakReference to the target player was garbage collected
- Target player logged out before handler execution

**Fix Applied**:
- Replaced unsafe `getInteractingPlayer()` with null-safe attribute access
- Added proper null checking with error message if partner cannot be found
- Added validation to prevent trading with yourself
- Added check to ensure current player isn't already in a trade or locked

**Impact**: Trading system now works reliably without crashes. Better error handling provides clear feedback when trading cannot proceed.

---

## Date: 2025-11-15 (LAN Access, RSProx Compatibility, and Project Cleanup)

### Summary
Fixed server network binding to accept LAN connections, improved RSProx compatibility, and cleaned up project files:
1. Updated server to bind to 0.0.0.0 (all interfaces) instead of 127.0.0.1 for LAN access
2. Fixed RSProx compatibility by removing port from codebase URL (matching blurite.io format)
3. Fixed world_list.ws string encoding to use explicit UTF-8
4. Cleaned up project by removing documentation files, batch scripts, and temporary files
5. Pushed all changes to GitHub

---

## Network and Connectivity Fixes

### 1. Server LAN Access - Bind to All Interfaces
**Files**: 
- `game-plugins/src/main/kotlin/org/alter/plugins/service/restapi/RestApiService.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/service/worldlist/WorldListService.kt`

**Issue**: Server was only listening on 127.0.0.1 (localhost), preventing connections from other devices on the LAN.

**Fix Applied**:
- Updated RestApiService to explicitly bind Spark to 0.0.0.0 using `ipAddress("0.0.0.0")`
- Updated WorldListService to bind Netty bootstrap to 0.0.0.0 using `InetSocketAddress("0.0.0.0", port)`
- Server now accepts connections from any network interface, enabling LAN access

**Impact**: Server can now be accessed from other devices on the local network (e.g., 192.168.0.13:8080 and 192.168.0.13:43594).

---

### 2. RSProx Compatibility - Codebase URL Format
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/service/restapi/routes/RestApiRoutes.kt`

**Issue**: RSProx couldn't match codebase URL with world address, causing "Required value was null" errors.

**Root Cause**: Codebase URL included port (`http://192.168.0.13:8080/`), but RSProx expects codebase without port (like blurite.io uses `http://127.0.0.1/`).

**Fix Applied**:
- Changed codebase URL from `http://$serverIp:$serverPort/` to `http://$serverIp/` (removed port)
- World list URL still includes port: `http://$serverIp:$serverPort/world_list.ws`
- Port is handled separately by RSProx via `game_server_port` config

**Impact**: RSProx can now successfully match codebase host/IP with world address and load the server configuration.

---

### 3. World List Encoding Fix
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/service/restapi/routes/RestApiRoutes.kt`

**Issue**: String encoding in world_list.ws used platform default instead of explicit UTF-8.

**Fix Applied**:
- Updated `writeString()` method to use `StandardCharsets.UTF_8` explicitly
- Added proper import for `java.nio.charset.StandardCharsets`

**Impact**: Ensures consistent string encoding in world_list.ws binary format, preventing parsing issues.

---

## Project Cleanup

### 4. Removed Documentation Files
**Files Removed**: 48+ .md files (kept only `docs/CHANGELOG.md`)

**Details**:
- Removed all troubleshooting guides, setup instructions, and temporary documentation
- Kept only the main changelog for project history

**Impact**: Cleaner project structure, reduced clutter.

---

### 5. Removed Batch and PowerShell Scripts
**Files Removed**: 
- 18 .bat files (all batch scripts)
- 5 .ps1 files (PowerShell scripts)

**Details**:
- Removed all temporary setup, build, and troubleshooting scripts
- Cleaned up client launch scripts and configuration helpers

**Impact**: Reduced project size, removed temporary/obsolete automation scripts.

---

### 6. Removed Temporary and Duplicate Files
**Files Removed**:
- 4 duplicate proxy-targets.yaml files (kept main one)
- 1 duplicate modulus file (kept root one)
- jav_local_228.ws (test file)
- custom-runelite-client.jar and runelite.jar (build artifacts)

**Impact**: Cleaner repository, no duplicate configurations.

---

## Date: 2025-11-11 (Wilderness Content and Player Save Fixes)

### Summary
Added wilderness monster spawns and made them aggressive, plus fixed critical player position saving bug:
1. Added comprehensive wilderness monster spawns (63 spawns across 8 monster types)
2. Made all wilderness monsters aggressive with proper combat configurations
3. Fixed player position not being saved/loaded correctly on server restart

---

## Features

### 1. Wilderness Monster Spawns
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/spawns/SpawnPlugin.kt`

**Feature**: Added comprehensive monster spawns throughout the wilderness area.

**Details**:
- Created new wilderness spawn plugin with 63 monster spawns
- Added spawns for: Dark Wizards (9), Skeletons (12), Bandits (8), Chaos Druids (5), Wolves (9), Dark Warriors (6), Green Dragons (8), Hellhounds (6)
- All spawns are within wilderness boundaries (x: 2941..3392, z: 3524..3968)
- Monsters distributed across different wilderness levels (1-20+)

**Impact**: Wilderness is now populated with monsters, making it a dangerous and engaging area for players.

---

### 2. Wilderness Monster Aggression
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/CombatConfigPlugin.kt`

**Feature**: Made all wilderness monsters aggressive towards players.

**Details**:
- Created combat configuration plugin for wilderness monsters
- Configured aggressive radius (7-10 tiles depending on monster type)
- Set search delay for aggro checks (2-3 cycles)
- Enabled `alwaysAggro()` for all wilderness monsters (aggressiveTimer = Int.MAX_VALUE)
- Added proper combat stats, animations, and respawn delays for each monster type

**Impact**: All wilderness monsters now actively attack players within range, creating a dangerous PvE experience in the wilderness.

---

## Bug Fixes

### 3. Player Position Not Saved on Server Restart
**File**: `game-server/src/main/kotlin/org/alter/game/saving/impl/DetailSerialisation.kt`

**Issue**: Player positions were not being saved correctly, causing players to respawn in Lumbridge (home location) after server restart instead of their last location.

**Root Cause**: The `fromDocument()` method used unsafe type casting (`doc["tile"] as List<Int>`) which would throw exceptions if:
- The "tile" field was missing from saved data
- The field existed but wasn't a List
- The field was in an unexpected format

**Fix Applied**:
- Replaced unsafe casting with safe `getList()` method call wrapped in try-catch
- Added validation to ensure tile data has at least 3 elements (x, z, height)
- Added fallback to home location if tile data is missing or invalid
- Improved error handling to prevent crashes during player data loading

**Impact**: Player positions are now correctly saved when logging out and loaded when logging back in. Players will spawn at their last location instead of always returning to Lumbridge.

---

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

