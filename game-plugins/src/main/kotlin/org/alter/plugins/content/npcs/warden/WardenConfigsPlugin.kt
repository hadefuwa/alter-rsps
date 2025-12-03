package org.alter.plugins.content.npcs.warden

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
 * Warden Boss Configuration Plugin
 * 
 * The Warden is the final boss of the Tombs of Amascut raid.
 * This is a high-level boss encounter with significant hitpoints and combat stats.
 * 
 * Location: 3237, 2774
 * Combat Level: 700+
 * Hitpoints: 2500+
 */
class WardenConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Spawn the Warden at the boss location
        spawnNpc("npc.tumekens_warden_11756", x = 3237, z = 2774, height = 0, walkRadius = 5)

        // Ensure the warden always respawns after death
        onNpcSpawn("npc.tumekens_warden_11756") {
            val npc = ctx as Npc
            npc.respawns = true
        }

        // Ensure the warden always respawns - prevent removal during combat
        onAnyNpcDeath {
            val npc = ctx as Npc
            if (npc.id == getRSCM("npc.tumekens_warden_11756")) {
                // Force respawns to true to prevent incorrect removal
                npc.respawns = true
            }
        }

        setCombatDef("npc.tumekens_warden_11756") {
            configs {
                attackSpeed = 4
                respawnDelay = 50  // Respawn delay (50 ticks = ~30 seconds)
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
            }

            stats {
                hitpoints = 2500  // High HP for a challenging boss encounter
                attack = 400
                strength = 400
                defence = 400
                magic = 400
                ranged = 400
            }

            bonuses {
                attackStab = 300
                attackSlash = 300
                attackCrush = 300
                attackMagic = 350
                attackRanged = 350

                defenceStab = 300
                defenceSlash = 300
                defenceCrush = 300
                defenceMagic = 320
                defenceRanged = 320

                attackBonus = 300
                strengthBonus = 350
                rangedStrengthBonus = 350
                magicDamageBonus = 400
            }

            anims {
                attack = 422  // Magic attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    // High-value raid drops
                    add("item.coins_995", min = 5000000, max = 10000000, weight = 30)
                    
                    // Weapons and armour
                    add("item.dragon_scimitar", min = 1, weight = 50)
                    add("item.dragon_longsword", min = 1, weight = 50)
                    add("item.dragon_battleaxe", min = 1, weight = 50)
                    add("item.dragon_2h_sword", min = 1, weight = 50)
                    add("item.dragon_chainbody", min = 1, weight = 50)
                    add("item.dragon_platelegs", min = 1, weight = 50)
                    add("item.dragon_plateskirt", min = 1, weight = 50)
                    add("item.dragon_sq_shield", min = 1, weight = 50)
                    add("item.dragon_kiteshield", min = 1, weight = 50)
                    add("item.dragon_full_helm", min = 1, weight = 50)
                    add("item.dragon_med_helm", min = 1, weight = 50)
                    
                    // Ranged items
                    add("item.dragon_crossbow", min = 1, weight = 30)
                    add("item.dragon_arrow", min = 100, max = 500, weight = 55)
                    add("item.dragonstone_bolts_e", min = 50, max = 200, weight = 37)
                    
                    // Magic items
                    add("item.ancient_staff", min = 1, weight = 50)
                    add("item.mystic_robe_top", min = 1, weight = 90)
                    add("item.mystic_robe_bottom", min = 1, weight = 90)
                    
                    // Runes
                    add("item.death_rune", min = 100, max = 500, weight = 100)
                    add("item.blood_rune", min = 50, max = 250, weight = 80)
                    add("item.soul_rune", min = 50, max = 250, weight = 80)
                    add("item.nature_rune", min = 100, max = 500, weight = 100)
                    add("item.law_rune", min = 50, max = 250, weight = 120)
                    add("item.chaos_rune", min = 200, max = 1000, weight = 120)
                    
                    // Food and potions
                    add("item.shark", min = 25, max = 50, weight = 120)
                    add("item.manta_ray", min = 25, max = 50, weight = 115)
                    add("item.saradomin_brew4", min = 10, max = 20, weight = 115)
                    add("item.super_restore4", min = 10, max = 20, weight = 115)
                    add("item.super_combat_potion4", min = 3, max = 10, weight = 100)
                    add("item.prayer_potion4", min = 30, max = 50, weight = 125)
                    
                    // Valuable resources
                    add("item.uncut_diamond", min = 10, max = 30, weight = 120)
                    add("item.uncut_ruby", min = 15, max = 40, weight = 130)
                    add("item.uncut_emerald", min = 20, max = 50, weight = 150)
                    add("item.uncut_sapphire", min = 25, max = 60, weight = 150)
                    add("item.gold_ore", min = 200, max = 500, weight = 155)
                    add("item.coal", min = 300, max = 600, weight = 150)
                    add("item.runite_ore", min = 10, max = 50, weight = 100)
                    
                    // Clue scrolls
                    add("item.casket_elite", min = 1, weight = 75)
                    add("item.casket_master", min = 1, weight = 70)
                    
                    // Seeds and herbs
                    add("item.ranarr_seed", min = 5, max = 15, weight = 100)
                    add("item.snapdragon_seed", min = 3, max = 10, weight = 95)
                    add("item.torstol_seed", min = 2, max = 8, weight = 90)
                    add("item.grimy_ranarr_weed", min = 10, max = 30, weight = 100)
                    add("item.grimy_snapdragon", min = 5, max = 20, weight = 95)
                    add("item.grimy_torstol", min = 3, max = 15, weight = 90)
                    
                    // Rare drops
                    add("item.tumekens_shadow_uncharged", min = 1, weight = 1)
                    add("item.osmentoms_fang", min = 1, weight = 3)
                    add("item.masori_mask", min = 1, weight = 2)
                    add("item.masori_body", min = 1, weight = 2)
                    add("item.masori_chaps", min = 1, weight = 2)
                    add("item.thread_of_elidinis", min = 1, weight = 20)
                    add("item.elidinis_ward", min = 1, weight = 2)
                    add("item.masori_asemberler", min = 1, weight = 4)
                }
            }
        }
    }
}


