package org.alter.plugins.content.objects.altar

import dev.openrune.cache.CacheManager.getItem
import dev.openrune.cache.CacheManager.getObject
import org.alter.api.Skills
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.INTERACTING_ITEM
import org.alter.game.model.attr.INTERACTING_ITEM_SLOT
import org.alter.game.model.attr.INTERACTING_OBJ_ATTR
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.BURY_BONE_DELAY
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.Plugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.asRSCM
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
                    this.wait(cycles = 2)
                    
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

        // Register bone offering for all bone types on all altars
        ALL_ALTAR_IDS.forEach { altarId ->
            try {
                // Convert altar ID to RSCM object name
                val altarObjName = altarId.asRSCM("object")
                BONE_XP_MAP.keys.forEach { boneItemName ->
                    onItemOnObj(obj = altarObjName, item = boneItemName) {
                        offerBoneOnAltar(player, altarId, boneItemName)
                    }
                }
            } catch (e: Exception) {
                // Altar might not exist in RSCM, skip it
                // This is fine - not all altars may be registered
            }
        }
    }

    /**
     * Handles offering bones on an altar
     * @param player The player offering the bone
     * @param altarId The altar object ID
     * @param boneItemName The bone item name (e.g., "item.dragon_bones")
     */
    private fun offerBoneOnAltar(player: Player, altarId: Int, boneItemName: String) {
        player.queue {
            // Get the interacting item and object
            val item = player.attr[INTERACTING_ITEM]?.get() ?: run {
                player.message("Nothing interesting happens.")
                return@queue
            }
            
            val obj = player.attr[INTERACTING_OBJ_ATTR]?.get() ?: run {
                player.message("Nothing interesting happens.")
                return@queue
            }
            
            // Verify the object matches the altar we're expecting
            // Check both base ID and transformed ID (in case object has transforms)
            val objId = obj.getTransform(player)
            if (objId != altarId && obj.id != altarId) {
                return@queue
            }
            
            // Verify the item matches the bone we're expecting
            val expectedItemId = try {
                getRSCM(boneItemName)
            } catch (e: Exception) {
                player.message("Nothing interesting happens.")
                return@queue
            }
            
            if (item.id != expectedItemId) {
                return@queue
            }
            
            // Check if player can interact with items
            if (!player.lock.canItemInteract()) {
                return@queue
            }
            
            // Get the inventory slot
            val inventorySlot = player.attr[INTERACTING_ITEM_SLOT] ?: -1
            if (inventorySlot < 0 || inventorySlot >= player.inventory.capacity) {
                player.message("You don't have that bone.")
                return@queue
            }
            
            // Verify the item at the slot matches
            val slotItem = player.inventory[inventorySlot]
            if (slotItem?.id != expectedItemId) {
                player.message("You don't have that bone.")
                return@queue
            }
            
            // Get base XP for this bone
            val baseXp = BONE_XP_MAP[boneItemName] ?: run {
                player.message("Nothing interesting happens.")
                return@queue
            }
            
            // Determine if this is a chaos altar
            val isChaosAltar = CHAOS_ALTAR_IDS.contains(altarId)
            
            // Calculate XP multiplier (2x for regular altars, 3.5x for chaos altars)
            val xpMultiplier = if (isChaosAltar) 3.5 else 2.0
            val xpGained = baseXp * xpMultiplier
            
            // Play animation and sound
            player.animate(Animation.OFFER_BONES_TO_ALTER_ANIM)
            player.playSound(Sound.ALTAR_PRAY)
            
            // Wait for animation
            this.wait(cycles = 2)
            
            // For chaos altars, 50% chance to not consume the bone
            val shouldConsumeBone = if (isChaosAltar) {
                Random.nextBoolean()
            } else {
                true
            }
            
            // Remove the bone from inventory (unless chaos altar saves it)
            if (shouldConsumeBone) {
                val remove = player.inventory.remove(
                    item = expectedItemId,
                    amount = 1,
                    beginSlot = inventorySlot,
                    assureFullRemoval = false
                )
                
                if (remove.hasSucceeded() && remove.completed > 0) {
                    // Add prayer experience
                    player.addXp(Skills.PRAYER, xpGained)
                    
                    // Get bone name for message
                    val boneName = getItem(expectedItemId).name.lowercase()
                    val bonePlural = if (boneName.endsWith("s", ignoreCase = false)) boneName else "$boneName bones"
                    
                    if (isChaosAltar) {
                        player.message("The gods are very pleased with your $bonePlural offering.")
                    } else {
                        player.message("The gods are pleased with your $bonePlural offering.")
                    }
                } else {
                    player.message("You don't have that bone.")
                }
            } else {
                // Chaos altar saved the bone - still give XP but don't remove bone
                player.addXp(Skills.PRAYER, xpGained)
                
                val boneName = getItem(expectedItemId).name.lowercase()
                val bonePlural = if (boneName.endsWith("s", ignoreCase = false)) boneName else "$boneName bones"
                player.message("The gods are very pleased with your $bonePlural offering. They don't require the bones.")
            }
        }
    }
}

