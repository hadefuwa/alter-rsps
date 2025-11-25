package org.alter.plugins.content.mechanics.shops

import org.alter.api.ext.message
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.shop.Shop
import org.alter.game.model.shop.ShopCurrency
import org.alter.game.model.shop.ShopItem
import org.alter.plugins.content.skills.slayer.Slayer

/**
 * Currency for slayer reward shops that uses slayer points instead of items.
 */
class SlayerPointCurrency : ShopCurrency {
    
    override fun onSellValueMessage(p: Player, shopItem: ShopItem) {
        val value = shopItem.sellPrice ?: getSellPrice(p.world, shopItem.item)
        val currency = if (value != 1) "slayer points" else "slayer point"
        val itemName = org.alter.game.model.item.Item(shopItem.item).getName()
        
        // Check if this is a clue casket bundle
        val isClueCasket = shopItem.item == 20544
        val bundleSize = when {
            isClueCasket && value == 50 -> 4  // 4-pack bundle
            isClueCasket && value == 100 -> 10  // 10-pack bundle
            else -> 1  // Regular purchase or single casket
        }
        
        if (bundleSize > 1) {
            p.message("$itemName (x$bundleSize): currently costs $value $currency")
        } else {
            p.message("$itemName: currently costs $value $currency")
        }
    }

    override fun onBuyValueMessage(p: Player, shop: Shop, item: Int) {
        // Slayer reward shops don't buy items from players
        p.message("You can't sell items to this shop.")
    }

    override fun getSellPrice(world: World, item: Int): Int {
        // Default price - should be overridden by shop items
        return 1
    }

    override fun getBuyPrice(world: World, item: Int): Int {
        // Slayer reward shops don't buy items
        return 0
    }

    override fun sellToPlayer(p: Player, shop: Shop, slot: Int, amt: Int) {
        val shopItem = shop.items[slot] ?: return

        val currencyCost = shopItem.sellPrice ?: getSellPrice(p.world, shopItem.item)
        val currencyCount = Slayer.getSlayerPoints(p)

        // Check if this is a 100m coins bundle purchase (item ID 995, slot 10, price 200)
        val is100mCoins = shopItem.item == 995 && currencyCost == 200 && slot == 10
        
        // Check if this is a clue casket bundle purchase (item ID 20544)
        val isClueCasket = shopItem.item == 20544
        val bundleSize = when {
            is100mCoins -> 100_000_000  // 100 million coins bundle
            isClueCasket && currencyCost == 50 -> 4  // 4-pack bundle
            isClueCasket && currencyCost == 100 -> 10  // 10-pack bundle
            else -> 1  // Regular purchase or single casket
        }

        // For bundle purchases, calculate how many bundles can be bought
        var bundlesToBuy = Math.min(Math.floor(currencyCount.toDouble() / currencyCost.toDouble()).toInt(), amt)
        
        if (bundlesToBuy == 0) {
            p.message("You don't have enough slayer points.")
            return
        }

        // Limit by stock (for bundles, stock represents number of bundles available)
        // For 100m coins, skip stock limit check - it's unlimited
        val is100mCoinsCheck = shopItem.item == 995 && currencyCost == 200 && slot == 10
        val moreThanStock = if (is100mCoinsCheck) false else bundlesToBuy > shopItem.currentAmount
        bundlesToBuy = if (is100mCoinsCheck) bundlesToBuy else Math.min(bundlesToBuy, shopItem.currentAmount)

        if (bundlesToBuy == 0 && !is100mCoinsCheck) {
            p.message("The shop has run out of stock.")
            return
        }

        if (moreThanStock) {
            p.message("The shop has run out of stock.")
        }

        // Calculate total cost and items to give
        val totalCost = currencyCost.toLong() * bundlesToBuy.toLong()
        val itemsToGive = bundlesToBuy * bundleSize

        if (totalCost > Int.MAX_VALUE) {
            return
        }

        if (Slayer.getSlayerPoints(p) < totalCost) {
            p.message("You don't have enough slayer points.")
            return
        }

        if (!Slayer.removeSlayerPoints(p, totalCost.toInt())) {
            p.message("You don't have enough slayer points.")
            return
        }

        val add = p.inventory.add(item = shopItem.item, amount = itemsToGive, assureFullInsertion = false)
        if (add.completed == 0) {
            p.message("You don't have enough inventory space.")
            // Refund points if we couldn't add the item
            Slayer.addSlayerPoints(p, totalCost.toInt())
        } else {
            // For coins (item 995), they always stack so we don't need bundle logic
            // For other bundle items, calculate how many complete bundles were actually given
            if (is100mCoins) {
                // Coins stack, so all coins given are valid
                // Calculate how many bundles were actually given based on coins received
                val bundlesGiven = add.completed / bundleSize
                val bundlesNotGiven = bundlesToBuy - bundlesGiven
                
                // Refund for bundles we couldn't give
                if (bundlesNotGiven > 0) {
                    val refund = bundlesNotGiven * currencyCost
                    Slayer.addSlayerPoints(p, refund)
                }
                
                // For 100m coins, stock is unlimited - don't reduce stock, just refresh display
                // The display will always show 100m coins available
                if (bundlesGiven > 0) {
                    shop.refresh(p.world)
                }
            } else {
                // For clue casket bundles, use the existing bundle logic
                val completeBundlesGiven = add.completed / bundleSize
                val itemsInCompleteBundles = completeBundlesGiven * bundleSize
                val extraItems = add.completed - itemsInCompleteBundles
                
                // For bundle purchases, we only give complete bundles
                // Remove any extra items that don't form a complete bundle
                if (extraItems > 0 && bundleSize > 1) {
                    p.inventory.remove(item = shopItem.item, amount = extraItems, assureFullRemoval = false)
                }
                
                val bundlesNotGiven = bundlesToBuy - completeBundlesGiven
                
                // Refund for bundles we couldn't give (including partial bundles)
                if (bundlesNotGiven > 0) {
                    val refund = bundlesNotGiven * currencyCost
                    Slayer.addSlayerPoints(p, refund)
                }

                if (completeBundlesGiven > 0 && shopItem.amount != Int.MAX_VALUE) {
                    val currentShopItem = shop.items[slot]
                    if (currentShopItem != null) {
                        // Decrease stock by number of complete bundles sold
                        currentShopItem.currentAmount -= completeBundlesGiven

                        /*
                         * Check if the item is temporary and should be removed from the shop.
                         */
                        if (currentShopItem.amount == 0 && currentShopItem.isTemporary == true) {
                            shop.items[slot] = null
                        }

                        shop.refresh(p.world)
                    }
                }
            }
        }
    }

    override fun buyFromPlayer(p: Player, shop: Shop, slot: Int, amt: Int) {
        // Slayer reward shops don't buy items from players
        p.message("You can't sell items to this shop.")
    }
}

