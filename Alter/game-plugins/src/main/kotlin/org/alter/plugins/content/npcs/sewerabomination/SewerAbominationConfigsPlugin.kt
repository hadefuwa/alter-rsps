package org.alter.plugins.content.npcs.sewerabomination

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
 * Sewer Abomination Configuration
 *
 * A mid-level boss found in the Varrock sewers
 * Spawns in the center of the sewer network
 */
class SewerAbominationConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Cerberus (5862) as Sewer Abomination in Varrock sewers
        // Coordinates: x=3237, z=9866 (adjust based on your sewer layout)
        spawnNpc("npc.cerberus", x = 3237, z = 9866, height = 0, walkRadius = 6)

        // Add attack option handler
        onNpcOption("npc.cerberus", option = "attack") {
            player.attack(npc)
        }

        setCombatDef("npc.cerberus") {
            configs {
                attackSpeed = 5  // Moderate attack speed
                respawnDelay = 100  // 60 seconds respawn (100 ticks)
            }

            aggro {
                radius = 8
                searchDelay = 1
            }

            stats {
                hitpoints = 180
                attack = 250  // Increased from 200
                strength = 280  // Increased from 220
                defence = 100
                magic = 220  // Increased from 180
                ranged = 200  // Increased from 160
            }

            bonuses {
                attackStab = 0
                attackSlash = 0
                attackCrush = 80  // Increased from 50
                attackMagic = 100  // Increased from 70
                attackRanged = 80  // Increased from 60

                defenceStab = 40
                defenceSlash = 50
                defenceCrush = 60
                defenceMagic = 20
                defenceRanged = 30

                attackBonus = 150  // Increased from 100
                strengthBonus = 130  // Increased from 90
                rangedStrengthBonus = 60  // Increased from 40
                magicDamageBonus = 50  // Increased from 30
            }

            anims {
                block = 424
                death = 2856  // Death animation
            }

            drops {
                always {
                    add("item.big_bones", 1)
                }

                main(weight = 128) {
                    // Unique drops
                    add("item.draconic_visage", min = 1, weight = 1) // Very rare
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_dagger", min = 1, weight = 3)

                    // Rune equipment
                    add("item.rune_chainbody", min = 1, weight = 8)
                    add("item.rune_platelegs", min = 1, weight = 7)
                    add("item.rune_full_helm", min = 1, weight = 10)
                    add("item.rune_scimitar", min = 1, weight = 8)

                    // Coins
                    add("item.coins_995", min = 500000, max = 2000000, weight = 25)

                    // Runes (poison/nature themed)
                    add("item.nature_rune", min = 500, max = 500, weight = 15)
                    add("item.death_rune", min = 500, max = 500, weight = 12)
                    add("item.blood_rune", min = 500, max = 500, weight = 10)
                    add("item.chaos_rune", min = 500, max = 500, weight = 15)

                    // Resources
                    add("item.grimy_ranarr", min = 3, max = 6, weight = 8)
                    add("item.grimy_snapdragon", min = 2, max = 4, weight = 5)
                    add("item.grimy_torstol", min = 1, max = 3, weight = 3)

                    // Bars and ores
                    add("item.adamantite_bar", min = 5, max = 10, weight = 10)
                    add("item.runite_ore", min = 2, max = 4, weight = 6)

                    // Food
                    add("item.shark", min = 5, max = 8, weight = 10)
                    add("item.super_restore4", min = 2, max = 3, weight = 7)

                    // Poison-themed items
                    add("item.antipoison4", min = 3, max = 5, weight = 12)
                    add("item.weapon_poison", min = 1, max = 2, weight = 8)
                }

                // Rare drop table (additional rare items)
                tertiary(weight = 256) {
                    add("item.dragon_spear", min = 1, weight = 50)
                    add("item.shield_left_half", min = 1, weight = 256)
                    add("item.dragon_platelegs", min = 1, weight = 200)
                    add("item.loop_half_of_a_key", min = 1, weight = 128)
                    add("item.tooth_half_of_a_key", min = 1, weight = 128)
                }
            }
        }

        // Note: Minion combat definitions are not set here because these NPCs
        // (zombie, archer, dark_wizard) already have combat definitions set elsewhere.
        // The guaranteed random item drop for minions is handled in SewerAbominationCombatPlugin
        // via onNpcDeath handlers that check if the NPC doesn't respawn (boss minions).
    }
}
