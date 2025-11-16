package org.alter.plugins.content.areas.varrock.npcs.stores

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
import org.alter.plugins.content.mechanics.shops.CoinCurrency
import org.alter.rscm.RSCM.getRSCM

/**
 * Varrock Supplies Shop
 * 
 * A shop in the center of Varrock that sells general supplies and tools.
 * Located at Varrock center (3218, 3425).
 */
class VarrockSuppliesShopPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val shopkeeper = "npc.shop_keeper_2888"
    
    private val dialogOptions: List<String> = listOf(
        "Yes please. What are you selling?",
        "No thanks.",
    )
    
    // Build shop items with supplies (1k-10k range)
    private val storeItems: List<ShopItem> = buildList {
        // Helper function to add item with pricing
        fun addItem(itemName: String, price: Int) {
            try {
                val itemId = getRSCM("item.$itemName")
                if (itemId != -1) {
                    add(ShopItem(itemId, 100, price, 50))
                }
            } catch (e: Exception) {
                // Skip items not found in RSCM
            }
        }
        
        // Tools - 1k-5k
        addItem("rune_pickaxe", 5000)
        addItem("rune_axe", 5000)
        addItem("rune_hatchet", 5000)
        addItem("dragon_pickaxe", 10000)
        addItem("dragon_axe", 10000)
        addItem("dragon_hatchet", 10000)
        addItem("infernal_pickaxe", 10000)
        addItem("infernal_axe", 10000)
        
        // Teleportation - 2k-10k
        // Basic teleports - 2k-3k
        addItem("varrock_teleport", 2000)
        addItem("lumbridge_teleport", 2000)
        addItem("falador_teleport", 2000)
        addItem("camelot_teleport", 3000)
        addItem("ardougne_teleport", 3000)
        
        // Mid-tier teleports - 4k-6k
        addItem("watchtower_teleport", 4000)
        addItem("rimmington_teleport", 4000)
        addItem("taverley_teleport", 4000)
        addItem("yanille_teleport", 4000)
        addItem("catherby_teleport", 4000)
        addItem("rellekka_teleport", 5000)
        addItem("brimhaven_teleport", 5000)
        addItem("pollnivneach_teleport", 5000)
        addItem("hosidius_teleport", 5000)
        addItem("trollheim_teleport", 6000)
        
        // High-tier teleports - 7k-10k
        addItem("ape_atoll_teleport", 7000)
        addItem("kourend_castle_teleport", 7000)
        addItem("barbarian_teleport", 7000)
        addItem("fishing_guild_teleport", 7000)
        addItem("khazard_teleport", 7000)
        addItem("draynor_manor_teleport", 8000)
        addItem("mind_altar_teleport", 8000)
        addItem("barrows_teleport", 9000)
        addItem("lunar_isle_teleport", 9000)
        addItem("zulandra_teleport", 10000)
        addItem("pest_control_teleport", 10000)
        addItem("piscatoris_teleport", 10000)
        
        // Runes - 1k-3k per 100
        addItem("air_rune", 1000) // per 100
        addItem("water_rune", 1000)
        addItem("earth_rune", 1000)
        addItem("fire_rune", 1000)
        addItem("mind_rune", 1000)
        addItem("body_rune", 1000)
        addItem("chaos_rune", 2000)
        addItem("death_rune", 2500)
        addItem("blood_rune", 3000)
        addItem("soul_rune", 3000)
        addItem("law_rune", 2000)
        addItem("nature_rune", 2000)
        
        // Ammunition - 1k-5k
        addItem("rune_arrow", 1000) // per 100
        addItem("rune_bolts", 1500)
        addItem("rune_dart", 1500)
        addItem("rune_knife", 2000)
        addItem("dragon_arrow", 5000)
        addItem("dragon_bolts", 5000)
        addItem("dragon_dart", 5000)
        
        // Herbs & Seeds - 1k-8k
        addItem("guam_leaf", 1000)
        addItem("marrentill", 1000)
        addItem("tarromin", 1500)
        addItem("harralander", 2000)
        addItem("ranarr_weed", 3000)
        addItem("irit_leaf", 4000)
        addItem("avantoe", 5000)
        addItem("kwuarm", 6000)
        addItem("cadantine", 7000)
        addItem("dwarf_weed", 8000)
        addItem("torstol", 10000)
        
        // Other Supplies - 1k-5k
        addItem("rope", 1000)
        addItem("tinderbox", 1000)
        addItem("hammer", 1000)
        addItem("chisel", 1000)
        addItem("knife", 1000)
        addItem("spade", 1000)
        addItem("bucket", 1000)
        addItem("vial", 1000)
        addItem("empty_vial", 1000)
        addItem("bowl", 1000)
        addItem("pot", 1000)
        addItem("jug", 1000)
    }

    init {
        // Spawn shopkeeper in Varrock center (stationary, no walking)
        spawnNpc(shopkeeper, 3218, 3425, 0, 0, Direction.SOUTH)

        // Create the supplies shop
        createShop(
            name = "Varrock Supplies Shop",
            currency = CoinCurrency(),
            stockSize = 100,  // Increased to accommodate all teleport tabs
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

    fun Player.shop() = this.openShop("Varrock Supplies Shop")

    suspend fun QueueTask.dialog(player: Player) {
        chatNpc(player, "Welcome to the Varrock Supplies Shop! We have everything an adventurer needs.")
        chatNpc(player, "Would you like to see what we have for sale?")

        when (options(player, *dialogOptions.toTypedArray())) {
            1 -> player.shop()
            2 -> chatPlayer(player, "No thanks.")
        }
    }
}

