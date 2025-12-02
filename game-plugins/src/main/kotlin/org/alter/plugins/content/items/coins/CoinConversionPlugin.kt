package org.alter.plugins.content.items.coins

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
import org.alter.rscm.RSCM.getRSCM

/**
 * Plugin to remove non-stackable coins (item 617) from the game.
 * 
 * This plugin:
 * 1. Converts item 617 to 995 (stackable coins) when picked up from ground
 * 2. Converts item 617 to 995 in inventory and bank on login
 * 
 * @author Auto-generated
 */
class CoinConversionPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        const val NON_STACKABLE_COINS = 617
        const val STACKABLE_COINS = 995
    }

    init {
        /**
         * Convert item 617 to 995 when picked up from ground
         */
        onGlobalItemPickup {
            val inventoryTransaction = player.attr[GROUNDITEM_PICKUP_TRANSACTION]?.get() ?: return@onGlobalItemPickup
            
            // Check if any items in the transaction are non-stackable coins (617)
            inventoryTransaction.items.forEach { slotItem ->
                if (slotItem.item.id == NON_STACKABLE_COINS) {
                    val amount = slotItem.item.amount
                    val slot = slotItem.slot
                    
                    // Remove the non-stackable coins
                    player.inventory.remove(item = NON_STACKABLE_COINS, amount = amount, beginSlot = slot)
                    
                    // Add stackable coins instead
                    player.inventory.add(item = STACKABLE_COINS, amount = amount)
                    
                    player.message("Your non-stackable coins have been converted to stackable coins.")
                }
            }
        }

        /**
         * Convert item 617 to 995 in inventory and bank on login
         */
        onLogin {
            var convertedInventory = 0
            var convertedBank = 0
            
            // Convert in inventory
            val inventoryCount = player.inventory.getItemCount(NON_STACKABLE_COINS)
            if (inventoryCount > 0) {
                player.inventory.remove(item = NON_STACKABLE_COINS, amount = inventoryCount, assureFullRemoval = true)
                player.inventory.add(item = STACKABLE_COINS, amount = inventoryCount)
                convertedInventory = inventoryCount
            }
            
            // Convert in bank
            val bankCount = player.bank.getItemCount(NON_STACKABLE_COINS)
            if (bankCount > 0) {
                player.bank.remove(item = NON_STACKABLE_COINS, amount = bankCount, assureFullRemoval = true)
                player.bank.add(item = STACKABLE_COINS, amount = bankCount)
                convertedBank = bankCount
            }
            
            // Notify player if any conversions occurred
            if (convertedInventory > 0 || convertedBank > 0) {
                val total = convertedInventory + convertedBank
                player.message("Converted $total non-stackable coin${if (total > 1) "s" else ""} to stackable coins.")
            }
        }
    }
}

