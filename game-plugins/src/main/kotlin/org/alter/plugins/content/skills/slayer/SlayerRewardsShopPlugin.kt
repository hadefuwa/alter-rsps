package org.alter.plugins.content.skills.slayer

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.shops.SlayerPointCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Slayer Rewards Shop
 * 
 * A shop where players can spend slayer points to purchase slayer-related items.
 */
class SlayerRewardsShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Create the slayer rewards shop
        createShop(
            name = "Slayer Rewards Shop",
            currency = SlayerPointCurrency(),
            stockSize = 50,
            purchasePolicy = PurchasePolicy.BUY_NONE // Can't sell items to this shop
        ) {
            // Slayer Helmets
            try {
                val slayerHelmet = getRSCM("item.slayer_helmet")
                if (slayerHelmet != -1) {
                    items[0] = ShopItem(slayerHelmet, 100, sellPrice = 400, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val slayerHelmetI = getRSCM("item.slayer_helmet_i")
                if (slayerHelmetI != -1) {
                    items[1] = ShopItem(slayerHelmetI, 100, sellPrice = 400, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            // Slayer Rings
            try {
                val slayerRing8 = getRSCM("item.slayer_ring_8")
                if (slayerRing8 != -1) {
                    items[2] = ShopItem(slayerRing8, 100, sellPrice = 75, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val slayerRingEternal = getRSCM("item.slayer_ring_eternal")
                if (slayerRingEternal != -1) {
                    items[3] = ShopItem(slayerRingEternal, 100, sellPrice = 300, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            // Broad Arrows and Bolts
            try {
                val broadArrows = getRSCM("item.broad_arrows")
                if (broadArrows != -1) {
                    items[4] = ShopItem(broadArrows, 10000, sellPrice = 35, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val broadBolts = getRSCM("item.broad_bolts")
                if (broadBolts != -1) {
                    items[5] = ShopItem(broadBolts, 10000, sellPrice = 35, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val broadArrowheads = getRSCM("item.broad_arrowheads")
                if (broadArrowheads != -1) {
                    items[6] = ShopItem(broadArrowheads, 10000, sellPrice = 35, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val unfinishedBroadBolts = getRSCM("item.unfinished_broad_bolts")
                if (unfinishedBroadBolts != -1) {
                    items[7] = ShopItem(unfinishedBroadBolts, 10000, sellPrice = 35, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            // Broad Arrowhead Packs
            try {
                val broadArrowheadPack = getRSCM("item.broad_arrowhead_pack")
                if (broadArrowheadPack != -1) {
                    items[8] = ShopItem(broadArrowheadPack, 100, sellPrice = 300, buyPrice = null)
                }
            } catch (e: Exception) {}
            
            try {
                val unfinishedBroadBoltPack = getRSCM("item.unfinished_broad_bolt_pack")
                if (unfinishedBroadBoltPack != -1) {
                    items[9] = ShopItem(unfinishedBroadBoltPack, 100, sellPrice = 300, buyPrice = null)
                }
            } catch (e: Exception) {}
        }
    }
    
    fun Player.openSlayerRewardsShop() {
        this.openShop("Slayer Rewards Shop")
    }
}

