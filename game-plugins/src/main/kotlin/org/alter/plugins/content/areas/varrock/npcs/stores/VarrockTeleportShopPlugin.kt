package org.alter.plugins.content.areas.varrock.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.info.NpcInfo
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.shops.CoinCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Varrock Teleport Shop
 * 
 * A shop in the center of Varrock that sells teleport tablets and scrolls.
 * Located at Varrock center (3205, 3431).
 */
class VarrockTeleportShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_7769"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Teleport shop items - only items that exist in RSCM
    private val storeItems = listOf(
        // Standard teleports
        ShopItem(getRSCM("item.varrock_teleport"), 500, 1000, 500),
        ShopItem(getRSCM("item.lumbridge_teleport"), 500, 1000, 500),
        ShopItem(getRSCM("item.falador_teleport"), 500, 1500, 750),
        ShopItem(getRSCM("item.camelot_teleport"), 500, 2000, 1000),
        ShopItem(getRSCM("item.ardougne_teleport"), 500, 2500, 1250),
        ShopItem(getRSCM("item.watchtower_teleport"), 500, 3000, 1500),
        ShopItem(getRSCM("item.trollheim_teleport"), 500, 3500, 1750),
        // Ancient teleports
        ShopItem(getRSCM("item.paddewwa_teleport"), 500, 3000, 1500),
        ShopItem(getRSCM("item.senntisten_teleport"), 500, 3500, 1750),
        ShopItem(getRSCM("item.kharyrll_teleport"), 500, 4000, 2000),
        ShopItem(getRSCM("item.lassar_teleport"), 500, 4500, 2250),
        ShopItem(getRSCM("item.dareeyak_teleport"), 500, 5000, 2500),
        ShopItem(getRSCM("item.carrallanger_teleport"), 500, 5500, 2750), // Note: spelled "carrallanger" not "carrallangar"
        ShopItem(getRSCM("item.annakarl_teleport"), 500, 6000, 3000),
        ShopItem(getRSCM("item.ghorrock_teleport"), 500, 7000, 3500),
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3205, z = 3431, height = 0)
        spawnNpc(shopkeeper, 3205, 3431, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Teleport Shopkeeper")
            }
        }

        // Create the teleport shop with enough space for items
        createShop(
            name = "Varrock Teleport Shop",
            currency = CoinCurrency(),
            stockSize = 100,
            purchasePolicy = PurchasePolicy.BUY_TRADEABLES
        ) {
            storeItems.forEachIndexed { index, item ->
                if (index < items.size) {
                    items[index] = item
                }
            }
        }

        // Set up NPC interactions
        onNpcOption(shopkeeper, option = "talk-to") { 
            player.queue { dialog(player) } 
        }

        onNpcOption(shopkeeper, option = "trade") { 
            player.shop() 
        }
    }

    fun Player.shop() = this.openShop("Varrock Teleport Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Teleport Shop! We have teleport tablets to help you travel quickly.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

