package org.alter.plugins.content.npcs.corporealbeast

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Corporeal Beast Configuration Plugin
 * 
 * Configures the Corporeal Beast's combat stats, drops, and behavior.
 * The Corporeal Beast is a high-level boss requiring team coordination.
 */
class CorporealBeastConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatDef("npc.corporeal_beast") {
            configs {
                attackSpeed = 4 // Attack speed (ticks)
                respawnDelay = 50 // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 15 // Large aggro radius
                searchDelay = 1
            }

            stats {
                hitpoints = 2000 // High HP - requires team effort
                attack = 300
                strength = 350
                defence = 280
                magic = 320
                ranged = 250
            }

            bonuses {
                attackStab = 200
                attackSlash = 200
                attackCrush = 200
                attackMagic = 250
                attackRanged = 200

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 180
                defenceRanged = 200

                attackBonus = 200
                strengthBonus = 250
                rangedStrengthBonus = 200
                magicDamageBonus = 300
            }

            anims {
                block = 1683 // Block animation
                death = 1684 // Death animation
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.coins_995", min = 1000000, max = 1000000) // Guaranteed 1M coins
                }

                main(weight = 200) {
                    // High-value equipment
                    add("item.dragon_med_helm", min = 1, weight = 5)
                    add("item.dragon_dagger", min = 1, weight = 5)
                    add("item.dragon_longsword", min = 1, weight = 5)
                    add("item.dragon_scimitar", min = 1, weight = 5)
                    
                    // Rune equipment
                    add("item.rune_full_helm", min = 1, weight = 10)
                    add("item.rune_platebody", min = 1, weight = 10)
                    add("item.rune_platelegs", min = 1, weight = 10)
                    add("item.rune_kiteshield", min = 1, weight = 10)
                    
                    // Coins
                    add("item.coins_995", min = 1000000, max = 5000000, weight = 30)
                    
                    // Runes
                    add("item.death_rune", min = 1000, max = 1000, weight = 20)
                    add("item.blood_rune", min = 1000, max = 1000, weight = 20)
                    add("item.soul_rune", min = 500, max = 500, weight = 15)
                    
                    // Resources
                    add("item.grimy_ranarr_weed", min = 10, max = 20, weight = 15)
                    add("item.grimy_snapdragon", min = 5, max = 15, weight = 12)
                    add("item.grimy_torstol", min = 3, max = 10, weight = 10)
                    
                    // Bars and ores
                    add("item.adamantite_bar", min = 20, max = 40, weight = 15)
                    add("item.runite_ore", min = 10, max = 20, weight = 12)
                    
                    // Food
                    add("item.shark", min = 20, max = 30, weight = 15)
                    add("item.super_restore4", min = 10, max = 20, weight = 12)
                    add("item.prayer_potion4", min = 5, max = 10, weight = 10)
                }

                // Rare drop table - Corporeal Beast signature drops
                tertiary(weight = 2000) {
                    // Base Spirit Shields - More common
                    add("item.spirit_shield", min = 1, weight = 10) // 1/10 chance
                    add("item.blessed_spirit_shield", min = 1, weight = 20) // 1/20 chance
                    
                    // Spirit Shields - Signature drops from Corporeal Beast
                    add("item.elysian_spirit_shield", min = 1, weight = 50) // 1/50 chance
                    add("item.spectral_spirit_shield", min = 1, weight = 50) // 1/50 chance
                    add("item.arcane_spirit_shield", min = 1, weight = 50) // 1/50 chance
                    
                    // Holy Elixir - Used to create blessed spirit shield
                    add("item.holy_elixir", min = 1, weight = 50) // 1/50 chance
                    
                    // Other rare drops
                    add("item.onyx", min = 1, weight = 50) // 1/50 chance
                    add("item.uncut_onyx", min = 1, weight = 50) // 1/50 chance
                    
                    // Very rare drops
                    add("item.dragon_spear", min = 1, weight = 25) // 1/25 chance
                    add("item.dragon_platelegs", min = 1, weight = 30) // 1/30 chance
                    add("item.dragon_plateskirt", min = 1, weight = 30) // 1/30 chance
                    add("item.dragon_boots", min = 1, weight = 40) // 1/40 chance
                    
                    // Key halves
                    add("item.loop_half_of_key", min = 1, weight = 10) // 1/10 chance
                    add("item.tooth_half_of_key", min = 1, weight = 10) // 1/10 chance
                    add("item.shield_left_half", min = 1, weight = 5) // 1/5 chance
                    
                    // Extremely rare
                    add("item.draconic_visage", min = 1, weight = 1000) // 1/1000 chance
                }
            }
        }
    }
}

