package org.alter.plugins.content.areas.wilderness

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
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
 * Wilderness Monster Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all wilderness monsters, making them
 * aggressive towards players. It sets up proper combat stats, animations, respawn delays,
 * and most importantly, aggression settings.
 * 
 * Aggression System:
 * - aggressiveRadius: The distance (in tiles) at which the NPC will detect and attack players
 * - searchDelay: How often (in cycles) the NPC checks for nearby targets
 * - alwaysAggro(): Sets aggressiveTimer to Int.MAX_VALUE, meaning the NPC will always
 *   be aggressive regardless of how long the player has been in the area
 * 
 * The aggression system works in conjunction with NpcAggroPlugin, which handles the
 * actual target detection and attack initiation.
 * 
 * Combat Stats:
 * Each monster type has appropriate hitpoints, attack speed, and respawn delays
 * configured to match their difficulty level and role in the wilderness.
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
    
    /**
     * Initialize the plugin and configure combat definitions for all wilderness monsters.
     * 
     * Each setCombatDef block configures:
     * - configs: Attack speed and respawn delay
     * - stats: Hitpoints (other stats use NPC defaults from cache)
     * - anims: Attack, block, and death animations
     * - aggro: Aggression settings (radius, search delay, always aggressive)
     */
    init {
        /**
         * Dark Wizards Combat Configuration
         * 
         * Low-level magic users that attack players with spells.
         * - Attack Speed: 4 cycles (standard magic attack speed)
         * - Respawn Delay: 25 cycles (quick respawn for low-level area)
         * - Hitpoints: 25 (low HP, easy to kill)
         * - Aggressive Radius: 7 tiles (moderate detection range)
         * - Search Delay: 3 cycles (checks for targets frequently)
         * - Always Aggressive: Yes (will attack any player within range)
         */
        // setCombatDef() is a function that sets up how a monster fights and behaves
        // The string "npc.dark_wizard" tells it which monster to configure
        // Everything inside the curly braces { } is the configuration for that monster
        setCombatDef("npc.dark_wizard") {
            // configs block: Basic combat settings
            configs {
                // attackSpeed = 4 means the monster attacks every 4 game cycles (cycles are like ticks)
                // Lower number = faster attacks. 4 is standard speed.
                attackSpeed = 4
                
                // respawnDelay = 15 means after the monster dies, wait 15 cycles before respawning it
                // Lower number = faster respawn. 15 cycles is faster respawn for quicker farming.
                respawnDelay = 15
            }
            
            // stats block: Combat statistics
            stats {
                // hitpoints = 25 means the monster has 25 HP (health points)
                // When HP reaches 0, the monster dies
                hitpoints = 25
            }
            
            // anims block: Animation IDs that play during combat
            anims {
                // attack = 711 is the animation ID that plays when the monster attacks
                // You can find animation IDs in the game's animation files
                attack = 711
                
                // block = 424 is the animation that plays when the monster blocks an attack
                block = 424
                
                // death = 836 is the animation that plays when the monster dies
                death = 836
            }
            
            // aggro block: Aggression settings (makes the monster attack players)
            aggro {
                // radius = 7 means the monster will detect and attack players within 7 tiles
                // Think of it like a circle around the monster - if a player enters, it attacks
                radius = 7
                
                // searchDelay = 3 means check for nearby players every 3 game cycles
                // Lower number = checks more often (faster reaction)
                searchDelay = 3
                
                // alwaysAggro() makes the monster ALWAYS aggressive (never stops being aggressive)
                // Without this, monsters might stop being aggressive after a certain time
                alwaysAggro()
            }
            
            // Add loot drops for Dark Wizards
            drops {
                always {
                    add(526, 1) // bones
                }
                
                main(weight = 100) {
                    // Random runes in quantities of 100
                    add("item.air_rune", min = 100, max = 100, weight = 10)
                    add("item.water_rune", min = 100, max = 100, weight = 10)
                    add("item.earth_rune", min = 100, max = 100, weight = 10)
                    add("item.fire_rune", min = 100, max = 100, weight = 10)
                    add("item.mind_rune", min = 100, max = 100, weight = 10)
                    add("item.body_rune", min = 100, max = 100, weight = 10)
                    add("item.chaos_rune", min = 100, max = 100, weight = 8)
                    add("item.death_rune", min = 100, max = 100, weight = 8)
                    add("item.nature_rune", min = 100, max = 100, weight = 8)
                    add("item.law_rune", min = 100, max = 100, weight = 6)
                    add("item.cosmic_rune", min = 100, max = 100, weight = 6)
                    add("item.blood_rune", min = 100, max = 100, weight = 4)
                    add("item.soul_rune", min = 100, max = 100, weight = 2)
                }
            }
        }

        /**
         * Configure dark wizards to use magic combat and cast Wind Strike
         */
        onNpcSpawn(npc = "npc.dark_wizard") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
        }
        
        /**
         * Skeletons Combat Configuration
         * 
         * Undead warriors with moderate combat stats.
         * - Hitpoints: 25 (low-medium HP)
         * - Aggressive Radius: 7 tiles
         * 
         * NOTE: Skeleton combat definition is already set in TaverleyCombatConfigPlugin.
         * Commenting out to avoid duplicate definition error for NPC 70.
         */
        // Configure skeletons - same structure as dark wizards but different values
        // setCombatDef("npc.skeleton") {
        //     configs {
        //         attackSpeed = 4   // Same attack speed as dark wizards
        //         respawnDelay = 25 // Same respawn speed
        //     }
        //     stats {
        //         hitpoints = 25    // Same HP as dark wizards
        //     }
        //     anims {
        //         attack = 422      // Different animation - this is a melee attack (not magic)
        //         block = 424       // Same block animation
        //         death = 836       // Same death animation
        //     }
        //     aggro {
        //         radius = 7        // Same detection radius
        //         searchDelay = 3   // Same search frequency
        //         alwaysAggro()     // Always aggressive
        //     }
        //     
        //     // Add loot drops for Skeletons
        //     drops {
        //         always {
        //             add("item.bones", 1)
        //         }
        //         
        //         main(weight = 100) {
        //             add("item.coins_995", min = 3, max = 12, weight = 45)
        //             add("item.bronze_arrow", min = 5, max = 15, weight = 25)
        //             add("item.iron_arrow", min = 2, max = 8, weight = 15)
        //             add("item.bronze_axe", min = 1, weight = 5)
        //             add("item.bronze_sword", min = 1, weight = 5)
        //             add("item.bronze_dagger", min = 1, weight = 8)
        //             add("item.iron_dagger", min = 1, weight = 3)
        //         }
        //     }
        // }
        
        /**
         * Bandits Combat Configuration
         * 
         * Human bandits with higher HP than skeletons.
         * Configured for all bandit variants (690, 691, 692).
         * - Hitpoints: 42 (medium HP, more durable)
         * - Aggressive Radius: 7 tiles
         */
        // You can configure multiple NPC types at once by listing them separated by commas
        // This applies the same settings to all three bandit variants (690, 691, 692)
        setCombatDef("npc.bandit_690", "npc.bandit_691", "npc.bandit_692") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 42  // Higher HP than skeletons (42 vs 25) - bandits are tougher
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
            
            // Add loot drops for Bandits
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 8, max = 2500, weight = 35)
                    add("item.bread", min = 1, weight = 20)
                    add("item.beer", min = 1, weight = 15)
                    add("item.iron_dagger", min = 1, weight = 10)
                    add("item.iron_sword", min = 1, weight = 8)
                    add("item.steel_dagger", min = 1, weight = 5)
                    add("item.leather_boots", min = 1, weight = 10)
                    add("item.leather_gloves", min = 1, weight = 8)
                    add("item.lockpick", min = 1, weight = 3)
                }
            }
        }
        
        /**
         * Chaos Druids Combat Configuration
         * 
         * Magic-using druids similar to dark wizards.
         * - Hitpoints: 25 (low HP)
         * - Aggressive Radius: 7 tiles
         * 
         * NOTE: Chaos Druid combat definition is already set in TaverleyCombatConfigPlugin.
         * Commenting out to avoid duplicate definition error for NPC 520.
         */
        // setCombatDef("npc.chaos_druid") {
        //     configs {
        //         attackSpeed = 4
        //         respawnDelay = 25
        //     }
        //     stats {
        //         hitpoints = 25
        //     }
        //     anims {
        //         attack = 422
        //         block = 424
        //         death = 836
        //     }
        //     aggro {
        //         radius = 7
        //         searchDelay = 3
        //         alwaysAggro()
        //     }
        //     
        //     // Add loot drops for Chaos Druids
        //     drops {
        //         always {
        //             add("item.bones", 1)
        //         }
        //         
        //         main(weight = 100) {
        //             add("item.coins_995", min = 6, max = 18, weight = 30)
        //             add("item.grimy_guam_leaf", min = 1, weight = 20)
        //             add("item.grimy_marrentill", min = 1, weight = 15)
        //             add("item.grimy_tarromin", min = 1, weight = 12)
        //             add("item.grimy_harralander", min = 1, weight = 10)
        //             add("item.grimy_ranarr_weed", min = 1, weight = 8)
        //             add("item.grimy_irit_leaf", min = 1, weight = 6)
        //             add("item.nature_rune", min = 1, max = 3, weight = 10)
        //             add("item.law_rune", min = 1, max = 2, weight = 8)
        //             add("item.chaos_rune", min = 2, max = 5, weight = 12)
        //         }
        //     }
        // }
        
        /**
         * Wolves Combat Configuration
         * 
         * Fast-moving predators with unique animations.
         * - Hitpoints: 20 (low HP but fast)
         * - Aggressive Radius: 7 tiles
         * - Uses wolf-specific animations (6559, 6558, 6557)
         */
        setCombatDef("npc.wolf") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 20  // Low HP but fast attackers
            }
            anims {
                attack = 6559  // Wolf-specific attack animation
                block = 6558   // Wolf-specific block animation
                death = 6557   // Wolf-specific death animation
            }
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            // Add loot drops for Wolves
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 2, max = 8, weight = 40)
                    add("item.raw_beef", min = 1, weight = 25)
                    add("item.cowhide", min = 1, weight = 20)
                    add("item.wolf_bones", min = 1, weight = 15)
                }
            }
        }
        
        /**
         * White Wolves Combat Configuration
         * 
         * White wolves with unique drop table including noted bones.
         * - Hitpoints: 20 (low HP but fast)
         * - Aggressive Radius: 7 tiles
         * - Uses wolf-specific animations (6559, 6558, 6557)
         */
        setCombatDef("npc.white_wolf") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 20  // Low HP but fast attackers
            }
            anims {
                attack = 6559  // Wolf-specific attack animation
                block = 6558   // Wolf-specific block animation
                death = 6557   // Wolf-specific death animation
            }
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            // Add loot drops for White Wolves - noted bones
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    // Noted bones drops
                    add("item.big_bones_noted", min = 1, max = 50, weight = 30)
                    add("item.wolf_bones_noted", min = 1, max = 50, weight = 30)
                    
                    // Other drops
                    add("item.coins_995", min = 10, max = 50000, weight = 20)
                    add("item.raw_beef_noted", min = 1, max = 20, weight = 15)
                    add("item.cowhide_noted", min = 1, max = 20, weight = 5)
                }
            }
        }
        
        /**
         * Dark Warriors Combat Configuration
         * 
         * Elite warriors with high combat stats.
         * - Hitpoints: 60 (high HP, dangerous enemies)
         * - Aggressive Radius: 7 tiles
         */
        setCombatDef("npc.dark_warrior") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 60  // High HP, elite warriors
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
            
            // Add loot drops for Dark Warriors
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 15, max = 40, weight = 30)
                    add("item.iron_dagger", min = 1, weight = 15)
                    add("item.iron_sword", min = 1, weight = 12)
                    add("item.steel_dagger", min = 1, weight = 10)
                    add("item.steel_sword", min = 1, weight = 8)
                    add("item.mithril_dagger", min = 1, weight = 5)
                    add("item.iron_chainbody", min = 1, weight = 8)
                    add("item.steel_chainbody", min = 1, weight = 5)
                    add("item.iron_platebody", min = 1, weight = 3)
                    add("item.blood_rune", min = 1, max = 3, weight = 6)
                    add("item.death_rune", min = 1, max = 2, weight = 4)
                }
            }
        }
        
        /**
         * Green Dragons Combat Configuration
         * 
         * Powerful dragons with high HP and larger detection radius.
         * - Hitpoints: 75 (very high HP)
         * - Respawn Delay: 50 cycles (slower respawn for high-level monsters)
         * - Aggressive Radius: 10 tiles (larger detection range for dangerous monsters)
         * - Search Delay: 2 cycles (more frequent checks, faster reaction)
         * - Uses dragon-specific animations (91, 89, 92)
         */
        setCombatDef("npc.green_dragon") {
            configs {
                attackSpeed = 4
                respawnDelay = 50  // Slower respawn for high-level monsters
            }
            stats {
                hitpoints = 75  // Very high HP
            }
            anims {
                attack = 91   // Dragon attack animation
                block = 89    // Dragon block animation
                death = 92    // Dragon death animation
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                // radius = 10 means dragons detect players from further away (10 tiles vs 7)
                // This makes them more dangerous because they'll attack sooner
                radius = 10
                
                // searchDelay = 2 means check for players every 2 cycles (faster than 3)
                // This makes them react faster to players entering their area
                searchDelay = 2
                
                alwaysAggro()
            }
            
            // Add loot drops for Green Dragons
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.green_dragonhide", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 50, max = 150, weight = 25)
                    add("item.nature_rune", min = 15, max = 30, weight = 20)
                    add("item.law_rune", min = 3, max = 8, weight = 15)
                    add("item.air_rune", min = 30, max = 60, weight = 18)
                    add("item.fire_rune", min = 25, max = 50, weight = 16)
                    add("item.adamant_arrow", min = 10, max = 25, weight = 12)
                    add("item.mithril_sword", min = 1, weight = 6)
                    add("item.adamant_dagger", min = 1, weight = 4)
                    add("item.rune_dagger", min = 1, weight = 2)
                }
                
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Battle Mages Combat Configuration
         * 
         * Aggressive mages that spawn in the Mage Arena area of the wilderness.
         * They use god spells (Saradomin Strike, Claws of Guthix, Flames of Zamorak)
         * that hit through prayer protection.
         * Configured for all three battle mage variants (1610, 1611, 1612).
         * - Hitpoints: 50 (moderate HP for mid-level wilderness area)
         * - Respawn Delay: 30 cycles (moderate respawn speed)
         * - Aggressive Radius: 8 tiles (good detection range for mages)
         * - Search Delay: 2 cycles (fast reaction to players)
         * - Always Aggressive: Yes (will attack any player within range)
         * - Uses magic attack animation (811) for god spells
         */
        setCombatDef("npc.battle_mage", "npc.battle_mage_1611", "npc.battle_mage_1612") {
            configs {
                attackSpeed = 4  // Standard magic attack speed
                respawnDelay = 30  // Moderate respawn delay
            }
            stats {
                hitpoints = 50  // Moderate HP for mid-level wilderness monsters
            }
            anims {
                attack = 811   // God spell attack animation
                block = 424    // Standard block animation
                death = 836    // Standard death animation
            }
            aggro {
                radius = 8     // Good detection range for mages
                searchDelay = 2  // Fast reaction to players entering area
                alwaysAggro()   // Always aggressive - never stops being aggressive
            }
            
            // Add loot drops for Battle Mages
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 20, max = 100, weight = 30)
                    add("item.blood_rune", min = 2, max = 8, weight = 20)
                    add("item.death_rune", min = 3, max = 10, weight = 18)
                    add("item.chaos_rune", min = 5, max = 15, weight = 15)
                    add("item.law_rune", min = 2, max = 6, weight = 12)
                    add("item.nature_rune", min = 3, max = 8, weight = 10)
                    add("item.air_rune", min = 10, max = 30, weight = 15)
                    add("item.fire_rune", min = 10, max = 30, weight = 15)
                    add("item.water_rune", min = 10, max = 30, weight = 15)
                    add("item.earth_rune", min = 10, max = 30, weight = 15)
                }
            }
        }
        
        /**
         * Hellhounds Combat Configuration
         * 
         * Extremely dangerous high-level monsters with the highest HP.
         * Configured for both hellhound variants (104, 105).
         * - Hitpoints: 116 (extremely high HP)
         * - Respawn Delay: 50 cycles (slower respawn)
         * - Aggressive Radius: 10 tiles (large detection range)
         * - Search Delay: 2 cycles (very fast reaction)
         * - Uses hellhound-specific animations (6562, 6563, 6564)
         * 
         * NOTE: Hellhound combat definitions are already set in TaverleyCombatConfigPlugin.
         * Commenting out to avoid duplicate definition errors for NPCs 104 and 105.
         */
        // setCombatDef("npc.hellhound_104", "npc.hellhound_105") {
        //     configs {
        //         attackSpeed = 4
        //         // respawnDelay = 50 means wait 50 cycles before respawning (slower than low-level monsters)
        //         // High-level monsters take longer to respawn to make them feel more valuable
        //         respawnDelay = 50
        //     }
        //     stats {
        //         // hitpoints = 116 is VERY high - these are the toughest wilderness monsters
        //         // Players will need to hit them many times to kill them
        //         hitpoints = 116
        //     }
        //     anims {
        //         // Hellhounds have their own unique animations (different from other monsters)
        //         attack = 6562
        //         block = 6563
        //         death = 6564
        //     }
        //     aggro {
        //         // Same aggressive settings as dragons - large radius and fast reaction
        //         radius = 10
        //         searchDelay = 2
        //         alwaysAggro()
        //     }
        //     
        //     // Add loot drops for Hellhounds
        //     drops {
        //         always {
        //             add("item.bones", 1)
        //         }
        //         
        //         main(weight = 100) {
        //             add("item.coins_995", min = 75, max = 200, weight = 20)
        //             add("item.blood_rune", min = 5, max = 15, weight = 18)
        //             add("item.death_rune", min = 3, max = 10, weight = 15)
        //             add("item.soul_rune", min = 2, max = 8, weight = 12)
        //             add("item.nature_rune", min = 8, max = 20, weight = 16)
        //             add("item.law_rune", min = 5, max = 12, weight = 14)
        //             add("item.rune_dagger", min = 1, weight = 6)
        //             add("item.rune_sword", min = 1, weight = 4)
        //             add("item.adamant_platebody", min = 1, weight = 3)
        //             add("item.rune_chainbody", min = 1, weight = 2)
        //             add("item.uncut_emerald", min = 1, max = 3, weight = 8)
        //             add("item.uncut_ruby", min = 1, max = 2, weight = 5)
        //             add("item.uncut_diamond", min = 1, weight = 3)
        //         }
        //         
        //         tertiary(weight = 512) {
        //             add("item.dragon_spear", min = 1, weight = 512)
        //             add("item.rune_platebody", min = 1, weight = 384)
        //         }
        //     }
        // }
        
        /**
         * Dwarf Combat Configuration
         * 
         * Dwarfs that spawn in the wilderness with mining-related drops.
         * - Hitpoints: 30 (moderate HP)
         * - Aggressive Radius: 6 tiles
         */
        setCombatDef("npc.dwarf_290") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 30
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 6
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    // Noted pickaxes - various tiers
                    add("item.bronze_pickaxe_noted", min = 1, max = 5, weight = 25)
                    add("item.iron_pickaxe_noted", min = 1, max = 5, weight = 20)
                    add("item.steel_pickaxe_noted", min = 1, max = 5, weight = 15)
                    add("item.mithril_pickaxe_noted", min = 1, max = 3, weight = 12)
                    add("item.adamant_pickaxe_noted", min = 1, max = 2, weight = 8)
                    add("item.rune_pickaxe_noted", min = 1, max = 2, weight = 5)
                    
                    // Noted ores - various types
                    add("item.copper_ore_noted", min = 5, max = 20, weight = 20)
                    add("item.tin_ore_noted", min = 5, max = 20, weight = 20)
                    add("item.iron_ore_noted", min = 3, max = 15, weight = 18)
                    add("item.silver_ore_noted", min = 2, max = 10, weight = 15)
                    add("item.gold_ore_noted", min = 2, max = 10, weight = 12)
                    add("item.mithril_ore_noted", min = 1, max = 5, weight = 10)
                    add("item.adamantite_ore_noted", min = 1, max = 3, weight = 6)
                    add("item.runite_ore_noted", min = 1, max = 2, weight = 3)
                    
                    // Other drops
                    add("item.coins_995", min = 50, max = 500, weight = 25)
                }
            }
        }
        
        /**
         * Pirates - Wilderness
         * 
         * Aggressive pirates found in the wilderness. They drop smithing items,
         * bars, and pickaxes (all noted).
         * 
         * Combat Level: Varies
         * - Aggressive Radius: 6 tiles
         */
        setCombatDef("npc.pirate", "npc.pirate_522", "npc.pirate_523", "npc.pirate_524") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            stats {
                hitpoints = 50
                attack = 40
                strength = 40
                defence = 35
            }
            bonuses {
                attackStab = 10
                attackSlash = 12
                attackCrush = 8
                defenceStab = 15
                defenceSlash = 15
                defenceCrush = 15
            }
            anims {
                attack = 422
                block = 424
                death = 836
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
                
                // Comprehensive drop table with smithing items, bars, and pickaxes (all noted)
                // Total item weights: ~280, using weight = 300 for ~93% drop chance
                main(weight = 300) {
                    // ========== NOTED BARS ==========
                    add("item.bronze_bar_noted", min = 5, max = 50, weight = 30)
                    add("item.iron_bar_noted", min = 5, max = 50, weight = 28)
                    add("item.steel_bar_noted", min = 3, max = 30, weight = 25)
                    add("item.mithril_bar_noted", min = 2, max = 20, weight = 20)
                    add("item.adamantite_bar_noted", min = 1, max = 15, weight = 15)
                    add("item.runite_bar_noted", min = 1, max = 10, weight = 10)
                    
                    // ========== NOTED PICKAXES ==========
                    add("item.bronze_pickaxe_noted", min = 1, max = 10, weight = 25)
                    add("item.iron_pickaxe_noted", min = 1, max = 10, weight = 22)
                    add("item.steel_pickaxe_noted", min = 1, max = 8, weight = 18)
                    add("item.mithril_pickaxe_noted", min = 1, max = 5, weight = 15)
                    add("item.adamant_pickaxe_noted", min = 1, max = 3, weight = 12)
                    add("item.rune_pickaxe_noted", min = 1, max = 2, weight = 8)
                    
                    // ========== NOTED SMITHING ITEMS (HAMMERS) ==========
                    add("item.hammer_noted", min = 1, max = 10, weight = 20)
                    add("item.bronze_warhammer_noted", min = 1, max = 5, weight = 15)
                    add("item.iron_warhammer_noted", min = 1, max = 5, weight = 12)
                    add("item.steel_warhammer_noted", min = 1, max = 3, weight = 10)
                    add("item.mithril_warhammer_noted", min = 1, max = 3, weight = 8)
                    add("item.adamant_warhammer_noted", min = 1, max = 2, weight = 6)
                    add("item.rune_warhammer_noted", min = 1, max = 2, weight = 4)
                    
                    // ========== OTHER SMITHING ITEMS ==========
                    add("item.coins_995", min = 100, max = 1000, weight = 30)
                }
            }
        }
        
        /**
         * Elder Chaos Druid Combat Configuration
         * 
         * Powerful magic-using druids that attack with spells.
         * - Attack Speed: 4 cycles (standard magic attack speed)
         * - Respawn Delay: 25 cycles (moderate respawn delay)
         * - Hitpoints: 60 (moderate HP for mid-level wilderness monster)
         * - Aggressive Radius: 7 tiles (moderate detection range)
         * - Search Delay: 3 cycles (checks for targets frequently)
         * - Always Aggressive: Yes (will attack any player within range)
         * - Combat Style: Magic (uses spells to attack)
         */
        setCombatDef("npc.elder_chaos_druid_7995") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 60
                magic = 80
            }
            
            bonuses {
                attackMagic = 50
                defenceMagic = 40
                defenceStab = 20
                defenceSlash = 20
                defenceCrush = 20
                defenceRanged = 20
            }
            
            anims {
                attack = 422 // Magic attack animation (similar to chaos druid)
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
                    add("item.coins_995", min = 50, max = 200, weight = 30)
                    add("item.chaos_rune", min = 5, max = 15, weight = 20)
                    add("item.death_rune", min = 2, max = 8, weight = 15)
                    add("item.nature_rune", min = 3, max = 10, weight = 12)
                    add("item.law_rune", min = 2, max = 6, weight = 10)
                    add("item.blood_rune", min = 1, max = 5, weight = 8)
                    add("item.grimy_guam_leaf", min = 1, max = 3, weight = 15)
                    add("item.grimy_marrentill", min = 1, max = 3, weight = 12)
                    add("item.grimy_tarromin", min = 1, max = 2, weight = 10)
                    add("item.grimy_harralander", min = 1, max = 2, weight = 8)
                }
            }
        }
        
        /**
         * Set Elder Chaos Druids to use Magic combat
         * 
         * When elder chaos druids spawn, configure them to attack with magic spells
         * instead of melee attacks.
         */
        onNpcSpawn(npc = "npc.elder_chaos_druid_7995") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
        }
    }
}

