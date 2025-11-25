package org.alter.plugins.content.npcs.cerberus

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
 * Cerberus Configuration
 *
 * A high-level boss found at coordinates 1240, 1253
 * Cerberus is a powerful three-headed hellhound boss
 */
class CerberusConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Cerberus at coordinates 1240, 1253
        spawnNpc("npc.cerberus", x = 1240, z = 1253, height = 0, walkRadius = 6)

        // Note: Attack option handler not needed - setCombatDef makes the NPC attackable automatically
        // The combat system handles option 2 (attack) automatically via OpNpcHandler

        setCombatDef("npc.cerberus") {
            configs {
                attackSpeed = 3  // Fast attack speed
                respawnDelay = 100  // 60 seconds respawn (100 ticks)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 150  // Much lower hitpoints for easier fight
                attack = 80
                strength = 90
                defence = 60
                magic = 70
                ranged = 65
            }

            bonuses {
                attackStab = 20
                attackSlash = 20
                attackCrush = 25
                attackMagic = 30
                attackRanged = 25

                defenceStab = 40
                defenceSlash = 45
                defenceCrush = 50
                defenceMagic = 30
                defenceRanged = 35

                attackBonus = 50
                strengthBonus = 40
                rangedStrengthBonus = 30
                magicDamageBonus = 25
            }

            anims {
                block = 424
                death = 2856  // Death animation
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 500000, max = 500000) // Guaranteed 500k coins
                }

                main(weight = 128) {
                    // Unique drops
                    add("item.draconic_visage", min = 1, weight = 1) // Very rare
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_dagger", min = 1, weight = 3)

                    // Rune equipment
                    add("item.rune_chainbody", min = 1, weight = 8)
                    add("item.rune_platelegs", min = 1, weight = 7)
                    add("item.rune_full_helm", min = 1, weight = 10)
                    add("item.rune_scimitar", min = 1, weight = 8)

                    // Coins
                    add("item.coins_995", min = 500000, max = 2000000, weight = 25)

                    // Runes
                    add("item.nature_rune", min = 500, max = 500, weight = 15)
                    add("item.death_rune", min = 500, max = 500, weight = 12)
                    add("item.blood_rune", min = 500, max = 500, weight = 10)
                    add("item.chaos_rune", min = 500, max = 500, weight = 15)

                    // Resources
                    add("item.grimy_ranarr_weed", min = 3, max = 6, weight = 8)
                    add("item.grimy_snapdragon", min = 2, max = 4, weight = 5)
                    add("item.grimy_torstol", min = 1, max = 3, weight = 3)

                    // Bars and ores
                    add("item.adamantite_bar", min = 5, max = 10, weight = 10)
                    add("item.runite_ore", min = 2, max = 4, weight = 6)

                    // Food
                    add("item.shark", min = 5, max = 8, weight = 10)
                    add("item.super_restore4", min = 2, max = 3, weight = 7)

                    // Poison-themed items
                    add("item.antipoison4", min = 3, max = 5, weight = 12)
                    add("item.weapon_poison", min = 1, max = 2, weight = 8)
                }

                // Rare drop table (all rare items in one table)
                tertiary(weight = 250) {
                    // Cerberus crystals (1/10 each = 25/250)
                    add("item.eternal_crystal", min = 1, weight = 25)      // 1/10 chance
                    add("item.primordial_crystal", min = 1, weight = 25)   // 1/10 chance
                    add("item.pegasian_crystal", min = 1, weight = 25)     // 1/10 chance
                    
                    // Cerberus boots (1/25 each = 10/250)
                    add("item.pegasian_boots", min = 1, weight = 10)       // 1/25 chance
                    add("item.primordial_boots", min = 1, weight = 10)     // 1/25 chance  
                    add("item.eternal_boots", min = 1, weight = 10)        // 1/25 chance
                    
                    // Other rare items (much rarer now)
                    add("item.dragon_spear", min = 1, weight = 15)
                    add("item.dragon_platelegs", min = 1, weight = 10)
                    add("item.loop_half_of_a_key", min = 1, weight = 5)
                    add("item.tooth_half_of_a_key", min = 1, weight = 5)
                    add("item.shield_left_half", min = 1, weight = 2)
                    add("item.draconic_visage", min = 1, weight = 1)       // Very rare
                    
                    // Nothing drop (to make total weight 250 and balance rates)
                    // Weight 107 = no rare drop this time
                }
            }
        }
    }
}

