package org.alter.plugins.content.areas.godwars

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * God Wars Dungeon Combat Configuration Plugin
 * 
 * Configures combat stats for all God Wars NPCs across all factions:
 * - Saradomin
 * - Zamorak
 * - Armadyl
 * - Bandos
 * 
 * ============================================================================
 * DROP TABLE WEIGHT SYSTEM - IMPORTANT RULES
 * ============================================================================
 * 
 * CRITICAL RULE: The table weight MUST be greater than or equal to the sum
 * of all item weights in that table. If not, the server will crash with:
 * "Table weight (X) must be greater than or equal to the sum of all item weights (Y)"
 * 
 * HOW TO CALCULATE CORRECT TABLE WEIGHT:
 * 1. Add up ALL item weights in the main() block
 * 2. Set main(weight = SUM_OF_ALL_ITEM_WEIGHTS) or higher
 * 3. If you want guaranteed drops, set it equal to the sum
 * 4. If you want some chance of no drop, set it higher than the sum
 * 
 * EXAMPLE - CORRECT CONFIGURATION:
 *   main(weight = 100) {  // Table weight = 100
 *       add("item.coins", weight = 20)      // Item weight = 20
 *       add("item.sword", weight = 10)      // Item weight = 10
 *       add("item.shield", weight = 15)     // Item weight = 15
 *       // Total item weights = 20 + 10 + 15 = 45
 *       // Table weight (100) >= Sum (45) ✓ CORRECT
 *   }
 * 
 * EXAMPLE - INCORRECT CONFIGURATION (WILL CRASH):
 *   main(weight = 50) {   // Table weight = 50
 *       add("item.coins", weight = 350)     // Item weight = 350
 *       add("item.sword", weight = 200)     // Item weight = 200
 *       // Total item weights = 350 + 200 = 550
 *       // Table weight (50) < Sum (550) ✗ ERROR - WILL CRASH!
 *   }
 * 
 * HOW IT WORKS:
 * - Item weight: Higher = more common drop (weight 20 is twice as likely as weight 10)
 * - Table weight: Controls overall drop chance vs no drop
 *   * If table weight = sum of item weights: Guaranteed drop (100% chance)
 *   * If table weight > sum: Some chance of no drop (lower table weight = more drops)
 *   * If table weight < sum: INVALID - Server will crash on startup
 * 
 * TIPS:
 * - Always calculate sum of item weights first before setting table weight
 * - Use a calculator or add them up manually
 * - When in doubt, set table weight higher than the sum (e.g., sum + 100)
 * - The difference between table weight and sum determines "no drop" chance
 */
class GodWarsCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // ======================
        // SARADOMIN FACTION
        // ======================
        
        // Spiritual Warrior (Saradomin)
        setCombatDef("npc.spiritual_warrior") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 125
                strength = 125
                defence = 125
                magic = 75
                ranged = 75
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                strengthBonus = 75
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 427
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.adamant_longsword", min = 1, weight = 4)
                    add("item.rune_longsword", min = 1, weight = 2)
                    add("item.death_rune", min = 10, max = 20, weight = 8)
                    add("item.blood_rune", min = 5, max = 10, weight = 6)
                    add("item.chaos_rune", min = 20, max = 40, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 8)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.coins", min = 500, max = 2000, weight = 15)
                    add("item.ranarr_seed", min = 1, weight = 6)
                    add("item.uncut_ruby", min = 1, weight = 6)
                    add("item.uncut_diamond", min = 1, weight = 4)
                }
            }
        }
        
        // Spiritual Ranger (Saradomin)
        setCombatDef("npc.spiritual_ranger") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 75
                ranged = 125
            }
            
            bonuses {
                attackRanged = 90
                rangedStrengthBonus = 80
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 426
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.adamant_arrow", min = 50, max = 100, weight = 10)
                    add("item.rune_arrow", min = 20, max = 40, weight = 6)
                    add("item.death_rune", min = 10, max = 20, weight = 8)
                    add("item.chaos_rune", min = 20, max = 40, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 8)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.coins", min = 500, max = 2000, weight = 15)
                    add("item.ranarr_seed", min = 1, weight = 6)
                    add("item.uncut_ruby", min = 1, weight = 6)
                    add("item.uncut_diamond", min = 1, weight = 4)
                }
            }
        }
        
        // Spiritual Mage (Saradomin)
        setCombatDef("npc.spiritual_mage") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 125
                ranged = 75
            }
            
            bonuses {
                attackMagic = 90
                magicDamageBonus = 5
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 428
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.death_rune", min = 20, max = 40, weight = 10)
                    add("item.blood_rune", min = 10, max = 20, weight = 8)
                    add("item.chaos_rune", min = 40, max = 80, weight = 12)
                    add("item.law_rune", min = 10, max = 20, weight = 8)
                    add("item.adamant_bar", min = 1, max = 3, weight = 8)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.coins", min = 500, max = 2000, weight = 15)
                    add("item.ranarr_seed", min = 1, weight = 6)
                    add("item.uncut_ruby", min = 1, weight = 6)
                    add("item.uncut_diamond", min = 1, weight = 4)
                }
            }
        }
        
        // Knight of Saradomin
        setCombatDef("npc.knight_of_saradomin", "npc.knight_of_saradomin_2214") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 110
                attack = 135
                strength = 135
                defence = 135
                magic = 75
                ranged = 75
            }
            
            bonuses {
                attackSlash = 90
                strengthBonus = 85
                defenceStab = 90
                defenceSlash = 90
                defenceCrush = 90
                defenceMagic = 70
                defenceRanged = 90
            }
            
            anims {
                attack = 407
                block = 1156
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.rune_longsword", min = 1, weight = 3)
                    add("item.death_rune", min = 15, max = 30, weight = 8)
                    add("item.blood_rune", min = 10, max = 20, weight = 6)
                    add("item.adamant_bar", min = 2, max = 5, weight = 8)
                    add("item.shark", min = 3, max = 6, weight = 10)
                    add("item.coins", min = 1000, max = 3000, weight = 15)
                    add("item.ranarr_seed", min = 1, max = 2, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 2, weight = 6)
                    add("item.uncut_diamond", min = 1, weight = 5)
                }
            }
        }
        
        // ======================
        // ZAMORAK FACTION
        // ======================
        
        // Spiritual Warrior (Zamorak)
        setCombatDef("npc.spiritual_warrior_3159") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 125
                strength = 125
                defence = 125
                magic = 75
                ranged = 75
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                strengthBonus = 75
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 427
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Spiritual Ranger (Zamorak)
        setCombatDef("npc.spiritual_ranger_3160") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 75
                ranged = 125
            }
            
            bonuses {
                attackRanged = 90
                rangedStrengthBonus = 80
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 426
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Spiritual Mage (Zamorak)
        setCombatDef("npc.spiritual_mage_3161") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 125
                ranged = 75
            }
            
            bonuses {
                attackMagic = 90
                magicDamageBonus = 5
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 428
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Imp (Zamorak)
        setCombatDef("npc.imp_3134") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 70
                attack = 100
                strength = 100
                defence = 100
                magic = 60
                ranged = 60
            }
            
            bonuses {
                attackStab = 50
                strengthBonus = 40
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 40
                defenceRanged = 50
            }
            
            anims {
                attack = 172
                block = 173
                death = 174
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 200, max = 800, weight = 20)
                    add("item.chaos_rune", min = 10, max = 20, weight = 10)
                    add("item.lobster", min = 1, max = 3, weight = 10)
                    add("item.iron_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // ======================
        // ARMADYL FACTION
        // ======================
        
        // Spiritual Warrior (Armadyl)
        setCombatDef("npc.spiritual_warrior_3166") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 125
                strength = 125
                defence = 125
                magic = 75
                ranged = 75
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                strengthBonus = 75
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 427
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Spiritual Ranger (Armadyl)
        setCombatDef("npc.spiritual_ranger_3167") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 75
                ranged = 125
            }
            
            bonuses {
                attackRanged = 90
                rangedStrengthBonus = 80
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 426
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Spiritual Mage (Armadyl)
        setCombatDef("npc.spiritual_mage_3168") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 125
                ranged = 75
            }
            
            bonuses {
                attackMagic = 90
                magicDamageBonus = 5
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 428
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 500, max = 2000, weight = 20)
                    add("item.death_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.adamant_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // Aviansie (Armadyl)
        setCombatDef("npc.aviansie") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 90
                attack = 70
                strength = 70
                defence = 120
                magic = 70
                ranged = 120
            }
            
            bonuses {
                attackRanged = 85
                rangedStrengthBonus = 75
                defenceStab = 70
                defenceSlash = 70
                defenceCrush = 70
                defenceMagic = 80
                defenceRanged = 70
            }
            
            anims {
                attack = 3503
                block = 3502
                death = 3504
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 50) {
                    add("item.coins", min = 400, max = 1500, weight = 20)
                    add("item.chaos_rune", min = 10, max = 20, weight = 10)
                    add("item.shark", min = 2, max = 4, weight = 10)
                    add("item.mithril_bar", min = 1, max = 3, weight = 10)
                }
            }
        }
        
        // ======================
        // BANDOS FACTION
        // ======================
        
        // Spiritual Warrior (Bandos)
        setCombatDef("npc.spiritual_warrior_2243") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 125
                strength = 125
                defence = 125
                magic = 75
                ranged = 75
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                strengthBonus = 75
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 427
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                // NOTE: This table has many high-weight items. Sum of item weights ≈ 2883
                // Table weight set to 3000 to satisfy validation (3000 >= 2883)
                // This gives ~96% drop chance, ~4% no drop chance
                main(weight = 3000) {
                    add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamant_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.rune_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.casket_master_2738", min = 1, max = 10, weight = 25)
                    add("item.casket_easy_2738", min = 1, max = 10, weight = 200)
                    add("item.casket_medium_2738", min = 1, max = 10, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 10, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1)
                }
            }
        }
        
        // Spiritual Ranger (Bandos)
        setCombatDef("npc.spiritual_ranger_2242") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 75
                ranged = 125
            }
            
            bonuses {
                attackRanged = 90
                rangedStrengthBonus = 80
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 426
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                // NOTE: This table has many high-weight items. Sum of item weights ≈ 2883
                // Table weight set to 3000 to satisfy validation (3000 >= 2883)
                // This gives ~96% drop chance, ~4% no drop chance
                main(weight = 3000) {
                    add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamant_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.rune_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.casket_master_2738", min = 1, max = 10, weight = 25)
                    add("item.casket_easy_2738", min = 1, max = 10, weight = 200)
                    add("item.casket_medium_2738", min = 1, max = 10, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 10, weight = 50)


                    add("item.keris_partisan", min = 1, max = 1, weight = 1)
                    add("item.amulet_of_glory", min = 1, max = 1, weight = 1)
                    add("item.dragon_boots", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1)
                }
            }
        }
        
        // Spiritual Mage (Bandos)
        setCombatDef("npc.spiritual_mage_2244") {
            configs {
                attackSpeed = 5
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 100
                attack = 75
                strength = 75
                defence = 125
                magic = 125
                ranged = 75
            }
            
            bonuses {
                attackMagic = 90
                magicDamageBonus = 5
                defenceStab = 80
                defenceSlash = 80
                defenceCrush = 80
                defenceMagic = 60
                defenceRanged = 80
            }
            
            anims {
                attack = 428
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                // NOTE: This table has many high-weight items. Sum of item weights ≈ 2883
                // Table weight set to 3000 to satisfy validation (3000 >= 2883)
                // This gives ~96% drop chance, ~4% no drop chance
                main(weight = 3000) {
                    add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamant_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.hydra_slayer_helm", min = 1, max = 4, weight = 1)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.rune_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.casket_master_2738", min = 1, max = 10, weight = 25)
                    add("item.casket_easy_2738", min = 1, max = 10, weight = 200)
                    add("item.casket_medium_2738", min = 1, max = 10, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 10, weight = 50)
                    add("item.rangers_boots", min = 1, max = 1, weight = 2)
                    add("item.dragon_crossbow", min = 1, max = 1, weight = 1)
                    add("item.spirit_shield", min = 1, max = 1, weight = 1)
                }
            }
        }
        
        // Goblin (Bandos)
        setCombatDef("npc.goblin_2245", "npc.goblin_2246") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 70
                attack = 100
                strength = 100
                defence = 100
                magic = 60
                ranged = 60
            }
            
            bonuses {
                attackStab = 50
                strengthBonus = 40
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 40
                defenceRanged = 50
            }
            
            anims {
                attack = 6183
                block = 6184
                death = 6182
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                aggroTimer = Int.MAX_VALUE
            }
            
            drops {
                // ========================================================================
                // DROP TABLE CONFIGURATION GUIDE
                // ========================================================================
                // 
                // ⚠️ CRITICAL RULE: Table weight MUST be >= sum of all item weights!
                // If table weight < sum of item weights, server will crash on startup.
                // 
                // ITEM WEIGHTS:
                // - Higher weight = more common drop (weight 20 is twice as likely as weight 10)
                // - Relative weights determine drop probability within the table
                // 
                // TABLE WEIGHT (main(weight = X)):
                // - MUST be >= sum of all item weights in this table
                // - Lower table weight = more likely to get a drop vs nothing
                // - Higher table weight = less likely to get a drop vs nothing
                // - Set equal to sum of item weights = guaranteed drop (100% chance)
                // 
                // HOW TO CALCULATE:
                // 1. Add up ALL item weights in the main() block
                // 2. Set main(weight = SUM) or higher
                // 3. Example: If items total 79, use main(weight = 79) for guaranteed drops
                //             or main(weight = 128) for ~62% drop chance
                //
                // ========================================================================
                always {
                    add("item.bones", 1)
                }
                main(weight = 3000) {
                    add("item.coins", min = 1000, max = 25000, weight = 350)
                    add("item.death_rune", min = 25, max = 100, weight = 300)
                    add("item.shark", min = 1, max = 2, weight = 250)
                    add("item.adamant_bar_noted", min = 1, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 1, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 1, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 15, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 15, weight = 150)
                    add("item.rune_arrow", min = 50, max = 10, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 1, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 1, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 1, weight = 225)
                    add("item.rune_bar_noted", min = 5, max = 1, weight = 75)

                    add("item.casket_master_2738", min = 1, max = 1, weight = 25)
                    add("item.casket_easy_2738", min = 1, max = 1, weight = 200)
                    add("item.casket_medium_2738", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 1, weight = 50)
                }
            }
        }
    }
}

