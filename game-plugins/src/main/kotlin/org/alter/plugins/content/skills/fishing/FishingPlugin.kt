package org.alter.plugins.content.skills.fishing

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class FishingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    init {
        loadService(FishingService())

        onWorldInit {
            val service = world.getService(FishingService::class.java) ?: return@onWorldInit
            service.entries.forEach { entry ->
                entry.objectIds.forEach { objId ->
                    val fishOptions = getObject(objId).actions.filterNotNull().filter {
                        it.equals("net", ignoreCase = true) || 
                        it.equals("bait", ignoreCase = true) || 
                        it.equals("lure", ignoreCase = true) ||
                        it.equals("harpoon", ignoreCase = true) ||
                        it.equals("cage", ignoreCase = true)
                    }
                    fishOptions.forEach { option ->
                        onObjOption(obj = objId, option = option) {
                            val obj = player.getInteractingGameObj()
                            player.queue { fish(this, player, obj, entry) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun fish(task: QueueTask, player: Player, obj: GameObject, entry: FishingEntry) {
        val level = player.getSkills().getCurrentLevel(Skills.FISHING)

        if (!obj.isSpawned(world)) {
            player.message("There's nothing here to fish right now.")
            return
        }

        // Check for tool
        if (!player.inventory.contains(entry.toolId) && !player.equipment.contains(entry.toolId)) {
            val toolName = when (entry.toolId) {
                getRSCM("item.small_fishing_net") -> "small fishing net"
                getRSCM("item.fishing_rod") -> "fishing rod"
                getRSCM("item.harpoon") -> "harpoon"
                getRSCM("item.lobster_pot") -> "lobster pot"
                else -> "fishing tool"
            }
            player.message("You need a $toolName to fish here.")
            return
        }

        // Check for bait if required
        if (entry.baitId != null && !player.inventory.contains(entry.baitId!!)) {
            player.message("You need some fishing bait to fish here.")
            return
        }

        if (level < entry.level) {
            player.message("You need a Fishing level of ${entry.level} to fish here.")
            return
        }

        // Continuously fish until inventory is full
        var fishedCount = 0
        val startTile = player.tile

        fun shouldStop(): Boolean {
            return !player.isOnline ||
                   player.hasMoveDestination() ||
                   !player.tile.sameAs(startTile)
        }

        fun interruptFishing() {
            player.interruptQueues()
        }

        while (true) {
            if (shouldStop()) {
                interruptFishing()
                break
            }

            if (!obj.isSpawned(world)) {
                if (fishedCount == 0) {
                    player.message("There's nothing here to fish right now.")
                }
                break
            }

            if (player.inventory.isFull) {
                if (fishedCount == 0) {
                    player.message("You need some inventory space to fish here.")
                }
                break
            }

            // Re-check for tool and bait
            if (!player.inventory.contains(entry.toolId) && !player.equipment.contains(entry.toolId)) {
                break
            }
            if (entry.baitId != null && !player.inventory.contains(entry.baitId!!)) {
                player.message("You've run out of fishing bait.")
                break
            }

            if (shouldStop()) {
                interruptFishing()
                return
            }

            player.faceTile(obj.tile)

            if (shouldStop()) {
                break
            }

            player.animate(entry.animation)

            val fishingDelay = 3 + world.random(0..2)
            var interrupted = false
            for (i in 0 until fishingDelay) {
                if (shouldStop()) {
                    interrupted = true
                    break
                }
                task.wait(1)
            }

            if (interrupted) {
                break
            }

            if (shouldStop() || !obj.isSpawned(world) || player.inventory.isFull) {
                break
            }

            // Consume bait if required
            if (entry.baitId != null) {
                player.inventory.remove(entry.baitId!!, 1)
            }

            rewardPlayer(player, entry)
            fishedCount++

            if (shouldStop()) {
                interruptFishing()
                return
            }
            task.wait(1)
        }

        if (fishedCount > 1) {
            player.message("You catch ${fishedCount} fish.")
        }
    }

    private fun rewardPlayer(player: Player, entry: FishingEntry) {
        val fish = rollFish(entry)
        player.addXp(Skills.FISHING, entry.experience)

        val transaction = player.inventory.add(item = fish.itemId, amount = 1)

        if (transaction.hasSucceeded()) {
            player.message("You catch a fish.")
        } else {
            player.message("You don't have enough inventory space.")
        }
    }

    private fun rollFish(entry: FishingEntry): FishLoot {
        if (entry.fish.size == 1) {
            return entry.fish.first()
        }
        val total = entry.fish.sumOf { it.weight }
        val roll = world.randomDouble() * total
        var cumulative = 0.0
        entry.fish.forEach { fish ->
            cumulative += fish.weight
            if (roll < cumulative) {
                return fish
            }
        }
        return entry.fish.last()
    }
}

