package org.alter.plugins.content.areas.slayertower

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Slayer Tower Combat Configuration Plugin
 * 
 * This plugin configures combat definitions and drop tables for all NPCs
 * found in the Slayer Tower located in Morytania.
 * 
 * NPCs configured:
 * - Crawling Hands (Ground Floor)
 * - Banshees (Ground Floor)
 * - Aberrant Spectres (First Floor)
 * - Bloodvelds (First Floor)
 * - Infernal Mages (First Floor)
 * - Gargoyles (Top Floor)
 * - Nechryael (Top Floor)
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class SlayerTowerCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // ======================
        // CRAWLING HANDS
        // ======================
        // Low-level slayer monsters (Level 5 slayer required)
        // Combat Level: 8-12
        setCombatDef("npc.crawling_hand_448", "npc.crawling_hand_453", "npc.crawling_hand_454") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 10
            }
            
            bonuses {
                attackStab = 5
                strengthBonus = 4
                defenceStab = 4
                defenceSlash = 4
                defenceCrush = 4
                defenceMagic = 2
                defenceRanged = 4
            }
            
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 4)
                    // Note: crawling_hand_noted doesn't exist in RSCM, using regular crawling_hand instead
                    // add("item.crawling_hand_noted", min = 1, weight = 3)
                    add("item.eternal_gem_noted", min = 1, weight = 1)
                    add("item.mist_battlestaff_noted", min = 1, weight = 2)
                    add("item.iron_boots_noted", min = 1, weight = 3)
                    add("item.opal_ring_noted", min = 1, max = 1, weight = 4)
                    add("item.jade_ring_noted", min = 1, weight = 3)
                    add("item.topaz_ring_noted", min = 1, weight = 1)

                }
            }
        }
        
        // ======================
        // BANSHEES
        // ======================
        // Require earmuffs or slayer helmet to prevent stat reductions
        // Combat Level: 23
        setCombatDef("npc.banshee_414") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 22
            }
            
            bonuses {
                attackMagic = 15
                magicDamageBonus = 2
                defenceStab = 8
                defenceSlash = 8
                defenceCrush = 8
                defenceMagic = 20
                defenceRanged = 8
            }
            
            anims {
                attack = 5540
                block = 5541
                death = 5542
            }
            
            aggro {
                radius = 5
                searchDelay = 3
            }
            
            drops {
                always {
                    add("item.ashes", 1)
                }
                
                main(weight = 128) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 4)
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 1)
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 2)
                    add("item.grimy_guam_leaf_noted", min = 1, weight = 10)
                    add("item.grimy_marrentill_noted", min = 1, weight = 8)
                    add("item.grimy_tarromin_noted", min = 1, weight = 6)
                    add("item.pure_essence_noted", min = 5, max = 15, weight = 12)
                }
            }
        }
        
        // ======================
        // ABERRANT SPECTRES
        // ======================
        // Require nose peg or slayer helmet to prevent stat reductions
        // Combat Level: 96
        setCombatDef("npc.aberrant_spectre_2", "npc.aberrant_spectre_3", "npc.aberrant_spectre_4", "npc.aberrant_spectre_5") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            
            stats {
                hitpoints = 90
                attack = 70
                strength = 70
                defence = 70
                magic = 80
            }
            
            bonuses {
                attackMagic = 60
                magicDamageBonus = 5
                defenceStab = 40
                defenceSlash = 40
                defenceCrush = 40
                defenceMagic = 50
                defenceRanged = 40
            }
            
            anims {
                attack = 5540
                block = 5541
                death = 5542
            }
            
            aggro {
                radius = 7
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 15)  // Increased from 5
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 8)  // Increased from 2
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 10)  // Increased from 3
                    add("item.grimy_ranarr_weed_noted", min = 1, weight = 35)  // Increased from 12
                    add("item.grimy_irit_leaf_noted", min = 1, weight = 45)  // Increased from 15
                    add("item.grimy_avantoe_noted", min = 1, weight = 45)  // Increased from 15
                    add("item.grimy_kwuarm_noted", min = 1, weight = 35)  // Increased from 12
                    add("item.grimy_cadantine_noted", min = 1, weight = 30)  // Increased from 10
                    add("item.grimy_dwarf_weed_noted", min = 1, weight = 30)  // Increased from 10
                    add("item.grimy_lantadyme_noted", min = 1, weight = 25)  // Increased from 8
                    add("item.grimy_torstol_noted", min = 1, weight = 15)  // Increased from 5
                    add("item.rune_full_helm_noted", min = 1, weight = 25)  // Increased from 8
                    add("item.rune_chainbody_noted", min = 1, weight = 20)  // Increased from 6
                    add("item.rune_platelegs_noted", min = 1, weight = 20)  // Increased from 6
                    add("item.rune_plateskirt_noted", min = 1, weight = 20)  // Increased from 6
                    add("item.rune_kiteshield_noted", min = 1, weight = 15)  // Increased from 5
                    add("item.rune_2h_sword_noted", min = 1, weight = 12)  // Increased from 4
                    add("item.rune_battleaxe_noted", min = 1, weight = 12)  // Increased from 4
                    add("item.rune_scimitar_noted", min = 1, weight = 25)  // Increased from 8
                    add("item.rune_longsword_noted", min = 1, weight = 20)  // Increased from 6
                    add("item.mystic_robe_top_dark_noted", min = 1, weight = 10)  // Increased from 3
                    add("item.black_mystic_gloves_noted", min = 1, weight = 8)  // Increased from 2
                    add("item.red_mystic_gloves_noted", min = 1, weight = 8)  // Increased from 2
                    add("item.death_rune", min = 3, max = 10, weight = 35)  // Increased from 12
                    add("item.blood_rune", min = 2, max = 8, weight = 30)  // Increased from 10
                    add("item.chaos_rune", min = 5, max = 15, weight = 45)  // Increased from 15
                    add("item.nature_rune", min = 4, max = 12, weight = 35)  // Increased from 12
                    add("item.adamant_boots_noted", min = 1, weight = 10)  // Increased from 3
                    add("item.coins_995", min = 100, max = 500, weight = 40)  // Add coins as common drop
                }
                
                tertiary(weight = 128) {
                    add("item.casket_hard", min = 1, weight = 8)
                }
            }
        }
        
        // ======================
        // BLOODVELDS
        // ======================
        // Large, aggressive monsters that use melee attacks
        // Combat Level: 76
        setCombatDef("npc.bloodveld_484") {
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
                attack = 422
                block = 424
                death = 836
            }
            
            aggro {
                radius = 7
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 256) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 8)
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 3)
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 4)
                    add("item.blood_rune", min = 5, max = 15, weight = 30)
                    add("item.death_rune", min = 3, max = 10, weight = 20)
                    add("item.chaos_rune", min = 5, max = 15, weight = 25)
                    add("item.nature_rune", min = 4, max = 12, weight = 20)
                    add("item.law_rune", min = 2, max = 8, weight = 15)
                    add("item.grimy_ranarr_weed_noted", min = 1, weight = 8)
                    add("item.grimy_irit_leaf_noted", min = 1, weight = 10)
                    add("item.grimy_avantoe_noted", min = 1, weight = 10)
                    add("item.grimy_kwuarm_noted", min = 1, weight = 8)
                    add("item.grimy_cadantine_noted", min = 1, weight = 6)
                    add("item.uncut_sapphire_noted", min = 1, weight = 8)
                    add("item.uncut_emerald_noted", min = 1, weight = 6)
                    add("item.uncut_ruby_noted", min = 1, weight = 4)
                    add("item.uncut_diamond_noted", min = 1, weight = 2)
                    add("item.rune_med_helm_noted", min = 1, weight = 5)
                    add("item.rune_full_helm_noted", min = 1, weight = 4)
                    add("item.rune_chainbody_noted", min = 1, weight = 4)
                    add("item.rune_platelegs_noted", min = 1, weight = 4)
                    add("item.rune_kiteshield_noted", min = 1, weight = 3)
                    add("item.adament_boots_noted", min = 1, weight = 4)  

                }
                
                tertiary(weight = 512) {
                    add("item.dark_bow_noted", min = 1, weight = 1)
                }
            }
        }
        
        // ======================
        // INFERNAL MAGES
        // ======================
        // Magic-using slayer monsters
        // Combat Level: 66
        setCombatDef("npc.infernal_mage_443", "npc.infernal_mage_445", "npc.infernal_mage_446", "npc.infernal_mage_447") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            
            stats {
                hitpoints = 60
                attack = 50
                strength = 50
                defence = 50
                magic = 70
            }
            
            bonuses {
                attackMagic = 55
                magicDamageBonus = 4
                defenceStab = 30
                defenceSlash = 30
                defenceCrush = 30
                defenceMagic = 45
                defenceRanged = 30
            }
            
            anims {
                attack = 428
                block = 424
                death = 836
            }
            
            aggro {
                radius = 6
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 256) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 4)
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 1)
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 3)
                    add("item.death_rune", min = 5, max = 15, weight = 25)
                    add("item.blood_rune", min = 3, max = 10, weight = 20)
                    add("item.chaos_rune", min = 5, max = 15, weight = 25)
                    add("item.law_rune", min = 3, max = 10, weight = 18)
                    add("item.nature_rune", min = 4, max = 12, weight = 20)
                    add("item.fire_rune", min = 10, max = 30, weight = 15)
                    add("item.mystic_hat_dark_noted", min = 1, weight = 4)
                    add("item.mystic_robe_top_dark_noted", min = 1, weight = 3)
                    add("item.mystic_robe_bottom_dark_noted", min = 1, weight = 3)
                    add("item.mystic_hat_light_noted", min = 1, weight = 4)
                    add("item.mystic_robe_top_light_noted", min = 1, weight = 3)
                    add("item.mystic_robe_bottom_light_noted", min = 1, weight = 3)
                    add("item.black_mystic_boots_noted", min = 1, weight = 2)
                    add("item.red_mystic_boots_noted", min = 1, weight = 2)
                    add("item.adamant_boots_noted", min = 1, weight = 3)  
                    
                }
            }
        }
        
        // ======================
        // ABYSSAL DEMONS
        // ======================
        // High-level demons that can teleport during combat
        // Combat Level: 124
        setCombatDef("npc.abyssal_demon_415", "npc.abyssal_demon_416") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 150
                attack = 97
                strength = 97
                defence = 97
                magic = 1
                ranged = 1
            }
            
            bonuses {
                attackStab = 0
                attackSlash = 0
                attackCrush = 0
                attackMagic = 0
                attackRanged = 0
                strengthBonus = 0
                defenceStab = 0
                defenceSlash = 0
                defenceCrush = 0
                defenceMagic = 0
                defenceRanged = 0
                magicDamageBonus = 0
                rangedStrengthBonus = 0
            }
            
            anims {
                attack = 2796
                block = 2797
                death = 2798
            }
            
            aggro {
                radius = 7
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.ashes", 1)
                }
                
                main(weight = 128) {
                    add("item.abyssal_whip", min = 1, weight = 4) // Very rare
                    add("item.abyssal_dagger", min = 1, weight = 8)
                    add("item.rune_full_helm_noted", min = 1, weight = 18)
                    add("item.rune_platelegs_noted", min = 1, weight = 28)
                    add("item.rune_2h_sword_noted", min = 1, weight = 26)
                    add("item.chaos_rune", min = 40, max = 60, weight = 25)
                    add("item.death_rune", min = 8, max = 12, weight = 20)
                    add("item.blood_rune", min = 4, max = 6, weight = 25)
                    add("item.coins_995", min = 100, max = 500000, weight = 30)
                }
                
                tertiary(weight = 128) {
                    add("item.casket_hard", min = 1, weight = 8)
                }
            }
        }
        
        // ======================
        // GARGOYLES
        // ======================
        // Stone creatures that require a rock hammer to finish
        // Combat Level: 111
        setCombatDef("npc.gargoyle_412") {
            configs {
                attackSpeed = 4
                respawnDelay = 35
            }
            
            stats {
                hitpoints = 105
                attack = 80
                strength = 85
                defence = 80
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
                attack = 422
                block = 424
                death = 836
            }
            
            aggro {
                radius = 7
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 256) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 10)
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 4)
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 6)
                    add("item.rune_full_helm_noted", min = 1, weight = 8)
                    add("item.rune_platelegs_noted", min = 1, weight = 8)
                    add("item.rune_2h_sword_noted", min = 1, weight = 6)
                    add("item.rune_battleaxe_noted", min = 1, weight = 5)
                    add("item.rune_pickaxe_noted", min = 1, weight = 10)
                    add("item.gold_ore_noted", min = 40, max = 75, weight = 12)
                    add("item.adamantite_bar_noted", min = 25, max = 40, weight = 10)
                    add("item.coal_noted", min = 180, max = 250, weight = 10)
                    add("item.gold_bar_noted", min = 35, max = 50, weight = 10)
                    add("item.mithril_bar_noted", min = 35, max = 45, weight = 10)
                    add("item.runite_ore_noted", min = 3, max = 6, weight = 6)
                    add("item.runite_bar_noted", min = 3, max = 5, weight = 5)
                    add("item.granite_dust_noted", min = 50, max = 100, weight = 20)
                    add("item.rune_boots_noted", min = 1, weight = 10)  
                }
                
                tertiary(weight = 250) {
                    add("item.granite_maul_noted", min = 1, weight = 8)
                }
            }
        }
        
        // ======================
        // NECHRYAEL
        // ======================
        // High-level slayer monsters that can summon death spawns
        // Combat Level: 115
        setCombatDef("npc.nechryael_8") {
            configs {
                attackSpeed = 4
                respawnDelay = 40
            }
            
            stats {
                hitpoints = 105
                attack = 85
                strength = 90
                defence = 85
                magic = 75
            }
            
            bonuses {
                attackStab = 65
                attackSlash = 65
                attackCrush = 65
                strengthBonus = 60
                defenceStab = 65
                defenceSlash = 65
                defenceCrush = 65
                defenceMagic = 50
                defenceRanged = 65
            }
            
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            aggro {
                radius = 8
                searchDelay = 2
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 512) {
                    add("item.slayer_ring_8", min = 1, max = 1, weight = 20)
                    add("item.eternal_gem_noted", min = 1, max = 1, weight = 8)
                    add("item.mist_battlestaff_noted", min = 1, max = 1, weight = 12)
                    add("item.chaos_rune", min = 40, max = 60, weight = 30)
                    add("item.law_rune", min = 30, max = 40, weight = 25)
                    add("item.death_rune", min = 8, max = 12, weight = 20)
                    add("item.blood_rune", min = 4, max = 6, weight = 15)
                    add("item.rune_2h_sword_noted", min = 1, weight = 8)
                    add("item.rune_full_helm_noted", min = 1, weight = 8)
                    add("item.adamant_kiteshield_noted", min = 1, weight = 8)
                    add("item.adamant_platelegs_noted", min = 1, weight = 8)
                    add("item.grimy_avantoe_noted", min = 2, weight = 10)
                    add("item.grimy_kwuarm_noted", min = 2, weight = 10)
                    add("item.grimy_irit_leaf_noted", min = 2, weight = 10)
                    add("item.grimy_toadflax_noted", min = 2, weight = 10)
                    add("item.grimy_dwarf_weed_noted", min = 2, weight = 8)
                    add("item.grimy_lantadyme_noted", min = 2, weight = 8)
                    add("item.grimy_cadantine_noted", min = 2, weight = 6)
                    add("item.grimy_snapdragon_noted", min = 2, weight = 6)
                    add("item.grimy_torstol_noted", min = 2, weight = 4)
                    add("item.limpwurt_root_noted", min = 10, max = 20, weight = 12)
                    add("item.soft_clay_noted", min = 20, max = 30, weight = 10)
                    add("item.tuna_noted", min = 1, weight = 8)
                }
                
                tertiary(weight = 98) {
                    add("item.rune_boots_noted", min = 1, weight = 12)
                    add("item.casket_hard", min = 1, weight = 1)
                }
                
                tertiary(weight = 18) {
                    add("item.abyssal_dagger_p_plus_plus_noted", min = 1, weight = 1)
                }
            }
        }
    }
}

