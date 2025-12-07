package org.alter.plugins.content.bosses.mining

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.GROUNDITEM_PICKUP_TRANSACTION
import org.alter.game.model.combat.*
import org.alter.game.model.entity.*
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.rscm.RSCM.getRSCM

// 👉 CHANGE "MyBossPlugin" to your boss name!
class MiningBossPlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

    init {
        // 1. SPAWN THE BOSS 📍
        // This makes the boss appear in the game at coordinates (2977, 3238)
        spawnNpc("npc.rock_925", x = 2977, z = 3238, walkRadius = 5)

        // Spawn a decoration item nearby (a pickaxe)
        spawnItem("item.gilded_pickaxe", 1, 2977, 3241)

        // 2. DEFINE STATS & DROPS 📊
        // This tells the game how strong the boss is and what it drops
        setCombatDef("npc.rock_925") {
            configs {
                attackSpeed = 4      // How fast it attacks
                respawnDelay = 50     // How long before it comes back after dying
            }
            aggro {
                radius = 10          // How far it can see players
                searchDelay = 2       // How often it looks for players
                alwaysAggro()         // Attacks players even if they're high level
            }
            stats {
                hitpoints = 100      // How much health it has
                attack = 50          // How accurate it is
                strength = 6000      // How hard it hits
                defence = 150         // How well it blocks
                magic = 300
                ranged = 1
            }
            bonuses {
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 100
                defenceRanged = 50
            }
            anims {
                attack = 3353        // Animation when it attacks
                block = 424          // Animation when it blocks
                death = 836          // Animation when it dies
            }
            
            // 🔊 SOUNDS: Make the boss sound epic!
            sound {
                attackSound = Sound.ROCK_CRAB_ATTACK  // Rock attack sound
                attackArea = true                    // All nearby players hear it
                attackVolume = 50                    // Volume level (0-100)
                attackRadius = 10                    // How far the sound travels
                
                blockSound = Sound.ROCK_CRAB_HIT     // Rock hit sound when blocking
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.ROCK_CRAB_DEATH   // Rock death sound
                deathArea = true                     // Everyone nearby hears the death
                deathVolume = 60
                deathRadius = 12
            }
            drops {
                // Always drops this item when killed
                always { add("item.runite_ore_noted", 1) }
                
                // Main drops - these are random
                main(weight = 500) {
                    // Common ores
                    add("item.copper_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.tin_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.iron_ore_noted", min = 30, max = 150, weight = 35)
                    add("item.coal_noted", min = 25, max = 100, weight = 35)

                    // Better ores
                    add("item.silver_ore_noted", min = 20, max = 80, weight = 30)
                    add("item.gold_ore_noted", min = 15, max = 60, weight = 28)
                    add("item.mithril_ore_noted", min = 10, max = 50, weight = 25)
                    add("item.adamantite_ore_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_ore_noted", min = 3, max = 20, weight = 15)

                    // Bars
                    add("item.iron_bar_noted", min = 20, max = 100, weight = 30)
                    add("item.mithril_bar_noted", min = 10, max = 50, weight = 25)
                    add("item.adamantite_bar_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_bar_noted", min = 3, max = 20, weight = 15)

                    // Pickaxes
                    add("item.bronze_pickaxe_noted", min = 5, max = 25, weight = 35)
                    add("item.iron_pickaxe_noted", min = 5, max = 20, weight = 32)
                    add("item.steel_pickaxe_noted", min = 3, max = 15, weight = 28)
                    add("item.mithril_pickaxe_noted", min = 2, max = 10, weight = 25)
                    add("item.adamant_pickaxe_noted", min = 1, max = 8, weight = 20)
                    add("item.rune_pickaxe_noted", min = 1, max = 5, weight = 15)
                    add("item.dragon_pickaxe", min = 1, max = 2, weight = 5)      // Rare!
                    add("item.gilded_pickaxe", min = 1, max = 1, weight = 3)      // Very rare!

                    // Other drops
                    add("item.coins", min = 5000, max = 10000, weight = 30)
                    add("item.dragon_scimitar", min = 1, max = 1, weight = 8)      // Rare weapon
                }
            }
        }

        // 3. COMBAT LOGIC ⚔️
        // When the boss starts fighting, run the combat loop
        // Also check if player is attacking with a pickaxe
        onNpcCombat("npc.rock_925") { npc.queue { combatLoop() } }
        
        // 4. SPECIAL FUNCTIONALITY 🎁
        // Give mining XP when the boss is killed
        onNpcDeath("npc.rock_925") {
            val npc = this.npc
            val killer = npc.damageMap.getMostDamage() as? Player ?: return@onNpcDeath
            
            // Give mining XP (1000 XP for 100 HP boss)
            val miningXp = npc.combatDef.hitpoints * 10.0
            killer.addXp(Skills.MINING, miningXp)
            killer.message("<col=00ff00>You gain ${miningXp.toInt()} Mining experience for defeating the Rock!</col>")
        }
    }

    // This is the brain of the boss! 🧠
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return
        var lastMessageTime = 0L

        // Keep fighting while boss is alive
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)

            // Turn off player's prayers
            Prayers.deactivateAll(target)

            // Check if player is attacking with a pickaxe
            // If they're not using a pickaxe, they deal 0 damage
            val hasPickaxe = target.hasWeaponType(WeaponType.PICKAXE)
            
            if (!hasPickaxe) {
                // Show message above boss's head (with cooldown to avoid spam)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastMessageTime > 3000) { // 3 second cooldown
                    npc.forceChat("Haha! Only a pickaxe can hurt me!")
                    lastMessageTime = currentTime
                }
                // Set damage multiplier to 0 so player deals no damage
                npc.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.0
            } else {
                // Reset damage multiplier if they have a pickaxe
                npc.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
            }

            // Trap: If player steps on the pickaxe, they get hurt!
            if (target.tile.x == 2977 && target.tile.z == 3241) {
                target.hit(135)
                target.graphic(100)
                target.message("IT'S A TRAP! The pickaxe was fake!")
            }

            // Move close and attack when ready
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) &&
                npc.isAttackDelayReady()
            ) {
                // Attack with melee! Can hit up to 110 damage
                BossAttacks.melee(npc, target, maxHit = 110, anim = 422)
                npc.postAttackLogic(target)
            }

            wait(1)
            target = npc.getCombatTarget() as? Player ?: break
        }

        // Clean up when combat ends
        npc.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
