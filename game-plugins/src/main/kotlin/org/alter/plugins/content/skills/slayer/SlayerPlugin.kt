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

object Slayer {
    val SLAYER_TASK_ATTR = AttributeKey<Int>("slayer_task") // Now stores NPC ID directly
    val SLAYER_AMOUNT_ATTR = AttributeKey<Int>("slayer_amount")
    val SLAYER_PROGRESS_ATTR = AttributeKey<Int>("slayer_progress")
    val SLAYER_MASTER_ATTR = AttributeKey<Int>("slayer_master")
    val SLAYER_POINTS_ATTR = AttributeKey<Int>("slayer_points")
    
    fun getSlayerPoints(player: Player): Int {
        return player.attr[SLAYER_POINTS_ATTR] ?: 0
    }
    
    fun addSlayerPoints(player: Player, amount: Int) {
        val current = getSlayerPoints(player)
        player.attr[SLAYER_POINTS_ATTR] = current + amount
        player.message("You have been awarded $amount slayer point${if (amount == 1) "" else "s"}. Total: ${current + amount}")
    }
    
    fun removeSlayerPoints(player: Player, amount: Int): Boolean {
        val current = getSlayerPoints(player)
        if (current < amount) {
            return false
        }
        player.attr[SLAYER_POINTS_ATTR] = current - amount
        return true
    }

    /**
     * NPC IDs that should be excluded from slayer tasks.
     * Add NPC IDs here to exclude them from being assigned as slayer tasks.
     * Example: 680, 681 (Giant Skeleton)
     */
    private val excludedNpcIds = setOf<Int>(
        680, 681, // Giant Skeleton - add more IDs here as needed
    )

    /**
     * NPC name patterns that should be excluded from slayer tasks.
     * NPCs whose names contain any of these patterns (case-insensitive) will be excluded.
     * Example: "giant skeleton" will exclude all NPCs with "giant skeleton" in their name.
     */
    private val excludedNamePatterns = listOf(
        "shop", "banker", "guard", "soldier", "knight", "wizard", "priest", "monk",
        "merchant", "trader", "farmer", "fisherman", "cook", "bartender", "nurse",
        "tutor", "master", "teacher", "guide", "leprechaun", "null", "spawn",
        "rock", "tentacle", "head", "wing", "twig", "pile",
        "giant skeleton", "zombie swab", "assassin", "assasin", "angry goblin", "baboon thrall", "rebel warrior", "elidinis warden", "rooster", "mourner", // Add more name patterns here as needed
        "fear repear", "strangled", "prince itzla arkan"
        )

    // Cache for valid NPC IDs (lazy initialization)
    private var cachedValidNpcIds: List<Int>? = null

    /**
     * Checks if an NPC is valid for slayer tasks
     */
    private fun isValidSlayerNpc(npcId: Int): Boolean {
        if (npcId in excludedNpcIds) return false
        
        return try {
            val npcDef = getNpc(npcId)
            // Exclude NPCs with invalid names
            if (npcDef.name.isBlank() || npcDef.name.lowercase() == "null") return false
            
            // Exclude NPCs with excluded name patterns
            val nameLower = npcDef.name.lowercase()
            if (excludedNamePatterns.any { nameLower.contains(it) }) return false
            
            // Only include combat NPCs (combat level > 0)
            // Note: Some NPCs might have combat level -1 or 0, we want those excluded
            npcDef.combatLevel > 0
        } catch (e: Exception) {
            // If we can't get the NPC definition, exclude it
            false
        }
    }

    /**
     * Gets a list of valid NPC IDs for slayer tasks
     * Scans a reasonable range of NPC IDs (0-20000 should cover most NPCs)
     * Results are cached for performance
     */
    private fun getValidNpcIds(): List<Int> {
        if (cachedValidNpcIds != null) {
            return cachedValidNpcIds!!
        }
        
        val validIds = mutableListOf<Int>()
        // Scan NPC IDs from 0 to 20000 (adjust range if needed)
        for (npcId in 0..20000) {
            if (isValidSlayerNpc(npcId)) {
                validIds.add(npcId)
            }
        }
        
        cachedValidNpcIds = validIds
        return validIds
    }

    fun assign(player: Player, master: SlayerMaster) {
        // Get all valid NPC IDs
        val validNpcIds = getValidNpcIds()
        
        if (validNpcIds.isEmpty()) {
            player.message("No valid slayer tasks available.")
            return
        }

        // Pick a random NPC
        val npcId = validNpcIds.random()
        val npcDef = getNpc(npcId)
        
        // Calculate task amount based on NPC combat level
        // Higher combat level = more kills required
        val baseAmount = when {
            npcDef.combatLevel >= 200 -> player.world.random(50..100)
            npcDef.combatLevel >= 100 -> player.world.random(30..60)
            npcDef.combatLevel >= 50 -> player.world.random(20..40)
            npcDef.combatLevel >= 20 -> player.world.random(15..30)
            else -> player.world.random(10..25)
        }

        player.attr[SLAYER_TASK_ATTR] = npcId
        player.attr[SLAYER_AMOUNT_ATTR] = baseAmount
        player.attr[SLAYER_PROGRESS_ATTR] = 0
        player.attr[SLAYER_MASTER_ATTR] = master.id

        val npcName = npcDef.name.lowercase()
        player.message("Your new task is to kill $baseAmount $npcName.")
    }

    fun checkProgress(player: Player) {
        val npcId = player.attr[SLAYER_TASK_ATTR]
        if (npcId == null) {
            player.message("You do not have a slayer task.")
            return
        }

        val npcDef = try {
            getNpc(npcId)
        } catch (e: Exception) {
            player.message("You have an invalid slayer task. Please reset it.")
            return
        }

        val amount = player.attr[SLAYER_AMOUNT_ATTR] ?: 0
        val progress = player.attr[SLAYER_PROGRESS_ATTR] ?: 0
        val left = amount - progress

        val npcName = npcDef.name.lowercase()
        player.message("You are assigned to kill $npcName; only $left more to go.")
    }
}

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
                val coinAmount = 500_000
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
    }
}
