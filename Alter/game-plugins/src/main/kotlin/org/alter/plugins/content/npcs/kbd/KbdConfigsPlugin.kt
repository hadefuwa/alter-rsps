package org.alter.plugins.content.npcs.kbd

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

class KbdConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        setMultiCombatRegion(region = 9033)

        spawnNpc("npc.king_black_dragon", x = 2274, z = 4698, walkRadius = 5)

        setCombatDef("npc.king_black_dragon") {
            species {
                +NpcSpecies.DRACONIC
                +NpcSpecies.BASIC_DRAGON
            }

            configs {
                attackSpeed = 3
                respawnDelay = 50
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 240
                attack = 240
                strength = 240
                defence = 240
                magic = 240
            }

            bonuses {
                defenceStab = 70
                defenceSlash = 90
                defenceCrush = 90
                defenceMagic = 80
                defenceRanged = 70
            }

            anims {
                block = 89
                death = 92
            }

            //slayerData {
            // /**
            //  * @TODO Bug : Currently mobs don't aggro player if he does not have slayer level
            //  */
            //    levelRequirement = 50
            //    xp = 258.0
            //}

            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.black_dragonhide", 1)
                }
                
                main(weight = 128) {
                    add("item.rune_longsword", min = 1, weight = 10)
                    add("item.adamant_platebody", min = 1, weight = 9)
                    add("item.adamant_kiteshield", min = 1, weight = 3)
                    add("item.dragon_med_helm", min = 1, weight = 1)
                    add("item.fire_rune", min = 300, weight = 5)
                    add("item.air_rune", min = 300, weight = 10)
                    add("item.iron_arrow", min = 690, weight = 10)
                    add("item.runite_bolts", min = 10, weight = 10)
                    add("item.law_rune", min = 30, weight = 5)
                    add("item.blood_rune", min = 30, weight = 5)
                    add("item.yew_logs", min = 150, weight = 10)
                    add("item.adamantite_bar", min = 3, weight = 5)
                    add("item.runite_bar", min = 1, weight = 3)
                    add("item.gold_ore", min = 100, weight = 2)
                    add("item.amulet_of_power", min = 1, weight = 7)
                    add("item.dragon_arrowtips", min = 5, weight = 5)
                    add("item.dragon_dart_tip", min = 5, weight = 5)
                    add("item.dragon_javelin_heads", min = 15, weight = 5)
                    add("item.runite_limbs", min = 1, weight = 4)
                    add("item.shark", min = 4, weight = 4)
                }
            }
        }
    }
}
