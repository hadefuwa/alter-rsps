package org.alter.plugins.content.areas.varrock.npcs

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.NpcInfo
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.asRSCM
import org.alter.rscm.RSCM.getRSCM

/**
 * Broken Item Repair NPC Plugin
 * 
 * An NPC in Varrock that can repair any broken item by swapping it for its unbroken version.
 * The NPC automatically detects items with "_broken" in their RSCM key and swaps them.
 */
class BrokenItemRepairPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val repairNpc = "npc.bob_10619"
    private val repairNpcTile = Tile(x = 3229, z = 3436, height = 0)
    private val repairCostPerItem = 20_000_000 // 20 million coins per item
    private val coinsItemId = 995
    
    init {
        // Spawn repair NPC at the specified location
        spawnNpc(repairNpc, x = 3229, z = 3436, walkRadius = 2, direction = Direction.SOUTH)
        
        // Set custom name for the repair NPC when it spawns
        onNpcSpawn(repairNpc) {
            if (npc.tile == repairNpcTile) {
                NpcInfo(npc).setTempName("Item Repairer")
            }
        }
        
        // Set up NPC interaction - both talk-to and repair options
        onNpcOption(repairNpc, option = "talk-to") {
            val npc = player.getInteractingNpc()
            // Check if this is our repair NPC (within walk radius of spawn location)
            val distanceX = Math.abs(npc.tile.x - repairNpcTile.x)
            val distanceZ = Math.abs(npc.tile.z - repairNpcTile.z)
            if (distanceX <= 2 && distanceZ <= 2 && npc.tile.height == repairNpcTile.height) {
                player.queue { dialog(player) }
            }
        }
        
        // Add repair option handler (only if the NPC has this option)
        try {
            if (npcHasOption(repairNpc, "repair")) {
                onNpcOption(repairNpc, option = "repair") {
                    val npc = player.getInteractingNpc()
                    // Check if this is our repair NPC (within walk radius of spawn location)
                    val distanceX = Math.abs(npc.tile.x - repairNpcTile.x)
                    val distanceZ = Math.abs(npc.tile.z - repairNpcTile.z)
                    if (distanceX <= 2 && distanceZ <= 2 && npc.tile.height == repairNpcTile.height) {
                        player.queue { 
                            // Skip dialog and go straight to repair
                            val brokenItems = findBrokenItems(player)
                            if (brokenItems.isEmpty()) {
                                chatNpc(
                                    player,
                                    "I don't see any broken items in your inventory.<br>Come back when you have something to repair!",
                                    animation = 554
                                )
                            } else {
                                repairAllItems(player, brokenItems)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Repair option doesn't exist for this NPC, that's okay
            println("BrokenItemRepairPlugin: NPC doesn't have 'repair' option, using talk-to only")
        }
    }
    
    /**
     * Main dialog function that handles the repair process
     */
    private suspend fun QueueTask.dialog(player: Player) {
        chatNpc(
            player,
            "Hello! I can repair any broken items you have.<br>My services cost 20 million coins per item.",
            animation = 567
        )
        
        // Find all broken items in inventory
        val brokenItems = findBrokenItems(player)
        
        if (brokenItems.isEmpty()) {
            chatNpc(
                player,
                "I don't see any broken items in your inventory.<br>Come back when you have something to repair!",
                animation = 554
            )
            return
        }
        
        val totalCost = brokenItems.size * repairCostPerItem
        val playerCoins = player.inventory.getItemCount(coinsItemId)
        
        chatNpc(
            player,
            "I found ${brokenItems.size} broken item${if (brokenItems.size == 1) "" else "s"} in your inventory.<br>Total cost: ${formatCoins(totalCost)} coins.",
            animation = 554
        )
        
        if (playerCoins < totalCost) {
            chatNpc(
                player,
                "You only have ${formatCoins(playerCoins)} coins.<br>You need ${formatCoins(totalCost)} coins to repair all items.",
                animation = 554
            )
            return
        }
        
        // Show options for what to do
        when (options(player, "Repair all broken items", "Show me what you can repair", "Nevermind")) {
            1 -> repairAllItems(player, brokenItems)
            2 -> showRepairableItems(player, brokenItems)
            3 -> chatPlayer(player, "Maybe later.", animation = 588)
        }
    }
    
    /**
     * Formats coin amounts for display (e.g., 20000000 -> "20,000,000" or "20M")
     */
    private fun formatCoins(amount: Int): String {
        return when {
            amount >= 1_000_000_000 -> "${amount / 1_000_000_000}B"
            amount >= 1_000_000 -> "${amount / 1_000_000}M"
            amount >= 1_000 -> "${amount / 1_000}K"
            else -> amount.toString()
        }
    }
    
    /**
     * Finds all broken items in the player's inventory
     * Returns a list of pairs: (slot, itemId, itemName, unbrokenItemKey)
     */
    private fun findBrokenItems(player: Player): List<BrokenItemInfo> {
        val brokenItems = mutableListOf<BrokenItemInfo>()
        
        for (slot in 0 until player.inventory.capacity) {
            val item = player.inventory[slot] ?: continue
            val itemId = item.id
            
            try {
                // Try to get the RSCM key for this item
                val rscmKey = itemId.asRSCM("item")
                
                // Check if the RSCM key contains repair-related keywords
                val isRepairable = rscmKey.contains("_broken", ignoreCase = true) ||
                                   rscmKey.contains("_damaged", ignoreCase = true)
                
                if (isRepairable) {
                    // Try multiple patterns to find the unbroken version
                    val possibleKeys = mutableListOf<String>()
                    
                    // Pattern 1: Remove "_broken" from anywhere in the string
                    possibleKeys.add(rscmKey.replace("_broken", "", ignoreCase = true))
                    
                    // Pattern 2: Remove "_damaged" from anywhere in the string
                    possibleKeys.add(rscmKey.replace("_damaged", "", ignoreCase = true))
                    
                    // Pattern 3: Remove "_broken" followed by underscore and numbers (e.g., "_broken_20487")
                    possibleKeys.add(rscmKey.replace(Regex("_broken_\\d+", RegexOption.IGNORE_CASE), ""))
                    
                    // Pattern 4: Remove "_damaged" followed by underscore and numbers
                    possibleKeys.add(rscmKey.replace(Regex("_damaged_\\d+", RegexOption.IGNORE_CASE), ""))
                    
                    // Pattern 5: Remove just "_broken" at the end
                    possibleKeys.add(rscmKey.replace(Regex("_broken$", RegexOption.IGNORE_CASE), ""))
                    
                    // Pattern 6: Remove just "_damaged" at the end
                    possibleKeys.add(rscmKey.replace(Regex("_damaged$", RegexOption.IGNORE_CASE), ""))
                    
                    // Try each possible key until we find one that works
                    var foundUnbroken = false
                    for (unbrokenKey in possibleKeys.distinct()) {
                        if (unbrokenKey == rscmKey) continue // Skip if no change
                        
                        try {
                            val unbrokenItemId = getRSCM(unbrokenKey)
                            val itemDef = getItem(itemId)
                            val itemName = itemDef.name
                            
                            brokenItems.add(BrokenItemInfo(slot, itemId, itemName, unbrokenKey, unbrokenItemId))
                            foundUnbroken = true
                            break // Found a working unbroken version, no need to try others
                        } catch (e: Exception) {
                            // This pattern didn't work, try the next one
                        }
                    }
                    
                    // If none of the patterns worked, the item can't be repaired
                    // This is fine - not all broken items may have unbroken versions
                }
            } catch (e: Exception) {
                // Item doesn't have an RSCM entry or other error, skip it
                // This is fine - not all items need to be repairable
            }
        }
        
        return brokenItems
    }
    
    /**
     * Shows the player what items can be repaired
     */
    private suspend fun QueueTask.showRepairableItems(player: Player, brokenItems: List<BrokenItemInfo>) {
        val totalCost = brokenItems.size * repairCostPerItem
        val playerCoins = player.inventory.getItemCount(coinsItemId)
        
        chatNpc(
            player,
            "I can repair the following items in your inventory<br>(${formatCoins(repairCostPerItem)} coins each):",
            animation = 567
        )
        
        brokenItems.forEach { (_, _, itemName, _, _) ->
            player.message("• $itemName")
        }
        
        chatNpc(
            player,
            "Total cost: ${formatCoins(totalCost)} coins.<br>You have ${formatCoins(playerCoins)} coins.",
            animation = 554
        )
        
        if (playerCoins < totalCost) {
            chatNpc(
                player,
                "You don't have enough coins to repair all items.",
                animation = 554
            )
            return
        }
        
        when (options(player, "Repair all of them", "Nevermind")) {
            1 -> repairAllItems(player, brokenItems)
            2 -> chatPlayer(player, "Maybe later.", animation = 588)
        }
    }
    
    /**
     * Repairs all broken items in the player's inventory
     */
    private suspend fun QueueTask.repairAllItems(player: Player, brokenItems: List<BrokenItemInfo>) {
        val totalCost = brokenItems.size * repairCostPerItem
        val playerCoins = player.inventory.getItemCount(coinsItemId)
        
        // Check if player has enough coins
        if (playerCoins < totalCost) {
            chatNpc(
                player,
                "You don't have enough coins!<br>You need ${formatCoins(totalCost)} but only have ${formatCoins(playerCoins)}.",
                animation = 554
            )
            return
        }
        
        // Remove coins upfront
        val coinRemoveResult = player.inventory.remove(item = coinsItemId, amount = totalCost, assureFullRemoval = true)
        if (!coinRemoveResult.hasSucceeded() || coinRemoveResult.completed < totalCost) {
            chatNpc(
                player,
                "Failed to process payment. Please try again.",
                animation = 554
            )
            return
        }
        
        var repairedCount = 0
        var failedCount = 0
        
        // Process items in reverse order to avoid slot shifting issues
        val sortedItems = brokenItems.sortedByDescending { it.slot }
        
        for (itemInfo in sortedItems) {
            val (slot, brokenItemId, itemName, unbrokenKey, unbrokenItemId) = itemInfo
            
            // Verify the item is still in the expected slot
            val currentItem = player.inventory[slot]
            if (currentItem == null || currentItem.id != brokenItemId) {
                // Item moved or was already removed, skip it
                failedCount++
                continue
            }
            
            // Remove the broken item
            val removeResult = player.inventory.remove(item = brokenItemId, amount = 1, beginSlot = slot)
            
            if (!removeResult.hasSucceeded()) {
                failedCount++
                continue
            }
            
            // Add the unbroken item
            try {
                val addResult = player.inventory.add(item = unbrokenKey, amount = 1)
                
                if (addResult.hasSucceeded()) {
                    repairedCount++
                } else {
                    // If inventory is full, drop the unbroken item on ground
                    val groundItem = org.alter.game.model.entity.GroundItem(
                        item = unbrokenItemId,
                        amount = 1,
                        tile = player.tile,
                        owner = player
                    )
                    groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
                    groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
                    groundItem.ownerShipType = 1
                    world.spawn(groundItem)
                    repairedCount++
                    player.message("Your inventory was full, so $itemName was dropped on the ground.")
                }
            } catch (e: Exception) {
                // Failed to add unbroken item, put broken item back
                player.inventory.add(item = brokenItemId, amount = 1)
                failedCount++
            }
        }
        
        // Refund coins for failed repairs
        if (failedCount > 0) {
            val refundAmount = failedCount * repairCostPerItem
            player.inventory.add(item = coinsItemId, amount = refundAmount)
        }
        
        // Give feedback to the player
        if (repairedCount > 0) {
            val actualCost = repairedCount * repairCostPerItem
            chatNpc(
                player,
                "I've repaired $repairedCount item${if (repairedCount == 1) "" else "s"} for you!<br>Total cost: ${formatCoins(actualCost)} coins.",
                animation = 567
            )
        }
        
        if (failedCount > 0) {
            chatNpc(
                player,
                "I had trouble repairing $failedCount item${if (failedCount == 1) "" else "s"}.<br>I've refunded ${formatCoins(failedCount * repairCostPerItem)} coins.",
                animation = 554
            )
        }
        
        if (repairedCount == 0 && failedCount == 0) {
            // Refund all coins if nothing was repaired
            player.inventory.add(item = coinsItemId, amount = totalCost)
            chatNpc(
                player,
                "Hmm, it seems there were no items to repair.<br>I've refunded your coins.",
                animation = 554
            )
        }
    }
    
    /**
     * Data class to hold information about a broken item
     */
    private data class BrokenItemInfo(
        val slot: Int,
        val brokenItemId: Int,
        val itemName: String,
        val unbrokenKey: String,
        val unbrokenItemId: Int
    )
}

