package org.alter.plugins.content.objects.crates

import dev.openrune.cache.CacheManager.getItem
import dev.openrune.cache.CacheManager.itemSize
import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Plugin for handling bounty crate (28094).
 * Gives 5 random items from the game's item table on click.
 */
class BountyCratePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    /**
     * List of valid item IDs from the game's item table.
     * Built once when the plugin initializes.
     */
    private val validItemIds: List<Int> by lazy {
        buildValidItemList()
    }
    
    init {
        // Pre-build the valid item list
        val itemCount = validItemIds.size
        
        // Register item 28094 with option 2
        r.bindItem(28094, 2) {
            player.queue {
                openBountyCrate(this, player)
            }
        }
    }
    
    /**
     * Builds a list of valid item IDs from the game's item table.
     * Uses the same logic as the NPC drop system.
     */
    private fun buildValidItemList(): List<Int> {
        val validItems = mutableListOf<Int>()
        
        for (itemId in 0 until itemSize()) {
            try {
                val def = getItem(itemId)
                // Filter out invalid items: placeholders, null names, and empty names
                if (!def.isPlaceholder && def.name.isNotBlank() && def.name.lowercase() != "null") {
                    validItems.add(itemId)
                }
            } catch (e: Exception) {
                // Skip items that can't be loaded
                continue
            }
        }
        
        return validItems.toList()
    }
    
    /**
     * Converts clue scroll item ID to its corresponding clue casket item ID.
     * Same logic as NPC drop system.
     */
    private fun convertClueScrollToCasket(itemId: Int): Int {
        return when (itemId) {
            getRSCM("item.clue_scroll_beginner") -> getRSCM("item.reward_casket_beginner")
            getRSCM("item.clue_scroll_easy") -> getRSCM("item.casket_easy")
            getRSCM("item.clue_scroll_medium") -> getRSCM("item.casket_medium")
            getRSCM("item.clue_scroll_hard") -> getRSCM("item.casket_hard")
            getRSCM("item.clue_scroll_elite") -> getRSCM("item.casket_elite")
            getRSCM("item.clue_scroll_master") -> getRSCM("item.reward_casket_master")
            else -> itemId
        }
    }
    
    private suspend fun openBountyCrate(it: QueueTask, player: Player) {
        player.lock()
        try {
            // Check inventory space (need at least 5 free slots)
            if (player.inventory.freeSlotCount < 5) {
                player.message("You need at least 5 free inventory slots to open this crate.")
                return
            }
            
            // Remove the crate from inventory
            val slot = player.inventory.getItemIndex(28094, false)
            if (slot == -1) {
                player.message("You don't have that bounty crate.")
                return
            }
            
            val removeResult = player.inventory.remove(item = 28094, amount = 1, beginSlot = slot)
            if (!removeResult.hasSucceeded()) {
                player.message("Failed to open the bounty crate.")
                return
            }
            
            player.message("You open the bounty crate...")
            it.wait(2)
            
            // Give 5 random items from the game's item table
            if (validItemIds.isEmpty()) {
                player.message("Error: No valid items found.")
                return
            }
            
            for (i in 1..5) {
                // Randomly select one item from the valid items
                val randomItemId = validItemIds[Random.nextInt(validItemIds.size)]
                
                // Convert clue scrolls to clue caskets
                val itemIdToDrop = convertClueScrollToCasket(randomItemId)
                val finalItemDef = getItem(itemIdToDrop)
                
                // Determine amount: 1-100 for stackable items, 1 for non-stackable
                val amount = if (finalItemDef.stackable) {
                    Random.nextInt(1, 101)
                } else {
                    1
                }
                
                // Add to inventory
                val transaction = player.inventory.add(item = itemIdToDrop, amount = amount)
                if (transaction.hasFailed()) {
                    // Drop on ground if inventory is full
                    val groundItem = GroundItem(
                        item = itemIdToDrop,
                        amount = amount,
                        tile = player.tile,
                        owner = player
                    )
                    groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
                    groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
                    groundItem.ownerShipType = 1
                    world.spawn(groundItem)
                }
            }
            
            player.message("You find 5 random items in the bounty crate!")
            
        } finally {
            player.unlock()
        }
    }
}
