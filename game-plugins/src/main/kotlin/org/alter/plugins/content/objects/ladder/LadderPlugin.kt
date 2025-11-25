package org.alter.plugins.content.objects.ladder

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
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

class LadderPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        /**Stairs*/

        val stairs =
            arrayOf(
                "object.staircase_16672",
                "object.staircase_16673",
                "object.staircase_16671",
            )

        stairs.forEach { stairs ->
            if (objHasOption(obj = stairs, option = "climb")) {
                onObjOption(obj = stairs, option = "climb") {
                    climbstairs(player)
                }
            }
            if (objHasOption(obj = stairs, option = "climb-up")) {
                onObjOption(obj = stairs, option = "climb-up") {
                    climbupstairs(player)
                }
            }
            if (objHasOption(obj = stairs, option = "climb-down")) {
                onObjOption(obj = stairs, option = "climb-down") {
                    climbdownstairs(player)
                }
            }
        }
        
        // Slayer Tower Staircase (object.staircase, ID 2114)
        // Teleports player to x=3437, y=3535, height=1
        // Check which options are available before registering handlers
        // Using lineOfSightDistance = 5 to allow interaction from further away
        if (objHasOption(obj = "object.staircase", option = "climb")) {
            onObjOption("object.staircase", option = "climb", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb up the stairs.")
                    player.animate(828) // Climb up animation
                    player.lock()
                    wait(2)
                    player.moveTo(3437, 3535, 1)
                    player.unlock()
                }
            }
        }
        
        if (objHasOption(obj = "object.staircase", option = "climb-up")) {
            onObjOption("object.staircase", option = "climb-up", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb up the stairs.")
                    player.animate(828) // Climb up animation
                    player.lock()
                    wait(2)
                    player.moveTo(3437, 3535, 1)
                    player.unlock()
                }
            }
        }
        
        // Also handle climb-down option if it exists
        if (objHasOption(obj = "object.staircase", option = "climb-down")) {
            onObjOption("object.staircase", option = "climb-down", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb down the stairs.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3437, 3535, 1)
                    player.unlock()
                }
            }
        }
        
        // Stairs 2119 (staircase_2119) - Teleport to (3418, 3541, height 2)
        // Using lineOfSightDistance = 5 to allow interaction from further away
        if (objHasOption(obj = "object.staircase_2119", option = "climb")) {
            onObjOption("object.staircase_2119", option = "climb", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb up the stairs.")
                    player.animate(828) // Climb up animation
                    player.lock()
                    wait(2)
                    player.moveTo(3418, 3541, 2)
                    player.unlock()
                }
            }
        }
        
        if (objHasOption(obj = "object.staircase_2119", option = "climb-up")) {
            onObjOption("object.staircase_2119", option = "climb-up", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb up the stairs.")
                    player.animate(828) // Climb up animation
                    player.lock()
                    wait(2)
                    player.moveTo(3418, 3541, 2)
                    player.unlock()
                }
            }
        }
        
        // Also handle climb-down option if it exists
        if (objHasOption(obj = "object.staircase_2119", option = "climb-down")) {
            onObjOption("object.staircase_2119", option = "climb-down", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb down the stairs.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3418, 3541, 2)
                    player.unlock()
                }
            }
        }
        
        // Stairs 2120 (staircase_2120) - Teleport down to (3413, 3540, height 1)
        // Using lineOfSightDistance = 5 to allow interaction from further away
        if (objHasOption(obj = "object.staircase_2120", option = "climb")) {
            onObjOption("object.staircase_2120", option = "climb", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb down the stairs.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3413, 3540, 1)
                    player.unlock()
                }
            }
        }
        
        if (objHasOption(obj = "object.staircase_2120", option = "climb-down")) {
            onObjOption("object.staircase_2120", option = "climb-down", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb down the stairs.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3413, 3540, 1)
                    player.unlock()
                }
            }
        }
        
        if (objHasOption(obj = "object.staircase_2120", option = "climb-up")) {
            onObjOption("object.staircase_2120", option = "climb-up", lineOfSightDistance = 5) {
                player.queue {
                    player.message("You climb down the stairs.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3413, 3540, 1)
                    player.unlock()
                }
            }
        }

        /**Ladders*/
        
        // Ladder 30191 - Teleports to (3412, 9932, height 3)
        // Handle all common ladder options
        if (objHasOption(obj = "object.ladder_30191", option = "climb")) {
            onObjOption("object.ladder_30191", option = "climb") {
                player.queue {
                    player.message("You climb down the ladder.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3412, 9932, 3)
                    player.unlock()
                }
            }
        }
        if (objHasOption(obj = "object.ladder_30191", option = "climb-down")) {
            onObjOption("object.ladder_30191", option = "climb-down") {
                player.queue {
                    player.message("You climb down the ladder.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3412, 9932, 3)
                    player.unlock()
                }
            }
        }
        if (objHasOption(obj = "object.ladder_30191", option = "climb-up")) {
            onObjOption("object.ladder_30191", option = "climb-up") {
                player.queue {
                    player.message("You climb down the ladder.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3412, 9932, 3)
                    player.unlock()
                }
            }
        }

        val ladders =
            arrayOf(
                "object.ladder_12964",
                "object.ladder_12965",
                "object.ladder_16683",
                "object.ladder_12966",
                "object.ladder_16679",
                "object.ladder_16684",
                "object.ladder_14745",
                "object.ladder_14746",
                "object.ladder_14747",
                "object.ladder_14748",
                // object.ladder_30191 is handled separately above
            )

        ladders.forEach { ladder ->
            if (objHasOption(obj = ladder, option = "climb")) {
                onObjOption(obj = ladder, option = "climb") {
                    climbladder(player)
                }
            }
            if (objHasOption(obj = ladder, option = "climb-up")) {
                onObjOption(obj = ladder, option = "climb-up") {
                    climbupladder(player)
                }
            }
            if (objHasOption(obj = ladder, option = "climb-down")) {
                onObjOption(obj = ladder, option = "climb-down") {
                    climbdownladder(player)
                }
            }
        }

        /**Trapdoors.*/

        onObjOption("object.trapdoor_14880", option = "climb-down") {
            player.moveTo(3210, 9616, 0)
        }
        onObjOption("object.ladder_17385", option = "climb-up") {
            player.moveTo(3210, 3216, 0)
        }
        
        // H.A.M. Hideout Ladder (objects 5491/5492)
        // Surface entrance: 3164, 3251, height 0
        // Underground hideout: 3164, 9627, height 0 (underground level)
        // Note: Object 5490 is the closed trapdoor (has "Open" option, not "climb-down")
        // Object 5491 is the opened ladder that can be climbed
        // Object 5492 is the opened state of 5491 (has "Open" option 1, "Pick-Lock" option 5)
        
        // Handle object 5492 - opened trapdoor (using numeric ID since RSCM name is null_5492)
        // Check what options object 5492 has and bind to the appropriate one
        try {
            val obj5492Def = getObject(5492)
            val obj5492Options = obj5492Def.actions.filterNotNull().map { it.lowercase() }
            
            // Try common trapdoor options
            val possibleOptions = listOf("open", "climb-down", "climb-up", "climb", "operate")
            var optionBound = false
            
            for (option in possibleOptions) {
                if (obj5492Options.contains(option) && !optionBound) {
                    try {
                        onObjOption(5492, option = option, lineOfSightDistance = 1) {
                            player.queue {
                                val playerTile = player.tile
                                if (playerTile.z > 5000) {
                                    // Underground, go to surface
                                    player.message("You climb up the ladder.")
                                    player.animate(828) // Climb up animation
                                    player.lock()
                                    wait(2)
                                    player.moveTo(3164, 3251, 0) // H.A.M. hideout surface entrance
                                    player.message("You climb out of the hideout.")
                                    player.unlock()
                                } else {
                                    // On surface, go to underground
                                    player.message("You climb down the trapdoor.")
                                    player.animate(827) // Climb down animation
                                    player.lock()
                                    wait(2)
                                    player.moveTo(3164, 9627, 0) // H.A.M. hideout underground
                                    player.message("You find yourself in the H.A.M. hideout.")
                                    player.unlock()
                                }
                            }
                        }
                        optionBound = true
                        break
                    } catch (e: IllegalStateException) {
                        // Option already bound, try next
                        continue
                    } catch (e: Exception) {
                        // Other error, try next option
                        continue
                    }
                }
            }
            
            // If no string option worked, try binding directly to option index 1
            if (!optionBound && obj5492Options.isNotEmpty()) {
                try {
                    r.bindObject(5492, 1, 1) {
                        player.queue {
                            val playerTile = player.tile
                            if (playerTile.z > 5000) {
                                // Underground, go to surface
                                player.message("You climb up the ladder.")
                                player.animate(828) // Climb up animation
                                player.lock()
                                wait(2)
                                player.moveTo(3164, 3251, 0) // H.A.M. hideout surface entrance
                                player.message("You climb out of the hideout.")
                                player.unlock()
                            } else {
                                // On surface, go to underground
                                player.message("You climb down the trapdoor.")
                                player.animate(827) // Climb down animation
                                player.lock()
                                wait(2)
                                player.moveTo(3164, 9627, 0) // H.A.M. hideout underground
                                player.message("You find yourself in the H.A.M. hideout.")
                                player.unlock()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Option might already be bound or object doesn't exist
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, skip
        }
        
        // Ladder 5491 - Handle all climbing options dynamically
        try {
            val obj5491Def = getObject(5491)
            val obj5491Options = obj5491Def.actions.filterNotNull().map { it.lowercase() }
            
            // Common climbing options to try
            val climbingOptions = listOf("climb-down", "climb-up", "climb", "open", "operate")
            val registeredOptions = mutableSetOf<String>()
            
            // Handler function for climbing logic
            val climbHandler: Plugin.() -> Unit = {
                player.queue {
                    val playerTile = player.tile
                    if (playerTile.z > 5000) {
                        // Underground, go to surface
                        player.message("You climb up the ladder.")
                        player.animate(828) // Climb up animation
                        player.lock()
                        wait(2)
                        player.moveTo(3164, 3251, 0) // H.A.M. hideout surface entrance
                        player.message("You climb out of the hideout.")
                        player.unlock()
                    } else {
                        // On surface, go to underground
                        player.message("You climb down the ladder.")
                        player.animate(827) // Climb down animation
                        player.lock()
                        wait(2)
                        player.moveTo(3164, 9627, 0) // H.A.M. hideout underground
                        player.message("You find yourself in the H.A.M. hideout.")
                        player.unlock()
                    }
                }
            }
            
            // Try to bind to available climbing options
            for (option in climbingOptions) {
                if (obj5491Options.contains(option) && !registeredOptions.contains(option)) {
                    try {
                        onObjOption(5491, option = option, lineOfSightDistance = 1) {
                            climbHandler()
                        }
                        registeredOptions.add(option)
                    } catch (e: IllegalStateException) {
                        // Option already bound, skip
                        continue
                    } catch (e: Exception) {
                        // Other error, try next option
                        continue
                    }
                }
            }
            
            // Also try using RSCM name if numeric ID didn't work
            if (registeredOptions.isEmpty()) {
                try {
                    for (option in climbingOptions) {
                        if (objHasOption("object.trapdoor_5491", option) && !registeredOptions.contains(option)) {
                            try {
                                onObjOption("object.trapdoor_5491", option = option, lineOfSightDistance = 1) {
                                    climbHandler()
                                }
                                registeredOptions.add(option)
                                break
                            } catch (e: IllegalStateException) {
                                // Option already bound, skip
                                continue
                            } catch (e: Exception) {
                                // Other error, try next option
                                continue
                            }
                        }
                    }
                } catch (e: Exception) {
                    // RSCM name might not work
                }
            }
            
            // Fallback: bind directly to option index 1 if no string options worked
            if (registeredOptions.isEmpty() && obj5491Options.isNotEmpty()) {
                try {
                    r.bindObject(5491, 1, 1) {
                        climbHandler()
                    }
                } catch (e: Exception) {
                    // Option might already be bound
                }
            }
        } catch (e: Exception) {
            // Object might not exist in cache, try RSCM name as fallback
            // Only try if we haven't registered any options yet (registeredOptions is not accessible here,
            // so we'll catch IllegalStateException if it's already bound)
            try {
                onObjOption("object.trapdoor_5491", option = "climb-down", lineOfSightDistance = 1) {
                    player.queue {
                        val playerTile = player.tile
                        if (playerTile.z > 5000) {
                            player.message("You climb up the ladder.")
                            player.animate(828)
                            player.lock()
                            wait(2)
                            player.moveTo(3164, 3251, 0)
                            player.message("You climb out of the hideout.")
                            player.unlock()
                        } else {
                            player.message("You climb down the ladder.")
                            player.animate(827)
                            player.lock()
                            wait(2)
                            player.moveTo(3164, 9627, 0)
                            player.message("You find yourself in the H.A.M. hideout.")
                            player.unlock()
                        }
                    }
                }
            } catch (e2: IllegalStateException) {
                // Option already bound, skip
            } catch (e2: Exception) {
                // Could not register
            }
        }
        
        // Note: Object 5491 is already handled above with dynamic option detection.
        // The duplicate binding section has been removed to prevent "already bound" errors.
        
        // KBD Ladder (object 18987) - Teleports to King Black Dragon lair
        onObjOption("object.ladder_18987", option = "climb-down") {
            player.queue {
                player.message("You climb down the ladder.")
                player.animate(827) // Climb down animation
                player.lock()
                wait(2)
                player.moveTo(2275, 4680, 0) // King Black Dragon lair entrance
                player.message("You find yourself in the King Black Dragon's lair.")
                player.unlock()
            }
        }
        
        // Edgeville Dungeon Trapdoor (object 1581)
        // Teleports to Edgeville dungeon entrance (near hill giants area)
        // Edgeville surface location: around 3096, 3468
        // Dungeon entrance location: 3110, 9830 (height 0, underground)
        // Note: Only using RSCM name to avoid conflict with TrapdoorPlugin
        // Object 1581 only has "Climb-down" and "Close" options, no "climb-up"
        
        onObjOption("object.trapdoor_1581", option = "climb-down") {
            player.queue {
                val playerTile = player.tile
                // If player is on surface (height 0, z around 3468), go to dungeon
                // If player is in dungeon (height 0, z around 9830), go to surface
                if (playerTile.z < 5000) {
                    // On surface, go to dungeon
                    player.message("You climb down the trapdoor.")
                    player.animate(827) // Climb down animation
                    player.lock()
                    wait(2)
                    player.moveTo(3110, 9830, 0) // Edgeville dungeon entrance
                    player.message("You find yourself in the Edgeville dungeon.")
                    player.unlock()
                } else {
                    // In dungeon, go to surface (using climb-down from dungeon perspective)
                    player.message("You climb up the ladder.")
                    player.animate(828) // Climb up animation
                    player.lock()
                    wait(2)
                    player.moveTo(3096, 3468, 0) // Edgeville surface (near trapdoor)
                    player.message("You climb out of the dungeon.")
                    player.unlock()
                }
            }
        }
        
        // Stairs 30190 - Teleport to (2883, 9825, height 0)
        // Try common stair options
        val stairs30190Options = listOf("climb", "climb-up", "climb-down", "use", "operate")
        stairs30190Options.forEach { option ->
            try {
                onObjOption(30190, option = option) {
                    player.moveTo(2883, 9825, 0)
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Stairs 30189 - Teleport to (2880, 9825, height 1)
        // Try common stair options
        val stairs30189Options = listOf("climb", "climb-up", "climb-down", "use", "operate")
        stairs30189Options.forEach { option ->
            try {
                onObjOption(30189, option = option) {
                    player.moveTo(2880, 9825, 1)
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Stairs 16665 (staircase_16665) - Teleport to (3045, 3927, height 0)
        // Try common stair options
        val stairs16665Options = listOf("climb", "climb-up", "climb-down", "use", "operate")
        stairs16665Options.forEach { option ->
            try {
                onObjOption(16665, option = option) {
                    player.queue {
                        player.message("You climb the staircase and find yourself in a new location.")
                        player.animate(828) // Climb animation
                        player.lock()
                        wait(2)
                        player.moveTo(3045, 3927, 0)
                        player.unlock()
                    }
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Also try using RSCM name as backup
        try {
            if (objHasOption(obj = "object.staircase_16665", option = "climb")) {
                onObjOption("object.staircase_16665", option = "climb") {
                    player.queue {
                        player.message("You climb the staircase and find yourself in a new location.")
                        player.animate(828)
                        player.lock()
                        wait(2)
                        player.moveTo(3045, 3927, 0)
                        player.unlock()
                    }
                }
            }
        } catch (e: Exception) {
            // RSCM name might not work, that's okay
        }
        
        // Stairs 16664 (staircase_16664) - Teleport to (3045, 10323, height 0)
        // Try common stair options
        val stairs16664Options = listOf("climb", "climb-up", "climb-down", "use", "operate")
        stairs16664Options.forEach { option ->
            try {
                onObjOption(16664, option = option) {
                    player.queue {
                        player.message("You climb the staircase and find yourself in a new location.")
                        player.animate(828) // Climb animation
                        player.lock()
                        wait(2)
                        player.moveTo(3045, 10323, 0)
                        player.unlock()
                    }
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Also try using RSCM name as backup
        try {
            if (objHasOption(obj = "object.staircase_16664", option = "climb")) {
                onObjOption("object.staircase_16664", option = "climb") {
                    player.queue {
                        player.message("You climb the staircase and find yourself in a new location.")
                        player.animate(828)
                        player.lock()
                        wait(2)
                        player.moveTo(3045, 10323, 0)
                        player.unlock()
                    }
                }
            }
        } catch (e: Exception) {
            // RSCM name might not work, that's okay
        }
        
        // Chain 16537 (spikey_chain) - Teleport to (3424, 3548, height 1)
        // Try common chain/climb options
        val chain16537Options = listOf("climb", "climb-up", "climb-down", "use", "operate", "swing")
        chain16537Options.forEach { option ->
            try {
                onObjOption(16537, option = option) {
                    player.queue {
                        player.message("You climb the chain.")
                        player.animate(828) // Climb animation
                        player.lock()
                        wait(2)
                        player.moveTo(3424, 3548, 1)
                        player.unlock()
                    }
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Also try using RSCM name as backup
        try {
            if (objHasOption(obj = "object.spikey_chain", option = "climb")) {
                onObjOption("object.spikey_chain", option = "climb") {
                    player.queue {
                        player.message("You climb the chain.")
                        player.animate(828)
                        player.lock()
                        wait(2)
                        player.moveTo(3424, 3548, 1)
                        player.unlock()
                    }
                }
            }
        } catch (e: Exception) {
            // RSCM name might not work, that's okay
        }
        
        // Chain 16538 (spikey_chain_16538) - Teleport to (3422, 3549, height 0) - Downstairs
        // Try common chain/climb options
        val chain16538Options = listOf("climb", "climb-up", "climb-down", "use", "operate", "swing")
        chain16538Options.forEach { option ->
            try {
                onObjOption(16538, option = option) {
                    player.queue {
                        player.message("You climb down the chain.")
                        player.animate(827) // Climb down animation
                        player.lock()
                        wait(2)
                        player.moveTo(3422, 3549, 0)
                        player.unlock()
                    }
                }
            } catch (e: Exception) {
                // Option might not exist for this object, continue to next
            }
        }
        
        // Also try using RSCM name as backup
        try {
            if (objHasOption(obj = "object.spikey_chain_16538", option = "climb")) {
                onObjOption("object.spikey_chain_16538", option = "climb") {
                    player.queue {
                        player.message("You climb down the chain.")
                        player.animate(827)
                        player.lock()
                        wait(2)
                        player.moveTo(3422, 3549, 0)
                        player.unlock()
                    }
                }
            }
        } catch (e: Exception) {
            // RSCM name might not work, that's okay
        }
    }

    /**Function for ladders.*/

    fun climbupladder(player: Player) {
        player.queue {
            player.animate(828)
            player.lock()
            wait(2)
            player.moveTo(player.tile.x, player.tile.z, player.tile.height + 1)
            player.unlock()
        }
    }

    fun climbdownladder(player: Player) {
        player.queue {
            player.animate(828)
            player.lock()
            wait(2)
            player.moveTo(player.tile.x, player.tile.z, player.tile.height - 1)
            player.unlock()
        }
    }

    fun climbladder(player: Player) {
        player.queue {
            when (options(player, "Climb up the ladder.", "Climb down the ladder")) {
                1 -> climbupladder(player)
                2 -> climbdownladder(player)
            }
        }
    }

    /**Function for stairs.*/

    fun climbupstairs(player: Player) {
        player.moveTo(player.tile.x, player.tile.z, player.tile.height + 1)
    }

    fun climbdownstairs(player: Player) {
        player.moveTo(player.tile.x, player.tile.z, player.tile.height - 1)
    }

    fun climbstairs(player: Player) {
        player.queue {
            when (options(player, "Climb up the stairs.", "Climb down the stairs.")) {
                1 -> climbupstairs(player)
                2 -> climbdownstairs(player)
            }
        }
    }
}
