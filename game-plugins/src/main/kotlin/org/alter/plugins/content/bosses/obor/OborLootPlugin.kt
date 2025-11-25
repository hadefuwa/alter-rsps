package org.alter.plugins.content.bosses.obor

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Obor Loot Plugin
 * 
 * Handles enhanced loot drops for Obor the Hill Giant boss.
 * Provides better rewards than regular Hill Giants.
 * 
 * Enhanced loot includes:
 * - Guaranteed coins (500-2000)
 * - Guaranteed big bones
 * - Rare equipment drops
 */
class OborLootPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Set up Obor loot drops
        setupOborLoot()
        
        println("Obor Loot: Initialized enhanced loot system for Obor boss")
    }
    
    /**
     * Sets up enhanced loot drops for Obor boss
     */
    private fun setupOborLoot() {
        onNpcDeath("npc.obor") {
            val npc = this.npc
            
            // Get all players who dealt damage to Obor
            val playerDamagers = mutableListOf<Player>()
            npc.world.players.forEach { player ->
                if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                    playerDamagers.add(player)
                }
            }
            
            if (playerDamagers.isEmpty()) {
                return@onNpcDeath // No valid damage dealers
            }
            
            // Find the top damager for loot assignment
            val topDamager = playerDamagers.maxByOrNull { npc.damageMap.getDamageFrom(it) }
            if (topDamager != null) {
                generateOborLoot(npc, topDamager)
            }
        }
    }
    
    /**
     * Generates Obor's enhanced loot drops using working API patterns
     */
    private fun generateOborLoot(npc: Npc, player: Player) {
        
        // Guaranteed coins (500-2000)
        val coinAmount = Random.nextInt(500, 2001)
        world.spawn(GroundItem(item = getRSCM("item.coins"), amount = coinAmount, tile = npc.tile, owner = player))
        
        // Guaranteed big bones
        world.spawn(GroundItem(item = getRSCM("item.big_bones"), amount = 1, tile = npc.tile, owner = player))
        
        // Rare drops (10% chance each)
        val rareDrops = listOf(
            "item.rune_scimitar",
            "item.rune_battleaxe", 
            "item.rune_longsword",
            "item.rune_kiteshield"
        )
        
        rareDrops.forEach { item ->
            if (Random.nextInt(10) == 0) { // 10% chance
                world.spawn(GroundItem(item = getRSCM(item), amount = 1, tile = npc.tile, owner = player))
            }
        }
        
        println("Obor Loot: Generated enhanced loot for ${player.username}")
    }
}