package org.alter.plugins.content.npcs.kalphitequeen

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

/**
 * @author Auto-generated
 * Kalphite Queen - The Desert Boss
 * Combat Level: 333
 * Hitpoints: 510 (255 per form)
 * Location: Kalphite Lair (3478, 9498)
 * Region: 13972 (Multi-combat)
 * 
 * The Kalphite Queen has two forms:
 * - First Form (Crawling): Uses melee and magic attacks, weak to ranged
 * - Second Form (Flying): Uses ranged and magic attacks, weak to melee
 * Transforms at 50% HP
 */
class KalphiteQueenConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Calculate region ID from spawn coordinates
        val spawnTile = Tile(x = 3478, z = 9498, height = 0)
        val regionId = spawnTile.regionId
        
        // Set the spawning area as multi-combat
        setMultiCombatRegion(region = regionId)

        // Spawn Kalphite Queen at the Kalphite Lair
        // Using kalphite_queen_963 (ID 963) which is the first form (crawling)
        spawnNpc("npc.kalphite_queen_963", x = 3478, z = 9498, walkRadius = 5)

        setCombatDef("npc.kalphite_queen_963") {
            species {
                +NpcSpecies.KALPHITE
            }

            configs {
                attackSpeed = 4
                respawnDelay = 50 // 30 seconds respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 255  // Form 1 has 255 HP
                attack = 300
                strength = 300
                defence = 300
                magic = 300
                ranged = 300
            }

            bonuses {
                defenceStab = 120
                defenceSlash = 120
                defenceCrush = 120
                defenceMagic = 100
                defenceRanged = 120
                attackMagic = 120
                attackRanged = 120
                attackStab = 120
                attackSlash = 120
                attackCrush = 120
            }

            anims {
                attack = 6240 // Kalphite Queen attack animation (first form)
                block = 6242
                death = 6241
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    // Kalphite Queen signature drops
                    add("item.kalphite_queen_head", min = 1, weight = 1) // 1/128 - Very rare
                    add("item.jar_of_sand", min = 1, weight = 1) // 1/2000 - Ultra rare
                    add("item.kalphite_princess", min = 1, weight = 1) // 1/3000 - Ultra rare
                    
                    // Dragon items (rare)
                    add("item.dragon_chainbody", min = 1, weight = 1) // 1/128
                    add("item.dragon_2h_sword", min = 1, weight = 1) // 1/256
                    add("item.dragon_pickaxe", min = 1, weight = 1) // 1/400
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_longsword", min = 1, weight = 2)
                    add("item.dragon_battleaxe", min = 1, weight = 2)
                    
                    // Rune equipment (uncommon)
                    add("item.rune_platebody", min = 1, weight = 4)
                    add("item.rune_platelegs", min = 1, weight = 4)
                    add("item.rune_kiteshield", min = 1, weight = 4)
                    add("item.rune_full_helm", min = 1, weight = 4)
                    add("item.rune_scimitar", min = 1, weight = 5)
                    add("item.rune_longsword", min = 1, weight = 5)
                    add("item.rune_battleaxe", min = 1, weight = 4)
                    add("item.rune_2h_sword", min = 1, weight = 3)
                    
                    // Potions and food
                    add("item.shark", min = 5, max = 10, weight = 15)
                    add("item.prayer_potion4", min = 2, max = 4, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 3, weight = 6)
                    add("item.saradomin_brew4", min = 2, max = 4, weight = 7)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    
                    // Ammunition and supplies
                    add("item.rune_arrow", min = 150, max = 300, weight = 10)
                    add("item.runite_bolts", min = 25, max = 50, weight = 8)
                    add("item.adamant_arrow", min = 200, max = 400, weight = 12)
                    
                    // Runes
                    add("item.death_rune", min = 50, max = 100, weight = 12)
                    add("item.blood_rune", min = 25, max = 50, weight = 10)
                    add("item.chaos_rune", min = 75, max = 150, weight = 15)
                    add("item.nature_rune", min = 40, max = 80, weight = 12)
                    add("item.law_rune", min = 30, max = 60, weight = 10)
                    
                    // Valuable items and coins
                    add("item.coins_995", min = 15000, max = 35000, weight = 20)
                    add("item.uncut_diamond", min = 2, max = 5, weight = 4)
                    add("item.uncut_dragonstone", min = 1, max = 2, weight = 2)
                    add("item.gold_ore", min = 75, max = 150, weight = 10)
                    
                    // Clue scrolls
                    add("item.clue_scroll_hard", min = 1, weight = 3)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                    
                    // Seeds and herbs (desert themed)
                    add("item.ranarr_seed", min = 1, max = 3, weight = 5)
                    add("item.snapdragon_seed", min = 1, max = 2, weight = 3)
                    add("item.torstol_seed", min = 1, weight = 1)
                }
            }
        }
        
        // Configure Form 2 (Orange Flying Form) if it exists
        try {
            setCombatDef("npc.kalphite_queen_964") {
                species {
                    +NpcSpecies.KALPHITE
                }

                configs {
                    attackSpeed = 4
                    respawnDelay = 50 // Same respawn delay
                }

                aggro {
                    radius = 16
                    searchDelay = 1
                }

                stats {
                    hitpoints = 255  // Form 2 has 255 HP
                    attack = 300
                    strength = 300
                    defence = 300
                    magic = 300
                    ranged = 300
                }

                bonuses {
                    defenceStab = 120
                    defenceSlash = 120
                    defenceCrush = 120
                    defenceMagic = 100
                    defenceRanged = 120
                    attackMagic = 120
                    attackRanged = 120
                    attackStab = 120
                    attackSlash = 120
                    attackCrush = 120
                }

                anims {
                    attack = 6245 // Kalphite Queen attack animation (flying form)
                    block = 6242
                    death = 6241
                }

                drops {
                    // Form 2 uses the same drop table as Form 1
                    always {
                        add("item.big_bones", 1)
                    }
                    
                    main(weight = 128) {
                        // Kalphite Queen signature drops
                        add("item.kalphite_queen_head", min = 1, weight = 1) // 1/128 - Very rare
                        add("item.jar_of_sand", min = 1, weight = 1) // 1/2000 - Ultra rare
                        add("item.kalphite_princess", min = 1, weight = 1) // 1/3000 - Ultra rare
                        
                        // Dragon items (rare)
                        add("item.dragon_chainbody", min = 1, weight = 1) // 1/128
                        add("item.dragon_2h_sword", min = 1, weight = 1) // 1/256
                        add("item.dragon_pickaxe", min = 1, weight = 1) // 1/400
                        add("item.dragon_med_helm", min = 1, weight = 2)
                        add("item.dragon_longsword", min = 1, weight = 2)
                        add("item.dragon_battleaxe", min = 1, weight = 2)
                        
                        // Rune equipment (uncommon)
                        add("item.rune_platebody", min = 1, weight = 4)
                        add("item.rune_platelegs", min = 1, weight = 4)
                        add("item.rune_kiteshield", min = 1, weight = 4)
                        add("item.rune_full_helm", min = 1, weight = 4)
                        add("item.rune_scimitar", min = 1, weight = 5)
                        add("item.rune_longsword", min = 1, weight = 5)
                        add("item.rune_battleaxe", min = 1, weight = 4)
                        add("item.rune_2h_sword", min = 1, weight = 3)
                        
                        // Potions and food
                        add("item.shark", min = 5, max = 10, weight = 15)
                        add("item.prayer_potion4", min = 2, max = 4, weight = 8)
                        add("item.super_combat_potion4", min = 1, max = 3, weight = 6)
                        add("item.saradomin_brew4", min = 2, max = 4, weight = 7)
                        add("item.super_restore4", min = 1, max = 3, weight = 6)
                        
                        // Ammunition and supplies
                        add("item.rune_arrow", min = 150, max = 300, weight = 10)
                        add("item.runite_bolts", min = 25, max = 50, weight = 8)
                        add("item.adamant_arrow", min = 200, max = 400, weight = 12)
                        
                        // Runes
                        add("item.death_rune", min = 50, max = 100, weight = 12)
                        add("item.blood_rune", min = 25, max = 50, weight = 10)
                        add("item.chaos_rune", min = 75, max = 150, weight = 15)
                        add("item.nature_rune", min = 40, max = 80, weight = 12)
                        add("item.law_rune", min = 30, max = 60, weight = 10)
                        
                        // Valuable items and coins
                        add("item.coins_995", min = 15000, max = 35000, weight = 20)
                        add("item.uncut_diamond", min = 2, max = 5, weight = 4)
                        add("item.uncut_dragonstone", min = 1, max = 2, weight = 2)
                        add("item.gold_ore", min = 75, max = 150, weight = 10)
                        
                        // Clue scrolls
                        add("item.clue_scroll_hard", min = 1, weight = 3)
                        add("item.clue_scroll_elite", min = 1, weight = 1)
                        
                        // Seeds and herbs (desert themed)
                        add("item.ranarr_seed", min = 1, max = 3, weight = 5)
                        add("item.snapdragon_seed", min = 1, max = 2, weight = 3)
                        add("item.torstol_seed", min = 1, weight = 1)
                    }
                }
            }
        } catch (e: Exception) {
            // Form 2 NPC doesn't exist, that's okay - we'll handle it via attribute
        }
    }
}

