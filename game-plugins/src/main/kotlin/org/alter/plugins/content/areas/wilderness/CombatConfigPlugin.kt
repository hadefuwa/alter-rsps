package org.alter.plugins.content.areas.wilderness

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
                
                // respawnDelay = 25 means after the monster dies, wait 25 cycles before respawning it
                // Lower number = faster respawn. 25 cycles is quick for low-level monsters.
                respawnDelay = 25
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
        }
        
        /**
         * Skeletons Combat Configuration
         * 
         * Undead warriors with moderate combat stats.
         * - Hitpoints: 25 (low-medium HP)
         * - Aggressive Radius: 7 tiles
         */
        // Configure skeletons - same structure as dark wizards but different values
        setCombatDef("npc.skeleton") {
            configs {
                attackSpeed = 4   // Same attack speed as dark wizards
                respawnDelay = 25 // Same respawn speed
            }
            stats {
                hitpoints = 25    // Same HP as dark wizards
            }
            anims {
                attack = 422      // Different animation - this is a melee attack (not magic)
                block = 424       // Same block animation
                death = 836       // Same death animation
            }
            aggro {
                radius = 7        // Same detection radius
                searchDelay = 3   // Same search frequency
                alwaysAggro()     // Always aggressive
            }
        }
        
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
        }
        
        /**
         * Chaos Druids Combat Configuration
         * 
         * Magic-using druids similar to dark wizards.
         * - Hitpoints: 25 (low HP)
         * - Aggressive Radius: 7 tiles
         */
        setCombatDef("npc.chaos_druid") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 25
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
        }
        
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
            aggro {
                // radius = 10 means dragons detect players from further away (10 tiles vs 7)
                // This makes them more dangerous because they'll attack sooner
                radius = 10
                
                // searchDelay = 2 means check for players every 2 cycles (faster than 3)
                // This makes them react faster to players entering their area
                searchDelay = 2
                
                alwaysAggro()
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
         */
        setCombatDef("npc.hellhound_104", "npc.hellhound_105") {
            configs {
                attackSpeed = 4
                // respawnDelay = 50 means wait 50 cycles before respawning (slower than low-level monsters)
                // High-level monsters take longer to respawn to make them feel more valuable
                respawnDelay = 50
            }
            stats {
                // hitpoints = 116 is VERY high - these are the toughest wilderness monsters
                // Players will need to hit them many times to kill them
                hitpoints = 116
            }
            anims {
                // Hellhounds have their own unique animations (different from other monsters)
                attack = 6562
                block = 6563
                death = 6564
            }
            aggro {
                // Same aggressive settings as dragons - large radius and fast reaction
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
        }
    }
}

