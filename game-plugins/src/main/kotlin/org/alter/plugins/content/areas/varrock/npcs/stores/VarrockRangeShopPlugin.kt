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
 * Varrock Range Shop
 * 
 * A shop in the center of Varrock that sells ranged equipment and supplies.
 * Located at Varrock center (3211, 3438).
 */
class VarrockRangeShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.lowe"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Range shop items 
    // first number is quantity, second is price, third is buy price, fourth is sell price
    private val storeItems = listOf(
        ShopItem(getRSCM("item.bronze_bolts"), 5000, 10, 25),
        ShopItem(getRSCM("item.iron_bolts"), 5000, 10, 25),
        ShopItem(getRSCM("item.adamant_bolts"), 5000, 1000, 25),
        ShopItem(getRSCM("item.runite_bolts"), 5000, 5000, 25),
        ShopItem(getRSCM("item.shortbow"), 5000, 5000, 25),
        ShopItem(getRSCM("item.maple_shortbow"), 5000, 5000, 25),
        ShopItem(getRSCM("item.magic_shortbow"), 5000, 5000000, 25),
        ShopItem(getRSCM("item.crossbow"), 5000, 5000, 25),
        ShopItem(getRSCM("item.dorgeshuun_crossbow"), 5000, 1000000, 25),
        ShopItem(getRSCM("item.bone_bolts"), 5000, 100, 25),
        ShopItem(getRSCM("item.green_dhide_body"), 5000, 5000, 25),
        ShopItem(getRSCM("item.green_dhide_chaps"), 5000, 5000, 25),
        ShopItem(getRSCM("item.green_dhide_vambraces"), 5000, 5000, 25),
        ShopItem(getRSCM("item.bronze_knife"), 5000, 10, 25),
        ShopItem(getRSCM("item.iron_knife"), 5000, 20, 25),
        ShopItem(getRSCM("item.rune_knife"), 5000, 10000, 25),       
    )

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        val shopkeeperTile = Tile(x = 3211, z = 3438, height = 0)
        spawnNpc(shopkeeper, 3211, 3438, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("Range Shopkeeper")
            }
        }

        // Create the range shop with enough space for items
        createShop(
            name = "Varrock Range Shop",
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

        // Set up NPC interactions - check tile location to avoid conflicts with other shops using same NPC type
        onNpcOption(shopkeeper, option = "talk-to") { 
            val npc = player.getInteractingNpc()
            if (npc.tile == shopkeeperTile) {
                player.queue { dialog(player) } 
            }
        }

        onNpcOption(shopkeeper, option = "trade") { 
            val npc = player.getInteractingNpc()
            if (npc.tile == shopkeeperTile) {
                player.shop() 
            }
        }
    }

    fun Player.shop() = this.openShop("Varrock Range Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Range Shop! We have all kinds of ranged equipment and supplies.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}


