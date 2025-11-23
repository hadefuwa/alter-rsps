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
        p.message("$itemName: currently costs $value $currency")
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

        var amount = Math.min(Math.floor(currencyCount.toDouble() / currencyCost.toDouble()).toInt(), amt)

        if (amount == 0) {
            p.message("You don't have enough slayer points.")
            return
        }

        val moreThanStock = amount > shopItem.currentAmount

        amount = Math.min(amount, shopItem.currentAmount)

        if (amount == 0) {
            p.message("The shop has run out of stock.")
            return
        }

        if (moreThanStock) {
            p.message("The shop has run out of stock.")
        }

        val totalCost = currencyCost.toLong() * amount.toLong()
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

        val add = p.inventory.add(item = shopItem.item, amount = amount, assureFullInsertion = false)
        if (add.completed == 0) {
            p.message("You don't have enough inventory space.")
            // Refund points if we couldn't add the item
            Slayer.addSlayerPoints(p, totalCost.toInt())
        } else {
            if (add.getLeftOver() > 0) {
                val refund = add.getLeftOver() * currencyCost
                Slayer.addSlayerPoints(p, refund)
            }

            if (add.completed > 0 && shopItem.amount != Int.MAX_VALUE) {
                val currentShopItem = shop.items[slot]
                if (currentShopItem != null) {
                    currentShopItem.currentAmount -= add.completed

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

    override fun buyFromPlayer(p: Player, shop: Shop, slot: Int, amt: Int) {
        // Slayer reward shops don't buy items from players
        p.message("You can't sell items to this shop.")
    }
}

