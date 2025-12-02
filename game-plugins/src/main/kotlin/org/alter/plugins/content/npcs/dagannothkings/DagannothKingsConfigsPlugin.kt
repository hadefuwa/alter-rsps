package org.alter.plugins.content.npcs.dagannothkings

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
import org.alter.game.model.combat.CombatClass
import org.alter.plugins.content.combat.*

/**
 * Dagannoth Kings Configuration Plugin
 * 
 * The Dagannoth Kings are three powerful bosses that each use a different combat style:
 * - Dagannoth Supreme (2265): Ranged attacks
 * - Dagannoth Prime (2266): Magic attacks
 * - Dagannoth Rex (2267): Melee attacks
 * 
 * Location: 2900, 4449 (Waterbirth Island)
 * Combat Levels: 303 (Supreme), 303 (Prime), 303 (Rex)
 * Hitpoints: 255 each
 */
class DagannothKingsConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Spawn the three Dagannoth Kings near the teleport location
        // Spread them out slightly to match the original layout
        spawnNpc("npc.dagannoth_supreme", x = 2900, z = 4449, height = 0, walkRadius = 3)
        spawnNpc("npc.dagannoth_prime", x = 2908, z = 4456, height = 0, walkRadius = 3)
        spawnNpc("npc.dagannoth_rex", x = 2915, z = 4444, height = 0, walkRadius = 3)

        // Dagannoth Supreme - Ranged attacks
        setCombatDef("npc.dagannoth_supreme") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }

            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }

            stats {
                hitpoints = 255
                attack = 255
                strength = 255
                defence = 255
                magic = 255
                ranged = 255
            }

            bonuses {
                attackStab = 200
                attackSlash = 200
                attackCrush = 200
                attackMagic = 200
                attackRanged = 300  // High ranged attack bonus

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 200
                defenceRanged = 200

                attackBonus = 200
                strengthBonus = 200
                rangedStrengthBonus = 300  // High ranged strength
                magicDamageBonus = 200
            }

            anims {
                attack = 426  // Ranged attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.dagannoth_bones_noted", min = 1, max = 3)
                }
                
                main(weight = 128) {
                    // High-value drops
                    add("item.coins_995", min = 10000, max = 50000, weight = 30)
                    
                    // Ranged equipment
                    add("item.dragon_arrow", min = 100, max = 500, weight = 15)
                    add("item.rune_arrow", min = 200, max = 1000, weight = 20)
                    add("item.adamant_arrow", min = 300, max = 1500, weight = 25)
                    
                    // Runes
                    add("item.death_rune", min = 50, max = 200, weight = 15)
                    add("item.blood_rune", min = 50, max = 200, weight = 15)
                    add("item.chaos_rune", min = 100, max = 500, weight = 20)
                    
                    // Food and potions
                    add("item.shark", min = 5, max = 15, weight = 15)
                    add("item.manta_ray", min = 3, max = 10, weight = 10)
                    add("item.super_restore4", min = 2, max = 5, weight = 12)
                    
                    // Rare drops
                    add("item.dagannoth_hide", min = 1, max = 3, weight = 10)
                    add("item.helm_of_neitiznot", min = 1, weight = 5)
                    add("item.dragon_axe", min = 1, weight = 3)
                    add("item.berserker_ring", min = 1, weight = 2)
                    add("item.warrior_ring", min = 1, weight = 2)
                    add("item.archers_ring", min = 1, weight = 2)
                }
            }
        }
        
        // Dagannoth Prime - Magic attacks
        setCombatDef("npc.dagannoth_prime") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }

            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }

            stats {
                hitpoints = 255
                attack = 255
                strength = 255
                defence = 255
                magic = 255
                ranged = 255
            }

            bonuses {
                attackStab = 200
                attackSlash = 200
                attackCrush = 200
                attackMagic = 300  // High magic attack bonus
                attackRanged = 200

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 200
                defenceRanged = 200

                attackBonus = 200
                strengthBonus = 200
                rangedStrengthBonus = 200
                magicDamageBonus = 300  // High magic damage
            }

            anims {
                attack = 422  // Magic attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.dagannoth_bones_noted", min = 1, max = 3)
                }
                
                main(weight = 128) {
                    // High-value drops
                    add("item.coins_995", min = 10000, max = 50000, weight = 30)
                    
                    // Runes
                    add("item.death_rune", min = 100, max = 500, weight = 25)
                    add("item.blood_rune", min = 100, max = 500, weight = 25)
                    add("item.chaos_rune", min = 200, max = 1000, weight = 30)
                    add("item.soul_rune", min = 50, max = 250, weight = 20)
                    
                    // Magic equipment
                    add("item.mystic_robe_top", min = 1, weight = 5)
                    add("item.mystic_robe_bottom", min = 1, weight = 5)
                    add("item.mystic_gloves", min = 1, weight = 5)
                    add("item.mystic_boots", min = 1, weight = 5)
                    
                    // Food and potions
                    add("item.shark", min = 5, max = 15, weight = 15)
                    add("item.manta_ray", min = 3, max = 10, weight = 10)
                    add("item.super_restore4", min = 2, max = 5, weight = 12)
                    
                    // Rare drops
                    add("item.dagannoth_hide", min = 1, max = 3, weight = 10)
                    add("item.helm_of_neitiznot", min = 1, weight = 5)
                    add("item.dragon_axe", min = 1, weight = 3)
                    add("item.seers_ring", min = 1, weight = 2)
                }
            }
        }
        
        // Dagannoth Rex - Melee attacks
        setCombatDef("npc.dagannoth_rex") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }

            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }

            stats {
                hitpoints = 255
                attack = 255
                strength = 255
                defence = 255
                magic = 255
                ranged = 255
            }

            bonuses {
                attackStab = 300  // High melee attack bonuses
                attackSlash = 300
                attackCrush = 300
                attackMagic = 200
                attackRanged = 200

                defenceStab = 200
                defenceSlash = 200
                defenceCrush = 200
                defenceMagic = 200
                defenceRanged = 200

                attackBonus = 300
                strengthBonus = 300  // High strength bonus
                rangedStrengthBonus = 200
                magicDamageBonus = 200
            }

            anims {
                attack = 2855  // Melee attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.dagannoth_bones_noted", min = 1, max = 3)
                }
                
                main(weight = 128) {
                    // High-value drops
                    add("item.coins_995", min = 10000, max = 50000, weight = 30)
                    
                    // Melee equipment
                    add("item.dragon_scimitar", min = 1, weight = 3)
                    add("item.dragon_longsword", min = 1, weight = 3)
                    add("item.dragon_battleaxe", min = 1, weight = 2)
                    add("item.dragon_2h_sword", min = 1, weight = 2)
                    add("item.dragon_chainbody", min = 1, weight = 2)
                    add("item.dragon_platelegs", min = 1, weight = 2)
                    add("item.dragon_plateskirt", min = 1, weight = 2)
                    
                    // Runes
                    add("item.death_rune", min = 50, max = 200, weight = 15)
                    add("item.blood_rune", min = 50, max = 200, weight = 15)
                    add("item.chaos_rune", min = 100, max = 500, weight = 20)
                    
                    // Food and potions
                    add("item.shark", min = 5, max = 15, weight = 15)
                    add("item.manta_ray", min = 3, max = 10, weight = 10)
                    add("item.super_restore4", min = 2, max = 5, weight = 12)
                    
                    // Rare drops
                    add("item.dagannoth_hide", min = 1, max = 3, weight = 10)
                    add("item.helm_of_neitiznot", min = 1, weight = 5)
                    add("item.dragon_axe", min = 1, weight = 3)
                    add("item.berserker_ring", min = 1, weight = 2)
                    add("item.warrior_ring", min = 1, weight = 2)
                }
            }
        }
        
        // Set combat classes for each king
        onNpcSpawn(npc = "npc.dagannoth_supreme") {
            npc.combatClass = CombatClass.RANGED
        }
        
        onNpcSpawn(npc = "npc.dagannoth_prime") {
            npc.combatClass = CombatClass.MAGIC
        }
        
        onNpcSpawn(npc = "npc.dagannoth_rex") {
            npc.combatClass = CombatClass.MELEE
        }
    }
}


