package org.alter.plugins.content.objects.nexusportal

import dev.openrune.cache.CacheManager.getObject
import net.rsprot.protocol.game.incoming.resumed.ResumePauseButton
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.*
import org.alter.plugins.content.magic.prepareForTeleport
import org.alter.rscm.RSCM.getRSCM

/**
 * Nexus Portal Teleportation Plugin
 *
 * This plugin creates a Portal Nexus-style teleportation system similar to POH portals.
 * Players can interact with a portal object to access a menu of teleport destinations.
 *
 * Features:
 * - Portal object interaction (right-click)
 * - Paginated teleport menu
 * - Multiple teleport categories
 * - Varrock Centre and other major locations
 *
 * Object IDs used:
 * - 409: Portal (generic portal object)
 * - Can be customized to use other portal object IDs
 */
class NexusPortalPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * All available teleport destinations organized by category
         */
        private val TELEPORT_LOCATIONS = listOf(
            // Cities & Towns
            "Varrock Centre" to Tile(x = 3213, z = 3428, height = 0),
            "Varrock East Bank" to Tile(x = 3253, z = 3420, height = 0),
            "Varrock West Bank" to Tile(x = 3185, z = 3436, height = 0),
            "Lumbridge" to Tile(x = 3222, z = 3217, height = 0),
            "Falador" to Tile(x = 2966, z = 3379, height = 0),
            "Edgeville" to Tile(x = 3087, z = 3499, height = 0),
            "Ardougne" to Tile(x = 2659, z = 3300, height = 0),
            "Camelot" to Tile(x = 2756, z = 3476, height = 0),
            "Yanille" to Tile(x = 2606, z = 3093, height = 0),
            "Seers' Village" to Tile(x = 2725, z = 3486, height = 0),

            // Desert
            "Al Kharid" to Tile(x = 3293, z = 3174, height = 0),
            "Shantay Pass" to Tile(x = 3304, z = 3116, height = 0),
            "Pollnivneach" to Tile(x = 3350, z = 2964, height = 0),
            "Nardah" to Tile(x = 3426, z = 2914, height = 0),

            // Wilderness
            "Edgeville Wilderness" to Tile(x = 3087, z = 3520, height = 0),
            "Mage Bank" to Tile(x = 2539, z = 4716, height = 0),
            "Lava Dragon Isle" to Tile(x = 3200, z = 3856, height = 0),
            "Resource Area" to Tile(x = 3184, z = 3944, height = 0),

            // Skilling Locations
            "Catherby" to Tile(x = 2804, z = 3433, height = 0),
            "Fishing Guild" to Tile(x = 2611, z = 3391, height = 0),
            "Mining Guild" to Tile(x = 3046, z = 9756, height = 0),
            "Crafting Guild" to Tile(x = 2933, z = 3289, height = 0),

            // Other Locations
            "Draynor Village" to Tile(x = 3093, z = 3244, height = 0),
            "Port Sarim" to Tile(x = 3014, z = 3176, height = 0),
            "Rimmington" to Tile(x = 2954, z = 3214, height = 0),
            "Gnome Stronghold" to Tile(x = 2461, z = 3443, height = 0),
            "Barbarian Village" to Tile(x = 3081, z = 3420, height = 0),
            "Taverly" to Tile(x = 2894, z = 3456, height = 0),
        )
    }

    init {
        // Portal object IDs from RSCM - using nexus portals and magic portals
        val portalObjects = listOf(
            "object.magic_portal",         // 2156
            "object.magic_portal_2157",    // 2157
            "object.portal_4525",          // 4525
            "object.carving",              // 22706 - Gilded Portal Nexus (carving)
            "object.portal_nexus_33354",   // 33354 - Portal Nexus from POH (backup)
        )

        // Common portal interaction options - try all common OSRS portal options
        // Including POH portal nexus options like "Ring-configure"
        val portalOptions = listOf(
            "Ring-configure",      // POH portal nexus configure option
            "configure",           // Standard configure option
            "enter",               // Standard enter option
            "teleport",            // Standard teleport option
            "use",                 // Standard use option
            "operate",             // Standard operate option
            "activate",            // Standard activate option
            "quick-start",         // Quick start option
            "Tree",                // POH portal nexus tree option
            "Ring-Zanaris",        // POH portal nexus ring option
            "Ring-last-destination (AIP)" // POH portal nexus last destination
        )

        portalObjects.forEach { portalId ->
            var optionBound = false

            // First, try to get available options for this object
            val availableOptions = try {
                val objDef = getObject(getRSCM(portalId))
                objDef.actions.filterNotNull().filter { action -> action.length > 0 }
            } catch (e: Exception) {
                emptyList()
            }

            // Try common portal interaction options (similar to obelisk plugin pattern)
            for (option in portalOptions) {
                try {
                    // Check if the object has this option before trying to bind
                    if (objHasOption(portalId, option)) {
                        onObjOption(obj = portalId, option = option) {
                            if (!player.lock.canTeleport()) {
                                player.message("You cannot teleport right now.")
                                return@onObjOption
                            }

                            player.queue(TaskPriority.STRONG) {
                                openTeleportMenu(player)
                            }
                        }
                        println("Successfully bound portal $portalId with option: $option")
                        optionBound = true
                        break // Found a working option, no need to try others
                    }
                } catch (e: Exception) {
                    // Option not available or binding failed, try next
                    continue
                }
            }

            // If no string options worked, try binding to numeric option slots as fallback
            // This handles objects that have options but not standard names
            // For POH portal nexus, option 1 is usually the primary interaction (left-click)
            if (!optionBound) {
                // Try to bind to option slot 1 first (left-click), then others
                for (optionSlot in 1..5) {
                    try {
                        onObjOption(obj = portalId, option = optionSlot) {
                            if (!player.lock.canTeleport()) {
                                player.message("You cannot teleport right now.")
                                return@onObjOption
                            }

                            player.queue(TaskPriority.STRONG) {
                                openTeleportMenu(player)
                            }
                        }
                        println("Successfully bound portal $portalId to option slot $optionSlot")
                        optionBound = true
                        break
                    } catch (e: Exception) {
                        // This option slot doesn't exist, continue to next
                        continue
                    }
                }
            }

            if (!optionBound) {
                println("WARNING: Failed to bind any options for portal $portalId")
                // Try to get available options for debugging
                try {
                    val objDef = getObject(getRSCM(portalId))
                    val availableOptions = objDef.actions.filterNotNull().filter { action -> action.length > 0 }
                    println("Available options for $portalId: $availableOptions")
                } catch (e: Exception) {
                    println("Could not retrieve object definition for $portalId: ${e.message}")
                }
            }

            // Right-click examine
            try {
                if (objHasOption(portalId, "examine")) {
                    onObjOption(obj = portalId, option = "examine") {
                        player.message("A mystical portal that can teleport you to many locations.")
                    }
                }
            } catch (e: Exception) {
                // Examine option not available, skip it
            }
        }
    }

    /**
     * Opens the paginated teleport menu for the player
     * This is a suspend function that runs within a QueueTask context
     * Uses a full-screen interface (187) with the crystalline portal nexus item display
     */
    private suspend fun QueueTask.openTeleportMenu(p: Player) {
        val locationsPerPage = 5
        val totalPages = (TELEPORT_LOCATIONS.size + locationsPerPage - 1) / locationsPerPage
        var currentPage = 0

        while (true) {
            val startIndex = currentPage * locationsPerPage
            val endIndex = minOf(startIndex + locationsPerPage, TELEPORT_LOCATIONS.size)
            val pageLocations = TELEPORT_LOCATIONS.subList(startIndex, endIndex)

            // Build menu options
            val options = mutableListOf<String>()

            // Add navigation
            if (currentPage > 0) {
                options.add("← Previous Page")
            }

            // Add locations
            options.addAll(pageLocations.map { it.first })

            // Add next page
            if (currentPage < totalPages - 1) {
                options.add("Next Page →")
            }

            val title = "Crystalline Portal Nexus (Page ${currentPage + 1}/$totalPages)"

            // Open the full-screen interface menu (interface 187)
            p.openInterface(187, InterfaceDestination.MAIN_SCREEN)

            // The INTERFACE_MENU script may accept an item ID as a third parameter
            // Try passing the crystalline portal nexus item ID (22707)
            p.runClientScript(CommonClientScripts.INTERFACE_MENU, title, options.joinToString("|"), 22707)
            p.setInterfaceEvents(interfaceId = 187, component = 3, from = 0, to = options.size, setting = 1)

            terminateAction = { p.closeInterface(187) }
            waitReturnValue()
            terminateAction!!(this)

            val selected = (requestReturnValue as? ResumePauseButton)?.sub ?: -1

            if (selected < 0) {
                break // Player closed menu
            }

            val selectedOption = options[selected]

            when {
                selectedOption == "← Previous Page" -> {
                    currentPage--
                }
                selectedOption == "Next Page →" -> {
                    currentPage++
                }
                else -> {
                    // Find the selected location
                    val location = pageLocations.find { it.first == selectedOption }
                    if (location != null) {
                        p.prepareForTeleport()
                        p.moveTo(location.second)
                        p.message("You teleport to ${location.first}.")
                        break
                    }
                }
            }
        }
    }
}
