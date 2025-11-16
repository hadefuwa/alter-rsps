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
    }
}

