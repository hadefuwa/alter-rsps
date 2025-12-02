package org.alter.plugins.content.areas.varrock

import org.alter.api.*
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
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.game.model.combat.CombatClass

/**
 * Varrock Area Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all Varrock area NPCs,
 * setting up proper combat stats, animations, respawn delays, and loot drops.
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class CombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Guards Combat Configuration
         * Level 21 guards that protect Varrock Castle and city.
         * - Hitpoints: 22 (moderate HP)
         * - Respawn Delay: 25 cycles
         */
        setCombatDef(
            "npc.guard_397", "npc.guard_398", "npc.guard_399", "npc.guard_400",
            "npc.guard_11912", "npc.guard_11913", "npc.guard_11914", "npc.guard_11915"  // Add new guard IDs here
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 22
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
                
                main(weight = 100) {
                    add("item.coins_995", min = 10, max = 30, weight = 50)
                    add("item.bread", min = 1, weight = 25)
                    add("item.iron_dagger", min = 1, weight = 15)
                    add("item.iron_sword", min = 1, weight = 10)
                    add("item.iron_chainbody", min = 1, weight = 8)
                    add("item.iron_platelegs", min = 1, weight = 8)
                    add("item.iron_full_helm", min = 1, weight = 5)
                }
            }
        }

        /**
         * Unicorn Combat Configuration
         * Level 15 creatures found near Varrock.
         * - Hitpoints: 15
         * - Respawn Delay: 30 cycles
         */
        setCombatDef("npc.unicorn") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            stats {
                hitpoints = 15
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add("item.bones", 1)
                    add("item.unicorn_horn", 1)
                }
            }
        }

        /**
         * Grizzly Bear Combat Configuration
         * Level 21 creatures found near Varrock.
         * - Hitpoints: 25
         * - Respawn Delay: 35 cycles
         */
        setCombatDef("npc.grizzly_bear") {
            configs {
                attackSpeed = 4
                respawnDelay = 35
            }
            stats {
                hitpoints = 25
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add("item.bones", 1)
                    add("item.bear_fur", 1)
                }
            }
        }

        /**
         * Dark Wizards Combat Configuration
         * NOTE: Dark wizards are configured in WildernessCombatConfigPlugin
         * as they are primarily wilderness NPCs. This configuration has been
         * removed to prevent duplicate combat definition errors.
         */

        /**
         * Moss Giants Combat Configuration
         * Level 42 creatures found in Varrock sewers.
         * - Hitpoints: 60
         * - Respawn Delay: 50 cycles
         * - Drops: Big bones (always), coins, runes, weapons, armor, herbs
         */
        setCombatDef("npc.moss_giant") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 60
            }
            anims {
                attack = 4658  // Moss giant attack animation
                block = 424
                death = 4659  // Moss giant death animation
            }
            
            drops {
                always {
                    add("item.big_bones", 1)  // Always drop big bones
                }
                
                main(weight = 128) {
                    // Coins - common drop
                    add("item.coins_995", min = 30, max = 120, weight = 40)
                    
                    // Runes - common drops
                    add("item.nature_rune", min = 2, max = 12, weight = 25)
                    add("item.law_rune", min = 1, max = 4, weight = 20)
                    add("item.chaos_rune", min = 3, max = 15, weight = 25)
                    add("item.death_rune", min = 1, max = 5, weight = 15)
                    add("item.air_rune", min = 10, max = 30, weight = 30)
                    add("item.fire_rune", min = 10, max = 30, weight = 30)
                    
                    // Weapons - medium tier
                    add("item.mithril_longsword", min = 1, weight = 8)
                    add("item.mithril_scimitar", min = 1, weight = 8)
                    add("item.mithril_sword", min = 1, weight = 8)
                    add("item.adamant_longsword", min = 1, weight = 5)
                    add("item.adamant_scimitar", min = 1, weight = 5)
                    add("item.rune_longsword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    
                    // Armor pieces
                    add("item.mithril_chainbody", min = 1, weight = 6)
                    add("item.mithril_platelegs", min = 1, weight = 6)
                    add("item.adamant_chainbody", min = 1, weight = 4)
                    add("item.adamant_platelegs", min = 1, weight = 4)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    
                    // Herbs
                    add("item.grimy_guam_leaf", min = 1, weight = 12)
                    add("item.grimy_marrentill", min = 1, weight = 12)
                    add("item.grimy_tarromin", min = 1, weight = 10)
                    add("item.grimy_harralander", min = 1, weight = 8)
                    add("item.grimy_ranarr_weed", min = 1, weight = 5)
                    add("item.grimy_irit_leaf", min = 1, weight = 5)
                }
                
                tertiary(weight = 6000) {
                    // Rare drops (each item rolls independently)
                    add("item.long_bone", min = 1, weight = 400)  // 1/400 chance
                    add("item.curved_bone", min = 1, weight = 5000)  // 1/5000 chance
                    add("item.rune_full_helm", min = 1, weight = 128)  // 1/128 chance
                    add("item.rune_platebody", min = 1, weight = 256)  // 1/256 chance
                }
            }
        }

        /**
         * Abomination Minions Combat Configuration
         * These are the minions spawned by the Sewer Abomination boss.
         * They are stronger than regular versions and have enhanced drops.
         * Minions are identified by not respawning (respawns = false).
         */

        /**
         * Abomination Melee Minion (Gnome Driver)
         * Spawned at 70% boss HP. Stronger than regular gnome drivers.
         * - Hitpoints: 40 (enhanced for boss minion)
         * - Respawn Delay: N/A (minions don't respawn)
         */
        setCombatDef("npc.gnome_driver") {
            configs {
                attackSpeed = 4
                respawnDelay = 25  // Only applies if spawned normally, minions don't respawn
            }
            stats {
                hitpoints = 40  // Enhanced HP for boss minion
                attack = 50
                strength = 50
                defence = 40
                magic = 1
                ranged = 1
            }
            anims {
                attack = 422
                block = 424
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
                    // Coins - better than regular zombies
                    add("item.coins_995", min = 50, max = 200, weight = 40)
                    
                    // Weapons - mid-tier
                    add("item.iron_sword", min = 1, weight = 15)
                    add("item.iron_dagger", min = 1, weight = 12)
                    add("item.steel_sword", min = 1, weight = 8)
                    add("item.steel_dagger", min = 1, weight = 6)
                    
                    // Armor
                    add("item.iron_chainbody", min = 1, weight = 10)
                    add("item.iron_platelegs", min = 1, weight = 10)
                    add("item.steel_chainbody", min = 1, weight = 5)
                    add("item.steel_platelegs", min = 1, weight = 5)
                }
                
                tertiary(weight = 64) {
                    // Runes
                    add("item.chaos_rune", min = 2, max = 8, weight = 15)
                    add("item.death_rune", min = 1, max = 4, weight = 10)
                }
            }
        }

        /**
         * Abomination Ranged Minion (Gnome Archer)
         * Spawned at 35% boss HP. Stronger than regular gnome archers.
         * - Hitpoints: 35 (enhanced for boss minion)
         * - Respawn Delay: N/A (minions don't respawn)
         */
        setCombatDef("npc.gnome_archer") {
            configs {
                attackSpeed = 4
                respawnDelay = 25  // Only applies if spawned normally, minions don't respawn
            }
            stats {
                hitpoints = 35  // Enhanced HP for boss minion
                attack = 40
                strength = 30
                defence = 35
                magic = 1
                ranged = 50
            }
            anims {
                attack = 422
                block = 424
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
                    // Coins - better than regular guard bandits
                    add("item.coins_995", min = 40, max = 150, weight = 40)
                    
                    // Ranged equipment
                    add("item.iron_arrow", min = 10, max = 50, weight = 25)
                    add("item.steel_arrow", min = 5, max = 30, weight = 15)
                    add("item.oak_shortbow", min = 1, weight = 8)
                    add("item.willow_shortbow", min = 1, weight = 5)
                    
                    // Weapons
                    add("item.iron_sword", min = 1, weight = 12)
                    add("item.steel_sword", min = 1, weight = 8)
                }
                
                tertiary(weight = 64) {
                    // Runes
                    add("item.air_rune", min = 5, max = 20, weight = 20)
                    add("item.mind_rune", min = 5, max = 20, weight = 15)
                }
            }
        }

        /**
         * Abomination Magic Minion (Gnome Mage)
         * Spawned at 10% boss HP. Stronger than regular gnome mages.
         * - Hitpoints: 45 (enhanced for boss minion)
         * - Respawn Delay: N/A (minions don't respawn)
         */
        setCombatDef("npc.gnome_mage") {
            configs {
                attackSpeed = 4
                respawnDelay = 25  // Only applies if spawned normally, minions don't respawn
            }
            stats {
                hitpoints = 45  // Enhanced HP for boss minion
                attack = 30
                strength = 20
                defence = 35
                magic = 60
                ranged = 1
            }
            anims {
                attack = 711  // Dark wizard attack animation
                block = 424
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
                    // Coins - better than regular bandits
                    add("item.coins_995", min = 60, max = 250, weight = 40)
                    
                    // Runes - enhanced drops
                    add("item.air_rune", min = 10, max = 30, weight = 30)
                    add("item.mind_rune", min = 5, max = 20, weight = 25)
                    add("item.chaos_rune", min = 3, max = 12, weight = 20)
                    add("item.death_rune", min = 2, max = 8, weight = 15)
                    add("item.law_rune", min = 1, max = 4, weight = 10)
                    
                    // Weapons - magic focused
                    add("item.staff_of_air", min = 1, weight = 8)
                    add("item.staff_of_fire", min = 1, weight = 6)
                    add("item.staff_of_water", min = 1, weight = 6)
                    add("item.staff_of_earth", min = 1, weight = 6)
                }
                
                tertiary(weight = 32) {
                    // Magic equipment
                    add("item.wizard_hat", min = 1, weight = 15)
                    add("item.wizard_robe_top", min = 1, weight = 12)
                    add("item.wizard_robe_skirt", min = 1, weight = 12)
                }
            }
        }

    }
}

