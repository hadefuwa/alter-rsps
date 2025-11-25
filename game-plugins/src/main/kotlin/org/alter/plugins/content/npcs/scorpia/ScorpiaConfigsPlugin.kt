package org.alter.plugins.content.npcs.scorpia

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

class ScorpiaConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 13206) // Wilderness region around Scorpia's lair

        // Spawn Scorpia at the Scorpia lair in deep wilderness
        spawnNpc("npc.scorpia", x = 3233, z = 3945, walkRadius = 8)

        setCombatDef("npc.scorpia") {
            configs {
                attackSpeed = 4
                respawnDelay = 100 // 1 minute respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 255
                attack = 225
                strength = 225
                defence = 225
                magic = 255
                ranged = 255
            }

            bonuses {
                defenceStab = 55
                defenceSlash = 55
                defenceCrush = 55
                defenceMagic = 60
                defenceRanged = 55
                attackMagic = 60
                attackRanged = 60
                attackStab = 55
            }

            anims {
                attack = 6254 // Scorpia attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    // Scorpion-themed weapons and armour
                    add("item.rune_crossbow_noted", min = 10, weight = 2)
                    add("item.magic_shortbow", min = 10, weight = 3)
                    add("item.rune_kiteshield_noted", min = 10, weight = 2)
                    add("item.rune_platelegs_noted", min = 10, weight = 2)
                    add("item.rune_chainbody_noted", min = 10, weight = 3)
                    add("item.rune_med_helm", min = 10, weight = 3)
                    add("item.rune_scimitar", min = 10, weight = 3)
                    add("item.rune_longsword", min = 10, weight = 2)
                    add("item.rune_battleaxe_noted", min = 10, weight = 2)
                    
                    // Poison and ranged supplies
                    add("item.rune_arrow", min = 150, max = 300, weight = 10)
                    add("item.adamant_bolts", min = 75, max = 150, weight = 10)
                    add("item.runite_bolts", min = 20, max = 40, weight = 6)
                    
                    // Runes (poison/nature themed)
                    add("item.nature_rune", min = 50, max = 1000, weight = 12)
                    add("item.death_rune", min = 35, max = 700, weight = 10)
                    add("item.chaos_rune", min = 75, max = 1500, weight = 12)
                    add("item.law_rune", min = 25, max = 500, weight = 8)
                    add("item.cosmic_rune", min = 30, max = 600, weight = 9)
                    
                    // Potions and food
                    add("item.shark", min = 5, max = 12, weight = 18)
                    add("item.antipoison4", min = 2, max = 4, weight = 8)
                    add("item.prayer_potion4", min = 2, max = 4, weight = 10)
                    add("item.super_combat_potion4", min = 1, max = 3, weight = 6)
                    add("item.saradomin_brew4", min = 2, max = 4, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    
                    // Valuable items
                    add("item.coins_995", min = 12000, max = 28000, weight = 22)
                    add("item.uncut_diamond", min = 2, max = 4, weight = 4)
                    add("item.uncut_ruby", min = 3, max = 6, weight = 6)
                    add("item.gold_ore", min = 80, max = 160, weight = 10)
                    
                    // Scorpia special drops
                    add("item.scorpion_tail", min = 1, weight = 3) // If this item exists
                    
                    // Rare drops
                    add("item.rune_pickaxe", min = 1, weight = 1)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_pickaxe", min = 1, weight = 1)
                    add("item.dragon_longsword", min = 1, weight = 2)
                    add("item.dragon_battleaxe", min = 1, weight = 2)
                    add("item.clue_scroll_hard", min = 1, weight = 3)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                }
            }
        }
    }
}