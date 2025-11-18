package org.alter.plugins.content.areas.revenantcaves

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Revenant Combat Configuration Plugin
 * 
 * This plugin configures combat definitions for all revenant NPCs with a universal drop table.
 * All revenants share the same drop table, which includes:
 * - Guaranteed revenant ether (10-1000)
 * - Standard drops: adamant bars, rune bars, manta rays, karambwans, prayer potions, and other valuable items
 * - Rare wilderness weapons: Craw's Bow, Thammaron's Sceptre, Viggora's Chainmace, Webweaver Bow, Ursine Chainmace, Accursed Sceptre
 * 
 * @param r The plugin repository for registering combat configurations
 * @param world The game world instance
 * @param server The server instance
 */
class RevenantCombatConfigPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /**
         * Universal Revenant Drop Table Configuration
         * 
         * All revenant NPCs share this drop table:
         * - Always: Revenant ether (10-1000)
         * - Main: Standard valuable drops (bars, food, potions, runes, etc.)
         * - Tertiary: Rare wilderness weapons
         */
        setCombatDef(
            "npc.revenant_imp",
            "npc.revenant_goblin",
            "npc.revenant_pyrefiend",
            "npc.revenant_hobgoblin",
            "npc.revenant_cyclops",
            "npc.revenant_hellhound",
            "npc.revenant_demon",
            "npc.revenant_ork",
            "npc.revenant_dark_beast",
            "npc.revenant_knight",
            "npc.revenant_dragon"
        ) {
            drops {
                // Always drop revenant ether (10-1000)
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                }
                
                // Main drop table with standard valuable items
                main(weight = 100) {
                    // Bars (valuable smithing materials)
                    add("item.adamantite_bar", min = 1, max = 5, weight = 15)
                    add("item.runite_bar", min = 1, max = 3, weight = 8)
                    
                    // Food (high healing items)
                    add("item.manta_ray", min = 1, max = 10, weight = 20)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 18)
                    
                    // Potions (useful consumables)
                    add("item.prayer_potion4", min = 1, max = 5, weight = 12)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 10)
                    add("item.super_restore4", min = 1, max = 3, weight = 8)
                    add("item.super_restore3", min = 1, max = 3, weight = 6)
                    
                    // Runes (magic supplies)
                    add("item.death_rune", min = 20, max = 100, weight = 15)
                    add("item.blood_rune", min = 15, max = 80, weight = 12)
                    add("item.chaos_rune", min = 30, max = 150, weight = 18)
                    add("item.soul_rune", min = 10, max = 50, weight = 10)
                    add("item.law_rune", min = 20, max = 100, weight = 14)
                    add("item.nature_rune", min = 25, max = 120, weight = 16)
                    
                    // Coins (direct wealth)
                    add("item.coins_995", min = 5000, max = 50000, weight = 25)
                    
                    // Other valuable items
                    add("item.dragon_bones", min = 1, max = 5, weight = 10)
                    add("item.uncut_diamond", min = 1, max = 5, weight = 8)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 10)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 12)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 10)
                    add("item.rune_dagger", min = 1, weight = 5)
                    add("item.rune_sword", min = 1, weight = 4)
                    add("item.rune_scimitar", min = 1, weight = 4)
                    add("item.rune_chainbody", min = 1, weight = 3)
                    add("item.rune_platelegs", min = 1, weight = 3)
                    add("item.rune_plateskirt", min = 1, weight = 3)
                }
                
                // Tertiary drop table for rare wilderness weapons
                // These are very rare drops (1/512 chance each)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                }
            }
        }
    }
}

