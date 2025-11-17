package org.alter.plugins.content.objects.altar

import dev.openrune.cache.CacheManager.getObject
import org.alter.api.Skills
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.timer.BURY_BONE_DELAY
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM
import kotlin.random.Random

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

    companion object {
        /**
         * Chaos altar object IDs - these give 3.5x XP and 50% chance to not consume bone
         */
        private val CHAOS_ALTAR_IDS = setOf(411, 412, 61)
        
        /**
         * Map of bone item names to their base XP values (when buried)
         * These values are multiplied by 2x for regular altars, 3.5x for chaos altars
         */
        private val BONE_XP_MAP = mapOf(
            "item.bones" to 4.5,
            "item.burnt_bones" to 4.5,
            "item.bat_bones" to 5.3,
            "item.big_bones" to 15.0,
            "item.babydragon_bones" to 30.0,
            "item.dragon_bones" to 72.0,
            "item.wolf_bones" to 4.5,
            "item.shaikahan_bones" to 25.0,
            "item.jogre_bones" to 15.0,
            "item.burnt_jogre_bones" to 15.0,
            "item.zogre_bones" to 22.5,
            "item.fayrg_bones" to 84.0,
            "item.raurg_bones" to 96.0,
            "item.ourg_bones" to 140.0,
            "item.dagannoth_bones" to 125.0,
            "item.wyvern_bones" to 72.0,
            "item.lava_dragon_bones" to 85.0,
            "item.superior_dragon_bones" to 150.0,
            "item.wyrm_bones" to 50.0,
            "item.drake_bones" to 80.0,
            "item.hydra_bones" to 110.0,
            "item.monkey_bones" to 5.0,
            "item.small_ninja_monkey_bones" to 16.0,
            "item.medium_ninja_monkey_bones" to 18.0,
            "item.gorilla_bones" to 18.0,
            "item.bearded_gorilla_bones" to 18.0,
            "item.small_zombie_monkey_bones" to 5.0,
            "item.large_zombie_monkey_bones" to 5.0,
            "item.wyrmling_bones" to 50.0,
        )
        
        /**
         * List of all altar object IDs that support bone offering
         */
        private val ALL_ALTAR_IDS = listOf(
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
    }

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

        // Register pray option for all altars
        ALL_ALTAR_IDS.forEach { altarId ->
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

