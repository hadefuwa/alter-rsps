package org.alter.plugins.content.npcs.vardorvis

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
 * Vardorvis Boss Configuration Plugin
 * 
 * Vardorvis is one of the four Ancient Warriors bosses from Desert Treasure II.
 * This is a high-level boss encounter with significant hitpoints and combat stats.
 * 
 * Location: 1130, 3419
 * Combat Level: 700+
 * Hitpoints: 1500+
 */
class VardorvisConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Spawn Vardorvis at the boss location
        spawnNpc("npc.vardorvis", x = 1130, z = 3419, height = 0, walkRadius = 5)

        setCombatDef("npc.vardorvis") {
            configs {
                attackSpeed = 4
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 1500  // High HP for a challenging boss encounter
                attack = 380
                strength = 380
                defence = 380
                magic = 380
                ranged = 380
            }

            bonuses {
                attackStab = 280
                attackSlash = 280
                attackCrush = 280
                attackMagic = 330
                attackRanged = 330

                defenceStab = 280
                defenceSlash = 280
                defenceCrush = 280
                defenceMagic = 300
                defenceRanged = 300

                attackBonus = 280
                strengthBonus = 330
                rangedStrengthBonus = 330
                magicDamageBonus = 380
            }

            anims {
                attack = 422  // Magic attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    // High-value boss drops
                    add("item.coins_995", min = 40000, max = 80000, weight = 30)
                    
                    // Weapons and armour
                    add("item.dragon_scimitar", min = 1, weight = 2)
                    add("item.dragon_longsword", min = 1, weight = 2)
                    add("item.dragon_battleaxe", min = 1, weight = 1)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_chainbody", min = 1, weight = 1)
                    add("item.dragon_platelegs", min = 1, weight = 1)
                    add("item.dragon_plateskirt", min = 1, weight = 1)
                    add("item.dragon_sq_shield", min = 1, weight = 1)
                    add("item.dragon_kiteshield", min = 1, weight = 1)
                    add("item.dragon_full_helm", min = 1, weight = 1)
                    add("item.dragon_med_helm", min = 1, weight = 1)
                    
                    // Ranged items
                    add("item.dragon_crossbow", min = 1, weight = 1)
                    add("item.dragon_arrow", min = 100, max = 500, weight = 5)
                    add("item.dragonstone_bolts_e", min = 50, max = 200, weight = 3)
                    
                    // Magic items
                    add("item.ahrims_staff", min = 1, weight = 1)
                    add("item.mystic_robe_top", min = 1, weight = 2)
                    add("item.mystic_robe_bottom", min = 1, weight = 2)
                    
                    // Runes
                    add("item.death_rune", min = 100, max = 500, weight = 10)
                    add("item.blood_rune", min = 50, max = 250, weight = 8)
                    add("item.soul_rune", min = 50, max = 250, weight = 8)
                    add("item.nature_rune", min = 100, max = 500, weight = 10)
                    add("item.law_rune", min = 50, max = 250, weight = 8)
                    add("item.chaos_rune", min = 200, max = 1000, weight = 12)
                    
                    // Food and potions
                    add("item.shark", min = 5, max = 15, weight = 8)
                    add("item.manta_ray", min = 3, max = 10, weight = 5)
                    add("item.saradomin_brew4", min = 5, max = 15, weight = 10)
                    add("item.super_restore4", min = 5, max = 15, weight = 10)
                    add("item.super_combat_potion4", min = 3, max = 10, weight = 8)
                    add("item.prayer_potion4", min = 5, max = 15, weight = 10)
                    
                    // Valuable resources
                    add("item.uncut_diamond", min = 10, max = 30, weight = 5)
                    add("item.uncut_ruby", min = 15, max = 40, weight = 6)
                    add("item.uncut_emerald", min = 20, max = 50, weight = 7)
                    add("item.uncut_sapphire", min = 25, max = 60, weight = 8)
                    add("item.gold_ore", min = 200, max = 500, weight = 10)
                    add("item.coal", min = 300, max = 600, weight = 12)
                    add("item.runite_ore", min = 10, max = 50, weight = 3)
                    
                    // Clue scrolls
                    add("item.clue_scroll_elite", min = 1, weight = 5)
                    add("item.clue_scroll_master", min = 1, weight = 2)
                    
                    // Seeds and herbs
                    add("item.ranarr_seed", min = 5, max = 15, weight = 6)
                    add("item.snapdragon_seed", min = 3, max = 10, weight = 4)
                    add("item.torstol_seed", min = 2, max = 8, weight = 2)
                    add("item.grimy_ranarr_weed", min = 10, max = 30, weight = 8)
                    add("item.grimy_snapdragon", min = 5, max = 20, weight = 6)
                    add("item.grimy_torstol", min = 3, max = 15, weight = 4)
                    
                    // Rare drops
                    add("item.onyx", min = 1, weight = 1)
                    add("item.dragon_claws", min = 1, weight = 1)
                    add("item.vardorvis_medallion", min = 1, weight = 1) // Vardorvis unique drop
                }
            }
        }
    }
}


