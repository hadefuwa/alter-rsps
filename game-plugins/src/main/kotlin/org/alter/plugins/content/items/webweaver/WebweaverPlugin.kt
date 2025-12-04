package org.alter.plugins.content.items.webweaver

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
import org.alter.plugins.content.combat.CombatConfigs
import org.alter.plugins.content.combat.getCombatTarget

/**
 * Webweaver Plugin
 * 
 * Features:
 * - Charging system with revenant ether (1 ether = 1 charge, max 16,000 charges)
 * - Consumes 1 ether per attack when charged
 * - Right-click options to check/uncharge the webweaver bow
 * - Item interactions for adding charges
 * - Charges lost on death in wilderness
 * 
 * Supports:
 * - Webweaver Bow (uncharged: webweaver_bow_u, charged: webweaver_bow)
 */
class WebweaverPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Charging constants
        const val MAX_CHARGES = 16000 // Maximum charges for webweaver bow
        
        // Webweaver variants
        private val WEBWEAVER_VARIANTS = listOf(
            "webweaver_bow",
            "webweaver_bow_u"
        )
        
        // Helper methods for webweaver variants
        fun isWebweaverVariant(itemId: Int): Boolean {
            return WEBWEAVER_VARIANTS.any { getRSCM("item.$it") == itemId }
        }
        
        fun getChargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.webweaver_bow_u") -> getRSCM("item.webweaver_bow")
                else -> itemId // Already charged
            }
        }
        
        fun getUnchargedVariant(itemId: Int): Int {
            return when(itemId) {
                getRSCM("item.webweaver_bow") -> getRSCM("item.webweaver_bow_u")
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
        // Handle charging uncharged webweaver bow
        onItemOnItem("item.revenant_ether", "item.webweaver_bow_u") {
            handleWebweaverCharging(player)
        }
        
        // Handle adding charges to already charged webweaver bow
        onItemOnItem("item.revenant_ether", "item.webweaver_bow") {
            handleWebweaverAddCharges(player)
        }
    }
    
    private fun handleWebweaverCharging(player: Player) {
        val etherId = getRSCM("item.revenant_ether")
        val webweaverUnchargedId = getRSCM("item.webweaver_bow_u")
        val webweaverChargedId = getRSCM("item.webweaver_bow")
        
        // Check if player has revenant ether
        val etherCount = player.inventory.getItemCount(etherId)
        if (etherCount < 1) {
            player.message("You need revenant ether to charge the webweaver bow.")
            return
        }
        
        // Find the uncharged webweaver bow
        if (player.inventory.getItemCount(webweaverUnchargedId) < 1) {
            player.message("You need an uncharged webweaver bow.")
            return
        }
        
        // Calculate how much ether we can add (up to max charges)
        val etherToUse = etherCount.coerceAtMost(MAX_CHARGES)
        
        // Remove items
        player.inventory.remove(etherId, etherToUse)
        player.inventory.remove(webweaverUnchargedId, 1)
        
        // Add charged webweaver bow (forceNoStack to ensure it doesn't stack, since it will have attributes)
        val addResult = player.inventory.add(webweaverChargedId, 1, forceNoStack = true)
        if (addResult.hasSucceeded()) {
            // Find the item we just added and set charges
            // Items with attributes don't stack, so we should find it
            val webweaverIndex = player.inventory.getItemIndex(webweaverChargedId, false)
            if (webweaverIndex != -1) {
                val newItem = player.inventory[webweaverIndex]
                newItem?.putAttr(ItemAttribute.CHARGES, etherToUse)
            }
            
            player.animate(832) // Charging animation
            player.graphic(363) // Charging graphic
            player.message("You charge the webweaver bow with $etherToUse revenant ether.")
        } else {
            // Refund ether if we couldn't add the item
            player.inventory.add(etherId, etherToUse)
            player.message("Could not add charged webweaver bow to inventory.")
        }
    }
    
    private fun handleWebweaverAddCharges(player: Player) {
        val etherId = getRSCM("item.revenant_ether")
        val webweaverChargedId = getRSCM("item.webweaver_bow")
        
        // Check if player has a charged webweaver bow
        val webweaverIndex = player.inventory.getItemIndex(webweaverChargedId, false)
        if (webweaverIndex == -1) {
            player.message("You need a charged webweaver bow.")
            return
        }
        
        val webweaver = player.inventory[webweaverIndex] ?: return
        val currentCharges = webweaver.getAttr(ItemAttribute.CHARGES) ?: 0
        
        if (currentCharges >= MAX_CHARGES) {
            player.message("Your webweaver bow is already fully charged.")
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
        webweaver.putAttr(ItemAttribute.CHARGES, newCharges)
        
        player.animate(832) // Charging animation
        player.graphic(363) // Charging graphic
        player.message("You add $etherToUse revenant ether to the webweaver bow. It now has $newCharges charges.")
    }
    
    private fun registerRightClickOptions() {
        // Register check option for inventory
        onItemOption("item.webweaver_bow", "check") {
            val webweaverIndex = player.inventory.getItemIndex(getRSCM("item.webweaver_bow"), false)
            if (webweaverIndex != -1) {
                val webweaver = player.inventory[webweaverIndex]
                val charges = webweaver?.getAttr(ItemAttribute.CHARGES) ?: 0
                player.message("Your webweaver bow has $charges revenant ether charges remaining.")
            } else {
                player.message("You need to have the webweaver bow in your inventory or equipped.")
            }
        }
        
        // Register check option for equipped webweaver bow
        onEquipmentOption("item.webweaver_bow", "check") {
            val equipped = player.getEquipment(EquipmentType.WEAPON)
            if (equipped?.id == getRSCM("item.webweaver_bow")) {
                val charges = equipped.getAttr(ItemAttribute.CHARGES) ?: 0
                player.message("Your webweaver bow has $charges revenant ether charges remaining.")
            }
        }
        
        // Register uncharge option for inventory (not available for equipped items)
        onItemOption("item.webweaver_bow", "uncharge") {
            handleWebweaverUncharge(player)
        }
    }
    
    private fun handleWebweaverUncharge(player: Player) {
        val webweaverChargedId = getRSCM("item.webweaver_bow")
        val webweaverUnchargedId = getRSCM("item.webweaver_bow_u")
        
        player.queue {
            chatPlayer(player, "Are you sure you want to uncharge the webweaver bow?")
            chatPlayer(player, "This will destroy all charges and cannot be undone.")
            
            when (options(player, "Yes, uncharge it.", "No, keep it charged.")) {
                1 -> {
                    // Check inventory first
                    val inventoryIndex = player.inventory.getItemIndex(webweaverChargedId, false)
                    if (inventoryIndex != -1) {
                        player.inventory.remove(webweaverChargedId, 1)
                        player.inventory.add(webweaverUnchargedId, 1)
                        player.message("You uncharge the webweaver bow. All charges have been lost.")
                        return@queue
                    }
                    
                    // Check if equipped
                    val equipped = player.getEquipment(EquipmentType.WEAPON)
                    if (equipped?.id == webweaverChargedId) {
                        player.equipment[EquipmentType.WEAPON.id] = null
                        val addResult = player.inventory.add(webweaverUnchargedId, 1)
                        if (addResult.hasSucceeded()) {
                            player.message("You uncharge the webweaver bow. All charges have been lost.")
                        } else {
                            // Put it back if we can't add to inventory
                            player.equipment[EquipmentType.WEAPON.id] = equipped
                            player.message("You don't have enough inventory space.")
                        }
                    } else {
                        player.message("You need to have the webweaver bow in your inventory or equipped.")
                    }
                }
                2 -> {
                    player.message("You decide to keep the webweaver bow charged.")
                }
            }
        }
    }
    
    private fun registerCombatLogic() {
        WEBWEAVER_VARIANTS.forEach { variant ->
            setItemCombatLogic("item.$variant") {
                handleWebweaverCombat(player)
            }
        }
    }
    
    private fun handleWebweaverCombat(player: Player) {
        val target = player.getCombatTarget() ?: return
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return
        
        // Check if webweaver bow is charged (has charges attribute)
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
                player.message("Your webweaver bow has run out of charges.")
            } else {
                // Update charges
                weapon.putAttr(ItemAttribute.CHARGES, newCharges)
                PlayerInfo(player).syncAppearance()
            }
        } else {
            // Uncharged webweaver bow - can still attack but no bonuses
            // Normal combat continues
        }

        // Execute standard combat logic
        val strategy = CombatConfigs.getCombatStrategy(player)
        strategy.attack(player, target)
    }
    
    private fun registerDeathHandler() {
        onPlayerDeath {
            val player = this.player
            
            // Check if player is in wilderness
            if (player.tile.getWildernessLevel() <= 0) {
                return@onPlayerDeath
            }
            
            // Check if player has a charged webweaver bow equipped
            val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return@onPlayerDeath
            if (!isWebweaverVariant(weapon.id)) {
                return@onPlayerDeath
            }
            
            val charges = weapon.getAttr(ItemAttribute.CHARGES) ?: 0
            if (charges > 0) {
                // Convert to uncharged variant on death in wilderness
                val unchargedId = getRSCM("item.webweaver_bow_u")
                player.equipment[EquipmentType.WEAPON.id] = null
                val addResult = player.inventory.add(unchargedId, 1)
                if (!addResult.hasSucceeded()) {
                    world.spawn(GroundItem(unchargedId, 1, player.tile, player))
                }
                player.message("Your webweaver bow lost all charges upon death in the wilderness.")
            }
        }
    }
}
