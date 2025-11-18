package gg.rsmod.plugins.content.npcs.combat.wilderness

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
 * @author Alycia <https://github.com/alycii>
 * Venenatis - The Spider Wilderness Boss
 * Combat Level: 464
 * Hitpoints: 850
 * Location: Silk Chasm (3319, 3754)
 * Region: 13171 (Multi-combat)
 * 
 * Special Attacks:
 * - Web Projectiles (Entangles players)
 * - Spawn Spiderlings (Summons smaller spiders)
 * - Venom Spit (Poison damage over time)
 * - Web Trap (Area denial)
 */

class VenenatisConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        // Region 13171 (around coordinates 3319, 3754)
        setMultiCombatRegion(region = 13171)
        
        spawnNpc(
            "npc.venenatis",
            x = 3319,
            z = 3754,
            height = 0,
            walkRadius = 12,
            direction = Direction.SOUTH
        )
        
        // Set combat definition
        setCombatDef("npc.venenatis") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 850
                attack = 250
                strength = 250
                defence = 200
                ranged = 280
                magic = 300
            }
            
            anims {
                attack = 422   // Spider attack animation
                block = 424    // Spider block animation  
                death = 836    // Standard death animation
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 0
                attackCrush = 0
                attackMagic = 150
                attackRanged = 120
                
                defenceStab = 100
                defenceSlash = 120
                defenceCrush = 120
                defenceMagic = 180
                defenceRanged = 160
            }
            
            slayerData {
                levelRequirement = 1
                xp = 650.0
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 1000) {
                    // Common drops
                    add("item.coins_995", min = 5000, max = 15000, weight = 150)
                    add("item.death_rune", min = 50, max = 100, weight = 100)
                    add("item.blood_rune", min = 25, max = 75, weight = 100)
                    add("item.chaos_rune", min = 100, max = 200, weight = 100)
                    add("item.raw_shark", min = 10, max = 25, weight = 80)
                    add("item.super_restore4", min = 2, max = 5, weight = 80)
                    add("item.prayer_potion4", min = 2, max = 4, weight = 60)
                    add("item.magic_logs", min = 25, max = 50, weight = 70)
                    add("item.yew_logs", min = 50, max = 100, weight = 50)
                    
                    // Uncommon drops
                    add("item.rune_platelegs", min = 1, weight = 25)
                    add("item.rune_platebody", min = 1, weight = 25)
                    add("item.rune_battleaxe", min = 1, weight = 20)
                    add("item.dragon_dagger", min = 1, weight = 15)
                    add("item.uncut_diamond", min = 5, max = 15, weight = 20)
                    add("item.uncut_dragonstone", min = 1, max = 3, weight = 10)
                    add("item.magic_seed", min = 1, max = 2, weight = 8)
                    add("item.palm_tree_seed", min = 1, weight = 7)
                    add("item.yew_seed", min = 1, max = 2, weight = 10)
                    
                    // Rare drops
                    add("item.dragon_med_helm", min = 1, weight = 8)
                    add("item.dragon_longsword", min = 1, weight = 7)
                    add("item.rune_pickaxe", min = 1, weight = 6)
                    add("item.shield_left_half", min = 1, weight = 5)
                    add("item.dragon_spear", min = 1, weight = 4)
                    add("item.clue_scroll_elite", min = 1, weight = 15)
                }
                
                // Very rare drops (0.5% chance) - Signature items
                tertiary(weight = 5) {
                    add("item.treasonous_ring", min = 1, weight = 2) // Venenatis ring
                    add("item.dragon_pickaxe", min = 1, weight = 2) // Shared with other wilderness bosses
                    add("item.dragon_axe", min = 1, weight = 1) // Very rare
                }
            }
        }
    }
}