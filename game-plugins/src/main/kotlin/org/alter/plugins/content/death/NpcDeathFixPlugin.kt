package org.alter.plugins.content.death

import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.Combat

/**
 * Fixes an issue where NPCs are not properly removed from the world after death,
 * causing them to become "invisible" attackers that can still damage players.
 */
class NpcDeathFixPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onAnyNpcDeath {
            val npc = ctx as Npc
            
            // Force stop combat immediately to prevent the NPC from attacking while dying
            Combat.reset(npc)
            Combat.resetCombatForTarget(npc)
            
            // Prevent the NPC from attacking again during the death animation
            // We set a long attack delay to ensure it doesn't re-engage
            npc.timers[org.alter.game.model.timer.ATTACK_DELAY] = 100
            npc.resetFacePawn()
            
            // Queue a task to force remove the NPC after a short delay.
            // This acts as a fallback if the engine's default death cleanup fails.
            // We wait 5 ticks (3 seconds) to allow the death animation to play.
            world.queue {
                wait(5)
                // Check if the NPC is still spawned
                if (npc.isSpawned()) {
                    // If the NPC is not supposed to respawn, force remove it.
                    // If it IS supposed to respawn, NpcDeathAction handles it by resetting/hiding it.
                    // Removing it here would break the respawn cycle.
                    if (!npc.respawns) {
                        world.remove(npc)
                    }
                }
            }
        }
    }
}
