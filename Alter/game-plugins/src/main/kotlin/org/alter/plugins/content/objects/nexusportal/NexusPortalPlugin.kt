package org.alter.plugins.content.objects.nexusportal

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
            "object.portal_nexus_33354",   // 33354 - Portal Nexus from POH
        )

        // Common portal interaction options
        val portalOptions = listOf("enter", "teleport", "use", "operate")

        portalObjects.forEach { portalId ->
            portalOptions.forEach { option ->
                try {
                    // Check if the object has this option before binding
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
                    }
                } catch (e: Exception) {
                    // Option not available for this portal, skip it
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

            val title = "Portal Nexus (Page ${currentPage + 1}/$totalPages)"
            val selected = options(p, *options.toTypedArray(), title = title)

            if (selected <= 0) {
                break // Player closed menu
            }

            val selectedOption = options[selected - 1]

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
