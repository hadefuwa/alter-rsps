package org.alter.plugins.content.areas.varrock

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.move.moveTo
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Varrock Manhole Plugin
 * 
 * This plugin handles the Varrock manhole (object ID 881) interaction,
 * allowing players to enter the Varrock sewers.
 * 
 * The manhole is located east of Varrock Palace and provides access
 * to the sewer system beneath the city.
 * 
 * @param r The plugin repository for registering object interactions
 * @param world The game world instance
 * @param server The server instance
 */
class VarrockManholePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Varrock Sewer entrance coordinates (height 0, underground)
        val sewerEntrance = Tile(x = 3235, z = 9870, height = 0)

        // Function to teleport player to sewers
        val enterSewers: Plugin.() -> Unit = {
            player.queue {
                player.message("You climb down into the sewer.")
                player.animate(827) // Climb down animation
                wait(2)
                player.moveTo(sewerEntrance)
                player.message("You find yourself in the Varrock sewers.")
            }
        }

        // Handle closed manhole (object 881) - check what options it has
        try {
            val closedManholeDef = getObject(881)
            val closedOptions = closedManholeDef.actions.filterNotNull().map { it.lowercase() }
            
            // If manhole has "open" option, handle opening it first
            if (closedOptions.contains("open")) {
                onObjOption(obj = 881, option = "open") {
                    val obj = player.getInteractingGameObj()
                    // Open the manhole (change to object 882)
                    val openedManhole = DynamicObject(id = 882, type = obj.type, rot = obj.rot, tile = obj.tile)
                    world.remove(obj)
                    world.spawn(openedManhole)
                    player.playSound(Sound.MANHOLE_OPEN)
                    player.message("You open the manhole cover.")
                }
            }

            // If manhole has "climb-down" or "enter" option, handle direct entry
            if (closedOptions.contains("climb-down")) {
                onObjOption(obj = 881, option = "climb-down", logic = enterSewers)
            }
            
            if (closedOptions.contains("enter")) {
                onObjOption(obj = 881, option = "enter", logic = enterSewers)
            }
        } catch (e: Exception) {
            // Object 881 might not exist in cache, that's okay - plugin will still load
        }

        // Handle open manhole (object 882) if it exists
        try {
            val openManholeDef = getObject(882)
            val openOptions = openManholeDef.actions.filterNotNull().map { it.lowercase() }
            
            if (openOptions.contains("climb-down")) {
                onObjOption(obj = 882, option = "climb-down", logic = enterSewers)
            }
            
            if (openOptions.contains("enter")) {
                onObjOption(obj = 882, option = "enter", logic = enterSewers)
            }
        } catch (e: Exception) {
            // Object 882 might not exist, that's okay
        }
    }
}

