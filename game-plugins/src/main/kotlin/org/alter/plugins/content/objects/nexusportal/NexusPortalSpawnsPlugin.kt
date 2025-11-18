package org.alter.plugins.content.objects.nexusportal

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Nexus Portal Spawns Plugin
 *
 * This plugin spawns Portal Nexus objects at convenient locations around the world.
 * Players can use these portals to quickly travel to major cities and locations.
 *
 * Portal Locations:
 * - Varrock Centre (center square)
 * - Edgeville (near bank)
 * - Lumbridge (castle courtyard)
 * - Falador (center square)
 *
 * To add more portal spawns, simply add new spawnObj() calls in the init block.
 */
class NexusPortalSpawnsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Portal object ID - using Crystalline portal nexus (ID 33410)
        // This is the regular Crystalline portal nexus which has proper interaction options
        val portalObjId = "object.portal_nexus_33410"

        // Spawn portals at key locations

        // 1. Varrock Centre (center of Varrock) - updated coordinates
        spawnObj(
            obj = portalObjId,
            x = 3212,
            z = 3433,
            height = 0,
            type = 10,
            rot = 0
        )

        // 2. Edgeville (near bank)
        spawnObj(
            obj = portalObjId,
            x = 3094,
            z = 3491,
            height = 0,
            type = 10,
            rot = 0
        )

        // 3. Lumbridge (castle courtyard)
        spawnObj(
            obj = portalObjId,
            x = 3222,
            z = 3218,
            height = 0,
            type = 10,
            rot = 0
        )

        // 4. Falador (center square)
        spawnObj(
            obj = portalObjId,
            x = 2966,
            z = 3380,
            height = 0,
            type = 10,
            rot = 0
        )

        // 5. Draynor Village (market square)
        spawnObj(
            obj = portalObjId,
            x = 3093,
            z = 3245,
            height = 0,
            type = 10,
            rot = 0
        )

        // 6. Al Kharid (near bank)
        spawnObj(
            obj = portalObjId,
            x = 3270,
            z = 3167,
            height = 0,
            type = 10,
            rot = 0
        )

        // 7. Ardougne (market square)
        spawnObj(
            obj = portalObjId,
            x = 2662,
            z = 3305,
            height = 0,
            type = 10,
            rot = 0
        )

        // 8. Camelot (castle entrance)
        spawnObj(
            obj = portalObjId,
            x = 2757,
            z = 3477,
            height = 0,
            type = 10,
            rot = 0
        )

        // ADD MORE PORTAL SPAWNS HERE
        // Example:
        // spawnObj(
        //     obj = portalObjId,
        //     x = YOUR_X_COORDINATE,
        //     z = YOUR_Z_COORDINATE,
        //     height = 0,
        //     type = 10,
        //     rot = 0  // 0 = South, 1 = West, 2 = North, 3 = East
        // )
    }
}
