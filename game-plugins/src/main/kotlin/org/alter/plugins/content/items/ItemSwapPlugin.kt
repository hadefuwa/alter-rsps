package org.alter.plugins.content.items

import dev.openrune.cache.CacheManager.getItem
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
            
            // Toxic Staff (Uncharged) -> Toxic Staff of the Dead
            "item.toxic_staff_uncharged" to "item.toxic_staff_of_the_dead",
            
            // Blade of Saeldor (Corrupted 24553) -> Blade of Saeldor (24551)
            "item.blade_of_saeldor_c_24553" to "item.blade_of_saeldor_c",
            
            // Add more item swaps here as needed:
            // "item.useless_item_1" to "item.useful_item_1",
            // "item.useless_item_2" to "item.useful_item_2",
            // etc...
        )
        
        /**
         * Special swaps that convert one item into multiple items.
         * Format: source item to list of destination items
         */
        private val MULTI_ITEM_SWAPS = mapOf(
            // Dwarf Cannon Set -> 4 individual cannon pieces
            "item.dwarf_cannon_set" to listOf(
                "item.cannon_barrels",  // 1. Cannon barrels
                "item.cannon_base",     // 2. Cannon base
                "item.cannon_furnace",  // 3. Cannon furnace
                "item.cannon_stand"     // 4. Cannon stand
            )
        )
    }

    init {
        // Register handlers for each item swap
        ITEM_SWAPS.forEach { (sourceItem, destinationItem) ->
            try {
                val sourceItemId = getRSCM(sourceItem)
                val destinationItemId = getRSCM(destinationItem)
                
                // Track which options were successfully registered to avoid duplicates
                var registeredOption: Int? = null
                
                // Try to register option 1 (usually "Use" or first option)
                if (!world.plugins.isItemBound(sourceItemId, 1)) {
                    try {
                        onItemOption(item = sourceItem, option = 1) {
                            player.queue {
                                swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                            }
                        }
                        registeredOption = 1
                    } catch (e: Exception) {
                        // Option 1 might not exist, continue
                    }
                }
                
                // Also try option 2 as backup (only if option 1 wasn't registered)
                if (registeredOption == null && !world.plugins.isItemBound(sourceItemId, 2)) {
                    try {
                        onItemOption(item = sourceItem, option = 2) {
                            player.queue {
                                swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                            }
                        }
                        registeredOption = 2
                    } catch (e: Exception) {
                        // Option 2 might not exist, continue
                    }
                }
                
                // Try option 3 (often "Wield" for weapons) (only if no option registered yet)
                if (registeredOption == null && !world.plugins.isItemBound(sourceItemId, 3)) {
                    try {
                        onItemOption(item = sourceItem, option = 3) {
                            player.queue {
                                swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                            }
                        }
                        registeredOption = 3
                    } catch (e: Exception) {
                        // Option 3 might not exist, continue
                    }
                }
                
                // Try string options as well (only if no numeric option was registered)
                // This prevents duplicate registration if a string option maps to an already-registered numeric option
                if (registeredOption == null) {
                    val optionNames = listOf("use", "Use", "activate", "Activate", "convert", "Convert", "transform", "Transform", "wield", "Wield")
                    for (optionName in optionNames) {
                        if (itemHasInventoryOption(sourceItem, optionName)) {
                            try {
                                // Check what option index this string maps to before registering
                                val itemDef = getItem(sourceItemId)
                                val optionIndex = itemDef.interfaceOptions.indexOfFirst { it?.lowercase() == optionName.lowercase() }
                                if (optionIndex != -1 && !world.plugins.isItemBound(sourceItemId, optionIndex + 1)) {
                                    onItemOption(item = sourceItem, option = optionName) {
                                        player.queue {
                                            swapItem(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                                        }
                                    }
                                    registeredOption = optionIndex + 1
                                    break // Successfully registered, no need to try more options
                                }
                            } catch (e: Exception) {
                                // Continue to next option
                            }
                        }
                    }
                }
                
                // Also register equip requirement handler to convert item when trying to equip/wield
                // This intercepts the equip action and converts the item before equipping
                try {
                    r.bindEquipItemRequirement(sourceItemId) {
                        // Convert the item and equip it
                        player.queue {
                            swapItemAndEquip(player, sourceItem, sourceItemId, destinationItem, destinationItemId)
                        }
                        // Return false to prevent the original item from being equipped
                        // The swapItemAndEquip function will handle equipping the converted item
                        false
                    }
                    println("ItemSwapPlugin: Registered equip requirement handler for $sourceItem -> $destinationItem")
                } catch (e: Exception) {
                    // Equip requirement might already be bound, that's okay
                    println("ItemSwapPlugin: Could not register equip requirement for $sourceItem: ${e.message}")
                }
                
                println("ItemSwapPlugin: Registered swap handler for $sourceItem -> $destinationItem")
            } catch (e: Exception) {
                println("ItemSwapPlugin: Warning - Could not register swap for $sourceItem -> $destinationItem: ${e.message}")
            }
        }
        
        // Register handlers for multi-item swaps (1 item -> multiple items)
        MULTI_ITEM_SWAPS.forEach { (sourceItem, destinationItems) ->
            try {
                val sourceItemId = getRSCM(sourceItem)
                val destinationItemIds = destinationItems.map { getRSCM(it) }
                val itemDef = getItem(sourceItemId)
                
                println("ItemSwapPlugin: Attempting to register multi-item swap for $sourceItem (ID: $sourceItemId)")
                println("ItemSwapPlugin: Available options: ${itemDef.interfaceOptions.filterNotNull()}")
                
                // Track which options were successfully registered to avoid duplicates
                var registeredOption: Int? = null
                
                // CRITICAL: Try option 2 first (this is what inventory clicks send)
                if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                    if (!world.plugins.isItemBound(sourceItemId, 2)) {
                        try {
                            r.bindItem(sourceItemId, 2) {
                                player.queue {
                                    swapItemToMultiple(player, sourceItem, sourceItemId, destinationItems, destinationItemIds)
                                }
                            }
                            registeredOption = 2
                            println("ItemSwapPlugin: Successfully registered option 2 for $sourceItem")
                        } catch (e: IllegalStateException) {
                            println("ItemSwapPlugin: Option 2 already bound for $sourceItem")
                        } catch (e: Exception) {
                            println("ItemSwapPlugin: Failed to register option 2 for $sourceItem: ${e.message}")
                        }
                    } else {
                        println("ItemSwapPlugin: Option 2 already bound for $sourceItem")
                    }
                }
                
                // Try option 1 if option 2 wasn't registered
                if (registeredOption == null && itemDef.interfaceOptions.size >= 1 && itemDef.interfaceOptions[0] != null) {
                    if (!world.plugins.isItemBound(sourceItemId, 1)) {
                        try {
                            r.bindItem(sourceItemId, 1) {
                                player.queue {
                                    swapItemToMultiple(player, sourceItem, sourceItemId, destinationItems, destinationItemIds)
                                }
                            }
                            registeredOption = 1
                            println("ItemSwapPlugin: Successfully registered option 1 for $sourceItem")
                        } catch (e: IllegalStateException) {
                            println("ItemSwapPlugin: Option 1 already bound for $sourceItem")
                        } catch (e: Exception) {
                            println("ItemSwapPlugin: Failed to register option 1 for $sourceItem: ${e.message}")
                        }
                    }
                }
                
                // Try option 3 if still not registered
                if (registeredOption == null && itemDef.interfaceOptions.size >= 3 && itemDef.interfaceOptions[2] != null) {
                    if (!world.plugins.isItemBound(sourceItemId, 3)) {
                        try {
                            r.bindItem(sourceItemId, 3) {
                                player.queue {
                                    swapItemToMultiple(player, sourceItem, sourceItemId, destinationItems, destinationItemIds)
                                }
                            }
                            registeredOption = 3
                            println("ItemSwapPlugin: Successfully registered option 3 for $sourceItem")
                        } catch (e: IllegalStateException) {
                            println("ItemSwapPlugin: Option 3 already bound for $sourceItem")
                        } catch (e: Exception) {
                            println("ItemSwapPlugin: Failed to register option 3 for $sourceItem: ${e.message}")
                        }
                    }
                }
                
                // Try string options as well (only if no numeric option was registered)
                if (registeredOption == null) {
                    val optionNames = listOf("use", "Use", "activate", "Activate", "convert", "Convert", "transform", "Transform", "open", "Open")
                    for (optionName in optionNames) {
                        try {
                            val optionIndex = itemDef.interfaceOptions.indexOfFirst { it?.lowercase() == optionName.lowercase() }
                            if (optionIndex != -1 && !world.plugins.isItemBound(sourceItemId, optionIndex + 1)) {
                                r.bindItem(sourceItemId, optionIndex + 1) {
                                    player.queue {
                                        swapItemToMultiple(player, sourceItem, sourceItemId, destinationItems, destinationItemIds)
                                    }
                                }
                                registeredOption = optionIndex + 1
                                println("ItemSwapPlugin: Successfully registered '$optionName' (option ${optionIndex + 1}) for $sourceItem")
                                break
                            }
                        } catch (e: Exception) {
                            // Continue to next option
                        }
                    }
                }
                
                if (registeredOption != null) {
                    println("ItemSwapPlugin: Successfully registered multi-item swap handler for $sourceItem -> ${destinationItems.size} items (option $registeredOption)")
                } else {
                    println("ItemSwapPlugin: WARNING - Could not register any option for $sourceItem. All options may be bound.")
                }
            } catch (e: Exception) {
                println("ItemSwapPlugin: ERROR - Could not register multi-item swap for $sourceItem: ${e.message}")
                e.printStackTrace()
            }
        }
        
        println("ItemSwapPlugin: Initialized with ${ITEM_SWAPS.size} item swap(s) and ${MULTI_ITEM_SWAPS.size} multi-item swap(s)")
        
        // Track which items were successfully registered in ITEM_SWAPS to avoid duplicates
        val registeredItems = mutableSetOf<Int>()
        ITEM_SWAPS.forEach { (sourceItem, _) ->
            try {
                val sourceItemId = getRSCM(sourceItem)
                registeredItems.add(sourceItemId)
            } catch (e: Exception) {
                // Item might not exist in RSCM, skip
            }
        }
        
        // Direct ID-based swap for Blade of Saeldor (24553 -> 24551) as fallback
        // This handles the case where RSCM name lookup might fail
        // Only register if it wasn't already registered in ITEM_SWAPS
        try {
            val bladeOfSaeldorCorruptedId = 24553
            val bladeOfSaeldorId = 24551
            
            // Skip if already registered in ITEM_SWAPS
            if (registeredItems.contains(bladeOfSaeldorCorruptedId)) {
                println("ItemSwapPlugin: Skipping fallback registration for 24553 (already registered in ITEM_SWAPS)")
            } else {
            
            // Register option 1
            if (!world.plugins.isItemBound(bladeOfSaeldorCorruptedId, 1)) {
                try {
                    r.bindItem(bladeOfSaeldorCorruptedId, 1) {
                        player.queue {
                            swapItemById(player, bladeOfSaeldorCorruptedId, bladeOfSaeldorId)
                        }
                    }
                } catch (e: Exception) {
                    // Option 1 might not exist
                }
            }
            
            // Register option 2
            if (!world.plugins.isItemBound(bladeOfSaeldorCorruptedId, 2)) {
                try {
                    r.bindItem(bladeOfSaeldorCorruptedId, 2) {
                        player.queue {
                            swapItemById(player, bladeOfSaeldorCorruptedId, bladeOfSaeldorId)
                        }
                    }
                } catch (e: Exception) {
                    // Option 2 might not exist
                }
            }
            
            // Register option 3 (wield)
            if (!world.plugins.isItemBound(bladeOfSaeldorCorruptedId, 3)) {
                try {
                    r.bindItem(bladeOfSaeldorCorruptedId, 3) {
                        player.queue {
                            swapItemAndEquipById(player, bladeOfSaeldorCorruptedId, bladeOfSaeldorId)
                        }
                    }
                } catch (e: Exception) {
                    // Option 3 might not exist
                }
            }
            
            // Register equip requirement handler (only if not already bound)
            try {
                r.bindEquipItemRequirement(bladeOfSaeldorCorruptedId) {
                    player.queue {
                        swapItemAndEquipById(player, bladeOfSaeldorCorruptedId, bladeOfSaeldorId)
                    }
                    false
                }
                println("ItemSwapPlugin: Registered direct ID swap handler for 24553 -> 24551")
            } catch (e: Exception) {
                println("ItemSwapPlugin: Could not register equip requirement for 24553: ${e.message}")
            }
            }
        } catch (e: Exception) {
            println("ItemSwapPlugin: Could not register direct ID swap for 24553 -> 24551: ${e.message}")
        }
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
    
    /**
     * Swaps the source item for the destination item and then equips it.
     * Used when player tries to wield/equip the uncharged item.
     */
    private suspend fun swapItemAndEquip(
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
            
            // Find the newly added item and equip it
            val newItemSlot = player.inventory.getItemIndex(destinationItemId, false)
            if (newItemSlot != -1) {
                val newItem = player.inventory[newItemSlot]
                if (newItem != null) {
                    val equipResult = org.alter.game.action.EquipAction.equip(player, newItem, newItemSlot)
                    if (equipResult != org.alter.game.action.EquipAction.Result.SUCCESS) {
                        player.message("The item was converted but could not be equipped.")
                    }
                }
            }
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
    
    /**
     * Swaps items by direct ID (used as fallback when RSCM names might not work).
     */
    private suspend fun swapItemById(
        player: Player,
        sourceItemId: Int,
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
        val addResult = player.inventory.add(item = destinationItemId, amount = 1)
        
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
    
    /**
     * Swaps items by direct ID and then equips the destination item.
     */
    private suspend fun swapItemAndEquipById(
        player: Player,
        sourceItemId: Int,
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
        val addResult = player.inventory.add(item = destinationItemId, amount = 1)
        
        if (addResult.hasSucceeded()) {
            player.message("The item transforms!")
            
            // Find the newly added item and equip it
            val newItemSlot = player.inventory.getItemIndex(destinationItemId, false)
            if (newItemSlot != -1) {
                val newItem = player.inventory[newItemSlot]
                if (newItem != null) {
                    val equipResult = org.alter.game.action.EquipAction.equip(player, newItem, newItemSlot)
                    if (equipResult != org.alter.game.action.EquipAction.Result.SUCCESS) {
                        player.message("The item was converted but could not be equipped.")
                    }
                }
            }
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
    
    /**
     * Swaps one source item for multiple destination items.
     * Used for items like cannon set that break down into multiple pieces.
     */
    private suspend fun swapItemToMultiple(
        player: Player,
        sourceItemName: String,
        sourceItemId: Int,
        destinationItemNames: List<String>,
        destinationItemIds: List<Int>
    ) {
        // Find the source item in inventory
        val slot = player.inventory.getItemIndex(sourceItemId, false)
        
        if (slot == -1) {
            player.message("You don't have that item.")
            return
        }
        
        // Check inventory space - need (destinationItems.size - 1) free slots
        // (we're removing 1 item and adding destinationItems.size items)
        val freeSlots = player.inventory.freeSlotCount
        val requiredSlots = destinationItemIds.size - 1
        if (freeSlots < requiredSlots) {
            player.message("You need at least $requiredSlots free inventory space${if (requiredSlots > 1) "s" else ""} to break down this item.")
            return
        }
        
        // Remove the source item
        val removeResult = player.inventory.remove(item = sourceItemId, amount = 1, beginSlot = slot, assureFullRemoval = true)
        
        if (!removeResult.hasSucceeded()) {
            player.message("Failed to remove the item.")
            return
        }
        
        // Add all destination items directly
        player.inventory.add(item = destinationItemIds[0], amount = 1) // Cannon barrels
        player.inventory.add(item = destinationItemIds[1], amount = 1) // Cannon base
        player.inventory.add(item = destinationItemIds[2], amount = 1) // Cannon furnace
        player.inventory.add(item = destinationItemIds[3], amount = 1) // Cannon stand
        
        player.message("You break down the cannon set into its individual pieces.")
    }
}


