# 🎮 How to Create Your Own Boss! 🐉

Hello! Today we are going to learn how to make a **BRAND NEW BOSS** for your game!

Follow these 3 simple steps to get started:

### 1️⃣ Create a New File
Go to your plugins folder at:
`game-plugins/src/main/kotlin/org/alter/plugins/content/bosses`

Create a NEW folder for your boss (e.g. `myboss`) and then create a NEW file inside it called:
`MyBossPlugin.kt`

(You can name it whatever you want, like `SuperDragon.kt` or `MegaGiant.kt`)

### 2️⃣ Find Your Boss ID
We need to know the code name for the monster you want to use.

1.  Open the file: `data/cfg/rscm/npc.rscm`
2.  Press **Ctrl+F** and search for the monster you want (e.g. "rock" or "dragon").
3.  Copy the full name, like `npc.rock_925` or `npc.king_black_dragon`.
4.  Use this name in your code!

### 3️⃣ Copy This Template
Copy and paste this **ENTIRE** code block into your new file. This is your starting point!

```kotlin
package org.alter.plugins.content.bosses.example

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.model.combat.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.rscm.RSCM.getRSCM

// 👉 CHANGE "MyBossPlugin" to your boss name!
class MyBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // ============================================================
        // 1. SPAWN THE BOSS 📍
        // ============================================================
        // This makes the boss appear in the game
        // 👉 CHANGE: Replace "npc.crazy_archaeologist" with your boss NPC name
        // 👉 CHANGE: Replace x and z with coordinates where you want the boss
        // Type ::coords in-game to find coordinates!
        spawnNpc("npc.crazy_archaeologist", x = 3200, z = 3200, walkRadius = 5)
        
        // 💡 MORE SPAWN EXAMPLES (uncomment to try):
        // spawnNpc("npc.king_black_dragon", x = 2272, z = 4680, walkRadius = 3)  // KBD location
        // spawnNpc("npc.rock_925", x = 2977, z = 3238, walkRadius = 10)  // Bigger walk area
        // spawnNpc("npc.dragon_245", x = 3200, z = 3200, walkRadius = 0)  // Doesn't walk around
        // spawnNpc("npc.crazy_archaeologist", x = 3200, z = 3200, height = 1, walkRadius = 5)  // On different floor

        // ============================================================
        // 2. DEFINE STATS & DROPS 📊
        // ============================================================
        // 👉 CHANGE: Replace "npc.crazy_archaeologist" with your boss NPC name
        setCombatDef("npc.crazy_archaeologist") {
            configs {
                attackSpeed = 4      // 👉 ADJUST: How fast it attacks (lower = faster)
                // attackSpeed = 3      // Very fast (3 ticks between attacks)
                // attackSpeed = 5      // Medium speed
                // attackSpeed = 6      // Slow but powerful
                
                respawnDelay = 50     // 👉 ADJUST: How long before it comes back after dying (in ticks)
                // respawnDelay = 30    // Quick respawn
                // respawnDelay = 100   // Slow respawn (harder to farm)
            }
            
            aggro {
                radius = 10          // 👉 ADJUST: How far it can see players
                // radius = 5          // Short range (only attacks nearby)
                // radius = 15          // Long range (sees players far away)
                
                searchDelay = 2       // 👉 ADJUST: How often it looks for players
                // searchDelay = 1      // Checks every tick (very aggressive)
                // searchDelay = 5      // Checks less often (lazy boss)
                
                alwaysAggro()         // 👉 ADJUST: Attacks players even if they're high level
                // remove alwaysAggro() to make it only attack players near its level
            }
            
            stats {
                // 👉 ADJUST: Change these numbers to make your boss stronger or weaker!
                hitpoints = 500      // How much health it has
                // hitpoints = 100      // Weak boss (dies fast)
                // hitpoints = 1000     // Tank boss (hard to kill)
                // hitpoints = 2500     // Super tank (very hard to kill)
                
                attack = 200         // How accurate it is (higher = hits more often)
                // attack = 50          // Inaccurate (misses a lot)
                // attack = 450          // Very accurate (hits almost always)
                
                strength = 200       // How hard it hits (higher = more damage)
                // strength = 50        // Weak hits
                // strength = 450       // Very strong hits
                // strength = 6000      // One-shot potential!
                
                defence = 150         // How well it blocks (higher = takes less damage)
                // defence = 50         // Low defense (takes lots of damage)
                // defence = 450        // High defense (tanky)
                
                magic = 300          // 👉 ADJUST: Magic accuracy and damage
                // magic = 1            // No magic (melee/ranged only)
                // magic = 450          // Strong magic user
                
                ranged = 1           // 👉 ADJUST: Ranged accuracy and damage
                // ranged = 450         // Strong ranged attacker
            }
            
            // 💡 STAT PRESETS (uncomment one to try):
            // TANK BOSS (High HP and Defense, but slow and weak attacks)
            // stats {
            //     hitpoints = 2000
            //     attack = 100
            //     strength = 100
            //     defence = 450
            //     magic = 1
            //     ranged = 1
            // }
            
            // GLASS CANNON (Low HP and Defense, but hits very hard)
            // stats {
            //     hitpoints = 100
            //     attack = 450
            //     strength = 6000
            //     defence = 50
            //     magic = 1
            //     ranged = 1
            // }
            
            // BALANCED BOSS (Good at everything)
            // stats {
            //     hitpoints = 1000
            //     attack = 300
            //     strength = 300
            //     defence = 300
            //     magic = 300
            //     ranged = 300
            // }
            
            // MAGIC BOSS (Strong magic, weak melee)
            // stats {
            //     hitpoints = 800
            //     attack = 100
            //     strength = 100
            //     defence = 200
            //     magic = 450
            //     ranged = 1
            // }
            
            bonuses {
                // 👉 ADJUST: Defense bonuses (how well it blocks different attack types)
                defenceStab = 50     // Defense against stabbing weapons
                // defenceStab = 200    // Very resistant to stabs
                
                defenceSlash = 50    // Defense against slashing weapons
                // defenceSlash = 200   // Very resistant to slashes
                
                defenceCrush = 50    // Defense against crushing weapons
                // defenceCrush = 200   // Very resistant to crushes
                
                defenceMagic = 100   // Defense against magic attacks
                // defenceMagic = 50    // Weak to magic
                // defenceMagic = 400   // Very resistant to magic
                
                defenceRanged = 50   // Defense against ranged attacks
                // defenceRanged = 200  // Very resistant to ranged
                
                // 💡 ATTACK BONUSES (uncomment to add):
                // attackStab = 100     // More accurate with stabs
                // attackSlash = 100    // More accurate with slashes
                // attackCrush = 100    // More accurate with crushes
                // attackMagic = 200    // More accurate with magic
                // attackRanged = 200   // More accurate with ranged
                // strengthBonus = 100  // Hits harder
                // magicDamageBonus = 200 // Magic hits harder
            }
            
            anims {
                // 👉 ADJUST: Change animation IDs to make your boss look different!
                attack = 3353        // Animation when it attacks
                // attack = 422         // Quick punch
                // attack = 451         // Sword swing
                // attack = 423         // Aggressive punch
                // attack = 64          // Demon claw
                // attack = 81          // Dragon claw
                // attack = 7060        // Heavy/unblockable attack
                // attack = 1978        // Single-target magic cast
                // attack = 1979        // Multi-target magic cast
                // attack = 2652        // Ranged attack
                // attack = 2656        // Magic attack (TzTok-Jad style)
                
                block = 424          // Animation when it blocks
                // block = 1683         // Different block animation
                // block = 424          // Standard block (most common)
                
                death = 836          // Animation when it dies
                // death = 1684         // Alternative death
                // death = 92           // Dragon death
                // death = 836          // Standard death (most common)
            }
            
            // 🔊 SOUNDS: Make your boss sound epic! (Optional but recommended)
            // 👉 ADJUST: Uncomment different sounds to try them!
            sound {
                attackSound = Sound.ROCK_CRAB_ATTACK  // Sound when boss attacks
                // attackSound = Sound.DRAGON_ATTACK    // Dragon roar
                // attackSound = Sound.DEMON_ATTACK    // Demon growl
                // attackSound = Sound.CHAOS_ELEMENTAL_ATTACK  // Chaos sound
                // attackSound = Sound.DARK_BEAST_ATTACK  // Dark beast sound
                // attackSound = Sound.COW_ATTACK        // Moo! (funny)
                
                attackArea = true                     // All nearby players hear it
                attackVolume = 50                     // 👉 ADJUST: Volume (0-100)
                // attackVolume = 30     // Quiet
                // attackVolume = 80     // Very loud
                attackRadius = 10                     // 👉 ADJUST: How far sound travels
                
                blockSound = Sound.ROCK_CRAB_HIT     // Sound when boss blocks
                // blockSound = Sound.DRAGON_HIT        // Dragon hit sound
                // blockSound = Sound.DEMON_HIT         // Demon hit sound
                // blockSound = Sound.COW_HIT           // Cow hit sound
                
                blockArea = true
                blockVolume = 40                      // 👉 ADJUST: Volume
                blockRadius = 8
                
                deathSound = Sound.ROCK_CRAB_DEATH   // Sound when boss dies
                // deathSound = Sound.DRAGON_DEATH      // Dragon death roar
                // deathSound = Sound.DEMON_DEATH       // Demon death sound
                // deathSound = Sound.CHAOS_ELEMENTAL_DEATH  // Chaos death
                // deathSound = Sound.COW_DEATH         // Cow death (moo!)
                
                deathArea = true
                deathVolume = 60                      // 👉 ADJUST: Volume
                deathRadius = 12
            }
            
            // 💡 NO SOUNDS (uncomment to remove all sounds):
            // Remove the entire "sound { }" block if you don't want any sounds
            
            drops {
                // 👉 ADJUST: Change what your boss drops!
                always { 
                    add("item.big_bones", 1)  // Always drops bones
                    // add("item.bones", 1)     // Or regular bones
                }
                
                // ⚠️ IMPORTANT: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                main(weight = 128) {  // 👉 ADJUST: Table weight (must be >= sum of item weights)
                    // ITEM WEIGHTS:
                    // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                    // - Relative weights determine drop probability within the table
                    
                    add("item.coins", min = 5000, max = 10000, weight = 20)  // Common drop
                    // add("item.coins", min = 1000, max = 5000, weight = 50)   // More common, less coins
                    // add("item.coins", min = 10000, max = 50000, weight = 5)  // Rare but lots of coins
                    
                    add("item.dragon_scimitar", min = 1, max = 1, weight = 5)  // 👉 ADJUST: Rare drop!
                    // add("item.dragon_scimitar", min = 1, max = 1, weight = 1)   // Very rare
                    // add("item.dragon_scimitar", min = 1, max = 1, weight = 20)  // More common
                    
                    // 💡 MORE DROP EXAMPLES (uncomment to add):
                    // add("item.rune_scimitar", min = 1, max = 1, weight = 30)     // Common weapon
                    // add("item.dragon_longsword", min = 1, max = 1, weight = 10)  // Rare weapon
                    // add("item.abyssal_whip", min = 1, max = 1, weight = 2)       // Very rare weapon
                    // add("item.shark", min = 5, max = 10, weight = 40)         // Food (5-10 sharks)
                    // add("item.super_restore_4", min = 1, max = 1, weight = 25)   // Potion
                    // add("item.dragon_bones", min = 5, max = 10, weight = 15)  // Bones (5-10)
                }
            }
            
            // 💡 RICH BOSS DROP TABLE (uncomment to try):
            // drops {
            //     always { add("item.big_bones", 1) }
            //     main(weight = 200) {  // Must be >= sum of all item weights!
            //         add("item.coins", min = 10000, max = 50000, weight = 30)
            //         add("item.dragon_scimitar", min = 1, max = 1, weight = 5)
            //         add("item.dragon_longsword", min = 1, max = 1, weight = 5)
            //         add("item.abyssal_whip", min = 1, max = 1, weight = 2)
            //         add("item.shark", min = 10, max = 20, weight = 50)
            //         add("item.super_restore_4", min = 3, max = 5, weight = 40)
            //     }
            // }
        }

        // ============================================================
        // 3. COMBAT LOGIC ⚔️
        // ============================================================
        // When the boss starts fighting, run the combat loop
        // 👉 CHANGE: Replace "npc.crazy_archaeologist" with your boss NPC name
        onNpcCombat("npc.crazy_archaeologist") { npc.queue { combatLoop() } }
    }

    // ============================================================
    // This is the brain of the boss! 🧠
    // ============================================================
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        // Keep fighting while boss is alive
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // 👉 ADJUST: Change distance and projectile settings
            // distance = 1 means melee range (must be next to player)
            // distance = 7 means ranged/magic range (can attack from far away)
            // projectile = false means melee attack
            // projectile = true means ranged/magic attack (needs projectile)
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && 
                npc.isAttackDelayReady()) {
                
                // ============================================================
                // ATTACK SELECTION - Choose one of these patterns! 🎯
                // ============================================================
                
                // 💡 PATTERN 1: Simple Melee Attack (Current - uncomment to use)
                BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                
                // 💡 PATTERN 2: Random Special Attacks (uncomment to use)
                // when {
                //     // 10% chance for AoE explosion
                //     npc.world.chance(1, 10) -> {
                //         npc.forceChat("Feel my wrath!")
                //         BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)
                //     }
                //     // 15% chance for unblockable attack
                //     npc.world.chance(1, 7) -> {
                //         BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
                //     }
                //     // 20% chance for magic freeze
                //     npc.world.chance(1, 5) -> {
                //         BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979,
                //             onHit = { hit -> if (hit.landed) target.freeze(5) })
                //     }
                //     // 55% chance for normal melee (default)
                //     else -> {
                //         BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                //     }
                // }
                
                // 💡 PATTERN 3: HP-Based Enrage Mode (uncomment to use)
                // val isEnraged = npc.getCurrentHp() < (npc.getMaxHp() * 0.3)  // Below 30% HP
                // when {
                //     isEnraged && npc.world.chance(1, 2) -> {  // 50% chance when enraged
                //         npc.forceChat("THIS ISN'T OVER!")
                //         BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)
                //     }
                //     npc.world.chance(1, 4) -> {  // 25% chance normally
                //         BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
                //     }
                //     else -> {
                //         BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                //     }
                // }
                
                // 💡 PATTERN 4: Magic Boss (uncomment to use)
                // if (npc.world.chance(1, 3)) {  // 33% chance
                //     BossAttacks.magic(
                //         npc = npc,
                //         target = target,
                //         projectile = 368,
                //         maxHit = 30,
                //         anim = 1979,
                //         onHit = { hit ->
                //             if (hit.landed) {
                //                 target.freeze(5)  // Freeze for 5 ticks on hit
                //             }
                //         }
                //     )
                // } else {
                //     BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                // }
                
                // 💡 PATTERN 5: Ranged Boss (uncomment to use)
                // if (npc.world.chance(1, 3)) {  // 33% chance
                //     BossAttacks.ranged(
                //         npc = npc,
                //         target = target,
                //         projectile = 10,       // Arrow graphic
                //         maxHit = 25,
                //         anim = 426
                //     )
                // } else {
                //     BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                // }
                
                // 💡 PATTERN 6: Knockback Boss (uncomment to use)
                // if (npc.world.chance(1, 5)) {  // 20% chance
                //     BossAttacks.knockback(npc, target)
                // } else {
                //     BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                // }
                
                // 💡 PATTERN 7: Teleport Boss (uncomment to use)
                // if (npc.world.chance(1, 4)) {  // 25% chance
                //     npc.forceChat("Get over here!")
                //     BossAttacks.teleportTargetToNpc(npc, target)
                //     // Then attack them!
                //     BossAttacks.melee(npc, target, maxHit = 30, anim = 422)
                // } else {
                //     BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                // }
                
                // 💡 PATTERN 8: Mixed Melee/Ranged/Magic (uncomment to use)
                // when {
                //     npc.world.chance(1, 3) -> {  // 33% melee
                //         BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                //     }
                //     npc.world.chance(1, 2) -> {  // 33% ranged
                //         BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 426)
                //     }
                //     else -> {  // 33% magic
                //         BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979)
                //     }
                // }
                
                // 💡 MORE ATTACK EXAMPLES (uncomment individual lines to try):
                // BossAttacks.melee(npc, target, maxHit = 50, anim = 7060)  // Heavy melee
                // BossAttacks.melee(npc, target, maxHit = 15, anim = 422)   // Light melee
                // BossAttacks.ranged(npc, target, projectile = 11, maxHit = 30, anim = 426)  // Bow
                // BossAttacks.ranged(npc, target, projectile = 27, maxHit = 25, anim = 7552)  // Crossbow
                // BossAttacks.magic(npc, target, projectile = 100, maxHit = 35, anim = 1978)  // Single-target spell
                // BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979)  // Multi-target spell
                // BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)  // Explosion on player
                // BossAttacks.aoe(npc, npc.tile, radius = 3, maxHit = 50, projectile = 100)  // Explosion on boss
                // BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)  // Always hits
                // BossAttacks.knockback(npc, target)  // Pushes player away
                // BossAttacks.stun(target, cycles = 5)  // Stuns player for 5 ticks
                // BossAttacks.teleportTargetToNpc(npc, target)  // Teleports player to boss
                
                npc.postAttackLogic(target)
            }
            
            wait(1)  // 👉 ADJUST: Wait time between checks (1 = every tick, 2 = every 2 ticks)
            target = npc.getCombatTarget() as? Player ?: break
        }
        
        // Clean up when combat ends
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
```

### 🎯 Adding Special Attacks to Your Boss

Want your boss to do more than just basic melee attacks? You can add special attacks using the `BossAttacks` collection! Here's how to add them to your `combatLoop`:

#### Basic Special Attack Pattern

Replace the simple `BossAttacks.melee` line with this pattern:

```kotlin
// Decide which attack to use
if (npc.world.chance(1, 4)) {  // 25% chance (1 in 4)
    // Special attack!
    BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)
} else {
    // Normal attack (75% chance)
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
npc.postAttackLogic(target)
```

**How `chance()` works:**
- `npc.world.chance(1, 4)` = 25% chance (1 out of 4)
- `npc.world.chance(1, 3)` = 33% chance (1 out of 3)
- `npc.world.chance(1, 2)` = 50% chance (1 out of 2)
- Lower the second number = more frequent special attacks!

#### Special Attack Examples from BossAttacks

Here are some powerful special attacks you can use (see `docs/BOSS_ATTACKS_GUIDE.md` for the full collection):

**1. Area of Effect (AoE) - Hits everyone nearby:**
```kotlin
if (npc.world.chance(1, 5)) {  // 20% chance
    BossAttacks.aoe(
        npc = npc,
        center = target.tile,  // Explode on player
        radius = 3,            // 3-tile radius
        combatClass = CombatClass.MAGIC,
        maxHit = 50,
        projectile = 100       // Fireball graphic
    )
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

**2. Unblockable Attack - Always hits:**
```kotlin
if (npc.world.chance(1, 4)) {  // 25% chance
    BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

**3. Magic Attack with Freeze:**
```kotlin
if (npc.world.chance(1, 3)) {  // 33% chance
    BossAttacks.magic(
        npc = npc,
        target = target,
        projectile = 368,
        maxHit = 30,
        anim = 1979,
        onHit = { hit ->
            if (hit.landed) {
                target.freeze(5)  // Freeze for 5 ticks on hit
            }
        }
    )
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

**4. Ranged Attack:**
```kotlin
if (npc.world.chance(1, 3)) {  // 33% chance
    BossAttacks.ranged(
        npc = npc,
        target = target,
        projectile = 10,       // Arrow graphic
        maxHit = 25,
        anim = 426
    )
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

**5. Knockback Attack - Pushes player away:**
```kotlin
if (npc.world.chance(1, 5)) {  // 20% chance
    BossAttacks.knockback(npc, target)
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

**6. Teleport Player to Boss:**
```kotlin
if (npc.world.chance(1, 4)) {  // 25% chance
    npc.forceChat("Get over here!")
    BossAttacks.teleportTargetToNpc(npc, target)
    // Then attack them!
    BossAttacks.melee(npc, target, maxHit = 30, anim = 422)
} else {
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
}
```

#### Multiple Special Attacks

You can have multiple special attacks with different chances:

```kotlin
when {
    // 10% chance for AoE explosion
    npc.world.chance(1, 10) -> {
        npc.forceChat("Feel my wrath!")
        BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)
    }
    // 15% chance for unblockable attack
    npc.world.chance(1, 7) -> {
        BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
    }
    // 20% chance for magic freeze
    npc.world.chance(1, 5) -> {
        BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979,
            onHit = { hit -> if (hit.landed) target.freeze(5) })
    }
    // 55% chance for normal melee (default)
    else -> {
        BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
    }
}
npc.postAttackLogic(target)
```

#### HP-Based Special Attacks (Enrage Mode)

Make special attacks more frequent when the boss is low on health:

```kotlin
val isEnraged = npc.getCurrentHp() < (npc.getMaxHp() * 0.3)  // Below 30% HP

when {
    isEnraged && npc.world.chance(1, 2) -> {  // 50% chance when enraged
        npc.forceChat("THIS ISN'T OVER!")
        BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)
    }
    npc.world.chance(1, 4) -> {  // 25% chance normally
        BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
    }
    else -> {
        BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
    }
}
npc.postAttackLogic(target)
```

#### Quick Reference: All BossAttacks Functions

- `BossAttacks.melee()` - Basic melee attack
- `BossAttacks.ranged()` - Ranged attack with projectile
- `BossAttacks.magic()` - Magic attack with projectile
- `BossAttacks.aoe()` - Area of Effect (hits multiple players)
- `BossAttacks.unblockable()` - Always hits, ignores accuracy
- `BossAttacks.knockback()` - Pushes player away
- `BossAttacks.stun()` - Stuns the player
- `BossAttacks.teleportTargetToNpc()` - Teleports player to boss

**📖 For more details on each attack, see `docs/BOSS_ATTACKS_GUIDE.md`**
```

### 4️⃣ Adjust This! (Customize Your Boss)

Now that you have the file created, look for the parts with `👉 CHANGE` or `👉 ADJUST`.

Here is what you can change:

#### A. Where does it live? (Spawning)
Look for `spawnNpc`. Change the **X** and **Z** numbers to move your boss.
*   Type `::coords` in-game to find your spot!

#### B. How strong is it? (Stats)
Look for `stats`.
*   Want a super tank? Increase `hitpoints` and `defence`.
*   Want a glass cannon? Increase `strength` but lower `hitpoints`.

#### C. What does it drop? (Loot)
Look for `drops`.
*   You can change "item.coins" to any item name you want, like "item.partyhat"!
*   Use `min = X, max = Y, weight = Z` syntax for drops
*   **Weight** determines rarity: Higher weight = more common drop
*   **Table weight** (`main(weight = X)`) must be >= sum of all item weights!

#### D. How does it fight? (Combat)
Look for `combatLoop`.
*   You can change the `dice` numbers to make special attacks happen more often.
*   You can change `BossAttacks.melee` to `BossAttacks.ranged` if you want it to shoot arrows!

#### E. What animations does it use? (Visuals)
Look for `anims`. See the **Animation Guide** section below for detailed information!

#### F. What sounds does it make? (Audio)
Look for `sound`. See the **Sound Guide** section below for detailed information!

---

## 🎬 Animation Guide: Making Your Boss Look Cool!

Animations control how your boss **looks** when it fights. This section will teach you everything about animations!

### What Are Animations?

Animations are visual effects that play when your boss does certain actions:
- **`attack`**: Plays when the boss attacks (swings weapon, casts spell, etc.)
- **`block`**: Plays when the boss blocks or defends against an attack
- **`death`**: Plays when the boss dies

### Can I Use Any Animation ID? (Like `attack = 9999`?)

**Short answer:** Technically yes, but it might not look right!

- ✅ **Valid IDs**: Animation IDs that exist in the game client will play. The game won't crash.
- ⚠️ **Invalid IDs**: If you use an ID that doesn't exist (like `9999`), the boss might:
  - Not animate at all (stand still)
  - Play a default/fallback animation
  - Look weird or glitchy

**Best Practice:** Use animation IDs that you know exist. See the examples below!

### How to Find Animation IDs

Here are several ways to find the perfect animation for your boss:

#### Method 1: Use the In-Game Command (Easiest!)
1. In-game, type: `::anim [ID]` (e.g., `::anim 3353`)
2. Your character will play that animation
3. Try different IDs until you find one you like!

#### Method 2: Look at Existing Bosses
Check other boss files in the codebase:
- `game-plugins/src/main/kotlin/org/alter/plugins/content/bosses/`
- See what animations they use and copy them!

#### Method 3: Use Online Resources
- **OSRS Entity Viewer**: Search for NPCs and see their animations
- **OSRS Wiki**: Look up NPC pages to find animation information
- **Rune-Server Forums**: Community resources with animation lists

#### Method 4: Check the Animation Constants
The codebase has many animation IDs defined in:
- `game-api/src/main/kotlin/org/alter/api/cfg/Animation.kt`
- Look for constants like `CRAZY_ARCHAEOLOGIST_BOOK = 3353`

### Common Animation IDs by NPC Type

Here are some popular animation IDs you can use:

#### 🐉 Dragon-Type Animations
```kotlin
anims {
    attack = 81   // Dragonfire attack
    block = 89    // Dragon block/hit
    death = 92    // Dragon death
}
```

#### 👹 Demon-Type Animations
```kotlin
anims {
    attack = 64   // Demon attack
    block = 65    // Demon block/hit
    death = 67    // Demon death
}
```

#### 🧙 Magic User Animations
```kotlin
anims {
    attack = 1979 // Magic spell cast (ancient)
    block = 424   // Standard block
    death = 836   // Standard death
}
```

#### ⚔️ Melee Warrior Animations
```kotlin
anims {
    attack = 422  // Punch/melee attack
    block = 424   // Block with shield
    death = 836   // Death animation
}
```

#### 🏹 Ranged Animations
```kotlin
anims {
    attack = 426  // Bow attack
    block = 424   // Block
    death = 836   // Death
}
```

#### 🦎 Lizard/Reptile Animations
```kotlin
anims {
    attack = 2776 // Lizard attack
    block = 2777  // Lizard hit
    death = 2778  // Lizard death
}
```

#### 🕷️ Spider Animations
```kotlin
anims {
    attack = 5327 // Giant spider attack
    block = 5328  // Spider defend
    death = 5329  // Spider death
}
```

#### 👻 Ghost/Undead Animations
```kotlin
anims {
    attack = 5532 // Ghost attack
    block = 5533  // Ghost hit
    death = 5534  // Ghost death
}
```

### More Animation Examples

Here are additional animation IDs you can experiment with:

```kotlin
// Boss-Specific Animations
anims {
    attack = 3353  // Crazy Archaeologist book attack
    block = 424    // Standard block
    death = 836    // Standard death
}

// Alternative Attack Animations
attack = 451      // Different melee swing
attack = 7060     // Heavy/unblockable attack
attack = 1978     // Single-target magic cast
attack = 1979     // Multi-target magic cast
attack = 2652     // Ranged attack (TzTok-Jad style)
attack = 2656     // Magic attack (TzTok-Jad style)

// Alternative Block Animations
block = 1683      // Different block animation
block = 424       // Standard block (most common)

// Alternative Death Animations
death = 836       // Standard death (most common)
death = 1684       // Alternative death
death = 92         // Dragon death
```

### Using Different Animations for Different Attacks

You can override the default `attack` animation in your combat loop! This lets you use different animations for different attack types:

```kotlin
// In your combatLoop():
if (dice < 30) {
    // Special attack with custom animation
    BossAttacks.melee(npc, target, maxHit = 50, anim = 7060) // Heavy attack!
} else {
    // Normal attack with different animation
    BossAttacks.melee(npc, target, maxHit = 25, anim = 422) // Quick punch!
}
```

### Animation Tips & Tricks

1. **Match the NPC Type**: If your boss is a dragon, use dragon animations. If it's a mage, use magic animations.

2. **Test in Game**: Always test your animations in-game using `::anim [ID]` to see how they look!

3. **Mix and Match**: You can use a dragon attack animation with a demon death animation - be creative!

4. **Special Attacks**: Use unique animations for special attacks to make them stand out:
   ```kotlin
   // Normal attack
   BossAttacks.melee(npc, target, anim = 422)
   
   // Special attack with dramatic animation
   BossAttacks.melee(npc, target, maxHit = 99, anim = 7060)
   npc.forceChat("ULTIMATE STRIKE!")
   ```

5. **Death Animations**: You can even have multiple death animations! (See advanced section below)

### Advanced: Multiple Death Animations

You can make your boss randomly choose from multiple death animations:

```kotlin
anims {
    attack = 3353
    block = 424
    death {  // Use a block instead of single value
        add(836)   // Death animation 1
        add(1684)  // Death animation 2
        add(92)    // Death animation 3
    }
}
```

The game will randomly pick one when the boss dies!

### Quick Reference: Animation ID Cheat Sheet

| Animation Type | Common IDs | Description |
|---------------|------------|-------------|
| **Attack - Melee** | `422`, `423`, `451`, `64`, `81` | Punches, swings, claws |
| **Attack - Magic** | `1978`, `1979`, `2656`, `3353` | Spell casting |
| **Attack - Ranged** | `426`, `2652`, `7552` | Bow, crossbow, thrown |
| **Attack - Special** | `7060`, `5069` | Heavy/unblockable attacks |
| **Block** | `424`, `1683` | Defending/blocking |
| **Death** | `836`, `1684`, `92`, `67` | Death animations |

### Testing Your Animations

**Before you finalize your boss, test the animations!**

1. **In-Game Testing:**
   ```
   ::anim 3353  // Test attack animation
   ::anim 424   // Test block animation
   ::anim 836   // Test death animation
   ```

2. **Spawn Your Boss:**
   - Spawn the boss in-game
   - Attack it to see the block animation
   - Watch it attack to see the attack animation
   - Kill it to see the death animation

3. **Quick Iteration:**
   - Change animation IDs in your code
   - Restart the server (or use hot-reload if available)
   - Test again until it looks perfect!

### Common Mistakes to Avoid

❌ **Don't:** Use random high numbers like `9999` without testing
✅ **Do:** Test animations with `::anim` command first

❌ **Don't:** Use player animations for NPCs (they might look weird)
✅ **Do:** Use NPC-specific animations when possible

❌ **Don't:** Forget to set all three animations (attack, block, death)
✅ **Do:** Set all three for a complete boss experience

❌ **Don't:** Use the same animation for everything
✅ **Do:** Mix different animations for variety (especially in combat loop)

---

## 🔊 Sound Guide: Making Your Boss Sound Epic!

Sounds add atmosphere and make your boss feel more alive! This section will teach you everything about boss sounds.

### 🎯 Quick Start: Testing Sounds

**The easiest way to find sounds is to test them in-game!**

1. **Use the `::sound` command** (requires developer privileges):
   ```
   ::sound <sound_id>
   ```
   Example: `::sound 718` will play that sound immediately!

2. **Try different IDs** until you find sounds you like:
   ```
   ::sound 718   // Rock attack
   ::sound 720   // Rock hit
   ::sound 719   // Rock death
   ::sound 448   // Giant attack
   ::sound 474   // Golem attack
   ```

3. **Then use the IDs in your boss code!**

### What Are Sounds?

Sounds are audio effects that play when your boss performs actions:
- **`attackSound`**: Plays when the boss attacks
- **`blockSound`**: Plays when the boss blocks or gets hit
- **`deathSound`**: Plays when the boss dies

### Basic Sound Configuration

Here's the basic structure for adding sounds to your boss:

```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK  // Sound ID when attacking
    attackArea = true                      // Can nearby players hear it?
    attackVolume = 50                     // How loud (0-100)
    attackRadius = 10                     // How far sound travels (in tiles)
    
    blockSound = Sound.ROCK_CRAB_HIT      // Sound when blocking/hit
    blockArea = true
    blockVolume = 40
    blockRadius = 8
    
    deathSound = Sound.ROCK_CRAB_DEATH    // Sound when dying
    deathArea = true
    deathVolume = 60
    deathRadius = 12
}
```

### Sound Parameters Explained

#### `attackSound`, `blockSound`, `deathSound`
- **What it does:** The sound ID to play
- **How to find:** Use constants from `Sound` object (see examples below)
- **Example:** `Sound.ROCK_CRAB_ATTACK`, `Sound.GIANT_ATTACK`

#### `attackArea`, `blockArea`, `deathArea`
- **What it does:** If `true`, all players nearby can hear the sound
- **If `false`:** Only the player being attacked hears it
- **Best for bosses:** Usually set to `true` for epic atmosphere!

#### `attackVolume`, `blockVolume`, `deathVolume`
- **What it does:** How loud the sound is (0 = silent, 100 = max volume)
- **Recommended:** 
  - Attack: 40-60 (loud enough to be heard)
  - Block: 30-50 (softer, less intrusive)
  - Death: 50-70 (dramatic!)

#### `attackRadius`, `blockRadius`, `deathRadius`
- **What it does:** How many tiles away players can hear the sound
- **Only works if:** `attackArea = true` (or `blockArea`/`deathArea`)
- **Recommended:**
  - Attack: 8-12 tiles
  - Block: 6-10 tiles
  - Death: 10-15 tiles (everyone should hear the boss die!)

### Common Sound IDs by NPC Type

Here are popular sound constants you can use:

#### 🪨 Rock/Stone Bosses
```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK    // 718
    blockSound = Sound.ROCK_CRAB_HIT        // 720
    deathSound = Sound.ROCK_CRAB_DEATH     // 719
}
```

**Alternative Rock Sounds:**
```kotlin
attackSound = Sound.ROCKSLUG_ATTACK       // 729
blockSound = Sound.ROCKSLUG_HIT           // 731
deathSound = Sound.ROCKSLUG_DEATH         // 730
```

#### 👹 Giant Bosses
```kotlin
sound {
    attackSound = Sound.GIANT_ATTACK       // 448
    blockSound = Sound.GIANT_HIT            // 451
    deathSound = Sound.GIANT_DEATH         // 450
}
```

**Giant Variants:**
```kotlin
attackSound = Sound.FIRE_GIANT_ATTACK     // 447
attackSound = Sound.EARTH_GIANT_ATTACK    // 446
attackSound = Sound.MOSS_GIANT_ATTACK     // 449
```

#### 🤖 Golem Bosses
```kotlin
sound {
    attackSound = Sound.GOLEM_ATTACK        // 474
    blockSound = Sound.GOLEM_HIT             // 476
    deathSound = Sound.GOLEM_DEATH          // 475
}
```

#### 🐉 Dragon Bosses
```kotlin
sound {
    attackSound = Sound.BABYDRAGON_ATTACK   // Dragon attack sound
    blockSound = Sound.BABYDRAGON_HIT       // Dragon hit sound
    deathSound = Sound.BABYDRAGON_DEATH     // Dragon death sound
}
```

#### 👻 Undead/Skeleton Bosses
```kotlin
sound {
    attackSound = Sound.SKELETAL_HELLHOUND_ATTACK  // Powerful skeletal sound
    blockSound = Sound.SKELETAL_HELLHOUND_HIT
    deathSound = Sound.SKELETAL_HELLHOUND_DEATH
}
```

#### 🕷️ Spider Bosses
```kotlin
sound {
    attackSound = Sound.GIANT_SPIDER_ATTACK
    blockSound = Sound.GIANT_SPIDER_HIT
    deathSound = Sound.GIANT_SPIDER_DEATH
}
```

#### 🦎 Lizard/Reptile Bosses
```kotlin
sound {
    attackSound = Sound.LIZARD_ATTACK
    blockSound = Sound.LIZARD_HIT
    deathSound = Sound.LIZARD_DEATH
}
```

### How to Find Sound IDs

#### Method 1: Test Sounds In-Game (Easiest!)
**The best way to find sounds is to test them directly in-game!**

1. **Use the `::sound` command** (requires developer privileges):
   ```
   ::sound <sound_id>
   ```
   Example: `::sound 2582` will play that sound so you can hear it!

2. **Try different IDs** until you find sounds you like:
   - Start with IDs from the `Sound.kt` file (see Method 2)
   - Test them in-game to see how they sound
   - Adjust volume and radius in your boss code

3. **Quick Testing Workflow**:
   ```
   ::sound 718   // Test rock attack sound
   ::sound 720   // Test rock hit sound
   ::sound 719   // Test rock death sound
   ```
   Then use the ones that sound good in your boss code!

#### Method 2: Check the Sound Constants
Look in: `game-api/src/main/kotlin/org/alter/api/cfg/Sound.kt`

Search for sounds related to your boss type:
- Rock boss? Search for "ROCK" or "STONE"
- Dragon boss? Search for "DRAGON"
- Giant boss? Search for "GIANT"

**Example constants you'll find:**
```kotlin
Sound.ROCK_CRAB_ATTACK = 718
Sound.ROCK_CRAB_HIT = 720
Sound.ROCK_CRAB_DEATH = 719
Sound.DRAGON_ATTACK = <id>
Sound.GIANT_ATTACK = 448
```

#### Method 3: Look at Existing Bosses
Check other boss files to see what sounds they use:
- `game-plugins/src/main/kotlin/org/alter/plugins/content/bosses/`
- `game-plugins/src/main/kotlin/org/alter/plugins/content/npcs/`

**Example from MiningBoss.kt:**
```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK  // 718
    blockSound = Sound.ROCK_CRAB_HIT      // 720
    deathSound = Sound.ROCK_CRAB_DEATH   // 719
}
```

#### Method 4: Use Generic Combat Sounds
If you can't find a specific sound, use generic combat sounds:
```kotlin
// Generic melee sounds
attackSound = Sound.SWORDCLASH4        // 21
blockSound = Sound.STEEL_MAIL         // 14

// Impact sounds
blockSound = Sound.ROCK_IMPACT         // 851
blockSound = Sound.STONE_IMPACT        // 860
```

#### Method 5: Use Direct Sound IDs
You can also use raw sound IDs directly (test with `::sound` first!):
```kotlin
sound {
    attackSound = 718   // Direct ID (test with ::sound 718 first!)
    blockSound = 720
    deathSound = 719
}
```

### Sound Tips & Tricks

1. **Match the Theme:** If your boss is a rock, use rock sounds. If it's a dragon, use dragon sounds!

2. **Volume Balance:**
   - Death sounds should be louder (50-70) - it's dramatic!
   - Attack sounds medium (40-60) - noticeable but not annoying
   - Block sounds softer (30-50) - happens frequently

3. **Area Sounds for Bosses:**
   - Set `attackArea = true` for epic boss fights
   - Makes the boss feel more powerful and immersive
   - Players nearby will hear the boss even if they're not fighting it

4. **Radius Matters:**
   - Small radius (5-8): Intimate, close-range sounds
   - Medium radius (8-12): Standard boss sounds
   - Large radius (12-20): Epic, world-shaking boss sounds

5. **Mix and Match:**
   - You can use different sound types for variety
   - Example: Rock attack sound + Giant death sound = Unique boss!

### Optional: No Sounds

If you don't want sounds, you can simply **omit the `sound { }` block entirely**. The boss will work fine without sounds, just silently.

### Quick Reference: Sound Cheat Sheet

| Sound Type | Common Constants | Description |
|------------|------------------|-------------|
| **Rock** | `ROCK_CRAB_ATTACK`, `ROCKSLUG_ATTACK` | Rock/stone themed |
| **Giant** | `GIANT_ATTACK`, `FIRE_GIANT_ATTACK` | Large creature sounds |
| **Golem** | `GOLEM_ATTACK`, `GOLEM_HIT` | Mechanical/stone golem |
| **Dragon** | `BABYDRAGON_ATTACK` | Dragon-themed |
| **Undead** | `SKELETAL_HELLHOUND_ATTACK` | Skeleton/undead |
| **Spider** | `GIANT_SPIDER_ATTACK` | Spider-themed |
| **Generic** | `SWORDCLASH4`, `STEEL_MAIL` | Generic combat sounds |

### Testing Your Sounds

#### Step 1: Test Individual Sounds
Before adding sounds to your boss, test them individually:

1. **In-game, use the `::sound` command**:
   ```
   ::sound 718   // Test attack sound
   ::sound 720   // Test block sound
   ::sound 719   // Test death sound
   ```

2. **Try different IDs** until you find sounds that fit your boss:
   ```
   ::sound 448   // Try giant attack
   ::sound 450   // Try giant death
   ::sound 474   // Try golem attack
   ```

3. **Note which IDs sound good** and use them in your code!

#### Step 2: Test Sounds on Your Boss
After adding sounds to your boss code:

1. **Spawn your boss in-game**
2. **Attack it** - Listen for the attack sound
3. **Let it hit you** - Listen for the block sound
4. **Kill it** - Listen for the death sound
5. **Check if nearby players can hear it** (if `attackArea = true`)
6. **Adjust volume/radius** if needed!

#### Step 3: Fine-Tune Settings
If sounds are too loud/quiet or don't travel far enough:

```kotlin
sound {
    attackSound = Sound.ROCK_CRAB_ATTACK
    attackVolume = 30    // 👉 ADJUST: Lower if too loud
    attackRadius = 5     // 👉 ADJUST: Smaller if too far
    // ... test and adjust until perfect!
}
```

### Common Sound Mistakes to Avoid

❌ **Don't:** Set volume to 100 (too loud, annoying!)
✅ **Do:** Use 40-70 range for most sounds

❌ **Don't:** Forget to set `attackArea = true` if you want area sounds
✅ **Do:** Set area to `true` for epic boss atmosphere

❌ **Don't:** Use player sounds for NPCs (might sound weird)
✅ **Do:** Use NPC-specific sounds when possible

❌ **Don't:** Make all sounds the same volume
✅ **Do:** Death should be loudest, block should be softest

### 💥 Damage: Fixed or Stat-Based?
You have two choices for how hard your boss hits:

1.  **Fixed Max Hit (Easiest)**:
    *   Example: `BossAttacks.melee(npc, target, maxHit = 50)`
    *   The boss will hit between 0 and 50. It ignores its strength level!
    *   *Good for:* Predictable bosses.

2.  **Stat-Based Damage (Advanced)**:
    *   Example: `BossAttacks.melee(npc, target)` (Delete `maxHit`)
    *   The game calculates damage using the boss's `strength` stat!
    *   High Strength + Low Attack = Inaccurate but deadly (Glass Cannon).
    *   Low Strength + High Attack = Hits often but weak (Chip Damage).
    *   *Good for:* Complex bosses where stats matter.

---

## 📚 More Combat Examples

If you want to try different attacks, here are some options you can swap into your `combatLoop`:

### 🗡️ Melee Attack
*The boss swings its weapon or punches.*

**Basic Melee:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 20, anim = 422)  // Quick punch
```

**Heavy Melee:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 50, anim = 7060)  // Powerful strike
```

**Alternative Melee Animations:**
```kotlin
BossAttacks.melee(npc, target, maxHit = 30, anim = 451)   // Sword swing
BossAttacks.melee(npc, target, maxHit = 30, anim = 423)   // Aggressive punch
BossAttacks.melee(npc, target, maxHit = 30, anim = 64)    // Demon claw
BossAttacks.melee(npc, target, maxHit = 30, anim = 81)    // Dragon claw
```

### ✨ Magic Attack
*The boss casts a spell with a projectile.*

**Basic Magic:**
```kotlin
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)
```

**Alternative Magic Animations:**
```kotlin
// Single-target spell
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1978)

// Multi-target spell (ancient)
BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)

// Different magic style
BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 2656)
```

**Magic with Freeze:**
```kotlin
BossAttacks.magic(npc, target, projectile = 368, maxHit = 30, anim = 1979)
target.freeze(5)  // Freeze player for 5 ticks
```

### 🏹 Ranged Attack
*The boss shoots an arrow or throws a rock.*

**Basic Ranged:**
```kotlin
BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 426)
```

**Alternative Ranged Animations:**
```kotlin
// Bow attack
BossAttacks.ranged(npc, target, projectile = 11, maxHit = 25, anim = 426)

// Crossbow attack
BossAttacks.ranged(npc, target, projectile = 27, maxHit = 25, anim = 7552)

// Thrown weapon
BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 2652)
```

### 💥 Super Area Attack (AoE)
*Hits EVERYONE nearby! Great for ultimate moves.*

**Basic AoE:**
```kotlin
BossAttacks.aoe(
    npc = npc,
    center = target.tile, // Explode on the player!
    radius = 3,           // 3x3 Explosion
    combatClass = CombatClass.MAGIC,
    maxHit = 50,
    projectile = 100      // Fireball graphic
)
```

**AoE Variations:**
```kotlin
// Small explosion (1x1)
BossAttacks.aoe(npc, target.tile, radius = 1, maxHit = 30, projectile = 100)

// Medium explosion (3x3)
BossAttacks.aoe(npc, target.tile, radius = 3, maxHit = 50, projectile = 100)

// Huge explosion (5x5)
BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)

// Explode on boss location instead of player
BossAttacks.aoe(npc, npc.tile, radius = 3, maxHit = 50, projectile = 100)
```

### 💀 Unblockable Attack
*Always hits, ignores accuracy checks!*

```kotlin
BossAttacks.unblockable(npc, target, damage = 40, anim = 7060)
```

**When to use:** Ultimate attacks, environmental damage, mechanics that must hit.

---

## 🧠 Advanced Logic (If Statements)

You can use `if` statements to make your boss smarter!

### ❤️ Last Stand (Low HP Enrage)
*Checking if HP is under 10%*
```kotlin
if (npc.getCurrentHp() < (npc.getMaxHp() * 0.10)) {
    npc.forceChat("THIS ISN'T OVER!")
    // Use a super strong attack!
    BossAttacks.aoe(npc, target.tile, radius = 5, maxHit = 99, projectile = 100)
}
```

### 📏 Distance Check (Too Far?)
*Checking how far away the player is*
```kotlin
if (npc.tile.getDistance(target.tile) > 5) {
    npc.forceChat("Get over here!")
    // Teleport player to boss!
    target.moveTo(npc.tile) 
}
```

### 🛡️ Equipment Check (Holding a Shield?)
*Checking if player has a specific item*
```kotlin
if (target.equipment[Equipment.SHIELD_SLOT].hasAny()) {
    npc.forceChat("Your shield won't save you!")
    // Use magic (goes through armor!)
    BossAttacks.magic(npc, target, projectile = 100, maxHit = 40)
}

---

## 🔮 Bonus: Prayer Smasher!

Make your boss smart! If the player tries to protect themselves, punish them!

### 1. Add these imports:
```kotlin
import org.alter.game.model.combat.Prayer
import org.alter.game.model.combat.Prayers
```

### 2. Add this code inside your `combatLoop`:
*Checking what prayers the player is using.*

```kotlin
            // 🚫 PRAYER CHECK: Is player using Protect from Melee?
            if (Prayers.isActive(target, Prayer.PROTECT_FROM_MELEE)) {
                npc.forceChat("Your prayers are useless!")
                
                // Switch to MAGIC attack to hit through their prayer!
                BossAttacks.magic(npc, target, projectile = 100, maxHit = 35, anim = 1979)
            }
```

---

## 🛡️ Making Your Boss Use Protection Prayers

Want to make your boss more challenging? You can make it use protection prayers to reduce damage from specific attack styles! The boss can protect from **1 or 2 attack styles** at a time.

### How Protection Prayers Work for NPCs

When an NPC has a protection prayer active:
- **Protect from Melee**: Reduces melee damage by 60% (only 40% gets through)
- **Protect from Magic**: Completely blocks magic damage (100% protection)
- **Protect from Missiles (Ranged)**: Reduces ranged damage by 60% (only 40% gets through)

**Important:** NPCs can only **display one prayer icon** at a time, but they can have **two prayers active** simultaneously (the combat system checks both internally).

### 1️⃣ Single Protection Prayer (Easiest!)

Make your boss protect from one attack style:

#### Add this import:
```kotlin
import org.alter.api.PrayerIcon
```

#### Add this code inside your `combatLoop`:
```kotlin
suspend fun QueueTask.combatLoop() {
    val npc = ctx as Npc
    var target = npc.getCombatTarget() as? Player ?: return

    while (npc.canEngageCombat(target)) {
        npc.facePawn(target)
        
        // 🛡️ PROTECT FROM MELEE: Boss reduces melee damage by 60%
        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id
        
        // Your attack code here
        if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && 
            npc.isAttackDelayReady()) {
            BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
            npc.postAttackLogic(target)
        }
        
        wait(1)
        target = npc.getCombatTarget() as? Player ?: break
    }
    
    // Clear prayer icon when combat ends
    npc.prayerIcon = -1
    npc.resetFacePawn()
    npc.removeCombatTarget()
}
```

#### Other Single Prayer Options:
```kotlin
// Protect from Magic (blocks all magic damage)
npc.prayerIcon = PrayerIcon.PROTECT_FROM_MAGIC.id

// Protect from Missiles/Ranged (reduces ranged damage by 60%)
npc.prayerIcon = PrayerIcon.PROTECT_FROM_MISSILES.id
```

### 2️⃣ Two Protection Prayers (Advanced!)

Make your boss protect from **two attack styles** at once! This is more complex but makes the boss much harder.

#### Add these imports:
```kotlin
import org.alter.api.PrayerIcon
import org.alter.game.model.attr.AttributeKey
```

#### Add this code to your plugin class (outside the `init` block):
```kotlin
companion object {
    // Store which two prayers are active
    private val PRAYER_1_ATTR = AttributeKey<Int>()  // First prayer (0=Magic, 1=Missiles, 2=Melee)
    private val PRAYER_2_ATTR = AttributeKey<Int>()  // Second prayer (0=Magic, 1=Missiles, 2=Melee)
}
```

#### Add this code inside your `combatLoop`:
```kotlin
suspend fun QueueTask.combatLoop() {
    val npc = ctx as Npc
    var target = npc.getCombatTarget() as? Player ?: return

    // Initialize prayers if not set (e.g., Protect from Melee + Missiles)
    if (!npc.attr.has(PRAYER_1_ATTR)) {
        npc.attr[PRAYER_1_ATTR] = 2  // Protect from Melee
        npc.attr[PRAYER_2_ATTR] = 1  // Protect from Missiles
        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id  // Display first prayer
    }

    while (npc.canEngageCombat(target)) {
        npc.facePawn(target)
        
        // Get the two active prayers
        val prayer1 = npc.attr[PRAYER_1_ATTR] ?: 2
        val prayer2 = npc.attr[PRAYER_2_ATTR] ?: 1
        
        // Check which attack types are blocked
        // 0 = Magic, 1 = Missiles (Ranged), 2 = Melee
        val hasMagicPrayer = prayer1 == 0 || prayer2 == 0
        val hasMissilesPrayer = prayer1 == 1 || prayer2 == 1
        val hasMeleePrayer = prayer1 == 2 || prayer2 == 2
        
        // Update the displayed prayer icon (show the first one)
        npc.prayerIcon = when (prayer1) {
            0 -> PrayerIcon.PROTECT_FROM_MAGIC.id
            1 -> PrayerIcon.PROTECT_FROM_MISSILES.id
            2 -> PrayerIcon.PROTECT_FROM_MELEE.id
            else -> -1
        }
        
        // Determine which attack type the boss can use
        // (The one that's NOT blocked by prayers)
        val canUseMagic = !hasMagicPrayer
        val canUseRanged = !hasMissilesPrayer
        val canUseMelee = !hasMeleePrayer
        
        // Attack with the allowed style
        if (npc.isAttackDelayReady()) {
            when {
                canUseMagic -> {
                    // Use magic attack
                    BossAttacks.magic(npc, target, projectile = 100, maxHit = 30, anim = 1979)
                    npc.postAttackLogic(target)
                }
                canUseRanged -> {
                    // Use ranged attack
                    BossAttacks.ranged(npc, target, projectile = 10, maxHit = 25, anim = 426)
                    npc.postAttackLogic(target)
                }
                canUseMelee -> {
                    // Use melee attack
                    if (npc.moveToAttackRange(this, target, distance = 1, projectile = false)) {
                        BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                        npc.postAttackLogic(target)
                    }
                }
            }
        }
        
        wait(1)
        target = npc.getCombatTarget() as? Player ?: break
    }
    
    // Clear prayer icon when combat ends
    npc.prayerIcon = -1
    npc.resetFacePawn()
    npc.removeCombatTarget()
}
```

### 🔄 Switching Prayers (Dynamic Prayer Changes)

Make your boss switch prayers based on conditions (e.g., every 50 damage taken, or at certain HP thresholds):

```kotlin
suspend fun QueueTask.combatLoop() {
    val npc = ctx as Npc
    var target = npc.getCombatTarget() as? Player ?: return
    
    // Track damage taken
    var damageTaken = 0
    var lastHp = npc.getMaxHp()
    
    while (npc.canEngageCombat(target)) {
        npc.facePawn(target)
        
        // Check if we've taken enough damage to switch prayers
        val currentHp = npc.getCurrentHp()
        val damageThisTick = lastHp - currentHp
        if (damageThisTick > 0) {
            damageTaken += damageThisTick
            lastHp = currentHp
            
            // Switch prayers every 50 damage
            if (damageTaken >= 50) {
                switchPrayers(npc)
                damageTaken = 0  // Reset counter
            }
        }
        
        // Set prayer icon based on current prayers
        val prayer1 = npc.attr[PRAYER_1_ATTR] ?: 2
        npc.prayerIcon = when (prayer1) {
            0 -> PrayerIcon.PROTECT_FROM_MAGIC.id
            1 -> PrayerIcon.PROTECT_FROM_MISSILES.id
            2 -> PrayerIcon.PROTECT_FROM_MELEE.id
            else -> -1
        }
        
        // Your attack code here...
        
        wait(1)
        target = npc.getCombatTarget() as? Player ?: break
    }
    
    npc.prayerIcon = -1
    npc.resetFacePawn()
    npc.removeCombatTarget()
}

// Helper function to cycle through prayer pairs
private fun switchPrayers(npc: Npc) {
    val current1 = npc.attr[PRAYER_1_ATTR] ?: 2
    val current2 = npc.attr[PRAYER_2_ATTR] ?: 1
    
    // Cycle: (Melee+Missiles) -> (Magic+Melee) -> (Missiles+Magic) -> repeat
    val (next1, next2) = when {
        current1 == 2 && current2 == 1 -> Pair(0, 2)  // Melee+Missiles -> Magic+Melee
        current1 == 0 && current2 == 2 -> Pair(1, 0)  // Magic+Melee -> Missiles+Magic
        current1 == 1 && current2 == 0 -> Pair(2, 1)  // Missiles+Magic -> Melee+Missiles
        else -> Pair(2, 1)  // Default
    }
    
    npc.attr[PRAYER_1_ATTR] = next1
    npc.attr[PRAYER_2_ATTR] = next2
    
    npc.forceChat("The boss switches protection prayers!")
}
```

### 📋 Prayer Icon Reference

| Prayer Icon | ID | Protection Against |
|------------|----|-------------------|
| `PrayerIcon.PROTECT_FROM_MELEE` | 0 | Melee attacks (60% reduction) |
| `PrayerIcon.PROTECT_FROM_MISSILES` | 1 | Ranged attacks (60% reduction) |
| `PrayerIcon.PROTECT_FROM_MAGIC` | 2 | Magic attacks (100% block) |
| `PrayerIcon.NONE` | -1 | No protection (clear prayers) |

### 💡 Tips & Tricks

1. **Display Only One Icon**: NPCs can only show one prayer icon at a time, but the combat system checks both prayers internally when you use attributes.

2. **Always Clear on Combat End**: Always set `npc.prayerIcon = -1` when combat ends to remove the prayer icon.

3. **Prayer Switching Sounds**: You can add a sound when switching prayers:
   ```kotlin
   import org.alter.api.cfg.Sound
   import org.alter.game.model.entity.AreaSound
   
   npc.world.spawn(AreaSound(npc.tile, id = Sound.ALTAR_PRAY, radius = 10, volume = 5))
   ```

4. **HP-Based Prayer Switching**: Switch prayers at certain HP thresholds:
   ```kotlin
   val hpPercent = npc.getCurrentHp().toDouble() / npc.getMaxHp()
   if (hpPercent < 0.5) {  // Below 50% HP
       // Switch to different prayers
       switchPrayers(npc)
   }
   ```

5. **Prevent Certain Attack Types**: When your boss has a protection prayer active, it typically shouldn't use that attack type (it's protected from it, so it makes sense to use a different style).

### ⚠️ Important Notes

- **Prayer Icon Display**: Only one prayer icon can be displayed at a time. If you have two prayers active, display the first one using `npc.prayerIcon`.

- **Combat Formula Integration**: The combat formulas automatically check `npc.prayerIcon` to reduce damage. Make sure to set it correctly!

- **Clearing Prayers**: Always clear the prayer icon (`npc.prayerIcon = -1`) when combat ends or the boss dies.

---

## 🎁 Adding Special Functionality to Your Boss

Want to make your boss unique? You can add special rewards, messages, or mechanics that trigger when the boss dies or during combat!

### 🪨 Example: Mining XP Reward

The Mining Boss gives Mining experience when killed, making it perfect for players who want to train Mining while fighting!

**Add this code to your boss plugin:**

```kotlin
// ==========================================
// 4. SPECIAL FUNCTIONALITY 🎁
// ==========================================
// 🪨 MINING XP REWARD: Players get Mining XP when they kill the boss!
onNpcDeath("npc.rock_925") {  // 👉 CHANGE to your NPC ID
    val npc = this.npc
    // Find the player who dealt the most damage (the killer)
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Award mining XP based on boss HP (scaled reward)
    val miningXp = npc.combatDef.hitpoints * 10.0 // 10 XP per HP (100 HP = 1000 XP)
    killer.addXp(Skills.MINING, miningXp)
    killer.message("<col=00ff00>You gain ${miningXp.toInt()} Mining experience for defeating the Rock!</col>")
}
```

**What this does:**
- When the boss dies, it finds the player who killed it
- Awards Mining XP based on the boss's HP (bigger boss = more XP!)
- Shows a colored message to the player

**You'll need this import:**
```kotlin
import org.alter.api.Skills
```

### 🎯 More Special Functionality Ideas

Here are other unique features you can add to your boss:

#### 💰 Bonus Coins on Kill
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Give bonus coins
    val bonusCoins = 50000
    killer.inventory.add("item.coins_995", bonusCoins)
    killer.message("You receive $bonusCoins coins as a bonus reward!")
}
```

#### 🏆 Kill Count Tracking
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Track how many times this player has killed the boss
    val currentKills = killer.attr.getOrDefault("your_boss_kills", 0)
    killer.attr["your_boss_kills"] = currentKills + 1
    killer.message("You have killed this boss ${currentKills + 1} times!")
}
```

#### ⚡ Special Item Drop (Guaranteed)
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Always drop a special item
    val specialItem = GroundItem(
        item = getRSCM("item.your_special_item"),
        amount = 1,
        tile = npc.tile,
        owner = killer
    )
    npc.world.spawn(specialItem)
    killer.message("The boss dropped a special item!")
}
```

#### 🎓 Skill XP in Multiple Skills
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Award XP in multiple skills
    killer.addXp(Skills.ATTACK, 500.0)
    killer.addXp(Skills.STRENGTH, 500.0)
    killer.addXp(Skills.DEFENCE, 500.0)
    killer.addXp(Skills.MINING, 250.0)  // Bonus mining XP!
    killer.message("You gain experience in multiple skills!")
}
```

#### 📢 Broadcast Message (Everyone Sees It!)
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // Broadcast to all players in the world
    npc.world.players.forEach { player ->
        player.message("<col=ff0000>${killer.username} has defeated the ${npc.def.name}!</col>")
    }
}
```

#### 🎲 Random Chance for Special Reward
```kotlin
onNpcDeath("npc.your_boss") {
    val npc = this.npc
    val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
    
    // 10% chance for a super rare drop
    if (npc.world.random(10) == 0) {  // 1 in 10 chance
        val rareItem = GroundItem(
            item = getRSCM("item.ultra_rare_item"),
            amount = 1,
            tile = npc.tile,
            owner = killer
        )
        npc.world.spawn(rareItem)
        
        // Broadcast to everyone!
        npc.world.players.forEach { player ->
            player.message("<col=ff00ff>${killer.username} received an ULTRA RARE drop from ${npc.def.name}!</col>")
        }
    }
}
```

### 💡 Tips for Adding Special Features

1. **Place it in the `init` block**: Add your `onNpcDeath` handler alongside your other boss setup code.

2. **Use the killer check**: Always check if `killer` is a `Player` before giving rewards, or your code might crash!

3. **Scale rewards**: Make rewards scale with boss difficulty (HP, combat level, etc.) so harder bosses give better rewards.

4. **Test thoroughly**: Make sure your special functionality works correctly and doesn't break the game!

5. **Be creative**: Think about what makes your boss unique. A fire boss could give Firemaking XP, a fishing boss could give Fishing XP, etc.!

### 📝 Quick Reference: Available Skills

You can award XP in any of these skills:
- `Skills.ATTACK`, `Skills.STRENGTH`, `Skills.DEFENCE`, `Skills.HITPOINTS`
- `Skills.MAGIC`, `Skills.RANGED`, `Skills.PRAYER`
- `Skills.MINING`, `Skills.WOODCUTTING`, `Skills.FISHING`
- `Skills.COOKING`, `Skills.SMITHING`, `Skills.HERBLORE`
- `Skills.CRAFTING`, `Skills.FLETCHING`, `Skills.CONSTRUCTION`
- `Skills.AGILITY`, `Skills.THIEVING`, `Skills.SLAYER`
- `Skills.FARMING`, `Skills.RUNECRAFT`, `Skills.HUNTER`

**Example:**
```kotlin
killer.addXp(Skills.MINING, 1000.0)  // 1000 Mining XP
killer.addXp(Skills.SMITHING, 500.0)  // 500 Smithing XP
```

---

## 📦 Drop Table Guide: Understanding How Drops Work

This section explains the drop table system in detail so you can create perfect drop tables for your boss!

### Drop Table Syntax

The correct syntax for drop tables is:

```kotlin
drops {
    always {
        add("item.bones", 1)  // Always drops this item
    }
    
    main(weight = 128) {  // Table weight (MUST be >= sum of item weights!)
        add("item.coins", min = 5000, max = 10000, weight = 20)
        add("item.dragon_scimitar", min = 1, max = 1, weight = 5)
    }
}
```

### Key Parameters Explained

#### `main(weight = X)`
- **What it does:** Sets the total weight of the drop table
- **CRITICAL RULE:** Must be **>= sum of all item weights** or server will crash!
- **How it works:**
  - If `weight = sum of item weights`: Guaranteed drop (100% chance)
  - If `weight > sum of item weights`: Less than 100% chance to get a drop
  - Example: Items total 79, `main(weight = 79)` = 100% drop chance
  - Example: Items total 79, `main(weight = 128)` = ~62% drop chance (79/128)

#### `add("item.name", min = X, max = Y, weight = Z)`
- **`min`**: Minimum quantity to drop
- **`max`**: Maximum quantity to drop
- **`weight`**: Drop probability (relative to other items)
  - Higher weight = more common drop
  - Weight 20 is twice as likely as weight 10
  - Weight determines probability **within the table**, not overall drop chance

### How Drop Probability Works

**Example Drop Table:**
```kotlin
main(weight = 100) {
    add("item.coins", min = 1000, max = 5000, weight = 50)      // 50% of drops
    add("item.shark", min = 1, max = 5, weight = 30)            // 30% of drops
    add("item.dragon_scimitar", min = 1, max = 1, weight = 20)  // 20% of drops
}
// Total item weights = 50 + 30 + 20 = 100
// Table weight = 100, so 100% chance to get a drop
```

**Probability Breakdown:**
- **Overall drop chance:** 100% (100/100)
- **If a drop occurs:**
  - 50% chance for coins (50/100)
  - 30% chance for shark (30/100)
  - 20% chance for dragon scimitar (20/100)

**Example with Lower Drop Chance:**
```kotlin
main(weight = 200) {
    add("item.coins", min = 1000, max = 5000, weight = 50)
    add("item.shark", min = 1, max = 5, weight = 30)
    add("item.dragon_scimitar", min = 1, max = 1, weight = 20)
}
// Total item weights = 100
// Table weight = 200, so 50% chance to get a drop (100/200)
```

**Probability Breakdown:**
- **Overall drop chance:** 50% (100/200)
- **If a drop occurs:**
  - 50% chance for coins (50/100)
  - 30% chance for shark (30/100)
  - 20% chance for dragon scimitar (20/100)

### Common Mistakes to Avoid

❌ **WRONG - Missing named parameters:**
```kotlin
main(128) {  // ❌ Missing "weight ="
    add("item.coins", 5000, 10000, 20)  // ❌ Missing "min =", "max =", "weight ="
}
```

✅ **CORRECT - Using named parameters:**
```kotlin
main(weight = 128) {
    add("item.coins", min = 5000, max = 10000, weight = 20)
}
```

❌ **WRONG - Table weight too low:**
```kotlin
main(weight = 50) {  // ❌ Items total 100, but table weight is only 50!
    add("item.coins", min = 1000, max = 5000, weight = 50)
    add("item.shark", min = 1, max = 5, weight = 30)
    add("item.dragon_scimitar", min = 1, max = 1, weight = 20)
    // Total = 100, but table weight = 50 → SERVER WILL CRASH!
}
```

✅ **CORRECT - Table weight >= sum of item weights:**
```kotlin
main(weight = 100) {  // ✅ Table weight (100) >= sum of items (100)
    add("item.coins", min = 1000, max = 5000, weight = 50)
    add("item.shark", min = 1, max = 5, weight = 30)
    add("item.dragon_scimitar", min = 1, max = 1, weight = 20)
    // Total = 100, table weight = 100 → Works perfectly!
}
```

### Quick Reference: Drop Table Cheat Sheet

| Parameter | Required? | Description | Example |
|-----------|-----------|-------------|---------|
| `main(weight = X)` | ✅ Yes | Table weight (must be >= sum of item weights) | `main(weight = 128)` |
| `min = X` | ✅ Yes | Minimum quantity | `min = 1` |
| `max = Y` | ✅ Yes | Maximum quantity | `max = 10` |
| `weight = Z` | ✅ Yes | Drop probability (relative to other items) | `weight = 20` |

### Example Drop Tables

#### Simple Drop Table (Guaranteed Drop)
```kotlin
drops {
    always { add("item.bones", 1) }
    
    main(weight = 50) {  // Table weight = sum of items = guaranteed drop
        add("item.coins", min = 1000, max = 5000, weight = 30)
        add("item.shark", min = 1, max = 5, weight = 20)
    }
}
```

#### Rare Drop Table (50% Drop Chance)
```kotlin
drops {
    always { add("item.bones", 1) }
    
    main(weight = 200) {  // Table weight = 2x sum of items = 50% drop chance
        add("item.coins", min = 5000, max = 10000, weight = 50)
        add("item.dragon_scimitar", min = 1, max = 1, weight = 30)
        add("item.abyssal_whip", min = 1, max = 1, weight = 20)
    }
}
```

#### Rich Boss Drop Table
```kotlin
drops {
    always { add("item.big_bones", 1) }
    
    main(weight = 500) {  // Large table for variety
        // Common drops
        add("item.coins", min = 10000, max = 50000, weight = 100)
        add("item.shark", min = 10, max = 20, weight = 80)
        add("item.super_restore_4", min = 3, max = 5, weight = 60)
        
        // Uncommon drops
        add("item.rune_scimitar", min = 1, max = 1, weight = 40)
        add("item.dragon_bones", min = 5, max = 10, weight = 30)
        
        // Rare drops
        add("item.dragon_scimitar", min = 1, max = 1, weight = 10)
        add("item.dragon_longsword", min = 1, max = 1, weight = 8)
        
        // Very rare drops
        add("item.abyssal_whip", min = 1, max = 1, weight = 2)
    }
}
```

### Tips for Creating Drop Tables

1. **Calculate First:** Add up all item weights before setting `main(weight = X)`
2. **Use Named Parameters:** Always use `min =`, `max =`, `weight =` for clarity
3. **Test Drop Rates:** Start with guaranteed drops (`weight = sum`), then adjust
4. **Balance Rarity:** Use lower weights for rare items, higher weights for common items
5. **Check Server Logs:** If server crashes on startup, check your drop table weights!

Have fun creating your monster! 🧟‍♂️🤖👹
