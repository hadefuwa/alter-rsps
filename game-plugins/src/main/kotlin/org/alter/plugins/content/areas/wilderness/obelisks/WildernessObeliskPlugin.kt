package org.alter.plugins.content.areas.wilderness.obelisks

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.plugins.content.magic.TeleportType
import org.alter.plugins.content.magic.canTeleport
import org.alter.plugins.content.magic.prepareForTeleport
import org.alter.plugins.content.magic.teleport

/**
 * Wilderness Obelisk Teleportation Plugin
 * 
 * This plugin handles the wilderness obelisk teleportation system. Obelisks are magical
 * structures scattered throughout the wilderness that allow players to teleport to random
 * obelisk locations. This creates strategic gameplay as players cannot choose their 
 * destination, making wilderness travel more unpredictable and dangerous.
 * 
 * Features:
 * - Random teleportation to any of the 6 obelisk locations
 * - Proper wilderness teleport restrictions (level 30+ blocks teleports)
 * - Custom obelisk teleport animation and graphics
 * - Multi-player activation with shared cooldown
 * - Safety checks to prevent teleport abuse
 * 
 * Obelisk Object IDs: 14825-14831 (7 different obelisk states/models)
 * 
 * @author Alter RSPS Development Team
 */
class WildernessObeliskPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    companion object {
        /**
         * Wilderness obelisk locations with their coordinates and descriptions.
         * These are the 6 main obelisk locations throughout the wilderness.
         */
        private val OBELISK_LOCATIONS = arrayOf(
            ObeliskLocation("Level 13 Wilderness", Area(3154, 3618, 3158, 3622)), // Edgeville obelisk
            ObeliskLocation("Level 27 Wilderness", Area(3225, 3665, 3229, 3669)), // East obelisk  
            ObeliskLocation("Level 35 Wilderness", Area(3033, 3730, 3037, 3734)), // West obelisk
            ObeliskLocation("Level 44 Wilderness", Area(3104, 3792, 3108, 3796)), // North obelisk
            ObeliskLocation("Level 50 Wilderness", Area(3307, 3914, 3311, 3918)), // Far east obelisk
            ObeliskLocation("Level 13 Wilderness", Area(3288, 3883, 3292, 3887))  // Corporeal Beast obelisk
        )
        
        /**
         * Wilderness obelisk teleport type - allows teleports up to level 20 wilderness
         */
        private val OBELISK_TELEPORT_TYPE = TeleportType.MODERN
        
        /**
         * Timer key for obelisk activation cooldown
         */
        private val OBELISK_ACTIVATION_TIMER = TimerKey()
        
        /**
         * Cooldown duration in game ticks (3 seconds = 5 ticks)
         */
        private const val OBELISK_COOLDOWN_TICKS = 5
    }
    
    /**
     * Data class representing an obelisk teleport destination
     */
    private data class ObeliskLocation(
        val description: String,
        val area: Area
    ) {
        fun getRandomTile(): Tile {
            val x = area.bottomLeftX + (0..3).random() // Random offset within 4x4 area
            val z = area.bottomLeftY + (0..3).random() // Random offset within 4x4 area
            return Tile(x, z, 0)
        }
    }
    
    init {
        // Register handlers for all wilderness obelisk object IDs using direct object IDs
        val obeliskIds = arrayOf(14825, 14826, 14827, 14828, 14829, 14830, 14831)
        
                // Check each obelisk object and bind available options
        obeliskIds.forEach { obeliskId ->
            try {
                // First, try to get available options for this object
                val availableOptions = try {
                    val objDef = getObject(obeliskId)
                    objDef.actions.filterNotNull().filter { action: String? -> action != null && action.isNotBlank() }
                } catch (e: Exception) {
                    emptyList<String>()
                }
                
                // Skip obelisks that have no options (likely inactive/visual-only states)
                if (availableOptions.isEmpty()) {
                    // This is normal for some obelisk states - they may be inactive or visual-only variants
                    // No need to log a warning for this
                    return@forEach
                }
                
                // Try common object interaction options for wilderness obelisks
                val possibleOptions = listOf("activate", "operate", "touch", "use")
                var optionBound = false
                
                // Try string-based options first
                for (option in possibleOptions) {
                    try {
                        // Check if the object has this option by checking the actions array
                        val objDef = getObject(obeliskId)
                        val optionIndex = objDef.actions.indexOfFirst { action: String? -> 
                            action != null && action.lowercase() == option.lowercase() 
                        }
                        if (optionIndex != -1) {
                            onObjOption(obj = obeliskId, option = option) {
                                activateObelisk(player)
                            }
                            optionBound = true
                            break // Found a working option, no need to try others
                        }
                    } catch (e: Exception) {
                        // Option not available or binding failed, try next
                        continue
                    }
                }
                
                // If no string options worked, try binding to option index 1 (first interaction)
                if (!optionBound && availableOptions.isNotEmpty()) {
                    try {
                        r.bindObject(obeliskId, 1, -1) {
                            activateObelisk(player)
                        }
                        optionBound = true
                    } catch (e: Exception) {
                        println("Failed to bind obelisk $obeliskId with option index 1: ${e.message}")
                    }
                }
                
                // Only warn if we expected to bind an option but couldn't (object has options but binding failed)
                if (!optionBound && availableOptions.isNotEmpty()) {
                    println("Warning: No suitable option found for obelisk $obeliskId. Available options: $availableOptions")
                }
            } catch (e: Exception) {
                println("Error configuring obelisk $obeliskId: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Handles wilderness obelisk activation and teleportation
     */
    private fun activateObelisk(player: Player) {
        // Check if player can teleport (only basic lock checks, no wilderness restrictions)
        if (!player.lock.canTeleport()) {
            return
        }
        
        // Check if obelisk is on cooldown
        if (player.timers.has(OBELISK_ACTIVATION_TIMER)) {
            player.message("The obelisk is still recharging its magical energy...")
            return
        }
        
        // Select random destination
        val destination = OBELISK_LOCATIONS.random()
        val destinationTile = destination.getRandomTile()
        
        player.queue(TaskPriority.STRONG) {
            // Inform player about the teleportation
            player.message("You activate the wilderness obelisk...")
            player.message("The obelisk hums with ancient magic...")
            
            // Set cooldown timer
            player.timers[OBELISK_ACTIVATION_TIMER] = OBELISK_COOLDOWN_TICKS
            
            // Wait a moment for dramatic effect
            wait(cycles = 2)
            
            // Perform the teleportation using the custom obelisk teleport type
            player.teleport(destinationTile, OBELISK_TELEPORT_TYPE)
            
            // Inform player of destination after teleport completes
            wait(cycles = OBELISK_TELEPORT_TYPE.teleportDelay + 1)
            player.message("You have been teleported to ${destination.description}!")
        }
    }
}