package org.alter.plugins.content.items.coins

import dev.openrune.cache.CacheManager.getItem
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
 * Plugin to handle opening coin pouches into coins.
 * 
 * Coin pouches are obtained from pickpocketing and need to be opened
 * to convert them into usable coins.
 */
class CoinPouchPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Coin pouch item IDs and their corresponding coin amounts.
         * Based on RuneScape mechanics, higher level pouches contain more coins.
         */
        private val COIN_POUCH_VALUES = mapOf(
            22523 to 30..100,   // H.A.M. member pouch
            22524 to 50..150,   // Warrior pouch
            22525 to 75..200,   // Rogue pouch
            22526 to 100..250,  // Cave goblin pouch
            22527 to 150..300,  // Guard pouch
            22528 to 200..400   // Fremennik pouch
        )
        
        const val COINS_ITEM_ID = 995
    }

    init {
        // Register handlers for all coin pouches
        COIN_POUCH_VALUES.keys.forEach { pouchId ->
            val pouchItemName = "item.coin_pouch_$pouchId"
            val pouchItemId = getRSCM(pouchItemName)
            val itemDef = getItem(pouchItemId)
            
            // Try to register "open" option first if it exists
            if (itemHasInventoryOption(pouchItemName, "open")) {
                try {
                    onItemOption(pouchItemName, "open") {
                        openCoinPouch(player, pouchId)
                    }
                    return@forEach
                } catch (e: Exception) {
                    // Continue to try other options
                }
            }
            
            // Try option 2 (common for openable items - this is what inventory clicks send)
            if (itemDef.interfaceOptions.size >= 2 && itemDef.interfaceOptions[1] != null) {
                if (!world.plugins.isItemBound(pouchItemId, 2)) {
                    try {
                        onItemOption(pouchItemName, 2) {
                            openCoinPouch(player, pouchId)
                        }
                        return@forEach
                    } catch (e: Exception) {
                        // Continue to try other options
                    }
                }
            }
            
            // Try option 1 as fallback
            if (itemDef.interfaceOptions.size >= 1 && itemDef.interfaceOptions[0] != null) {
                if (!world.plugins.isItemBound(pouchItemId, 1)) {
                    try {
                        onItemOption(pouchItemName, 1) {
                            openCoinPouch(player, pouchId)
                        }
                        return@forEach
                    } catch (e: Exception) {
                        println("Warning: Could not register handler for coin pouch $pouchId: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Opens coin pouches, removing all of them from inventory and adding coins.
     * If the player has a stack, all pouches in the stack will be opened at once.
     */
    private fun openCoinPouch(player: Player, pouchId: Int) {
        val coinRange = COIN_POUCH_VALUES[pouchId] ?: return
        
        player.queue {
            // Get the total count of coin pouches the player has
            val pouchCount = player.inventory.getItemCount(pouchId)
            if (pouchCount == 0) {
                player.message("You don't have that coin pouch.")
                return@queue
            }
            
            // Calculate total coins from all pouches
            var totalCoins = 0
            for (i in 0 until pouchCount) {
                val coinAmount = if (coinRange.first == coinRange.last) {
                    coinRange.first
                } else {
                    world.random(coinRange)
                }
                totalCoins += coinAmount
            }
            
            // Remove all pouches from inventory
            val removeResult = player.inventory.remove(item = pouchId, amount = pouchCount, assureFullRemoval = true)
            if (!removeResult.hasSucceeded()) {
                player.message("Failed to open the coin pouches.")
                return@queue
            }
            
            // Add all coins to inventory
            val addResult = player.inventory.add(item = COINS_ITEM_ID, amount = totalCoins)
            if (addResult.hasSucceeded()) {
                if (pouchCount == 1) {
                    player.message("You open the coin pouch and find $totalCoins coins.")
                } else {
                    player.message("You open $pouchCount coin pouches and find $totalCoins coins.")
                }
            } else {
                // If inventory is full, drop coins on ground
                val groundItem = GroundItem(
                    item = COINS_ITEM_ID,
                    amount = totalCoins,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = world.gameContext.gItemPublicDelay
                groundItem.timeUntilDespawn = world.gameContext.gItemDespawnDelay
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                if (pouchCount == 1) {
                    player.message("You open the coin pouch and find $totalCoins coins, but your inventory is full!")
                } else {
                    player.message("You open $pouchCount coin pouches and find $totalCoins coins, but your inventory is full!")
                }
            }
        }
    }
}

