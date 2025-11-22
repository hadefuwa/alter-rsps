package org.alter.plugins.content.areas.taverley

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

/**
 * Taverley Dungeon Monster Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all monsters in Taverley dungeon,
 * making them aggressive towards players. It sets up proper combat stats, animations,
 * respawn delays, and most importantly, aggression settings.
 * 
 * Aggression System:
 * - aggressiveRadius: The distance (in tiles) at which the NPC will detect and attack players
 * - searchDelay: How often (in cycles) the NPC checks for nearby targets
 * - alwaysAggro(): Sets aggressiveTimer to Int.MAX_VALUE, meaning the NPC will always
 *   be aggressive regardless of how long the player has been in the area
 * 
 * The aggression system works in conjunction with NpcAggroPlugin, which handles the
 * actual target detection and attack initiation.
 */
class CombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Skeletons Combat Configuration
         * Undead warriors found in the dungeon.
         */
        setCombatDef("npc.skeleton") {
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
         * Spiders Combat Configuration
         * Small aggressive arachnids.
         */
        setCombatDef("npc.spider") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 15
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
        
        // Giant Bats are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        
        // Ghosts are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        
        /**
         * Chaos Druids Combat Configuration
         * Magic-using enemies.
         */
        setCombatDef("npc.chaos_druid") {
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
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
        }
        
        // Black Knights are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        
        // Poison Scorpions are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        // Chaos Dwarfs are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        // Lesser Demons are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        // Hill Giants are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        // Blue Dragons are already configured in DragonConfigsPlugin - skipping to avoid duplicate
        
        // Black Demons are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        // Poison Spiders are already configured in GenericNpcCombatConfigPlugin - skipping to avoid duplicate
        
        
        /**
         * Hellhounds Combat Configuration
         * Powerful demonic hounds.
         */
        setCombatDef("npc.hellhound_104") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 116
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 8
                searchDelay = 3
                alwaysAggro()
            }
        }
        
        setCombatDef("npc.hellhound_105") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 116
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 8
                searchDelay = 3
                alwaysAggro()
            }
        }
        
        // Black Dragons are already configured in DragonConfigsPlugin - skipping to avoid duplicate
    }
}

