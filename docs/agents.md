# AI Agent Guide for Alter PNDA RSPS

## Project Overview

This is an **Old School RuneScape (OSRS) Private Server** built in **Kotlin** using a custom plugin-based architecture. The server emulates OSRS gameplay mechanics including combat, skills, NPCs, items, and game content.

### Technology Stack
- **Language**: Kotlin (JVM)
- **Build Tool**: Gradle
- **Architecture**: Plugin-based modular system
- **Game Version**: OSRS Revision 228

## Project Structure

```
alter-pnda-rsps/
├── game-server/        # Core game engine and server logic
├── game-api/           # API extensions and utilities
├── game-plugins/       # Content plugins (NPCs, combat, items, areas, etc.)
├── plugins/
│   ├── rscm/          # Resource configuration management
│   ├── tools/         # Development tools
│   └── filestore/     # Cache and file management
└── data/
    └── cfg/           # Configuration files (.rscm files)
```

### Key Directories

- **`game-plugins/src/main/kotlin/org/alter/plugins/content/`** - All game content plugins
  - `areas/` - Location-specific content (wilderness, God Wars, etc.)
  - `combat/` - Combat system and formulas
  - `items/` - Item interactions and behaviors
  - `npcs/` - NPC configurations and behaviors
  - `mechanics/` - Game mechanics (trading, shops, equipment, etc.)
  - `skills/` - Skill implementations

- **`game-server/src/main/kotlin/org/alter/game/`** - Core server code
  - `model/` - Game models (Player, Npc, Item, Tile, etc.)
  - `plugin/` - Plugin system base classes
  - `service/` - Game services

- **`game-api/src/main/kotlin/org/alter/api/`** - Extension functions and utilities

## Core Concepts

### 1. Plugin System

All game content is implemented as **Kotlin plugins** that extend `KotlinPlugin`:

```kotlin
class MyContentPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Register your handlers here
    }
}
```

### 2. Event Handlers

Plugins register handlers for various game events:

```kotlin
// NPC spawn
onNpcSpawn(npc = "npc.spiritual_mage") {
    npc.combatClass = CombatClass.MAGIC
}

// Item click
onItemOption("item.bracelet_of_ethereum", "Check") {
    player.message("Charges: ${charges}")
}

// Equipment option
onEquipmentOption("item.bracelet_of_ethereum", "Check") {
    // Use exact option text as shown in error messages
}

// Object interaction
onObjOption("obj.altar", "Pray-at") {
    player.skills.restore(Skills.PRAYER)
}

// Combat
onNpcCombat(npc = "npc.revenant_imp") {
    // Custom combat logic
}
```

### 3. NPC Combat Configuration

Use the DSL to configure NPC combat stats, animations, aggression, and drops:

```kotlin
setCombatDef("npc.spiritual_warrior") {
    configs {
        attackSpeed = 4
        respawnDelay = 30
    }
    
    stats {
        hitpoints = 150
        attack = 120
        strength = 120
        defence = 120
        magic = 1
        ranged = 1
    }
    
    bonuses {
        attackStab = 0
        attackSlash = 90
        attackCrush = 0
        attackMagic = 0
        attackRanged = 0
        
        defenceStab = 70
        defenceSlash = 70
        defenceCrush = 70
        defenceMagic = 60
        defenceRanged = 70
        
        attackBonus = 0
        strengthBonus = 85
        rangedStrengthBonus = 0
        magicDamageBonus = 0
    }
    
    anims {
        attack = 6184
        block = 6183
        death = 6182
    }
    
    aggro {
        radius = 8
        searchDelay = 1
        aggroMinutes = 10
    }
    
    drops {
        main(table = "gwd_spiritual_warrior")
        main(table = "gwd_common")
    }
}
```

### 4. Item Options

**CRITICAL**: Item options work differently for inventory vs. equipped items.

```kotlin
// For items in INVENTORY
onItemOption("item.bracelet_of_ethereum", "Check") {
    val braceletSlot = player.inventory.getItemIndex(getRSCM("item.bracelet_of_ethereum"), false)
    val bracelet = player.inventory[braceletSlot]
    // ...
}

// For items EQUIPPED
onEquipmentOption("item.bracelet_of_ethereum", "Check") {
    val bracelet = player.getEquipment(EquipmentType.GLOVES)
    // ...
}

// ⚠️ IMPORTANT: Equipment option names can differ from inventory options!
// Always check error messages for exact option text:
// Error: "Option 'Toggle-absorption' not found for item equipment [options=[Check, Toggle absorption]]"
// This tells you the equipped version uses "Toggle absorption" (with space), not "Toggle-absorption" (with hyphen)
```

### 5. Combat Attributes

Use attributes to modify combat behavior:

```kotlin
// Damage multipliers
npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = 10.0  // NPC deals 10x damage
player.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.5  // Player takes 50% damage

// Combat class
npc.combatClass = CombatClass.MAGIC  // or RANGED, MELEE

// Casting spell
npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
```

### 6. Tile and Coordinate System

```kotlin
// Check wilderness level
val wildLevel = tile.getWildernessLevel()

// Tile coordinates (x, y, z)
// z = height level (0 = ground, 1 = first floor, etc.)
// Special zones can use high z values (e.g., Revenant Caves uses z >= 10000)

// Coordinate checks
if (tile.z >= 10000 && tile.z <= 10300 && tile.x >= 3100 && tile.x <= 3300) {
    // In Revenant Caves
}

// Distance checks
if (npc.tile.isWithinRadius(player.tile, 8)) {
    // Within 8 tiles
}
```

## Common Patterns

### Pattern 1: Player Interaction with Items

```kotlin
onItemOption("item.my_item", "Use") {
    val itemSlot = player.inventory.getItemIndex(getRSCM("item.my_item"), false)
    val item = player.inventory[itemSlot] ?: return@onItemOption
    
    // Get/set attributes
    val charges = item.getAttr(ItemAttribute.CHARGES) ?: 0
    item.putAttr(ItemAttribute.CHARGES, charges + 1)
    
    // Modify inventory
    player.inventory[itemSlot] = item
    
    player.message("You use the item.")
}
```

### Pattern 2: Item on Item

```kotlin
onItemOnItem("item.source_item", "item.target_item") {
    val sourceSlot = player.inventory.getItemIndex(getRSCM("item.source_item"), false)
    val targetSlot = player.inventory.getItemIndex(getRSCM("item.target_item"), false)
    
    val source = player.inventory[sourceSlot] ?: return@onItemOnItem
    val target = player.inventory[targetSlot] ?: return@onItemOnItem
    
    // Do something
    player.inventory.remove(source.id, 1)
    player.inventory.add(getRSCM("item.result"), 1)
}
```

### Pattern 3: Equipment Handling

```kotlin
// Check if wearing item
if (player.hasEquipped(EquipmentType.WEAPON, "item.abyssal_whip")) {
    // Player has whip equipped
}

// Get equipped item
val weapon = player.getEquipment(EquipmentType.WEAPON)
if (weapon != null && weapon.id == getRSCM("item.abyssal_whip")) {
    // ...
}

// Equipment types
EquipmentType.HEAD, CAPE, AMULET, WEAPON, CHEST, SHIELD, 
LEGS, GLOVES, BOOTS, RING, AMMO
```

### Pattern 4: NPC Spawning

```kotlin
onNpcSpawn(npc = "npc.my_npc") {
    // Set combat properties
    npc.combatClass = CombatClass.MAGIC
    npc.attr[Combat.CASTING_SPELL] = CombatSpell.FIRE_BLAST
    
    // Set attributes
    npc.attr[MY_CUSTOM_ATTR] = someValue
}
```

### Pattern 5: Custom Combat Logic

```kotlin
onNpcCombat(npc = "npc.my_boss") {
    var target = npc.getCombatTarget() ?: return@onNpcCombat
    
    while (npc.isAlive() && target.isAlive()) {
        // Attack logic
        npc.animate(npc.combatDef.attackAnimation)
        npc.dealHit(target, Hit(damage, HitType.NORMAL))
        
        wait(npc.combatDef.attackSpeed)
        
        // Re-validate target
        target = npc.getCombatTarget() ?: break
    }
}
```

### Pattern 6: Multi-Combat Regions

```kotlin
class MyMultiCombatPlugin : KotlinPlugin(r, world, server) {
    init {
        // Region IDs define 64x64 tile areas
        // Calculate: regionId = ((x >> 6) << 8) | (y >> 6)
        setMultiCombatRegion(11602) // Saradomin area
        setMultiCombatRegion(11603) // Zamorak area
    }
}
```

### Pattern 7: Timers and Delays

```kotlin
// Suspend and wait
wait(5) // Wait 5 game ticks

// Set timer for later execution
player.timers[TIMER_KEY] = 100 // 100 ticks

// Check timer
if (player.timers.has(TIMER_KEY)) {
    player.message("You must wait.")
    return
}
```

## Combat System Deep Dive

### Combat Formulas

The combat system has three formula types:
- **MeleeCombatFormula** - Handles STAB, SLASH, CRUSH combat styles
- **RangedCombatFormula** - Handles ranged combat
- **MagicCombatFormula** - Handles magic combat

### Important: Combat Style Handling

**Problem**: NPCs with `CombatClass.MAGIC` or `CombatClass.RANGED` can still be processed through melee formulas if their combat strategy isn't properly set.

**Solution**: Melee formula should gracefully handle non-melee styles:

```kotlin
private fun getEquipmentAttackBonus(pawn: Pawn): Double {
    val combatStyle = CombatConfigs.getCombatStyle(pawn)
    val bonus = when (combatStyle) {
        CombatStyle.STAB -> BonusSlot.ATTACK_STAB
        CombatStyle.SLASH -> BonusSlot.ATTACK_SLASH
        CombatStyle.CRUSH -> BonusSlot.ATTACK_CRUSH
        else -> return 0.0 // Non-melee styles return 0 instead of throwing error
    }
    return pawn.getBonus(bonus).toDouble()
}
```

### Wilderness Mechanics

Wilderness weapons get bonuses, and NPCs deal increased damage:

```kotlin
// Check if in wilderness
val wildernessLevel = tile.getWildernessLevel()
if (wildernessLevel > 0) {
    // Apply wilderness bonuses
}

// Special case: Revenant Caves (z >= 10000)
val isInRevenantCaves = tile.z >= 10000 && tile.z <= 10300 &&
                        tile.x >= 3100 && tile.x <= 3300
```

### Damage Multipliers

```kotlin
// In combat formulas:
val damageMultiplier = pawn.attr[Combat.DAMAGE_DEAL_MULTIPLIER] ?: 1.0
damage = (damage * damageMultiplier).toInt()

val damageTakeMultiplier = target.attr[Combat.DAMAGE_TAKE_MULTIPLIER] ?: 1.0
damage = (damage * damageTakeMultiplier).toInt()
```

## Avoiding Compilation Errors

### Error 1: Unresolved Reference

**Problem**: `Unresolved reference 'World'`

**Solution**: Use correct import path
```kotlin
// ❌ WRONG
import org.alter.game.World

// ✅ CORRECT
import org.alter.game.model.World
```

### Error 2: For-loop Range Iterator

**Problem**: `For-loop range must have an 'iterator()' method`

**Cause**: `PawnList` doesn't support direct iteration in for-loops

**Solution**: Use `forEach` instead
```kotlin
// ❌ WRONG
for (player in npc.world.players) {
    // ...
}

// ✅ CORRECT
npc.world.players.forEach { player ->
    // ...
}
```

### Error 3: Invalid Combat Style

**Problem**: `IllegalStateException: Invalid combat style. MAGIC`

**Cause**: Melee formula doesn't handle non-melee combat styles

**Solution**: Return default value instead of throwing exception
```kotlin
// ❌ WRONG
else -> throw IllegalStateException("Invalid combat style. $combatStyle")

// ✅ CORRECT
else -> return 0.0 // Non-melee styles have no melee bonus
```

### Error 4: Option Not Found for Item

**Problem**: `IllegalStateException: Option "Toggle-absorption" not found for item equipment [options=[Check, Toggle absorption]]`

**Cause**: Equipment options use different text than inventory options

**Solution**: Check error message for exact option text
```kotlin
// Inventory uses: "Toggle-absorption" (with hyphen)
onItemOption("item.bracelet_of_ethereum", "Toggle-absorption") { }

// Equipment uses: "Toggle absorption" (with space)
onEquipmentOption("item.bracelet_of_ethereum", "Toggle absorption") { }
```

### Error 5: Max Health Must Be Set

**Problem**: `IllegalStateException: Max health must be set`

**Cause**: Using `setCombatDef` without setting `hitpoints` in `stats` block

**Solution**: Always set hitpoints when creating combat definitions
```kotlin
setCombatDef("npc.my_npc") {
    stats {
        hitpoints = 100  // ✅ REQUIRED
        attack = 50
        // ... other stats
    }
}
```

### Error 6: Missing Extension Function

**Problem**: `Unresolved reference 'hasEquipped'`

**Cause**: Extension function not imported

**Solution**: Add import
```kotlin
import org.alter.api.ext.hasEquipped
```

### Error 7: Cannot Infer Type

**Problem**: `Cannot infer type for this parameter`

**Cause**: Using methods that don't exist on collections (e.g., `.filter` on PawnList)

**Solution**: Convert to list first or use appropriate method
```kotlin
// ❌ WRONG
val enemies = npc.world.npcs.filter { it.id == targetId }

// ✅ CORRECT
val enemies = mutableListOf<Npc>()
npc.world.npcs.forEach { otherNpc ->
    if (otherNpc.id == targetId) {
        enemies.add(otherNpc)
    }
}
```

## Best Practices

### 1. **Always Import Required Extensions**

```kotlin
import org.alter.api.ext.*
import org.alter.api.ext.hasEquipped
import org.alter.api.ext.message
import org.alter.api.ext.getEquipment
```

### 2. **Use getRSCM() for Item/NPC IDs**

```kotlin
// ✅ CORRECT - Uses named identifiers from .rscm files
val whipId = getRSCM("item.abyssal_whip")
val impId = getRSCM("npc.revenant_imp")

// ❌ WRONG - Magic numbers are fragile
val whipId = 4151
```

### 3. **Null Safety**

```kotlin
// Always handle nulls properly
val item = player.inventory[slot] ?: return
val target = npc.getCombatTarget() ?: return@onNpcCombat
```

### 4. **Use Attributes for State**

```kotlin
// Store state on entities using attributes
npc.attr[MY_STATE] = value
val state = npc.attr[MY_STATE] ?: defaultValue

// Common attribute keys
Combat.DAMAGE_DEAL_MULTIPLIER
Combat.DAMAGE_TAKE_MULTIPLIER
Combat.CASTING_SPELL
ItemAttribute.CHARGES
ItemAttribute.ATTACHED_ITEM_ID
```

### 5. **Validate Before Acting**

```kotlin
// Check conditions before performing actions
if (!player.inventory.hasSpace) {
    player.message("Your inventory is full.")
    return
}

if (!npc.isAlive() || !target.isAlive()) {
    return
}

if (player.timers.has(COOLDOWN_TIMER)) {
    player.message("You must wait before doing that again.")
    return
}
```

### 6. **Clean Up Debug Code**

```kotlin
// ❌ REMOVE before committing
println("Here")
println("Debug: $value")

// ✅ Use proper logging if needed
logger.debug { "NPC spawned: ${npc.id}" }
```

### 7. **Coordinate Validation**

```kotlin
// When adding area-specific logic, validate coordinates
private fun isInSpecialArea(tile: Tile): Boolean {
    return tile.z >= 10000 && tile.z <= 10300 &&
           tile.x >= 3100 && tile.x <= 3300
}
```

### 8. **Combat Class Assignment**

```kotlin
// Set combat class on spawn, not in combat definitions
onNpcSpawn(npc = "npc.spiritual_mage") {
    npc.combatClass = CombatClass.MAGIC
    npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
}
```

## Testing and Building

### Build Commands

```bash
# Build entire project
./gradlew.bat build

# Build specific module (faster)
./gradlew.bat :game-plugins:build -x test

# Clean and rebuild
./gradlew.bat clean build
```

### Common Build Issues

1. **Kotlin daemon errors**: Restart IDE or kill Kotlin daemon
2. **Out of memory**: Increase Gradle heap size in `gradle.properties`
3. **Stale caches**: Run `./gradlew.bat clean`

### Testing in Game

1. Build the project
2. Start the server
3. Test specific functionality
4. Check server logs for errors
5. Use debug messages (remove before committing!)

## File Naming Conventions

- **Plugins**: `MyFeaturePlugin.kt`
- **Combat**: `MyNpcCombatPlugin.kt` for logic, `MyNpcConfigsPlugin.kt` for stats
- **Areas**: Place in `content/areas/myarea/`
- **Items**: Place in `content/items/myitem/`

## Common Tasks

### Adding a New NPC

1. Create combat config plugin with stats
2. Create combat plugin for special behavior (if needed)
3. Create spawn plugin to place in world
4. Define drops in combat config or separate drops file
5. Set combat class if non-melee

### Adding a New Item

1. Define in appropriate `.rscm` file
2. Create plugin for item interactions
3. Handle both inventory and equipment options if applicable
4. Add item attributes for state (charges, etc.)

### Adding a New Area

1. Create area directory in `content/areas/myarea/`
2. Create plugins for area-specific mechanics
3. Add spawn plugin for NPCs
4. Configure multi-combat regions if needed
5. Add teleport handlers if applicable

### Modifying Combat Formulas

1. Identify which formula to modify (Melee/Ranged/Magic)
2. Add coordinate checks for special areas if needed
3. Apply multipliers via attributes, not hardcoded values
4. Handle edge cases (null checks, non-standard combat styles)
5. Test thoroughly in-game

## Resource Files

### .rscm Files

Located in `data/cfg/rscm/`, these define game objects:

- `item.rscm` - Item definitions
- `npc.rscm` - NPC definitions
- `obj.rscm` - Object definitions
- `shop.rscm` - Shop definitions
- `drops.rscm` - Drop table definitions

## Debugging Tips

1. **Check error messages carefully** - They often tell you exactly what's wrong
2. **Read stack traces** - Find the line number where error occurs
3. **Use println() temporarily** - But remove before committing
4. **Test incrementally** - Don't change many things at once
5. **Check server logs** - Many runtime issues appear there
6. **Verify coordinates** - Use in-game commands to check tile positions
7. **Validate assumptions** - Check that IDs, names, and options are correct

## Important APIs

### Player Extensions
- `player.message(text)` - Send chat message
- `player.hasEquipped(type, item)` - Check equipment
- `player.getEquipment(type)` - Get equipped item
- `player.inventory` - Access inventory
- `player.tile` - Player location
- `player.skills` - Access skills

### NPC Extensions
- `npc.attack(target)` - Start combat
- `npc.getCombatTarget()` - Get current target
- `npc.dealHit(target, hit)` - Apply damage
- `npc.animate(id)` - Play animation
- `npc.combatDef` - Get combat definition

### Tile Extensions
- `tile.getWildernessLevel()` - Get wilderness level
- `tile.isWithinRadius(other, radius)` - Distance check
- `tile.x, tile.y, tile.z` - Coordinates

## Summary

This codebase uses a **plugin-based architecture** where all content is modular and extensible. Key principles:

1. **Plugins extend KotlinPlugin** and register event handlers
2. **Use DSLs** for NPC combat configuration
3. **Handle edge cases** (nulls, different combat styles, etc.)
4. **Follow naming conventions** (getRSCM, proper imports)
5. **Test incrementally** and watch for compilation errors
6. **Clean up debug code** before committing

The most common errors are:
- Wrong import paths
- Using wrong iteration methods on PawnList
- Not handling non-melee combat styles
- Mismatched item option names (inventory vs equipped)
- Missing required fields in combat definitions

Always check error messages carefully - they usually tell you exactly what's wrong!

