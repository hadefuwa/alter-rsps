package org.alter.plugins.content.areas.canifis

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * Canifis/Morytania Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for NPCs in the Canifis and Morytania area.
 */
class CombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Vampyre Juvinate Combat Configuration
         * 
         * Young vampyres found in Morytania.
         * - Hitpoints: 40 (moderate HP)
         * - Aggressive Radius: 6 tiles
         */
        setCombatDef("npc.vampyre_juvinate") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 40
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
                    // Vyre clothing - common drops
                    add("item.vyrewatch_top", min = 1, weight = 15)
                    add("item.vyrewatch_top_noted", min = 1, weight = 12)
                    add("item.vyrewatch_legs", min = 1, weight = 15)
                    add("item.vyrewatch_legs_noted", min = 1, weight = 12)
                    add("item.vyrewatch_shoes", min = 1, weight = 15)
                    add("item.vyrewatch_shoes_noted", min = 1, weight = 12)
                    
                    // Noted uncut gems - 3-40 quantity
                    add("item.uncut_sapphire_noted", min = 3, max = 40, weight = 20)
                    add("item.uncut_emerald_noted", min = 3, max = 40, weight = 18)
                    add("item.uncut_ruby_noted", min = 3, max = 40, weight = 15)
                    add("item.uncut_diamond_noted", min = 3, max = 40, weight = 12)
                    
                    // Other drops
                    add("item.coins_995", min = 50, max = 500, weight = 25)
                }
                
                // Rare drop: Vial of blood
                tertiary(weight = 128) {
                    add("item.vial_of_blood", min = 1, max = 3, weight = 128)
                    add("item.vial_of_blood_noted", min = 1, max = 3, weight = 100)
                }
            }
        }
    }
}

