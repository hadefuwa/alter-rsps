package org.alter.plugins.content.areas.wilderness

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

/**
 * Wilderness Ambient Sound Plugin
 * 
 * This plugin provides continuous background ambient sound effects for the wilderness area.
 * It plays wind and atmospheric sounds to enhance the wilderness experience and create
 * a more immersive environment.
 * 
 * Features:
 * - Plays wilderness wind sounds periodically while players are in wilderness
 * - Uses area sounds so all nearby players can hear the ambient effects
 * - Automatically stops when players leave wilderness
 * 
 * Sound Effects Used:
 * - WILDERNESS_WIND_1 (2179): Primary wilderness wind sound
 * - WILDERNESS_WIND_2 (2180): Secondary wilderness wind sound
 * - BLUSTERY_WIND_LOOP_1 (2182): Continuous blustery wind loop
 * - BLUSTERY_WIND_LOOP_2 (2183): Alternative blustery wind loop
 * - STRONG_WIND_LOOP_1 (2188): Strong wind loop for higher wilderness levels
 * 
 * @param r The plugin repository for registering handlers
 * @param world The game world instance
 * @param server The server instance
 */
class WildernessAmbientSoundPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Timer key for the wilderness ambient sound update cycle.
         * This timer runs periodically to check all players and play sounds.
         */
        private val WILDERNESS_SOUND_TIMER = TimerKey()
        
        /**
         * How often to check for players in wilderness and play sounds (in game cycles).
         * At 600ms per cycle:
         * - 50 cycles = 30 seconds
         * - 100 cycles = 60 seconds (1 minute)
         * - 150 cycles = 90 seconds (1.5 minutes)
         * 
         * Using 100 cycles (60 seconds) since loop sounds should continue playing on the client.
         * This ensures sounds are refreshed periodically without creating too many overlapping sounds.
         */
        private const val SOUND_CHECK_INTERVAL = 100
        
        /**
         * Radius for area sounds (in tiles).
         * Players within this radius will hear the ambient sound.
         */
        private const val SOUND_RADIUS = 15
        
        /**
         * Volume for area sounds (0-255, where 1 is standard volume).
         */
        private const val SOUND_VOLUME = 1
    }
    
    init {
        // Initialize the world timer on server startup
        onWorldInit {
            // Start the timer that will periodically check for players in wilderness
            world.timers[WILDERNESS_SOUND_TIMER] = SOUND_CHECK_INTERVAL
        }
        
        // Handle the timer - this runs every SOUND_CHECK_INTERVAL cycles
        onTimer(WILDERNESS_SOUND_TIMER) {
            // Check all online players
            world.players.forEach { player ->
                // Only process initiated players (logged in)
                if (!player.initiated) return@forEach
                
                // Check if player is in wilderness
                val wildernessLevel = player.tile.getWildernessLevel()
                
                if (wildernessLevel > 0) {
                    // Player is in wilderness - play ambient sounds
                    playWildernessAmbientSounds(player, wildernessLevel)
                }
            }
            
            // Reset the timer for the next check
            world.timers[WILDERNESS_SOUND_TIMER] = SOUND_CHECK_INTERVAL
        }
    }
    
    /**
     * Plays appropriate wilderness ambient sounds based on the player's location.
     * 
     * @param player The player to play sounds for
     * @param wildernessLevel The wilderness level (1-56)
     */
    private fun playWildernessAmbientSounds(player: Player, wildernessLevel: Int) {
        val playerTile = player.tile
        
        // Play different sounds based on wilderness level for variety
        // Lower wilderness (1-20): Gentler wind sounds
        // Mid wilderness (21-40): Moderate wind sounds
        // High wilderness (41-56): Strong wind sounds
        
        when {
            wildernessLevel <= 20 -> {
                // Lower wilderness: Play gentle blustery wind loop
                // Using loop sound so it continues playing on the client
                world.spawn(AreaSound(playerTile, Sound.BLUSTERY_WIND_LOOP_1, SOUND_RADIUS, SOUND_VOLUME))
            }
            
            wildernessLevel <= 40 -> {
                // Mid wilderness: Play moderate blustery wind loop
                world.spawn(AreaSound(playerTile, Sound.BLUSTERY_WIND_LOOP_2, SOUND_RADIUS, SOUND_VOLUME))
            }
            
            else -> {
                // High wilderness (41-56): Play strong wind loop for more intense atmosphere
                world.spawn(AreaSound(playerTile, Sound.STRONG_WIND_LOOP_1, SOUND_RADIUS, SOUND_VOLUME))
            }
        }
    }
}

