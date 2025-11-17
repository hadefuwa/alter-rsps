package org.alter.plugins.content.skills.mining

import dev.openrune.cache.CacheManager.getItem
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

class MiningPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    init {
        loadService(MiningService())

        onWorldInit {
            val service = world.getService(MiningService::class.java) ?: return@onWorldInit
            service.entries.forEach { entry ->
                entry.objectIds.forEach { objId ->
                    val mineOptions = getObject(objId).actions.filterNotNull().filter {
                        it.equals("mine", ignoreCase = true) || it.equals("prospect", ignoreCase = true)
                    }
                    mineOptions.forEach { option ->
                        onObjOption(obj = objId, option = option) {
                            val obj = player.getInteractingGameObj()
                            player.queue { mineRock(this, player, obj, entry) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun mineRock(task: QueueTask, player: Player, obj: GameObject, entry: MiningEntry) {
        val level = player.getSkills().getCurrentLevel(Skills.MINING)
        val rockName = obj.getDef().name?.lowercase()?.let { if (it.startsWith("the ")) it.drop(4) else it } ?: "rock"

        if (!obj.isSpawned(world)) {
            player.message("There's nothing here to mine right now.")
            return
        }

        // Check for pickaxe
        val pickaxe = getBestPickaxe(player)
        if (pickaxe == null) {
            player.message("You need a pickaxe to mine this rock.")
            return
        }

        if (level < entry.level) {
            player.message("You need a Mining level of ${entry.level} to mine this $rockName.")
            return
        }

        // Continuously mine until inventory is full or rock is depleted
        var minedCount = 0
        val startTile = player.tile

        // Helper function to check if we should stop mining
        fun shouldStop(): Boolean {
            return !player.isOnline ||
                   player.hasMoveDestination() ||
                   !player.tile.sameAs(startTile)
        }

        // Helper function to interrupt and stop mining
        fun interruptMining() {
            player.interruptQueues()
        }

        while (true) {
            // Check if player has moved or has movement queued
            if (shouldStop()) {
                interruptMining()
                break
            }

            if (!obj.isSpawned(world)) {
                if (minedCount == 0) {
                    player.message("There's nothing here to mine right now.")
                }
                break
            }

            // Check inventory space
            if (player.inventory.isFull) {
                if (minedCount == 0) {
                    player.message("You need some inventory space to mine this $rockName.")
                }
                break
            }

            // Re-check for pickaxe (might have been unequipped)
            val currentPickaxe = getBestPickaxe(player)
            if (currentPickaxe == null) {
                if (minedCount == 0) {
                    player.message("You need a pickaxe to mine this rock.")
                }
                break
            }

            // Check one more time before proceeding
            if (shouldStop()) {
                interruptMining()
                return
            }

            player.faceTile(obj.tile)

            // Check again right before animation
            if (shouldStop()) {
                break
            }

            // Play mining animation
            val animation = getMiningAnimation(currentPickaxe)
            player.animate(animation)
            player.playSound(Sound.MINE_ORE)

            // Mining delay - varies by pickaxe and level
            val miningDelay = calculateMiningDelay(player, entry, currentPickaxe)
            var interrupted = false
            for (i in 0 until miningDelay) {
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

            // Successfully mined ore
            rewardPlayer(player, entry, rockName)
            minedCount++

            // Check if rock should be depleted (random chance or after certain amount)
            if (shouldDepleteRock(entry, minedCount)) {
                depleteRock(world, obj, entry)
                break
            }

            // Small delay between mining attempts
            if (shouldStop()) {
                interruptMining()
                return
            }
            task.wait(1)
        }

        if (minedCount > 1) {
            player.message("You mine ${minedCount} ores from the $rockName.")
        }
    }

    private fun getBestPickaxe(player: Player): PickaxeInfo? {
        // Check equipment first (weapon slot)
        val equippedWeapon = player.getEquipment(EquipmentType.WEAPON)
        if (equippedWeapon != null) {
            val pickaxe = getPickaxeInfo(equippedWeapon.id)
            if (pickaxe != null) {
                return pickaxe
            }
        }

        // Check inventory for best pickaxe
        var bestPickaxe: PickaxeInfo? = null
        for (i in 0 until player.inventory.capacity) {
            val item = player.inventory[i] ?: continue
            val pickaxe = getPickaxeInfo(item.id)
            if (pickaxe != null) {
                if (bestPickaxe == null || pickaxe.level > bestPickaxe.level) {
                    bestPickaxe = pickaxe
                }
            }
        }

        return bestPickaxe
    }

    private fun getPickaxeInfo(pickaxeId: Int): PickaxeInfo? {
        return when (pickaxeId) {
            getRSCM("item.bronze_pickaxe") -> PickaxeInfo(1, Animation.MINING_BRONZE_PICKAXE)
            getRSCM("item.iron_pickaxe") -> PickaxeInfo(1, Animation.MINING_IRON_PICKAXE)
            getRSCM("item.steel_pickaxe") -> PickaxeInfo(6, Animation.MINING_STEEL_PICKAXE)
            getRSCM("item.black_pickaxe") -> PickaxeInfo(11, Animation.MINING_BLACK_PICKAXE)
            getRSCM("item.mithril_pickaxe") -> PickaxeInfo(21, Animation.MINING_MITHRIL_PICKAXE)
            getRSCM("item.adamant_pickaxe") -> PickaxeInfo(31, Animation.MINING_ADAMANT_PICKAXE)
            getRSCM("item.rune_pickaxe") -> PickaxeInfo(41, Animation.MINING_RUNE_PICKAXE)
            getRSCM("item.dragon_pickaxe") -> PickaxeInfo(61, Animation.MINING_DRAGON_PICKAXE)
            getRSCM("item.infernal_pickaxe") -> PickaxeInfo(61, Animation.MINING_INFERNAL_PICKAXE)
            getRSCM("item._3rd_age_pickaxe") -> PickaxeInfo(61, Animation.MINING_THIRDAGE_PICKAXE)
            getRSCM("item.crystal_pickaxe") -> PickaxeInfo(71, Animation.MINING_CRYSTAL_PICKAXE)
            else -> null
        }
    }

    private fun getMiningAnimation(pickaxe: PickaxeInfo): Int {
        return pickaxe.animation
    }

    private fun calculateMiningDelay(player: Player, entry: MiningEntry, pickaxe: PickaxeInfo): Int {
        val baseDelay = 3
        val level = player.getSkills().getCurrentLevel(Skills.MINING)
        val levelDiff = level - entry.level
        val pickaxeBonus = pickaxe.level

        // Faster with higher level and better pickaxe
        var delay = baseDelay
        if (levelDiff > 0) {
            delay -= (levelDiff / 10).coerceAtMost(2)
        }
        if (pickaxeBonus > 0) {
            delay -= (pickaxeBonus / 20).coerceAtMost(2)
        }

        return delay.coerceAtLeast(2)
    }

    private fun shouldDepleteRock(entry: MiningEntry, minedCount: Int): Boolean {
        // Higher level rocks deplete faster
        // Simple chance-based depletion
        return when {
            entry.level <= 15 -> world.random(1..10) == 1 // 10% chance
            entry.level <= 30 -> world.random(1..5) == 1 // 20% chance
            entry.level <= 55 -> world.random(1..3) == 1 // 33% chance
            else -> world.random(1..2) == 1 // 50% chance
        }
    }

    private fun depleteRock(world: World, obj: GameObject, entry: MiningEntry) {
        val tile = obj.tile
        val type = obj.type
        val rot = obj.rot
        val originalId = obj.id
        val emptyId = entry.emptyObjectId

        world.remove(obj)

        val empty = DynamicObject(id = emptyId, type = type, rot = rot, tile = tile)
        world.spawn(empty)

        world.queue {
            wait(entry.respawnTicks)
            if (world.isSpawned(empty)) {
                world.remove(empty)
            }
            world.spawn(DynamicObject(id = originalId, type = type, rot = rot, tile = tile))
        }
    }

    private fun rewardPlayer(player: Player, entry: MiningEntry, rockName: String) {
        player.addXp(Skills.MINING, entry.experience)

        val transaction = player.inventory.add(item = entry.oreId, amount = entry.oreAmount)

        if (transaction.hasSucceeded()) {
            player.message("You mine some ore.")
        } else {
            player.message("You don't have enough inventory space.")
        }
    }

    private data class PickaxeInfo(
        val level: Int,
        val animation: Int
    )
}

