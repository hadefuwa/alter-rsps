package org.alter.plugins.content.npcs.chaosfanatic

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

class ChaosFanaticConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 12847) // Wilderness region around the chaos altar

        // Spawn Chaos Fanatic near the Chaos Altar in wilderness
        spawnNpc("npc.chaos_fanatic", x = 2980, z = 3849, walkRadius = 8)

        setCombatDef("npc.chaos_fanatic") {
            configs {
                attackSpeed = 4
                respawnDelay = 100 // 1 minute respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 202
                attack = 240
                strength = 200
                defence = 240
                magic = 260
                ranged = 1
            }

            bonuses {
                defenceStab = 45
                defenceSlash = 45
                defenceCrush = 45
                defenceMagic = 80
                defenceRanged = 45
                attackMagic = 70
            }

            anims {
                attack = 3337 // Chaos magic animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    // Chaos-themed weapons and armour
                    add("item.rune_crossbow", min = 1, weight = 2)
                    add("item.magic_shortbow", min = 1, weight = 3)
                    add("item.rune_kiteshield", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_chainbody", min = 1, weight = 3)
                    add("item.rune_med_helm", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_longsword", min = 1, weight = 2)
                    add("item.rune_warhammer", min = 1, weight = 1)
                    
                    // Chaos runes and magic supplies
                    add("item.chaos_rune", min = 100, max = 200, weight = 15)
                    add("item.death_rune", min = 30, max = 60, weight = 10)
                    add("item.blood_rune", min = 20, max = 40, weight = 8)
                    add("item.nature_rune", min = 40, max = 80, weight = 10)
                    add("item.law_rune", min = 20, max = 40, weight = 8)
                    add("item.cosmic_rune", min = 25, max = 50, weight = 9)
                    
                    // Ammunition
                    add("item.rune_arrow", min = 120, max = 250, weight = 8)
                    add("item.adamant_bolts", min = 60, max = 120, weight = 10)
                    add("item.runite_bolts", min = 15, max = 30, weight = 5)
                    
                    // Other supplies
                    add("item.shark", min = 4, max = 10, weight = 15)
                    add("item.prayer_potion4", min = 1, max = 3, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 2, weight = 5)
                    add("item.saradomin_brew4", min = 1, max = 3, weight = 6)
                    add("item.super_restore4", min = 1, max = 2, weight = 5)
                    
                    // Valuable items
                    add("item.coins_995", min = 8000, max = 20000, weight = 20)
                    add("item.uncut_diamond", min = 1, max = 3, weight = 3)
                    add("item.uncut_ruby", min = 2, max = 5, weight = 5)
                    add("item.gold_ore", min = 60, max = 120, weight = 8)
                    
                    // Special chaos drops
                    add("item.chaos_talisman", min = 1, weight = 2)
                    
                    // Rare drops
                    add("item.rune_pickaxe", min = 1, weight = 1)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_pickaxe", min = 1, weight = 1)
                    add("item.clue_scroll_hard", min = 1, weight = 2)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                }
            }
        }
    }
}