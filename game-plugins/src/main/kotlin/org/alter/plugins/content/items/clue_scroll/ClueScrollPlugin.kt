package org.alter.plugins.content.items.clue_scroll

import dev.openrune.cache.CacheManager.getItem
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

/**
 * Plugin for handling clue scrolls.
 * Converts clue scrolls to their corresponding caskets when used.
 */
class ClueScrollPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Register all clue scroll types to convert to caskets
        registerClueScroll("item.clue_scroll_beginner", "item.reward_casket_beginner")
        registerClueScroll("item.clue_scroll_easy", "item.casket_easy")
        registerClueScroll("item.clue_scroll_medium", "item.casket_medium")
        registerClueScroll("item.clue_scroll_hard", "item.casket_hard")
        registerClueScroll("item.clue_scroll_elite", "item.casket_elite")
        registerClueScroll("item.clue_scroll_master", "item.reward_casket_master")
        
        // Also register by item ID 2804 (medium clue scroll variant)
        // Check if it's actually a scroll or casket
        try {
            val itemDef = getItem(2804)
            val itemName = itemDef.name.lowercase()
            if (itemName.contains("clue") && itemName.contains("scroll") && itemName.contains("medium")) {
                // It's a clue scroll, register it
                r.bindItem(2804, 2) {
                    player.queue {
                        convertClueScrollToCasket(this, player, 2804, "item.casket_medium")
                    }
                }
            } else if (itemName.contains("casket") && itemName.contains("medium")) {
                // It's already a casket, register it to open
                r.bindItem(2804, 2) {
                    player.queue {
                        // This should be handled by ClueCasketPlugin, but let's make sure
                        player.message("You read the clue scroll and receive a casket!")
                    }
                }
            }
        } catch (e: Exception) {
            // Item might not exist, that's okay
        }
    }
    
    private fun registerClueScroll(scrollName: String, casketName: String) {
        try {
            val scrollId = getRSCM(scrollName)
            val itemDef = getItem(scrollId)
            
            // Register option 2 (most common for "use" or "read")
            if (!world.plugins.isItemBound(scrollId, 2)) {
                try {
                    onItemOption(scrollName, 2) {
                        player.queue {
                            convertClueScrollToCasket(this, player, scrollId, casketName)
                        }
                    }
                    return
                } catch (e: Exception) {
                    // Option 2 might not work, try others
                }
            }
            
            // Try option 1
            if (!world.plugins.isItemBound(scrollId, 1)) {
                try {
                    onItemOption(scrollName, 1) {
                        player.queue {
                            convertClueScrollToCasket(this, player, scrollId, casketName)
                        }
                    }
                    return
                } catch (e: Exception) {
                    // Option 1 might not work
                }
            }
            
            // Try "read" option
            if (itemHasInventoryOption(scrollName, "read")) {
                try {
                    onItemOption(scrollName, "read") {
                        player.queue {
                            convertClueScrollToCasket(this, player, scrollId, casketName)
                        }
                    }
                    return
                } catch (e: Exception) {
                    // "read" option might not work
                }
            }
            
            // Try "use" option
            if (itemHasInventoryOption(scrollName, "use")) {
                try {
                    onItemOption(scrollName, "use") {
                        player.queue {
                            convertClueScrollToCasket(this, player, scrollId, casketName)
                        }
                    }
                } catch (e: Exception) {
                    // "use" option might not work
                }
            }
        } catch (e: Exception) {
            // Item might not exist, that's okay
        }
    }
    
    private suspend fun convertClueScrollToCasket(it: QueueTask, player: Player, scrollId: Int, casketName: String) {
        player.lock()
        try {
            // Find the scroll in inventory
            val slot = player.inventory.getItemIndex(scrollId, false)
            if (slot == -1) {
                player.message("You don't have that clue scroll.")
                return
            }
            
            // Remove the scroll
            val removeResult = player.inventory.remove(item = scrollId, amount = 1, beginSlot = slot)
            if (!removeResult.hasSucceeded()) {
                player.message("Failed to read the clue scroll.")
                return
            }
            
            player.message("You read the clue scroll...")
            it.wait(2)
            
            // Add the casket
            val addResult = player.inventory.add(item = casketName, amount = 1)
            if (addResult.hasSucceeded()) {
                player.message("You receive a clue casket!")
            } else {
                // Drop on ground if inventory is full
                val casketId = getRSCM(casketName)
                val groundItem = GroundItem(
                    item = casketId,
                    amount = 1,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
                groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full. The casket appears on the ground.")
            }
        } finally {
            player.unlock()
        }
    }
}

