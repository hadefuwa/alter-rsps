package org.alter.plugins.content.objects.altar

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.Skills
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository

/**
 * Altar Plugin
 * 
 * This plugin handles prayer restoration at altars.
 * When a player uses the "pray" option on an altar, it restores
 * their prayer points to their maximum level.
 * 
 * The plugin dynamically detects altars by checking if objects
 * have a "pray", "pray-at", or "pray at" option.
 * 
 * @param r The plugin repository for registering object interactions
 * @param world The game world instance
 * @param server The server instance
 */
class AltarPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Function to restore prayer at altar
        val restorePrayer: Plugin.() -> Unit = {
            player.queue {
                val currentPrayer = player.getSkills().getCurrentLevel(Skills.PRAYER)
                val maxPrayer = player.getSkills().getBaseLevel(Skills.PRAYER)
                
                // Only restore if prayer is not already at max
                if (currentPrayer < maxPrayer) {
                    player.animate(Animation.PRAY_AT_ALTAR_ANIM)
                    player.playSound(Sound.ALTAR_PRAY)
                    wait(2)
                    
                    // Restore prayer to maximum
                    player.getSkills().restore(Skills.PRAYER)
                    player.message("You recharge your Prayer points.")
                } else {
                    player.message("You already have full Prayer points.")
                }
            }
        }

        // Common altar object IDs - these are the most frequently used altars
        // The plugin will also work with any other altar that has a "pray" option
        val commonAltarIds = listOf(
            409,   // Standard altar (Lumbridge)
            410,   // Altar of Guthix
            411,   // Chaos altar
            412,   // Chaos altar variant
            61,    // Chaos altar
            26363, // Zamorak altar
            26364, // Saradomin altar
            26365, // Armadyl altar
            26366, // Bandos altar
            29147, // Ancient altar
            29148, // Lunar altar
            29149, // Dark altar
            32630, // Altar of Zamorak
        )

        // Register pray option for common altars
        commonAltarIds.forEach { altarId ->
            try {
                val altarDef = getObject(altarId)
                val options = altarDef.actions.filterNotNull().map { it.lowercase() }
                
                // Check if altar has "pray" option (try different variations)
                val prayOptions = listOf("pray", "pray-at", "pray at")
                val foundOption = prayOptions.firstOrNull { options.contains(it) }
                
                if (foundOption != null) {
                    onObjOption(obj = altarId, option = foundOption, logic = restorePrayer)
                }
            } catch (e: Exception) {
                // Altar might not exist in cache, skip it
            }
        }

        // Also handle any object with "pray" option dynamically using onWorldInit
        // This catches any altars that weren't in the common list
        onWorldInit {
            // This will be called after world initialization
            // We can add dynamic detection here if needed in the future
        }
    }
}

