package org.alter.plugins.content.objects.deathsdomain

import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.prepareForTeleport

/**
 * Death's Domain Plugin
 * 
 * This plugin handles the Death's Domain object (39547) interaction,
 * allowing players to enter Death's Domain.
 * 
 * Death's Domain is a special area where players can interact with Death
 * and reclaim items after dying.
 * 
 * Object: deaths_domain_39547 (object 39547)
 * Death's Domain Location: 3200, 3200 (typical location)
 */
class DeathsDomainPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Death's Domain entrance coordinates
         * This is where players teleport to when entering Death's Domain
         */
        private val DEATHS_DOMAIN_LOCATION = Tile(x = 3200, z = 3200, height = 0)
    }

    init {
        // Function to teleport player to Death's Domain
        val enterDeathsDomain: Plugin.() -> Unit = {
            player.queue(TaskPriority.STRONG) {
                // Check if player can teleport
                if (!player.lock.canTeleport()) {
                    player.message("You cannot teleport right now.")
                    return@queue
                }
                
                player.message("You enter Death's Domain...")
                player.animate(827) // Climb down animation (similar to entering a coffin)
                
                // Wait for animation
                wait(2)
                
                // Teleport to Death's Domain
                player.prepareForTeleport()
                player.moveTo(DEATHS_DOMAIN_LOCATION)
                player.message("You find yourself in Death's Domain.")
            }
        }

        // Handle Death's Domain object (39547) - check what options it has
        // Common interaction options for Death's Domain
        val domainOptions = listOf("enter", "teleport", "use", "operate", "climb-into", "climb-into-coffin", "climb-down")
        
        domainOptions.forEach { option ->
            // Try using RSCM name first
            try {
                if (objHasOption("object.deaths_domain_39547", option)) {
                    onObjOption(obj = "object.deaths_domain_39547", option = option, logic = enterDeathsDomain)
                }
            } catch (e: Exception) {
                // Option might not exist, continue
            }
            
            // Also handle by object ID directly (using RSCM name)
            try {
                if (objHasOption("object.deaths_domain_39547", option)) {
                    onObjOption(obj = "object.deaths_domain_39547", option = option, logic = enterDeathsDomain)
                }
            } catch (e: Exception) {
                // Option might not exist, that's okay
            }
        }

        // Handle examine option
        try {
            if (objHasOption("object.deaths_domain_39547", "examine")) {
                onObjOption(obj = "object.deaths_domain_39547", option = "examine") {
                    player.message("It looks like a coffin, but somehow different...")
                }
            }
            
            // Also handle examine with RSCM name (already handled above, but keeping for consistency)
        } catch (e: Exception) {
            // Examine option might not exist
        }
    }
}

