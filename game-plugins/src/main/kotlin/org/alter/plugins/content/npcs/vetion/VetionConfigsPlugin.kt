package org.alter.plugins.content.npcs.vetion

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

class VetionConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 12342) // Wilderness region around Vet'ion location

        // Spawn Vet'ion at Bone Yard in wilderness  
        spawnNpc("npc.vetion", x = 3229, z = 3788, walkRadius = 3)

        setCombatDef("npc.vetion") {
            configs {
                attackSpeed = 4
                respawnDelay = 120 // 2 minute respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 255
                attack = 270
                strength = 250
                defence = 270
                magic = 200
                ranged = 200
            }

            bonuses {
                defenceStab = 65
                defenceSlash = 65
                defenceCrush = 65
                defenceMagic = 40
                defenceRanged = 65
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
            }

            anims {
                attack = 5485 // Vet'ion attack animation
                block = 5489
                death = 5487
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    // Vet'ion signature drops
                    add("item.skull_of_vetion", min = 1, weight = 1) // Ultra rare signature drop
                    
                    // Dragon items (rare)
                    add("item.dragon_pickaxe", min = 1, weight = 1)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_chainbody", min = 1, weight = 2)
                    add("item.dragon_longsword", min = 1, weight = 2)
                    add("item.dragon_battleaxe", min = 1, weight = 2)
                    
                    // Rune equipment (uncommon)
                    add("item.rune_platebody", min = 1, weight = 4)
                    add("item.rune_platelegs", min = 1, weight = 4)
                    add("item.rune_kiteshield", min = 1, weight = 4)
                    add("item.rune_full_helm", min = 1, weight = 4)
                    add("item.rune_scimitar", min = 1, weight = 5)
                    add("item.rune_longsword", min = 1, weight = 5)
                    add("item.rune_battleaxe", min = 1, weight = 4)
                    add("item.rune_2h_sword", min = 1, weight = 3)
                    
                    // Potions and food
                    add("item.shark", min = 5, max = 10, weight = 15)
                    add("item.prayer_potion4", min = 2, max = 4, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 3, weight = 6)
                    add("item.saradomin_brew4", min = 2, max = 4, weight = 7)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    
                    // Ammunition and supplies
                    add("item.rune_arrow", min = 150, max = 300, weight = 10)
                    add("item.runite_bolts", min = 25, max = 50, weight = 8)
                    add("item.cannonball", min = 100, max = 200, weight = 12)
                    
                    // Runes (death/bone themed)
                    add("item.death_rune", min = 50, max = 100, weight = 12)
                    add("item.blood_rune", min = 25, max = 50, weight = 10)
                    add("item.soul_rune", min = 20, max = 40, weight = 8)
                    add("item.wrath_rune", min = 15, max = 30, weight = 6)
                    add("item.chaos_rune", min = 75, max = 150, weight = 15)
                    add("item.nature_rune", min = 40, max = 80, weight = 12)
                    
                    // Valuable items and coins
                    add("item.coins_995", min = 15000, max = 35000, weight = 20)
                    add("item.uncut_diamond", min = 2, max = 5, weight = 4)
                    add("item.uncut_dragonstone", min = 1, max = 2, weight = 2)
                    add("item.gold_ore", min = 75, max = 150, weight = 10)
                    
                    // Clue scrolls
                    add("item.clue_scroll_hard", min = 1, weight = 3)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                    
                    // Bones and skeletal items (thematic)
                    add("item.dragon_bones", min = 5, max = 15, weight = 8)
                    add("item.wyvern_bones", min = 3, max = 8, weight = 6)
                }
            }
        }
    }
}