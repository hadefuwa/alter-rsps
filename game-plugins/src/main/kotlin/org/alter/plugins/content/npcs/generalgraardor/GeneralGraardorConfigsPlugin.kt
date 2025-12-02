package org.alter.plugins.content.npcs.generalgraardor

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
 * General Graardor Configuration
 *
 * General Graardor is the leader of Bandos' forces in the God Wars Dungeon.
 * He is located in Bandos' Stronghold at coordinates 2864, 5354, height 2.
 * 
 * Combat:
 * - Uses melee attacks (max hit: 60)
 * - Uses ranged attacks that hit all players in the room (max hit: 35)
 * - Accompanied by 3 bodyguards: Sergeant Strongstack (melee), Sergeant Steelwill (magic), Sergeant Grimspike (ranged)
 */
class GeneralGraardorConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn General Graardor in Bandos' Stronghold
        // Coordinates: 2864, 5354, height 2 (God Wars Dungeon)
        spawnNpc("npc.general_graardor", x = 2864, z = 5354, height = 2, walkRadius = 3)

        setCombatDef("npc.general_graardor") {
            configs {
                attackSpeed = 7  // Slow attack speed (long interval)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 255  // OSRS hitpoints
                attack = 260
                strength = 260
                defence = 240
                magic = 1  // Very low magic (doesn't use magic)
                ranged = 250
            }

            bonuses {
                attackStab = 150
                attackSlash = 150
                attackCrush = 160
                attackMagic = 0
                attackRanged = 150

                defenceStab = 180
                defenceSlash = 180
                defenceCrush = 190
                defenceMagic = 100
                defenceRanged = 160

                attackBonus = 180
                strengthBonus = 200
                rangedStrengthBonus = 150
                magicDamageBonus = 0
            }

            anims {
                block = 7019  // GENERAL_GRAARDOR_DEFEND
                death = 7020  // GENERAL_GRAARDOR_DEATH
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 500000, max = 500000) // Guaranteed 500k coins
                }

                // DROP TABLE 1: MAIN TABLE
                // Guaranteed drop from this table (weight = sum of item weights)
                // Total item weights = 785, so set weight to 785 for 100% drop chance
                main(weight = 785) {
                    // Bandos signature uniques (spread across tables)
                    add("item.bandos_chestplate", min = 1, weight = 30)  // ~3.8% (weight 30/785)
                    add("item.bandos_tassets", min = 1, weight = 30)    // ~3.8% (weight 30/785)
                    add("item.bandos_boots", min = 1, weight = 30)      // ~3.8% (weight 30/785)
                    add("item.bandos_hilt", min = 1, weight = 50)       // ~6.4% (weight 50/785) - rarest unique
                    
                    // Bandos godsword variants
                    add("item.bandos_godsword", min = 1, weight = 40)        // ~5.1% (weight 40/785)
                    add("item.bandos_godsword_or", min = 1, weight = 25)     // ~3.2% (weight 25/785)
                    add("item.bandos_godsword_20782", min = 1, weight = 25)  // ~3.2% (weight 25/785)
                    add("item.bandos_godsword_21060", min = 1, weight = 25)  // ~3.2% (weight 25/785)
                    
                    // Bandos armour sets
                    add("item.bandos_platebody", min = 1, weight = 20)  // ~2.5% (weight 20/785)
                    add("item.bandos_platelegs", min = 1, weight = 20) // ~2.5% (weight 20/785)
                    add("item.bandos_plateskirt", min = 1, weight = 20) // ~2.5% (weight 20/785)
                    add("item.bandos_full_helm", min = 1, weight = 20)  // ~2.5% (weight 20/785)
                    add("item.bandos_kiteshield", min = 1, weight = 20) // ~2.5% (weight 20/785)
                    
                    // Bandos rune armour sets
                    add("item.bandos_rune_armour_set_lg", min = 1, weight = 15)  // ~1.9% (weight 15/785)
                    add("item.bandos_rune_armour_set_sk", min = 1, weight = 15)  // ~1.9% (weight 15/785)
                    
                    // Bandos dragonhide armour
                    add("item.bandos_dhide_body", min = 1, weight = 20)  // ~2.5% (weight 20/785)
                    add("item.bandos_chaps", min = 1, weight = 20)      // ~2.5% (weight 20/785)
                    add("item.bandos_coif", min = 1, weight = 20)       // ~2.5% (weight 20/785)
                    add("item.bandos_dhide_boots", min = 1, weight = 20) // ~2.5% (weight 20/785)
                    add("item.bandos_dhide_shield", min = 1, weight = 20) // ~2.5% (weight 20/785)
                    
                    // Bandos vestment robes
                    add("item.bandos_robe_top", min = 1, weight = 15)   // ~1.9% (weight 15/785)
                    add("item.bandos_robe_legs", min = 1, weight = 15)  // ~1.9% (weight 15/785)
                    add("item.bandos_stole", min = 1, weight = 15)      // ~1.9% (weight 15/785)
                    add("item.bandos_mitre", min = 1, weight = 15)      // ~1.9% (weight 15/785)
                    add("item.bandos_cloak", min = 1, weight = 15)      // ~1.9% (weight 15/785)
                    add("item.bandos_crozier", min = 1, weight = 15)    // ~1.9% (weight 15/785)
                    
                    // Other Bandos items
                    add("item.bandos_bracers", min = 1, weight = 15)   // ~1.9% (weight 15/785)
                    add("item.ancient_mace", min = 1, weight = 25)      // ~3.2% (weight 25/785)
                    add("item.war_blessing", min = 1, weight = 20)      // ~2.5% (weight 20/785)
                    add("item.bandos_halo", min = 1, weight = 30)      // ~3.8% (weight 30/785)
                    add("item.guardian_boots", min = 1, weight = 25)    // ~3.2% (weight 25/785)
                    add("item.echo_boots", min = 1, weight = 25)       // ~3.2% (weight 25/785)
                    
                    // Regular drops
                    add("item.coins_995", min = 1000000, max = 3000000, weight = 30) // ~3.8% (weight 30/785)
                    add("item.death_rune", min = 500, max = 1000, weight = 20)     // ~2.5% (weight 20/785)
                    add("item.blood_rune", min = 500, max = 1000, weight = 20)     // ~2.5% (weight 20/785)
                }

                // DROP TABLE 2: PRE-ROLL TABLE
                // Rolls before main table. If it hits, main doesn't roll.
                // Low weight items so main still rolls most of the time
                // Total item weights = 3875, so set weight to 5000 for proper validation (with buffer)
                preroll(weight = 5000) {
                    // Bandos signature uniques (spread across tables)
                    add("item.bandos_chestplate", min = 1, weight = 128)  // ~0.78% chance (1/128)
                    add("item.bandos_tassets", min = 1, weight = 128)    // ~0.78% chance (1/128)
                    add("item.bandos_boots", min = 1, weight = 128)      // ~0.78% chance (1/128)
                    add("item.bandos_hilt", min = 1, weight = 256)       // ~0.39% chance (1/256) - rarest
                    
                    // Bandos godsword variants
                    add("item.bandos_godsword", min = 1, weight = 100)        // ~1% chance (1/100)
                    add("item.bandos_godsword_or", min = 1, weight = 150)     // ~0.67% chance (1/150)
                    add("item.bandos_godsword_20782", min = 1, weight = 150)  // ~0.67% chance (1/150)
                    add("item.bandos_godsword_21060", min = 1, weight = 150)  // ~0.67% chance (1/150)
                    
                    // Book of War and pages
                    add("item.book_of_war", min = 1, weight = 200)      // ~0.5% chance (1/200)
                    add("item.bandos_page_1", min = 1, weight = 100)    // ~1% chance (1/100)
                    add("item.bandos_page_2", min = 1, weight = 100)    // ~1% chance (1/100)
                    add("item.bandos_page_3", min = 1, weight = 100)   // ~1% chance (1/100)
                    add("item.bandos_page_4", min = 1, weight = 100)   // ~1% chance (1/100)
                    add("item.book_of_war_page_set", min = 1, weight = 80) // ~1.25% chance (1/80)
                    add("item.book_of_war_or", min = 1, weight = 250)   // ~0.4% chance (1/250)
                    
                    // Bandos armour sets
                    add("item.bandos_platebody", min = 1, weight = 80)  // ~1.25% chance (1/80)
                    add("item.bandos_platelegs", min = 1, weight = 80) // ~1.25% chance (1/80)
                    add("item.bandos_plateskirt", min = 1, weight = 80) // ~1.25% chance (1/80)
                    add("item.bandos_full_helm", min = 1, weight = 80)  // ~1.25% chance (1/80)
                    add("item.bandos_kiteshield", min = 1, weight = 80) // ~1.25% chance (1/80)
                    
                    // Bandos rune armour sets
                    add("item.bandos_rune_armour_set_lg", min = 1, weight = 60)  // ~1.67% chance (1/60)
                    add("item.bandos_rune_armour_set_sk", min = 1, weight = 60)  // ~1.67% chance (1/60)
                    
                    // Bandos dragonhide armour
                    add("item.bandos_dhide_body", min = 1, weight = 70)  // ~1.43% chance (1/70)
                    add("item.bandos_chaps", min = 1, weight = 70)      // ~1.43% chance (1/70)
                    add("item.bandos_coif", min = 1, weight = 70)       // ~1.43% chance (1/70)
                    add("item.bandos_dhide_boots", min = 1, weight = 70) // ~1.43% chance (1/70)
                    add("item.bandos_dhide_shield", min = 1, weight = 70) // ~1.43% chance (1/70)
                    
                    // Bandos vestment robes
                    add("item.bandos_robe_top", min = 1, weight = 90)   // ~1.11% chance (1/90)
                    add("item.bandos_robe_legs", min = 1, weight = 90)  // ~1.11% chance (1/90)
                    add("item.bandos_stole", min = 1, weight = 90)      // ~1.11% chance (1/90)
                    add("item.bandos_mitre", min = 1, weight = 90)      // ~1.11% chance (1/90)
                    add("item.bandos_cloak", min = 1, weight = 90)     // ~1.11% chance (1/90)
                    add("item.bandos_crozier", min = 1, weight = 90)    // ~1.11% chance (1/90)
                    
                    // Other Bandos items
                    add("item.bandos_bracers", min = 1, weight = 85)   // ~1.18% chance (1/85)
                    add("item.ancient_mace", min = 1, weight = 120)     // ~0.83% chance (1/120)
                    add("item.war_blessing", min = 1, weight = 100)    // ~1% chance (1/100)
                    add("item.bandos_halo", min = 1, weight = 150)     // ~0.67% chance (1/150)
                    add("item.guardian_boots", min = 1, weight = 130)   // ~0.77% chance (1/130)
                    add("item.echo_boots", min = 1, weight = 130)      // ~0.77% chance (1/130)
                }

                // DROP TABLE 3: TERTIARY TABLE
                // Can roll multiple items independently (each item rolls separately)
                // This table always rolls, each item has its own chance
                // Total item weights = 3875, so set weight to 4000 for proper validation (with buffer)
                tertiary(weight = 4000) {
                    // Bandos signature uniques (spread across tables)
                    add("item.bandos_chestplate", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    add("item.bandos_tassets", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    add("item.bandos_boots", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    add("item.bandos_hilt", min = 1, weight = 400)       // ~0.25% chance per kill (1/400) - rarest
                    
                    // Bandos godsword variants
                    add("item.bandos_godsword", min = 1, weight = 150)        // ~0.67% chance per kill (1/150)
                    add("item.bandos_godsword_or", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    add("item.bandos_godsword_20782", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    add("item.bandos_godsword_21060", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    
                    // Book of War and pages
                    add("item.book_of_war", min = 1, weight = 300)      // ~0.33% chance per kill (1/300)
                    add("item.bandos_page_1", min = 1, weight = 150)    // ~0.67% chance per kill (1/150)
                    add("item.bandos_page_2", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.bandos_page_3", min = 1, weight = 150)    // ~0.67% chance per kill (1/150)
                    add("item.bandos_page_4", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.book_of_war_page_set", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    add("item.book_of_war_or", min = 1, weight = 350)   // ~0.29% chance per kill (1/350)
                    
                    // Bandos armour sets
                    add("item.bandos_platebody", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.bandos_platelegs", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.bandos_plateskirt", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.bandos_full_helm", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.bandos_kiteshield", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Bandos rune armour sets
                    add("item.bandos_rune_armour_set_lg", min = 1, weight = 80)  // ~1.25% chance per kill (1/80)
                    add("item.bandos_rune_armour_set_sk", min = 1, weight = 80)  // ~1.25% chance per kill (1/80)
                    
                    // Bandos dragonhide armour
                    add("item.bandos_dhide_body", min = 1, weight = 90)  // ~1.11% chance per kill (1/90)
                    add("item.bandos_chaps", min = 1, weight = 90)     // ~1.11% chance per kill (1/90)
                    add("item.bandos_coif", min = 1, weight = 90)       // ~1.11% chance per kill (1/90)
                    add("item.bandos_dhide_boots", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    add("item.bandos_dhide_shield", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    
                    // Bandos vestment robes
                    add("item.bandos_robe_top", min = 1, weight = 110)   // ~0.91% chance per kill (1/110)
                    add("item.bandos_robe_legs", min = 1, weight = 110)  // ~0.91% chance per kill (1/110)
                    add("item.bandos_stole", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.bandos_mitre", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.bandos_cloak", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.bandos_crozier", min = 1, weight = 110)    // ~0.91% chance per kill (1/110)
                    
                    // Other Bandos items
                    add("item.bandos_bracers", min = 1, weight = 105)   // ~0.95% chance per kill (1/105)
                    add("item.ancient_mace", min = 1, weight = 140)     // ~0.71% chance per kill (1/140)
                    add("item.war_blessing", min = 1, weight = 120)     // ~0.83% chance per kill (1/120)
                    add("item.bandos_halo", min = 1, weight = 180)     // ~0.56% chance per kill (1/180)
                    add("item.guardian_boots", min = 1, weight = 160)   // ~0.63% chance per kill (1/160)
                    add("item.echo_boots", min = 1, weight = 160)      // ~0.63% chance per kill (1/160)
                    
                    // Rare drops
                    add("item.dragon_spear", min = 1, weight = 50)      // ~2% chance per kill (1/50)
                    add("item.dragon_platelegs", min = 1, weight = 60) // ~1.67% chance per kill (1/60)
                    add("item.dragon_plateskirt", min = 1, weight = 60) // ~1.67% chance per kill (1/60)
                    add("item.dragon_boots", min = 1, weight = 80)     // ~1.25% chance per kill (1/80)
                }
            }
        }
    }
}

