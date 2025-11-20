package org.alter.plugins.content.areas.falador.npcs.stores

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.shop.PurchasePolicy
import org.alter.game.model.shop.ShopItem
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.shops.GeneralStoreCurrency
import org.alter.rscm.RSCM.getRSCM

class FaladorShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    private val shopkeepers = listOf("npc.shop_assistant_2820", "npc.shop_keeper_2819")

    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )

    private val storeItems = listOf(
        ShopItem(getRSCM("item.pot"), 5, 1, 0),
        ShopItem(getRSCM("item.jug"), 2, 1, 0),
        ShopItem(getRSCM("item.shears"), 2, 1, 0),
        ShopItem(getRSCM("item.knife"), 5, 7, 0),
        ShopItem(getRSCM("item.bucket"), 3, 2, 0),
        ShopItem(getRSCM("item.bowl"), 2, 5, 1),
        ShopItem(getRSCM("item.cake_tin"), 2, 13, 4),
        ShopItem(getRSCM("item.tinderbox"), 2, 1, 0),
        ShopItem(getRSCM("item.chisel"), 2, 1, 0),
        ShopItem(getRSCM("item.spade"), 1, 3, 1),
        ShopItem(getRSCM("item.hammer"), 5, 1, 0),
    )

    init {
        // Falador General Store - using valid RSCM names
        spawnNpc("npc.shop_keeper_2819", 2955, 3388, 0, 3, Direction.EAST)
        spawnNpc("npc.shop_assistant_2820", 2956, 3388, 0, 3, Direction.EAST)

        createShop("Falador General Store", GeneralStoreCurrency(), purchasePolicy = PurchasePolicy.BUY_TRADEABLES) {
            storeItems.forEachIndexed { index, item ->
                items[index] = item
            }
        }

        shopkeepers.forEach {
            onNpcOption(it, option = "talk-to") { player.queue { dialog(player) } }

            onNpcOption(it, option = "trade") { player.shop() }
        }
    }

    fun Player.shop() = this.openShop("Falador General Store")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Can I help you at all?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}