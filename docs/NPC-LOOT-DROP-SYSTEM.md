# NPC Loot Drop System Fix

## Overview
This document describes the implementation of a complete NPC loot drop system that was missing from the server. Previously, NPCs would die but not drop any items, making combat unrewarding for players.

## Problem Identified
The core issue was that while the loot table system existed and NPCs could have loot tables configured in their combat definitions, the NPC death action (`NpcDeathAction.kt`) never actually called the loot rolling function to generate and spawn drops.

## Solution Implementation

### 1. Core Loot Drop Handler
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/death/NpcLootDropPlugin.kt`

Created a universal NPC death handler that:
- Hooks into `onAnyNpcDeath` to catch all NPC deaths
- Retrieves the killer (player who dealt most damage)
- Extracts loot tables from the NPC's combat definition
- Uses the existing `roll()` function to generate loot
- Spawns ground items at the NPC's death location
- Includes error handling for misconfigured loot tables
- Provides optional messaging for valuable drops

### 2. Compilation Fix
**File**: `game-server/src/main/kotlin/org/alter/game/model/move/GroundItemRouteAction.kt`

Fixed Kotlin compilation error by adding proper lambda labels to resolve return statement conflicts.

### 3. Wilderness NPC Loot Configuration
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/wilderness/CombatConfigPlugin.kt`

Added complete loot tables for all wilderness monsters:

#### Dark Wizards (9 spawns)
- **Always**: Bones
- **Main Drops**: Coins (5-15), various runes (mind, air, water, earth, fire)
- **Rare Drops**: Wizard hat, black robe

#### Skeletons (12 spawns)
- **Always**: Bones
- **Main Drops**: Coins (3-12), bronze/iron arrows, bronze weapons
- **Rare Drops**: Iron dagger

#### Bandits (8 spawns)
- **Always**: Bones
- **Main Drops**: Coins (8-25), bread, beer, iron/steel weapons, leather gear
- **Rare Drops**: Lockpick

#### Chaos Druids (5 spawns)
- **Always**: Bones
- **Main Drops**: Coins (6-18), grimy herbs (guam to irit), runes (nature, law, chaos)
- **Rare Drops**: Grimy ranarr

#### Wolves (9 spawns)
- **Always**: Bones
- **Main Drops**: Coins (2-8), raw beef, cowhide, wolf bones

#### Dark Warriors (6 spawns)
- **Always**: Bones
- **Main Drops**: Coins (25-60), steel/mithril weapons, armor pieces, runes
- **Rare Drops**: Mithril sword

#### Green Dragons (8 spawns)
- **Always**: Dragon bones, green dragonhide
- **Main Drops**: Coins (50-150), nature/law runes, air/fire runes, arrows, weapons
- **Tertiary Drops**: Dragon med helm (1/128), Shield left half (1/256)

#### Hellhounds (6 spawns)
- **Always**: Bones
- **Main Drops**: Coins (75-200), blood/death/soul runes, rune weapons, gems
- **Tertiary Drops**: Dragon spear (1/512), Rune platebody (1/384)

### 4. Lumbridge Area Combat Configuration
**File**: `game-plugins/src/main/kotlin/org/alter/plugins/content/areas/lumbridge/LumbridgeCombatConfigPlugin.kt`

Created complete combat definitions and loot tables for Lumbridge creatures:

#### Rats (6+ spawns)
- **Stats**: 5 HP, non-aggressive
- **Always**: Bones
- **Main Drops**: Coins (1-3), raw rat meat

#### Giant Spiders (7+ spawns)
- **Stats**: 15 HP, non-aggressive
- **Always**: Bones
- **Main Drops**: Coins (2-8), spider silk, spider legs
- **Rare Drops**: Poison spider eggs

#### Goblins (8+ spawns)
- **Stats**: 18 HP, aggressive (3 tile radius)
- **Always**: Bones
- **Main Drops**: Coins (3-12), bronze weapons, arrows, goblin mail

#### Imps (1 spawn)
- **Stats**: 12 HP, fast attacks, non-aggressive
- **Always**: Bones
- **Main Drops**: Coins (1-5), bread, beer, colored beads
- **Tertiary Drops**: Imp jar (1/20)

#### Sheep (5+ spawns)
- **Stats**: 8 HP, slow attacks, non-aggressive
- **Always**: Bones, raw mutton
- **Main Drops**: Wool (1-3), coins (1-4)

#### Rams (3+ spawns)
- **Stats**: 12 HP, mildly aggressive (2 tile radius)
- **Always**: Bones, raw mutton
- **Main Drops**: Wool (2-4), coins (2-8)

#### Zombie Rats (2 spawns)
- **Stats**: 8 HP, non-aggressive
- **Always**: Bones
- **Main Drops**: Coins (1-5), raw rat meat, rotten food

## Loot Table System Structure

### Table Types
1. **Always**: Items guaranteed to drop every time
2. **Main**: Primary drop table with weighted rolls
3. **Tertiary**: Independent rare drop rolls

### Weight System
- Each item in a table has a weight determining drop chance
- Higher weight = more common
- Total table weight determines overall drop rates

### Example Configuration
```kotlin
drops {
    always {
        add("item.bones", 1)
    }
    
    main(tableWeight = 100) {
        add("item.coins", min = 5, max = 15, weight = 40)
        add("item.sword", min = 1, weight = 10)
    }
    
    tertiary {
        add("item.rare_drop", min = 1, weight = 128) // 1/128 chance
    }
}
```

## NPCs Now With Loot Systems

### Wilderness Area (63+ NPCs)
- 9 Dark Wizards
- 12 Skeletons  
- 8 Bandits
- 5 Chaos Druids
- 9 Wolves
- 6 Dark Warriors
- 8 Green Dragons
- 6 Hellhounds

### Lumbridge Area (30+ NPCs)
- 6+ Rats
- 7+ Giant Spiders
- 8+ Goblins
- 1 Imp
- 5+ Sheep
- 3+ Rams
- 2 Zombie Rats

### Pre-existing NPCs
- Cows (already had combat definitions)
- King Black Dragon (already had combat definitions)
- Barrows Brothers (already had combat definitions)

## Impact
- **Combat is now rewarding**: Players receive appropriate loot for defeating monsters
- **Economy support**: Coins and resources enter the game through monster drops
- **Progression system**: Different monsters provide different tiers of rewards
- **Thematic consistency**: Drops match the monster type (dragons drop dragon items, etc.)

## Technical Details
- The system is fail-safe with error handling for misconfigured loot tables
- Loot ownership is properly assigned to the killer
- Ground items spawn at the NPC's death location
- The system integrates seamlessly with existing ground item mechanics
- No changes required to existing NPC combat logic - purely additive

## Future Considerations
- Additional NPCs can easily be given loot tables using the same system
- Loot tables can be adjusted for balance without changing core mechanics
- The system supports complex drop mechanics (announced drops, conditional drops, etc.)
- Integration with future content like bosses and special monsters is straightforward
