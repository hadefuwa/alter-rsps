package org.alter.plugins.content.skills.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.shops.CoinCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Slayer Equipment Shop
 * 
 * A shop where players can buy slayer equipment with coins.
 * This is the "Trade" option on slayer masters.
 */
class SlayerEquipmentShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Create the slayer equipment shop
        createShop(
            name = "Slayer Equipment Shop",
            currency = CoinCurrency(),
            stockSize = 50,
            purchasePolicy = PurchasePolicy.BUY_TRADEABLES
        ) {
            // Slayer Helmets
            try {
                val slayerHelmet = getRSCM("item.slayer_helmet")
                if (slayerHelmet != -1) {
                    items[0] = ShopItem(slayerHelmet, 100, sellPrice = 100000, buyPrice = 60000)
                }
            } catch (e: Exception) {}
            
            // Slayer Rings
            try {
                val slayerRing8 = getRSCM("item.slayer_ring_8")
                if (slayerRing8 != -1) {
                    items[1] = ShopItem(slayerRing8, 100, sellPrice = 750, buyPrice = 450)
                }
            } catch (e: Exception) {}
            
            // Broad Arrows and Bolts
            try {
                val broadArrows = getRSCM("item.broad_arrows")
                if (broadArrows != -1) {
                    items[2] = ShopItem(broadArrows, 10000, sellPrice = 60, buyPrice = 36)
                }
            } catch (e: Exception) {}
            
            try {
                val broadBolts = getRSCM("item.broad_bolts")
                if (broadBolts != -1) {
                    items[3] = ShopItem(broadBolts, 10000, sellPrice = 60, buyPrice = 36)
                }
            } catch (e: Exception) {}
            
            try {
                val broadArrowheads = getRSCM("item.broad_arrowheads")
                if (broadArrowheads != -1) {
                    items[4] = ShopItem(broadArrowheads, 10000, sellPrice = 60, buyPrice = 36)
                }
            } catch (e: Exception) {}
            
            try {
                val unfinishedBroadBolts = getRSCM("item.unfinished_broad_bolts")
                if (unfinishedBroadBolts != -1) {
                    items[5] = ShopItem(unfinishedBroadBolts, 10000, sellPrice = 60, buyPrice = 36)
                }
            } catch (e: Exception) {}
            
            // Slayer equipment items (nose peg, earmuffs, etc.)
            try {
                val nosePeg = getRSCM("item.nose_peg")
                if (nosePeg != -1) {
                    items[6] = ShopItem(nosePeg, 100, sellPrice = 100, buyPrice = 60)
                }
            } catch (e: Exception) {}
            
            try {
                val earmuffs = getRSCM("item.earmuffs")
                if (earmuffs != -1) {
                    items[7] = ShopItem(earmuffs, 100, sellPrice = 100, buyPrice = 60)
                }
            } catch (e: Exception) {}
            
            try {
                val facemask = getRSCM("item.facemask")
                if (facemask != -1) {
                    items[8] = ShopItem(facemask, 100, sellPrice = 200, buyPrice = 120)
                }
            } catch (e: Exception) {}
            
            try {
                val spinyHelmet = getRSCM("item.spiny_helmet")
                if (spinyHelmet != -1) {
                    items[9] = ShopItem(spinyHelmet, 100, sellPrice = 650, buyPrice = 390)
                }
            } catch (e: Exception) {}
            
            try {
                val bootsOfStone = getRSCM("item.boots_of_stone")
                if (bootsOfStone != -1) {
                    items[10] = ShopItem(bootsOfStone, 100, sellPrice = 1000, buyPrice = 600)
                }
            } catch (e: Exception) {}
        }
    }
    
    fun Player.openSlayerEquipmentShop() {
        this.openShop("Slayer Equipment Shop")
    }
}

