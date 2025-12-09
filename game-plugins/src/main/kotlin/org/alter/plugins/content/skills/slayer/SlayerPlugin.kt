package org.alter.plugins.content.skills.slayer

import dev.openrune.cache.CacheManager.getNpc
import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.TimeConstants
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.doompoints.DoomPoints

class SlayerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onAnyNpcDeath {
            val npc = ctx as Npc
            
            // Find all players who dealt damage to this NPC
            val playersWhoDamaged = mutableListOf<Player>()
            npc.world.players.forEach { player ->
                if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                    playersWhoDamaged.add(player)
                }
            }
            
            // Get the player who dealt the most damage
            val killer = if (playersWhoDamaged.isNotEmpty()) {
                playersWhoDamaged.maxByOrNull { npc.damageMap.getDamageFrom(it) }
            } else {
                // Fallback to getMostDamage() if no players found in the list
                npc.damageMap.getMostDamage() as? Player
            }
            
            if (killer == null) {
                return@onAnyNpcDeath
            }

            val taskNpcId = killer.attr[Slayer.SLAYER_TASK_ATTR] ?: return@onAnyNpcDeath

            // Get the task NPC definition to compare names
            val taskNpcDef = try {
                getNpc(taskNpcId)
            } catch (e: Exception) {
                // If we can't get the task NPC definition, just compare IDs
                null
            }

            // Check if the killed NPC matches the assigned NPC ID
            // Also check by name to handle NPC variants (e.g., crawling_hand_448 vs crawling_hand_453)
            val idMatches = npc.id == taskNpcId
            val nameMatches = taskNpcDef != null && npc.name.lowercase() == taskNpcDef.name.lowercase()
            
            // Special case: If task is a TzHaar NPC, allow any TzHaar NPC to count
            val tzhaarMatches = if (taskNpcDef != null) {
                val taskNameLower = taskNpcDef.name.lowercase()
                val killedNameLower = npc.name.lowercase()
                // Check if both are TzHaar NPCs (name contains "tzhaar")
                (taskNameLower.contains("tzhaar") || taskNameLower.contains("tz-haar")) &&
                (killedNameLower.contains("tzhaar") || killedNameLower.contains("tz-haar"))
            } else {
                false
            }
            
            if (idMatches || nameMatches || tzhaarMatches) {
                val amount = killer.attr[Slayer.SLAYER_AMOUNT_ATTR] ?: 0
                val progress = (killer.attr[Slayer.SLAYER_PROGRESS_ATTR] ?: 0) + 1

                killer.attr[Slayer.SLAYER_PROGRESS_ATTR] = progress
                
                // Add XP based on NPC hitpoints (or use a base amount)
                val xpGain = npc.combatDef.hitpoints.toDouble().coerceAtLeast(1.0)
                killer.addXp(Skills.SLAYER, xpGain)

                // Drop 500k coins for slayer task kill
                val coinItemId = 995
                var coinAmount = 500_000
                
                // Apply doom points coin multiplier perk
                val coinMultiplier = DoomPoints.getCoinMultiplier(killer)
                if (coinMultiplier > 0) {
                    coinAmount = (coinAmount * (1.0 + coinMultiplier / 100.0)).toInt()
                }
                
                val coinGroundItem = GroundItem(
                    item = coinItemId,
                    amount = coinAmount,
                    tile = npc.tile,
                    owner = killer
                )
                coinGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                coinGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                coinGroundItem.ownerShipType = 1
                npc.world.spawn(coinGroundItem)
                killer.message("Slayer task bonus: ${coinAmount} coins!")

                if (progress >= amount) {
                    killer.message("You have completed your slayer task! Return to a slayer master.")
                    // Award slayer points (20 points per task)
                    Slayer.addSlayerPoints(killer, 20)
                    killer.attr.remove(Slayer.SLAYER_TASK_ATTR)
                    killer.attr.remove(Slayer.SLAYER_AMOUNT_ATTR)
                    killer.attr.remove(Slayer.SLAYER_PROGRESS_ATTR)
                } else {
                    val left = amount - progress
                    killer.message("Slayer task: $progress/$amount ${npc.name.lowercase()} killed. $left remaining.")
                }
            }
        }
        
        // Command for testing
        onCommand("slayer") {
            val p = player
            Slayer.checkProgress(p)
        }
        
        // Command to check slayer points
        onCommand("slayerpoints") {
            val p = player
            val points = Slayer.getSlayerPoints(p)
            p.message("You have $points slayer point${if (points == 1) "" else "s"}.")
        }
        
        onCommand("resettask") {
            val p = player
            p.attr.remove(Slayer.SLAYER_TASK_ATTR)
            p.attr.remove(Slayer.SLAYER_AMOUNT_ATTR)
            p.attr.remove(Slayer.SLAYER_PROGRESS_ATTR)
            p.message("Slayer task reset.")
        }
        
        onCommand("r") {
            val p = player
            p.attr.remove(Slayer.SLAYER_TASK_ATTR)
            p.attr.remove(Slayer.SLAYER_AMOUNT_ATTR)
            p.attr.remove(Slayer.SLAYER_PROGRESS_ATTR)
            p.message("Slayer task reset.")
        }
        
        // Debug command to check task details
        onCommand("slayerdebug") {
            val p = player
            val taskNpcId = p.attr[Slayer.SLAYER_TASK_ATTR]
            if (taskNpcId == null) {
                p.message("No slayer task assigned.")
                return@onCommand
            }
            
            val taskNpcDef = try {
                getNpc(taskNpcId)
            } catch (e: Exception) {
                p.message("Error getting task NPC definition: ${e.message}")
                return@onCommand
            }
            
            val amount = p.attr[Slayer.SLAYER_AMOUNT_ATTR] ?: 0
            val progress = p.attr[Slayer.SLAYER_PROGRESS_ATTR] ?: 0
            
            p.message("Slayer Task Debug:")
            p.message("  Task NPC ID: $taskNpcId")
            p.message("  Task NPC Name: ${taskNpcDef.name}")
            p.message("  Amount: $amount")
            p.message("  Progress: $progress")
            p.message("  Remaining: ${amount - progress}")
        }
        
        // Command to refresh the slayer NPC cache (useful after spawning new NPCs)
        onCommand("slayerrefresh") {
            val p = player
            Slayer.clearCache()
            p.message("Slayer NPC cache cleared. Next task assignment will use fresh data.")
        }
        
        // Debug command to list valid slayer NPCs (shows unique NPC names)
        onCommand("slayerlist") {
            val p = player
            val validNpcIds = Slayer.getSpawnedNpcIdsPublic(p.world)
            val npcNames = validNpcIds.mapNotNull { id ->
                try {
                    getNpc(id).name
                } catch (e: Exception) {
                    null
                }
            }.distinct().sorted()
            
            p.message("Valid slayer NPCs (${npcNames.size} unique types, ${validNpcIds.size} total spawns):")
            npcNames.take(20).forEach { name ->
                p.message("  - $name")
            }
            if (npcNames.size > 20) {
                p.message("  ... and ${npcNames.size - 20} more")
            }
        }
    }
}
