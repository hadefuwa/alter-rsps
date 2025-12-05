package org.alter.plugins.content.bosses.eclipsemoon


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

class EclipseMoonConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatDef("npc.eclipse_moon") {
            configs {
                attackSpeed = 5
                respawnDelay = 60
            }

            aggro {
                radius = 12
            }

            stats {
                hitpoints = 1500
                attack = 250
                strength = 300
                defence = 200
                magic = 280
                ranged = 200
            }

            bonuses {
                attackStab = 150
                attackSlash = 150
                attackCrush = 150
                attackMagic = 200
                attackRanged = 150

                defenceStab = 150
                defenceSlash = 150
                defenceCrush = 150
                defenceMagic = 150
                defenceRanged = 150

                attackBonus = 150
                strengthBonus = 200
                rangedStrengthBonus = 150
                magicDamageBonus = 250
            }

            anims {
                block = 1683
                death = 1684
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }

                main(weight = 150) {
                    add("item.coins_995", min = 500000, max = 1000000, weight = 20)
                    add("item.death_rune", min = 500, max = 1000, weight = 15)
                    add("item.blood_rune", min = 500, max = 1000, weight = 15)
                }

                tertiary(weight = 1000) {
                    add("item.eclipse_moon_helm", min = 1, weight = 10)
                    add("item.eclipse_moon_chestplate", min = 1, weight = 10)
                    add("item.eclipse_moon_tassets", min = 1, weight = 10)
                    add("item.draconic_visage", min = 1, weight = 500)
                }
            }
        }
    }
}
