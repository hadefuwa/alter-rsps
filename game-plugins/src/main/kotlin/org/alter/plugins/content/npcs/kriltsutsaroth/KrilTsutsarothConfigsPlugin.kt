package org.alter.plugins.content.npcs.kriltsutsaroth

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
 * K'ril Tsutsaroth Configuration
 *
 * K'ril Tsutsaroth is the leader of Zamorak's forces in the God Wars Dungeon.
 * He is located in Zamorak's Fortress at coordinates 2925, 5330, height 2.
 * 
 * Combat:
 * - Hard-hitting melee attacks (very high damage)
 * - Special attack: Typeless damage that ignores prayer and drains prayer points
 * - Can use magic splash attack rarely
 * - Accompanied by 3 minions: Balfrug Kreeyath (magic), Tstanon Karlak (melee), Zakl'n Gritch (ranged)
 */
class KrilTsutsarothConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn K'ril Tsutsaroth in Zamorak's Fortress
        // Coordinates: 2925, 5330, height 2 (God Wars Dungeon)
        spawnNpc("npc.kril_tsutsaroth", x = 2925, z = 5330, height = 2, walkRadius = 3)

        setCombatDef("npc.kril_tsutsaroth") {
            configs {
                attackSpeed = 4  // Attack speed (ticks)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 255  // OSRS hitpoints
                attack = 260
                strength = 280
                defence = 240
                magic = 250  // Has magic for splash attack
                ranged = 1  // Very low ranged (doesn't use ranged)
            }

            bonuses {
                attackStab = 150
                attackSlash = 150
                attackCrush = 160
                attackMagic = 200
                attackRanged = 0

                defenceStab = 180
                defenceSlash = 180
                defenceCrush = 190
                defenceMagic = 120
                defenceRanged = 160

                attackBonus = 180
                strengthBonus = 220
                rangedStrengthBonus = 0
                magicDamageBonus = 200
            }

            anims {
                block = 6947  // KRIL_TSUTSAROTH_DEFEND
                death = 6949  // KRIL_TSUTSAROTH_DEATH
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
                    // Zamorak signature uniques (spread across tables)
                    add("item.zamorakian_spear", min = 1, weight = 30)  // ~1.2% (weight 30/2500)
                    add("item.zamorak_hilt", min = 1, weight = 50)      // ~2% (weight 50/2500) - rarest unique
                    add("item.staff_of_the_dead", min = 1, weight = 30)  // ~1.2% (weight 30/2500)
                    
                    // Zamorak godsword variants
                    add("item.zamorak_godsword", min = 1, weight = 40)        // ~1.6% (weight 40/2500)
                    add("item.zamorak_godsword_or", min = 1, weight = 25)     // ~1% (weight 25/2500)
                    
                    // Zamorak staff and cape
                    add("item.zamorak_staff", min = 1, weight = 25)      // ~1% (weight 25/2500)
                    add("item.zamorak_cape", min = 1, weight = 20)       // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_cape", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_max_cape", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_max_cape_l", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_cape_deadman", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_cape_23605", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Unholy book and related
                    add("item.unholy_book", min = 1, weight = 25)      // ~1% (weight 25/2500)
                    add("item.unholy_book_page_set", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.unholy_book_or", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.unholy_book_27191", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.unholy_blessing", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.unholy_symbol", min = 1, weight = 15)    // ~0.6% (weight 15/2500)
                    add("item.unholy_symbol_4683", min = 1, weight = 15) // ~0.6% (weight 15/2500)
                    add("item.unpowered_symbol", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    
                    // Zamorak robes
                    add("item.zamorak_robe_top", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    add("item.zamorak_robe_legs", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    add("item.zamorak_monk_top", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    add("item.zamorak_monk_bottom", min = 1, weight = 15) // ~0.6% (weight 15/2500)
                    
                    // Elder chaos druid robes
                    add("item.elder_chaos_top", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_robe", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_hood", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_top_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_robe_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_hood_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_top_27174", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_robe_27175", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.elder_chaos_hood_27176", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Dagon'hai robes
                    add("item.dagonhai_hat", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.dagonhai_robe_top", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.dagonhai_robe_bottom", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.dagonhai_robes_set", min = 1, weight = 15) // ~0.6% (weight 15/2500)
                    add("item.dagonhai_hat_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.dagonhai_robe_top_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.dagonhai_robe_bottom_or", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Zamorak rune armour
                    add("item.zamorak_platebody", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.zamorak_platelegs", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_plateskirt", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_full_helm", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.zamorak_kiteshield", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_armour_set_lg", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    add("item.zamorak_armour_set_sk", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    
                    // Rune scimitar (Zamorak)
                    add("item.rune_scimitar_ornament_kit_zamorak", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Zamorak dragonhide armour
                    add("item.zamorak_dhide_body", min = 1, weight = 20)  // ~0.8% (weight 20/2500)
                    add("item.zamorak_chaps", min = 1, weight = 20)      // ~0.8% (weight 20/2500)
                    add("item.zamorak_coif", min = 1, weight = 20)       // ~0.8% (weight 20/2500)
                    add("item.zamorak_dhide_boots", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_dhide_shield", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_dragonhide_set", min = 1, weight = 15) // ~0.6% (weight 15/2500)
                    add("item.zamorak_chaps_27181", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Zamorak vestment robes
                    add("item.zamorak_robe_top", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    add("item.zamorak_robe_legs", min = 1, weight = 15)  // ~0.6% (weight 15/2500)
                    add("item.zamorak_stole", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.zamorak_mitre", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.zamorak_cloak", min = 1, weight = 15)      // ~0.6% (weight 15/2500)
                    add("item.zamorak_crozier", min = 1, weight = 15)    // ~0.6% (weight 15/2500)
                    
                    // Zamorakian spear variants
                    add("item.zamorakian_spear", min = 1, weight = 30)  // ~1.2% (weight 30/2500)
                    add("item.zamorakian_hasta", min = 1, weight = 30)  // ~1.2% (weight 30/2500)
                    
                    // Inquisitor's armour
                    add("item.inquisitors_mace", min = 1, weight = 35)  // ~1.4% (weight 35/2500)
                    add("item.inquisitors_great_helm", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_hauberk", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_plateskirt", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_armour_set", min = 1, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.inquisitors_great_helm_27195", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_hauberk_27196", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_plateskirt_27197", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    add("item.inquisitors_mace_27198", min = 1, weight = 35) // ~1.4% (weight 35/2500)
                    
                    // Staff of the dead variants
                    add("item.staff_of_the_dead", min = 1, weight = 30)      // ~1.2% (weight 30/2500)
                    add("item.toxic_staff_of_the_dead", min = 1, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.staff_of_the_dead_23613", min = 1, weight = 30) // ~1.2% (weight 30/2500) - light/balance variant
                    
                    // Thammaron's sceptre variants
                    add("item.thammarons_sceptre", min = 1, weight = 40)       // ~1.6% (weight 40/2500)
                    add("item.thammarons_sceptre_u", min = 1, weight = 40)      // ~1.6% (weight 40/2500)
                    add("item.thammarons_sceptre_au", min = 1, weight = 40)     // ~1.6% (weight 40/2500) - autocast
                    add("item.thammarons_sceptre_a", min = 1, weight = 40)      // ~1.6% (weight 40/2500) - accursed
                    
                    // Viggora's chainmace variants
                    add("item.viggoras_chainmace", min = 1, weight = 40)       // ~1.6% (weight 40/2500)
                    add("item.viggoras_chainmace_u", min = 1, weight = 40)     // ~1.6% (weight 40/2500) - ursine
                    
                    // Dragon hunter lance
                    add("item.dragon_hunter_lance", min = 1, weight = 40)       // ~1.6% (weight 40/2500)
                    
                    // Elite black armour
                    add("item.elite_black_full_helm", min = 1, weight = 25)  // ~1% (weight 25/2500)
                    add("item.elite_black_platebody", min = 1, weight = 25) // ~1% (weight 25/2500)
                    add("item.elite_black_platelegs", min = 1, weight = 25) // ~1% (weight 25/2500)
                    
                    // Dark Squall robes
                    add("item.dark_squall_hood", min = 1, weight = 25)  // ~1% (weight 25/2500)
                    add("item.dark_squall_robe_top", min = 1, weight = 25) // ~1% (weight 25/2500)
                    add("item.dark_squall_robe_bottom", min = 1, weight = 25) // ~1% (weight 25/2500)
                    
                    // Other Zamorak items
                    add("item.zamorak_bracers", min = 1, weight = 15)   // ~0.6% (weight 15/2500)
                    add("item.zamorak_mjolnir", min = 1, weight = 25)      // ~1% (weight 25/2500)
                    add("item.zamorak_banner", min = 1, weight = 20)      // ~0.8% (weight 20/2500)
                    add("item.zamorak_banner_11892", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamorak_halo", min = 1, weight = 30)      // ~1.2% (weight 30/2500)
                    add("item.zamorak_halo_l", min = 1, weight = 30)     // ~1.2% (weight 30/2500)
                    add("item.zamorak_halo_27164", min = 1, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.zamorak_max_cape", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.zamorak_max_hood", min = 1, weight = 20)   // ~0.8% (weight 20/2500)
                    add("item.imbued_zamorak_max_hood", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Wine of Zamorak
                    add("item.wine_of_zamorak", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.wine_of_zamorak_23489", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    add("item.zamoraks_grapes", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.zamoraks_unfermented_wine", min = 1, weight = 20) // ~0.8% (weight 20/2500)
                    
                    // Zamorak brew
                    add("item.zamorak_brew4", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.zamorak_brew3", min = 1, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.zamorak_brew2", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    add("item.zamorak_brew1", min = 1, weight = 20)    // ~0.8% (weight 20/2500)
                    
                    // Regular drops
                    add("item.coins_995", min = 1000000, max = 3000000, weight = 30) // ~1.2% (weight 30/2500)
                    add("item.death_rune", min = 500, max = 1000, weight = 20)     // ~0.8% (weight 20/2500)
                    add("item.blood_rune", min = 500, max = 1000, weight = 20)     // ~0.8% (weight 20/2500)
                }

                // DROP TABLE 2: PRE-ROLL TABLE
                // Rolls before main table. If it hits, main doesn't roll.
                // Low weight items so main still rolls most of the time
                // Total item weights = 3875, so set weight to 5000 for proper validation (with buffer)
                preroll(weight = 5000) {
                    // Zamorak signature uniques (spread across tables)
                    add("item.zamorakian_spear", min = 1, weight = 128)  // ~0.78% chance (1/128)
                    add("item.zamorak_hilt", min = 1, weight = 256)      // ~0.39% chance (1/256) - rarest
                    add("item.staff_of_the_dead", min = 1, weight = 128) // ~0.78% chance (1/128)
                    
                    // Zamorak godsword variants
                    add("item.zamorak_godsword", min = 1, weight = 100)        // ~1% chance (1/100)
                    add("item.zamorak_godsword_or", min = 1, weight = 150)     // ~0.67% chance (1/150)
                    
                    // Very rare weapons
                    add("item.holy_scythe_of_vitur", min = 1, weight = 500)     // ~0.2% chance (1/500)
                    add("item.holy_ghrazi_rapier", min = 1, weight = 500)       // ~0.2% chance (1/500)
                    add("item.holy_sanguinesti_staff", min = 1, weight = 500)   // ~0.2% chance (1/500)
                    
                    // Inquisitor's armour (very rare)
                    add("item.inquisitors_mace", min = 1, weight = 300)  // ~0.33% chance (1/300)
                    add("item.inquisitors_great_helm", min = 1, weight = 300) // ~0.33% chance (1/300)
                    add("item.inquisitors_hauberk", min = 1, weight = 300) // ~0.33% chance (1/300)
                    add("item.inquisitors_plateskirt", min = 1, weight = 300) // ~0.33% chance (1/300)
                    
                    // Thammaron's sceptre and Viggora's chainmace
                    add("item.thammarons_sceptre", min = 1, weight = 250)       // ~0.4% chance (1/250)
                    add("item.viggoras_chainmace", min = 1, weight = 250)       // ~0.4% chance (1/250)
                    
                    // Dragon hunter lance
                    add("item.dragon_hunter_lance", min = 1, weight = 300)       // ~0.33% chance (1/300)
                    
                    // Zamorak max cape variants
                    add("item.zamorak_max_cape", min = 1, weight = 150)   // ~0.67% chance (1/150)
                    add("item.imbued_zamorak_max_cape", min = 1, weight = 150) // ~0.67% chance (1/150)
                }

                // DROP TABLE 3: TERTIARY TABLE
                // Can roll multiple items independently (each item rolls separately)
                // This table always rolls, each item has its own chance
                // Total item weights = 3875, so set weight to 4000 for proper validation (with buffer)
                tertiary(weight = 4000) {
                    // Zamorak signature uniques (spread across tables)
                    add("item.zamorakian_spear", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    add("item.zamorak_hilt", min = 1, weight = 400)      // ~0.25% chance per kill (1/400) - rarest
                    add("item.staff_of_the_dead", min = 1, weight = 200) // ~0.5% chance per kill (1/200)
                    
                    // Zamorak godsword variants
                    add("item.zamorak_godsword", min = 1, weight = 150)        // ~0.67% chance per kill (1/150)
                    add("item.zamorak_godsword_or", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    
                    // Zamorak staff and cape
                    add("item.zamorak_staff", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.zamorak_cape", min = 1, weight = 100)       // ~1% chance per kill (1/100)
                    add("item.imbued_zamorak_cape", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Unholy book and related
                    add("item.unholy_book", min = 1, weight = 120)      // ~0.83% chance per kill (1/120)
                    add("item.unholy_book_page_set", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.unholy_blessing", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.unholy_symbol", min = 1, weight = 80)     // ~1.25% chance per kill (1/80)
                    add("item.unpowered_symbol", min = 1, weight = 80)  // ~1.25% chance per kill (1/80)
                    
                    // Zamorak robes
                    add("item.zamorak_robe_top", min = 1, weight = 80)   // ~1.25% chance per kill (1/80)
                    add("item.zamorak_robe_legs", min = 1, weight = 80)  // ~1.25% chance per kill (1/80)
                    
                    // Elder chaos druid robes
                    add("item.elder_chaos_top", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.elder_chaos_robe", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.elder_chaos_hood", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    
                    // Dagon'hai robes
                    add("item.dagonhai_hat", min = 1, weight = 100)   // ~1% chance per kill (1/100)
                    add("item.dagonhai_robe_top", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.dagonhai_robe_bottom", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Zamorak rune armour
                    add("item.zamorak_platebody", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.zamorak_platelegs", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.zamorak_plateskirt", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    add("item.zamorak_full_helm", min = 1, weight = 100)  // ~1% chance per kill (1/100)
                    add("item.zamorak_kiteshield", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Rune scimitar (Zamorak)
                    add("item.rune_scimitar_ornament_kit_zamorak", min = 1, weight = 100) // ~1% chance per kill (1/100)
                    
                    // Zamorak dragonhide armour
                    add("item.zamorak_dhide_body", min = 1, weight = 90)  // ~1.11% chance per kill (1/90)
                    add("item.zamorak_chaps", min = 1, weight = 90)     // ~1.11% chance per kill (1/90)
                    add("item.zamorak_coif", min = 1, weight = 90)       // ~1.11% chance per kill (1/90)
                    add("item.zamorak_dhide_boots", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    add("item.zamorak_dhide_shield", min = 1, weight = 90) // ~1.11% chance per kill (1/90)
                    
                    // Zamorak vestment robes
                    add("item.zamorak_robe_top", min = 1, weight = 110)   // ~0.91% chance per kill (1/110)
                    add("item.zamorak_robe_legs", min = 1, weight = 110)  // ~0.91% chance per kill (1/110)
                    add("item.zamorak_stole", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.zamorak_mitre", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.zamorak_cloak", min = 1, weight = 110)      // ~0.91% chance per kill (1/110)
                    add("item.zamorak_crozier", min = 1, weight = 110)    // ~0.91% chance per kill (1/110)
                    
                    // Zamorakian spear variants
                    add("item.zamorakian_spear", min = 1, weight = 150)  // ~0.67% chance per kill (1/150)
                    add("item.zamorakian_hasta", min = 1, weight = 150)  // ~0.67% chance per kill (1/150)
                    
                    // Inquisitor's armour
                    add("item.inquisitors_mace", min = 1, weight = 300)  // ~0.33% chance per kill (1/300)
                    add("item.inquisitors_great_helm", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.inquisitors_hauberk", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    add("item.inquisitors_plateskirt", min = 1, weight = 300) // ~0.33% chance per kill (1/300)
                    
                    // Staff of the dead variants
                    add("item.staff_of_the_dead", min = 1, weight = 180)      // ~0.56% chance per kill (1/180)
                    add("item.toxic_staff_of_the_dead", min = 1, weight = 180) // ~0.56% chance per kill (1/180)
                    add("item.staff_of_the_dead_23613", min = 1, weight = 180) // ~0.56% chance per kill (1/180)
                    
                    // Thammaron's sceptre variants
                    add("item.thammarons_sceptre", min = 1, weight = 200)       // ~0.5% chance per kill (1/200)
                    add("item.thammarons_sceptre_u", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    add("item.thammarons_sceptre_au", min = 1, weight = 200)     // ~0.5% chance per kill (1/200)
                    add("item.thammarons_sceptre_a", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    
                    // Viggora's chainmace variants
                    add("item.viggoras_chainmace", min = 1, weight = 200)       // ~0.5% chance per kill (1/200)
                    add("item.viggoras_chainmace_u", min = 1, weight = 200)     // ~0.5% chance per kill (1/200)
                    
                    // Dragon hunter lance
                    add("item.dragon_hunter_lance", min = 1, weight = 250)       // ~0.4% chance per kill (1/250)
                    
                    // Elite black armour
                    add("item.elite_black_full_helm", min = 1, weight = 120)  // ~0.83% chance per kill (1/120)
                    add("item.elite_black_platebody", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    add("item.elite_black_platelegs", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    
                    // Dark Squall robes
                    add("item.dark_squall_hood", min = 1, weight = 120)  // ~0.83% chance per kill (1/120)
                    add("item.dark_squall_robe_top", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    add("item.dark_squall_robe_bottom", min = 1, weight = 120) // ~0.83% chance per kill (1/120)
                    
                    // Other Zamorak items
                    add("item.zamorak_bracers", min = 1, weight = 105)   // ~0.95% chance per kill (1/105)
                    add("item.zamorak_mjolnir", min = 1, weight = 140)     // ~0.71% chance per kill (1/140)
                    add("item.zamorak_banner", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.zamorak_halo", min = 1, weight = 180)     // ~0.56% chance per kill (1/180)
                    add("item.zamorak_max_cape", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    add("item.zamorak_max_hood", min = 1, weight = 150)   // ~0.67% chance per kill (1/150)
                    
                    // Wine of Zamorak
                    add("item.wine_of_zamorak", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    add("item.zamoraks_grapes", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    
                    // Zamorak brew
                    add("item.zamorak_brew4", min = 1, weight = 100)    // ~1% chance per kill (1/100)
                    
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

