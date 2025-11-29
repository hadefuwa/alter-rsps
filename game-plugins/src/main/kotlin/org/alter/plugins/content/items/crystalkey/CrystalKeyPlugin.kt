package org.alter.plugins.content.items.crystalkey

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.OTHER_ITEM_SLOT_ATTR
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.Player
import org.alter.game.model.item.Item
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class CrystalKeyPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val TOOTH_HALF = getRSCM("item.tooth_half_of_key") // 985
    private val LOOP_HALF = getRSCM("item.loop_half_of_key") // 987
    private val CRYSTAL_KEY = getRSCM("item.crystal_key") // 989
    
    private val CLOSED_CHEST = 172 // Standard closed chest
    private val OPENED_CHEST = 173 // Standard open chest
    
    private val CHEST_TILE = Tile(3204, 3430, 0)

    init {
        // Combine the two key halves to create a crystal key
        onItemOnItem(item1 = "item.tooth_half_of_key", item2 = "item.loop_half_of_key") {
            val sourceSlot = player.getInteractingItemSlot()
            val targetSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: return@onItemOnItem
            
            val source = player.inventory[sourceSlot] ?: return@onItemOnItem
            val target = player.inventory[targetSlot] ?: return@onItemOnItem
            
            // Verify we have both halves (one must be tooth, one must be loop)
            val hasTooth = source.id == TOOTH_HALF || target.id == TOOTH_HALF
            val hasLoop = source.id == LOOP_HALF || target.id == LOOP_HALF
            
            if (!hasTooth || !hasLoop) {
                return@onItemOnItem
            }
            
            // Remove both halves
            player.inventory.remove(TOOTH_HALF, 1)
            player.inventory.remove(LOOP_HALF, 1)
            
            // Add crystal key
            val transaction = player.inventory.add(CRYSTAL_KEY, 1)
            if (transaction.hasSucceeded()) {
                player.message("You join the two halves of the key together.")
                player.message("You now have a complete Crystal key.")
            } else {
                // If inventory is full, drop the key on the ground
                val groundItem = org.alter.game.model.entity.GroundItem(
                    item = CRYSTAL_KEY,
                    amount = 1,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full! The crystal key appears on the ground.")
            }
        }
        
        // Spawn the crystal chest when the world initializes
        onWorldInit {
            if (!world.isSpawned(CLOSED_CHEST, CHEST_TILE.x, CHEST_TILE.z, CHEST_TILE.height)) {
                val chest = DynamicObject(CLOSED_CHEST, 10, 0, CHEST_TILE)
                world.spawn(chest)
            }
        }

        // Handle opening the crystal chest
        onObjOption(CLOSED_CHEST, option = "Open") {
            handleChestOpening(player, player.getInteractingGameObj() as DynamicObject)
        }
    }

    private fun handleChestOpening(player: Player, obj: DynamicObject) {
        if (!player.inventory.contains(CRYSTAL_KEY)) {
            player.message("You need a Crystal key to unlock this chest.")
            return
        }

        if (player.inventory.remove(CRYSTAL_KEY, 1).hasSucceeded()) {
            player.animate(832) // Standard opening animation
            player.message("You unlock the chest with your Crystal key.")
            
            // Temporarily swap to opened chest
            val openedChest = DynamicObject(OPENED_CHEST, 10, 0, obj.tile)
            world.remove(obj)
            world.spawn(openedChest)
            
            // Give loot
            val loot = generateLoot()
            // Convert to noted version (except coins which can't be noted)
            val coinsItemId = getRSCM("item.coins_995")
            val finalLoot = if (loot.id != coinsItemId) {
                loot.toNoted()
            } else {
                loot
            }
            
            val transaction = player.inventory.add(item = finalLoot.id, amount = finalLoot.amount)
            if (transaction.hasSucceeded()) {
                player.message("You find some treasure: ${finalLoot.amount} x ${finalLoot.getName()}.")
            } else {
                // Drop on ground if inventory is full
                val groundItem = org.alter.game.model.entity.GroundItem(
                    item = finalLoot.id,
                    amount = finalLoot.amount,
                    tile = player.tile,
                    owner = player
                )
                groundItem.timeUntilPublic = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE
                groundItem.timeUntilDespawn = org.alter.game.model.timer.TimeConstants.CYCLES_PER_MINUTE * 4
                groundItem.ownerShipType = 1
                world.spawn(groundItem)
                player.message("Your inventory is full! The treasure appears on the ground: ${finalLoot.amount} x ${finalLoot.getName()}.")
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
            // Super Rare (5%) - High-tier items
            random < 1 -> Item(getRSCM("item.dragonstone"), world.random(10..50))
            random < 2 -> Item(getRSCM("item.onyx"), world.random(10..50))
            random < 3 -> Item(getRSCM("item.magic_logs"), world.random(10..50))
            random < 4 -> Item(getRSCM("item.torstol"), world.random(10..50))
            random < 5 -> Item(getRSCM("item.diamond"), world.random(10..50))
            
            // Rare (15%) - High-value items
            random < 8 -> Item(getRSCM("item.ranarr_weed"), world.random(10..50))
            random < 11 -> Item(getRSCM("item.snapdragon"), world.random(10..50))
            random < 14 -> Item(getRSCM("item.dwarf_weed"), world.random(10..50))
            random < 17 -> Item(getRSCM("item.lantadyme"), world.random(10..50))
            random < 20 -> Item(getRSCM("item.cadantine"), world.random(10..50))
            
            // Uncommon (30%) - Mid-tier items
            random < 25 -> Item(getRSCM("item.ruby"), world.random(10..50))
            random < 30 -> Item(getRSCM("item.emerald"), world.random(10..50))
            random < 35 -> Item(getRSCM("item.sapphire"), world.random(10..50))
            random < 40 -> Item(getRSCM("item.avantoe"), world.random(10..50))
            random < 45 -> Item(getRSCM("item.kwuarm"), world.random(10..50))
            random < 50 -> Item(getRSCM("item.irit_leaf"), world.random(10..50))
            
            // Common (50%) - Standard items
            random < 55 -> Item(getRSCM("item.vial_of_water"), world.random(10..50))
            random < 60 -> Item(getRSCM("item.unicorn_horn_dust"), world.random(10..50))
            random < 65 -> Item(getRSCM("item.bird_nest"), world.random(10..50))
            random < 70 -> Item(getRSCM("item.white_berries"), world.random(10..50))
            random < 75 -> Item(getRSCM("item.chocolate_dust"), world.random(10..50))
            random < 80 -> Item(getRSCM("item.gold_bar"), world.random(10..50))
            random < 85 -> Item(getRSCM("item.silver_bar"), world.random(10..50))
            random < 90 -> Item(getRSCM("item.leather"), world.random(10..50))
            random < 95 -> Item(getRSCM("item.thread"), world.random(10..50))
            else -> Item(getRSCM("item.coins_995"), world.random(100000..500000))
        }
    }
    
    private fun Item.getName(): String {
        return this.getDef().name
    }
    
    // Helper to check if object is spawned
    private fun World.isSpawned(id: Int, x: Int, z: Int, height: Int): Boolean {
        val tile = Tile(x, z, height)
        return this.getObject(tile, id) != null
    }
}

