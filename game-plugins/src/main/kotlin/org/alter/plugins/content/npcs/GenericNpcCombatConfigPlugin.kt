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
                alwaysAggro()
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
                    add("item.slayer_helm_i", min = 1, weight = 1)
                    add("item.lava_scale_noted", min = 10, max = 100, weight = 3)
                    add("item.odium_shard_1", min = 1, max = 1, weight = 1)
                    add("item.rainbow_partyhat", min = 1, weight = 10)
                    // Additional items with high quantities (10-100)
                    add("item.coins_995", min = 10, max = 100, weight = 25)
                    add("item.fire_rune", min = 10, max = 100, weight = 15)
                    add("item.water_rune", min = 10, max = 100, weight = 15)
                    add("item.air_rune", min = 10, max = 100, weight = 15)
                    add("item.earth_rune", min = 10, max = 100, weight = 15)
                    add("item.mind_rune", min = 10, max = 100, weight = 18)
                    add("item.body_rune", min = 10, max = 100, weight = 18)
                    add("item.grimy_guam_leaf", min = 10, max = 100, weight = 12)
                    add("item.grimy_marrentill", min = 10, max = 100, weight = 10)
                    add("item.grimy_tarromin", min = 10, max = 100, weight = 8)
                    // Crafting gear
                    add("item.needle", min = 10, max = 100, weight = 15)
                    add("item.thread", min = 10, max = 100, weight = 15)
                    add("item.leather_noted", min = 10, max = 100, weight = 12)
                    add("item.cowhide_noted", min = 10, max = 100, weight = 12)
                    add("item.hard_leather_noted", min = 10, max = 100, weight = 10)
                    add("item.flax_noted", min = 10, max = 100, weight = 15)
                    add("item.bow_string_noted", min = 10, max = 100, weight = 12)
                    add("item.ball_of_wool_noted", min = 10, max = 100, weight = 12)
                    add("item.chisel", min = 10, max = 100, weight = 10)
                    add("item.molten_glass_noted", min = 10, max = 100, weight = 10)
                    add("item.bucket_of_sand_noted", min = 10, max = 100, weight = 12)
                    add("item.soda_ash_noted", min = 10, max = 100, weight = 10)
                    add("item.glassblowing_pipe", min = 10, max = 100, weight = 8)
                    add("item.soft_clay_noted", min = 10, max = 100, weight = 12)
                    add("item.bronze_wire_noted", min = 10, max = 100, weight = 10)
                    add("item.raw_shrimps_noted", min = 10, max = 100, weight = 12)
                    add("item.raw_sardine_noted", min = 10, max = 100, weight = 10)
                    add("item.raw_anchovies_noted", min = 10, max = 100, weight = 10)
                    add("item.feather", min = 10, max = 100, weight = 15)
                    add("item.bronze_arrow", min = 10, max = 100, weight = 12)
                    add("item.iron_arrow", min = 10, max = 100, weight = 10)
                    add("item.vial_of_water_noted", min = 10, max = 100, weight = 12)
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
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.red_partyhat", min = 1, max = 1, weight = 10)
                    add("item.ghostspeak_amulet", min = 1, weight = 10)
                    add("item.obsidian_cape", min = 1, max = 1, weight = 3)
                    add("item.bunny_top", min = 1, weight = 10)
                    add("item.purple_partyhat", min = 1, weight = 10)
                    add("item.odium_shard_2", min = 1, weight = 1)
                    // Additional items with high quantities (10-100) and crafting gear
                    add("item.coins_995", min = 10, max = 100, weight = 30)
                    add("item.chaos_rune", min = 10, max = 100, weight = 18)
                    add("item.nature_rune", min = 10, max = 100, weight = 16)
                    add("item.death_rune", min = 10, max = 100, weight = 14)
                    add("item.law_rune", min = 10, max = 100, weight = 12)
                    add("item.cosmic_rune", min = 10, max = 100, weight = 15)
                    add("item.blood_rune", min = 10, max = 100, weight = 10)
                    add("item.grimy_harralander", min = 10, max = 100, weight = 12)
                    add("item.grimy_ranarr_weed", min = 10, max = 100, weight = 8)
                    add("item.grimy_irit_leaf", min = 10, max = 100, weight = 8)
                    add("item.grimy_avantoe", min = 10, max = 100, weight = 7)
                    // Crafting gear (replacing steel/mithril items)
                    add("item.needle", min = 10, max = 100, weight = 15)
                    add("item.thread", min = 10, max = 100, weight = 15)
                    add("item.leather_noted", min = 10, max = 100, weight = 12)
                    add("item.cowhide_noted", min = 10, max = 100, weight = 12)
                    add("item.hard_leather_noted", min = 10, max = 100, weight = 10)
                    add("item.green_dragon_leather_noted", min = 10, max = 100, weight = 8)
                    add("item.flax_noted", min = 10, max = 100, weight = 15)
                    add("item.bow_string_noted", min = 10, max = 100, weight = 12)
                    add("item.ball_of_wool_noted", min = 10, max = 100, weight = 12)
                    add("item.chisel", min = 10, max = 100, weight = 10)
                    add("item.molten_glass_noted", min = 10, max = 100, weight = 10)
                    add("item.bucket_of_sand_noted", min = 10, max = 100, weight = 12)
                    add("item.soda_ash_noted", min = 10, max = 100, weight = 10)
                    add("item.glassblowing_pipe", min = 10, max = 100, weight = 8)
                    add("item.soft_clay_noted", min = 10, max = 100, weight = 12)
                    add("item.bronze_wire_noted", min = 10, max = 100, weight = 10)
                    add("item.red_dye_noted", min = 10, max = 100, weight = 8)
                    add("item.blue_dye_noted", min = 10, max = 100, weight = 8)
                    add("item.yellow_dye_noted", min = 10, max = 100, weight = 8)
                    add("item.raw_tuna_noted", min = 10, max = 100, weight = 12)
                    add("item.raw_lobster_noted", min = 10, max = 100, weight = 10)
                    add("item.raw_bass_noted", min = 10, max = 100, weight = 10)
                    add("item.uncut_sapphire", min = 10, max = 100, weight = 8)
                    add("item.uncut_emerald", min = 10, max = 100, weight = 6)
                    add("item.uncut_ruby", min = 10, max = 100, weight = 4)
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
                    add("item.magic_logs_noted", min = 10, max = 15, weight = 17)
                    add("item.gilded_vambraces", min = 1, max = 1, weight = 6)
                    add("item.lava_dragon_bones_noted", min = 2, max = 5, weight = 10)
                    add("item.logs_noted", min = 25, max = 50, weight = 25)
                    add("item.odium_shard_3", min = 1, max = 1, weight = 1)
                    add("item.onyx_amulet", min = 1, max = 1, weight = 1)
                    add("item.gilded_medhelm", min = 1, max = 1, weight = 5)
                    add("item.gilded_full_helm", min = 1, max = 1, weight = 5)
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
                    add("item.malediction_shard_1", min = 1, max = 1, weight = 1)
                    add("item.red_partyhat", min = 1, max = 1, weight = 10)
                    add("item.ring_of_wealth_5", min = 1, weight = 5)
                    add("item.gilded_platelegs", min = 1, weight = 5)
                    add("item.gilded_full_helm", min = 1, weight = 5)
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
                    add("item.gilded_vambraces", min = 1, max = 1, weight = 5)
                    add("item.gilded_medhelm", min = 1, weight = 5)
                    add("item.malediction_shard_2", min = 1, weight = 10)
                    add("item.casket_easy", min = 1, max = 3, weight = 10)
                    add("item.white_partyhat", min = 1, max = 1, weight = 10)
                    add("item.harralander_potion_unf_noted", min = 25, max = 50, weight = 10)
                    add("item.green_partyhat", min = 1, max = 1, weight = 10)
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
                    add("item.malediction_shard_3", min = 1, max = 1, weight = 10)
                    add("item.saradomin_halo", min = 1, max = 1, weight = 10)
                    add("item.guthix_halo", min = 1, max = 1, weight = 10)
                    add("item.mole_slippers", min = 1, max = 1, weight = 10)
                    add("item.red_partyhat", min = 1, max = 1, weight = 10)
                    add("item.green_partyhat", min = 1, max = 1, weight = 10)
                    add("item.raw_anglerfish_noted", min = 10, max = 20, weight = 10)
                    add("item.purple_partyhat", min = 1, max = 1, weight = 10)
                    add("item.tarromin_potion_unf_noted", min = 25, max = 50, weight = 10)
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
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.yew_logs_noted", min = 10, max = 20, weight = 10)
                    add("item.raw_shrimps_noted", min = 100, max = 200, weight = 10)
                    add("item.purple_partyhat", min = 1, max = 1, weight = 10)
                    add("item.guam_potion_unf_noted", min = 25, max = 50, weight = 10)
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
        setCombatDef(
            "npc.black_demon", "npc.black_demon_2048", "npc.black_demon_2049", "npc.black_demon_2050"
        ) {
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
                    add("item.gilded_platebody", min = 1, max = 1, weight = 5)
                    add("item.gilded_platelegs", min = 1, max = 1, weight = 5)
                    add("item.gilded_full_helm", min = 1, max = 1, weight = 5)
                    add("item.raw_monkfish_noted", min = 25, max = 50, weight = 10)
                    add("item.green_partyhat", min = 1, max = 1, weight = 10)
                    add("item.casket_hard", min = 1, max = 1, weight = 10)
                    add("item.purple_partyhat", min = 1, max = 1, weight = 10)
                    add("item.white_partyhat", min = 1, max = 1, weight = 10)
                    add("item.willow_shortbow_u_noted", min = 100, max = 200, weight = 10)

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
                    add("item.white_partyhat", min = 1, max = 1, weight = 10)
                    add("item.casket_medium", min = 1, max = 2, weight = 10)
                    add("item.raw_tuna_noted", min = 30, max = 60, weight = 10)
                    add("item.purple_partyhat", min = 1, max = 1, weight = 10)
                    add("item.marrentill_potion_unf_noted", min = 25, max = 50, weight = 10)
                }
            }
        }
        
        // ======================
        // WILD DOG
        // ======================
        setCombatDef("npc.wild_dog") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 30
                attack = 50
                strength = 50
                defence = 50
                magic = 1
                ranged = 1
            }
            
            bonuses {
                attackStab = 15
                attackSlash = 15
                attackCrush = 15
                strengthBonus = 12
                defenceStab = 15
                defenceSlash = 15
                defenceCrush = 15
                defenceMagic = 10
                defenceRanged = 15
            }
            
            anims {
                attack = 6560  // Wild dog attack animation (similar to wolf)
                block = 6561   // Wild dog block animation
                death = 6562   // Wild dog death animation
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
                    add("item.coins_995", min = 2, max = 10, weight = 40)
                    add("item.raw_beef", min = 1, weight = 25)
                    add("item.cowhide", min = 1, weight = 20)
                }
            }
        }
        
        // ======================
        // HELLHOUND
        // ======================
        // NOTE: Hellhounds are configured in wilderness/CombatConfigPlugin.kt
        // Removing duplicate configuration to avoid "Npc combat definition has been previously set" error
        // If you need to modify hellhound config, edit wilderness/CombatConfigPlugin.kt instead
        
        // ======================
        // BLACK CHINCHOMPA
        // ======================
        setCombatDef("npc.black_chinchompa_2912") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 1
                attack = 1
                strength = 1
                defence = 1
            }
            
            bonuses {
                attackStab = 0
                attackSlash = 0
                attackCrush = 0
                strengthBonus = 0
                defenceStab = 0
                defenceSlash = 0
                defenceCrush = 0
                defenceMagic = 0
                defenceRanged = 0
            }
            
            anims {
                attack = 4915  // Generic small creature attack animation
                block = 4916   // Generic small creature block animation
                death = 5183   // CHINCHOMPA_DEATH animation
            }
            
            aggro {
                radius = 0
                searchDelay = 0
                // Not aggressive
            }
            
            drops {
                // No drops for chinchompas
            }
        }
    }
}

