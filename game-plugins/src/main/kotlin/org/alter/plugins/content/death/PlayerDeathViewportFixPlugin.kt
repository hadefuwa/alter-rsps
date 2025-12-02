package org.alter.plugins.content.death

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Player Death Viewport Fix Plugin
 * 
 * Fixes an issue where objects (like portal nexus) become invisible after player death.
 * This happens because after teleporting to home, the chunk updates aren't properly sent.
 * 
 * Solution: Clear lastKnownRegionBase after death to force a region rebuild on the next cycle,
 * which will send all chunk updates including objects.
 */
class PlayerDeathViewportFixPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Handle player death to fix viewport/object visibility
        onPlayerDeath {
            val player = this.player
            
            // Clear lastKnownRegionBase to force a region rebuild on the next cycle
            // This ensures all chunks and objects are properly sent to the client
            player.lastKnownRegionBase = null
            
            // The region will be rebuilt in the next cycle by SynchronizationTask.playerPreSynchronizationTask()
            // which will send all chunk updates including objects like portal nexus
        }
    }
}





