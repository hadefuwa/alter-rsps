package org.alter.plugins.content.objects.ladder

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

        /**Ladders*/

        val ladders =
            arrayOf(
                "object.ladder_12964",
                "object.ladder_12965",
                "object.ladder_16683",
                "object.ladder_12966",
                "object.ladder_16679",
                "object.ladder_16684",
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
