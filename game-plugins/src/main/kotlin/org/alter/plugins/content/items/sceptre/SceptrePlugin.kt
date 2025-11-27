package org.alter.plugins.content.items.sceptre

import org.alter.api.EquipmentType
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.item.ItemAttribute
import org.alter.game.info.PlayerInfo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Sceptre Plugin
 * 
 * Features:
 * - Charging system with revenant ether (1 ether = 1 charge, max 16,000 charges)
 * - Consumes 1 ether per attack when charged
 * - Right-click options to check/uncharge the sceptre
 * - Item interactions for adding charges
 * - Charges lost on death in wilderness
 * 
 * Supports:
 * - Thammaron's Sceptre (uncharged: 22552, charged: 22555)
 * - Accursed Sceptre (uncharged: 27662, charged: 27665)
 */
class SceptrePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Charging constants
        const val MAX_CHARGES = 16000 // Maximum charges for sceptres
        
        // Sceptre variants
        private val SCEPTRE_VARIANTS = listOf(
            "thammarons_sceptre",
            "thammarons_sceptre_u",
            "accursed_sceptre",
            "accursed_sceptre_u"
        )
        
        // Helper methods for sceptre variants
        fun isSceptreVariant(itemId: Int): Boolean {
            return SCEPTRE_VARIANTS.any { getRSCM("item.$it") == itemId }
        }
        
        fun getChargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.thammarons_sceptre_u") -> getRSCM("item.thammarons_sceptre")
                getRSCM("item.accursed_sceptre_u") -> getRSCM("item.accursed_sceptre")
                else -> itemId // Already charged
            }
        }
        
        fun getUnchargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.thammarons_sceptre") -> getRSCM("item.thammarons_sceptre_u")
                getRSCM("item.accursed_sceptre") -> getRSCM("item.accursed_sceptre_u")
                else -> itemId // Already uncharged
            }
        }
    }

    init {
        // Register charging handlers
        registerChargingHandlers()
        
        // Register right-click options
        registerRightClickOptions()
        
        // Register combat logic for ether consumption
        registerCombatLogic()
        
        // Handle charge loss on death in wilderness
        registerDeathHandler()
    }
    
    private fun registerChargingHandlers() {
        val unchargedVariants = listOf(
            "thammarons_sceptre_u",
            "accursed_sceptre_u"
        )
        
        val chargedVariants = listOf(
            "thammarons_sceptre",
            "accursed_sceptre"
        )
        
        // Handle charging uncharged sceptres
        unchargedVariants.forEach { uncharged ->
            onItemOnItem("item.revenant_ether", "item.$uncharged") {
                handleSceptreCharging(player, uncharged)
            }
        }
        
        // Handle adding charges to already charged sceptres
        chargedVariants.forEach { charged ->
            onItemOnItem("item.revenant_ether", "item.$charged") {
                handleSceptreAddCharges(player, charged)
            }
        }
    }
    
    private fun handleSceptreCharging(player: Player, unchargedVariant: String) {
        val etherId = getRSCM("item.revenant_ether")
        val sceptreUnchargedId = getRSCM("item.$unchargedVariant")
        val sceptreChargedId = getChargedVariant(sceptreUnchargedId)
        
        // Check if player has revenant ether
        val etherCount = player.inventory.getItemCount(etherId)
        if (etherCount < 1) {
            player.message("You need revenant ether to charge the sceptre.")
            return
        }
        
        // Find the uncharged sceptre
        if (player.inventory.getItemCount(sceptreUnchargedId) < 1) {
            player.message("You need an uncharged sceptre.")
            return
        }
        
        // Calculate how much ether we can add (up to max charges)
        val etherToUse = etherCount.coerceAtMost(MAX_CHARGES)
        
        // Remove items
        player.inventory.remove(etherId, etherToUse)
        player.inventory.remove(sceptreUnchargedId, 1)
        
        // Add charged sceptre (forceNoStack to ensure it doesn't stack, since it will have attributes)
        val addResult = player.inventory.add(sceptreChargedId, 1, forceNoStack = true)
        if (addResult.hasSucceeded()) {
            // Find the item we just added and set charges
            // Items with attributes don't stack, so we should find it
            val sceptreIndex = player.inventory.getItemIndex(sceptreChargedId, false)
            if (sceptreIndex != -1) {
                val newItem = player.inventory[sceptreIndex]
                newItem?.putAttr(ItemAttribute.CHARGES, etherToUse)
            }
            
            player.animate(832) // Charging animation
            player.graphic(363) // Charging graphic
            player.message("You charge the sceptre with $etherToUse revenant ether.")
        } else {
            // Refund ether if we couldn't add the item
            player.inventory.add(etherId, etherToUse)
            player.message("Could not add charged sceptre to inventory.")
        }
    }
    
    private fun handleSceptreAddCharges(player: Player, chargedVariant: String) {
        val etherId = getRSCM("item.revenant_ether")
        val sceptreChargedId = getRSCM("item.$chargedVariant")
        
        // Check if player has a charged sceptre
        val sceptreIndex = player.inventory.getItemIndex(sceptreChargedId, false)
        if (sceptreIndex == -1) {
            player.message("You need a charged sceptre.")
            return
        }
        
        val sceptre = player.inventory[sceptreIndex] ?: return
        val currentCharges = sceptre.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (currentCharges >= MAX_CHARGES) {
            player.message("Your sceptre is already fully charged.")
            return
        }
        
        // Calculate how much ether we can add
        val etherCount = player.inventory.getItemCount(etherId)
        val chargesNeeded = MAX_CHARGES - currentCharges
        val etherToUse = etherCount.coerceAtMost(chargesNeeded)
        
        if (etherToUse == 0) {
            player.message("You need revenant ether to add charges.")
            return
        }
        
        // Remove ether
        player.inventory.remove(etherId, etherToUse)
        
        // Update charges
        val newCharges = currentCharges + etherToUse
        sceptre.putAttr(ItemAttribute.CHARGES, newCharges)
        
        player.animate(832) // Charging animation
        player.graphic(363) // Charging graphic
        player.message("You add $etherToUse revenant ether to the sceptre. It now has $newCharges charges.")
    }
    
    private fun registerRightClickOptions() {
        val chargedVariants = listOf(
            "thammarons_sceptre",
            "accursed_sceptre"
        )
        
        chargedVariants.forEach { variant ->
            onItemOption("item.$variant", "check") {
                val sceptreIndex = player.inventory.getItemIndex(getRSCM("item.$variant"), false)
                if (sceptreIndex == -1) {
                    // Check equipped sceptre
                    val equipped = player.getEquipment(EquipmentType.WEAPON)
                    if (equipped?.id == getRSCM("item.$variant")) {
                        val charges = equipped.getAttr(ItemAttribute.CHARGES) ?: 0
                        player.message("Your sceptre has $charges revenant ether charges remaining.")
                    } else {
                        player.message("You need to have the sceptre in your inventory or equipped.")
                    }
                } else {
                    val sceptre = player.inventory[sceptreIndex]
                    val charges = sceptre?.getAttr(ItemAttribute.CHARGES) ?: 0
                    player.message("Your sceptre has $charges revenant ether charges remaining.")
                }
            }
            
            onItemOption("item.$variant", "uncharge") {
                handleSceptreUncharge(player, variant)
            }
        }
    }
    
    private fun handleSceptreUncharge(player: Player, chargedVariant: String) {
        val sceptreChargedId = getRSCM("item.$chargedVariant")
        val sceptreUnchargedId = getUnchargedVariant(sceptreChargedId)
        
        player.queue {
            chatPlayer(player, "Are you sure you want to uncharge the sceptre?")
            chatPlayer(player, "This will destroy all charges and cannot be undone.")
            
            when (options(player, "Yes, uncharge it.", "No, keep it charged.")) {
                1 -> {
                    // Check inventory first
                    val inventoryIndex = player.inventory.getItemIndex(sceptreChargedId, false)
                    if (inventoryIndex != -1) {
                        player.inventory.remove(sceptreChargedId, 1)
                        player.inventory.add(sceptreUnchargedId, 1)
                        player.message("You uncharge the sceptre. All charges have been lost.")
                        return@queue
                    }
                    
                    // Check if equipped
                    val equipped = player.getEquipment(EquipmentType.WEAPON)
                    if (equipped?.id == sceptreChargedId) {
                        player.equipment[EquipmentType.WEAPON.id] = null
                        val addResult = player.inventory.add(sceptreUnchargedId, 1)
                        if (addResult.hasSucceeded()) {
                            player.message("You uncharge the sceptre. All charges have been lost.")
                        } else {
                            // Put it back if we can't add to inventory
                            player.equipment[EquipmentType.WEAPON.id] = equipped
                            player.message("You don't have enough inventory space.")
                        }
                    } else {
                        player.message("You need to have the sceptre in your inventory or equipped.")
                    }
                }
                2 -> {
                    player.message("You decide to keep the sceptre charged.")
                }
            }
        }
    }
    
    private fun registerCombatLogic() {
        SCEPTRE_VARIANTS.forEach { variant ->
            setItemCombatLogic("item.$variant") {
                handleSceptreCombat(player)
            }
        }
    }
    
    private fun handleSceptreCombat(player: Player) {
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return
        
        // Check if sceptre is charged (has charges attribute)
        val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (charges > 0) {
            // Consume 1 ether per attack
            val newCharges = charges - 1
            if (newCharges <= 0) {
                // Convert to uncharged variant
                val unchargedId = getUnchargedVariant(weapon.id)
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    // If we can't add to inventory, drop it
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your sceptre has run out of charges.")
            } else {
                // Update charges
                weapon.putAttr(ItemAttribute.CHARGES, newCharges)
                PlayerInfo(player).syncAppearance()
            }
        } else {
            // Uncharged sceptre - can still attack but no bonuses
            // Normal combat continues
        }
    }
    
    private fun registerDeathHandler() {
        onPlayerDeath {
            val player = this.player
            
            // Check if player is in wilderness
            if (player.tile.getWildernessLevel() <= 0) {
                return@onPlayerDeath
            }
            
            // Check if player has a charged sceptre equipped
            val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return@onPlayerDeath
            if (!isSceptreVariant(weapon.id)) {
                return@onPlayerDeath
            }
            
            val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
            if (charges > 0) {
                // Convert to uncharged variant on death in wilderness
                val unchargedId = getUnchargedVariant(weapon.id)
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your sceptre lost all charges upon death in the wilderness.")
            }
        }
    }
}

