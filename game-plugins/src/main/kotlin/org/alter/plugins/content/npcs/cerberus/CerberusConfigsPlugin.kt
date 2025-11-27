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
                respawnDelay = 8  // 5 seconds respawn (8 ticks, matching OSRS)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 250  // Balanced HP - challenging but not too tanky
                attack = 250
                strength = 280
                defence = 220
                magic = 240
                ranged = 230
            }

            bonuses {
                attackStab = 120
                attackSlash = 120
                attackCrush = 130
                attackMagic = 140
                attackRanged = 130

                defenceStab = 150
                defenceSlash = 160
                defenceCrush = 170
                defenceMagic = 120
                defenceRanged = 140

                attackBonus = 150
                strengthBonus = 180
                rangedStrengthBonus = 140
                magicDamageBonus = 250  // Increased from 160 - makes magic attacks hit much harder
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

                // Total item weights: 185 (2+3+8+7+10+8+25+15+12+10+15+8+5+3+10+6+10+7+12+8)
                // Using weight = 200 for ~93% drop chance (200 >= 185)
                main(weight = 200) {
                    // Unique drops (draconic visage removed - should only be in tertiary table)
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
                // Tertiary tables roll each item independently: Random.nextInt(weight) == 0 means drop
                // Higher weight = rarer drop (weight 1000 = 1/1000 chance, weight 1 = 100% chance)
                // Total item weights: 1142 (25+25+25+10+10+10+15+10+5+5+2+1000)
                tertiary(weight = 1200) {
                    // Cerberus crystals (1/25 chance each)
                    add("item.eternal_crystal", min = 1, weight = 25)      // 1/25 chance
                    add("item.primordial_crystal", min = 1, weight = 25)   // 1/25 chance
                    add("item.pegasian_crystal", min = 1, weight = 25)     // 1/25 chance
                    
                    // Cerberus boots (1/10 chance each)
                    add("item.pegasian_boots", min = 1, weight = 10)       // 1/10 chance
                    add("item.primordial_boots", min = 1, weight = 10)     // 1/10 chance  
                    add("item.eternal_boots", min = 1, weight = 10)        // 1/10 chance
                    
                    // Other rare items
                    add("item.dragon_spear", min = 1, weight = 15)        // 1/15 chance
                    add("item.dragon_platelegs", min = 1, weight = 10)     // 1/10 chance
                    add("item.loop_half_of_key", min = 1, weight = 5)    // 1/5 chance
                    add("item.tooth_half_of_key", min = 1, weight = 5)   // 1/5 chance
                    add("item.shield_left_half", min = 1, weight = 2)      // 1/2 chance
                    add("item.draconic_visage", min = 1, weight = 1000)    // Very rare: 1/1000 chance
                }
            }
        }
    }
}

