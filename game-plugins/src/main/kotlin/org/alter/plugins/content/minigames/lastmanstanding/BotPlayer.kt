package org.alter.plugins.content.minigames.lastmanstanding

import net.rsprot.protocol.common.client.OldSchoolClientType
import org.alter.api.Skills
import org.alter.api.ext.calculateAndSetCombatLevel
import org.alter.game.model.PlayerUID
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.World

/**
 * A bot player that replicates a real player for Last Man Standing minigame.
 * Bot players can be attacked and can attack other players, unlike NPCs.
 */
class BotPlayer(
    world: World,
    val botName: String,
    spawnTile: Tile
) : Player(world) {
    
    init {
        // Initialize basic player properties
        uid = PlayerUID("bot_$botName")
        username = botName
        tile = spawnTile
        
        // Register to world first to get an index
        if (!world.register(this)) {
            throw IllegalStateException("Failed to register bot player $botName")
        }
        
        // Initialize protocols (required for player to be visible)
        // Must be done after registration to get a valid index
        playerInfo = world.network.playerInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
        npcInfo = world.network.npcInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
        worldEntityInfo = world.network.worldEntityInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP)
        
        // Set maxed combat stats (like a maxed player)
        val skills = getSkills()
        skills.setBaseLevel(Skills.ATTACK, 99)
        skills.setBaseLevel(Skills.STRENGTH, 99)
        skills.setBaseLevel(Skills.DEFENCE, 99)
        skills.setBaseLevel(Skills.HITPOINTS, 99)
        skills.setBaseLevel(Skills.RANGED, 99)
        skills.setBaseLevel(Skills.MAGIC, 99)
        skills.setBaseLevel(Skills.PRAYER, 99)
        
        // Set current levels to match base levels
        skills.setCurrentLevel(Skills.ATTACK, 99)
        skills.setCurrentLevel(Skills.STRENGTH, 99)
        skills.setCurrentLevel(Skills.DEFENCE, 99)
        skills.setCurrentLevel(Skills.HITPOINTS, 99)
        skills.setCurrentLevel(Skills.RANGED, 99)
        skills.setCurrentLevel(Skills.MAGIC, 99)
        skills.setCurrentLevel(Skills.PRAYER, 99)
        
        // Calculate and set combat level
        calculateAndSetCombatLevel()
        
        // Initialize player info coordinates
        playerInfo.updateCoord(tile.height, tile.x, tile.z)
        npcInfo.updateCoord(-1, tile.height, tile.x, tile.z)
        worldEntityInfo.updateCoord(-1, tile.height, tile.x, tile.z)
        
        // Set appearance
        org.alter.game.info.PlayerInfo(this).syncAppearance()
        
        // Mark as initiated so it appears in the world
        initiated = true
        
        // Register to world chunks (this happens automatically via ChunkCreationTask, but we can do it manually too)
        world.chunks.getOrCreate(tile).addEntity(world, this, tile)
    }
    
    /**
     * Cleanup when bot is removed
     */
    fun cleanup() {
        world.unregister(this)
    }
}











