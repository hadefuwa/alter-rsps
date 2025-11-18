package org.alter.plugins.content.npcs.greaterdemon

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

class GreaterDemonConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Greater demons use a shared combat definition and drop table
        val npcIds = listOf(
            "npc.greater_demon",
            "npc.greater_demon_2026",
            "npc.greater_demon_2027",
            "npc.greater_demon_2028",
            "npc.greater_demon_2029",
            "npc.greater_demon_2030",
            "npc.greater_demon_2031",
            "npc.greater_demon_2032",
        )

        npcIds.forEach { npcId ->
            setCombatDef(npcId) {
                configs {
                    attackSpeed = 4
                    respawnDelay = 50 // ~30s respawn
                }

                aggro {
                    radius = 8
                    searchDelay = 1
                }

                stats {
                    hitpoints = 105
                    attack = 90
                    strength = 95
                    defence = 85
                    magic = 1
                    ranged = 1
                }

                bonuses {
                    defenceStab = 40
                    defenceSlash = 60
                    defenceCrush = 40
                    defenceMagic = 0
                    defenceRanged = 20
                    attackStab = 0
                    attackSlash = 65
                    attackCrush = 0
                }

                anims {
                    attack = 65   // generic demon attack
                    block = 64
                    death = 67
                }

                drops {
                    always {
                        add("item.big_bones", 1)
                    }

                    main(weight = 128) {
                        // Rune / weapon drops (uncommon)
                        add("item.rune_full_helm", min = 1, weight = 4)
                        add("item.rune_chainbody", min = 1, weight = 3)
                        add("item.rune_battleaxe", min = 1, weight = 3)
                        add("item.rune_2h_sword", min = 1, weight = 2)

                        // Armour / supplies
                        add("item.black_dagger", min = 1, weight = 6)
                        add("item.black_full_helm", min = 1, weight = 5)
                        add("item.black_platelegs", min = 1, weight = 5)
                        add("item.mithril_ore", min = 20, max = 40, weight = 6)

                        // Runes
                        add("item.fire_rune", min = 75, max = 150, weight = 10)
                        add("item.chaos_rune", min = 15, max = 45, weight = 10)
                        add("item.death_rune", min = 10, max = 25, weight = 6)

                        // Food & potions
                        add("item.lobster", min = 3, max = 8, weight = 10)
                        add("item.shark", min = 1, max = 3, weight = 4)
                        add("item.prayer_potion4", min = 1, max = 2, weight = 3)

                        // Coins & gems
                        add("item.coins_995", min = 1000, max = 6000, weight = 18)
                        add("item.uncut_sapphire", min = 1, max = 2, weight = 6)
                        add("item.uncut_emerald", min = 1, max = 2, weight = 5)
                        add("item.uncut_ruby", min = 1, max = 2, weight = 3)

                        // Rare drops
                        add("item.clue_scroll_hard", min = 1, weight = 2)
                        add("item.shield_left_half", min = 1, weight = 1)
                    }
                }
            }
        }
    }
}
