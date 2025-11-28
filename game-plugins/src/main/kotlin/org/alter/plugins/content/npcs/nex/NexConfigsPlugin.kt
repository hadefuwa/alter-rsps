package org.alter.plugins.content.npcs.nex

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
 * Nex Configuration
 *
 * Nex is a 5-phase boss located in the Ancient Prison.
 * Each phase is tied to a minion that must be killed to proceed.
 * 
 * Phases in order:
 * 1. Smoke (Fumus minion)
 * 2. Shadow (Umbra minion)
 * 3. Blood (Cruor minion)
 * 4. Ice (Glacies minion)
 * 5. Zaros (final phase, no minion)
 * 
 * Location: Ancient Prison (2925, 5203, height 0)
 */
class NexConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Nex in the Ancient Prison
        // Coordinates: 2925, 5203, height 0
        spawnNpc("npc.nex", x = 2925, z = 5203, height = 0, walkRadius = 5)

        setCombatDef("npc.nex") {
            configs {
                attackSpeed = 4  // Attack speed (ticks)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 1000  // High HP for 5-phase boss
                attack = 300
                strength = 300
                defence = 300
                magic = 300
                ranged = 300
            }

            bonuses {
                attackStab = 200
                attackSlash = 200
                attackCrush = 200
                attackMagic = 250
                attackRanged = 250

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 180
                defenceRanged = 180

                attackBonus = 200
                strengthBonus = 250
                rangedStrengthBonus = 250
                magicDamageBonus = 300
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

                // DROP TABLE 1: MAIN TABLE
                // Guaranteed drop from this table (weight = sum of item weights)
                // Total item weights = ~2500, so set weight to 2500 for 100% drop chance
                main(weight = 2500) {
                    // Ancient staff and sceptre variants
                    add("item.ancient_staff", min = 1, weight = 30)      // ~1.2% (weight 30/2500)
                    add("item.ancient_staff_20431", min = 1, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.ancient_sceptre", min = 1, weight = 40)    // ~1.6% (weight 40/2500)
                    add("item.ancient_sceptre_l", min = 1, weight = 40)  // ~1.6% (weight 40/2500)
                    add("item.blood_ancient_sceptre", min = 1, weight = 40)  // ~1.6% (weight 40/2500)
                    add("item.ice_ancient_sceptre", min = 1, weight = 40)   // ~1.6% (weight 40/2500)
                    add("item.smoke_ancient_sceptre", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.shadow_ancient_sceptre", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.blood_ancient_sceptre_l", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.ice_ancient_sceptre_l", min = 1, weight = 40)  // ~1.6% (weight 40/2500)
                    add("item.smoke_ancient_sceptre_l", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.shadow_ancient_sceptre_l", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.blood_ancient_sceptre_28260", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.ice_ancient_sceptre_28262", min = 1, weight = 40)  // ~1.6% (weight 40/2500)
                    add("item.smoke_ancient_sceptre_28264", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.shadow_ancient_sceptre_28266", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    
                    // Book of darkness
                    add("item.book_of_darkness", min = 1, weight = 25)      // ~1% (weight 25/2500)
                    add("item.book_of_darkness_page_set", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.book_of_darkness_or", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.ancient_page_1", min = 1, weight = 15)       // ~0.6% (weight 15/2500)
                    add("item.ancient_page_2", min = 1, weight = 15)       // ~0.6% (weight 15/2500)
                    add("item.ancient_page_3", min = 1, weight = 15)       // ~0.6% (weight 15/2500)
                    add("item.ancient_page_4", min = 1, weight = 15)       // ~0.6% (weight 15/2500)
                    
                    // Robes of darkness (ancient vestment robes)
                    add("item.ancient_robe_top", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    add("item.ancient_robe_legs", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    add("item.ancient_stole", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.ancient_mitre", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.ancient_cloak", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.ancient_crozier", min = 1, weight = 15)    // ~0.6% (weight 15/2500)
                    
                    // Ancient rune armour
                    add("item.ancient_platebody", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.ancient_platelegs", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.ancient_plateskirt", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.ancient_full_helm", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.ancient_kiteshield", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.ancient_rune_armour_set_lg", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    add("item.ancient_rune_armour_set_sk", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    
                    // Ancient dragonhide armour
                    add("item.ancient_dhide_body", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.ancient_chaps", min = 1, weight = 20)      // ~0.8% (weight 20/2500)
                    add("item.ancient_coif", min = 1, weight = 20)       // ~0.8% (weight 20/2500)
                    add("item.ancient_dhide_boots", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.ancient_dhide_shield", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.ancient_dragonhide_set", min = 1, weight = 15) // ~0.6% (weight 15/2500)
                    add("item.ancient_bracers", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    
                    // Ancient blessing and halo
                    add("item.ancient_blessing", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.ancient_halo", min = 1, weight = 30)      // ~1.2% (weight 30/2500)
                    add("item.ancient_halo_l", min = 1, weight = 30)     // ~1.2% (weight 30/2500)
                    
                    // Ancient godsword
                    add("item.ancient_godsword", min = 1, weight = 50)   // ~2% (weight 50/2500)
                    add("item.ancient_godsword_27184", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.ancient_hilt", min = 1, weight = 50)       // ~2% (weight 50/2500)
                    
                    // Zaryte crossbow and vambraces
                    add("item.zaryte_crossbow", min = 1, weight = 40)    // ~1.6% (weight 40/2500)
                    add("item.zaryte_crossbow_27186", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.zaryte_vambraces", min = 1, weight = 40)   // ~1.6% (weight 40/2500)
                    
                    // Venator bow
                    add("item.venator_bow", min = 1, weight = 40)        // ~1.6% (weight 40/2500)
                    add("item.venator_bow_uncharged", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    add("item.echo_venator_bow", min = 1, weight = 40)   // ~1.6% (weight 40/2500)
                    add("item.echo_venator_bow_uncharged", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    
                    // Ancient ceremonial robes
                    add("item.ancient_ceremonial_top", min = 1, weight = 25)  // ~1% (weight 25/2500)
                    add("item.ancient_ceremonial_legs", min = 1, weight = 25) // ~1% (weight 25/2500)
                    add("item.ancient_ceremonial_mask", min = 1, weight = 25)  // ~1% (weight 25/2500)
                    add("item.ancient_ceremonial_gloves", min = 1, weight = 25) // ~1% (weight 25/2500)
                    add("item.ancient_ceremonial_boots", min = 1, weight = 25) // ~1% (weight 25/2500)
                    
                    // Torva armour
                    add("item.torva_full_helm", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platebody", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platelegs", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.torva_full_helm_damaged", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platebody_damaged", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platelegs_damaged", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.sanguine_torva_full_helm", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.sanguine_torva_platebody", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.sanguine_torva_platelegs", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.torva_full_helm_30302", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platebody_30303", min = 1, weight = 50)  // ~2% (weight 50/2500)
                    add("item.torva_platelegs_30304", min = 1, weight = 50) // ~2% (weight 50/2500)
                    
                    // Virtus robes
                    add("item.virtus_mask", min = 1, weight = 50)      // ~2% (weight 50/2500)
                    add("item.virtus_robe_top", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.virtus_robe_bottom", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.echo_virtus_mask", min = 1, weight = 50)      // ~2% (weight 50/2500)
                    add("item.echo_virtus_robe_top", min = 1, weight = 50) // ~2% (weight 50/2500)
                    add("item.echo_virtus_robe_bottom", min = 1, weight = 50) // ~2% (weight 50/2500)
                    
                    // Ring of shadows
                    add("item.ring_of_shadows", min = 1, weight = 40)    // ~1.6% (weight 40/2500)
                    add("item.ring_of_shadows_uncharged", min = 1, weight = 40) // ~1.6% (weight 40/2500)
                    
                    // Soulreaper axe
                    add("item.soulreaper_axe", min = 1, weight = 50)     // ~2% (weight 50/2500)
                    add("item.soulreaper_axe_28338", min = 1, weight = 50) // ~2% (weight 50/2500)
                    
                    // Ancient talisman and signet
                    add("item.ancient_talisman", min = 1, weight = 30)   // ~1.2% (weight 30/2500)
                    add("item.ancient_signet", min = 1, weight = 30)     // ~1.2% (weight 30/2500)
                    
                    // Ancient brew and forgotten brew
                    add("item.ancient_brew4", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.ancient_brew3", min = 1, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.ancient_brew2", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.ancient_brew1", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.forgotten_brew4", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.forgotten_brew3", min = 1, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.forgotten_brew2", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.forgotten_brew1", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    
                    // Regular drops
                    add("item.coins_995", min = 2000000, max = 5000000, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.death_rune", min = 1000, max = 2000, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.blood_rune", min = 1000, max = 2000, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.soul_rune", min = 500, max = 1000, weight = 15)        // ~0.6% (weight 15/2500)
                }

                // DROP TABLE 2: PRE-ROLL TABLE
                // Rolls before main table. If it hits, main doesn't roll.
                // Low weight items so main still rolls most of the time
                preroll {
                    // Ancient signature uniques (spread across tables)
                    add("item.ancient_godsword", min = 1, weight = 256)   // ~0.39% chance (1/256)
                    add("item.ancient_hilt", min = 1, weight = 256)       // ~0.39% chance (1/256)
                    
                    // Very rare weapons
                    add("item.zaryte_crossbow", min = 1, weight = 300)    // ~0.33% chance (1/300)
                    add("item.venator_bow", min = 1, weight = 300)        // ~0.33% chance (1/300)
                    add("item.soulreaper_axe", min = 1, weight = 400)     // ~0.25% chance (1/400)
                    
                    // Torva armour (very rare)
                    add("item.torva_full_helm", min = 1, weight = 300)  // ~0.33% chance (1/300)
                    add("item.torva_platebody", min = 1, weight = 300)  // ~0.33% chance (1/300)
                    add("item.torva_platelegs", min = 1, weight = 300) // ~0.33% chance (1/300)
                    
                    // Virtus robes (very rare)
                    add("item.virtus_mask", min = 1, weight = 300)      // ~0.33% chance (1/300)
                    add("item.virtus_robe_top", min = 1, weight = 300) // ~0.33% chance (1/300)
                    add("item.virtus_robe_bottom", min = 1, weight = 300) // ~0.33% chance (1/300)
                    
                    // Ring of shadows
                    add("item.ring_of_shadows", min = 1, weight = 350)    // ~0.29% chance (1/350)
                }

                // DROP TABLE 3: TERTIARY TABLE
                // Can roll multiple items independently (each item rolls separately)
                // This table always rolls, each item has its own chance
                tertiary(weight = 0) {
                    // Ancient signature uniques (spread across tables)
                    add("item.ancient_godsword", min = 1, weight = 400)   // ~0.25% chance per kill (1/400)
                    add("item.ancient_hilt", min = 1, weight = 400)       // ~0.25% chance per kill (1/400)
                    
                    // Ancient staff and sceptre variants
                    add("item.ancient_staff", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.ancient_sceptre", min = 1, weight = 150)    // ~0.67% chance per kill (1/150)
                    add("item.blood_ancient_sceptre", min = 1, weight = 150)  // ~0.67% chance per kill (1/150)
                    add("item.ice_ancient_sceptre", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.smoke_ancient_sceptre", min = 1, weight = 150) // ~0.67% chance per kill (1/150)
                    add("item.shadow_ancient_sceptre", min = 1, weight = 150) // ~0.67% chance per kill (1/150)
                    
                    // Book of darkness
                    add("item.book_of_darkness", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.book_of_darkness_page_set", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.ancient_blessing", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    
                    // Robes of darkness (ancient vestment robes)
                    add("item.ancient_robe_top", min = 1, weight = 110)   // ~0.91% chance per kill (1/110)
                    add("item.ancient_robe_legs", min = 1, weight = 110)  // ~0.91% chance per kill (1/110)
                    add("item.ancient_stole", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.ancient_mitre", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.ancient_cloak", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.ancient_crozier", min = 1, weight = 110)    // ~0.91% chance per kill (1/110)
                    
                    // Ancient rune armour
                    add("item.ancient_platebody", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.ancient_platelegs", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.ancient_plateskirt", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.ancient_full_helm", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.ancient_kiteshield", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Ancient dragonhide armour
                    add("item.ancient_dhide_body", min = 1, weight = 90)  // ~1.11% chance per kill (1/90)
                    add("item.ancient_chaps", min = 1, weight = 90)     // ~1.11% chance per kill (1/90)
                    add("item.ancient_coif", min = 1, weight = 90)       // ~1.11% chance per kill (1/90)
                    add("item.ancient_dhide_boots", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    add("item.ancient_dhide_shield", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    
                    // Ancient blessing and halo
                    add("item.ancient_blessing", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.ancient_halo", min = 1, weight = 180)     // ~0.56% chance per kill (1/180)
                    
                    // Zaryte crossbow and vambraces
                    add("item.zaryte_crossbow", min = 1, weight = 250)    // ~0.4% chance per kill (1/250)
                    add("item.zaryte_vambraces", min = 1, weight = 250)   // ~0.4% chance per kill (1/250)
                    
                    // Venator bow
                    add("item.venator_bow", min = 1, weight = 250)        // ~0.4% chance per kill (1/250)
                    add("item.venator_bow_uncharged", min = 1, weight = 250) // ~0.4% chance per kill (1/250)
                    
                    // Ancient ceremonial robes
                    add("item.ancient_ceremonial_top", min = 1, weight = 120)  // ~0.83% chance per kill (1/120)
                    add("item.ancient_ceremonial_legs", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    add("item.ancient_ceremonial_mask", min = 1, weight = 120)  // ~0.83% chance per kill (1/120)
                    add("item.ancient_ceremonial_gloves", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    add("item.ancient_ceremonial_boots", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    
                    // Torva armour
                    add("item.torva_full_helm", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.torva_platebody", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.torva_platelegs", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.torva_full_helm_damaged", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.torva_platebody_damaged", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.torva_platelegs_damaged", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    
                    // Virtus robes
                    add("item.virtus_mask", min = 1, weight = 300)      // ~0.33% chance per kill (1/300)
                    add("item.virtus_robe_top", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.virtus_robe_bottom", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.echo_virtus_mask", min = 1, weight = 300)      // ~0.33% chance per kill (1/300)
                    add("item.echo_virtus_robe_top", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.echo_virtus_robe_bottom", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    
                    // Ring of shadows
                    add("item.ring_of_shadows", min = 1, weight = 350)    // ~0.29% chance per kill (1/350)
                    add("item.ring_of_shadows_uncharged", min = 1, weight = 350) // ~0.29% chance per kill (1/350)
                    
                    // Soulreaper axe
                    add("item.soulreaper_axe", min = 1, weight = 400)     // ~0.25% chance per kill (1/400)
                    
                    // Ancient talisman and signet
                    add("item.ancient_talisman", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.ancient_signet", min = 1, weight = 150)     // ~0.67% chance per kill (1/150)
                    
                    // Ancient brew and forgotten brew
                    add("item.ancient_brew4", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.forgotten_brew4", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    
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

