package org.alter.plugins.content.areas.hydra

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Alchemical Hydra Combat Configuration Plugin
 * 
 * Configures the Alchemical Hydra's combat stats and comprehensive drop table.
 * 
 * The Alchemical Hydra is a high-level boss (combat level 392) that requires
 * level 95 Slayer to fight. It has unique phase-based mechanics and drops
 * valuable items including hydra-specific uniques.
 */
class AlchemicalHydraConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatDef("npc.alchemical_hydra") {
            configs {
                attackSpeed = 4 // 4 ticks per attack (faster in enraged phase)
                respawnDelay = 50 // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10 // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 1100 // High HP for challenging boss fight
                attack = 300
                strength = 300
                defence = 300
                magic = 300
                ranged = 300
            }

            bonuses {
                attackStab = 200
                attackSlash = 200
                attackCrush = 200
                attackMagic = 250
                attackRanged = 250

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 180
                defenceRanged = 180

                attackBonus = 200
                strengthBonus = 250
                rangedStrengthBonus = 250
                magicDamageBonus = 250
            }

            anims {
                block = 424 // Generic block animation
                death = Animation.ALCHEMICAL_HYDRA_BEGIN_DEATH // Hydra death animation
            }

            drops {
                // ========================================================================
                // ALWAYS DROPS
                // ========================================================================
                always {
                    add("item.hydra_bones", min = 1, max = 1) // Always drops hydra bones
                }

                // ========================================================================
                // MAIN DROP TABLE
                // ========================================================================
                // Total item weights = ~1850, set weight to 2000 to allow ~7.5% no-drop chance
                main(weight = 2000) {
                    // Common drops - Coins and resources
                    add("item.coins_995", min = 50000, max = 150000, weight = 200) // 10% chance
                    add("item.death_rune", min = 200, max = 500, weight = 150) // 7.5% chance
                    add("item.blood_rune", min = 200, max = 500, weight = 150) // 7.5% chance
                    add("item.soul_rune", min = 100, max = 300, weight = 120) // 6% chance
                    add("item.fire_rune", min = 500, max = 1000, weight = 100) // 5% chance
                    add("item.water_rune", min = 500, max = 1000, weight = 100) // 5% chance
                    add("item.chaos_rune", min = 300, max = 600, weight = 100) // 5% chance
                    
                    // Food and potions
                    add("item.shark", min = 5, max = 15, weight = 120) // 6% chance
                    add("item.manta_ray", min = 3, max = 10, weight = 80) // 4% chance
                    add("item.super_restore4", min = 2, max = 5, weight = 60) // 3% chance
                    add("item.saradomin_brew4", min = 2, max = 5, weight = 60) // 3% chance
                    
                    // Herbs and seeds
                    add("item.grimy_ranarr_weed", min = 10, max = 30, weight = 80) // 4% chance
                    add("item.grimy_snapdragon", min = 5, max = 15, weight = 60) // 3% chance
                    add("item.grimy_torstol", min = 5, max = 15, weight = 50) // 2.5% chance
                    add("item.ranarr_seed", min = 1, max = 3, weight = 40) // 2% chance
                    add("item.snapdragon_seed", min = 1, max = 2, weight = 30) // 1.5% chance
                    add("item.torstol_seed", min = 1, max = 2, weight = 25) // 1.25% chance
                    
                    // Ores and bars
                    add("item.runite_ore", min = 5, max = 15, weight = 70) // 3.5% chance
                    add("item.runite_bar", min = 3, max = 10, weight = 50) // 2.5% chance
                    add("item.adamantite_ore", min = 10, max = 25, weight = 60) // 3% chance
                    add("item.adamantite_bar", min = 5, max = 15, weight = 40) // 2% chance
                    
                    // Alchemical Hydra specific items
                    add("item.hydra_leather", min = 1, max = 1, weight = 30) // 1.5% chance
                    add("item.hydra_tail", min = 1, max = 1, weight = 25) // 1.25% chance
                    
                    // Rare equipment
                    add("item.dragon_boots", min = 1, max = 1, weight = 15) // 0.75% chance
                    add("item.dragon_platelegs", min = 1, max = 1, weight = 10) // 0.5% chance
                    add("item.dragon_plateskirt", min = 1, max = 1, weight = 10) // 0.5% chance
                    add("item.dragon_med_helm", min = 1, max = 1, weight = 8) // 0.4% chance
                }

                // ========================================================================
                // TERTIARY DROP TABLE (Rare Uniques)
                // ========================================================================
                // Each item rolls independently - can get multiple drops
                tertiary(weight = 10000) {
                    // Hydra-specific unique items (OSRS drop rates)
                    add("item.hydras_claw", min = 1, max = 1, weight = 1000) // 1/1000 chance
                    add("item.hydras_heart", min = 1, max = 1, weight = 2000) // 1/2000 chance
                    add("item.hydras_fang", min = 1, max = 1, weight = 2000) // 1/2000 chance
                    add("item.hydras_eye", min = 1, max = 1, weight = 2000) // 1/2000 chance
                    
                    // Very rare drops
                    add("item.dragon_thrownaxe", min = 50, max = 100, weight = 500) // 1/500 chance
                    add("item.dragon_knife", min = 50, max = 100, weight = 500) // 1/500 chance
                    add("item.dragon_dart", min = 100, max = 200, weight = 400) // 1/400 chance
                    
                    // Rare equipment
                    add("item.dragon_chainbody", min = 1, max = 1, weight = 300) // 1/300 chance
                    add("item.dragon_spear", min = 1, max = 1, weight = 200) // 1/200 chance
                    add("item.dragon_longsword", min = 1, max = 1, weight = 150) // 1/150 chance
                    add("item.dragon_scimitar", min = 1, max = 1, weight = 150) // 1/150 chance
                    
                    // Alchemical Hydra head (for slayer helm upgrade)
                    add("item.alchemical_hydra_head", min = 1, max = 1, weight = 100) // 1/100 chance
                }
            }
        }
    }
}
