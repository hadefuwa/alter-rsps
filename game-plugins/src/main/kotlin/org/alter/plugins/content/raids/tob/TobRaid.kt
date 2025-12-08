package org.alter.plugins.content.raids.tob

import java.util.concurrent.CopyOnWriteArrayList
import org.alter.api.ext.*
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.instance.InstancedChunkSet
import org.alter.game.model.instance.InstancedMap

class TobRaid(val world: World, val instance: InstancedMap, val party: List<Player>) {

    // 0 = Maiden, 1 = Bloat, 2 = Nylocas, 3 = Sotetseg, 4 = Xarpus, 5 = Verzik, 6 = Reward
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
    val bloodPools = CopyOnWriteArrayList<Tile>()

    fun start() {
        if (!active) return
        party.forEach { player ->
            player.message("<col=ff0000>Welcome to the Theatre of Blood!</col>")
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
    }

    fun nextRoom() {
        cleanupRoom()
        currentRoomIndex++
        if (currentRoomIndex > 6) {
            // Index 6 is reward room, > 6 means finished
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
        val roomOffsetX = roomIndex * 64 // Each room is 64x64

        // Calculate relative offset based on global spawn tile and its region base
        val (relX, relZ, relH) = getRelativeSpawn(roomIndex)

        val targetTile = base.transform(roomOffsetX + relX, relZ, relH)
        player.tile = targetTile
    }

    private fun spawnRoom(roomIndex: Int) {
        val base = instance.area.bottomLeft
        val roomOffsetX = roomIndex * 64

        val (relX, relZ, relH) = getRelativeSpawn(roomIndex)
        val spawnTile = base.transform(roomOffsetX + relX, relZ, relH)

        when (roomIndex) {
            0 -> spawnMaiden(spawnTile)
            1 -> spawnBloat(spawnTile)
            2 -> spawnNylocas(spawnTile)
            3 -> spawnSotetseg(spawnTile)
            4 -> spawnXarpus(spawnTile)
            5 -> spawnVerzik(spawnTile)
            6 -> {
                /* Reward room - just teleport players there */
            }
        }
    }

    private fun getRelativeSpawn(roomIndex: Int): Triple<Int, Int, Int> {
        return when (roomIndex) {
            0 -> { // Maiden
                val globalX = TobConstants.MAIDEN_SPAWN.first
                val globalZ = TobConstants.MAIDEN_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            1 -> { // Bloat (Height 1)
                val globalX = TobConstants.BLOAT_SPAWN.first
                val globalZ = TobConstants.BLOAT_SPAWN.second
                val globalH = TobConstants.BLOAT_SPAWN.third
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, globalH)
            }
            2 -> { // Nylocas
                val globalX = TobConstants.NYLOCAS_SPAWN.first
                val globalZ = TobConstants.NYLOCAS_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            3 -> { // Sotetseg
                val globalX = TobConstants.SOTETSEG_SPAWN.first
                val globalZ = TobConstants.SOTETSEG_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            4 -> { // Xarpus
                val globalX = TobConstants.XARPUS_SPAWN.first
                val globalZ = TobConstants.XARPUS_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            5 -> { // Verzik
                val globalX = TobConstants.VERZIK_SPAWN.first
                val globalZ = TobConstants.VERZIK_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            6 -> { // Reward
                val globalX = TobConstants.REWARD_SPAWN.first
                val globalZ = TobConstants.REWARD_SPAWN.second
                val regionBaseX = (globalX shr 6) shl 6
                val regionBaseZ = (globalZ shr 6) shl 6
                Triple(globalX - regionBaseX, globalZ - regionBaseZ, 0)
            }
            else -> Triple(32, 32, 0)
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
            player.tile =
                    org.alter.game.model.Tile(TobConstants.EXIT_TILE_X, TobConstants.EXIT_TILE_Z, 0)
        }
    }

    private fun finishRaid() {
        completed = true
        active = false
        party.forEach { player ->
            player.message(
                    "<col=ff0000>Congratulations! You have completed the Theatre of Blood!</col>"
            )
            player.tile =
                    org.alter.game.model.Tile(TobConstants.EXIT_TILE_X, TobConstants.EXIT_TILE_Z, 0)
        }
    }

    // --- Spawning Logic ---
    private fun spawnMaiden(tile: Tile) {
        val npc = Npc(TobConstants.MAIDEN_NPC_ID, tile, world)
        world.spawn(npc)
        roomNpcs.add(npc)
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

            // Map regions linearly: 0, 1, 2, 3, 4, 5, 6 (Reward)
            // Each "step" is 8 chunks (1 region width)

            // Maiden (Region 13125)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_MAIDEN), 0, 0)

            // Bloat (Region 12612)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_BLOAT), 8, 0)

            // Nylocas (Region 12613)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_NYLOCAS), 16, 0)

            // Sotetseg (Region 13123)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_SOTETSEG), 24, 0)

            // Xarpus (Region 13123)
            // Note: Use offset 32 for the next X slot
            // Since it's the SAME region as Sote, we just copy it again to a new instance slot
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_XARPUS), 32, 0)

            // Verzik (Region 12611)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_VERZIK), 40, 0)

            // Reward (Region 12867)
            copyRegion(builder, Tile.fromRegion(TobConstants.REGION_REWARD), 48, 0)

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
