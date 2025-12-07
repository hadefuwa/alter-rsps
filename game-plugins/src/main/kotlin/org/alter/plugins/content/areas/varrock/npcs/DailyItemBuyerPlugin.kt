package org.alter.plugins.content.areas.varrock.npcs

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.NpcInfo
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import java.util.*

/**
 * Daily Item Buyer NPC
 * 
 * An NPC that spawns at tile 3206 3415 and buys 1 random item per day from players for 100m.
 * Each player can only sell once per day.
 */
class DailyItemBuyerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val npcId = "npc.shop_keeper_2825" // Using shop keeper NPC
    private val npcTile = Tile(x = 3206, z = 3415, height = 0)
    private val buyPrice = 100_000_000L // 100 million coins
    private val npcName = "Daily Collector"
    
    // World attribute keys for storing daily item and day
    private val DAILY_ITEM_ATTR_KEY = AttributeKey<Int>("daily_item_buyer_item")
    private val DAILY_DAY_ATTR_KEY = AttributeKey<Long>("daily_item_buyer_day")
    
    // Player attribute key for tracking if they've sold today
    private val PLAYER_SOLD_TODAY_ATTR_KEY = AttributeKey<Long>("daily_item_buyer_sold_day")
    
    // List of valid item IDs to choose from (you can expand this list)
    // Using a reasonable range of item IDs - adjust based on your server's item database
    // Note: We use a smaller range for handler binding to avoid performance issues
    private val validItemIds = (1..10000).toList() // Adjust range as needed
    
    init {
        // Spawn the NPC
        spawnNpc(npcId, 3206, 3415, 0, 0, Direction.SOUTH)
        
        // Set custom name for the NPC when it spawns
        onNpcSpawn(npcId) {
            val npc = ctx as org.alter.game.model.entity.Npc
            if (npc.tile == npcTile) {
                NpcInfo(npc).setTempName(npcName)
            }
        }
        
        // Handle talk-to interaction
        onNpcOption(npcId, option = "talk-to") {
            val npc = player.getInteractingNpc()
            if (npc.tile == npcTile) {
                player.queue { dialog(player) }
            }
        }
        
        // Bind global item-on-NPC handlers for items
        // This allows any item to be used on any NPC, then we check if it's our target NPC
        // We'll bind for a reasonable range of items (1-10000)
        val itemRange = (1..10000) // Adjust range as needed
        
        itemRange.forEach { itemId ->
            try {
                // Verify item exists and is valid
                val itemDef = getItem(itemId)
                if (itemDef.name.isNotBlank() && itemDef.name.lowercase() != "null") {
                    // Bind global handler for this item
                    // When any item is used on any NPC, this handler will be called
                    // We then check if the NPC is our target NPC
                    onItemOnNpcGlobal(itemId) {
                        val npc = player.getInteractingNpc()
                        if (npc.tile == npcTile) {
                            handleItemSale(player, npc)
                        }
                    }
                }
            } catch (e: Exception) {
                // Item doesn't exist, skip
            }
        }
    }
    
    /**
     * Gets the current day number (days since epoch) for daily resets
     */
    private fun getCurrentDay(): Long {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000)
    }
    
    /**
     * Gets or generates today's random item
     */
    private fun getTodaysItem(): Int {
        val currentDay = getCurrentDay()
        val storedDay = world.attr.getOrDefault(DAILY_DAY_ATTR_KEY, -1L) as Long
        val storedItem = world.attr.getOrDefault(DAILY_ITEM_ATTR_KEY, -1) as Int
        
        // If we have a stored item for today, verify it's still valid and return it
        if (storedDay == currentDay && storedItem > 0) {
            if (isValidItem(storedItem)) {
                return storedItem
            }
        }
        
        // Generate a new random item for today
        // Try to find a valid item (with retries)
        var randomItemId = -1
        var attempts = 0
        val maxAttempts = 100
        
        while (randomItemId == -1 && attempts < maxAttempts) {
            val candidateId = validItemIds.random()
            if (isValidItem(candidateId)) {
                randomItemId = candidateId
            }
            attempts++
        }
        
        // Fallback: if we couldn't find a valid item, try a few more times with a larger range
        if (randomItemId == -1) {
            // Try a few more times with extended attempts
            var extendedAttempts = 0
            val maxExtendedAttempts = 200
            while (randomItemId == -1 && extendedAttempts < maxExtendedAttempts) {
                val candidateId = validItemIds.random()
                if (isValidItem(candidateId)) {
                    randomItemId = candidateId
                }
                extendedAttempts++
            }
        }
        
        // If still no valid item found, use a default item (like a common item)
        // This should rarely happen, but we need a fallback
        if (randomItemId == -1) {
            // Try some common item IDs as last resort
            val fallbackItems = listOf(4151, 4153, 1163, 1165, 1127) // Whip, dragon items, etc.
            for (fallbackId in fallbackItems) {
                if (isValidItem(fallbackId)) {
                    randomItemId = fallbackId
                    break
                }
            }
        }
        
        // Store the new item and day
        world.attr[DAILY_DAY_ATTR_KEY] = currentDay
        world.attr[DAILY_ITEM_ATTR_KEY] = randomItemId
        
        return randomItemId
    }
    
    /**
     * Checks if an item ID is valid (exists in cache and has a valid name)
     */
    private fun isValidItem(itemId: Int): Boolean {
        return try {
            val itemDef = getItem(itemId)
            val name = itemDef.name.trim()
            val nameLower = name.lowercase()
            
            // Exclude coins
            val coinsId = getRSCM("item.coins_995")
            if (itemId == coinsId || nameLower.contains("coin")) {
                return false
            }
            
            // Exclude placeholders, notes, and invalid items
            name.isNotBlank() && 
            nameLower != "null" &&
            !nameLower.contains("placeholder") &&
            !nameLower.contains("empty") &&
            itemDef.noteTemplateId == 0 // Exclude noted items
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if the player has already sold today
     */
    private fun hasPlayerSoldToday(player: Player): Boolean {
        val currentDay = getCurrentDay()
        val lastSoldDay = player.attr.getOrDefault(PLAYER_SOLD_TODAY_ATTR_KEY, -1L) as Long
        return lastSoldDay == currentDay
    }
    
    /**
     * Marks that the player has sold today
     */
    private fun markPlayerSoldToday(player: Player) {
        val currentDay = getCurrentDay()
        player.attr[PLAYER_SOLD_TODAY_ATTR_KEY] = currentDay
    }
    
    /**
     * Gets the item name from item ID
     */
    private fun getItemName(itemId: Int): String {
        return try {
            val itemDef = getItem(itemId)
            itemDef.name
        } catch (e: Exception) {
            "Item #$itemId"
        }
    }
    
    /**
     * Handles when a player uses an item on the NPC
     */
    private fun handleItemSale(player: Player, npc: org.alter.game.model.entity.Npc) {
        val item = player.getInteractingItem() ?: return
        val itemId = item.id
        val todaysItemId = getTodaysItem()
        
        // Check if player has already sold today
        if (hasPlayerSoldToday(player)) {
            player.message("I've already bought an item from you today. Come back tomorrow!")
            return
        }
        
        // Check if the item matches today's item
        if (itemId != todaysItemId) {
            val todaysItemName = getItemName(todaysItemId)
            player.message("I'm not looking for that today. I'm looking for: $todaysItemName")
            return
        }
        
        // Check if player has the item in inventory
        val itemSlot = player.inventory.getItemIndex(itemId, false)
        if (itemSlot == -1) {
            player.message("You don't have that item in your inventory.")
            return
        }
        
        // Remove the item from inventory
        val removeResult = player.inventory.remove(item = itemId, amount = 1, assureFullRemoval = true)
        if (removeResult.hasFailed()) {
            player.message("Failed to remove the item.")
            return
        }
        
        // Add coins to inventory
        val coinsId = getRSCM("item.coins_995")
        val addResult = player.inventory.add(item = coinsId, amount = buyPrice.toInt(), assureFullInsertion = false)
        
        if (addResult.completed == 0) {
            // Inventory full, refund the item
            player.inventory.add(item = itemId, amount = 1, assureFullInsertion = false)
            player.message("You don't have enough inventory space for the coins!")
            return
        }
        
        // Mark that player has sold today
        markPlayerSoldToday(player)
        
        // Success message
        val itemName = getItemName(itemId)
        player.message("Thank you! I've paid you ${buyPrice / 1_000_000} million coins for the $itemName.")
        
        // If not all coins could fit, drop the rest
        if (addResult.getLeftOver() > 0) {
            val leftoverCoins = addResult.getLeftOver()
            val groundItem = org.alter.game.model.entity.GroundItem(
                item = coinsId,
                amount = leftoverCoins,
                tile = player.tile,
                owner = player
            )
            groundItem.timeUntilPublic = 100 // 1 minute
            groundItem.timeUntilDespawn = 400 // 4 minutes
            groundItem.ownerShipType = 1
            world.spawn(groundItem)
            player.message("Some coins were dropped on the ground as you don't have enough space.")
        }
    }
    
    /**
     * Dialog when talking to the NPC
     */
    suspend fun QueueTask.dialog(player: Player) {
        val todaysItemId = getTodaysItem()
        val todaysItemName = getItemName(todaysItemId)
        val hasSold = hasPlayerSoldToday(player)
        
        chatNpc(player, "Hello! I'm looking for a specific item today.")
        
        if (hasSold) {
            chatNpc(player, "I've already bought an item from you today. Come back tomorrow!")
        } else {
            chatNpc(player, "Today I'm looking for: $todaysItemName")
            chatNpc(player, "I'll pay you ${buyPrice / 1_000_000} million coins for it!")
            chatNpc(player, "Just use the item on me when you have it, or use the 'Sell Item' option.")
        }
    }
    
    /**
     * Alternative dialog for selling item via NPC option
     */
    suspend fun QueueTask.sellItemDialog(player: Player) {
        val todaysItemId = getTodaysItem()
        val todaysItemName = getItemName(todaysItemId)
        val hasSold = hasPlayerSoldToday(player)
        
        if (hasSold) {
            chatNpc(player, "I've already bought an item from you today. Come back tomorrow!")
            return
        }
        
        chatNpc(player, "I'm looking for: $todaysItemName")
        chatNpc(player, "Do you have it?")
        
        when (options(player, "Yes, here it is.", "No, not yet.")) {
            1 -> {
                // Check if player has the item
                val itemSlot = player.inventory.getItemIndex(todaysItemId, false)
                if (itemSlot == -1) {
                    chatPlayer(player, "I don't have that item.")
                } else {
                    // Process the sale
                    val npc = player.getInteractingNpc()
                    handleItemSale(player, npc)
                }
            }
            2 -> chatPlayer(player, "I'll come back when I have it.")
        }
    }
}

