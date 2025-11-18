package org.alter.plugins.content.npcs.crazyarchaeologist

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

class CrazyArchaeologistConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 12589) // Wilderness region around the ruin location

        // Spawn Crazy Archaeologist at the Ruin location in wilderness
        spawnNpc("npc.crazy_archaeologist", x = 2984, z = 3713, walkRadius = 8)

        setCombatDef("npc.crazy_archaeologist") {
            configs {
                attackSpeed = 6 // Reduced attack frequency (6 cycles = 3.6 seconds between attacks)
                respawnDelay = 4 // 2 seconds respawn delay (4 cycles = 2.4 seconds, ensures at least 2 seconds)
            }

            aggro {
                radius = 25  // Wide range of view - can detect players from 25 tiles away
                searchDelay = 1  // Check for targets every cycle (very frequent)
                alwaysAggro()  // Always aggressive - never stops being aggressive
            }

            stats {
                hitpoints = 225
                attack = 204
                strength = 204
                defence = 204
                magic = 350  // Increased from 240 to 350 for much higher damage
                ranged = 1
            }

            bonuses {
                defenceStab = 40
                defenceSlash = 40
                defenceCrush = 40
                defenceMagic = 100
                defenceRanged = 40
                attackMagic = 150  // Increased from 50 to 150 for significantly higher magic damage
            }

            anims {
                attack = 3353 // Book throwing animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    // Weapons and armour
                    add("item.rune_crossbow", min = 1, weight = 2)
                    add("item.magic_shortbow", min = 1, weight = 3)
                    add("item.rune_kiteshield", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_chainbody", min = 1, weight = 3)
                    add("item.rune_med_helm", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_longsword", min = 1, weight = 2)
                    add("item.rune_warhammer", min = 1, weight = 1)
                    
                    // Runes and magic supplies
                    add("item.death_rune", min = 25, max = 50, weight = 8)
                    add("item.blood_rune", min = 15, max = 30, weight = 6)
                    add("item.chaos_rune", min = 50, max = 100, weight = 10)
                    add("item.nature_rune", min = 30, max = 60, weight = 8)
                    add("item.law_rune", min = 15, max = 30, weight = 6)
                    add("item.cosmic_rune", min = 20, max = 40, weight = 7)
                    
                    // Ammunition
                    add("item.rune_arrow", min = 100, max = 200, weight = 8)
                    add("item.adamant_bolts", min = 50, max = 100, weight = 10)
                    add("item.runite_bolts", min = 10, max = 25, weight = 5)
                    
                    // Other supplies
                    add("item.shark", min = 3, max = 8, weight = 15)
                    add("item.prayer_potion4", min = 1, max = 3, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 2, weight = 5)
                    add("item.saradomin_brew4", min = 1, max = 3, weight = 6)
                    add("item.super_restore4", min = 1, max = 2, weight = 5)
                    
                    // Valuable items
                    add("item.coins_995", min = 5000, max = 15000, weight = 20)
                    add("item.uncut_diamond", min = 1, max = 3, weight = 3)
                    add("item.uncut_ruby", min = 2, max = 5, weight = 5)
                    add("item.gold_ore", min = 50, max = 100, weight = 8)
                    
                    // Special drops
                    add("item.archaeologists_diary", min = 1, weight = 1) // Unique drop

                    // Rare drops - lower weights
                    add("item.rune_pickaxe", min = 1, weight = 2)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_pickaxe", min = 1, weight = 1) // Very rare
                    add("item.clue_scroll_hard", min = 1, weight = 4)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                }
            }
        }
    }
}