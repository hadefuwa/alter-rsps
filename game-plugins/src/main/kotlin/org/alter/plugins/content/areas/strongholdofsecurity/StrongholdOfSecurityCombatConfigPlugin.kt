package org.alter.plugins.content.areas.strongholdofsecurity

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
 * Stronghold of Security Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all NPCs in the Stronghold of Security
 * that don't already have combat stats configured elsewhere.
 * 
 * NPCs configured:
 * - Rat (Level 1)
 * - Minotaur (Levels 14, 52)
 * - Zombie (Levels 11-12)
 * - Flesh Crawler (Levels 39-40)
 * - Giant Rat (Level 9)
 * - Scorpion (Levels 25-26)
 * - Catablepon (Levels 53-54)
 * - Shade (Level 61)
 * - Ankou (Levels 60-63)
 * 
 * Note: The following NPCs are already configured in other plugins:
 * - Goblin (LumbridgeCombatConfigPlugin)
 * - Wolf (WildernessCombatConfigPlugin)
 * - Ghost (GenericNpcCombatConfigPlugin)
 * - Skeleton (TaverleyCombatConfigPlugin)
 * - Spider (TaverleyCombatConfigPlugin)
 * - Giant Spider (LumbridgeCombatConfigPlugin)
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class StrongholdOfSecurityCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Rat Combat Configuration
         * Level 1 creature, perfect for absolute beginners.
         */
        setCombatDef("npc.rat") {
            configs {
                attackSpeed = 4
                respawnDelay = 15
            }
            stats {
                hitpoints = 5
            }
            bonuses {
                attackStab = 2
                strengthBonus = 2
                defenceStab = 2
                defenceSlash = 2
                defenceCrush = 2
                defenceMagic = 1
                defenceRanged = 2
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 5
                searchDelay = 3
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Minotaur Combat Configuration
         * Level 14 and 52 creatures, mid-level training.
         */
        setCombatDef("npc.minotaur") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 20
                attack = 18
                strength = 20
                defence = 15
            }
            bonuses {
                attackStab = 12
                attackSlash = 12
                attackCrush = 12
                strengthBonus = 15
                defenceStab = 10
                defenceSlash = 10
                defenceCrush = 10
                defenceMagic = 5
                defenceRanged = 10
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Zombie Combat Configuration
         * Level 11-12 creatures, undead warriors.
         */
        setCombatDef("npc.zombie") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 18
                attack = 12
                strength = 14
                defence = 10
            }
            bonuses {
                attackStab = 10
                attackSlash = 10
                attackCrush = 10
                strengthBonus = 12
                defenceStab = 8
                defenceSlash = 8
                defenceCrush = 8
                defenceMagic = 5
                defenceRanged = 8
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }

                main(weight = 128) {
                    // Low-tier weapons
                    add("item.iron_sword", min = 1, weight = 15)
                    add("item.iron_scimitar", min = 1, weight = 12)
                    add("item.iron_longsword", min = 1, weight = 10)
                    add("item.steel_sword", min = 1, weight = 8)
                    add("item.steel_scimitar", min = 1, weight = 6)
                    add("item.steel_longsword", min = 1, weight = 5)

                    // Low-tier armor
                    add("item.iron_full_helm", min = 1, weight = 10)
                    add("item.iron_platebody", min = 1, weight = 8)
                    add("item.iron_platelegs", min = 1, weight = 8)
                    add("item.iron_kiteshield", min = 1, weight = 10)
                    add("item.steel_full_helm", min = 1, weight = 6)
                    add("item.steel_platebody", min = 1, weight = 5)
                    add("item.steel_platelegs", min = 1, weight = 5)

                    // Coins & supplies
                    add("item.coins_995", min = 10, max = 200, weight = 25)
                    add("item.bread", min = 1, max = 2, weight = 12)
                    add("item.meat", min = 1, max = 2, weight = 10)

                    // Runes
                    add("item.mind_rune", min = 3, max = 10, weight = 15)
                    add("item.body_rune", min = 2, max = 8, weight = 12)
                    add("item.chaos_rune", min = 1, max = 3, weight = 5)

                    // Herbs (uncommon)
                    add("item.grimy_guam", min = 1, weight = 8)
                    add("item.grimy_marrentill", min = 1, weight = 6)
                    add("item.grimy_tarromin", min = 1, weight = 4)
                }
            }
        }
        
        /**
         * Flesh Crawler Combat Configuration
         * Level 39-40 creatures, fast and aggressive.
         */
        setCombatDef("npc.flesh_crawler") {
            configs {
                attackSpeed = 3
                respawnDelay = 25
            }
            stats {
                hitpoints = 50
                attack = 40
                strength = 42
                defence = 35
            }
            bonuses {
                attackStab = 25
                attackSlash = 25
                attackCrush = 25
                strengthBonus = 30
                defenceStab = 20
                defenceSlash = 20
                defenceCrush = 20
                defenceMagic = 10
                defenceRanged = 20
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 8
                searchDelay = 2
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Giant Rat Combat Configuration
         * Level 9 creature, larger and stronger than regular rats.
         */
        setCombatDef("npc.giant_rat") {
            configs {
                attackSpeed = 4
                respawnDelay = 20
            }
            stats {
                hitpoints = 12
                attack = 8
                strength = 9
                defence = 7
            }
            bonuses {
                attackStab = 5
                attackSlash = 5
                attackCrush = 5
                strengthBonus = 6
                defenceStab = 5
                defenceSlash = 5
                defenceCrush = 5
                defenceMagic = 2
                defenceRanged = 5
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 6
                searchDelay = 3
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Scorpion Combat Configuration
         * Level 25-26 creatures, can poison players.
         */
        setCombatDef("npc.scorpion") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }
            stats {
                hitpoints = 35
                attack = 25
                strength = 28
                defence = 22
            }
            bonuses {
                attackStab = 18
                attackSlash = 18
                attackCrush = 18
                strengthBonus = 20
                defenceStab = 15
                defenceSlash = 15
                defenceCrush = 15
                defenceMagic = 8
                defenceRanged = 15
            }
            anims {
                attack = 6254
                block = 6255
                death = 6256
            }
            aggro {
                radius = 7
                searchDelay = 3
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Catablepon Combat Configuration
         * Level 53-54 creatures, high-level training.
         */
        setCombatDef("npc.catablepon") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            stats {
                hitpoints = 70
                attack = 55
                strength = 60
                defence = 50
            }
            bonuses {
                attackStab = 35
                attackSlash = 35
                attackCrush = 35
                strengthBonus = 40
                defenceStab = 30
                defenceSlash = 30
                defenceCrush = 30
                defenceMagic = 15
                defenceRanged = 30
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 8
                searchDelay = 2
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Shade Combat Configuration
         * Level 61 creature, powerful undead.
         */
        setCombatDef("npc.shade") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            stats {
                hitpoints = 80
                attack = 65
                strength = 70
                defence = 60
                magic = 50
            }
            bonuses {
                attackStab = 40
                attackSlash = 40
                attackCrush = 40
                attackMagic = 35
                strengthBonus = 45
                defenceStab = 35
                defenceSlash = 35
                defenceCrush = 35
                defenceMagic = 25
                defenceRanged = 35
            }
            anims {
                attack = 5540
                block = 5541
                death = 5542
            }
            aggro {
                radius = 8
                searchDelay = 2
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
        
        /**
         * Ankou Combat Configuration
         * Level 60-63 creatures, high-level undead warriors.
         */
        setCombatDef("npc.ankou") {
            configs {
                attackSpeed = 4
                respawnDelay = 30
            }
            stats {
                hitpoints = 75
                attack = 60
                strength = 65
                defence = 55
            }
            bonuses {
                attackStab = 38
                attackSlash = 38
                attackCrush = 38
                strengthBonus = 42
                defenceStab = 32
                defenceSlash = 32
                defenceCrush = 32
                defenceMagic = 18
                defenceRanged = 32
            }
            anims {
                attack = 422
                block = 424
                death = 836
            }
            aggro {
                radius = 8
                searchDelay = 2
                alwaysAggro()
            }
            // Drops configured in StrongholdOfSecurityDropsPlugin.kt
        }
    }
}

