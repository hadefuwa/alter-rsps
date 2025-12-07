package org.alter.plugins.content.bosses.zulrah

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Zulrah Combat Configuration
 *
 * Zulrah is a powerful boss located at the Zulrah Shrine.
 * Uses a mix of Ranged and Magic attacks with special effects.
 * 
 * Location: 2268, 3076, 0
 */
class ZulrahConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatDef("npc.zulrah") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }

            aggro {
                radius = 10
                searchDelay = 1
            }

            stats {
                hitpoints = 500  // High HP for a boss
                attack = 300
                strength = 300
                defence = 280
                magic = 350  // High magic for magic attacks
                ranged = 350  // High ranged for ranged attacks
            }

            bonuses {
                attackStab = 0
                attackSlash = 0
                attackCrush = 0
                attackMagic = 250
                attackRanged = 250

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 180
                defenceRanged = 180

                attackBonus = 0
                strengthBonus = 0
                rangedStrengthBonus = 250
                magicDamageBonus = 250
            }

            anims {
                attack = 5063  // Ranged attack animation
                block = 5064
                death = 5071
            }

            drops {
                // Always drop: Zulrah's scales (100-300)
                always {
                    add("item.zulrahs_scales", min = 100, max = 300)
                }

                // Main drop table: One resource drop guaranteed
                // Each item has equal weight, so each has 1/7 chance
                // Total weight = 7, so set weight to 7 for guaranteed drop
                main(weight = 7) {
                    add("item.battlestaff", min = 10, max = 10, weight = 1)
                    add("item.dragon_bones", min = 12, max = 12, weight = 1)
                    add("item.magic_logs", min = 35, max = 35, weight = 1)
                    add("item.runite_ore", min = 2, max = 2, weight = 1)
                    add("item.raw_shark", min = 35, max = 35, weight = 1)
                    add("item.grapes", min = 250, max = 250, weight = 1)
                    add("item.coins_995", min = 20000, max = 20000, weight = 1)
                }

                // Tertiary drop table: Rare drops (1/128 each)
                // Table weight must be >= sum of item weights (4 × 128 = 512)
                tertiary(weight = 512) {
                    add("item.tanzanite_fang", min = 1, max = 1, weight = 128, announce = true)
                    add("item.magic_fang", min = 1, max = 1, weight = 128, announce = true)
                    add("item.serpentine_visage", min = 1, max = 1, weight = 128, announce = true)
                    add("item.uncut_onyx", min = 1, max = 1, weight = 128, announce = true)
                }
            }
        }
    }
}
