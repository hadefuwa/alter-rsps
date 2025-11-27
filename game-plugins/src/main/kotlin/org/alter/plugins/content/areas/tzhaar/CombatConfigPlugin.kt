package org.alter.plugins.content.areas.tzhaar

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.api.cfg.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*

/**
 * TzHaar City Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all combat-capable NPCs in TzHaar City.
 * 
 * Combat NPCs:
 * - TzHaar-Mej: Mage-type NPCs (Combat Level 86) - Use fire magic and melee
 * - TzHaar-Ket: Fighter-type NPCs (Combat Level 86) - Use melee attacks
 * - TzHaar-Xil: Ranger-type NPCs (Combat Level 86) - Use ranged and melee attacks
 * - TzTok-Jad: Final boss of the Fight Cave (Combat Level 702) - Uses all combat styles
 * 
 * Non-Combat NPCs (not configured here):
 * - TzHaar-Hur: Artisans, non-aggressive
 * - Named NPCs (Mej-Jal, Mej-Kah, Hur-Tel, Hur-Lek, Mej-Roh): Shopkeepers and quest NPCs
 * - Banker: Banking services
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class CombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * TzHaar-Mej Combat Configuration
         * 
         * Mage-type TzHaar NPCs that use fire magic and melee attacks.
         * Combat Level: 86
         * All TzHaar-Mej variants share the same combat stats.
         */
        setCombatDef(
            "npc.tzhaarmej",
            "npc.tzhaarmej_2155",
            "npc.tzhaarmej_2156",
            "npc.tzhaarmej_2157",
            "npc.tzhaarmej_2158",
            "npc.tzhaarmej_2159"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 100
                attack = 85
                strength = 85
                defence = 85
                magic = 120  // High magic level for fire magic attacks
                ranged = 1
            }
            
            bonuses {
                attackStab = 60
                attackSlash = 60
                attackCrush = 60
                attackMagic = 80  // High magic attack bonus
                strengthBonus = 60
                defenceStab = 60
                defenceSlash = 60
                defenceCrush = 60
                defenceMagic = 50
                defenceRanged = 60
            }
            
            anims {
                attack = 261  // TzHaar-Mej attack animation (magic/staff)
                block = 262  // TzHaar-Mej block animation
                death = 263  // TzHaar-Mej death animation
            }
            
            sound {
                attackSound = Sound.TZHAAR_MEJ_ATTACK_STAFF
                blockSound = Sound.TZHAAR_MEJ_HIT
                deathSound = Sound.TZHAAR_MEJ_DEATH
            }
            
            aggro {
                radius = 8  // Moderate aggro radius
                searchDelay = 3
                alwaysAggro()  // Always aggressive in TzHaar City
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    // Tokkul (TzHaar currency)
                    add("item.tokkul", min = 1000, max = 50000, weight = 100)
                    
                    // Runes (magic-focused drops)
                    add("item.fire_rune", min = 100, max = 1000, weight = 75)
                    add("item.chaos_rune", min = 100, max = 750, weight = 75)
                    add("item.death_rune", min = 100, max = 500, weight = 75)
                    add("item.blood_rune", min = 100, max = 250, weight = 75)
                    
                    // Gems (uncut)
                    add("item.obsidian_platebody_noted", min = 1, max = 20, weight = 1)
                    add("item.obsidian_platelegs_noted", min = 1, max = 20, weight = 1)
                    add("item.toktz_ket_xil_noted", min = 1, max = 5, weight = 1)
                    add("item.obsidiab_helmet_noted", min = 1, max = 20, weight = 1)
                    add("item.toktz_mej_tal_noted", min = 1, max = 5, weight = 1)
                    add("item.obsidian_cape_noted", min = 1, max = 20, weight = 1)
                }
            }
        }
        
        /**
         * TzHaar-Ket Combat Configuration
         * 
         * Fighter-type TzHaar NPCs that use melee attacks with TzHaar-Ket-Om weapons.
         * Combat Level: 86
         * All TzHaar-Ket variants share the same combat stats.
         */
        setCombatDef(
            "npc.tzhaarket",
            "npc.tzhaarket_2174",
            "npc.tzhaarket_2175",
            "npc.tzhaarket_2176",
            "npc.tzhaarket_2177"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 100
                attack = 120
                strength = 120
                defence = 100
                magic = 1
                ranged = 1
            }
            
            bonuses {
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
                attackMagic = 0
                strengthBonus = 80  // High strength bonus for melee damage
                defenceStab = 70
                defenceSlash = 70
                defenceCrush = 70
                defenceMagic = 40
                defenceRanged = 70
            }
            
            anims {
                attack = 2661  // TzHaar-Ket attack animation (HUMAN_TZHAAR_KET_OM_SWING)
                block = 424  // Standard block animation
                death = 836  // Standard death animation
            }
            
            sound {
                attackSound = Sound.TZHAAR_KET_ATTACK_CLUB
                blockSound = Sound.TZHAAR_KET_HIT_SHIELD
                deathSound = Sound.TZHAAR_KET_DEATH
            }
            
            aggro {
                radius = 8
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    // Tokkul (TzHaar currency)
                    add("item.tokkul", min = 1000, max = 50000, weight = 100)
                    
                    // Runes (magic-focused drops)
                    add("item.fire_rune", min = 100, max = 1000, weight = 75)
                    add("item.chaos_rune", min = 100, max = 750, weight = 75)
                    add("item.death_rune", min = 100, max = 500, weight = 75)
                    add("item.blood_rune", min = 100, max = 250, weight = 75)
                    
                    // Gems (uncut)
                    add("item.obsidian_platebody_noted", min = 1, max = 20, weight = 1)
                    add("item.obsidian_platelegs_noted", min = 1, max = 20, weight = 1)
                    add("item.toktz_ket_xil_noted", min = 1, max = 5, weight = 1)
                    add("item.obsidiab_helmet_noted", min = 1, max = 20, weight = 1)
                    add("item.toktz_ket_om_noted", min = 1, max = 5, weight = 1)
                    add("item.toktz_ket_em_noted", min = 1, max = 5, weight = 1)
                    add("item.toktz_xil_ak_noted", min = 1, max = 5, weight = 1)
                    add("item.toktz_xil_ek_noted", min = 1, max = 5, weight = 1)
                    add("item.obsidian_cape_noted", min = 1, max = 20, weight = 1)
                }
            }
        }
        
        /**
         * TzHaar-Xil Combat Configuration
         * 
         * Ranger-type TzHaar NPCs that use ranged and melee attacks.
         * Combat Level: 86
         * All TzHaar-Xil variants share the same combat stats.
         */
        setCombatDef(
            "npc.tzhaarxil",
            "npc.tzhaarxil_2168",
            "npc.tzhaarxil_2169",
            "npc.tzhaarxil_2170"
        ) {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 100
                attack = 100
                strength = 100
                defence = 85
                magic = 1
                ranged = 120  // High ranged level for ranged attacks
            }
            
            bonuses {
                attackStab = 70
                attackSlash = 70
                attackCrush = 70
                attackMagic = 0
                attackRanged = 80  // High ranged attack bonus
                strengthBonus = 70
                defenceStab = 60
                defenceSlash = 60
                defenceCrush = 60
                defenceMagic = 40
                defenceRanged = 60
            }
            
            anims {
                attack = 266  // TzHaar-Xil attack animation
                block = 270  // TzHaar-Xil block animation
                death = 270  // TzHaar-Xil death animation
            }
            
            sound {
                attackSound = Sound.TZHAAR_XIL_ATTACK_BLADE
                blockSound = Sound.TZHAAR_XIL_HIT
                deathSound = Sound.TZHAAR_XIL_DEATH
            }
            
            aggro {
                radius = 8
                searchDelay = 3
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 128) {
                    // Tokkul (TzHaar currency)
                    add("item.tokkul", min = 1000, max = 10000, weight = 40)
                    
                    // Ranged equipment
                    add("item.toktz_xil_ul", min = 100, max = 1000, weight = 10)
                    add("item.toktz_ket_xil_noted", min = 1, max = 5, weight = 1)
                    
                    // Arrows/ammunition
                    add("item.rune_arrow", min = 100, max = 1000, weight = 20)
                    add("item.dragon_arrow", min = 1, max = 100, weight = 10)
                    
                    // Gems (uncut)
                    add("item.obsidian_platebody_noted", min = 1, max = 20, weight = 1)
                    add("item.obsidian_platelegs_noted", min = 1, max = 20, weight = 1)
                    add("item.obsidiab_helmet_noted", min = 1, max = 20, weight = 1)
                    add("item.obsidian_cape_noted", min = 1, max = 20, weight = 1)
                }
            }
        }
        
        /**
         * TzTok-Jad Combat Configuration
         * 
         * TzTok-Jad is the final boss of the TzHaar Fight Cave minigame.
         * Combat Level: 702
         * Uses only ranged and magic attacks (melee disabled)
         * 
         * Drops:
         * - Fire Cape (guaranteed drop)
         * - Tokkul and other valuable items
         */
        setCombatDef("npc.tztokjad") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 250  // High HP for a boss
                attack = 0  // Melee disabled
                strength = 0  // Melee disabled
                defence = 150
                magic = 150
                ranged = 150
            }
            
            bonuses {
                attackStab = 0  // Melee disabled
                attackSlash = 0  // Melee disabled
                attackCrush = 0  // Melee disabled
                attackMagic = 100
                attackRanged = 100
                strengthBonus = 0  // Melee disabled
                defenceStab = 100
                defenceSlash = 100
                defenceCrush = 100
                defenceMagic = 100
                defenceRanged = 100
            }
            
            anims {
                attack = 2656  // Jad attack animation
                block = 2655  // Jad block animation
                death = 2657  // Jad death animation
            }
            
            sound {
                // Note: Attack sounds are handled in TzTokJadCombatPlugin for ranged/magic distinction
                deathSound = Sound.TZTOK_JAD_DEATH
            }
            
            aggro {
                radius = 10  // Large aggro radius for a boss
                searchDelay = 2
                alwaysAggro()
            }
            
            drops {
                always {
                    add("item.bones", 1)
                    add("item.fire_cape", 1)  // Guaranteed fire cape drop
                }
                
                main(weight = 128) {
                    // Tokkul (TzHaar currency) - large amounts for a boss
                    add("item.tokkul", min = 10000, max = 100000, weight = 100)
                    
                    // High-level runes
                    add("item.death_rune", min = 500, max = 2000, weight = 75)
                    add("item.blood_rune", min = 500, max = 1500, weight = 75)
                    add("item.soul_rune", min = 500, max = 1000, weight = 75)
                    
                    // Obsidian equipment
                    add("item.obsidian_platebody_noted", min = 1, max = 5, weight = 10)
                    add("item.obsidian_platelegs_noted", min = 1, max = 5, weight = 10)
                    add("item.obsidiab_helmet_noted", min = 1, max = 5, weight = 10)
                    add("item.obsidian_cape_noted", min = 1, max = 5, weight = 10)
                }
            }
        }
    }
}