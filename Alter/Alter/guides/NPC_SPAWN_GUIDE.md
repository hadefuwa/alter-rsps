# NPC Spawn Guide for Alter RSPS

## Table of Contents
1. [Introduction](#introduction)
2. [Understanding NPC Spawns](#understanding-npc-spawns)
3. [Finding Available NPCs](#finding-available-npcs)
4. [Basic NPC Spawning](#basic-npc-spawning)
5. [Advanced NPC Configuration](#advanced-npc-configuration)
6. [Common Examples](#common-examples)
7. [Troubleshooting](#troubleshooting)

---

## Introduction

This guide will teach you how to spawn NPCs (Non-Player Characters) in your Alter RSPS server. NPCs include monsters, shopkeepers, bankers, quest NPCs, and more.

**Important:** NPCs must exist in the OSRS client cache. You cannot create NPCs with custom names unless you modify the client cache. See [NPC_MODEL_GUIDE.md](../../NPC_MODEL_GUIDE.md) for more information.

---

## Understanding NPC Spawns

### What is an NPC Spawn?

An NPC spawn is a location in the game world where an NPC appears. When you spawn an NPC, you're telling the server:
- **What** NPC to spawn (NPC name/ID)
- **Where** to spawn it (X, Z, Height coordinates)
- **How** it should behave (walk radius, direction, etc.)

### Server vs Client

**Server-side (what you control):**
- Where NPCs spawn
- NPC stats (HP, attack, defence, etc.)
- NPC behaviors (aggression, combat, drops)
- NPC dialogue and interactions

**Client-side (cannot change without cache editing):**
- NPC appearance/model
- NPC name
- NPC animations
- NPC right-click options

---

## Finding Available NPCs

### Method 1: Using the RSCM File

The RSCM (RuneScape Cache Manager) file contains all available NPCs.

**Location:** `Alter/data/cfg/rscm/npc.rscm`

**Format:**
```
npc_name:npc_id
```

**Examples:**
```
unicorn:2678
unicorn_2679:2679
unicorn_2680:2680
cow:2790
cerberus:5862
king_black_dragon:239
```

### Method 2: Searching for Specific NPCs

**Using Command Line (Git Bash/WSL):**
```bash
# Search for all cows
grep -i "cow" Alter/data/cfg/rscm/npc.rscm

# Search for dragons
grep -i "dragon" Alter/data/cfg/rscm/npc.rscm

# Search for bankers
grep -i "banker" Alter/data/cfg/rscm/npc.rscm

# Search for shops
grep -i "shop" Alter/data/cfg/rscm/npc.rscm
```

**Using Windows PowerShell:**
```powershell
# Search for NPCs
Select-String -Path "Alter\data\cfg\rscm\npc.rscm" -Pattern "cow"
```

### Method 3: Using the NPC CSV

**Location:** `Alter/data/cfg/npcs.csv`

This file contains NPC descriptions.

**Format:**
```
npc_id,description
```

**Example:**
```
2678,A magical unicorn.
2679,A magical unicorn.
2680,A magical unicorn.
```

---

## Basic NPC Spawning

### Step 1: Create a Plugin File

Create a new Kotlin plugin file in the appropriate location.

**Example Location:**
```
Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/areas/[area_name]/spawns/[PluginName].kt
```

**For example:**
```
Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/areas/lumbridge/spawns/LumbridgeCowSpawns.kt
```

### Step 2: Basic Plugin Structure

```kotlin
package org.alter.plugins.content.areas.lumbridge.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class LumbridgeCowSpawns(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Your NPC spawns go here
    }
}
```

### Step 3: Spawn an NPC

**Basic Syntax:**
```kotlin
spawnNpc("npc.npc_name", x = X_COORDINATE, z = Z_COORDINATE, height = HEIGHT, walkRadius = WALK_RADIUS)
```

**Example - Spawn a Cow:**
```kotlin
init {
    // Spawn a cow at coordinates (3253, 3267, 0) with walk radius of 5
    spawnNpc("npc.cow", x = 3253, z = 3267, height = 0, walkRadius = 5)
}
```

### Understanding Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `"npc.npc_name"` | NPC identifier from npc.rscm (add "npc." prefix) | `"npc.cow"` |
| `x` | X coordinate in the game world | `3253` |
| `z` | Z coordinate (often called Y in other games) | `3267` |
| `height` | Height/floor level (0 = ground, 1 = first floor, etc.) | `0` |
| `walkRadius` | How far NPC can wander from spawn point | `5` |

---

## Advanced NPC Configuration

### Adding Direction

NPCs can face a specific direction when spawned.

```kotlin
spawnNpc(
    npc = "npc.shop_keeper",
    x = 3211,
    z = 3246,
    height = 0,
    walkRadius = 3,
    direction = Direction.EAST  // NPC faces East
)
```

**Available Directions:**
- `Direction.NORTH`
- `Direction.SOUTH`
- `Direction.EAST`
- `Direction.WEST`

### Multiple Spawns

Spawn multiple NPCs efficiently:

```kotlin
init {
    // Spawn 5 cows
    spawnNpc("npc.cow", x = 3253, z = 3267, height = 0, walkRadius = 5)
    spawnNpc("npc.cow", x = 3256, z = 3269, height = 0, walkRadius = 5)
    spawnNpc("npc.cow", x = 3250, z = 3265, height = 0, walkRadius = 5)
    spawnNpc("npc.cow", x = 3248, z = 3270, height = 0, walkRadius = 5)
    spawnNpc("npc.cow", x = 3255, z = 3272, height = 0, walkRadius = 5)
}
```

### Using Lists for Mass Spawning

```kotlin
init {
    // List of cow spawn locations
    val cowSpawns = listOf(
        Triple(3253, 3267, 5),  // x, z, walkRadius
        Triple(3256, 3269, 5),
        Triple(3250, 3265, 5),
        Triple(3248, 3270, 5),
        Triple(3255, 3272, 5)
    )

    // Spawn all cows
    cowSpawns.forEach { (x, z, radius) ->
        spawnNpc("npc.cow", x = x, z = z, height = 0, walkRadius = radius)
    }
}
```

---

## Common Examples

### Example 1: Spawn Shop Keeper

```kotlin
package org.alter.plugins.content.areas.varrock.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class VarrockShopSpawns(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn shop keeper facing west, doesn't wander
        spawnNpc(
            npc = "npc.shop_keeper",
            x = 3211,
            z = 3246,
            height = 0,
            walkRadius = 0,  // 0 = doesn't move
            direction = Direction.WEST
        )

        // Spawn shop assistant nearby
        spawnNpc(
            npc = "npc.shop_assistant",
            x = 3212,
            z = 3246,
            height = 0,
            walkRadius = 2,
            direction = Direction.WEST
        )
    }
}
```

### Example 2: Spawn Banker

```kotlin
package org.alter.plugins.content.areas.edgeville.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class EdgevilleBankerSpawns(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn banker at Edgeville bank
        spawnNpc(
            npc = "npc.banker",
            x = 3096,
            z = 3489,
            height = 0,
            walkRadius = 0,
            direction = Direction.SOUTH
        )
    }
}
```

### Example 3: Spawn Combat NPCs

```kotlin
package org.alter.plugins.content.areas.wilderness.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class WildernessMonsterSpawns(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn green dragons in Wilderness
        val dragonSpawns = listOf(
            Pair(3340, 3664),
            Pair(3343, 3667),
            Pair(3337, 3670),
            Pair(3345, 3662)
        )

        dragonSpawns.forEach { (x, z) ->
            spawnNpc(
                npc = "npc.green_dragon",
                x = x,
                z = z,
                height = 0,
                walkRadius = 7  // Dragons can wander 7 tiles
            )
        }
    }
}
```

### Example 4: Spawn Boss

```kotlin
package org.alter.plugins.content.areas.sewers.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class SewerBossSpawns(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Cerberus as a boss in sewers
        spawnNpc(
            npc = "npc.cerberus",
            x = 3237,
            z = 9866,
            height = 0,
            walkRadius = 6
        )
    }
}
```

---

## Finding Coordinates

### Method 1: In-Game Command

If your server has position commands:

```
::pos
```

This will show your current X, Z, and Height coordinates.

### Method 2: Developer Tools

Some clients have built-in developer tools that show coordinates on screen.

### Method 3: OSRS Wiki

The [OSRS Wiki](https://oldschool.runescape.wiki/) often lists coordinates for specific locations.

---

## NPC Naming Convention

When using NPCs from the RSCM file, **always add the "npc." prefix**.

**Correct:**
```kotlin
spawnNpc("npc.cow", ...)           // ✓ Correct
spawnNpc("npc.king_black_dragon", ...) // ✓ Correct
spawnNpc("npc.cerberus", ...)      // ✓ Correct
```

**Incorrect:**
```kotlin
spawnNpc("cow", ...)               // ✗ Wrong - missing prefix
spawnNpc("king_black_dragon", ...) // ✗ Wrong - missing prefix
spawnNpc("Cerberus", ...)          // ✗ Wrong - wrong capitalization and missing prefix
```

---

## Organizing Spawn Plugins

### Recommended Structure

Organize your spawn plugins by area:

```
Alter/game-plugins/src/main/kotlin/org/alter/plugins/content/areas/
├── lumbridge/
│   ├── spawns/
│   │   ├── LumbridgeCowSpawns.kt
│   │   ├── LumbridgeChickenSpawns.kt
│   │   └── LumbridgeNpcSpawns.kt
├── varrock/
│   ├── spawns/
│   │   ├── VarrockGuardSpawns.kt
│   │   └── VarrockShopSpawns.kt
├── edgeville/
│   ├── spawns/
│   │   └── EdgevilleBankerSpawns.kt
```

### Naming Conventions

**Good Names:**
- `LumbridgeCowSpawns.kt`
- `VarrockShopKeeperSpawns.kt`
- `WildernessGreenDragonSpawns.kt`

**Bad Names:**
- `Spawns.kt` (too generic)
- `npcs.kt` (not descriptive)
- `test.kt` (unprofessional)

---

## Troubleshooting

### Problem: NPC Doesn't Spawn

**Causes:**
1. **Wrong NPC name** - Check spelling in npc.rscm
2. **Missing "npc." prefix**
3. **Invalid coordinates** - coordinates might be out of bounds
4. **Plugin not loading** - check server logs for errors

**Solution:**
```kotlin
// Make sure the NPC name exists in npc.rscm
// Check: grep "your_npc" Alter/data/cfg/rscm/npc.rscm

// Ensure you use "npc." prefix
spawnNpc("npc.cow", x = 3253, z = 3267, height = 0, walkRadius = 5)
```

### Problem: "RSCM returned -1" Error

**Cause:** NPC name doesn't exist in the client cache.

**Solution:**
1. Check the exact spelling in `npc.rscm`
2. Use an NPC that exists in the cache
3. See [NPC_MODEL_GUIDE.md](../../NPC_MODEL_GUIDE.md) for details

### Problem: NPC Spawns in Wrong Location

**Cause:** Incorrect coordinates or height.

**Solution:**
- Double-check coordinates using `::pos` command
- Ensure height is correct (0 = ground floor, 1 = first floor, etc.)
- Verify coordinates are within map bounds

### Problem: Multiple NPCs Spawn at Same Location

**Cause:** Duplicate spawn calls or copy-paste error.

**Solution:**
- Check for duplicate `spawnNpc()` calls
- Ensure each spawn has unique coordinates

---

## Advanced Topics

### NPC Variants

Many NPCs have multiple variants (different IDs for the same NPC type).

**Example - Unicorns:**
```
unicorn:2678
unicorn_2679:2679
unicorn_2680:2680
```

These are all unicorns but might have slight differences (colors, stats, etc.).

**Usage:**
```kotlin
spawnNpc("npc.unicorn", x = 3100, z = 3400, height = 0, walkRadius = 5)
spawnNpc("npc.unicorn_2679", x = 3105, z = 3405, height = 0, walkRadius = 5)
```

### Conditional Spawning

Spawn NPCs based on conditions:

```kotlin
init {
    // Only spawn if it's a PvP world
    if (world.gameContext.pvp) {
        spawnNpc("npc.guard", x = 3100, z = 3400, height = 0, walkRadius = 5)
    }
}
```

### Dynamic Spawning

Spawn NPCs at runtime (not on server start):

```kotlin
// This would go in an event handler, not init
fun spawnEventBoss() {
    world.spawn(
        Npc(
            def = world.definitions.get(NpcDef::class.java, "cerberus"),
            tile = Tile(3237, 9866, 0)
        ).apply {
            walkRadius = 6
        }
    )
}
```

---

## Quick Reference

### Basic Spawn Template

```kotlin
package org.alter.plugins.content.areas.AREA_NAME.spawns

import org.alter.api.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class YourSpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Add your spawns here
        spawnNpc("npc.REPLACE_ME", x = 0, z = 0, height = 0, walkRadius = 5)
    }
}
```

### Common NPCs Quick Reference

| NPC Type | Example Name | Common Use |
|----------|--------------|------------|
| Banker | `npc.banker` | Banks |
| Shop Keeper | `npc.shop_keeper` | General stores |
| Guard | `npc.guard` | City protection |
| Cow | `npc.cow` | Low-level combat/food |
| Chicken | `npc.chicken` | Very low-level combat |
| Man | `npc.man` | Low-level NPCs |
| Woman | `npc.woman` | Low-level NPCs |
| Dragon | `npc.green_dragon` | Mid-level combat |
| Unicorn | `npc.unicorn` | Mid-level NPCs |

---

## Summary

1. **Find NPCs** in `Alter/data/cfg/rscm/npc.rscm`
2. **Create plugin** in appropriate area folder
3. **Use `spawnNpc()`** with correct parameters
4. **Always use "npc." prefix** before NPC name
5. **Test in-game** to verify spawn location
6. **Check server logs** for any errors

---

## Additional Resources

- [NPC_MODEL_GUIDE.md](../../NPC_MODEL_GUIDE.md) - Understanding client vs server NPCs
- [OSRS Wiki](https://oldschool.runescape.wiki/) - NPC information and locations
- Server logs: `Alter/logs/server.log` - Check for plugin loading errors

---

**Created for:** Alter RSPS Development
**Last Updated:** 2025-01-16
**Version:** 1.0
