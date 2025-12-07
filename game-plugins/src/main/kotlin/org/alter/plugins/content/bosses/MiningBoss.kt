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
        // ==========================================
        // 1. SPAWN THE BOSS 📍
        // ==========================================
        // 👉 CHANGE "npc.crazy_archaeologist" to your NPC ID
        // 👉 CHANGE x and z to where you want it to spawn
        spawnNpc("npc.rock_925", x = 2977, z = 3238, walkRadius = 5)

        // Bonus: Spawn a decoration item nearby
        spawnItem("item.gilded_pickaxe", 1, 2977, 3241)

        // ==========================================
        // 2. DEFINE STATS & DROPS 📊
        // ==========================================
        //  CHANGE "npc.crazy_archaeologist" to your NPC ID
        setCombatDef("npc.rock_925") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            aggro {
                radius = 10
                searchDelay = 2 // How often to check for targets (in cycles)
                alwaysAggro() // Attacks even high-level players
            }
            stats {
                hitpoints = 100
                attack = 50
                strength = 6000
                defence = 150
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
                attack = 3353 // Attack Animation ID
                block = 424 // Block Animation ID
                death = 836 // Death Animation ID
            }
            drops {
                always { add("item.runite_ore_noted", 1) }
                main(weight = 500) {
                    // ========== NOTED ORES ==========
                    // Common ores (bronze/iron tier)
                    add("item.copper_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.tin_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.iron_ore_noted", min = 30, max = 150, weight = 35)
                    add("item.coal_noted", min = 25, max = 100, weight = 35)

                    // Mid-tier ores
                    add("item.silver_ore_noted", min = 20, max = 80, weight = 30)
                    add("item.gold_ore_noted", min = 15, max = 60, weight = 28)
                    add("item.mithril_ore_noted", min = 10, max = 50, weight = 25)

                    // High-tier ores
                    add("item.adamantite_ore_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_ore_noted", min = 3, max = 20, weight = 15)

                    // ========== NOTED BARS ==========
                    add("item.iron_bar_noted", min = 20, max = 100, weight = 30)
                    add("item.mithril_bar_noted", min = 10, max = 50, weight = 25)
                    add("item.adamantite_bar_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_bar_noted", min = 3, max = 20, weight = 15)

                    // ========== NOTED PICKAXES ==========
                    // Lower tier pickaxes (common)
                    add("item.bronze_pickaxe_noted", min = 5, max = 25, weight = 35)
                    add("item.iron_pickaxe_noted", min = 5, max = 20, weight = 32)
                    add("item.steel_pickaxe_noted", min = 3, max = 15, weight = 28)

                    // Mid-tier pickaxes
                    add("item.mithril_pickaxe_noted", min = 2, max = 10, weight = 25)
                    add("item.adamant_pickaxe_noted", min = 1, max = 8, weight = 20)

                    // High-tier pickaxes
                    add("item.rune_pickaxe_noted", min = 1, max = 5, weight = 15)
                    add("item.dragon_pickaxe", min = 1, max = 2, weight = 5) // Rare!
                    add("item.gilded_pickaxe", min = 1, max = 1, weight = 3) // Very rare!

                    // ========== OTHER DROPS ==========
                    add("item.coins", min = 5000, max = 10000, weight = 30)
                    add("item.dragon_scimitar", min = 1, max = 1, weight = 8) // Rare weapon
                }
            }
        }

        // ==========================================
        // 3. COMBAT LOGIC ⚔️
        // ==========================================

        // 🪤 TRAP LOGIC: When player picks up the pickaxe
        onGlobalItemPickup {
            val transaction =
                    player.attr[GROUNDITEM_PICKUP_TRANSACTION]?.get() ?: return@onGlobalItemPickup
            transaction.items.forEach { item ->
                if (item.item.id == getRSCM("item.gilded_pickaxe")) {
                    // Check if they are near the trap spot (2977, 3241)
                    if (player.tile.getDistance(Tile(2977, 3241)) <= 2) {
                        player.hit(135)
                        player.graphic(100) // Explosion!
                        player.message("IT'S A TRAP! The pickaxe explodes!")
                        // Remove the fake item
                        player.inventory.remove(item.item.id, item.item.amount)
                    }
                }
            }
        }

        // 👉 CHANGE "npc.crazy_archaeologist" to your NPC ID
        onNpcCombat("npc.rock_925") { npc.queue { combatLoop() } }
    }

    // This is the brain of the boss! 🧠
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        // Loop continuously while in combat
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)

            // 🚫 DISABLE PRAYERS: Boss drains player's connection to gods!
            Prayers.deactivateAll(target)

            // 🪤 TRAP: If player touches the Gilded Pickaxe (2977, 3241) while fighting
            if (target.tile.x == 2977 && target.tile.z == 3241) {
                target.hit(135)
                target.graphic(100) // Explosion!
                target.message("IT'S A TRAP! The pickaxe was fake!")
            }

            // Move to attack range if needed and check if attack is ready
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) &&
                            npc.isAttackDelayReady()
            ) {
                // 🎲 Randomly choose what to do
                val dice = npc.world.random(100)

                // 👉 ADJUST these numbers to change how often attacks happen!
                if (dice < 20) { // 20% Chance: SPECIAL ATTACK 💥
                    npc.forceChat("Feel the power!")
                    BossAttacks.aoe(npc, target.tile, radius = 3, projectile = 100)
                } else if (dice < 60) { // 40% Chance: MAGIC 🔮
                    BossAttacks.magic(npc, target, projectile = 100, anim = 3353)
                } else { // 40% Chance: MELEE 🗡️
                    BossAttacks.melee(npc, target, anim = 422)
                }

                npc.postAttackLogic(target)
            }

            // Wait before next cycle
            wait(1)

            // Update target reference (in case it changed)
            target = npc.getCombatTarget() as? Player ?: break
        }

        // Clean up when combat ends
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
