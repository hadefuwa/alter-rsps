package org.alter.plugins.content.raids.tob

import java.util.concurrent.CopyOnWriteArrayList
import org.alter.api.ext.* // Import for message, etc
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.instance.InstancedChunkSet
import org.alter.game.model.instance.InstancedMap

class TobRaid(val world: World, val instance: InstancedMap, val party: List<Player>) {

    // 0 = Maiden, 1 = Bloat, 2 = Nylocas, 3 = Sotetseg, 4 = Xarpus, 5 = Verzik
    var currentRoomIndex = 0
    var active = true
    var completed = false

    // Shared death count
    var deathCount = 0

    // Scaling factor based on party size
    val scaleFactor: Double
        get() =
                when (party.size) {
                    1 -> 1.0
                    2 -> 1.9
                    3 -> 2.8
                    4 -> 3.6
                    5 -> 4.5
                    else -> party.size * 0.9
                }

    val roomNpcs = CopyOnWriteArrayList<Npc>()
    val bloodPools = CopyOnWriteArrayList<Tile>() // Example for Maiden mechanic

    fun start() {
        if (!active) return
        party.forEach { player ->
            player.message("<col=ff0000>Welcome to the Theatre of Blood!</col>")
            // Teleport to Maiden room (Room 0)
            teleportToRoom(0, player)
        }
        spawnRoom(0)
    }

    fun process() {
        if (!active) return

        // Check for wipe
        val alivePlayers = party.count { !it.isDead() && instance.area.contains(it.tile) }
        if (alivePlayers == 0) {
            wipe()
            return
        }

        // Additional process logic (e.g. timers)
    }

    fun nextRoom() {
        cleanupRoom()
        currentRoomIndex++
        if (currentRoomIndex > 5) {
            finishRaid()
            return
        }

        party.forEach { player ->
            if (!player.isDead()) {
                teleportToRoom(currentRoomIndex, player)
                player.message("You advance to the next room...")
            }
        }
        spawnRoom(currentRoomIndex)
    }

    private fun teleportToRoom(roomIndex: Int, player: Player) {
        val base = instance.area.bottomLeft
        // Each room is 64 tiles wide (1 region) in our linear layout
        val roomOffsetX = roomIndex * 64

        // Approximate safe spawn for each room relative to its region start
        // These offsets are estimates and should be refined
        val (relX, relZ) =
                when (roomIndex) {
                    0 -> 32 to 32 // Maiden
                    1 -> 32 to 32 // Bloat
                    2 -> 32 to 32 // Nylocas
                    3 -> 32 to 32 // Sotetseg
                    4 -> 32 to 32 // Xarpus
                    5 -> 32 to 32 // Verzik
                    else -> 32 to 32
                }

        val targetTile = base.transform(roomOffsetX + relX, relZ, 0)
        player.tile = targetTile
    }

    private fun spawnRoom(roomIndex: Int) {
        val base = instance.area.bottomLeft
        val roomOffsetX = roomIndex * 64
        val centerTile = base.transform(roomOffsetX + 32, 32, 0)

        when (roomIndex) {
            0 -> spawnMaiden(centerTile)
            1 -> spawnBloat(centerTile)
            2 -> spawnNylocas(centerTile)
            3 -> spawnSotetseg(centerTile)
            4 -> spawnXarpus(centerTile)
            5 -> spawnVerzik(centerTile)
        }
    }

    private fun cleanupRoom() {
        roomNpcs.forEach { world.remove(it) }
        roomNpcs.clear()
        bloodPools.clear()
    }

    private fun wipe() {
        active = false
        party.forEach { player ->
            player.message("Your party has been wiped!")
            // Teleport to exit
            player.tile =
                    org.alter.game.model.Tile(TobConstants.EXIT_TILE_X, TobConstants.EXIT_TILE_Z, 0)
        }
        // Deallocate instance handled by world if setup correctly, or manually deallocate here
    }

    private fun finishRaid() {
        completed = true
        active = false
        party.forEach { player ->
            player.message(
                    "<col=ff0000>Congratulations! You have completed the Theatre of Blood!</col>"
            )
            // Teleport to Exit for now
            player.tile =
                    org.alter.game.model.Tile(TobConstants.EXIT_TILE_X, TobConstants.EXIT_TILE_Z, 0)
        }
    }

    // --- Spawning Logic ---

    private fun spawnMaiden(tile: Tile) {
        val npc = Npc(TobConstants.MAIDEN_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        // Set HP based on scale
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    private fun spawnBloat(tile: Tile) {
        val npc = Npc(TobConstants.BLOAT_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    private fun spawnNylocas(tile: Tile) {
        // Nylo waves logic is complex, just spawn boss for now
        val npc = Npc(TobConstants.NYLOCAS_BOSS_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    private fun spawnSotetseg(tile: Tile) {
        val npc = Npc(TobConstants.SOTETSEG_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    private fun spawnXarpus(tile: Tile) {
        val npc = Npc(TobConstants.XARPUS_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    private fun spawnVerzik(tile: Tile) {
        val npc = Npc(TobConstants.VERZIK_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
        val newHp = (npc.combatDef.hitpoints * scaleFactor).toInt()
        npc.combatDef = npc.combatDef.copy(hitpoints = newHp)
        npc.setCurrentHp(newHp)
    }

    companion object {
        fun buildTobChunks(): InstancedChunkSet {
            val builder = InstancedChunkSet.Builder()

            // Map regions linearly: 0, 1, 2, 3, 4, 5

            // Maiden (Region 12613)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_MAIDEN), 0, 0)

            // Bloat (Region 13125)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_BLOAT), 8, 0)

            // Nylocas (Region 13122)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_NYLOCAS), 16, 0)

            // Sotetseg (Region 13123)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_SOTETSEG), 24, 0)

            // Xarpus (Region 12612)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_XARPUS), 32, 0)

            // Verzik (Region 12611)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_VERZIK), 40, 0)

            return builder.build()
        }

        private fun copyRegion(
                builder: InstancedChunkSet.Builder,
                base: Tile,
                targetChunkX: Int,
                targetChunkY: Int
        ) {
            for (x in 0 until 8) {
                for (z in 0 until 8) {
                    for (h in 0 until 4) {
                        val copyTile = base.transform(x shl 3, z shl 3, h)
                        builder.set(targetChunkX + x, targetChunkY + z, h, 0, copyTile)
                    }
                }
            }
        }
    }
}
