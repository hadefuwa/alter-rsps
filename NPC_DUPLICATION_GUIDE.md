# Guide: How to Duplicate an NPC and Create a New NPC

This guide will walk you through the process of duplicating an existing NPC and creating a new NPC in your RuneScape private server.

## Table of Contents
1. [Understanding NPC Structure](#understanding-npc-structure)
2. [Method 1: Using the Inherit System (Recommended)](#method-1-using-the-inherit-system-recommended)
3. [Method 2: Creating a Completely New NPC](#method-2-creating-a-completely-new-npc)
4. [Step-by-Step: Duplicating an NPC](#step-by-step-duplicating-an-npc)
5. [Configuring NPC Combat Stats](#configuring-npc-combat-stats)
6. [Adding NPC to RSCM Mapping](#adding-npc-to-rscm-mapping)
7. [Adding Examine Text](#adding-examine-text)
8. [Spawning Your New NPC](#spawning-your-new-npc)
9. [Complete Example](#complete-example)

---

## Understanding NPC Structure

NPCs in this server have two main components:

1. **NPC Definition (NpcType)**: Stored in the cache, contains visual properties like:
   - Models, animations, colors
   - Name, size, combat level
   - Actions (right-click options)
   - Inherit field (allows inheriting from another NPC)

2. **NPC Combat Definition (NpcCombatDef)**: Set via plugins, contains combat properties like:
   - Stats (attack, strength, defence, hitpoints, magic, ranged)
   - Attack speed, animations, sounds
   - Aggressiveness settings
   - Drops, immunities, species

---

## Method 1: Using the Inherit System (Recommended)

The easiest way to duplicate an NPC is to use the `inherit` field. This allows your new NPC to copy all properties from an existing NPC, and you only need to override what you want to change.

### Advantages:
- Minimal configuration needed
- Automatically inherits all visual properties
- Only specify what differs from the base NPC

### Steps:
1. Find the NPC ID you want to duplicate
2. Create a new NPC definition with a new ID
3. Set the `inherit` field to the original NPC's ID
4. Override only the properties you want to change (e.g., name, combat level)

---

## Method 2: Creating a Completely New NPC

If you want full control, you can create a new NPC from scratch. This requires defining all properties manually.

---

## Step-by-Step: Duplicating an NPC

### Step 1: Find the NPC You Want to Duplicate

First, you need to identify the NPC you want to duplicate. You can find NPCs in:

**File**: `Alter/data/cfg/rscm/npc.rscm`

This file maps NPC names to IDs. For example:
```
tool_leprechaun:0
zombie:26
skeleton:70
```

### Step 2: Choose a New NPC ID

Pick an unused NPC ID for your new NPC. Common practice:
- Use IDs above 10,000 for custom NPCs
- Or find the highest existing ID and increment it

### Step 3: Create NPC Definition

NPC definitions can be created in two ways:

#### Option A: Using JSON (Deprecated but still works)

Create a JSON file in your NPC definitions directory (if using the old system):

```json
{
  "id": 10000,
  "inherit": 26,
  "name": "Custom Zombie",
  "combatLevel": 50,
  "size": 1,
  "isMinimapVisible": true,
  "isInteractable": true,
  "isClickable": true,
  "actions": ["Attack", null, null, null, null]
}
```

**Key fields:**
- `id`: Your new NPC's unique ID
- `inherit`: The ID of the NPC to inherit from (optional but recommended)
- `name`: Display name of the NPC
- `combatLevel`: Combat level (set to -1 for non-combat NPCs)
- `actions`: Array of 5 right-click options (null = no option)

#### Option B: Using TOML (Newer method)

The system has moved to TOML configuration. Check for `PackConfig` with `ConfigType.NPCS` in the codebase.

### Step 4: Pack the NPC into Cache

After creating your NPC definition file, you need to pack it into the cache. This is typically done through build tools or cache packing utilities.

---

## Adding NPC to RSCM Mapping

To use your NPC by name in code, add it to the RSCM mapping file.

**File**: `Alter/data/cfg/rscm/npc.rscm`

Add a line in the format:
```
your_npc_name:10000
```

For example:
```
custom_zombie:10000
```

**Important**: Use lowercase with underscores for the name (e.g., `custom_zombie`, not `CustomZombie`).

---

## Adding Examine Text

Add examine text for your NPC so players can examine it.

**File**: `Alter/data/cfg/npcs.csv`

Add a line in the format:
```
10000,"A custom zombie variant."
```

Format: `NPC_ID,"Examine text in quotes"`

---

## Configuring NPC Combat Stats

NPC combat definitions are set via Kotlin plugins. Create or modify a plugin file in:
`Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/`

### Example Plugin:

```kotlin
package org.alter.plugins.content.npcs.custom

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class CustomZombiePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Spawn the NPC
        spawnNpc("npc.custom_zombie", x = 3222, z = 3222, walkRadius = 5)
        
        // Configure combat stats
        setCombatDef("npc.custom_zombie") {
            // Set species (affects weaknesses, etc.)
            species {
                +NpcSpecies.UNDEAD
            }
            
            // Basic configs
            configs {
                attackSpeed = 4  // Attack every 4 ticks
                respawnDelay = 25  // Respawn after 25 cycles
            }
            
            // Aggressiveness
            aggro {
                radius = 10  // Aggro radius in tiles
                searchDelay = 1  // Delay between aggro searches
                alwaysAggro()  // Or use neverAggro() or aggroTimer = X
            }
            
            // Combat stats
            stats {
                hitpoints = 100
                attack = 50
                strength = 50
                defence = 50
                magic = 1
                ranged = 1
            }
            
            // Defence bonuses
            bonuses {
                defenceStab = 20
                defenceSlash = 20
                defenceCrush = 20
                defenceMagic = 10
                defenceRanged = 20
            }
            
            // Attack bonuses
            bonuses {
                attackStab = 30
                strengthBonus = 20
            }
            
            // Animations
            anims {
                attack = 422  // Attack animation ID
                block = 424   // Block animation ID
                death = 836  // Death animation ID
            }
            
            // Optional: Sounds
            sound {
                attackSound = 1
                blockSound = 2
                deathSound = 3
            }
            
            // Optional: Immunities
            immunities {
                poison = false
                venom = false
                cannon = false
                thralls = false
            }
            
            // Optional: Slayer data
            slayerData {
                levelRequirement = 1
                xp = 10.0
            }
            
            // Optional: Drops
            drops {
                // Add drop tables here
            }
        }
    }
}
```

### Key Combat Configuration Options:

**Stats:**
- `hitpoints`: Maximum HP
- `attack`, `strength`, `defence`, `magic`, `ranged`: Combat levels

**Configs:**
- `attackSpeed`: Ticks between attacks (lower = faster)
- `respawnDelay`: Cycles before respawn (0 = no respawn)

**Aggro:**
- `radius`: How many tiles away NPC can detect players
- `searchDelay`: Ticks between aggro searches
- `alwaysAggro()`: Always aggressive
- `neverAggro()`: Never aggressive
- `aggroTimer`: Specific timer value

**Bonuses:**
- `attackStab/Slash/Crush/Magic/Ranged`: Attack bonuses
- `defenceStab/Slash/Crush/Magic/Ranged`: Defence bonuses
- `strengthBonus`: Strength bonus
- `rangedStrengthBonus`: Ranged strength bonus
- `magicDamageBonus`: Magic damage bonus

**Species:**
- `NpcSpecies.UNDEAD`, `NpcSpecies.DRACONIC`, `NpcSpecies.DEMON`, etc.
- Affects weaknesses and special interactions

---

## Spawning Your New NPC

### In Plugin Code:

```kotlin
spawnNpc("npc.custom_zombie", x = 3222, z = 3222, height = 0, walkRadius = 5)
```

### Using Admin Command:

In-game, use the admin command:
```
::npc 10000
```

This spawns the NPC at your current location.

---

## Complete Example

Let's create a complete example: duplicating a zombie and making it a "Super Zombie".

### Step 1: Check Original NPC

Look in `Alter/data/cfg/rscm/npc.rscm`:
```
zombie:26
```

So zombie has ID 26.

### Step 2: Create New NPC Definition

Create a JSON file (if using JSON method) or TOML file:

**super_zombie.json:**
```json
{
  "id": 10000,
  "inherit": 26,
  "name": "Super Zombie",
  "combatLevel": 100,
  "size": 1
}
```

### Step 3: Add to RSCM Mapping

Add to `Alter/data/cfg/rscm/npc.rscm`:
```
super_zombie:10000
```

### Step 4: Add Examine Text

Add to `Alter/data/cfg/npcs.csv`:
```
10000,"A much stronger zombie variant."
```

### Step 5: Create Plugin

Create `Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/superzombie/SuperZombiePlugin.kt`:

```kotlin
package org.alter.plugins.content.npcs.superzombie

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class SuperZombiePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Spawn at Lumbridge
        spawnNpc("npc.super_zombie", x = 3222, z = 3222, walkRadius = 5)
        
        setCombatDef("npc.super_zombie") {
            species {
                +NpcSpecies.UNDEAD
            }
            
            configs {
                attackSpeed = 3
                respawnDelay = 50
            }
            
            aggro {
                radius = 15
                searchDelay = 1
                alwaysAggro()
            }
            
            stats {
                hitpoints = 200
                attack = 100
                strength = 100
                defence = 100
                magic = 1
                ranged = 1
            }
            
            bonuses {
                attackStab = 50
                attackSlash = 50
                attackCrush = 50
                strengthBonus = 40
                defenceStab = 40
                defenceSlash = 40
                defenceCrush = 40
                defenceMagic = 30
                defenceRanged = 40
            }
            
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            immunities {
                poison = false
                venom = false
            }
        }
    }
}
```

### Step 6: Register Plugin

Make sure your plugin is registered in the plugin system (usually auto-discovered if in the correct package).

### Step 7: Rebuild and Test

1. Rebuild the project
2. Pack NPC definitions into cache (if needed)
3. Start the server
4. Test by spawning: `::npc 10000`

---

## Tips and Best Practices

1. **Use Inherit**: Always use `inherit` when duplicating - it saves time and ensures consistency.

2. **ID Management**: Keep track of your custom NPC IDs in a document to avoid conflicts.

3. **Naming Convention**: Use lowercase with underscores for RSCM names (e.g., `super_zombie`).

4. **Testing**: Test NPCs in a safe location first before adding to important areas.

5. **Backup**: Always backup your cache and configuration files before making changes.

6. **Combat Balance**: When creating combat NPCs, test thoroughly to ensure balanced stats.

7. **Documentation**: Document your custom NPCs with their IDs, locations, and purposes.

---

## Troubleshooting

### NPC doesn't appear:
- Check that the NPC ID is correct
- Verify the RSCM mapping is correct
- Ensure the NPC was packed into the cache
- Check server logs for errors

### NPC has wrong appearance:
- Verify the `inherit` field points to the correct NPC
- Check that models/animations are correct in the definition

### NPC has no combat stats:
- Ensure you've created a plugin with `setCombatDef`
- Check that the plugin is registered and loaded
- Verify the NPC name in `setCombatDef` matches the RSCM name

### NPC doesn't spawn:
- Check coordinates are valid
- Verify the spawn command/plugin is correct
- Check server logs for spawn errors

---

## Additional Resources

- **NPC Definition Structure**: `Alter/plugins/filestore/src/main/kotlin/dev/openrune/cache/filestore/definition/data/NpcType.kt`
- **Combat Definition DSL**: `Alter/game-api/src/main/kotlin/org/alter/api/dsl/NpcCombatDsl.kt`
- **Example NPC Plugin**: `Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/kbd/KbdConfigsPlugin.kt`

---

## Summary

To duplicate an NPC:
1. ✅ Find the original NPC ID
2. ✅ Create new NPC definition with `inherit` field
3. ✅ Choose a new unique ID
4. ✅ Add to RSCM mapping file
5. ✅ Add examine text to CSV
6. ✅ Create plugin with combat stats
7. ✅ Spawn NPC in game
8. ✅ Test and adjust as needed

Good luck creating your custom NPCs!


