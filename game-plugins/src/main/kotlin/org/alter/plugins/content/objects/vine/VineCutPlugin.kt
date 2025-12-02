package org.alter.plugins.content.objects.vine

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Vine Cut Plugin
 * 
 * This plugin handles cutting vines (object 21735) to clear paths.
 * Players can cut vines using weapons, similar to how webs work.
 */
class VineCutPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Check what options the vine object has
        try {
            val vineDef = getObject(21735)
            val vineOptions = vineDef.actions.filterNotNull().map { it.lowercase() }
            
            // Handle "cut" option if it exists
            if (vineOptions.contains("cut")) {
                onObjOption(obj = 21735, option = "cut") {
                    val obj = player.getInteractingGameObj()
                    player.queue { cutVine(this, player, obj) }
                }
            }
            
            // Handle "slash" option if it exists (alternative option name)
            if (vineOptions.contains("slash")) {
                onObjOption(obj = 21735, option = "slash") {
                    val obj = player.getInteractingGameObj()
                    player.queue { cutVine(this, player, obj) }
                }
            }
        } catch (e: Exception) {
            // Object 21735 might not exist in cache, that's okay - plugin will still load
        }
    }

    private suspend fun cutVine(task: QueueTask, player: Player, obj: GameObject) {
        // Check if player has a weapon equipped (any weapon should work)
        val weapon = player.equipment[3] // Slot 3 is weapon slot
        if (weapon == null) {
            player.message("You need a weapon to cut these vines.")
            return
        }

        // Play slash animation (same as web cutting)
        player.animate(Animation.THICK_WEB_SLASH)
        
        // Wait for animation to complete (animation is typically 1-2 ticks)
        task.wait(2)
        
        // Remove the vines (or replace with cut version if object 21736 exists)
        world.remove(obj)
        
        player.message("You cut through the vines.")
    }
}



