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

class LarrensKeyChestPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    private val CLOSED_CHEST = getRSCM("object.loot_chest") // 43468
    private val OPENED_CHEST = getRSCM("object.loot_chest_43469") // 43469
    private val LARRENS_KEY = getRSCM("item.larrans_key") // 23490
    
    // Chest location at 3206, 3426
    private val CHEST_TILE = Tile(3206, 3426, 0)

    init {
        // Spawn the chest when the world initializes
        onWorldInit {
            if (!world.isSpawned(CLOSED_CHEST, CHEST_TILE.x, CHEST_TILE.z, CHEST_TILE.height)) {
                val chest = DynamicObject(CLOSED_CHEST, 10, 2, CHEST_TILE) // Rotation 2 = EAST
                world.spawn(chest)
            }
        }

        onObjOption(CLOSED_CHEST, option = "Loot") {
            handleChestOpening(player, player.getInteractingGameObj() as DynamicObject)
        }
    }

    private fun handleChestOpening(player: Player, obj: DynamicObject) {
        if (!player.inventory.contains(LARRENS_KEY)) {
            player.message("You need a Larren's key to unlock this chest.")
            return
        }

        if (player.inventory.remove(LARRENS_KEY, 1).hasSucceeded()) {
            player.animate(832) // Standard opening animation
            player.message("You unlock the chest with your Larren's key.")
            
            // Temporarily swap to opened chest (keep same rotation - facing east)
            val openedChest = DynamicObject(OPENED_CHEST, 10, 2, obj.tile) // Rotation 2 = EAST
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
        val random = world.random(1000)
        return when {
            // ==========================================
            // VERY RARE (0-85 = 8.5%) - Ancient Warrior Equipment
            // ==========================================
            
            // Vesta's Equipment (Melee offensive) - All noted
            random < 5 -> Item(getRSCM("item.vestas_chainbody_noted"), 1)
            random < 10 -> Item(getRSCM("item.vestas_plateskirt_noted"), 1)
            random < 15 -> Item(getRSCM("item.vestas_longsword_noted"), 1)
            random < 20 -> Item(getRSCM("item.vestas_spear_noted"), 1)
            
            // Statius's Equipment (Tanky strength) - All noted
            random < 25 -> Item(getRSCM("item.statiuss_full_helm_noted"), 1)
            random < 30 -> Item(getRSCM("item.statiuss_platebody_noted"), 1)
            random < 35 -> Item(getRSCM("item.statiuss_platelegs_noted"), 1)
            random < 40 -> Item(getRSCM("item.statiuss_warhammer_noted"), 1)
            // Note: Corrupted Statius warhammer not found in item definitions, using regular
            
            // Zuriel's Equipment (Magic) - All noted
            random < 45 -> Item(getRSCM("item.zuriels_hood_noted"), 1)
            random < 50 -> Item(getRSCM("item.zuriels_robe_top_noted"), 1)
            random < 55 -> Item(getRSCM("item.zuriels_robe_bottom_noted"), 1)
            random < 60 -> Item(getRSCM("item.zuriels_staff_noted"), 1)
            
            // Morrigan's Equipment (Ranged) - Armor noted, weapons are stackable so no noted version
            random < 65 -> Item(getRSCM("item.morrigans_coif_noted"), 1)
            random < 70 -> Item(getRSCM("item.morrigans_leather_body_noted"), 1)
            random < 75 -> Item(getRSCM("item.morrigans_leather_chaps_noted"), 1)
            random < 80 -> Item(getRSCM("item.morrigans_javelin"), world.random(50..100)) // Stackable, no noted version
            random < 85 -> Item(getRSCM("item.morrigans_throwing_axe"), world.random(50..100)) // Stackable, no noted version
            
            // ==========================================
            // RARE (85-100 = 1.5%) - Dagon'hai Set - All noted
            // ==========================================
            random < 90 -> Item(getRSCM("item.dagonhai_hat_noted"), 1)
            random < 95 -> Item(getRSCM("item.dagonhai_robe_top_noted"), 1)
            random < 100 -> Item(getRSCM("item.dagonhai_robe_bottom_noted"), 1)
            
            // ==========================================
            // UNCOMMON (100-160 = 6%) - Dragon Resources (stackable, no noted versions)
            // ==========================================
            random < 120 -> Item(getRSCM("item.dragon_arrowtips"), world.random(50..150)) // Stackable
            random < 140 -> Item(getRSCM("item.dragon_dart"), world.random(50..150)) // Stackable
            random < 160 -> Item(getRSCM("item.dragon_bolts_unf"), world.random(50..150)) // Stackable
            
            // ==========================================
            // COMMON (160-1000 = 84%) - Regular Resources - All noted
            // ==========================================
            random < 200 -> Item(getRSCM("item.magic_logs_noted"), world.random(100..300))
            random < 240 -> Item(getRSCM("item.yew_logs_noted"), world.random(200..500))
            random < 280 -> Item(getRSCM("item.coal_noted"), world.random(200..500))
            random < 320 -> Item(getRSCM("item.gold_ore_noted"), world.random(100..300))
            random < 360 -> Item(getRSCM("item.mithril_ore_noted"), world.random(50..150))
            random < 400 -> Item(getRSCM("item.adamantite_ore_noted"), world.random(30..100))
            random < 440 -> Item(getRSCM("item.runite_ore_noted"), world.random(10..50))
            random < 480 -> Item(getRSCM("item.pure_essence_noted"), world.random(500..1000))
            random < 520 -> Item(getRSCM("item.snape_grass_noted"), world.random(100..300))
            
            // More common resources (higher chance) - All noted
            random < 600 -> Item(getRSCM("item.magic_logs_noted"), world.random(150..400))
            random < 680 -> Item(getRSCM("item.yew_logs_noted"), world.random(250..600))
            random < 760 -> Item(getRSCM("item.coal_noted"), world.random(250..600))
            random < 840 -> Item(getRSCM("item.gold_ore_noted"), world.random(150..400))
            random < 920 -> Item(getRSCM("item.pure_essence_noted"), world.random(600..1200))
            else -> Item(getRSCM("item.snape_grass_noted"), world.random(150..400))
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

