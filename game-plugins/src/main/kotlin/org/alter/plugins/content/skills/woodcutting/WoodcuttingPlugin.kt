package org.alter.plugins.content.skills.woodcutting

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class WoodcuttingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    init {
        loadService(WoodcuttingService())

        onWorldInit {
            val service = world.getService(WoodcuttingService::class.java) ?: return@onWorldInit
            service.entries.forEach { entry ->
                entry.objectIds.forEach { objId ->
                    val chopOptions = getObject(objId).actions.filterNotNull().filter {
                        it.equals("chop down", ignoreCase = true) || it.equals("chop", ignoreCase = true) || it.equals("cut", ignoreCase = true)
                    }
                    chopOptions.forEach { option ->
                        onObjOption(obj = objId, option = option) {
                            val obj = player.getInteractingGameObj()
                            player.queue { chopTree(this, player, obj, entry) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun chopTree(task: QueueTask, player: Player, obj: GameObject, entry: WoodcuttingEntry) {
        val level = player.getSkills().getCurrentLevel(Skills.WOODCUTTING)
        val treeName = obj.getDef().name?.lowercase()?.let { if (it.startsWith("the ")) it.drop(4) else it } ?: "tree"

        if (!obj.isSpawned(world)) {
            player.message("There's nothing here to chop down right now.")
            return
        }

        // Check for axe
        val axe = getBestAxe(player)
        if (axe == null) {
            player.message("You need an axe to chop down this tree.")
            return
        }

        if (level < entry.level) {
            player.message("You need a Woodcutting level of ${entry.level} to chop down this $treeName.")
            return
        }

        // Continuously chop until inventory is full or tree is felled
        var choppedCount = 0
        val startTile = player.tile

        // Helper function to check if we should stop chopping
        fun shouldStop(): Boolean {
            return !player.isOnline ||
                   player.hasMoveDestination() ||
                   !player.tile.sameAs(startTile)
        }

        // Helper function to interrupt and stop chopping
        fun interruptChopping() {
            player.interruptQueues()
        }

        while (true) {
            // Check if player has moved or has movement queued
            if (shouldStop()) {
                interruptChopping()
                break
            }

            if (!obj.isSpawned(world)) {
                if (choppedCount == 0) {
                    player.message("There's nothing here to chop down right now.")
                }
                break
            }

            // Check inventory space
            if (player.inventory.isFull) {
                if (choppedCount == 0) {
                    player.message("You need some inventory space to chop down this $treeName.")
                }
                break
            }

            // Re-check for axe (might have been unequipped)
            val currentAxe = getBestAxe(player)
            if (currentAxe == null) {
                if (choppedCount == 0) {
                    player.message("You need an axe to chop down this tree.")
                }
                break
            }

            // Check one more time before proceeding
            if (shouldStop()) {
                interruptChopping()
                return
            }

            player.faceTile(obj.tile)

            // Check again right before animation
            if (shouldStop()) {
                break
            }

            // Play woodcutting animation
            val animation = getWoodcuttingAnimation(currentAxe)
            player.animate(animation)

            // Woodcutting delay - varies by axe and level
            val choppingDelay = calculateChoppingDelay(player, entry, currentAxe)
            var interrupted = false
            for (i in 0 until choppingDelay) {
                if (shouldStop()) {
                    interrupted = true
                    break
                }
                task.wait(1)
            }

            if (interrupted) {
                break
            }

            // Final check before rewarding
            if (shouldStop() || !obj.isSpawned(world) || player.inventory.isFull) {
                break
            }

            // Successfully chopped logs
            rewardPlayer(player, entry, treeName)
            choppedCount++

            // Check if tree should be felled (random chance)
            if (shouldFellTree(entry, choppedCount)) {
                fellTree(world, obj, entry)
                break
            }

            // Small delay between chops
            if (shouldStop()) {
                interruptChopping()
                return
            }
            task.wait(1)
        }

        if (choppedCount > 1) {
            player.message("You chop ${choppedCount} logs from the $treeName.")
        }
    }

    private fun getBestAxe(player: Player): AxeInfo? {
        // Check equipment first (weapon slot)
        val equippedWeapon = player.getEquipment(EquipmentType.WEAPON)
        if (equippedWeapon != null) {
            val axe = getAxeInfo(equippedWeapon.id)
            if (axe != null) {
                return axe
            }
        }

        // Check inventory for best axe
        var bestAxe: AxeInfo? = null
        for (i in 0 until player.inventory.capacity) {
            val item = player.inventory[i] ?: continue
            val axe = getAxeInfo(item.id)
            if (axe != null) {
                if (bestAxe == null || axe.level > bestAxe.level) {
                    bestAxe = axe
                }
            }
        }

        return bestAxe
    }

    private fun getAxeInfo(axeId: Int): AxeInfo? {
        return when (axeId) {
            getRSCM("item.bronze_axe") -> AxeInfo(1, Animation.WOODCUTTING_BRONZE_AXE)
            getRSCM("item.iron_axe") -> AxeInfo(1, Animation.WOODCUTTING_IRON_AXE)
            getRSCM("item.steel_axe") -> AxeInfo(6, Animation.WOODCUTTING_STEEL_AXE)
            getRSCM("item.black_axe") -> AxeInfo(11, Animation.WOODCUTTING_BLACK_AXE)
            getRSCM("item.mithril_axe") -> AxeInfo(21, Animation.WOODCUTTING_MITHRIL_AXE)
            getRSCM("item.adamant_axe") -> AxeInfo(31, Animation.WOODCUTTING_ADAMANT_AXE)
            getRSCM("item.rune_axe") -> AxeInfo(41, Animation.WOODCUTTING_RUNE_AXE)
            getRSCM("item.dragon_axe") -> AxeInfo(61, Animation.WOODCUTTING_DRAGON_AXE)
            getRSCM("item.infernal_axe") -> AxeInfo(61, Animation.WOODCUTTING_INFERNAL_AXE)
            getRSCM("item._3rd_age_axe") -> AxeInfo(61, Animation.WOODCUTTING_THIRDAGE_AXE)
            getRSCM("item.crystal_axe") -> AxeInfo(71, Animation.WOODCUTTING_CRYSTAL_AXE)
            else -> null
        }
    }

    private fun getWoodcuttingAnimation(axe: AxeInfo): Int {
        return axe.animation
    }

    private fun calculateChoppingDelay(player: Player, entry: WoodcuttingEntry, axe: AxeInfo): Int {
        val baseDelay = 4
        val level = player.getSkills().getCurrentLevel(Skills.WOODCUTTING)
        val levelDiff = level - entry.level
        val axeBonus = axe.level

        // Faster with higher level and better axe
        var delay = baseDelay
        if (levelDiff > 0) {
            delay -= (levelDiff / 10).coerceAtMost(2)
        }
        if (axeBonus > 0) {
            delay -= (axeBonus / 20).coerceAtMost(2)
        }

        return delay.coerceAtLeast(2)
    }

    private fun shouldFellTree(entry: WoodcuttingEntry, choppedCount: Int): Boolean {
        // Higher level trees fall faster
        // Simple chance-based felling
        return when {
            entry.level <= 15 -> world.random(1..8) == 1 // ~12.5% chance
            entry.level <= 30 -> world.random(1..5) == 1 // 20% chance
            entry.level <= 45 -> world.random(1..4) == 1 // 25% chance
            entry.level <= 60 -> world.random(1..3) == 1 // 33% chance
            else -> world.random(1..2) == 1 // 50% chance
        }
    }

    private fun fellTree(world: World, obj: GameObject, entry: WoodcuttingEntry) {
        val tile = obj.tile
        val type = obj.type
        val rot = obj.rot
        val originalId = obj.id
        val stumpId = entry.stumpObjectId

        world.remove(obj)

        val stump = DynamicObject(id = stumpId, type = type, rot = rot, tile = tile)
        world.spawn(stump)

        world.queue {
            wait(entry.respawnTicks)
            if (world.isSpawned(stump)) {
                world.remove(stump)
            }
            world.spawn(DynamicObject(id = originalId, type = type, rot = rot, tile = tile))
        }
    }

    private fun rewardPlayer(player: Player, entry: WoodcuttingEntry, treeName: String) {
        player.addXp(Skills.WOODCUTTING, entry.experience)

        val transaction = player.inventory.add(item = entry.logsId, amount = entry.logsAmount)

        if (transaction.hasSucceeded()) {
            player.message("You get some logs.")
        } else {
            player.message("You don't have enough inventory space.")
        }
    }

    private data class AxeInfo(
        val level: Int,
        val animation: Int
    )
}

