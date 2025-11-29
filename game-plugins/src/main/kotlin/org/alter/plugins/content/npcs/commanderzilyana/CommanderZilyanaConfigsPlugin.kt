package org.alter.plugins.content.npcs.commanderzilyana

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
 * Commander Zilyana Configuration
 *
 * Commander Zilyana is the leader of Saradomin's forces in the God Wars Dungeon.
 * She is located at coordinates 2899, 5268, height 2.
 * 
 * Combat:
 * - Very fast melee attacks (2 ticks)
 * - Long movement range (chases player quickly)
 * - Special dash attack every few ticks: unavoidable light damage
 * - No magic/ranged attacks
 * - Accompanied by 3 minions: Starlight (melee), Growler (magic), Bree (ranged)
 */
class CommanderZilyanaConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Commander Zilyana
        // Coordinates: 2899, 5268, height 2
        spawnNpc("npc.commander_zilyana", x = 2899, z = 5268, height = 2, walkRadius = 10)

        setCombatDef("npc.commander_zilyana") {
            configs {
                attackSpeed = 2  // Very fast attack speed (2 ticks)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 254  // OSRS hitpoints
                attack = 250
                strength = 250
                defence = 240
                magic = 1  // Very low magic (doesn't use magic)
                ranged = 1  // Very low ranged (doesn't use ranged)
            }

            bonuses {
                attackStab = 150
                attackSlash = 150
                attackCrush = 160
                attackMagic = 0
                attackRanged = 0

                defenceStab = 180
                defenceSlash = 180
                defenceCrush = 190
                defenceMagic = 100
                defenceRanged = 160

                attackBonus = 180
                strengthBonus = 200
                rangedStrengthBonus = 0
                magicDamageBonus = 0
            }

            anims {
                block = 6969  // COMMANDER_ZILYANA_DEFEND
                death = 6968  // COMMANDER_ZILYANA_DEATH
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 500000, max = 500000) // Guaranteed 500k coins
                }

                // DROP TABLE 1: MAIN TABLE
                // Guaranteed drop from this table (weight = sum of item weights)
                // Total item weights = ~2500, so set weight to 2500 for 100% drop chance
                main(weight = 2500) {
                    // Saradomin signature uniques (spread across tables)
                    add("item.saradomin_sword", min = 1, weight = 30)  // ~3.75% (weight 30/800)
                    add("item.saradomin_hilt", min = 1, weight = 50)   // ~6.25% (weight 50/800) - rarest unique
                    add("item.saradomins_light", min = 1, weight = 30) // ~3.75% (weight 30/800)
                    
                    // Saradomin godsword variants
                    add("item.saradomin_godsword", min = 1, weight = 40)        // ~5% (weight 40/800)
                    add("item.saradomin_godsword_or", min = 1, weight = 25)     // ~3.1% (weight 25/800)
                    
                    // Saradomin staff and cape
                    add("item.saradomin_staff", min = 1, weight = 25)      // ~3.1% (weight 25/800)
                    add("item.saradomin_cape", min = 1, weight = 20)       // ~2.5% (weight 20/800)
                    add("item.imbued_saradomin_cape", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.imbued_saradomin_max_cape", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.imbued_saradomin_max_cape_l", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.imbued_saradomin_cape_deadman", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Holy book and related
                    add("item.holy_book", min = 1, weight = 25)      // ~3.1% (weight 25/800)
                    add("item.holy_book_page_set", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.holy_blessing", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.holy_symbol", min = 1, weight = 15)    // ~1.9% (weight 15/800)
                    add("item.holy_symbol_4682", min = 1, weight = 15) // ~1.9% (weight 15/800)
                    
                    // Monk's robes
                    add("item.monks_robe_top", min = 1, weight = 15)   // ~1.9% (weight 15/800)
                    add("item.monks_robe", min = 1, weight = 15)      // ~1.9% (weight 15/800)
                    add("item.monks_robe_top_g", min = 1, weight = 15) // ~1.9% (weight 15/800)
                    add("item.monks_robe_g", min = 1, weight = 15)    // ~1.9% (weight 15/800)
                    add("item.monks_robe_top_t", min = 1, weight = 15) // ~1.9% (weight 15/800)
                    add("item.monks_robe_t", min = 1, weight = 15)    // ~1.9% (weight 15/800)
                    
                    // Saradomin rune armour
                    add("item.saradomin_platebody", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.saradomin_platelegs", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_plateskirt", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_full_helm", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.saradomin_kiteshield", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_armour_set_lg", min = 1, weight = 15)  // ~1.9% (weight 15/800)
                    add("item.saradomin_armour_set_sk", min = 1, weight = 15)  // ~1.9% (weight 15/800)
                    
                    // Rune scimitar (Saradomin)
                    add("item.rune_scimitar_ornament_kit_saradomin", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.rune_scimitar_23330", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.rune_scimitar_23332", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.rune_scimitar_23334", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.rune_scimitar_26262", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Saradomin dragonhide armour
                    add("item.saradomin_dhide_body", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.saradomin_chaps", min = 1, weight = 20)      // ~2.5% (weight 20/800)
                    add("item.saradomin_coif", min = 1, weight = 20)       // ~2.5% (weight 20/800)
                    add("item.saradomin_dhide_boots", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_dhide_shield", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_dragonhide_set", min = 1, weight = 15) // ~1.9% (weight 15/800)
                    
                    // Saradomin vestment robes
                    add("item.saradomin_robe_top", min = 1, weight = 15)   // ~1.9% (weight 15/800)
                    add("item.saradomin_robe_legs", min = 1, weight = 15)  // ~1.9% (weight 15/800)
                    add("item.saradomin_stole", min = 1, weight = 15)      // ~1.9% (weight 15/800)
                    add("item.saradomin_mitre", min = 1, weight = 15)      // ~1.9% (weight 15/800)
                    add("item.saradomin_cloak", min = 1, weight = 15)      // ~1.9% (weight 15/800)
                    add("item.saradomin_crozier", min = 1, weight = 15)    // ~1.9% (weight 15/800)
                    
                    // Other Saradomin items
                    add("item.saradomin_bracers", min = 1, weight = 15)   // ~1.9% (weight 15/800)
                    add("item.saradomin_mjolnir", min = 1, weight = 25)      // ~3.1% (weight 25/800)
                    add("item.saradomin_banner", min = 1, weight = 20)      // ~2.5% (weight 20/800)
                    add("item.saradomin_banner_11891", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.saradomin_halo", min = 1, weight = 30)      // ~3.75% (weight 30/800)
                    add("item.saradomin_halo_l", min = 1, weight = 30)     // ~3.75% (weight 30/800)
                    add("item.saradomin_max_cape", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.saradomin_max_hood", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.imbued_saradomin_max_hood", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Holy items
                    add("item.holy_wraps", min = 1, weight = 20)      // ~2.5% (weight 20/800)
                    add("item.holy_sandals", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.devout_boots", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.ring_of_endurance", min = 1, weight = 25)    // ~3.1% (weight 25/800)
                    add("item.ring_of_endurance_uncharged", min = 1, weight = 25) // ~3.1% (weight 25/800)
                    
                    // Hallowed items
                    add("item.hallowed_focus", min = 1, weight = 30)      // ~3.75% (weight 30/800)
                    add("item.hallowed_grapple", min = 1, weight = 30)    // ~3.75% (weight 30/800)
                    add("item.hallowed_hammer", min = 1, weight = 30)     // ~3.75% (weight 30/800)
                    add("item.hallowed_ring", min = 1, weight = 30)       // ~3.75% (weight 30/800)
                    add("item.hallowed_symbol", min = 1, weight = 30)     // ~3.75% (weight 30/800)
                    add("item.hallowed_mark", min = 1, weight = 30)      // ~3.75% (weight 30/800)
                    add("item.hallowed_crystal_shard", min = 1, weight = 25) // ~3.1% (weight 25/800)
                    add("item.hallowed_token", min = 1, weight = 25)      // ~3.1% (weight 25/800)
                    add("item.hallowed_sack", min = 1, weight = 25)       // ~3.1% (weight 25/800)
                    
                    // Holy weapons
                    add("item.holy_scythe_of_vitur", min = 1, weight = 40)     // ~5% (weight 40/800)
                    add("item.holy_scythe_of_vitur_uncharged", min = 1, weight = 40) // ~5% (weight 40/800)
                    add("item.holy_ghrazi_rapier", min = 1, weight = 40)       // ~5% (weight 40/800)
                    add("item.holy_sanguinesti_staff", min = 1, weight = 40)   // ~5% (weight 40/800)
                    add("item.holy_sanguinesti_staff_uncharged", min = 1, weight = 40) // ~5% (weight 40/800)
                    
                    // Justiciar armour
                    add("item.justiciar_faceguard", min = 1, weight = 35)  // ~4.4% (weight 35/800)
                    add("item.justiciar_chestguard", min = 1, weight = 35) // ~4.4% (weight 35/800)
                    add("item.justiciar_legguards", min = 1, weight = 35) // ~4.4% (weight 35/800)
                    add("item.justiciar_armour_set", min = 1, weight = 30) // ~3.75% (weight 30/800)
                    add("item.justiciars_hand", min = 1, weight = 30)     // ~3.75% (weight 30/800)
                    
                    // Staff of light
                    add("item.staff_of_light", min = 1, weight = 30)      // ~3.75% (weight 30/800)
                    
                    // Castlewars items
                    add("item.castlewars_hood", min = 1, weight = 20)     // ~2.5% (weight 20/800)
                    add("item.castlewars_cloak", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.castlewars_hood_4515", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.castlewars_cloak_4516", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Initiate and Proselyte armour
                    add("item.initiate_sallet", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.initiate_hauberk", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.initiate_cuisse", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.initiate_harness_m", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.proselyte_sallet", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.proselyte_hauberk", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    add("item.proselyte_cuisse", min = 1, weight = 20)   // ~2.5% (weight 20/800)
                    add("item.proselyte_tasset", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.proselyte_harness_m", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.proselyte_harness_f", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.proselyte_sallet_20563", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.proselyte_hauberk_20564", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    add("item.proselyte_cuisse_20565", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Prayer book
                    add("item.prayer_book", min = 1, weight = 20)       // ~2.5% (weight 20/800)
                    add("item.prayer_book_10890", min = 1, weight = 20)  // ~2.5% (weight 20/800)
                    
                    // White lily
                    add("item.white_lily", min = 1, weight = 15)        // ~1.9% (weight 15/800)
                    add("item.white_lily_seed", min = 1, weight = 15)   // ~1.9% (weight 15/800)
                    
                    // Saradomin brew
                    add("item.saradomin_brew4", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.saradomin_brew3", min = 1, weight = 20)     // ~2.5% (weight 20/800)
                    add("item.saradomin_brew2", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.saradomin_brew1", min = 1, weight = 20)    // ~2.5% (weight 20/800)
                    add("item.saradomin_brew4_23575", min = 1, weight = 20) // ~2.5% (weight 20/800)
                    
                    // Regular drops
                    add("item.coins_995", min = 1000000, max = 3000000, weight = 30) // ~3.75% (weight 30/800)
                    add("item.death_rune", min = 500, max = 1000, weight = 20)     // ~2.5% (weight 20/800)
                    add("item.blood_rune", min = 500, max = 1000, weight = 20)     // ~2.5% (weight 20/800)
                }

                // DROP TABLE 2: PRE-ROLL TABLE
                // Rolls before main table. If it hits, main doesn't roll.
                // Low weight items so main still rolls most of the time
                // Total item weights = 3875, so set weight to 5000 for proper validation (with buffer)
                preroll(weight = 5000) {
                    // Saradomin signature uniques (spread across tables)
                    add("item.saradomin_sword", min = 1, weight = 128)  // ~0.78% chance (1/128)
                    add("item.saradomin_hilt", min = 1, weight = 256)  // ~0.39% chance (1/256) - rarest
                    add("item.saradomins_light", min = 1, weight = 128) // ~0.78% chance (1/128)
                    
                    // Saradomin godsword variants
                    add("item.saradomin_godsword", min = 1, weight = 100)        // ~1% chance (1/100)
                    add("item.saradomin_godsword_or", min = 1, weight = 150)     // ~0.67% chance (1/150)
                    
                    // Holy weapons (very rare)
                    add("item.holy_scythe_of_vitur", min = 1, weight = 500)     // ~0.2% chance (1/500)
                    add("item.holy_ghrazi_rapier", min = 1, weight = 500)       // ~0.2% chance (1/500)
                    add("item.holy_sanguinesti_staff", min = 1, weight = 500)   // ~0.2% chance (1/500)
                    
                    // Justiciar armour (very rare)
                    add("item.justiciar_faceguard", min = 1, weight = 300)  // ~0.33% chance (1/300)
                    add("item.justiciar_chestguard", min = 1, weight = 300) // ~0.33% chance (1/300)
                    add("item.justiciar_legguards", min = 1, weight = 300) // ~0.33% chance (1/300)
                    
                    // Staff of light
                    add("item.staff_of_light", min = 1, weight = 200)      // ~0.5% chance (1/200)
                    
                    // Saradomin max cape variants
                    add("item.saradomin_max_cape", min = 1, weight = 150)   // ~0.67% chance (1/150)
                    add("item.imbued_saradomin_max_cape", min = 1, weight = 150) // ~0.67% chance (1/150)
                    
                    // Ring of endurance
                    add("item.ring_of_endurance", min = 1, weight = 250)    // ~0.4% chance (1/250)
                }

                // DROP TABLE 3: TERTIARY TABLE
                // Can roll multiple items independently (each item rolls separately)
                // This table always rolls, each item has its own chance
                // Total item weights = 3875, so set weight to 4000 for proper validation (with buffer)
                tertiary(weight = 4000) {
                    // Saradomin signature uniques (spread across tables)
                    add("item.saradomin_sword", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    add("item.saradomin_hilt", min = 1, weight = 400)  // ~0.25% chance per kill (1/400) - rarest
                    add("item.saradomins_light", min = 1, weight = 200) // ~0.5% chance per kill (1/200)
                    
                    // Saradomin godsword variants
                    add("item.saradomin_godsword", min = 1, weight = 150)        // ~0.67% chance per kill (1/150)
                    add("item.saradomin_godsword_or", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    
                    // Saradomin staff and cape
                    add("item.saradomin_staff", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.saradomin_cape", min = 1, weight = 100)       // ~1% chance per kill (1/100)
                    add("item.imbued_saradomin_cape", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Holy book and related
                    add("item.holy_book", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.holy_book_page_set", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.holy_blessing", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.holy_symbol", min = 1, weight = 80)     // ~1.25% chance per kill (1/80)
                    
                    // Monk's robes
                    add("item.monks_robe_top", min = 1, weight = 80)   // ~1.25% chance per kill (1/80)
                    add("item.monks_robe", min = 1, weight = 80)      // ~1.25% chance per kill (1/80)
                    add("item.monks_robe_top_g", min = 1, weight = 80) // ~1.25% chance per kill (1/80)
                    add("item.monks_robe_g", min = 1, weight = 80)    // ~1.25% chance per kill (1/80)
                    add("item.monks_robe_top_t", min = 1, weight = 80) // ~1.25% chance per kill (1/80)
                    add("item.monks_robe_t", min = 1, weight = 80)    // ~1.25% chance per kill (1/80)
                    
                    // Saradomin rune armour
                    add("item.saradomin_platebody", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.saradomin_platelegs", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.saradomin_plateskirt", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.saradomin_full_helm", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.saradomin_kiteshield", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Rune scimitar (Saradomin)
                    add("item.rune_scimitar_ornament_kit_saradomin", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.rune_scimitar_23330", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.rune_scimitar_23332", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.rune_scimitar_23334", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.rune_scimitar_26262", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Saradomin dragonhide armour
                    add("item.saradomin_dhide_body", min = 1, weight = 90)  // ~1.11% chance per kill (1/90)
                    add("item.saradomin_chaps", min = 1, weight = 90)     // ~1.11% chance per kill (1/90)
                    add("item.saradomin_coif", min = 1, weight = 90)       // ~1.11% chance per kill (1/90)
                    add("item.saradomin_dhide_boots", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    add("item.saradomin_dhide_shield", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    
                    // Saradomin vestment robes
                    add("item.saradomin_robe_top", min = 1, weight = 110)   // ~0.91% chance per kill (1/110)
                    add("item.saradomin_robe_legs", min = 1, weight = 110)  // ~0.91% chance per kill (1/110)
                    add("item.saradomin_stole", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.saradomin_mitre", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.saradomin_cloak", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.saradomin_crozier", min = 1, weight = 110)    // ~0.91% chance per kill (1/110)
                    
                    // Other Saradomin items
                    add("item.saradomin_bracers", min = 1, weight = 105)   // ~0.95% chance per kill (1/105)
                    add("item.saradomin_mjolnir", min = 1, weight = 140)     // ~0.71% chance per kill (1/140)
                    add("item.saradomin_banner", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.saradomin_halo", min = 1, weight = 180)     // ~0.56% chance per kill (1/180)
                    add("item.saradomin_max_cape", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.saradomin_max_hood", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    
                    // Holy items
                    add("item.holy_wraps", min = 1, weight = 100)      // ~1% chance per kill (1/100)
                    add("item.holy_sandals", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.devout_boots", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.ring_of_endurance", min = 1, weight = 160)    // ~0.63% chance per kill (1/160)
                    
                    // Hallowed items
                    add("item.hallowed_focus", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    add("item.hallowed_grapple", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    add("item.hallowed_hammer", min = 1, weight = 200)     // ~0.5% chance per kill (1/200)
                    add("item.hallowed_ring", min = 1, weight = 200)       // ~0.5% chance per kill (1/200)
                    add("item.hallowed_symbol", min = 1, weight = 200)     // ~0.5% chance per kill (1/200)
                    add("item.hallowed_mark", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    
                    // Holy weapons
                    add("item.holy_scythe_of_vitur", min = 1, weight = 400)     // ~0.25% chance per kill (1/400)
                    add("item.holy_ghrazi_rapier", min = 1, weight = 400)       // ~0.25% chance per kill (1/400)
                    add("item.holy_sanguinesti_staff", min = 1, weight = 400)   // ~0.25% chance per kill (1/400)
                    
                    // Justiciar armour
                    add("item.justiciar_faceguard", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.justiciar_chestguard", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.justiciar_legguards", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    
                    // Staff of light
                    add("item.staff_of_light", min = 1, weight = 180)      // ~0.56% chance per kill (1/180)
                    
                    // Castlewars items
                    add("item.castlewars_hood", min = 1, weight = 120)     // ~0.83% chance per kill (1/120)
                    add("item.castlewars_cloak", min = 1, weight = 120)    // ~0.83% chance per kill (1/120)
                    
                    // Initiate and Proselyte armour
                    add("item.initiate_sallet", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.initiate_hauberk", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.initiate_cuisse", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.proselyte_sallet", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.proselyte_hauberk", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.proselyte_cuisse", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.proselyte_tasset", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    
                    // Prayer book
                    add("item.prayer_book", min = 1, weight = 100)       // ~1% chance per kill (1/100)
                    
                    // White lily
                    add("item.white_lily", min = 1, weight = 80)        // ~1.25% chance per kill (1/80)
                    
                    // Saradomin brew
                    add("item.saradomin_brew4", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    
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

