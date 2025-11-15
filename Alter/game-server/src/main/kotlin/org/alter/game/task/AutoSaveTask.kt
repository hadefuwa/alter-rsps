package org.alter.game.task

import io.github.oshai.kotlinlogging.KotlinLogging
import org.alter.game.model.World
import org.alter.game.model.entity.Client
import org.alter.game.saving.PlayerSaving
import org.alter.game.service.GameService

/**
 * A [GameTask] responsible for periodically auto-saving all online players.
 * This ensures player data is not lost in case of server crashes or unexpected disconnections.
 *
 * Auto-save interval is configurable via GameContext (default: every 100 game cycles / 1 minute at 600ms cycle time)
 */
class AutoSaveTask(
    /**
     * How often to auto-save, in game cycles.
     * Examples (at 600ms per cycle):
     * - 50 cycles = 30 seconds
     * - 100 cycles = 60 seconds (1 minute) - DEFAULT
     * - 200 cycles = 120 seconds (2 minutes)
     * - 300 cycles = 180 seconds (3 minutes)
     */
    private val autoSaveInterval: Int = 100
) : GameTask {

    private var cyclesSinceLastSave = 0

    override fun execute(world: World, service: GameService) {
        // Skip auto-save if disabled (interval = 0)
        if (autoSaveInterval <= 0) {
            logger.warn { "Auto-save is DISABLED (interval = 0). Player positions will NOT be saved automatically!" }
            return
        }

        cyclesSinceLastSave++

        // Only auto-save every [autoSaveInterval] cycles
        if (cyclesSinceLastSave >= autoSaveInterval) {
            performAutoSave(world)
            cyclesSinceLastSave = 0
        }
    }

    private fun performAutoSave(world: World) {
        var savedCount = 0
        var errorCount = 0

        val playerCount = world.players.count { it.initiated && it is Client }
        
        if (playerCount == 0) {
            return // No players to save
        }

        world.players.forEach { player ->
            // Only save fully initialized players (skip players still logging in)
            if (!player.initiated || player !is Client) {
                return@forEach
            }

            try {
                // Save player data including current position
                val positionBeforeSave = player.tile
                PlayerSaving.savePlayer(player)
                savedCount++
                // Log position save for debugging (log first save and every 5th to avoid spam)
                if (savedCount == 1 || savedCount % 5 == 0) {
                    logger.info { "Auto-saved player '${player.username}' at position (${positionBeforeSave.x}, ${positionBeforeSave.z}, ${positionBeforeSave.height})" }
                }
            } catch (e: Exception) {
                errorCount++
                logger.error(e) { "Failed to auto-save player '${player.username}': ${e.message}" }
            }
        }

        if (savedCount > 0) {
            logger.info { "Auto-save completed: $savedCount player(s) saved${if (errorCount > 0) ", $errorCount error(s)" else ""}" }
        } else if (playerCount > 0) {
            logger.warn { "Auto-save attempted but no players were saved (${playerCount} players online)" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
