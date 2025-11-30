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
import org.alter.plugins.content.mechanics.shops.GeneralStoreCurrency
import org.alter.rscm.RSCM.getRSCM

class VarrockShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    private val shopkeepers = listOf("npc.shop_assistant_2818", "npc.shop_keeper_2817")

    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )

    private val storeItems = listOf(
        ShopItem(getRSCM("item.pot"), 5, 1, 0),
        ShopItem(getRSCM("item.jug"), 2, 1, 0),
        ShopItem(getRSCM("item.empty_jug_pack"), 5, 182, 56),
        ShopItem(getRSCM("item.shears"), 2, 1, 0),
        ShopItem(getRSCM("item.knife"), 5, 7, 0),
        ShopItem(getRSCM("item.bucket"), 3, 2, 0),
        ShopItem(getRSCM("item.empty_bucket_pack"), 15, 650, 200),
        ShopItem(getRSCM("item.bowl"), 2, 5, 1),
        ShopItem(getRSCM("item.cake_tin"), 2, 13, 4),
        ShopItem(getRSCM("item.tinderbox"), 2, 1, 0),
        ShopItem(getRSCM("item.chisel"), 2, 1, 0),
        ShopItem(getRSCM("item.spade"), 1, 3, 1),
        ShopItem(getRSCM("item.hammer"), 5, 1, 0),
        ShopItem(getRSCM("item.newcomer_map"), 5, 1, 0),
        ShopItem(getRSCM("item.security_book"), 5, 2, 0),
        ShopItem(getRSCM("item.red_partyhat"), 0, null, 2_000_000),
        ShopItem(getRSCM("item.yellow_partyhat"), 0, null, 2_000_000),
        ShopItem(getRSCM("item.blue_partyhat"), 0, null, 2_000_000),
        ShopItem(getRSCM("item.green_partyhat"), 0, null, 2_000_000),
        ShopItem(getRSCM("item.purple_partyhat"), 0, null, 2_000_000),
        ShopItem(getRSCM("item.white_partyhat"), 0, null, 2_000_000),
    )

    init {
        // Varrock General Store (main square) - using valid RSCM names
        val shopkeeperTile1 = Tile(x = 3217, z = 3415, height = 0)
        val shopkeeperTile2 = Tile(x = 3218, z = 3415, height = 0)
        spawnNpc("npc.shop_keeper_2817", 3217, 3415, 0, 3, Direction.SOUTH)
        spawnNpc("npc.shop_assistant_2818", 3218, 3415, 0, 3, Direction.SOUTH)
        
        // Set custom name for the shopkeepers when they spawn
        onNpcSpawn("npc.shop_keeper_2817") {
            if (npc.tile == shopkeeperTile1) {
                NpcInfo(npc).setTempName("General Store Shopkeeper")
            }
        }
        onNpcSpawn("npc.shop_assistant_2818") {
            if (npc.tile == shopkeeperTile2) {
                NpcInfo(npc).setTempName("General Store Shopkeeper")
            }
        }

        createShop(
            name = "Varrock General Store",
            currency = GeneralStoreCurrency(),
            stockSize = 100,
            purchasePolicy = PurchasePolicy.BUY_ALL
        ) {
            storeItems.forEachIndexed { index, item ->
                items[index] = item
            }
        }

        shopkeepers.forEach {
            onNpcOption(it, option = "talk-to") { player.queue { dialog(player) } }

            onNpcOption(it, option = "trade") { player.shop() }
        }
    }

    fun Player.shop() = this.openShop("Varrock General Store")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Can I help you at all?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}