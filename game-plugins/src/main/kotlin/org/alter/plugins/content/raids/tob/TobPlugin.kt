package org.alter.plugins.content.raids.tob

import java.util.concurrent.ConcurrentHashMap
import org.alter.api.ext.* // IMPORT EXTENSIONS for player, message, etc
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.instance.InstancedMap
import org.alter.game.model.instance.InstancedMapAttribute
import org.alter.game.model.instance.InstancedMapConfiguration
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class TobPlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {
    init {
        onObjOption(TobConstants.ENTER_RAID_OBJECT, option = "enter") {
            if (player.tile.regionId != TobConstants.REGION_MAIDEN
            ) { // Simple check to ensure we are at lobby
                TobService.startRaid(player)
            }
        }

        // Global raid tick? Or use instance cycle?
        // We can hook into World cycle via a task or just rely on NPC/Player ticks.
        // But for things like "Blood pool healing" independently of NPC combat, we might want a
        // controller.
        // Best usage is binding logic to the specific Npc or the Raid instance tick.
    }
}

object TobService {
    val activeRaids = ConcurrentHashMap<InstancedMap, TobRaid>()

    fun startRaid(player: Player) {
        val world = player.world
        val party = listOf(player) // Extend to clan support later

        val chunks = TobRaid.buildTobChunks()
        val config =
                InstancedMapConfiguration.Builder()
                        .setExitTile(Tile(TobConstants.EXIT_TILE_X, TobConstants.EXIT_TILE_Z, 0))
                        .setOwner(player.uid)
                        .addAttribute(InstancedMapAttribute.DEALLOCATE_ON_LOGOUT)
                        .addAttribute(
                                InstancedMapAttribute.DEALLOCATE_ON_DEATH
                        ) // If owner dies? Or handle internally.
                        // Actually TOB handles death internally (viewing area).
                        // If we use DEALLOCATE_ON_DEATH, the instance vanishes on owner death while
                        // party might be alive.
                        // Better NOT to use DEALLOCATE_ON_DEATH if we want custom wipe logic.
                        // But we can use DEALLOCATE_ON_LOGOUT.
                        .setBypassObjectChunkBounds(true)
                        .build()

        val instance = world.instanceAllocator.allocate(world, chunks, config)
        if (instance != null) {
            val raid = TobRaid(world, instance, party)
            activeRaids[instance] = raid
            raid.start()
        } else {
            player.message("Could not create Theatre of Blood instance.")
        }
    }

    fun getRaid(instance: InstancedMap): TobRaid? = activeRaids[instance]
}
