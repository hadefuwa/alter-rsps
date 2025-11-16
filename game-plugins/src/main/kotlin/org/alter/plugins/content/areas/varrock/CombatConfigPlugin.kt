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
        setCombatDef("npc.guard_397", "npc.guard_398", "npc.guard_399", "npc.guard_400") {
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
    }
}

