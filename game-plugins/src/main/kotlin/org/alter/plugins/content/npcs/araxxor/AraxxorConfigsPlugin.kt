package org.alter.plugins.content.npcs.araxxor

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

/**
 * Araxxor Configuration
 *
 * Araxxor is a high-level spider boss located in the Araxyte hive.
 * Location: 3633, 9816, height 0
 */
class AraxxorConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Araxxor at coordinates 3633, 9816
        spawnNpc("npc.araxxor", x = 3633, z = 9816, height = 0, walkRadius = 5)

        setCombatDef("npc.araxxor") {
            configs {
                attackSpeed = 4  // Attack speed (ticks)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 2000  // High HP for a challenging boss
                attack = 400
                strength = 400
                defence = 400
                magic = 400
                ranged = 400
            }

            bonuses {
                attackStab = 300
                attackSlash = 300
                attackCrush = 300
                attackMagic = 350
                attackRanged = 350

                defenceStab = 300
                defenceSlash = 300
                defenceCrush = 300
                defenceMagic = 280
                defenceRanged = 280

                attackBonus = 300
                strengthBonus = 350
                rangedStrengthBonus = 350
                magicDamageBonus = 400
            }

            anims {
                block = 424  // Generic block animation
                death = 836  // Generic death animation
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 1000000, max = 1000000) // Guaranteed 1M coins
                }

                main(weight = 200) {
                    // Araxxor-specific drops
                    add("item.coins_995", min = 1000000, max = 5000000, weight = 30)
                    add("item.death_rune", min = 1000, max = 2000, weight = 20)
                    add("item.blood_rune", min = 1000, max = 2000, weight = 20)
                    add("item.soul_rune", min = 500, max = 1000, weight = 15)
                    add("item.dragon_platelegs", min = 1, weight = 10)
                    add("item.dragon_plateskirt", min = 1, weight = 10)
                    add("item.dragon_boots", min = 1, weight = 8)
                    add("item.shark", min = 10, max = 20, weight = 15)
                    add("item.super_restore4", min = 5, max = 10, weight = 12)
                }

                tertiary(weight = 1000) {
                    // Rare Araxxor drops
                    add("item.dragon_spear", min = 1, weight = 50)
                    add("item.draconic_visage", min = 1, weight = 500)
                }
            }
        }
    }
}

