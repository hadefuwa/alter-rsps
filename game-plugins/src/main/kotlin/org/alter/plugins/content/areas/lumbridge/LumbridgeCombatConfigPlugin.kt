package org.alter.plugins.content.areas.lumbridge

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
 * Lumbridge Area Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all Lumbridge area monsters,
 * setting up proper combat stats, animations, respawn delays, and loot drops
 * for creatures that players can fight around Lumbridge.
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class LumbridgeCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Goblins Combat Configuration  
         * Level 2 creatures, aggressive to low-level players.
         */
        setCombatDef("npc.goblin", "npc.goblin_656", "npc.goblin_657", "npc.goblin_658", "npc.goblin_659", "npc.goblin_660") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 5
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                }
                main(64) {
                    add(995, min = 2, max = 10, weight = 30) // coins 2-10
                    add(1239, 1, weight = 5) // bronze spear
                    add(1205, 1, weight = 8) // bronze dagger
                    add(1277, 1, weight = 3) // bronze sword
                }
                tertiary(32) {
                    add(558, min = 2, max = 7, weight = 10) // mind runes
                    add(559, min = 2, max = 5, weight = 5) // body runes
                    add(557, min = 3, max = 9, weight = 8) // earth runes
                }
            }
        }

        /**
         * Hobgoblins Combat Configuration
         * Level 28 creatures, stronger than goblins.
         */
        setCombatDef("npc.hobgoblin") {
            configs {
                attackSpeed = 4
                respawnDelay = 45
            }
            stats {
                hitpoints = 29
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                }
                main(128) {
                    add(995, min = 5, max = 25, weight = 25) // coins 5-25
                    add(1205, 1, weight = 12) // iron dagger
                    add(1279, 1, weight = 8) // iron sword
                }
                tertiary(32) {
                    add(562, min = 1, max = 3, weight = 10) // chaos runes  
                    add(563, 1, weight = 5) // law rune
                }
            }
        }

        /**
         * Men Configuration
         * Level 2 human NPCs around Lumbridge.
         */
        setCombatDef("npc.man_385") {
            configs {
                attackSpeed = 4
                respawnDelay = 20
            }
            stats {
                hitpoints = 7
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop  
                    add(995, min = 1, max = 5) // coins 1-5 - guaranteed drop for testing
                }
            }
        }

        /**
         * Women Configuration
         * Level 2 human NPCs around Lumbridge.
         */
        setCombatDef("npc.woman", "npc.woman_1130", "npc.woman_1131", "npc.woman_1139", "npc.woman_1140", "npc.woman_1141", "npc.woman_1142") {
            configs {
                attackSpeed = 4
                respawnDelay = 20
            }
            stats {
                hitpoints = 7
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                    add(995, min = 1, max = 5) // coins 1-5 - guaranteed drop for testing
                }
            }
        }

        /**
         * Chickens Configuration
         * Level 1 creatures, perfect for absolute beginners.
         */
        setCombatDef("npc.chicken_1173", "npc.chicken_1174", "npc.chicken_2804", "npc.chicken_2805", "npc.chicken_2806") {
            configs {
                attackSpeed = 4
                respawnDelay = 15
            }
            stats {
                hitpoints = 3
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                }
                main(64) {
                    add(314, 1, weight = 50) // raw chicken - common
                    add(2138, min = 5, max = 25, weight = 15) // feathers
                }
            }
        }

    }
}
