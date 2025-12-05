package org.alter.plugins.content.bosses.bloodmoon


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

class BloodMoonConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatDef("npc.blood_moon") {
            configs {
                attackSpeed = 5
                respawnDelay = 60
            }

            aggro {
                radius = 12
            }

            stats {
                hitpoints = 1600
                attack = 260
                strength = 310
                defence = 210
                magic = 290
                ranged = 210
            }

            bonuses {
                attackStab = 160
                attackSlash = 160
                attackCrush = 160
                attackMagic = 210
                attackRanged = 160

                defenceStab = 160
                defenceSlash = 160
                defenceCrush = 160
                defenceMagic = 160
                defenceRanged = 160

                attackBonus = 160
                strengthBonus = 210
                rangedStrengthBonus = 160
                magicDamageBonus = 260
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
                    add("item.coins_995", min = 600000, max = 1200000, weight = 20)
                    add("item.death_rune", min = 600, max = 1200, weight = 15)
                    add("item.blood_.rune", min = 600, max = 1200, weight = 15)
                }

                tertiary(weight = 1000) {
                    add("item.blood_moon_helm", min = 1, weight = 10)
                    add("item.blood_moon_chestplate", min = 1, weight = 10)
                    add("item.blood_moon_tassets", min = 1, weight = 10)
                    add("item.draconic_visage", min = 1, weight = 500)
                }
            }
        }
    }
}
