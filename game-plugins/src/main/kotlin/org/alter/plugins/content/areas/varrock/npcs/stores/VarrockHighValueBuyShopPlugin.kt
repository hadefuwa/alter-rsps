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

/**
 * Varrock High Value Buy Shop
 * 
 * A shop in Varrock that buys specific high-value items from players for 10m each.
 * Items have 0 stock, so players can only sell to the store, not buy from it.
 * Located at Varrock (3226, 3414).
 */
class VarrockHighValueBuyShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_2825"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you buying?",
        "No thanks.",
    )
    
    // Items that can be sold to the shop for 10m each
    // amount = 0 means no stock, so players can only sell to the store
    // sellPrice = null means the shop doesn't sell these items
    // buyPrice = 10000000 means the shop buys from players for 10 million coins each
    private val storeItems = listOf(
        // Original items
        ShopItem(26113, 0, null, 10000000), // Sigil of Versatility
        ShopItem(29664, 0, null, 10000000), // Sigil of Adroit
        ShopItem(29665, 0, null, 10000000), // Sigil of Adroit (noted)
        ShopItem(26128, 0, null, 10000000), // Sigil of Pious Protection
        ShopItem(26083, 0, null, 10000000), // Sigil of the Dwarves
        ShopItem(26123, 0, null, 10000000), // Sigil of Preservation
        ShopItem(26059, 0, null, 10000000), // Sigil of Specialised Strikes
        ShopItem(28522, 0, null, 10000000), // (Unknown item)
        ShopItem(28525, 0, null, 10000000), // (Unknown item)
        ShopItem(26056, 0, null, 10000000), // Sigil of Exaggeration
        
        // Additional sigils
        ShopItem(25990, 0, null, 10000000), // Sigil of Resilience
        ShopItem(25993, 0, null, 10000000), // Sigil of Consistency
        ShopItem(25996, 0, null, 10000000), // Sigil of the Formidable Fighter
        ShopItem(25999, 0, null, 10000000), // Sigil of the Rigorous Ranger
        ShopItem(26002, 0, null, 10000000), // Sigil of the Meticulous Mage
        ShopItem(26005, 0, null, 10000000), // Sigil of Fortification
        ShopItem(26008, 0, null, 10000000), // Sigil of Barrows
        ShopItem(26011, 0, null, 10000000), // Sigil of Deft Strikes
        ShopItem(26014, 0, null, 10000000), // Sigil of Freedom
        ShopItem(26017, 0, null, 10000000), // Sigil of Enhanced Harvest
        ShopItem(26020, 0, null, 10000000), // Sigil of Storage
        ShopItem(26023, 0, null, 10000000), // Sigil of the Smith
        ShopItem(26026, 0, null, 10000000), // Sigil of the Alchemist
        ShopItem(26029, 0, null, 10000000), // Sigil of the Fletcher
        ShopItem(26032, 0, null, 10000000), // Sigil of the Chef
        ShopItem(26035, 0, null, 10000000), // Sigil of the Craftsman
        ShopItem(26038, 0, null, 10000000), // Sigil of the Abyss
        ShopItem(26041, 0, null, 10000000), // Sigil of Stamina
        ShopItem(26044, 0, null, 10000000), // Sigil of the Potion Master
        ShopItem(26047, 0, null, 10000000), // Sigil of the Eternal Jeweller
        ShopItem(26050, 0, null, 10000000), // Sigil of the Treasure Hunter
        ShopItem(26051, 0, null, 10000000), // Sigil of the Treasure Hunter (variant)
        ShopItem(26053, 0, null, 10000000), // Sigil of Mobility
        ShopItem(26062, 0, null, 10000000), // Sigil of the Porcupine
        ShopItem(26065, 0, null, 10000000), // Sigil of Binding
        ShopItem(26068, 0, null, 10000000), // Sigil of Escaping
        ShopItem(26071, 0, null, 10000000), // Sigil of the Ruthless Ranger
        ShopItem(26074, 0, null, 10000000), // Sigil of the Feral Fighter
        ShopItem(26077, 0, null, 10000000), // Sigil of the Menacing Mage
        ShopItem(26080, 0, null, 10000000), // Sigil of Prosperity
        ShopItem(26086, 0, null, 10000000), // Sigil of the Elves
        ShopItem(26089, 0, null, 10000000), // Sigil of the Barbarians
        ShopItem(26092, 0, null, 10000000), // Sigil of the Gnomes
        ShopItem(26095, 0, null, 10000000), // Sigil of Nature
        ShopItem(26098, 0, null, 10000000), // Sigil of Devotion
        ShopItem(26101, 0, null, 10000000), // Sigil of the Forager
        ShopItem(26104, 0, null, 10000000), // Sigil of Garments
        ShopItem(26105, 0, null, 10000000), // Sigil of Garments (variant)
        ShopItem(26107, 0, null, 10000000), // Sigil of Slaughter
        ShopItem(26110, 0, null, 10000000), // Sigil of the Fortune Farmer
        ShopItem(26116, 0, null, 10000000), // Sigil of the Serpent
        ShopItem(26119, 0, null, 10000000), // Sigil of Supreme Stamina
        ShopItem(26122, 0, null, 10000000), // Sigil of Preservation (base)
        ShopItem(26125, 0, null, 10000000), // Sigil of Finality
        ShopItem(26131, 0, null, 10000000), // Sigil of Aggression
        ShopItem(26134, 0, null, 10000000), // Sigil of Rampage
        ShopItem(26137, 0, null, 10000000), // Sigil of the Skiller
        ShopItem(26140, 0, null, 10000000), // Sigil of Remote Storage
        ShopItem(26143, 0, null, 10000000), // Sigil of Last Recall
        ShopItem(26146, 0, null, 10000000), // Sigil of the Guardian Angel
        ShopItem(29655, 0, null, 10000000), // Sigil of Rampart
        ShopItem(28523, 0, null, 10000000), // Sigil of Titanium
    )

    init {
        // Spawn shopkeeper in Varrock (stationary, no walking)
        val shopkeeperTile = Tile(x = 3226, z = 3414, height = 0)
        spawnNpc(shopkeeper, 3226, 3414, 0, 0, Direction.SOUTH)
        
        // Set custom name for the shopkeeper when it spawns
        onNpcSpawn(shopkeeper) {
            if (npc.tile == shopkeeperTile) {
                NpcInfo(npc).setTempName("High Value Buyer")
            }
        }

        // Create the shop with enough space for items
        // Using BUY_STOCK so it only accepts items we've defined in the shop (regardless of tradeable status)
        createShop(
            name = "Varrock High Value Buy Shop",
            currency = CoinCurrency(),
            stockSize = 100,
            purchasePolicy = PurchasePolicy.BUY_STOCK
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

    fun Player.shop() = this.openShop("Varrock High Value Buy Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome! I buy special items for 10 million coins each.")
        chatNpc(player, "Would you like to see what I'm buying?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

