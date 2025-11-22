# How to Extract NPC Spawns from JSON File

This guide explains how to extract NPC spawn data from `NPCList_OSRS.json` and implement it in the game's spawn plugins.

## Overview

The process involves:
1. Identifying the target area coordinates
2. Using a Python script to filter NPCs from the JSON file
3. Mapping NPC IDs to RSCM names
4. Creating or updating the spawn plugin with the extracted data

## Prerequisites

- Python 3.x installed
- Access to `docs/NPCList_OSRS.json`
- Knowledge of the target area coordinates

## Step-by-Step Guide

### Step 1: Identify Target Area Coordinates

First, determine the coordinate range for your target area. You can find location coordinates in:
- `guides/TELEPORT_LOCATIONS.md` - Contains teleport coordinates for various locations
- Existing spawn plugins in `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/`
- Teleport command plugins that define area coordinates

**Detailed Process for Finding Coordinates:**

1. **Check TELEPORT_LOCATIONS.md:**
   ```bash
   # Search for your area name
   grep -i "slayer" guides/TELEPORT_LOCATIONS.md
   ```
   This will show you the exact coordinates. For Slayer Tower, it shows: `3428, 3537, height 0`

2. **Determine Coordinate Range:**
   - Start with the exact coordinates from the teleport location
   - Add a buffer around the coordinates to capture all NPCs in the area
   - For buildings, consider multiple floors (heights 0, 1, 2, etc.)
   - For Slayer Tower: Starting point (3428, 3537), expanded to x: 3420-3450, y: 3530-3560

3. **Verify with Existing Spawns (if any):**
   - Check if there's already a spawn plugin for the area
   - Look at existing spawn coordinates to understand the area boundaries

**Example for Slayer Tower:**
- Location: Slayer Tower in Morytania
- Coordinates from `TELEPORT_LOCATIONS.md`: (3428, 3537, height 0)
- Coordinate range: x: 3420-3450, y: 3530-3560
- Heights to check: 0 (ground floor), 1 (first floor), 2 (top floor)

### Step 2: Configure the Extraction Script

The extraction script is already created at `scripts/extract_npc_spawns.py`. You just need to configure it for your target area.

**Script Configuration:**

1. **Open the script:**
   ```bash
   # Edit the script file
   # On Windows: notepad scripts/extract_npc_spawns.py
   # On Linux/Mac: nano scripts/extract_npc_spawns.py
   ```

2. **Modify the configuration section (lines 24-39):**
   ```python
   # Target area name (for output file naming)
   AREA_NAME = "slayer_tower"  # Change to your area name

   # Coordinate range for filtering NPCs
   MIN_X = 3420  # Minimum X coordinate
   MAX_X = 3450  # Maximum X coordinate
   MIN_Y = 3530  # Minimum Y coordinate
   MAX_Y = 3560  # Maximum Y coordinate

   # Input and output file paths
   INPUT_JSON = "docs/NPCList_OSRS.json"  # Usually stays the same
   OUTPUT_JSON = f"{AREA_NAME}_npcs.json"  # Auto-generated from AREA_NAME
   ```

**Key features of the script:**
- Reads `NPCList_OSRS.json` (handles large files efficiently)
- Filters NPCs by coordinate range (x, y)
- Groups NPCs by height/plane (p parameter)
- Outputs a detailed summary grouped by height and NPC name
- Saves filtered data to a JSON file for easy review
- Handles Windows console encoding issues automatically

### Step 3: Run the Extraction Script

```bash
# Navigate to project root
cd "C:\Users\Hamed\Documents\alter-pnda-rsps"

# Run the extraction script
python scripts/extract_npc_spawns.py
```

**Script parameters:**
You'll need to modify the script to set:
- `min_x`, `max_x`: X coordinate range
- `min_y`, `max_y`: Y coordinate range
- `output_file`: Name for the filtered JSON output

**Example output:**
```
NPC Spawn Extraction Script
============================================================
[OK] Loaded 24110 NPCs from docs/NPCList_OSRS.json

Filtering NPCs in range: X: 3420-3450, Y: 3530-3560...

============================================================
NPC Extraction Summary for SLAYER TOWER
============================================================
Coordinate Range: X: 3420-3450, Y: 3530-3560
Total NPCs found: 42

NPCs by Height:
  Height 0: 14 NPCs
  Height 1: 19 NPCs
  Height 2: 9 NPCs

=== Height 0 ===
  Banshee (ID: 414) - 7 spawns:
    - (3433, 3552)
    - (3436, 3559)
    ...
  Crawling Hand (ID: 448) - 7 spawns:
    - (3420, 3551)
    ...
[OK] Saved 42 NPCs to slayer_tower_npcs.json
```

**Note:** The script groups NPCs by name for readability, but each spawn may have a different ID. Check the output JSON file for individual NPC details including specific IDs.

**Understanding the Output:**
- The summary shows total NPCs found and breakdown by height
- The detailed list groups NPCs by name and shows all spawn locations
- The output JSON file contains the raw filtered data with all fields (id, name, x, y, p)
- Review the output JSON file to see exact IDs for each spawn location

### Step 4: Map NPC IDs to RSCM Names

After extracting NPCs, you need to map their IDs to RSCM (RuneScape Config Manager) names used in the codebase. This is a critical step because the `spawnNpc` function requires RSCM names, not raw IDs.

**RSCM file location:**
- `data/cfg/rscm/npc.rscm`

**Detailed Mapping Process:**

1. **Extract unique NPC IDs from your filtered data:**
   - Review the output JSON file or script output
   - Note all unique NPC IDs (e.g., 448, 414, 412, 484, etc.)

2. **Search the RSCM file for each ID:**
   ```bash
   # Search for specific NPC IDs in RSCM file
   # Format in RSCM: name:id
   grep ":448" data/cfg/rscm/npc.rscm
   grep ":414" data/cfg/rscm/npc.rscm
   grep ":412" data/cfg/rscm/npc.rscm
   ```

3. **Or search by NPC type name:**
   ```bash
   # Search for NPC types (useful when you know the name)
   grep -i "banshee\|gargoyle\|bloodveld\|infernal_mage\|crawling_hand\|aberrant_spectre" data/cfg/rscm/npc.rscm
   ```

4. **Understand the RSCM format:**
   - Format: `rscm_name:id`
   - Example: `crawling_hand_448:448`
   - In spawn calls, use: `npc.crawling_hand_448`

5. **Handle special cases:**
   - Some NPCs may have `null_<id>` as their RSCM name (e.g., `null_6122:6122`)
   - These still work: use `npc.null_6122` in spawn calls
   - If an NPC ID is not found in RSCM, it may not be implemented in the game

**Example mappings from Slayer Tower:**
- ID 448 → `crawling_hand_448:448` → `npc.crawling_hand_448`
- ID 414 → `banshee_414:414` → `npc.banshee_414`
- ID 412 → `gargoyle_412:412` → `npc.gargoyle_412`
- ID 484 → `bloodveld_484:484` → `npc.bloodveld_484`
- ID 443 → `infernal_mage_443:443` → `npc.infernal_mage_443`
- ID 6122 → `null_6122:6122` → `npc.null_6122` (special case)

**Create a mapping table:**
It's helpful to create a quick reference table:
```
NPC Name          | ID  | RSCM Name              | Spawn Format
------------------|-----|------------------------|------------------
Crawling Hand     | 448 | crawling_hand_448      | npc.crawling_hand_448
Banshee           | 414 | banshee_414            | npc.banshee_414
Gargoyle          | 412 | gargoyle_412           | npc.gargoyle_412
Aberrant Spectre  | 2   | aberrant_spectre_2     | npc.aberrant_spectre_2
Bloodveld         | 484 | bloodveld_484          | npc.bloodveld_484
Infernal Mage     | 443 | infernal_mage_443      | npc.infernal_mage_443
Mysterious Ghost  | 6122| null_6122              | npc.null_6122
```

### Step 5: Check Existing Spawn Plugin Structure

Before creating a new plugin, check if one already exists and understand the structure:

**Plugin location pattern:**
```
game-plugins/src/main/kotlin/org/alter/plugins/content/areas/<areaname>/spawns/SpawnPlugin.kt
```

**Check for existing plugin:**
```bash
# Search for existing spawn plugins
find game-plugins/src/main/kotlin/org/alter/plugins/content/areas -name "SpawnPlugin.kt"
```

**Example existing plugins to reference:**
- `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/edgeville/spawns/SpawnPlugin.kt`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/godwars/spawns/SpawnPlugin.kt`

**Key things to note from existing plugins:**
1. Package structure matches directory path
2. Class extends `KotlinPlugin` with three parameters: `PluginRepository`, `World`, `Server`
3. All spawn calls are in the `init` block
4. NPCs are typically grouped by height/floor with comments
5. Standard parameters: `walkRadius = 5`, `direction = Direction.SOUTH`

### Step 6: Create or Update the Spawn Plugin

**Detailed Plugin Creation Process:**

1. **Create the directory structure:**
   ```bash
   # Create directory for your area (if it doesn't exist)
   mkdir -p game-plugins/src/main/kotlin/org/alter/plugins/content/areas/<areaname>/spawns
   ```
   For Slayer Tower: `slayertower/spawns/`

2. **Create the SpawnPlugin.kt file:**
   - Use the exact filename: `SpawnPlugin.kt`
   - Package name must match directory structure

3. **Plugin structure template:**
```kotlin
package org.alter.plugins.content.areas.<areaname>.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * <Area Name> Spawn Plugin
 * 
 * This plugin spawns NPCs in <area description>.
 * Coordinates are based on NPCList_OSRS.json
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        // Group NPCs by height/floor
        // Height 0 (Ground Floor)
        spawnNpc(npc = "npc.<name>", x = <x>, z = <y>, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        // Height 1 (First Floor)
        spawnNpc(npc = "npc.<name>", x = <x>, z = <y>, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        
        // Height 2 (Second Floor)
        spawnNpc(npc = "npc.<name>", x = <x>, z = <y>, height = 2, walkRadius = 5, direction = Direction.SOUTH)
    }
}
```

4. **Convert JSON data to spawn calls:**
   - For each NPC in your filtered JSON file:
     - Extract: `id`, `name`, `x`, `y`, `p`
     - Map `id` to RSCM name (from Step 4)
     - Convert `y` to `z` parameter
     - Convert `p` to `height` parameter
     - Create spawn call: `spawnNpc(npc = "npc.<rscm_name>", x = <x>, z = <y>, height = <p>, ...)`

5. **Important notes:**
   - **Coordinate conversion:** JSON uses `x`, `y`, `p` → Kotlin uses `x`, `z`, `height`
     - `x` stays the same
     - `y` from JSON becomes `z` in Kotlin
     - `p` from JSON becomes `height` in Kotlin
   - **RSCM format:** Always use `npc.<rscm_name>` format
   - **Standard parameters:**
     - `walkRadius = 5` (typical for most NPCs)
     - `direction = Direction.SOUTH` (standard default)
   - **Grouping:** Organize spawns by height/floor with descriptive comments

6. **Example conversion:**
   ```json
   // JSON entry:
   {
     "id": 448,
     "name": "Crawling Hand",
     "x": 3420,
     "y": 3551,
     "p": 0
   }
   ```
   ```kotlin
   // Kotlin spawn call:
   spawnNpc(npc = "npc.crawling_hand_448", x = 3420, z = 3551, height = 0, walkRadius = 5, direction = Direction.SOUTH)
   ```

### Step 7: Organize NPCs by Height

Group your spawn calls by height/floor for better organization:

```kotlin
/**
 * Ground Floor (Height 0) - <NPC Type>
 * 
 * Description of NPCs on this floor
 */
spawnNpc(...)
spawnNpc(...)

/**
 * First Floor (Height 1) - <NPC Type>
 * 
 * Description of NPCs on this floor
 */
spawnNpc(...)
```

### Step 8: Verify the Plugin

**Verification Checklist:**

1. **Check for linter errors:**
   ```bash
   # The IDE should show any syntax errors automatically
   # Or manually check with:
   # - Read lints tool in your IDE
   # - Build the project to see compilation errors
   ```

2. **Verify NPC counts:**
   - Count the number of `spawnNpc` calls in your plugin
   - Compare with the total NPC count from the extraction script
   - Ensure all NPCs from the filtered JSON are included
   - Check that you haven't missed any heights/floors

3. **Verify coordinate mapping:**
   - Double-check that JSON `y` values are correctly mapped to Kotlin `z` parameters
   - Verify that JSON `p` values are correctly mapped to Kotlin `height` parameters
   - Ensure all coordinates are within the expected range

4. **Verify RSCM names:**
   - Check that all RSCM names exist in `data/cfg/rscm/npc.rscm`
   - Verify the format is correct: `npc.<rscm_name>`
   - Test that special cases (like `null_6122`) are handled correctly

5. **Test compilation:**
   - Build the project: The plugin should compile without errors
   - All imports should be valid
   - All RSCM names should be recognized

6. **Code organization:**
   - NPCs are grouped by height/floor
   - Each group has descriptive comments
   - Code is readable and maintainable

**Common Issues to Check:**
- Missing imports (Direction, etc.)
- Typos in RSCM names
- Coordinate system confusion (y vs z)
- Missing NPCs (check the count matches)
- Wrong height values

## Example: Slayer Tower Implementation

### Coordinates
- X range: 3420-3450
- Y range: 3530-3560
- Heights: 0, 1, 2

### Extracted NPCs
- **Height 0:** 14 NPCs (7 Crawling Hands, 7 Banshees)
- **Height 1:** 19 NPCs (11 Aberrant Spectres, 2 Bloodvelds, 5 Infernal Mages, 1 Mysterious Ghost)
- **Height 2:** 9 NPCs (8 Gargoyles, 1 Nechryael)

### RSCM Mappings Used
- `crawling_hand_448`, `crawling_hand_453`, `crawling_hand_454`
- `banshee_414`
- `aberrant_spectre_2`, `aberrant_spectre_3`, `aberrant_spectre_4`, `aberrant_spectre_5`
- `bloodveld_484`
- `infernal_mage_443`, `infernal_mage_445`, `infernal_mage_446`, `infernal_mage_447`
- `gargoyle_412`
- `nechryael_8`
- `null_6122` (Mysterious Ghost - may require special handling)

### Final Plugin Location
`game-plugins/src/main/kotlin/org/alter/plugins/content/areas/slayertower/spawns/SpawnPlugin.kt`

## Troubleshooting

### Issue: NPC ID not found in RSCM file
**Symptoms:** You can't find the NPC ID in `npc.rscm` when searching
**Solutions:** 
- Check if the NPC exists in the game cache - some NPCs may not be implemented
- Some NPCs may have different IDs - try searching by name instead of ID
- Search for similar NPC names or check `docs/npc-ids.txt` for alternative IDs
- If the NPC truly doesn't exist, you may need to skip it or add it to the RSCM file

### Issue: Too many NPCs in coordinate range
**Symptoms:** The extraction script returns hundreds of NPCs, including ones outside your target area
**Solutions:**
- Narrow the coordinate range (reduce MIN_X, MAX_X, MIN_Y, MAX_Y)
- Filter by additional criteria (NPC name, category, etc.) - modify the script
- Manually review the filtered list and remove unwanted NPCs
- Check if your coordinate range includes adjacent areas

### Issue: No NPCs found in coordinate range
**Symptoms:** The extraction script returns 0 NPCs
**Solutions:**
- Verify the coordinates are correct - check `TELEPORT_LOCATIONS.md` again
- Expand the coordinate range (increase the buffer around the center point)
- Check if the area uses a different coordinate system (underground areas may have different coordinates)
- Verify the JSON file contains data for that area

### Issue: NPC spawns in wrong location
**Symptoms:** NPCs appear in incorrect positions when testing in-game
**Solutions:**
- Verify coordinate system conversion: JSON `y` → Kotlin `z`, JSON `p` → Kotlin `height`
- Check if coordinates are in the correct coordinate system (surface vs. underground)
- Compare with existing spawn plugins for similar areas
- Verify you didn't mix up x and y/z coordinates
- Test with a single NPC first to verify the coordinate system

### Issue: Plugin doesn't load
**Symptoms:** Plugin doesn't appear in-game or server doesn't recognize it
**Solutions:**
- Check package name matches directory structure exactly
- Verify class name matches filename (must be `SpawnPlugin.kt` with class `SpawnPlugin`)
- Ensure plugin extends `KotlinPlugin` correctly with all three parameters
- Check for compilation errors - the plugin must compile successfully
- Verify the directory structure matches the package declaration

### Issue: Windows console encoding errors
**Symptoms:** Python script shows encoding errors with special characters
**Solutions:**
- The script already includes Windows encoding fixes
- If issues persist, ensure you're using Python 3.x
- Run the script from PowerShell or Command Prompt (not Git Bash if on Windows)
- The script uses `[OK]` and `[ERROR]` instead of Unicode symbols for compatibility

### Issue: JSON file too large to read
**Symptoms:** Trying to read the entire JSON file causes memory issues
**Solutions:**
- The extraction script handles large files efficiently using Python's JSON parser
- Don't try to read the entire file manually - use the script
- If the script still has issues, check available system memory

## Tips and Best Practices

1. **Always verify coordinates** from multiple sources before filtering
2. **Group NPCs by height** for better code organization
3. **Add descriptive comments** explaining what each NPC type is
4. **Use consistent walkRadius** (typically 5 for most NPCs)
5. **Test in-game** after implementing to verify spawns are correct
6. **Keep the extraction script** for future use with different areas
7. **Document any special NPCs** that may require additional configuration

## Quick Reference

### Common Coordinate Ranges

| Area | X Range | Y Range | Heights |
|------|---------|---------|---------|
| Slayer Tower | 3420-3450 | 3530-3560 | 0, 1, 2 |
| Edgeville | 3080-3120 | 3480-3520 | 0 |
| Varrock | 3200-3250 | 3400-3450 | 0 |

### Common RSCM Name Patterns

| NPC Type | Pattern | Example |
|----------|---------|---------|
| Crawling Hand | `crawling_hand_<id>` | `crawling_hand_448` |
| Banshee | `banshee_<id>` | `banshee_414` |
| Gargoyle | `gargoyle_<id>` | `gargoyle_412` |
| Aberrant Spectre | `aberrant_spectre_<id>` | `aberrant_spectre_5` |
| Bloodveld | `bloodveld_<id>` | `bloodveld_484` |
| Infernal Mage | `infernal_mage_<id>` | `infernal_mage_443` |

### Script Configuration Template

```python
# Target area name (for output file naming)
AREA_NAME = "your_area_name"

# Coordinate range for filtering NPCs
MIN_X = 3000
MAX_X = 3100
MIN_Y = 3200
MAX_Y = 3300
```

## Related Files

- `docs/NPCList_OSRS.json` - Source NPC data
- `data/cfg/rscm/npc.rscm` - NPC ID to RSCM name mappings
- `docs/npc-ids.txt` - NPC ID reference
- `guides/TELEPORT_LOCATIONS.md` - Location coordinates
- `scripts/extract_npc_spawns.py` - Extraction script

## Next Steps

After implementing spawns:
1. Test NPCs spawn correctly in-game
2. Verify NPCs are at correct locations
3. Check NPC behavior (aggression, combat, etc.)
4. Add combat configurations if needed (see `GenericNpcCombatConfigPlugin.kt`)
5. Update documentation if area has special features

## Workflow Summary

```
1. Find coordinates → 2. Configure script → 3. Run script → 4. Map IDs → 5. Create plugin → 6. Verify → 7. Test
   ↓                      ↓                    ↓              ↓              ↓            ↓          ↓
TELEPORT_            Modify script        Extract NPCs    Check RSCM    Write Kotlin   Lint/      In-game
LOCATIONS.md         configuration        from JSON       mappings      spawn calls    Compile    testing
   or
Existing spawns     MIN_X, MAX_X,        Review output   Create table  Group by       Check      Verify
                     MIN_Y, MAX_Y        JSON file       of mappings   height         counts     locations
```

## Detailed Workflow Example: Slayer Tower

Here's the exact process used for implementing Slayer Tower NPCs:

1. **Found coordinates:**
   - Checked `guides/TELEPORT_LOCATIONS.md`
   - Found: Slayer Tower at (3428, 3537, height 0)
   - Expanded range: x: 3420-3450, y: 3530-3560

2. **Configured script:**
   - Set `AREA_NAME = "slayer_tower"`
   - Set coordinate ranges: `MIN_X = 3420`, `MAX_X = 3450`, `MIN_Y = 3530`, `MAX_Y = 3560`

3. **Ran extraction:**
   - Executed: `python scripts/extract_npc_spawns.py`
   - Result: 42 NPCs found (14 at height 0, 19 at height 1, 9 at height 2)
   - Saved to: `slayer_tower_npcs.json`

4. **Mapped RSCM names:**
   - Searched `data/cfg/rscm/npc.rscm` for each unique ID
   - Created mapping table with all NPC types
   - Found special case: `null_6122` for Mysterious Ghost

5. **Created plugin:**
   - Created directory: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/slayertower/spawns/`
   - Created file: `SpawnPlugin.kt`
   - Converted all 42 NPCs to spawn calls
   - Grouped by height with descriptive comments

6. **Verified:**
   - Checked linter: No errors
   - Verified count: 42 spawn calls = 42 NPCs
   - Verified RSCM names: All found in `npc.rscm`
   - Verified coordinates: All within expected range

7. **Ready for testing:**
   - Plugin compiles successfully
   - All NPCs should spawn at correct locations in-game

