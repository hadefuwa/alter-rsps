package org.alter.plugins.content.npcs.dragons

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

class DragonConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        configureDragons()
    }

    private fun configureDragons() {
        // Blue Dragons (Combat Level 111)
        setCombatDef(
            "npc.blue_dragon", "npc.blue_dragon_266", "npc.blue_dragon_267", 
            "npc.blue_dragon_268", "npc.blue_dragon_269", "npc.blue_dragon_8074",
            "npc.blue_dragon_8077", "npc.blue_dragon_8083"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 105
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 14
                searchDelay = 1
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.blue_dragonhide", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 75, max = 200, weight = 25)
                    add("item.nature_rune", min = 20, max = 40, weight = 20)
                    add("item.law_rune", min = 5, max = 10, weight = 15)
                    add("item.air_rune", min = 40, max = 80, weight = 18)
                    add("item.fire_rune", min = 30, max = 60, weight = 16)
                    add("item.adamant_arrow", min = 15, max = 30, weight = 12)
                    add("item.rune_sword", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                }
            }
        }

        // Red Dragons (Combat Level 152) 
        setCombatDef(
            "npc.red_dragon", "npc.red_dragon_248", "npc.red_dragon_249",
            "npc.red_dragon_250", "npc.red_dragon_251", "npc.red_dragon_8075",
            "npc.red_dragon_8078", "npc.red_dragon_8079"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 140
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.red_dragonhide", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 100, max = 300, weight = 25)
                    add("item.nature_rune", min = 25, max = 50, weight = 20)
                    add("item.law_rune", min = 8, max = 15, weight = 15)
                    add("item.air_rune", min = 50, max = 100, weight = 18)
                    add("item.fire_rune", min = 40, max = 80, weight = 16)
                    add("item.rune_arrow", min = 10, max = 20, weight = 12)
                    add("item.rune_sword", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 6)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                }
            }
        }

        // Black Dragons (Combat Level 227)
        setCombatDef(
            "npc.black_dragon", "npc.black_dragon_253", "npc.black_dragon_254",
            "npc.black_dragon_255", "npc.black_dragon_256", "npc.black_dragon_257",
            "npc.black_dragon_258", "npc.black_dragon_259", "npc.black_dragon_8084",
            "npc.black_dragon_8085"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 190
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.black_dragonhide", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 150, max = 400, weight = 25)
                    add("item.nature_rune", min = 30, max = 60, weight = 20)
                    add("item.law_rune", min = 10, max = 20, weight = 15)
                    add("item.air_rune", min = 60, max = 120, weight = 18)
                    add("item.fire_rune", min = 50, max = 100, weight = 16)
                    add("item.rune_arrow", min = 15, max = 30, weight = 12)
                    add("item.dragon_sword", min = 1, weight = 6)
                    add("item.dragon_dagger", min = 1, weight = 4)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                    add("item.visage", min = 1, weight = 5000) // Very rare
                }
            }
        }

        // Bronze Dragons (Combat Level 131)
        setCombatDef("npc.bronze_dragon", "npc.bronze_dragon_271") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 105
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 100, max = 250, weight = 30)
                    add("item.bronze_bar", min = 1, max = 3, weight = 20)
                    add("item.nature_rune", min = 15, max = 30, weight = 15)
                    add("item.law_rune", min = 3, max = 8, weight = 12)
                    add("item.mithril_sword", min = 1, weight = 10)
                    add("item.adamant_dagger", min = 1, weight = 8)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                }
            }
        }

        // Iron Dragons (Combat Level 189)
        setCombatDef("npc.iron_dragon", "npc.iron_dragon_273", "npc.iron_dragon_8080") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 160
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 150, max = 350, weight = 30)
                    add("item.iron_bar", min = 1, max = 5, weight = 25)
                    add("item.nature_rune", min = 20, max = 40, weight = 15)
                    add("item.law_rune", min = 5, max = 12, weight = 12)
                    add("item.adamant_sword", min = 1, weight = 10)
                    add("item.rune_dagger", min = 1, weight = 8)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                    add("item.visage", min = 1, weight = 10000) // Rare
                }
            }
        }

        // Steel Dragons (Combat Level 246)
        setCombatDef("npc.steel_dragon_274", "npc.steel_dragon_275", "npc.steel_dragon_8086") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            stats {
                hitpoints = 210
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BASIC_DRAGON
            }
            aggro {
                radius = 10
                searchDelay = 2
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 200, max = 500, weight = 30)
                    add("item.steel_bar", min = 2, max = 8, weight = 25)
                    add("item.nature_rune", min = 30, max = 60, weight = 15)
                    add("item.law_rune", min = 8, max = 15, weight = 12)
                    add("item.rune_sword", min = 1, weight = 10)
                    add("item.dragon_dagger", min = 1, weight = 6)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 128)
                    add("item.shield_left_half", min = 1, weight = 256)
                    add("item.visage", min = 1, weight = 5000) // Rare
                }
            }
        }

        // Brutal Dragons (higher combat levels)
        setCombatDef("npc.brutal_green_dragon_8081") {
            configs {
                attackSpeed = 3 // Faster attacks
                respawnDelay = 60
            }
            stats {
                hitpoints = 240
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BRUTAL_DRAGON
            }
            aggro {
                radius = 12
                searchDelay = 1
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.green_dragonhide", 2) // More hides
                }
                main(weight = 100) {
                    add("item.coins_995", min = 300, max = 800, weight = 25)
                    add("item.nature_rune", min = 40, max = 80, weight = 20)
                    add("item.law_rune", min = 15, max = 25, weight = 15)
                    add("item.dragon_sword", min = 1, weight = 8)
                    add("item.dragon_dagger", min = 1, weight = 6)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 64) // Better drop rate
                }
            }
        }

        setCombatDef("npc.brutal_red_dragon_8087") {
            configs {
                attackSpeed = 3
                respawnDelay = 60
            }
            stats {
                hitpoints = 315
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BRUTAL_DRAGON
            }
            aggro {
                radius = 12
                searchDelay = 1
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.red_dragonhide", 2)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 400, max = 1000, weight = 25)
                    add("item.nature_rune", min = 50, max = 100, weight = 20)
                    add("item.law_rune", min = 20, max = 35, weight = 15)
                    add("item.dragon_sword", min = 1, weight = 10)
                    add("item.dragon_dagger", min = 1, weight = 8)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 64)
                }
            }
        }

        setCombatDef("npc.brutal_black_dragon_8092", "npc.brutal_black_dragon_8093") {
            configs {
                attackSpeed = 3
                respawnDelay = 60
            }
            stats {
                hitpoints = 380
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            species {
                +NpcSpecies.BRUTAL_DRAGON
            }
            aggro {
                radius = 12
                searchDelay = 1
                alwaysAggro()
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                    add("item.black_dragonhide", 2)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 500, max = 1200, weight = 25)
                    add("item.nature_rune", min = 60, max = 120, weight = 20)
                    add("item.law_rune", min = 25, max = 40, weight = 15)
                    add("item.dragon_sword", min = 1, weight = 12)
                    add("item.dragon_dagger", min = 1, weight = 10)
                }
                tertiary(weight = 256) {
                    add("item.dragon_med_helm", min = 1, weight = 32) // Best drop rate
                    add("item.visage", min = 1, weight = 3000) // Good visage chance
                }
            }
        }

        // Baby Dragons (no fire breath, melee only)
        setCombatDef(
            "npc.baby_blue_dragon", "npc.baby_blue_dragon_242", "npc.baby_blue_dragon_243",
            "npc.baby_red_dragon_244", "npc.baby_red_dragon_245", "npc.baby_red_dragon_246"
        ) {
            configs {
                attackSpeed = 5
                respawnDelay = 25
            }
            stats {
                hitpoints = 48
            }
            anims {
                attack = 91
                block = 89
                death = 92
            }
            aggro {
                radius = 5
                searchDelay = 3
            }
            drops {
                always {
                    add("item.dragon_bones", 1)
                }
                main(weight = 100) {
                    add("item.coins_995", min = 10, max = 50, weight = 40)
                    add("item.air_rune", min = 5, max = 15, weight = 20)
                    add("item.fire_rune", min = 5, max = 10, weight = 20)
                    add("item.bronze_sword", min = 1, weight = 15)
                    add("item.iron_dagger", min = 1, weight = 5)
                }
            }
        }
    }
}