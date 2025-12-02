package org.alter.plugins.content.skills.firemaking

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.EntityType
import org.alter.game.model.World
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class FiremakingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val logData = mapOf(
        "item.logs" to FiremakingData(1, 40.0),
        "item.oak_logs" to FiremakingData(15, 60.0),
        "item.willow_logs" to FiremakingData(30, 90.0),
        "item.maple_logs" to FiremakingData(45, 135.0),
        "item.yew_logs" to FiremakingData(60, 202.5),
        "item.magic_logs" to FiremakingData(75, 303.8),
    )

    private val tinderboxName = "item.tinderbox"
    // Fire object ID - using fire_3769 as it exists in RSCM
    private val fireObjectId = getRSCM("object.fire_3769")

    init {
        // Use logs on tinderbox
        // Note: bindItemOnItem normalizes order, so we only need to bind once per pair
        logData.keys.forEach { logName ->
            onItemOnItem(item1 = logName, item2 = tinderboxName) {
                player.queue { lightFire(this, player, logName) }
            }
        }
    }

    private suspend fun lightFire(task: QueueTask, player: Player, logName: String) {
        val data = logData[logName] ?: return
        val level = player.getSkills().getCurrentLevel(Skills.FIREMAKING)
        val logId = getRSCM(logName)
        val tinderboxId = getRSCM(tinderboxName)

        if (level < data.level) {
            player.message("You need a Firemaking level of ${data.level} to light these logs.")
            return
        }

        if (!player.inventory.contains(logId)) {
            return
        }

        if (!player.inventory.contains(tinderboxId)) {
            player.message("You need a tinderbox to light a fire.")
            return
        }

        // Check if there's space for fire - check if there's already an object on this tile
        val tile = player.tile
        val chunk = world.chunks.getOrCreate(tile)
        val existingObjects = chunk.getEntities<GameObject>(
            tile,
            EntityType.STATIC_OBJECT,
            EntityType.DYNAMIC_OBJECT
        )
        if (existingObjects.isNotEmpty()) {
            player.message("You can't light a fire here.")
            return
        }

        player.lock()
        try {
            player.animate(Animation.FIREMAKING_TINDERBOX)
            task.wait(3)

            if (!player.inventory.contains(logId)) {
                return
            }

            player.inventory.remove(logId, 1)
            player.addXp(Skills.FIREMAKING, data.experience)

            // Spawn fire
            val fire = DynamicObject(id = fireObjectId, type = 10, rot = 0, tile = tile)
            world.spawn(fire)

            player.message("The fire catches and the logs begin to burn.")

            // Remove fire after delay
            world.queue {
                wait(100) // Fire lasts ~60 seconds
                if (world.isSpawned(fire)) {
                    world.remove(fire)
                    world.spawn(DynamicObject(id = getRSCM("object.fire_remains"), type = 10, rot = 0, tile = tile))
                }
            }
        } finally {
            player.unlock()
        }
    }

    private data class FiremakingData(
        val level: Int,
        val experience: Double
    )
}

