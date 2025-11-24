package org.alter.plugins.content.npcs.vorkath

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
 * Vorkath Configuration
 *
 * A high-level dragon boss found at coordinates 2270, 4056
 * Vorkath is an undead dragon boss known for its powerful attacks
 */
class VorkathConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Vorkath at coordinates 2270, 4056
        spawnNpc("npc.vorkath", x = 2270, z = 4056, height = 0, walkRadius = 6)

        // Note: Attack option handler not needed - setCombatDef makes the NPC attackable automatically
        // Players can attack Vorkath by clicking option 2 (attack) which is handled by the combat system

        setCombatDef("npc.vorkath") {
            species {
                +NpcSpecies.DRACONIC
                +NpcSpecies.BASIC_DRAGON
            }

            configs {
                attackSpeed = 3  // Fast attack speed
                respawnDelay = 100  // 60 seconds respawn (100 ticks)
            }

            aggro {
                radius = 12  // Large detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 750  // Very high hitpoints for a tough boss
                attack = 400
                strength = 450
                defence = 300
                magic = 350
                ranged = 350
            }

            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 200
                attackMagic = 220
                attackRanged = 180

                defenceStab = 150
                defenceSlash = 170
                defenceCrush = 190
                defenceMagic = 120
                defenceRanged = 130

                attackBonus = 300
                strengthBonus = 280
                rangedStrengthBonus = 180
                magicDamageBonus = 150
            }

            anims {
                block = 89  // Dragon block animation
                death = 92  // Dragon death animation
            }

            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.blue_dragonhide", 1)
                }

                main(weight = 128) {
                    // Unique drops
                    add("item.draconic_visage", min = 1, weight = 1) // Very rare
                    add("item.vorkaths_head", min = 1, weight = 1) // Very rare
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_dagger", min = 1, weight = 3)
                    add("item.dragon_longsword", min = 1, weight = 2)

                    // Dragon equipment
                    add("item.dragon_platelegs", min = 1, weight = 5)
                    add("item.dragon_platebody", min = 1, weight = 4)
                    add("item.dragon_full_helm", min = 1, weight = 6)
                    add("item.dragon_kiteshield", min = 1, weight = 5)

                    // Rune equipment
                    add("item.rune_chainbody", min = 1, weight = 8)
                    add("item.rune_platelegs", min = 1, weight = 7)
                    add("item.rune_full_helm", min = 1, weight = 10)
                    add("item.rune_scimitar", min = 1, weight = 8)

                    // Coins
                    add("item.coins_995", min = 1000000, max = 3000000, weight = 30)

                    // Runes
                    add("item.nature_rune", min = 750, max = 1000, weight = 18)
                    add("item.death_rune", min = 750, max = 1000, weight = 15)
                    add("item.blood_rune", min = 500, max = 750, weight = 12)
                    add("item.chaos_rune", min = 750, max = 1000, weight = 18)
                    add("item.law_rune", min = 100, max = 200, weight = 10)

                    // Dragon items
                    add("item.dragon_arrowtips", min = 10, max = 20, weight = 8)
                    add("item.dragon_dart_tip", min = 10, max = 20, weight = 8)
                    add("item.dragon_javelin_heads", min = 20, max = 30, weight = 6)

                    // Resources
                    add("item.grimy_ranarr_weed", min = 5, max = 10, weight = 10)
                    add("item.grimy_snapdragon", min = 3, max = 6, weight = 6)
                    add("item.grimy_torstol", min = 2, max = 5, weight = 4)

                    // Bars and ores
                    add("item.adamantite_bar", min = 10, max = 20, weight = 12)
                    add("item.runite_ore", min = 5, max = 10, weight = 8)
                    add("item.runite_bar", min = 2, max = 5, weight = 5)

                    // Food
                    add("item.shark", min = 10, max = 15, weight = 12)
                    add("item.super_restore4", min = 3, max = 5, weight = 8)
                    add("item.super_combat_potion4", min = 2, max = 4, weight = 6)
                }

                // Rare drop table (additional rare items)
                tertiary(weight = 256) {
                    add("item.dragon_spear", min = 1, weight = 50)
                    add("item.shield_left_half", min = 1, weight = 256)
                    add("item.dragon_platelegs", min = 1, weight = 150)
                    add("item.dragon_platebody", min = 1, weight = 150)
                    add("item.loop_half_of_a_key", min = 1, weight = 128)
                    add("item.tooth_half_of_a_key", min = 1, weight = 128)
                }
            }
        }
    }
}

