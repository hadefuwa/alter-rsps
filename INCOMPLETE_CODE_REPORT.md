# Incomplete Code Report

This document provides a comprehensive overview of all incomplete implementations found in the Alter RSPS codebase.

## Summary

**Total Incomplete Implementations Found: 11**

- **Critical (Game-breaking):** 2
- **Important (Feature incomplete):** 6
- **Optional (Enhancement):** 3

---

## Critical Issues

### 1. WaterPlugin.kt - Completely Empty Plugin
**File:** `game-plugins/src/main/kotlin/org/alter/plugins/content/mechanics/water/WaterPlugin.kt`

**Status:** ❌ **CRITICAL** - Core game mechanic missing

**Issue:** The entire water container mechanics plugin is empty. All functionality has been commented out.

**Impact:** 
- Players cannot fill water containers (bowls, buckets, jugs, cups, vials, waterskins, watering cans)
- Players cannot empty water containers
- Water-related quests and activities are broken

**Required Functionality:**
1. Fill water containers from water sources
2. Empty water containers (except watering can and waterskin)
3. Prevent transferring water between containers
4. Handle toy sink item special drop behavior
5. Handle hot water transfer from bowls to cups

**Migration Required:** Previous implementation needs to be migrated from hardcoded IDs to RSCM (RuneScape Configuration Manager) identifiers.

**See:** Already documented in the file with detailed TODO comments.

---

### 2. Social.kt - Incomplete Friend/Ignore/Private Messaging
**File:** `game-server/src/main/kotlin/org/alter/game/model/social/Social.kt`

**Status:** ❌ **CRITICAL** - Social features broken

**Issues:**
- `pushFriends()` - Friend list updates are commented out (missing protocol messages)
- `pushIgnores()` - Ignore list updates are commented out (missing protocol messages)
- `sendPrivateMessage()` - Private messaging implementation is commented out (missing protocol messages)

**Impact:**
- Friend list doesn't update properly
- Ignore list doesn't work
- Private messaging is broken

**Required:** Protocol messages need to be implemented/imported:
- `UpdateFriendListMessage` - For friend list updates
- `UpdateIgnoreListMessage` - For ignore list updates
- `MessagePrivate` protocol message - Already exists but needs proper integration

**See:** Already documented in the file with detailed TODO comments.

---

## Important Issues

### 3. BankTabsPlugin.kt - Incomplete Bank Tab Features
**File:** `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/bank/BankTabsPlugin.kt`

**Status:** ⚠️ **IMPORTANT** - Bank functionality incomplete

**Issues:**

#### Issue 3a: Empty Button Handler (Component 113)
- **Line:** 74-78
- **Problem:** Button handler is completely empty with commented code
- **Impact:** Unknown bank feature is not working
- **Needs:** Investigation to determine what component 113 does and implement functionality

#### Issue 3b: Bank Tab Option 5
- **Line:** 57-59
- **Problem:** Shows "Not implemented [Bank1]" message
- **Impact:** Bank tab feature option 5 is unavailable
- **Needs:** Determine what option 5 should do (possibly tab deletion or renaming)

#### Issue 3c: Bank Tab Option 6 - Placeholder Removal
- **Line:** 60-64
- **Problem:** Shows "Not implemented [Bank2]" message
- **Impact:** Cannot remove placeholders from bank tabs
- **Required Implementation:**
  - Check if tab has placeholders (items with amount -2)
  - If no placeholders: Display "You don't have any placeholders to release."
  - If placeholders exist: Remove all placeholders from that tab

**See:** Now documented in the file with detailed TODO comments.

---

### 4. Mongo.kt - Missing loadAll() Implementation
**File:** `game-server/src/main/kotlin/org/alter/game/saving/formats/impl/Mongo.kt`

**Status:** ⚠️ **IMPORTANT** - Database functionality incomplete

**Issue:** `loadAll()` method throws `TODO("Not yet implemented")`

**Impact:**
- Admin commands that need to list all players may fail
- Server statistics/analytics may be incomplete
- Bulk operations on player data are not possible

**Required Implementation:**
```kotlin
override fun loadAll(): Map<String, Document> {
    return DatabaseManager.getCollection(collectionName)
        .find()
        .associate { doc -> 
            doc.getString("loginUsername") to doc 
        }
}
```

**See:** Now documented in the file with detailed TODO comments.

---

### 5. GroundItem.kt - Missing Ownership Type Logic
**File:** `game-server/src/main/kotlin/org/alter/game/model/entity/GroundItem.kt`

**Status:** ⚠️ **IMPORTANT** - Item ownership system incomplete

**Issue:** `ownerShipType` always defaults to 0 (None) and is never set

**Impact:**
- All items are treated as public (no ownership)
- Ironman/Group Ironman item restrictions may not work correctly
- Item visibility rules may not be enforced

**Ownership Types:**
- 0: None (public item)
- 1: Self Player (only dropper can see/pick up initially)
- 2: Other Player (visible to others, not dropper)
- 3: Group Ironman (only visible to group members)

**Required Implementation:**
- Set `ownerShipType` when item is dropped based on:
  - Player's game mode (Ironman, Group Ironman, etc.)
  - Item value (high-value items may have different rules)
  - Server settings
- Use `ownerShipType` for visibility and pickup permission checks
- Handle transitions (e.g., when item becomes public after timer expires)

**See:** Now documented in the file with detailed TODO comments.

---

### 6. KeptOnDeath.kt - Incomplete Risk Value Calculation
**File:** `game-plugins/src/main/kotlin/org/alter/plugins/content/interfaces/gameframe/tabs/worn_equipment/kod/KeptOnDeath.kt`

**Status:** ⚠️ **IMPORTANT** - Death mechanics incomplete

**Issue:** Risk value is hardcoded to 0, and kept items use dummy data

**Impact:**
- Players cannot see what items they would keep on death
- Risk value (total value of items that would be lost) is not calculated
- Death mechanics are not properly displayed

**Required Implementation:**
1. Calculate which items would be kept on death (3-4 items based on protect item prayer)
2. Calculate total value of items that would be lost
3. Display actual kept items in containers instead of dummy data
4. Uncomment and fix client script once calculation is implemented

**See:** Already documented in the file with detailed TODO comments.

---

### 7. HansPlugin.kt - Playtime Calculation Not Implemented
**File:** `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/lumbridge/npcs/HansPlugin.kt`

**Status:** ⚠️ **IMPORTANT** - NPC interaction incomplete

**Issue:** Age/playtime calculation is commented out, shows "Not implemented."

**Impact:**
- Players cannot check their playtime by talking to Hans
- Quest/NPC interaction is incomplete

**Required Implementation:**
- Uncomment and fix playtime calculation code
- Ensure `player.playtime` exists and is accurate
- Ensure `player.registryDate` exists and is set correctly
- Format time string properly
- Add required imports: `java.time.LocalDate`, `java.time.temporal.ChronoUnit`

**See:** Now documented in the file with detailed TODO comments.

---

### 8. MovementQueue.kt - Missing Crawling Support
**File:** `game-server/src/main/kotlin/org/alter/game/model/move/MovementQueue.kt`

**Status:** ⚠️ **IMPORTANT** - Movement system incomplete

**Issue:** Crawling movement type and NPC travel rules are not implemented

**Impact:**
- Players cannot crawl in low-ceiling areas
- NPCs may not follow proper movement restrictions
- Some quests/areas may not work correctly

**Required Implementation:**
- Add crawling movement type to MovementType enum (if not exists)
- Handle crawling speed/animations
- Add NPC-specific movement rules
- Validate NPC movement against area restrictions

**See:** Now documented in the file with detailed TODO comments.

---

## Optional/Enhancement Issues

### 9. EventMouseClickHandler.kt - Empty Mouse Click Handler
**File:** `game-server/src/main/kotlin/org/alter/game/message/handler/EventMouseClickHandler.kt`

**Status:** ℹ️ **OPTIONAL** - May not be needed

**Issue:** Handler is empty with TODO comment

**Impact:** None if mouse click functionality is not needed

**Note:** This is documented as an intentional placeholder. Mouse clicks are typically used for:
- Custom interface interactions
- Mini-map interactions
- Special game mechanics requiring precise coordinates

**See:** Already documented in the file as an intentional placeholder.

---

### 10. MessagePrivateSenderHandler.kt - TODO Comment
**File:** `game-server/src/main/kotlin/org/alter/game/message/handler/MessagePrivateSenderHandler.kt`

**Status:** ℹ️ **OPTIONAL** - Appears functional

**Issue:** Has TODO comment but implementation seems complete

**Impact:** None - handler appears to work correctly

**Potential Enhancements:**
- Add message filtering/validation
- Add rate limiting to prevent spam
- Add logging for moderation
- Add support for message history
- Add support for offline messaging

**See:** Now documented in the file clarifying that it appears functional.

---

### 11. RSCM.kt - Test Method Not Implemented
**File:** `plugins/rscm/src/main/kotlin/org/alter/rscm/RSCM.kt`

**Status:** ℹ️ **OPTIONAL** - Testing/validation incomplete

**Issue:** `test()` method is empty with TODO comment

**Impact:** Cannot validate RSCM configuration values

**Required Implementation:**
- Compare loaded RSCM data against known good values
- Log mismatches or unexpected values
- Return validation results
- Potentially throw exceptions for critical mismatches

**See:** Now documented in the file with detailed TODO comments.

---

## Recommendations

### Priority 1 (Fix Immediately)
1. **WaterPlugin.kt** - Core game mechanic, affects many quests and activities
2. **Social.kt** - Social features are essential for multiplayer experience

### Priority 2 (Fix Soon)
3. **BankTabsPlugin.kt** - Bank is a core feature, incomplete functionality affects user experience
4. **Mongo.kt** - Database functionality needed for admin tools
5. **GroundItem.kt** - Item ownership affects game balance and Ironman modes
6. **KeptOnDeath.kt** - Death mechanics are important for PvP and PvM
7. **HansPlugin.kt** - NPC interactions should be complete
8. **MovementQueue.kt** - Movement system affects many areas of the game

### Priority 3 (Enhancements)
9. **EventMouseClickHandler.kt** - Only if mouse click functionality is needed
10. **MessagePrivateSenderHandler.kt** - Add enhancements if needed
11. **RSCM.kt** - Add validation for better error detection

---

## How to Use This Report

1. **Review each item** - Check if the incomplete code affects your server
2. **Prioritize fixes** - Start with Critical issues, then Important, then Optional
3. **Check file comments** - Each file now has detailed TODO comments explaining what needs to be done
4. **Test after fixes** - Ensure fixes don't break existing functionality
5. **Update this report** - Mark items as complete when fixed

---

## Notes

- All incomplete code has been documented with detailed TODO comments
- Some items may be intentionally incomplete (e.g., EventMouseClickHandler)
- Some items require protocol messages or external dependencies
- Some items require investigation to determine exact requirements

---

**Last Updated:** 2025-01-11
**Total Files Reviewed:** 11
**Files with Incomplete Code:** 11

