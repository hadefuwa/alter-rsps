package org.alter.plugins.content.npcs.callisto

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
 * @author Alycia <https://github.com/alycii>
 * Callisto - The Bear Wilderness Boss
 * Combat Level: 470
 * Hitpoints: 1000
 * Location: Callisto's Den (3294, 3846)
 * Region: 13123 (Multi-combat)
 * 
 * Special Attacks:
 * - Shockwave Attack (AoE damage)
 * - Bear Swipe (High melee damage)
 * - Ground Slam (Traps players)
 * - Roar (Fear effect)
 */

class CallistoConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 13123) // Callisto's Den wilderness region

        // Spawn Callisto at Callisto's Den location in wilderness
        spawnNpc("npc.callisto", x = 3294, z = 3846, walkRadius = 3)

        setCombatDef("npc.callisto") {
            configs {
                attackSpeed = 4
                respawnDelay = 30 // 30 seconds respawn delay (reduced from 100)
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 255
                attack = 300  // Reduced for less accuracy
                strength = 250  // Reduced for less damage
                defence = 250
                magic = 1
                ranged = 1
            }

            bonuses {
                defenceStab = 150
                defenceSlash = 200
                defenceCrush = -50  // Weak to crush attacks (negative = takes more damage)
                defenceMagic = 100
                defenceRanged = 150
                attackStab = 0
                attackSlash = 150  // Reduced for less accuracy
                attackCrush = 150  // Reduced for less accuracy
                attackMagic = 0
                attackRanged = 0
                strengthBonus = 75  // Reduced strength bonus for less damage
            }

            anims {
                attack = 4925 // Bear attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.larrans_key", min = 1, max = 3)
                }
                
                main(weight = 128) {
                    // Weapons and armour - high value wilderness boss drops
                    add("item.dragon_pickaxe", min = 1, weight = 1) // Signature drop
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.rune_pickaxe", min = 1, weight = 3)
                    add("item.rune_2h_sword", min = 1, weight = 2)
                    add("item.rune_warhammer", min = 1, weight = 2)
                    add("item.rune_battleaxe", min = 1, weight = 2)
                    add("item.rune_longsword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 3)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_kiteshield", min = 1, weight = 2)
                    add("item.rune_full_helm", min = 1, weight = 3)
                    
                    // Ranged items
                    add("item.rune_crossbow", min = 1, weight = 2)
                    add("item.magic_longbow", min = 1, weight = 3)
                    add("item.rune_arrow", min = 200, max = 400, weight = 8)
                    add("item.runite_bolts", min = 25, max = 50, weight = 5)
                    add("item.adamant_bolts", min = 100, max = 200, weight = 10)
                    
                    // Runes and magic supplies
                    add("item.death_rune", min = 50, max = 100, weight = 8)
                    add("item.blood_rune", min = 25, max = 50, weight = 6)
                    add("item.nature_rune", min = 50, max = 100, weight = 8)
                    add("item.law_rune", min = 25, max = 50, weight = 6)
                    add("item.cosmic_rune", min = 30, max = 60, weight = 7)
                    add("item.chaos_rune", min = 100, max = 200, weight = 10)
                    
                    // Food and potions
                    add("item.shark", min = 1, max = 3, weight = 5)
                    add("item.monkfish", min = 1, max = 3, weight = 4)
                    add("item.prayer_potion4", min = 2, max = 5, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 3, weight = 5)
                    add("item.saradomin_brew4", min = 2, max = 4, weight = 6)
                    add("item.super_restore4", min = 1, max = 3, weight = 5)
                    add("item.super_strength4", min = 1, max = 2, weight = 4)
                    add("item.super_attack4", min = 1, max = 2, weight = 4)
                    add("item.super_defence4", min = 1, max = 2, weight = 4)
                    
                    // Valuable items and resources
                    add("item.coins_995", min = 10000, max = 25000, weight = 20)
                    add("item.uncut_diamond", min = 2, max = 5, weight = 3)
                    add("item.uncut_ruby", min = 3, max = 8, weight = 5)
                    add("item.uncut_emerald", min = 5, max = 12, weight = 7)
                    add("item.uncut_sapphire", min = 8, max = 15, weight = 8)
                    add("item.gold_ore", min = 100, max = 200, weight = 8)
                    add("item.coal", min = 150, max = 300, weight = 10)
                    add("item.iron_ore", min = 200, max = 400, weight = 12)
                    
                    // Clue scrolls
                    add("item.clue_scroll_hard", min = 1, weight = 5)
                    add("item.clue_scroll_elite", min = 1, weight = 2)
                    
                    // Rare/unique drops
                    add("item.tyrannical_ring", min = 1, weight = 1) // Callisto's unique ring (very rare)
                    add("item.dragon_med_helm", min = 1, weight = 1) // Very rare
                    
                    // Seeds and herbs
                    add("item.ranarr_seed", min = 2, max = 5, weight = 4)
                    add("item.snapdragon_seed", min = 1, max = 3, weight = 2)
                    add("item.torstol_seed", min = 1, max = 2, weight = 1)
                    add("item.grimy_ranarr_weed", min = 5, max = 10, weight = 6)
                    add("item.grimy_snapdragon", min = 3, max = 6, weight = 4)
                    add("item.grimy_torstol", min = 1, max = 3, weight = 2)
                }
            }
        }
    }
}
