package org.alter.plugins.content.npcs

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Generic NPC Combat Configuration Plugin
 * 
 * Configures combat stats and drop tables for NPCs that are used across multiple locations.
 * This file provides shared configurations for common NPCs found in various dungeons and areas.
 * 
 * NPCs configured:
 * - Giant Bat
 * - Ghost
 * - Black Knight
 * - Poison Scorpion
 * - Chaos Dwarf
 * - Lesser Demon
 * - Hill Giant
 * - Blue Dragon (also configured in DragonConfigsPlugin)
 * - Black Demon
 * - Black Dragon (also configured in DragonConfigsPlugin)
 * - Poison Spider
 * - Hellhound
 */
class GenericNpcCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // ======================
        // GIANT BAT
        // ======================
        setCombatDef("npc.giant_bat") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 15
            }
            
            bonuses {
                attackStab = 10
                strengthBonus = 8
                defenceStab = 8
                defenceSlash = 8
                defenceCrush = 8
                defenceMagic = 5
                defenceRanged = 8
            }
            
            anims {
                attack = 4915
                block = 4916
                death = 4917
            }
            
            aggro {
                radius = 5
                searchDelay = 3
            }
            
            drops {
                // How to change drop rates:
                // - Item weight: Increase = more likely, Decrease = less likely (weight 10 = twice as likely as weight 5)
                // - main(weight = X): YES, you can change this! 
                //   * Lower number (e.g., 50) = more likely to get something vs nothing
                //   * Higher number (e.g., 200) = less likely to get something vs nothing
                //   * Set it equal to sum of item weights = guaranteed drop (no empty rolls)
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 1, max = 10, weight = 40)
                    add("item.bat_bone", min = 1, weight = 30)
                    add("item.iron_arrow", min = 2, max = 8, weight = 20)
                    add("item.iron_dagger", min = 1, weight = 10)
                }
            }
        }
        
        // ======================
        // GHOST
        // ======================
        setCombatDef("npc.ghost") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 25
            }
            
            bonuses {
                attackStab = 15
                strengthBonus = 12
                defenceStab = 12
                defenceSlash = 12
                defenceCrush = 12
                defenceMagic = 20
                defenceRanged = 12
            }
            
            anims {
                attack = 5540
                block = 5541
                death = 5542
            }
            
            aggro {
                radius = 7
                searchDelay = 3
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 5, max = 25, weight = 35)
                    add("item.ghostspeak_amulet", min = 1, weight = 5)
                    add("item.iron_arrow", min = 5, max = 15, weight = 25)
                    add("item.iron_dagger", min = 1, weight = 15)
                    add("item.iron_sword", min = 1, weight = 10)
                    add("item.steel_dagger", min = 1, weight = 10)
                }
            }
        }
        
        // ======================
        // BLACK KNIGHT
        // ======================
        setCombatDef("npc.black_knight") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 50
                attack = 50
                strength = 50
                defence = 50
            }
            
            bonuses {
                attackStab = 30
                attackSlash = 30
                attackCrush = 30
                strengthBonus = 25
                defenceStab = 30
                defenceSlash = 30
                defenceCrush = 30
                defenceMagic = 15
                defenceRanged = 30
            }
            
            anims {
                attack = 407
                block = 1156
                death = 836
            }
            
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 20, max = 80, weight = 30)
                    add("item.iron_sword", min = 1, weight = 20)
                    add("item.steel_sword", min = 1, weight = 15)
                    add("item.iron_full_helm", min = 1, weight = 15)
                    add("item.iron_platebody", min = 1, weight = 12)
                    add("item.iron_platelegs", min = 1, weight = 12)
                    add("item.steel_dagger", min = 1, weight = 10)
                    add("item.steel_arrow", min = 10, max = 30, weight = 15)
                    add("item.chaos_rune", min = 2, max = 8, weight = 12)
                    add("item.death_rune", min = 1, max = 3, weight = 8)
                }
            }
        }
        
        // ======================
        // POISON SCORPION
        // ======================
        setCombatDef("npc.poison_scorpion") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 35
            }
            
            bonuses {
                attackStab = 20
                strengthBonus = 18
                defenceStab = 15
                defenceSlash = 15
                defenceCrush = 15
                defenceMagic = 10
                defenceRanged = 15
            }
            
            anims {
                attack = 6254
                block = 6255
                death = 6256
            }
            
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 10, max = 40, weight = 35)
                    add("item.iron_dagger", min = 1, weight = 20)
                    add("item.iron_sword", min = 1, weight = 15)
                    add("item.steel_dagger", min = 1, weight = 12)
                    add("item.iron_arrow", min = 5, max = 20, weight = 18)
                }
            }
        }
        
        // ======================
        // CHAOS DWARF
        // ======================
        setCombatDef("npc.chaos_dwarf") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 40
                attack = 40
                strength = 40
                defence = 40
            }
            
            bonuses {
                attackStab = 25
                attackSlash = 25
                attackCrush = 25
                strengthBonus = 22
                defenceStab = 25
                defenceSlash = 25
                defenceCrush = 25
                defenceMagic = 15
                defenceRanged = 25
            }
            
            anims {
                attack = 412
                block = 1156
                death = 836
            }
            
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 15, max = 60, weight = 30)
                    add("item.iron_pickaxe", min = 1, weight = 15)
                    add("item.steel_pickaxe", min = 1, weight = 10)
                    add("item.iron_bar", min = 1, max = 3, weight = 20)
                    add("item.steel_bar", min = 1, max = 2, weight = 15)
                    add("item.chaos_rune", min = 3, max = 10, weight = 18)
                    add("item.death_rune", min = 1, max = 5, weight = 12)
                }
            }
        }
        
        // ======================
        // LESSER DEMON
        // ======================
        setCombatDef("npc.lesser_demon") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 79
                attack = 75
                strength = 75
                defence = 75
            }
            
            bonuses {
                attackStab = 50
                attackSlash = 50
                attackCrush = 50
                strengthBonus = 45
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 30
                defenceRanged = 50
            }
            
            anims {
                attack = 64
                block = 65
                death = 67
            }
            
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.coins", min = 50, max = 200, weight = 25)
                    add("item.chaos_rune", min = 5, max = 15, weight = 20)
                    add("item.death_rune", min = 3, max = 10, weight = 18)
                    add("item.fire_rune", min = 20, max = 50, weight = 20)
                    add("item.steel_longsword", min = 1, weight = 12)
                    add("item.mithril_longsword", min = 1, weight = 8)
                    add("item.steel_full_helm", min = 1, weight = 10)
                    add("item.steel_platebody", min = 1, weight = 8)
                    add("item.steel_platelegs", min = 1, weight = 8)
                    add("item.adamant_dagger", min = 1, weight = 6)
                    add("item.rune_dagger", min = 1, weight = 3)
                }
            }
        }
        
        // ======================
        // HILL GIANT
        // ======================
        setCombatDef("npc.hill_giant") {
            configs {
                attackSpeed = 5
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 35
                attack = 30
                strength = 30
                defence = 30
            }
            
            bonuses {
                attackStab = 20
                attackSlash = 20
                attackCrush = 25
                strengthBonus = 22
                defenceStab = 20
                defenceSlash = 20
                defenceCrush = 20
                defenceMagic = 10
                defenceRanged = 20
            }
            
            anims {
                attack = 4652
                block = 4653
                death = 4654
            }
            
            aggro {
                radius = 7
                searchDelay = 3
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 15, max = 50, weight = 30)
                    add("item.big_bones", min = 1, weight = 5)
                    add("item.iron_arrow", min = 10, max = 30, weight = 25)
                    add("item.iron_dagger", min = 1, weight = 15)
                    add("item.iron_sword", min = 1, weight = 12)
                    add("item.steel_dagger", min = 1, weight = 10)
                    add("item.limpwurt_root", min = 1, weight = 8)
                    add("item.iron_full_helm", min = 1, weight = 10)
                }
            }
        }
        
        // ======================
        // BLUE DRAGON
        // ======================
        // NOTE: Blue dragons are configured in DragonConfigsPlugin.kt
        // Removing duplicate configuration to avoid "Npc combat definition has been previously set" error
        // If you need to modify blue dragon config, edit DragonConfigsPlugin.kt instead
        
        // ======================
        // BLACK DEMON
        // ======================
        setCombatDef("npc.black_demon") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 157
                attack = 120
                strength = 120
                defence = 120
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                strengthBonus = 75
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 50
                defenceRanged = 80
            }
            
            anims {
                attack = 64
                block = 65
                death = 67
            }
            
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.coins", min = 100, max = 400, weight = 25)
                    add("item.chaos_rune", min = 10, max = 30, weight = 20)
                    add("item.death_rune", min = 5, max = 20, weight = 18)
                    add("item.blood_rune", min = 3, max = 15, weight = 15)
                    add("item.fire_rune", min = 30, max = 80, weight = 20)
                    add("item.rune_longsword", min = 1, weight = 10)
                    add("item.rune_full_helm", min = 1, weight = 8)
                    add("item.rune_platebody", min = 1, weight = 6)
                    add("item.rune_platelegs", min = 1, weight = 6)
                    add("item.rune_dagger", min = 1, weight = 8)
                    add("item.adamant_longsword", min = 1, weight = 12)
                }
            }
        }
        
        // ======================
        // BLACK DRAGON
        // ======================
        // NOTE: Black dragons are configured in DragonConfigsPlugin.kt
        // Removing duplicate configuration to avoid "Npc combat definition has been previously set" error
        // If you need to modify black dragon config, edit DragonConfigsPlugin.kt instead
        
        // ======================
        // POISON SPIDER
        // ======================
        setCombatDef("npc.poison_spider") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 20
            }
            
            bonuses {
                attackStab = 12
                strengthBonus = 10
                defenceStab = 10
                defenceSlash = 10
                defenceCrush = 10
                defenceMagic = 8
                defenceRanged = 10
            }
            
            anims {
                attack = 5327
                block = 5328
                death = 5329
            }
            
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins", min = 5, max = 20, weight = 35)
                    add("item.spider_silk", min = 1, weight = 25)
                    add("item.spider_legs", min = 1, weight = 20)
                    add("item.iron_arrow", min = 3, max = 12, weight = 20)
                }
            }
        }
        
        // ======================
        // HELLHOUND
        // ======================
        // NOTE: Hellhounds are configured in wilderness/CombatConfigPlugin.kt
        // Removing duplicate configuration to avoid "Npc combat definition has been previously set" error
        // If you need to modify hellhound config, edit wilderness/CombatConfigPlugin.kt instead
    }
}

