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
 * This plugin configures combat definitions for all revenant NPCs with individual stats
 * and a universal drop table. All revenants share the same drop table, which includes:
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
         * Universal revenant drops - applied to all revenants
         * - Always: Revenant ether (10-1000)
         * - Main: Standard valuable drops (bars, food, potions, runes, etc.)
         * - Tertiary: Rare wilderness weapons
         */
        
        /**
         * Revenant Imp - Combat Level 7
         * HP: 10
         */
        setCombatDef("npc.revenant_imp") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 10
                attack = 2
                strength = 100  // Increased for max hit ~15-20
                defence = 1
                magic = 1
                ranged = 1
            }
            
            anims {
                attack = 422
                block = 424
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Goblin - Combat Level 15
         * HP: 20
         */
        setCombatDef("npc.revenant_goblin") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 20
                attack = 10
                strength = 110  // Increased for max hit ~18-22
                defence = 5
                magic = 5
                ranged = 5
            }
            
            anims {
                attack = 6184
                block = 6185
                death = 6182
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Pyrefiend - Combat Level 52
         * HP: 50
         */
        setCombatDef("npc.revenant_pyrefiend") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 50
                attack = 80
                strength = 120  // Increased for max hit ~20-25
                defence = 40
                magic = 40
                ranged = 40
            }
            
            anims {
                attack = 1582
                block = 1583
                death = 1581
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Hobgoblin - Combat Level 60
         * HP: 60
         */
        setCombatDef("npc.revenant_hobgoblin") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 60
                attack = 100
                strength = 130  // Increased for max hit ~22-27
                defence = 50
                magic = 50
                ranged = 50
            }
            
            anims {
                attack = 6184
                block = 6185
                death = 6182
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Cyclops - Combat Level 82
         * HP: 80
         */
        setCombatDef("npc.revenant_cyclops") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 80
                attack = 120
                strength = 140  // Increased for max hit ~24-29
                defence = 60
                magic = 60
                ranged = 60
            }
            
            anims {
                attack = 4652
                block = 4653
                death = 4651
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Hellhound - Combat Level 90
         * HP: 90
         */
        setCombatDef("npc.revenant_hellhound") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 90
                attack = 140
                strength = 150  // Increased for max hit ~25-30
                defence = 70
                magic = 70
                ranged = 70
            }
            
            anims {
                attack = 6579
                block = 6578
                death = 6576
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Demon (Greater Demon) - Combat Level 98
         * HP: 100
         */
        setCombatDef("npc.revenant_demon") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 100
                attack = 160
                strength = 160  // Already good for max hit ~27-32
                defence = 80
                magic = 80
                ranged = 80
            }
            
            anims {
                attack = 64
                block = 65
                death = 67
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Ork - Combat Level 105
         * HP: 110
         */
        setCombatDef("npc.revenant_ork") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 110
                attack = 170
                strength = 170  // Already good for max hit ~28-33
                defence = 85
                magic = 85
                ranged = 85
            }
            
            anims {
                attack = 6184
                block = 6185
                death = 6182
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Dark Beast - Combat Level 120
         * HP: 120
         */
        setCombatDef("npc.revenant_dark_beast") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 120
                attack = 200
                strength = 200  // Already good for max hit ~30-35
                defence = 100
                magic = 100
                ranged = 100
            }
            
            anims {
                attack = 2731
                block = 2732
                death = 2730
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Knight - Combat Level 126
         * HP: 130
         */
        setCombatDef("npc.revenant_knight") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 130
                attack = 220
                strength = 220  // Already good for max hit ~32-37
                defence = 110
                magic = 110
                ranged = 110
            }
            
            anims {
                attack = 406
                block = 1156
                death = 836
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
        
        /**
         * Revenant Dragon - Combat Level 135
         * HP: 140
         */
        setCombatDef("npc.revenant_dragon") {
            configs {
                attackSpeed = 4
                respawnDelay = 50
            }
            
            stats {
                hitpoints = 140
                attack = 240
                strength = 240  // Already good for max hit ~33-38
                defence = 120
                magic = 120
                ranged = 120
            }
            
            anims {
                attack = 80
                block = 89
                death = 92
            }
            
            aggro {
                radius = 10
                searchDelay = 1
                alwaysAggro()
            }
            
            species {
                +NpcSpecies.UNDEAD
            }
            
            drops {
                // Always drop revenant ether (10-1000), food, and prayer potions
                always {
                    add("item.revenant_ether", min = 10, max = 1000)
                    add("item.manta_ray", min = 1, max = 5)
                    add("item.prayer_potion4", min = 1, max = 3)
                }
                
                // Main drop table with standard valuable items
                // Total weight: 271 (matches total item weights to prevent roll failures)
                main(weight = 271) {
                    // Bars (valuable smithing materials) - reduced weights
                    add("item.adamantite_bar", min = 1, max = 5, weight = 12)
                    add("item.runite_bar", min = 1, max = 3, weight = 6)
                    
                    // Food (high healing items) - reduced weights
                    add("item.manta_ray", min = 1, max = 10, weight = 15)
                    add("item.cooked_karambwan", min = 1, max = 15, weight = 12)
                    
                    // Potions (useful consumables) - reduced weights
                    add("item.prayer_potion4", min = 1, max = 5, weight = 10)
                    add("item.prayer_potion3", min = 1, max = 5, weight = 8)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    add("item.super_restore3", min = 1, max = 3, weight = 5)
                    
                    // Runes (magic supplies) - reduced weights
                    add("item.death_rune", min = 20, max = 100, weight = 12)
                    add("item.blood_rune", min = 15, max = 80, weight = 10)
                    add("item.chaos_rune", min = 30, max = 150, weight = 14)
                    add("item.soul_rune", min = 10, max = 50, weight = 8)
                    add("item.law_rune", min = 20, max = 100, weight = 11)
                    add("item.nature_rune", min = 25, max = 120, weight = 13)
                    
                    // Coins are handled separately with level-based scaling (100k-5m)
                    // See NpcLootDropPlugin.dropWildernessCoins() for revenant coin drops
                    
                    // Other valuable items - reduced weights
                    add("item.dragon_bones_noted", min = 1, max = 20, weight = 8) // Quantity scaled by level in NpcLootDropPlugin
                    add("item.uncut_diamond", min = 1, max = 5, weight = 6)
                    add("item.uncut_ruby", min = 1, max = 8, weight = 8)
                    add("item.uncut_emerald", min = 1, max = 10, weight = 10)
                    add("item.bracelet_of_ethereum_uncharged", min = 1, weight = 8)
                    add("item.rune_dagger", min = 1, weight = 4)
                    add("item.rune_sword", min = 1, weight = 3)
                    add("item.rune_scimitar", min = 1, weight = 3)
                    add("item.rune_chainbody", min = 1, weight = 2)
                    add("item.rune_platelegs", min = 1, weight = 2)
                    add("item.rune_plateskirt", min = 1, weight = 2)
                    add("item.rune_platebody", min = 1, weight = 3) // Increased from 2 to 3
                    
                    // High-value alch items - increased weights for better drop rates
                    add("item.dragon_platelegs", min = 1, weight = 3) // Increased from 1 to 3
                    add("item.dragon_med_helm", min = 1, weight = 4)
                    add("item.dragon_longsword", min = 1, weight = 4)
                    add("item.dragon_scimitar", min = 1, weight = 4)
                    
                    // Barrows armour (rare high-value drops) - increased weights
                    add("item.ahrims_hood", min = 1, weight = 2)
                    add("item.ahrims_robetop", min = 1, weight = 2)
                    add("item.ahrims_robeskirt", min = 1, weight = 2)
                    add("item.dharoks_helm", min = 1, weight = 2)
                    add("item.dharoks_platebody", min = 1, weight = 2)
                    add("item.dharoks_platelegs", min = 1, weight = 2)
                    add("item.guthans_helm", min = 1, weight = 2)
                    add("item.guthans_platebody", min = 1, weight = 2)
                    add("item.guthans_chainskirt", min = 1, weight = 2)
                    add("item.karils_coif", min = 1, weight = 2)
                    add("item.karils_leathertop", min = 1, weight = 2)
                    add("item.karils_leatherskirt", min = 1, weight = 2)
                    add("item.torags_helm", min = 1, weight = 2)
                    add("item.torags_platebody", min = 1, weight = 2)
                    add("item.torags_platelegs", min = 1, weight = 2)
                    add("item.veracs_helm", min = 1, weight = 2)
                    add("item.veracs_brassard", min = 1, weight = 2)
                    add("item.veracs_plateskirt", min = 1, weight = 2)
                    
                    // Chinchompas (ranged training items) - slightly reduced
                    add("item.red_chinchompa", min = 50, max = 200, weight = 6)
                    add("item.black_chinchompa", min = 50, max = 200, weight = 5)
                    
                    // Rune bolts and gems - slightly reduced
                    add("item.runite_bolts", min = 100, max = 500, weight = 8)
                    add("item.ruby", min = 5, max = 20, weight = 6)
                    add("item.diamond", min = 5, max = 20, weight = 6)
                }
                
                // Tertiary drop table for rare wilderness weapons, party hats, sigils, and corrupted weapons
                // Drop rates: Amulet of Avarice (rarest: 1/1024), Wilderness weapons (1/512), Corrupted weapons (1/384), Party hats (1/256), Sigils (1/256)
                tertiary(weight = 512) {
                    add("item.craws_bow", min = 1, weight = 512)
                    add("item.thammarons_sceptre", min = 1, weight = 512)
                    add("item.viggoras_chainmace", min = 1, weight = 512)
                    add("item.webweaver_bow", min = 1, weight = 512)
                    add("item.ursine_chainmace", min = 1, weight = 512)
                    add("item.accursed_sceptre", min = 1, weight = 512)
                    add("item.amulet_of_avarice", min = 1, weight = 1024) // Rarest drop
                    // Corrupted weapons (rare high-tier weapons)
                    add("item.corrupted_twisted_bow", min = 1, weight = 384)
                    add("item.corrupted_scythe_of_vitur", min = 1, weight = 384)
                    add("item.blade_of_saeldor_c", min = 1, weight = 384)
                    add("item.corrupted_bow_attuned", min = 1, weight = 384)
                    add("item.corrupted_staff_attuned", min = 1, weight = 384)
                    add("item.corrupted_halberd_attuned", min = 1, weight = 384)
                    add("item.corrupted_bow_perfected", min = 1, weight = 384)
                    add("item.corrupted_staff_perfected", min = 1, weight = 384)
                    add("item.corrupted_halberd_perfected", min = 1, weight = 384)
                    // Party hats (more common cosmetic items)
                    add("item.red_partyhat", min = 1, weight = 256)
                    add("item.yellow_partyhat", min = 1, weight = 256)
                    add("item.blue_partyhat", min = 1, weight = 256)
                    add("item.green_partyhat", min = 1, weight = 256)
                    add("item.purple_partyhat", min = 1, weight = 256)
                    add("item.white_partyhat", min = 1, weight = 256)
                    // Sigils (more common utility and combat items)
                    add("item.sigil_of_versatility", min = 1, weight = 256)
                    add("item.sigil_of_adroit", min = 1, weight = 256)
                    add("item.sigil_of_pious_protection", min = 1, weight = 256)
                    add("item.sigil_of_the_dwarves", min = 1, weight = 256)
                    add("item.sigil_of_preservation", min = 1, weight = 256)
                    add("item.sigil_of_specialised_strikes", min = 1, weight = 256)
                    add("item.sigil_of_exaggeration", min = 1, weight = 256)
                    add("item.sigil_of_rampart", min = 1, weight = 256)
                    add("item.sigil_of_titanium", min = 1, weight = 256)
                    add("item.sigil_of_resilience", min = 1, weight = 256)
                    add("item.sigil_of_consistency", min = 1, weight = 256)
                    add("item.sigil_of_the_formidable_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_rigorous_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_meticulous_mage", min = 1, weight = 256)
                    add("item.sigil_of_fortification", min = 1, weight = 256)
                    add("item.sigil_of_barrows", min = 1, weight = 256)
                    add("item.sigil_of_deft_strikes", min = 1, weight = 256)
                    add("item.sigil_of_freedom", min = 1, weight = 256)
                    add("item.sigil_of_the_ruthless_ranger", min = 1, weight = 256)
                    add("item.sigil_of_the_feral_fighter", min = 1, weight = 256)
                    add("item.sigil_of_the_menacing_mage", min = 1, weight = 256)
                    add("item.sigil_of_prosperity", min = 1, weight = 256)
                    add("item.sigil_of_the_elves", min = 1, weight = 256)
                    add("item.sigil_of_the_barbarians", min = 1, weight = 256)
                    add("item.sigil_of_the_gnomes", min = 1, weight = 256)
                    add("item.sigil_of_nature", min = 1, weight = 256)
                    add("item.sigil_of_devotion", min = 1, weight = 256)
                    add("item.sigil_of_slaughter", min = 1, weight = 256)
                    add("item.sigil_of_the_serpent", min = 1, weight = 256)
                    add("item.sigil_of_supreme_stamina", min = 1, weight = 256)
                    add("item.sigil_of_finality", min = 1, weight = 256)
                    add("item.sigil_of_aggression", min = 1, weight = 256)
                    add("item.sigil_of_rampage", min = 1, weight = 256)
                }
            }
        }
    }
}
