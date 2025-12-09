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
class RunecraftBossPlugin(
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
        spawnNpc("npc.balance_elemental", x = 1720, z = 3829, walkRadius = 5)


        // ============================================================
        // 2. DEFINE STATS & DROPS 📊
        // ============================================================
        // 👉 CHANGE: Replace "npc.crazy_archaeologist" with your boss NPC name
        setCombatDef("npc.balance_elemental") {
            configs {
                attackSpeed = 1      // 👉 ADJUST: How fast it attacks (lower = faster)
                // attackSpeed = 3      // Very fast (3 ticks between attacks)
                // attackSpeed = 5      // Medium speed
                // attackSpeed = 6      // Slow but powerful
                
                respawnDelay = 30     // 👉 ADJUST: How long before it comes back after dying (in ticks)
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
                hitpoints = 250      // How much health it has
                // hitpoints = 100      // Weak boss (dies fast)
                // hitpoints = 1000     // Tank boss (hard to kill)
                // hitpoints = 2500     // Super tank (very hard to kill)
                
                attack = 3         // How accurate it is (higher = hits more often)
                // attack = 50          // Inaccurate (misses a lot)
                // attack = 450          // Very accurate (hits almost always)
                
                strength = 3       // How hard it hits (higher = more damage)
                // strength = 50        // Weak hits
                // strength = 450       // Very strong hits
                // strength = 6000      // One-shot potential!
                
                defence = 1         // How well it blocks (higher = takes less damage)
                // defence = 50         // Low defense (takes lots of damage)
                // defence = 450        // High defense (tanky)
                
                magic = 4          // 👉 ADJUST: Magic accuracy and damage
                // magic = 1            // No magic (melee/ranged only)
                // magic = 450          // Strong magic user
                
                ranged = 2          // 👉 ADJUST: Ranged accuracy and damage
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
                defenceStab = 1000     // Defense against stabbing weapons
                // defenceStab = 200    // Very resistant to stabs
                
                defenceSlash = 1000    // Defense against slashing weapons
                // defenceSlash = 200   // Very resistant to slashes
                
                defenceCrush = 1000    // Defense against crushing weapons
                // defenceCrush = 200   // Very resistant to crushes
                
                defenceMagic = 1   // Defense against magic attacks
                // defenceMagic = 50    // Weak to magic
                // defenceMagic = 400   // Very resistant to magic
                
                defenceRanged = 1000   // Defense against ranged attacks
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
                attackSound = Sound.DRAGON_DEATH  // Sound when boss attacks
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
                
                blockSound = Sound.DRAGON_DEATH    // Sound when boss blocks
                // blockSound = Sound.DRAGON_HIT        // Dragon hit sound
                // blockSound = Sound.DEMON_HIT         // Demon hit sound
                // blockSound = Sound.COW_HIT           // Cow hit sound
                
                blockArea = true
                blockVolume = 40                      // 👉 ADJUST: Volume
                blockRadius = 8
                
                deathSound = Sound.DRAGON_DEATH   // Sound when boss dies
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
                
                main(weight = 128) {  // 👉 ADJUST: The number (128) is the drop rate denominator
                    // Lower number = rarer drops (e.g., 5 = very rare, 128 = common)
                    
                    add("item.coins", min = 5000, max = 10000, weight = 20)  // 20/128 chance, drops 5000-10000 coins
                    add("item.pure_essence_noted", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    
                    // add("item.coins", min = 1000, max = 5000, weight = 50)   // More common, less coins
                    // add("item.coins", min = 10000, max = 50000, weight = 5)  // Rare but lots of coins
                    
                    add("item.air_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.water_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.earth_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.fire_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.mind_rune", min = 40, max = 600, weight = 5)  // � ADJUST: Rare drop! (5/128 chance)
                    add("item.wrath_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.chaos_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.death_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.nature_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.soul_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.cosmic_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                    add("item.blood_rune", min = 40, max = 600, weight = 5)  // 👉 ADJUST: Rare drop! (5/128 chance)
                }
            }
            
            // 💡 RICH BOSS DROP TABLE (uncomment to try):
            // drops {
            //     always { add("item.big_bones", 1) }
            //     main(128) {
            //         add("item.coins", 10000, 50000, 30)
            //         add("item.dragon_scimitar", 1, 5)
            //         add("item.dragon_longsword", 1, 5)
            //         add("item.abyssal_whip", 1, 2)
            //         add("item.shark", 10, 20, 50)
            //         add("item.super_restore_4", 3, 5, 40)
            //     }
            // }
        }

        // ============================================================
        // 3. COMBAT LOGIC ⚔️
        // ============================================================
        // When the boss starts fighting, run the combat loop
        // 👉 CHANGE: Replace "npc.crazy_archaeologist" with your boss NPC name
        onNpcCombat("npc.balance_elemental_13530") { npc.queue { combatLoop() } }
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
                BossAttacks.magic(npc, target, maxHit = 25, anim = 422)
                
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