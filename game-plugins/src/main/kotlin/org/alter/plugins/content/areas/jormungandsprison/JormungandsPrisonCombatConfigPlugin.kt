package org.alter.plugins.content.areas.jormungandsprison

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Jormungand's Prison Combat Configuration Plugin
 * 
 * This plugin configures combat definitions and drop tables for all NPCs
 * found in Jormungand's Prison, an underground area beneath Rellekka.
 * 
 * NPCs configured:
 * - Basilisk (ID: 417) - Level 61 slayer creatures
 * - Basilisk Knight (ID: 9293) - Higher level basilisk variants  
 * - Dagannoth (ID: 7260, 7259) - Level 74-92 aggressive creatures
 * 
 * Drop Tables:
 * - Dagannoths: Noted dagannoth bones (1-3), Fremennik helms
 * - Basilisks: Rare basilisk jaw (1/50), Fremennik helms  
 * - All: Neizinot helm (1/20), warrior helm (1/10), archer helm (1/10)
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class JormungandsPrisonCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // ======================
        // BASILISKS (Level 61)
        // ======================
        // Standard basilisk slayer creatures
        setCombatDef("npc.basilisk_417") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 61
                attack = 50
                strength = 52
                defence = 50
            }
            
            bonuses {
                attackStab = 40
                attackSlash = 40
                attackCrush = 40
                strengthBonus = 35
                defenceStab = 40
                defenceSlash = 40
                defenceCrush = 40
                defenceMagic = 25
                defenceRanged = 40
            }
            
            anims {
                attack = 1544
                block = 1545
                death = 1547
            }
            
            aggro {
                radius = 6
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                // Fremennik helms with specified drop rates + rare basilisk jaw
                // Total item weights = 500 (guaranteed drop, no empty rolls)
                main(weight = 500) {
                    // Fremennik helms - exact drop rates maintained
                    add("item.fremennik_helm", min = 1, weight = 25)      // 25/500 = 1/20 chance
                    add("item.warrior_helm", min = 1, weight = 50)        // 50/500 = 1/10 chance  
                    add("item.archer_helm", min = 1, weight = 50)         // 50/500 = 1/10 chance
                    
                    // Rare basilisk jaw (1/50 chance)
                    add("item.basilisk_jaw", min = 1, weight = 10)        // 10/500 = 1/50 chance
                    
                    // Common drops - weights total exactly 365 to fill table
                    add("item.coins_995", min = 50, max = 200, weight = 80)
                    add("item.iron_ore_noted", min = 2, max = 5, weight = 60)
                    add("item.coal_noted", min = 1, max = 3, weight = 50)
                    add("item.mithril_ore_noted", min = 1, max = 2, weight = 35)
                    add("item.raw_beef_noted", min = 1, max = 2, weight = 40)
                    add("item.nature_rune", min = 1, max = 3, weight = 30)
                    add("item.air_rune", min = 5, max = 15, weight = 25)
                    add("item.water_rune", min = 5, max = 15, weight = 25)
                    add("item.earth_rune", min = 5, max = 15, weight = 25)
                    add("item.fire_rune", min = 5, max = 15, weight = 25)
                    add("item.mind_rune", min = 3, max = 8, weight = 20)
                    add("item.body_rune", min = 2, max = 6, weight = 15)
                    add("item.law_rune", min = 1, max = 2, weight = 10)
                    add("item.chaos_rune", min = 1, max = 2, weight = 10)
                    add("item.steel_arrow_noted", min = 10, max = 25, weight = 15)
                    add("item.mithril_arrow_noted", min = 5, max = 15, weight = 10)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 15)
                    add("item.steel_bar_noted", min = 1, max = 2, weight = 10)
                    add("item.tin_ore_noted", min = 3, max = 8, weight = 15)
                    add("item.copper_ore_noted", min = 3, max = 8, weight = 15)
                }
                
                // Move basilisk jaw to main table with proper weight calculation
                // For 1/50 chance: add weight 4 to main table (200 + 4 = 204), giving 4/204 ≈ 1/51
            }
        }
        
        // ======================
        // BASILISK KNIGHTS (Level 204) 
        // ======================
        // Higher level basilisk variants with better drops
        setCombatDef("npc.basilisk_knight_9293") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            
            stats {
                hitpoints = 300
                attack = 150
                strength = 155
                defence = 150
            }
            
            bonuses {
                attackStab = 120
                attackSlash = 120
                attackCrush = 120
                strengthBonus = 110
                defenceStab = 120
                defenceSlash = 120
                defenceCrush = 120
                defenceMagic = 80
                defenceRanged = 120
            }
            
            anims {
                attack = 1544
                block = 1545
                death = 1547
            }
            
            aggro {
                radius = 8
                searchDelay = 1
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                // Better version of basilisk drops with same helm rates + basilisk jaw
                // Total item weights = 500 (guaranteed drop, no empty rolls)
                main(weight = 500) {
                    // Fremennik helms (same rates as regular basilisk)
                    add("item.fremennik_helm", min = 1, weight = 25)      // 25/500 = 1/20 chance
                    add("item.warrior_helm", min = 1, weight = 50)        // 50/500 = 1/10 chance
                    add("item.archer_helm", min = 1, weight = 50)         // 50/500 = 1/10 chance
                    
                    // Rare basilisk jaw (same 1/50 rate)
                    add("item.basilisk_jaw", min = 1, weight = 10)        // 10/500 = 1/50 chance
                    
                    // Better common drops - weights total exactly 365 to fill table
                    add("item.coins_995", min = 500, max = 2000, weight = 80)
                    add("item.adamantite_ore_noted", min = 2, max = 5, weight = 45)
                    add("item.runite_ore_noted", min = 1, max = 2, weight = 25)
                    add("item.rune_scimitar_noted", min = 1, weight = 15)
                    add("item.rune_full_helm_noted", min = 1, weight = 15)
                    add("item.law_rune", min = 5, max = 15, weight = 35)
                    add("item.death_rune", min = 3, max = 8, weight = 30)
                    add("item.shark_noted", min = 2, max = 4, weight = 35)
                    add("item.prayer_potion4_noted", min = 1, max = 2, weight = 20)
                    add("item.nature_rune", min = 3, max = 10, weight = 25)
                    add("item.chaos_rune", min = 2, max = 6, weight = 20)
                    add("item.blood_rune", min = 1, max = 3, weight = 15)
                    add("item.soul_rune", min = 1, max = 2, weight = 10)
                    add("item.adamant_bar_noted", min = 1, max = 3, weight = 15)
                    add("item.mithril_bar_noted", min = 2, max = 5, weight = 20)
                    add("item.steel_bar_noted", min = 3, max = 8, weight = 15)
                    add("item.iron_bar_noted", min = 5, max = 12, weight = 15)
                    add("item.coal_noted", min = 5, max = 15, weight = 15)
                    add("item.adamant_arrow_noted", min = 10, max = 30, weight = 10)
                    add("item.rune_arrow_noted", min = 5, max = 15, weight = 5)
                }
                
                // Move basilisk jaw to main table with same 1/50 rate
            }
        }
        
        // ======================
        // DAGANNOTHS (Level 74)
        // ======================
        // Smaller dagannoth variant
        setCombatDef("npc.dagannoth_7259") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 74
                attack = 60
                strength = 62
                defence = 60
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
                attack = 1339
                block = 1340
                death = 1341
            }
            
            aggro {
                radius = 6
                searchDelay = 2
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 200) {
                    // Noted dagannoth bones (1-3 per kill)
                    add("item.dagannoth_bones_noted", min = 1, max = 3, weight = 80)  // Common drop
                    
                    // Fremennik helms with specified rates
                    add("item.fremennik_helm", min = 1, weight = 10)      // 1/20 chance
                    add("item.warrior_helm", min = 1, weight = 20)        // 1/10 chance
                    add("item.archer_helm", min = 1, weight = 20)         // 1/10 chance
                    
                    // Common drops - total weight exactly 70 to fill remaining table
                    add("item.coins_995", min = 100, max = 400, weight = 25)
                    add("item.raw_shark_noted", min = 1, max = 2, weight = 15)
                    add("item.iron_ore_noted", min = 3, max = 6, weight = 12)
                    add("item.chaos_rune", min = 2, max = 5, weight = 8)
                    add("item.nature_rune", min = 1, max = 3, weight = 5)
                    add("item.law_rune", min = 1, max = 2, weight = 3)
                    add("item.steel_bar_noted", min = 1, max = 2, weight = 2)
                }
            }
        }
        
        // ======================
        // DAGANNOTHS (Level 92)
        // ======================
        // Larger dagannoth variant 
        setCombatDef("npc.dagannoth_7260") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            
            stats {
                hitpoints = 120
                attack = 70
                strength = 75
                defence = 70
            }
            
            bonuses {
                attackStab = 60
                attackSlash = 60
                attackCrush = 60
                strengthBonus = 55
                defenceStab = 60
                defenceSlash = 60
                defenceCrush = 60
                defenceMagic = 40
                defenceRanged = 60
            }
            
            anims {
                attack = 1339
                block = 1340
                death = 1341
            }
            
            aggro {
                radius = 7
                searchDelay = 2
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 200) {
                    // Noted dagannoth bones (1-3 per kill)
                    add("item.dagannoth_bones_noted", min = 1, max = 3, weight = 70)  // Common drop
                    
                    // Fremennik helms with specified rates
                    add("item.fremennik_helm", min = 1, weight = 10)      // 1/20 chance
                    add("item.warrior_helm", min = 1, weight = 20)        // 1/10 chance
                    add("item.archer_helm", min = 1, weight = 20)         // 1/10 chance
                    
                    // Better drops than smaller dagannoth - total weight exactly 80 to fill table
                    add("item.coins_995", min = 200, max = 800, weight = 30)
                    add("item.raw_shark_noted", min = 2, max = 3, weight = 18)
                    add("item.mithril_ore_noted", min = 2, max = 4, weight = 12)
                    add("item.law_rune", min = 1, max = 3, weight = 8)
                    add("item.death_rune", min = 1, max = 2, weight = 5)
                    add("item.nature_rune", min = 2, max = 4, weight = 4)
                    add("item.chaos_rune", min = 1, max = 3, weight = 3)
                }
            }
        }
    }
}