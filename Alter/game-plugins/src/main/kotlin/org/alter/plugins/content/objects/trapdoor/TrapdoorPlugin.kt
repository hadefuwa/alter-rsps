package org.alter.plugins.content.objects.trapdoor

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Plugin to handle trapdoor open/close actions.
 * Trapdoors work similarly to doors - they have closed and opened states.
 * @author Auto-generated
 */
class TrapdoorPlugin(
    r: PluginRepository, world: World,
    server: Server) : KotlinPlugin(r, world, server) {

    init {
        loadService(TrapdoorService())

        onWorldInit {
            world.getService(TrapdoorService::class.java)?.let { service ->
                service.trapdoors.forEach { trapdoor ->
                    // Check if opened trapdoor has "close" option
                    val openedDef = getObject(trapdoor.opened)
                    val hasCloseOption = openedDef.actions.any { it?.lowercase() == "close" }
                    
                    // Check if closed trapdoor has "open" option
                    val closedDef = getObject(trapdoor.closed)
                    val hasOpenOption = closedDef.actions.any { it?.lowercase() == "open" }
                    
                    // Handle closing an open trapdoor (only if the object has "close" option)
                    if (hasCloseOption) {
                        onObjOption(obj = trapdoor.opened, option = "close") {
                            val obj = player.getInteractingGameObj()
                            val newTrapdoor = world.closeTrapdoor(obj, closed = trapdoor.closed)
                            player.playSound(Sound.TRAPDOOR_CLOSE)
                        }
                    }

                    // Handle opening a closed trapdoor (only if the object has "open" option)
                    if (hasOpenOption) {
                        onObjOption(obj = trapdoor.closed, option = "open") {
                            val obj = player.getInteractingGameObj()
                            val newTrapdoor = world.openTrapdoor(obj, opened = trapdoor.opened)
                            player.playSound(Sound.TRAPDOOR_OPEN)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extension function to open a trapdoor.
 * Trapdoors don't move like doors - they just change state in place.
 */
fun World.openTrapdoor(
    obj: GameObject,
    opened: Int = obj.id + 1,
): GameObject {
    val newTrapdoor = DynamicObject(id = opened, type = obj.type, rot = obj.rot, tile = obj.tile)
    remove(obj)
    spawn(newTrapdoor)
    return newTrapdoor
}

/**
 * Extension function to close a trapdoor.
 * Trapdoors don't move like doors - they just change state in place.
 */
fun World.closeTrapdoor(
    obj: GameObject,
    closed: Int = obj.id - 1,
): GameObject {
    val newTrapdoor = DynamicObject(id = closed, type = obj.type, rot = obj.rot, tile = obj.tile)
    remove(obj)
    spawn(newTrapdoor)
    return newTrapdoor
}

