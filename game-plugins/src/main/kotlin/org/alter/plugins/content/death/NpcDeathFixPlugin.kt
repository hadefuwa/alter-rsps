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
            // This must happen BEFORE the death animation to prevent race conditions
            Combat.reset(npc)
            Combat.resetCombatForTarget(npc)
            
            // Clear all combat-related attributes and timers
            npc.attr.remove(org.alter.game.model.attr.COMBAT_TARGET_FOCUS_ATTR)
            npc.timers.remove(org.alter.game.model.timer.ATTACK_DELAY)
            npc.timers.remove(org.alter.game.model.timer.ACTIVE_COMBAT_TIMER)
            npc.resetFacePawn()
            
            // NOTE: Do NOT call interruptQueues() here as it can interrupt the death/respawn queue
            // The NpcDeathAction already handles queue interruption before queuing the death task.
            // Interrupting here would break the respawn mechanism.
            
            // Queue a task to double-check cleanup after death animation completes
            // This acts as a fallback if the engine's default death cleanup fails.
            // Only check for non-respawning NPCs to avoid interfering with respawn logic
            world.queue {
                // Wait for death animation to complete (typically 3-5 ticks)
                wait(6)
                
                // Check if the NPC is still spawned and verify it hasn't been removed
                if (npc.isSpawned()) {
                    // Re-check respawns flag as it may have been updated by setNpcDefaults() during reset()
                    val shouldRespawn = npc.combatDef.respawnDelay > 0
                    
                    // If the NPC is not supposed to respawn, force remove it.
                    // If it IS supposed to respawn, NpcDeathAction handles it by resetting/hiding it.
                    // We don't interfere with respawning NPCs here to avoid breaking the respawn mechanism.
                    if (!shouldRespawn) {
                        // Double-check combat is reset before removal
                        Combat.reset(npc)
                        Combat.resetCombatForTarget(npc)
                        npc.interruptQueues()
                        world.remove(npc)
                    }
                    // NOTE: For respawning NPCs, we let NpcDeathAction handle everything.
                    // The timers will be cleared in NpcDeathAction after respawn completes.
                }
            }
        }
    }
}
