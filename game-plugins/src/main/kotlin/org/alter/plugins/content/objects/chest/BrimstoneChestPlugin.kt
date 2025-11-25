package org.alter.plugins.content.objects.chest

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.Player
import org.alter.game.model.item.Item
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class BrimstoneChestPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val CLOSED_CHEST = 34660
    private val OPENED_CHEST = 34661
    private val BRIMSTONE_KEY = 23083
    
    // Konar is at 3204, 3427. Placing chest nearby.
    private val CHEST_TILE = Tile(3205, 3427, 0)

    init {
        // Spawn the chest when the world initializes
        onWorldInit {
            if (!world.isSpawned(CLOSED_CHEST, CHEST_TILE.x, CHEST_TILE.z, CHEST_TILE.height)) {
                val chest = DynamicObject(CLOSED_CHEST, 10, 0, CHEST_TILE)
                world.spawn(chest)
            }
        }

        onObjOption(CLOSED_CHEST, option = "unlock") {
            handleChestOpening(player, player.getInteractingGameObj() as DynamicObject)
        }
    }

    private fun handleChestOpening(player: Player, obj: DynamicObject) {
        if (!player.inventory.contains(BRIMSTONE_KEY)) {
            player.message("You need a Brimstone key to unlock this chest.")
            return
        }

        if (player.inventory.remove(BRIMSTONE_KEY, 1).hasSucceeded()) {
            player.animate(832) // Standard opening animation
            player.message("You unlock the chest with your key.")
            
            // Temporarily swap to opened chest
            val openedChest = DynamicObject(OPENED_CHEST, 10, 0, obj.tile)
            world.remove(obj)
            world.spawn(openedChest)
            
            // Give loot
            val loot = generateLoot()
            val transaction = player.inventory.add(item = loot.id, amount = loot.amount)
            if (transaction.hasSucceeded()) {
                player.message("You find some treasure: ${loot.amount} x ${loot.getName()}.")
            } else {
                // Drop on ground if inventory is full
                val groundItem = org.alter.game.model.entity.GroundItem(
                    item = loot.id,
                    amount = loot.amount,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full! The treasure appears on the ground: ${loot.amount} x ${loot.getName()}.")
            }
            
            // Reset chest after a delay
            world.queue {
                wait(2)
                world.remove(openedChest)
                world.spawn(obj)
            }
        }
    }

    private fun generateLoot(): Item {
        val random = world.random(100)
        return when {
            // Super Rare (5%) - High-tier herblore/crafting supplies
            random < 1 -> Item(getRSCM("item.torstol"), world.random(50..100)) // Clean torstol
            random < 2 -> Item(getRSCM("item.magic_logs"), world.random(100..200)) // Magic logs
            random < 3 -> Item(getRSCM("item.diamond"), world.random(20..50)) // Cut diamonds
            random < 4 -> Item(getRSCM("item.dragonstone"), world.random(5..15)) // Cut dragonstones
            random < 5 -> Item(getRSCM("item.onyx"), world.random(1..3)) // Cut onyx
            
            // Rare (15%) - High-value herblore supplies
            random < 8 -> Item(getRSCM("item.ranarr_weed"), world.random(100..200)) // Clean ranarr
            random < 11 -> Item(getRSCM("item.snapdragon"), world.random(50..150)) // Clean snapdragon
            random < 14 -> Item(getRSCM("item.dwarf_weed"), world.random(50..100)) // Clean dwarf weed
            random < 17 -> Item(getRSCM("item.lantadyme"), world.random(50..100)) // Clean lantadyme
            random < 20 -> Item(getRSCM("item.cadantine"), world.random(75..150)) // Clean cadantine
            
            // Uncommon (30%) - Mid-tier supplies
            random < 25 -> Item(getRSCM("item.ruby"), world.random(50..100)) // Cut ruby
            random < 30 -> Item(getRSCM("item.emerald"), world.random(75..150)) // Cut emerald
            random < 35 -> Item(getRSCM("item.sapphire"), world.random(100..200)) // Cut sapphire
            random < 40 -> Item(getRSCM("item.avantoe"), world.random(100..200)) // Clean avantoe
            random < 45 -> Item(getRSCM("item.kwuarm"), world.random(75..150)) // Clean kwuarm
            random < 50 -> Item(getRSCM("item.irit_leaf"), world.random(150..300)) // Clean irit
            
            // Common Herblore (25%) - Potions and secondaries
            random < 55 -> Item(getRSCM("item.vial_of_water"), world.random(200..500)) // Vials of water
            random < 60 -> Item(getRSCM("item.unicorn_horn_dust"), world.random(100..200)) // Unicorn horn dust
            random < 65 -> Item(getRSCM("item.bird_nest"), world.random(50..100)) // Bird's nest (herblore secondary)
            random < 70 -> Item(getRSCM("item.white_berries"), world.random(100..300)) // White berries
            random < 75 -> Item(getRSCM("item.chocolate_dust"), world.random(200..400)) // Chocolate dust
            
            // Common Crafting (25%) - Gems and materials
            random < 80 -> Item(getRSCM("item.gold_bar"), world.random(100..300)) // Gold bars
            random < 85 -> Item(getRSCM("item.silver_bar"), world.random(200..400)) // Silver bars
            random < 90 -> Item(getRSCM("item.leather"), world.random(300..500)) // Leather
            random < 95 -> Item(getRSCM("item.thread"), world.random(100..200)) // Thread
            else -> Item(getRSCM("item.coins_995"), world.random(25000..75000)) // Coins
        }
    }
    
    private fun Item.getName(): String {
        return this.getDef().name
    }
    
    // Helper to check if object is spawned (simplified)
    private fun World.isSpawned(id: Int, x: Int, z: Int, height: Int): Boolean {
        val tile = Tile(x, z, height)
        return this.getObject(tile, id) != null
    }
}
