package org.alter.plugins.content.items

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Generic Item Swap Plugin
 * 
 * Handles clicking on items in inventory to swap them for other items.
 * This allows mapping thousands of useless items into more useful items.
 * 
 * To add new item swaps, simply add entries to the ITEM_SWAPS map below.
 * Format: "item.source_item_name" to "item.destination_item_name"
 */
class ItemSwapPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Map of source items to destination items.
         * When a player clicks on a source item, it will be swapped for the destination item.
         * 
         * Format: "item.source_item" to "item.destination_item"
         * 
         * Example: "item.demons_heart" to "item.imbued_zamorak_cape"
         */
        private val ITEM_SWAPS = mapOf(
            // Demon's Heart -> Imbued Zamorak Cape (Mage Arena 2 Cape)
            "item.demons_heart" to "item.imbued_zamorak_cape",
            
            // Add more item swaps here as needed:
            // "item.useless_item_1" to "item.useful_item_1",
            // "item.useless_item_2" to "item.useful_item_2",
            // etc...
        )
    }

    init {
        // Register handlers for each item swap
        ITEM_SWAPS.forEach { (sourceItem, destinationItem) ->
            try {
                val sourceItemId = getRSCM(sourceItem)
                val destinationItemId = getRSCM(destinationItem)
                
                // Try to register option 1 (usually "Use" or first option)
                if (!world.plugins.isItemBound(sourceItemId, 1)) {
                    try {
                        onItemOption(item = sourceItem, option = 1) {
                            player.queue {
                                swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                            }
                        }
                    } catch (e: Exception) {
                        // Option 1 might not exist, continue
                    }
                }
                
                // Also try option 2 as backup
                if (!world.plugins.isItemBound(sourceItemId, 2)) {
                    try {
                        onItemOption(item = sourceItem, option = 2) {
                            player.queue {
                                swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                            }
                        }
                    } catch (e: Exception) {
                        // Option 2 might not exist, continue
                    }
                }
                
                // Try string options as well
                val optionNames = listOf("use", "Use", "activate", "Activate", "convert", "Convert", "transform", "Transform")
                for (optionName in optionNames) {
                    if (itemHasInventoryOption(sourceItem, optionName)) {
                        try {
                            onItemOption(item = sourceItem, option = optionName) {
                                player.queue {
                                    swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                                }
                            }
                            break // Successfully registered, no need to try more options
                        } catch (e: Exception) {
                            // Continue to next option
                        }
                    }
                }
                
                println("ItemSwapPlugin: Registered swap handler for $sourceItem -> $destinationItem")
            } catch (e: Exception) {
                println("ItemSwapPlugin: Warning - Could not register swap for $sourceItem -> $destinationItem: ${e.message}")
            }
        }
        
        println("ItemSwapPlugin: Initialized with ${ITEM_SWAPS.size} item swap(s)")
    }

    /**
     * Swaps the source item for the destination item in the player's inventory.
     */
    private suspend fun swapItem(
        player: Player,
        sourceItemName: String,
        sourceItemId: Int,
        destinationItemName: String,
        destinationItemId: Int
    ) {
        // Find the source item in inventory
        val slot = player.inventory.getItemIndex(sourceItemId, false)
        
        if (slot == -1) {
            player.message("You don't have that item.")
            return
        }
        
        // Remove the source item
        val removeResult = player.inventory.remove(item = sourceItemId, amount = 1, beginSlot = slot)
        
        if (!removeResult.hasSucceeded()) {
            player.message("Failed to remove the item.")
            return
        }
        
        // Add the destination item
        val addResult = player.inventory.add(item = destinationItemName, amount = 1)
        
        if (addResult.hasSucceeded()) {
            player.message("The item transforms!")
        } else {
            // If inventory is full, drop on ground
            player.message("Your inventory is full. The item appears on the ground.")
            val groundItem = org.alter.game.model.entity.GroundItem(
                item = destinationItemId,
                amount = 1,
                tile = player.tile,
                owner = player
            )
            groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
            groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
            groundItem.ownerShipType = 1
            world.spawn(groundItem)
        }
    }
}

