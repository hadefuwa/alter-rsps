package org.alter.plugins.content.areas.varrock

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Varrock Area Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all Varrock area monsters,
 * setting up proper combat stats, animations, respawn delays, and loot drops
 * for creatures that players can fight around Varrock.
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class VarrockCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Bat Combat Configuration
         * Level 1 creatures, found in various locations around Varrock.
         */
        setCombatDef("npc.bat") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            
            stats {
                hitpoints = 5
            }
            
            bonuses {
                attackStab = 2
                strengthBonus = 2
                defenceStab = 2
                defenceSlash = 2
                defenceCrush = 2
                defenceMagic = 1
                defenceRanged = 2
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
                // Always drop bones
                always {
                    add("item.bones", 1)
                }
                
                // Main drop table: noted bat bones (1-15)
                // Table weight 200 with item weight 100 = ~50% drop chance
                main(weight = 200) {
                    add("item.bat_bones_noted", min = 1, max = 15, weight = 100)
                }
            }
        }
    }
}

