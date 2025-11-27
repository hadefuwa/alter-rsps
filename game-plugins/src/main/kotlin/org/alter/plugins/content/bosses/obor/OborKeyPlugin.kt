package org.alter.plugins.content.bosses.obor

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.timer.TimeConstants
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

/**
 * Obor Key (Giant Key) Plugin
 * 
 * Handles the Giant Key item which is required to access Obor's lair.
 * The key is dropped by Hill Giants and consumed upon entering Obor's chamber.
 * 
 * Giant Key Item ID: 20754 (from RSCM)
 * 
 * Features:
 * - Drop from Hill Giants
 * - Simple key drop system
 */
class OborKeyPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Giant Key item ID from RSCM (found in objs.csv as ID 20754)
         */
        private const val GIANT_KEY_ID = "item.giant_key" // Maps to ID 20754
        
        /**
         * Drop chance for Giant Key from Hill Giants (1 in 128 chance)
         */
        private const val GIANT_KEY_DROP_CHANCE = 128
    }

    init {
        // Set up Giant Key drops from Hill Giants
        setupGiantKeyDrops()
    }
    
    /**
     * Sets up Giant Key drops from all Hill Giant variants
     */
    private fun setupGiantKeyDrops() {
        val hillGiantTypes = listOf(
            "npc.hill_giant",
            "npc.hill_giant_2099", 
            "npc.hill_giant_2100",
            "npc.hill_giant_2101",
            "npc.hill_giant_2102",
            "npc.hill_giant_2103"
        )
        
        hillGiantTypes.forEach { npcType ->
            onNpcDeath(npcType) {
                val npc = this.npc
                
                // Check for Giant Key drop (1 in 128 chance)
                if (world.random(GIANT_KEY_DROP_CHANCE) == 0) {
                    // Find the player who dealt the most damage using the pattern from SlayerPlugin
                    val topDamager = npc.damageMap.getMostDamage() as? Player
                    if (topDamager != null) {
                        // Try to add to inventory first, if fails drop on ground (like SearchHayPlugin)
                        val addResult = topDamager.inventory.add(item = GIANT_KEY_ID)
                        if (addResult.hasFailed()) {
                            // Inventory full, drop on ground using exact SearchHayPlugin pattern
                            world.spawn(GroundItem(item = getRSCM(GIANT_KEY_ID), amount = 1, tile = npc.tile, owner = topDamager))
                        }
                    }
                }
            }
        }
    }
}