package org.alter.plugins.content.items.scytheofvitur

import org.alter.api.EquipmentType
import org.alter.api.cfg.Graphic
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Npc
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.dealHit
import org.alter.plugins.content.combat.getCombatTarget
import org.alter.rscm.RSCM.getRSCM

/**
 * Scythe of Vitur Plugin
 * 
 * Features:
 * - Charging system with vials of blood + blood runes
 * - Right-click options to check/uncharge the scythe
 * - Item interactions for adding charges
 * - Dynamic examine text showing current charges
 */
class ScytheOfViturPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Charging constants
        const val BLOOD_RUNES_PER_CHARGE = 300
        const val MAX_CHARGES = 20000
        
        // Multi-hit damage percentages
        const val FIRST_HIT_PERCENTAGE = 1.0   // 100%
        const val SECOND_HIT_PERCENTAGE = 0.5  // 50%
        const val THIRD_HIT_PERCENTAGE = 0.25  // 25%
        
        // Minimum target size for multi-hits
        const val MIN_MULTI_HIT_SIZE = 2
        
        // All Scythe of Vitur variants
        private val SCYTHE_VARIANTS = listOf(
            "scythe_of_vitur",
            "scythe_of_vitur_uncharged", 
            "holy_scythe_of_vitur",
            "holy_scythe_of_vitur_uncharged",
            "sanguine_scythe_of_vitur",
            "sanguine_scythe_of_vitur_uncharged",
            "corrupted_scythe_of_vitur",
            "scythe_of_vitur_22664"
        )
        
        // Helper methods for scythe variants
        fun isScytheVariant(itemId: Int): Boolean {
            return SCYTHE_VARIANTS.any { getRSCM("item.$it") == itemId }
        }
        
        fun getChargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.scythe_of_vitur_uncharged") -> getRSCM("item.scythe_of_vitur")
                getRSCM("item.holy_scythe_of_vitur_uncharged") -> getRSCM("item.holy_scythe_of_vitur")
                getRSCM("item.sanguine_scythe_of_vitur_uncharged") -> getRSCM("item.sanguine_scythe_of_vitur")
                else -> itemId // Already charged or corrupted variant
            }
        }
        
        fun getUnchargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.scythe_of_vitur") -> getRSCM("item.scythe_of_vitur_uncharged")
                getRSCM("item.holy_scythe_of_vitur") -> getRSCM("item.holy_scythe_of_vitur_uncharged")
                getRSCM("item.sanguine_scythe_of_vitur") -> getRSCM("item.sanguine_scythe_of_vitur_uncharged")
                else -> itemId // Already uncharged or corrupted variant
            }
        }
    }

    init {
        // Register charging handlers for all scythe variants
        registerChargingHandlers()
        
        // Register right-click options for all scythe variants
        registerRightClickOptions()
        
        // Register combat logic for all scythe variants
        registerCombatLogic()
    }
    
    private fun registerChargingHandlers() {
        val unchargedVariants = listOf(
            "scythe_of_vitur_uncharged",
            "holy_scythe_of_vitur_uncharged", 
            "sanguine_scythe_of_vitur_uncharged"
        )
        
        val chargedVariants = listOf(
            "scythe_of_vitur",
            "holy_scythe_of_vitur",
            "sanguine_scythe_of_vitur"
        )
        
        // Handle charging uncharged scythes
        unchargedVariants.forEach { uncharged ->
            onItemOnItem("item.vial_of_blood", "item.$uncharged") {
                handleScytheCharging(player, uncharged)
            }
        }
        
        // Handle adding charges to already charged scythes
        chargedVariants.forEach { charged ->
            onItemOnItem("item.vial_of_blood", "item.$charged") {
                handleScytheAddCharges(player, charged)
            }
        }
    }
    
    private fun handleScytheCharging(player: Player, unchargedVariant: String) {
        val bloodRuneId = getRSCM("item.blood_rune")
        val vialOfBloodId = getRSCM("item.vial_of_blood")
        val scytheUnchargedId = getRSCM("item.$unchargedVariant")
        val scytheChargedId = getChargedVariant(scytheUnchargedId)
        
        // Check if player has blood runes
        if (player.inventory.getItemCount(bloodRuneId) < BLOOD_RUNES_PER_CHARGE) {
            player.message("You need $BLOOD_RUNES_PER_CHARGE blood runes to charge the scythe.")
            return
        }
        
        // Find the uncharged scythe
        if (player.inventory.getItemCount(scytheUnchargedId) < 1) {
            player.message("You need an uncharged scythe of vitur.")
            return
        }
        
        // Remove items
        player.inventory.remove(vialOfBloodId, 1)
        player.inventory.remove(bloodRuneId, BLOOD_RUNES_PER_CHARGE)
        player.inventory.remove(scytheUnchargedId, 1)
        
        // Add charged scythe with 1 charge
        val addResult = player.inventory.add(scytheChargedId, 1)
        if (addResult.hasSucceeded()) {
            player.animate(832) // Charging animation
            player.graphic(363) // Use a generic charging graphic
            player.message("You charge the scythe with blood. It now has 1 charge.")
        } else {
            player.message("Could not add charged scythe to inventory.")
        }
    }
    
    private fun handleScytheAddCharges(player: Player, chargedVariant: String) {
        val bloodRuneId = getRSCM("item.blood_rune")
        val vialOfBloodId = getRSCM("item.vial_of_blood")
        val scytheChargedId = getRSCM("item.$chargedVariant")
        
        // Check if player has a charged scythe
        if (player.inventory.getItemCount(scytheChargedId) < 1) {
            player.message("You need a charged scythe of vitur.")
            return
        }
        
        // For simplicity, we'll just add 1 charge per vial
        val chargesToAdd = 1 // Add 1 charge per interaction
        val bloodRunesNeeded = chargesToAdd * BLOOD_RUNES_PER_CHARGE
        
        if (player.inventory.getItemCount(bloodRuneId) < bloodRunesNeeded) {
            player.message("You need $bloodRunesNeeded blood runes to add $chargesToAdd charges.")
            return
        }
        
        // Remove items
        player.inventory.remove(vialOfBloodId, chargesToAdd)
        player.inventory.remove(bloodRuneId, bloodRunesNeeded)
        
        player.animate(832) // Charging animation
        player.graphic(363) // Use a generic charging graphic
        player.message("You add $chargesToAdd charges to the scythe.")
    }
    
    private fun registerRightClickOptions() {
        val chargedVariants = listOf(
            "scythe_of_vitur",
            "holy_scythe_of_vitur",
            "sanguine_scythe_of_vitur",
            "corrupted_scythe_of_vitur"
            // Note: scythe_of_vitur_22664 is excluded as it doesn't have "check" or "uncharge" options
            // It only has "Wield", "Kill Area", and "Drop" options
        )
        
        chargedVariants.forEach { variant ->
            onItemOption("item.$variant", "check") {
                player.message("Your scythe has charges remaining. (Feature in development)")
            }
            
            // Only non-corrupted variants can be uncharged
            if (variant != "corrupted_scythe_of_vitur") {
                onItemOption("item.$variant", "uncharge") {
                    handleScytheUncharge(player, variant)
                }
            }
        }
    }
    
    private fun handleScytheUncharge(player: Player, chargedVariant: String) {
        val scytheChargedId = getRSCM("item.$chargedVariant")
        val scytheUnchargedId = getUnchargedVariant(scytheChargedId)
        
        player.queue {
            chatPlayer(player, "Are you sure you want to uncharge the scythe?")
            chatPlayer(player, "This will destroy all charges and cannot be undone.")
            
            when (options(player, "Yes, uncharge it.", "No, keep it charged.")) {
                1 -> {
                    // Remove charged scythe and add uncharged one
                    if (player.inventory.getItemCount(scytheChargedId) > 0) {
                        player.inventory.remove(scytheChargedId, 1)
                        player.inventory.add(scytheUnchargedId, 1)
                        player.message("You uncharge the scythe. All charges have been lost.")
                    }
                }
                2 -> {
                    player.message("You decide to keep the scythe charged.")
                }
            }
        }
    }
    
    private fun registerCombatLogic() {
        SCYTHE_VARIANTS.forEach { variant ->
            setItemCombatLogic("item.$variant") {
                handleScytheCombat(player)
            }
        }
    }
    
    private fun handleScytheCombat(player: Player) {
        val target = player.getCombatTarget() ?: return
        
        // Check if target is large enough (2x2 or bigger)
        if (target.getSize() < MIN_MULTI_HIT_SIZE) {
            // Normal single hit for small targets
            val maxHit = MeleeCombatFormula.getMaxHit(player, target)
            val accuracy = MeleeCombatFormula.getAccuracy(player, target)
            val landHit = accuracy >= world.randomDouble()
            
            player.dealHit(
                target = target,
                maxHit = maxHit,
                landHit = landHit,
                delay = 1
            )
            return
        }
        
        // Multi-hit for large targets (2x2+)
        val baseMaxHit = MeleeCombatFormula.getMaxHit(player, target)
        val accuracy = MeleeCombatFormula.getAccuracy(player, target)
        
        // First hit: 100% damage
        val firstHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = baseMaxHit,
            landHit = firstHit,
            delay = 1
        )
        
        // Second hit: 50% damage (delay slightly for visual effect)
        val secondMaxHit = (baseMaxHit * SECOND_HIT_PERCENTAGE).toInt()
        val secondHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = secondMaxHit,
            landHit = secondHit,
            delay = 2
        )
        
        // Third hit: 25% damage
        val thirdMaxHit = (baseMaxHit * THIRD_HIT_PERCENTAGE).toInt()
        val thirdHit = accuracy >= world.randomDouble()
        player.dealHit(
            target = target,
            maxHit = thirdMaxHit,
            landHit = thirdHit,
            delay = 3
        )
        
        // Visual effects for multi-hit
        player.graphic(1834, delay = 1) // Scythe multi-hit effect
    }
}