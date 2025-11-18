package org.alter.plugins.content.areas.barbarianvillage

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
 * Combat / drop configuration for Barbarian Village NPCs.
 *
 * Currently only configures the main Barbarian (id 3055 in cache,
 * referenced as `npc.barbarian` via `npc.rscm`).
 */
class BarbarianVillageCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Barbarian (Alberich, a fierce barbarian warrior - npc id 3055)
        setCombatDef("npc.barbarian") {
            configs {
                attackSpeed = 4
                respawnDelay = 35
            }

            stats {
                hitpoints = 40
                attack = 30
                strength = 32
                defence = 28
            }

            anims {
                attack = 422
                block = 424
                death = 836
            }

            drops {
                // Always drop bones
                always {
                    add(526, 1)
                }

                // Main table: low-level melee gear, coins, food
                main(128) {
                    add(995, min = 10, max = 80, weight = 40) // coins
                    add(1323, 1, weight = 12) // iron scimitar
                    add(1295, 1, weight = 10) // iron longsword
                    add(1153, 1, weight = 10) // iron full helm
                    add(1115, 1, weight = 8)  // iron platebody
                    add(1067, 1, weight = 8)  // iron platelegs
                    add(882, min = 15, max = 60, weight = 10) // bronze arrows
                    add(379, min = 1, max = 3, weight = 10)  // cooked lobster
                }

                // Small chance at runes / better food
                tertiary(32) {
                    add(561, min = 2, max = 6, weight = 8)  // nature runes
                    add(562, min = 2, max = 6, weight = 6)  // chaos runes
                    add(385, min = 1, max = 2, weight = 4)  // shark
                }
            }
        }
    }
}
