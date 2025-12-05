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
            
            // For NPCs that respawn, we need to ensure they can't attack during the respawn delay
            // The NPC will be set to inaccessible in NpcDeathAction, but we add extra protection here
            if (npc.respawns) {
                // Set a very long attack delay to prevent re-engagement during respawn
                npc.timers[org.alter.game.model.timer.ATTACK_DELAY] = 1000
            }
            
            // Queue a task to double-check cleanup after death animation completes
            // This acts as a fallback if the engine's default death cleanup fails.
            world.queue {
                // Wait for death animation to complete (typically 3-5 ticks)
                wait(6)
                
                // Check if the NPC is still spawned and verify it hasn't been removed
                if (npc.isSpawned()) {
                    // Re-check respawns flag as it may have been updated by setNpcDefaults() during reset()
                    val shouldRespawn = npc.combatDef.respawnDelay > 0
                    
                    // If the NPC is not supposed to respawn, force remove it.
                    // If it IS supposed to respawn, NpcDeathAction handles it by resetting/hiding it.
                    if (!shouldRespawn) {
                        // Double-check combat is reset before removal
                        Combat.reset(npc)
                        Combat.resetCombatForTarget(npc)
                        npc.interruptQueues()
                        world.remove(npc)
                    } else {
                        // For respawning NPCs, ensure they're still locked/inaccessible
                        // and can't attack during respawn delay
                        // NOTE: Do NOT interrupt queues here as it would break the respawn wait()
                        if (!npc.isLocked()) {
                            npc.lock()
                        }
                        Combat.reset(npc)
                        npc.timers[org.alter.game.model.timer.ATTACK_DELAY] = 1000
                    }
                }
            }
        }
    }
}
