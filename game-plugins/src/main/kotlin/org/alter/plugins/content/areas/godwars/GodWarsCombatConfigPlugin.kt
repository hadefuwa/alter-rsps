package org.alter.plugins.content.areas.godwars

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 3, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1) 
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1) 
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1) 
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.barrows_gloves", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.dragon_axe", min = 1, max = 1, weight = 1)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)

                    add("item.uncut_dragonstone", min = 10, max = 30, weight = 50)
                    add("item.uncut_ruby", min = 30, max = 50, weight = 70)
                    add("item.uncut_diamond", min = 50, max = 70, weight = 90)
                    add("item.uncut_sapphire", min = 70, max = 100, weight = 110) 
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 1, max = 2, weight = 250)
                    add("item.adamantite_bar_noted", min = 1, max = 1, weight = 200)
                    add("item.manta_ray", min = 1, max = 2, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)
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
                alwaysAggro()
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
                main(weight = 200) {
                    //add("item.coins", min = 10000, max = 200000, weight = 200)
                    add("item.coins_995", min = 10000, max = 200000, weight = 200)
                    add("item.death_rune", min = 250, max = 1000, weight = 200)
                    add("item.shark", min = 2, max = 4, weight = 200)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 200)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 200)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 200)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 200)
                    add("item.rune_arrow", min = 50, max = 1000, weight =200)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)

                    add("item.logs_2771_noted", min = 500, max = 1000, weight = 200)
                    add("item.oak_logs_2771_noted", min = 100, max = 500, weight = 200)
                    add("item.yew_logs_2773_noted", min = 100, max = 200, weight = 200) 
                    add("item.magic_logs_2772_noted", min = 10, max = 30, weight = 200)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.dargon_crossbow", min = 1, max = 1, weight = 1)
                    add("item.ancient_staff_2773", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword_2774", min = 1, max = 1, weight = 1)
                    add("item.dragon_dagger_2775", min = 1, max = 1, weight = 1)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


                    add("item.saradomin_godsword", min = 1, max = 1, weight = 1)
                    add("item.blessed_dhide_body", min = 1, max = 1, weight = 1)
                    add("item.ancient_godsword", min = 1, max = 1, weight = 1)
                    add("item.abyssal_dagger", min = 1, max = 1, weight = 1)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)
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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 10, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)


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
                alwaysAggro()
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
                    //add("item.coins", min = 10000, max = 200000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 250, max = 1000, weight = 300)
                    add("item.shark", min = 2, max = 4, weight = 250)
                    add("item.adamantite_bar_noted", min = 6, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 3, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 1, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 150, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 150, weight = 150)
                    add("item.rune_arrow", min = 50, max = 1000, weight =250)
                    add("item.hydra_slayer_helm", min = 1, max = 1, weight = 1)
                    add("item.iron_bar_noted", min = 1, max = 3, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 100, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 15, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 10, weight = 75)
                    add("item.reward_casket_master", min = 1, max = 2, weight = 25)
                    add("item.casket_hard_2738", min = 1, max = 2, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 2, weight = 50)

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
                alwaysAggro()
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
                    //add("item.coins", min = 1000, max = 25000, weight = 350)
                    add("item.coins_995", min = 10000, max = 200000, weight = 350)
                    add("item.death_rune", min = 25, max = 100, weight = 300)
                    add("item.shark", min = 1, max = 2, weight = 250)
                    add("item.adamantite_bar_noted", min = 1, max = 13, weight = 200)
                    add("item.manta_ray", min = 1, max = 1, weight = 100)
                    add("item.casket_hard_2738", min = 1, max = 1, weight = 100)
                    add("item.diamond_bolt_tips", min = 10, max = 15, weight = 10)
                    add("item.dragon_arrow", min = 1, max = 15, weight = 150)
                    add("item.rune_arrow", min = 50, max = 10, weight =250)

                    add("item.iron_bar_noted", min = 1, max = 1, weight = 300)
                    add("item.steel_bar_noted", min = 10, max = 1, weight = 290)
                    add("item.mithril_bar_noted", min = 5, max = 1, weight = 225)
                    add("item.runite_bar_noted", min = 5, max = 1, weight = 75)

                    add("item.reward_casket_master", min = 1, max = 1, weight = 25)
                    add("item.casket_easy_2738", min = 1, max = 1, weight = 200)
                    add("item.casket_medium", min = 1, max = 1, weight = 150)
                    add("item.casket_elite_2738", min = 1, max = 1, weight = 50)
                }
            }
        }
    }
}

