package org.alter.plugins.content.skills.agility.shortcuts

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*

/**
 * Plugin to handle agility shortcuts (hop, cross, jump actions).
 * @author Auto-generated
 */
class AgilityShortcutPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        loadService(AgilityShortcutService())

        onWorldInit {
            world.getService(AgilityShortcutService::class.java)?.let { service ->
                service.shortcuts.forEach { shortcut ->
                    // Check if the object has the specified option before registering
                    val objDef = getObject(shortcut.objectId)
                    val hasOption = objDef.actions.any { it?.lowercase() == shortcut.option.lowercase() }
                    
                    if (hasOption) {
                        onObjOption(obj = shortcut.objectId, option = shortcut.option) {
                            val obj = player.getInteractingGameObj()
                            player.crossShortcut(obj, shortcut)
                        }
                    }
                }
            }
        }
    }

    fun Player.crossShortcut(obj: GameObject, shortcut: AgilityShortcut) {
        queue {
            // Check agility level
            val agilityLevel = getSkills().getCurrentLevel(Skills.AGILITY)
            if (agilityLevel < shortcut.requiredLevel) {
                message("You need an Agility level of ${shortcut.requiredLevel} to use this shortcut.")
                return@queue
            }

            // Calculate end tile
            val endTile = if (shortcut.useRelativePosition) {
                Tile(
                    obj.tile.x + shortcut.endTileX,
                    obj.tile.z + shortcut.endTileZ,
                    shortcut.endTileHeight ?: obj.tile.height
                )
            } else {
                Tile(
                    shortcut.endTileX,
                    shortcut.endTileZ,
                    shortcut.endTileHeight ?: obj.tile.height
                )
            }

            // Calculate direction angle if not provided
            val directionAngle = shortcut.directionAngle ?: Direction.between(tile, endTile).angle

            // Create forced movement
            val movement = ForcedMovement.of(
                src = tile,
                dst = endTile,
                clientDuration1 = shortcut.clientDuration1,
                clientDuration2 = shortcut.clientDuration2,
                directionAngle = directionAngle
            )

            // Play animation and sound
            animate(shortcut.animationId)
            shortcut.soundId?.let { playSound(it) }

            // Move player
            forceMove(this, movement)

            // Award agility XP (optional - can be configured per shortcut)
            // addXp(Skills.AGILITY, 1.0)
        }
    }
}

