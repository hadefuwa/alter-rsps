package org.alter.plugins.content.npcs.chaoselemental

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

class ChaosElementalConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 12853) // Wilderness region around Chaos Elemental location

        // Chaos Elemental spawn removed from Lumbridge (was at 3200, 3266)
        // Chaos Elemental is now only available on Boss Island

        setCombatDef("npc.chaos_elemental") {
            configs {
                attackSpeed = 4
                respawnDelay = 50 // 30 second respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 250
                attack = 270
                strength = 200
                defence = 270
                magic = 270
                ranged = 270
            }

            bonuses {
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 50
                defenceRanged = 50
                attackMagic = 80
                attackRanged = 80
            }

            anims {
                attack = 3144 // Chaos Elemental attack animation
                block = 3145
                death = 3147
            }

            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.chaos_rune", min = 75, max = 125)
                }
                
                main(weight = 128) {
                    // Runes (common chaos-themed drops)
                    add("item.death_rune", min = 25, max = 50, weight = 16)
                    add("item.blood_rune", min = 15, max = 25, weight = 12)
                    add("item.nature_rune", min = 50, max = 75, weight = 16)
                    add("item.law_rune", min = 25, max = 40, weight = 12)
                    
                    // Weapons and armour (uncommon)
                    add("item.rune_battleaxe", min = 1, weight = 4)
                    add("item.rune_2h_sword", min = 1, weight = 4)
                    add("item.rune_chainbody", min = 1, weight = 4)
                    add("item.rune_kiteshield", min = 1, weight = 4)
                    add("item.adamant_platebody", min = 1, weight = 4)
                    add("item.mystic_robe_top_dark", min = 1, weight = 4)
                    add("item.mystic_robe_bottom_dark", min = 1, weight = 4)
                    
                    // Valuable supplies
                    add("item.shark", min = 3, max = 8, weight = 10)
                    add("item.prayer_potion4", min = 1, max = 3, weight = 6)
                    add("item.super_combat_potion4", min = 1, max = 2, weight = 4)
                    add("item.saradomin_brew4", min = 1, max = 3, weight = 5)
                    add("item.super_restore4", min = 1, max = 2, weight = 4)
                    
                    // Coins and gems
                    add("item.coins_995", min = 10000, max = 25000, weight = 15)
                    add("item.uncut_diamond", min = 1, max = 3, weight = 3)
                    add("item.uncut_ruby", min = 2, max = 5, weight = 5)
                    
                    // Dragon items (rare)
                    add("item.dragon_longsword", min = 1, weight = 1)
                    add("item.dragon_battleaxe", min = 1, weight = 1)
                    add("item.dragon_chainbody", min = 1, weight = 1)
                    add("item.dragon_med_helm", min = 1, weight = 1)
                    
                    // Very rare drops
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_pickaxe", min = 1, weight = 1)
                    
                    // Clue scrolls
                    add("item.clue_scroll_hard", min = 1, weight = 2)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                }
            }
        }
    }
}