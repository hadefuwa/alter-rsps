package org.alter.plugins.content.npcs.kreearra

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
 * Kree'arra Configuration
 *
 * Kree'arra is the leader of Armadyl's forces in the God Wars Dungeon.
 * He is located in Armadyl's Eyrie at coordinates 2839, 5293, height 2.
 * 
 * Combat:
 * - Uses ranged attacks (very high damage)
 * - Can use magic attacks
 * - Accompanied by 3 minions: Wingman Skree (melee), Flockleader Geerin (ranged), Flight Kilisa (melee)
 */
class KreeArraConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Kree'arra in Armadyl's Eyrie
        // Coordinates: 2832, 5302, height 2 (God Wars Dungeon)
        spawnNpc("npc.kreearra_3162", x = 2832, z = 5302, height = 2, walkRadius = 3)

        setCombatDef("npc.kreearra_3162") {
            configs {
                attackSpeed = 4  // Attack speed (ticks)
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 300  // Increased from 255 - much tankier
                attack = 1  // Very low attack (doesn't use melee)
                strength = 1  // Very low strength (doesn't use melee)
                defence = 280  // Increased from 240 - harder to hit
                magic = 280  // Increased from 250 - stronger magic attacks
                ranged = 320  // Increased from 280 - MUCH higher ranged (primary attack style)
            }

            bonuses {
                attackStab = 0
                attackSlash = 0
                attackCrush = 0
                attackMagic = 250  // Increased from 200
                attackRanged = 300  // Increased from 250 - much more accurate

                defenceStab = 200  // Increased from 180
                defenceSlash = 200  // Increased from 180
                defenceCrush = 210  // Increased from 190
                defenceMagic = 150  // Increased from 120
                defenceRanged = 180  // Increased from 160

                attackBonus = 0
                strengthBonus = 0
                rangedStrengthBonus = 300  // Increased from 250 - MUCH higher damage
                magicDamageBonus = 250  // Increased from 200
            }

            anims {
                block = 6978  // KREEARRA_DEFEND
                death = 6979  // KREEARRA_DEATH
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 500000, max = 500000) // Guaranteed 500k coins
                }

                // DROP TABLE 1: MAIN TABLE
                // Guaranteed drop from this table (weight = sum of item weights)
                // Total item weights = 446, so set weight to 446 for 100% drop chance
                main(weight = 446) {
                    // Armadyl equipment (signature drops) - OSRS rates: ~1/128 for uniques
                    add("item.armadyl_helmet", min = 1, weight = 50)  // ~11.2% (weight 50/446)
                    add("item.armadyl_chestplate", min = 1, weight = 50)  // ~11.2% (weight 50/446)
                    add("item.armadyl_chainskirt", min = 1, weight = 50)  // ~11.2% (weight 50/446)
                    add("item.armadyl_hilt", min = 1, weight = 100)     // ~22.4% (weight 100/446) - rarest unique
                    add("item.armadyl_crossbow", min = 1, weight = 50)  // ~11.2% (weight 50/446)
                    
                    // High-value equipment
                    add("item.dragon_med_helm", min = 1, weight = 5)  // ~1.1% (weight 5/446)
                    add("item.dragon_dagger", min = 1, weight = 5)   // ~1.1% (weight 5/446)
                    add("item.dragon_longsword", min = 1, weight = 5) // ~1.1% (weight 5/446)
                    add("item.dragon_scimitar", min = 1, weight = 5) // ~1.1% (weight 5/446)
                    
                    // Rune equipment
                    add("item.rune_full_helm", min = 1, weight = 10)  // ~2.2% (weight 10/446)
                    add("item.rune_platebody", min = 1, weight = 10) // ~2.2% (weight 10/446)
                    add("item.rune_platelegs", min = 1, weight = 10)  // ~2.2% (weight 10/446)
                    add("item.rune_kiteshield", min = 1, weight = 10) // ~2.2% (weight 10/446)
                    
                    // Coins
                    add("item.coins_995", min = 1000000, max = 3000000, weight = 30) // ~6.7% (weight 30/446)
                    
                    // Runes
                    add("item.death_rune", min = 500, max = 1000, weight = 20) // ~4.5% (weight 20/446)
                    add("item.blood_rune", min = 500, max = 1000, weight = 20) // ~4.5% (weight 20/446)
                    add("item.chaos_rune", min = 500, max = 1000, weight = 15)  // ~3.4% (weight 15/446)
                    
                    // Resources
                    add("item.grimy_ranarr_weed", min = 5, max = 15, weight = 15) // ~3.4% (weight 15/446)
                    add("item.grimy_snapdragon", min = 3, max = 10, weight = 12)  // ~2.7% (weight 12/446)
                    add("item.grimy_torstol", min = 2, max = 8, weight = 10)      // ~2.2% (weight 10/446)
                    
                    // Bars and ores
                    add("item.adamantite_bar", min = 10, max = 30, weight = 15) // ~3.4% (weight 15/446)
                    add("item.runite_ore", min = 5, max = 15, weight = 12)      // ~2.7% (weight 12/446)
                    
                    // Food
                    add("item.shark", min = 10, max = 20, weight = 15)           // ~3.4% (weight 15/446)
                    add("item.super_restore4", min = 5, max = 10, weight = 12)   // ~2.7% (weight 12/446)
                    add("item.prayer_potion4", min = 3, max = 8, weight = 10)    // ~2.2% (weight 10/446)
                }

                // DROP TABLE 2: PRE-ROLL TABLE
                // Rolls before main table. If it hits, main doesn't roll.
                // Low weight items so main still rolls most of the time
                // Total item weights = 768, so set weight to 1000 for proper validation (with buffer)
                preroll(weight = 1000) {
                    // Armadyl signature uniques (spread across tables)
                    add("item.armadyl_helmet", min = 1, weight = 128)  // ~0.78% chance (1/128)
                    add("item.armadyl_chestplate", min = 1, weight = 128)    // ~0.78% chance (1/128)
                    add("item.armadyl_chainskirt", min = 1, weight = 128)      // ~0.78% chance (1/128)
                    add("item.armadyl_hilt", min = 1, weight = 256)       // ~0.39% chance (1/256) - rarest
                    add("item.armadyl_crossbow", min = 1, weight = 128)  // ~0.78% chance (1/128)
                }

                // DROP TABLE 3: TERTIARY TABLE
                // Can roll multiple items independently (each item rolls separately)
                // This table always rolls, each item has its own chance
                // Total item weights = 2285, so set weight to 2500 for proper validation (with buffer)
                tertiary(weight = 2500) {
                    // Armadyl signature uniques (spread across tables)
                    add("item.armadyl_helmet", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    add("item.armadyl_chestplate", min = 1, weight = 200)    // ~0.5% chance per kill (1/200)
                    add("item.armadyl_chainskirt", min = 1, weight = 200)      // ~0.5% chance per kill (1/200)
                    add("item.armadyl_hilt", min = 1, weight = 400)       // ~0.25% chance per kill (1/400) - rarest
                    add("item.armadyl_crossbow", min = 1, weight = 200)  // ~0.5% chance per kill (1/200)
                    
                    // Rare drops
                    add("item.dragon_spear", min = 1, weight = 50)      // ~2% chance per kill (1/50)
                    add("item.dragon_platelegs", min = 1, weight = 60) // ~1.67% chance per kill (1/60)
                    add("item.dragon_plateskirt", min = 1, weight = 60) // ~1.67% chance per kill (1/60)
                    add("item.dragon_boots", min = 1, weight = 80)     // ~1.25% chance per kill (1/80)
                    
                    // Key halves
                    add("item.loop_half_of_key", min = 1, weight = 10)   // ~10% chance per kill (1/10)
                    add("item.tooth_half_of_key", min = 1, weight = 10)  // ~10% chance per kill (1/10)
                    add("item.shield_left_half", min = 1, weight = 5)    // ~20% chance per kill (1/5)
                    
                    // Extremely rare
                    add("item.draconic_visage", min = 1, weight = 1000)   // ~0.1% chance per kill (1/1000)
                }
            }
        }
    }
}

