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
         * Rats Combat Configuration
         * Level 1 creatures, perfect for absolute beginners.
         */
        setCombatDef("npc.rat_2854") {
            configs {
                attackSpeed = 4
                respawnDelay = 15
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
                    add(995, min = 1, max = 3, weight = 40) // coins 1-3
                    add(2134, 1, weight = 30) // raw rat meat
                }
            }
        }

        /**
         * Imps Combat Configuration
         * Level 2 creatures, fast attacks, non-aggressive.
         */
        setCombatDef("npc.imp_5007") {
            configs {
                attackSpeed = 3
                respawnDelay = 20
            }
            stats {
                hitpoints = 12
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
                    add(995, min = 1, max = 5, weight = 35) // coins 1-5
                    add(2309, 1, weight = 15) // bread
                    add(1919, 1, weight = 10) // beer
                    add(1474, 1, weight = 8) // red bead
                    add(1472, 1, weight = 8) // yellow bead
                    add(1470, 1, weight = 8) // white bead
                    add(1476, 1, weight = 6) // black bead
                }
                tertiary(20) {
                    add(11256, 1, weight = 20) // imp jar
                }
            }
        }

        /**
         * Sheep Combat Configuration
         * Level 1 creatures, peaceful, slow attacks.
         */
        setCombatDef("npc.sheep_2789") {
            configs {
                attackSpeed = 5
                respawnDelay = 20
            }
            stats {
                hitpoints = 8
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                    add(2136, 1) // raw mutton - always drop
                }
                main(64) {
                    add(1737, min = 1, max = 3, weight = 50) // wool 1-3
                    add(995, min = 1, max = 4, weight = 20) // coins 1-4
                }
            }
        }

        /**
         * Rams Combat Configuration
         * Level 2 creatures, mildly aggressive.
         */
        setCombatDef("npc.ram_1265") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 12
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                    add(2136, 1) // raw mutton - always drop
                }
                main(64) {
                    add(1737, min = 2, max = 4, weight = 50) // wool 2-4 (better than sheep)
                    add(995, min = 2, max = 6, weight = 25) // coins 2-6
                }
            }
        }

        /**
         * Giant Spiders Combat Configuration
         * Level 2 creatures, non-aggressive.
         */
        setCombatDef("npc.giant_spider", "npc.giant_spider_3017", "npc.giant_spider_3018", "npc.huge_spider_134") {
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
            
            drops {
                always {
                    add(526, 1) // bones - always drop
                }
                main(64) {
                    add(995, min = 2, max = 8, weight = 30) // coins 2-8
                    add(2237, 1, weight = 25) // spider silk
                    add(2239, 1, weight = 20) // spider legs
                }
                tertiary(32) {
                    add(2238, 1, weight = 10) // poison spider eggs (rare)
                }
            }
        }

        /**
         * Goblins Combat Configuration  
         * Level 2 creatures, aggressive to low-level players.
         * Includes all goblin variants found in Lumbridge.
         */
        setCombatDef("npc.goblin", "npc.goblin_656", "npc.goblin_657", "npc.goblin_658", "npc.goblin_659", "npc.goblin_660", 
                     "npc.goblin_3028", "npc.goblin_2248", "npc.goblin_2484", "npc.goblin_3039", "npc.goblin_3054") {
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
         * Includes all man variants found in Lumbridge.
         */
        setCombatDef("npc.man_385", "npc.man_3106", "npc.man_3108", "npc.man_3109", "npc.man_3014") {
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
         * Includes all woman variants found in Lumbridge.
         */
        setCombatDef("npc.woman", "npc.woman_1130", "npc.woman_1131", "npc.woman_1139", "npc.woman_1140", "npc.woman_1141", "npc.woman_1142",
                     "npc.woman_3111", "npc.woman_3112") {
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

        /**
         * Zombie Rats Combat Configuration
         * Level 3 creatures, undead rats.
         */
        setCombatDef("npc.zombie_rat", "npc.zombie_rat_3970") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 8
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
                    add(995, min = 1, max = 4, weight = 35) // coins 1-4
                    add(2134, 1, weight = 20) // raw rat meat
                    add(1897, 1, weight = 15) // rotten food
                }
            }
        }

        /**
         * Drunken Man Configuration
         * Level 2 human NPC, non-combat NPC that can be attacked.
         */
        setCombatDef("npc.drunken_man") {
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
                    add(995, min = 1, max = 5) // coins 1-5
                }
            }
        }

        /**
         * Giant Frogs Combat Configuration
         * Level 2 creatures, found in Lumbridge Swamp area.
         */
        setCombatDef("npc.giant_frog") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 10
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
                    add(995, min = 1, max = 5, weight = 35) // coins 1-5
                    add(2134, 1, weight = 25) // raw rat meat (frog meat)
                }
            }
        }

    }
}
