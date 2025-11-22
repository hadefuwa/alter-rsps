package org.alter.plugins.content.combat

import org.alter.api.EquipmentType
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.COMBAT_TARGET_FOCUS_ATTR
import org.alter.game.model.attr.FACING_PAWN_ATTR
import org.alter.game.model.attr.INTERACTING_PLAYER_ATTR
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.game.model.move.MovementQueue.StepType
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.move.stopMovement
import org.alter.game.model.move.walkRoute
import org.alter.game.model.move.walkTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.specialattack.SpecialAttacks
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.interfaces.attack.AttackTab
import java.util.*

class CombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        setCombatLogic {
            pawn.attr[COMBAT_TARGET_FOCUS_ATTR]?.get()?.let { target ->
                pawn.facePawn(target)
            }
            pawn.queue {
                while (true) {
                    // NPCs can follow players up to maxFollowDistance tiles from spawn point (default 20).
                    // This limit can be customized per NPC by setting the maxFollowDistance property.
                    if (!cycle(pawn, this)) {
                        break
                    }
                    wait(1)
                }
            }
        }

        onPlayerOption("Attack") {
            val target = pawn.attr[INTERACTING_PLAYER_ATTR]?.get() ?: return@onPlayerOption
            player.attack(target)
        }
    }

    /**
     * @TODO Bigger creatures seem to have bugged range + their route finding sucks due to conditions given.
     */
    suspend fun cycle(pawn: Pawn, queue: QueueTask): Boolean {
        // Stop combat if the pawn is dead
        if (pawn.isDead()) {
            Combat.reset(pawn)
            return false
        }
        val target = pawn.getCombatTarget() ?: return false
        // Stop combat if the target is dead
        if (target.isDead()) {
            Combat.reset(pawn)
            return false
        }
        // Check if NPC is too far from spawn point (only for NPCs)
        if (pawn.entityType.isNpc) {
            val npc = pawn as Npc
            val distanceFromSpawn = npc.tile.getDistance(npc.spawnTile)
            if (distanceFromSpawn > npc.maxFollowDistance) {
                // NPC is too far from spawn, reset combat and return to spawn
                Combat.reset(pawn)
                pawn.resetFacePawn()
                pawn.interruptQueues()
                // Walk back to spawn tile
                npc.walkTo(npc.spawnTile)
                return false
            }
        }
        val strategy = CombatConfigs.getCombatStrategy(pawn)
        val attackRange = strategy.getAttackRange(pawn)
        var routeLogic = 1
        if (target != pawn.attr[FACING_PAWN_ATTR]?.get()) {
            return false
        }
        if (pawn.entityType.isNpc) {
            routeLogic = (pawn as Npc).routeLogic
        }
        var reached = world.reachStrategy.reached(
            flags = world.collision,
            level = pawn.tile.height,
            srcX = pawn.tile.x ,
            srcZ = pawn.tile.z,
            destX = target.tile.x,
            destZ = target.tile.z,
            destWidth = target.getSize(),
            destLength = target.getSize(),
            srcSize = pawn.getSize(),
            locShape = -2
        )
        if (!reached) {
            var movementAdded = false
            when (routeLogic) {
                1 -> {
                    val route = world.smartRouteFinder.findRoute(
                        level = pawn.tile.height,
                        srcX = pawn.tile.x,
                        srcZ = pawn.tile.z,
                        destX = target.tile.x,
                        destZ = target.tile.z,
                        locShape = -2,
                        destWidth = target.getSize(),
                        destLength = target.getSize()
                    )
                    // Only walk if route is successful and has waypoints
                    if (route.success && route.waypoints.isNotEmpty()) {
                        pawn.walkRoute(route, StepType.NORMAL)
                        movementAdded = true
                    } else {
                        // Fallback to dumb route finder if smart route finder fails
                        val fallbackRoute = LinkedList<Tile>()
                        val destination = world.dumbRouteFinder.naiveDestination(
                            sourceX = pawn.tile.x,
                            sourceZ = pawn.tile.z,
                            sourceWidth = pawn.getSize(),
                            sourceLength = pawn.getSize(),
                            targetX = target.tile.x,
                            targetZ = target.tile.z,
                            targetWidth = target.getSize(),
                            targetLength = target.getSize()
                        )
                        val dx = destination.x - pawn.tile.x
                        val dz = destination.z - pawn.tile.z
                        // Try diagonal move (both x and z)
                        val diagonalMove = Tile(pawn.tile.x + dx.coerceIn(-1, 1), pawn.tile.z + dz.coerceIn(-1, 1))
                        if (!world.canTraverse(pawn.tile, Direction.between(pawn.tile, diagonalMove), pawn, pawn.getSize())) {
                            // If diagonal blocked, try horizontal (east/west)
                            val horizontalMove = Tile(pawn.tile.x + dx.coerceIn(-1, 1), pawn.tile.z)
                            if (!world.canTraverse(pawn.tile, Direction.between(pawn.tile, horizontalMove), pawn, pawn.getSize())) {
                                // If horizontal blocked, try vertical (north/south)
                                val verticalMove = Tile(pawn.tile.x, pawn.tile.z + dz.coerceIn(-1, 1))
                                if (world.canTraverse(pawn.tile, Direction.between(pawn.tile, verticalMove), pawn, pawn.getSize())) {
                                    fallbackRoute.add(verticalMove)
                                }
                            } else {
                                fallbackRoute.add(horizontalMove)
                            }
                        } else {
                            fallbackRoute.add(diagonalMove)
                        }
                        if (fallbackRoute.isNotEmpty()) {
                            pawn.walkRoute(fallbackRoute, stepType = StepType.NORMAL)
                            movementAdded = true
                        }
                    }
                }
                0 -> {
                    val route = LinkedList<Tile>()
                    val destination = world.dumbRouteFinder.naiveDestination(
                        sourceX = pawn.tile.x,
                        sourceZ = pawn.tile.z,
                        sourceWidth = pawn.getSize(),
                        sourceLength = pawn.getSize(),
                        targetX = target.tile.x,
                        targetZ = target.tile.z,
                        targetWidth = target.getSize(),
                        targetLength = target.getSize()
                    )
                    val dx = destination.x - pawn.tile.x
                    val dz = destination.z - pawn.tile.z
                    // Try diagonal move (both x and z)
                    val diagonalMove = Tile(pawn.tile.x + dx.coerceIn(-1, 1), pawn.tile.z + dz.coerceIn(-1, 1))
                    if (!world.canTraverse(pawn.tile, Direction.between(pawn.tile, diagonalMove), pawn, pawn.getSize())) {
                        // If diagonal blocked, try horizontal (east/west)
                        val horizontalMove = Tile(pawn.tile.x + dx.coerceIn(-1, 1), pawn.tile.z)
                        if (!world.canTraverse(pawn.tile, Direction.between(pawn.tile, horizontalMove), pawn, pawn.getSize())) {
                            // If horizontal blocked, try vertical (north/south)
                            val verticalMove = Tile(pawn.tile.x, pawn.tile.z + dz.coerceIn(-1, 1))
                            if (world.canTraverse(pawn.tile, Direction.between(pawn.tile, verticalMove), pawn, pawn.getSize())) {
                                route.add(verticalMove)
                            }
                        } else {
                            route.add(horizontalMove)
                        }
                    } else {
                        route.add(diagonalMove)
                    }
                    // If all primary directions are blocked, try any valid adjacent tile towards the target
                    if (route.isEmpty()) {
                        // Try all 8 directions, prioritizing those closer to target
                        val directions = listOf(
                            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
                            Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST
                        )
                        val bestDirection = directions.minByOrNull { dir ->
                            val testTile = pawn.tile.step(dir)
                            if (world.canTraverse(pawn.tile, dir, pawn, pawn.getSize())) {
                                testTile.getDistance(target.tile)
                            } else {
                                Int.MAX_VALUE
                            }
                        }
                        bestDirection?.let { dir ->
                            val testTile = pawn.tile.step(dir)
                            if (world.canTraverse(pawn.tile, dir, pawn, pawn.getSize())) {
                                route.add(testTile)
                            }
                        }
                    }
                    if (route.isNotEmpty()) {
                        pawn.walkRoute(route, stepType = StepType.NORMAL)
                        movementAdded = true
                    }
                }
            }
            // If no movement was added and we're not in attack range, wait a bit and try again
            if (!movementAdded && pawn.tile.getDistance(target.tile) > attackRange + target.getSize()) {
                queue.wait(1)
                return cycle(pawn, queue)
            }
        }
        // Check if we're in attack range
        if (pawn.tile.getDistance(target.tile) <= attackRange + target.getSize()) {
            reached = true
            pawn.stopMovement()
        }
        // Wait for movement to complete, or if we're not in range and have no movement, try again
        while (pawn.hasMoveDestination() || (!reached && pawn.tile.getDistance(target.tile) > attackRange + target.getSize())) {
            queue.wait(1)
            if (!target.isAlive()) {
                return false
            }
            // Re-check if we reached the target during movement
            if (pawn.tile.getDistance(target.tile) <= attackRange + target.getSize()) {
                reached = true
                pawn.stopMovement()
                break
            }
            // If we have no movement destination and we're still not in range, try to move again
            if (!pawn.hasMoveDestination() && !reached) {
                return cycle(pawn, queue)
            }
        }
        if (!Combat.canEngage(pawn, target)) {
            Combat.reset(pawn)
            pawn.resetFacePawn()
            return false
        }
        if (!pawn.lock.canAttack()) {
            Combat.reset(pawn)
            return false
        }
        if (pawn is Player) {
            pawn.setVarp(Combat.PRIORITY_PID_VARP, target.index)
            if (!pawn.attr.has(Combat.CASTING_SPELL) && pawn.getVarbit(Combat.SELECTED_AUTOCAST_VARBIT) != 0) {
                val spell =
                    CombatSpell.values.firstOrNull { it.autoCastId == pawn.getVarbit(Combat.SELECTED_AUTOCAST_VARBIT) }
                if (spell != null) {
                    pawn.attr[Combat.CASTING_SPELL] = spell
                }
            }
        }
        if (target != pawn.attr[FACING_PAWN_ATTR]?.get()) {
            return false
        }
        if (Combat.isAttackDelayReady(pawn)) {
            if (Combat.canAttack(pawn, target, strategy)) {
                if (pawn is Player && AttackTab.isSpecialEnabled(pawn) && pawn.getEquipment(EquipmentType.WEAPON) != null) {
                    AttackTab.disableSpecial(pawn)
                    if (SpecialAttacks.execute(pawn, target, world)) {
                        Combat.postAttack(pawn, target)
                        return true
                    }
                    pawn.message("You don't have enough power left.")
                }
                strategy.attack(pawn, target)
                Combat.postAttack(pawn, target)
            } else {
                Combat.reset(pawn)
                return false
            }
        }
        return true
    }
}