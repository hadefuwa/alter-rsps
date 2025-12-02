package org.alter.plugins.content.objects.web

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Web Slash Plugin
 * 
 * This plugin handles slashing webs (object 733) to create slashed webs (object 734).
 * Players can slash webs using weapons to clear paths.
 */
class WebSlashPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Check what options the web object has
        try {
            val webDef = getObject(733)
            val webOptions = webDef.actions.filterNotNull().map { it.lowercase() }
            
            // Handle "slash" option if it exists
            if (webOptions.contains("slash")) {
                onObjOption(obj = 733, option = "slash") {
                    val obj = player.getInteractingGameObj()
                    player.queue { slashWeb(this, player, obj) }
                }
            }
            
            // Handle "cut" option if it exists (alternative option name)
            if (webOptions.contains("cut")) {
                onObjOption(obj = 733, option = "cut") {
                    val obj = player.getInteractingGameObj()
                    player.queue { slashWeb(this, player, obj) }
                }
            }
        } catch (e: Exception) {
            // Object 733 might not exist in cache, that's okay - plugin will still load
        }
    }

    private suspend fun slashWeb(task: QueueTask, player: Player, obj: GameObject) {
        // Check if player has a weapon equipped (any weapon should work)
        val weapon = player.equipment[3] // Slot 3 is weapon slot
        if (weapon == null) {
            player.message("You need a weapon to slash this web.")
            return
        }

        // Play slash animation
        player.animate(Animation.THICK_WEB_SLASH)
        
        // Wait for animation to complete (animation is typically 1-2 ticks)
        task.wait(2)
        
        // Replace the web with a slashed web (object 734)
        val slashedWeb = DynamicObject(
            id = 734, // slashed_web
            type = obj.type,
            rot = obj.rot,
            tile = obj.tile
        )
        
        world.remove(obj)
        world.spawn(slashedWeb)
        
        player.message("You slash through the web.")
    }
}

