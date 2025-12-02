package org.alter.plugins.content.commands.commands.developer

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.priv.Privilege
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

class ItemstatsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        onCommand("itemstats", Privilege.DEV_POWER, description = "Show item stats and cache info") {
            val args = player.getCommandArgs()
            if (args.isEmpty()) {
                player.message("Usage: ::itemstats <item_id>")
                return@onCommand
            }
            
            val itemId = args[0].toIntOrNull()
            if (itemId == null) {
                player.message("Invalid item ID: ${args[0]}")
                return@onCommand
            }
            
            val def = getItem(itemId)
            if (def.id == -1 || def.name == "null") {
                player.message("Item ID $itemId not found in cache.")
                return@onCommand
            }
            
            player.message("=== Item Stats: ${def.name} (ID: $itemId) ===")
            player.message("Name: ${def.name}")
            player.message("Examine: ${def.examine ?: "N/A"}")
            player.message("Equip Slot: ${def.equipSlot} (${if (def.equipSlot == -1) "Not equipable" else "Equipable"})")
            player.message("Weapon Type: ${def.weaponType}")
            player.message("Attack Speed: ${def.attackSpeed}")
            player.message("Weight: ${def.weight}kg")
            player.message("Tradeable: ${def.isTradeable}")
            
            if (def.equipSlot != -1) {
                try {
                    // Check if bonuses is initialized by trying to access it
                    val bonuses = def.bonuses
                    if (bonuses.size >= 14) {
                        player.message("")
                        player.message("=== Combat Bonuses ===")
                        player.message("Attack Stab: ${bonuses[0]}")
                        player.message("Attack Slash: ${bonuses[1]}")
                        player.message("Attack Crush: ${bonuses[2]}")
                        player.message("Attack Magic: ${bonuses[3]}")
                        player.message("Attack Ranged: ${bonuses[4]}")
                        player.message("Defence Stab: ${bonuses[5]}")
                        player.message("Defence Slash: ${bonuses[6]}")
                        player.message("Defence Crush: ${bonuses[7]}")
                        player.message("Defence Magic: ${bonuses[8]}")
                        player.message("Defence Ranged: ${bonuses[9]}")
                        player.message("Melee Strength: ${bonuses[10]}")
                        player.message("Ranged Strength: ${bonuses[11]}")
                        player.message("Magic Damage: ${bonuses[12]}%")
                        player.message("Prayer: ${bonuses[13]}")
                    } else {
                        player.message("")
                        player.message("=== Combat Bonuses ===")
                        player.message("Bonuses array size is ${bonuses.size}, expected 14.")
                    }
                } catch (e: kotlin.UninitializedPropertyAccessException) {
                    player.message("")
                    player.message("=== Combat Bonuses ===")
                    player.message("Bonuses not initialized for this item.")
                } catch (e: Exception) {
                    player.message("")
                    player.message("=== Combat Bonuses ===")
                    player.message("Error reading bonuses: ${e.message}")
                }
            }
            
            // Show cache params (raw cache values before override)
            player.message("")
            player.message("=== Cache Params (Raw Cache Values) ===")
            val cacheParams = def.params ?: emptyMap()
            if (cacheParams.isEmpty()) {
                player.message("No cache params found (item may not have stats in cache)")
            } else {
                val paramNames = mapOf(
                    0 to "Stab Attack",
                    1 to "Slash Attack",
                    2 to "Crush Attack",
                    3 to "Magic Attack",
                    4 to "Ranged Attack",
                    5 to "Stab Defence",
                    6 to "Slash Defence",
                    7 to "Crush Defence",
                    8 to "Magic Defence",
                    9 to "Ranged Defence",
                    10 to "Melee Strength",
                    11 to "Prayer Bonus",
                    14 to "Attack Rate",
                    189 to "Ranged Strength",
                    299 to "Magic Damage (x10)"
                )
                
                paramNames.forEach { (key, name) ->
                    val value = cacheParams[key]
                    if (value != null) {
                        val displayValue = if (key == 299) (value as? Int ?: 0) / 10 else value
                        player.message("$name (param $key): $displayValue")
                    }
                }
            }
            
            // Check if skill requirements exist
            if (def.skillReqs != null && def.skillReqs!!.isNotEmpty()) {
                player.message("")
                player.message("=== Skill Requirements ===")
                def.skillReqs!!.forEach { (skillId, level) ->
                    val skillNames = mapOf(
                        0 to "Attack", 1 to "Defence", 2 to "Strength", 3 to "Hitpoints",
                        4 to "Ranged", 5 to "Prayer", 6 to "Magic", 7 to "Cooking",
                        8 to "Woodcutting", 9 to "Fletching", 10 to "Fishing",
                        11 to "Firemaking", 12 to "Crafting", 13 to "Smithing",
                        14 to "Mining", 15 to "Herblore", 16 to "Agility",
                        17 to "Thieving", 18 to "Slayer", 19 to "Farming",
                        20 to "Runecrafting", 21 to "Hunter", 22 to "Construction"
                    )
                    player.message("${skillNames[skillId.toInt()] ?: "Skill $skillId"}: $level")
                }
            }
            
            player.message("")
            player.message("Note: These are the CURRENT loaded values.")
            player.message("If an override file exists, it has replaced cache values.")
            player.message("To check cache-only values, temporarily rename the override file.")
        }
    }
}

