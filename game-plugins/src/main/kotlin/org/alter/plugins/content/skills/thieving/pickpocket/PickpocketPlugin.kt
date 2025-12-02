package org.alter.plugins.content.skills.thieving.pickpocket

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.Skills
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.GroundItem
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.item.Item
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Global variable to toggle between old and new pickpocketing behavior.
 * Set to false to use the new enhanced pickpocketing system.
 * Set to true to use the original pickpocketing system.
 * 
 * Note: Coin pouch capacity increase (3x) would need to be implemented
 * in a coin pouch management system if one exists.
 */
var USE_OLD_PICKPOCKETING = false

class PickpocketPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    init {
        loadService(PickpocketService())

        onWorldInit {
            val service = world.getService(PickpocketService::class.java) ?: return@onWorldInit
            service.entries.forEach { entry ->
                entry.npcs.forEach { npcId ->
                    onNpcOption(npc = npcId, option = "pickpocket") {
                        player.queue {
                            if (USE_OLD_PICKPOCKETING) {
                                oldAttemptPickpocket(this, player, player.getInteractingNpc(), entry)
                            } else {
                                attemptPickpocket(this, player, player.getInteractingNpc(), entry)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Original pickpocketing implementation - wrapped for toggling.
     */
    private suspend fun oldAttemptPickpocket(task: QueueTask, player: Player, npc: Npc, entry: PickpocketEntry) {
        val npcName = npc.name.ifBlank { "target" }.lowercase()
        val level = player.getSkills().getCurrentLevel(Skills.THIEVING)

        if (level < entry.level) {
            player.message("You need a Thieving level of ${entry.level} to pickpocket this ${npcName}.")
            return
        }

        if (!canReceiveLoot(player, entry)) {
            player.message("You need some inventory space to pickpocket this ${npcName}.")
            return
        }

        player.facePawn(npc)
        player.lock()
        try {
            player.animate(Animation.THIEVING_PICKPOCKET)
            task.wait(2)

            val successChance = computeSuccessChance(level, entry)
            val success = player.world.randomDouble() <= successChance

            if (success) {
                onSuccess(player, npc, entry, npcName)
            } else {
                onFailure(player, npc, entry, npcName)
            }
        } finally {
            player.unlock()
        }
    }

    /**
     * New enhanced pickpocketing system:
     * - Continuously pickpockets until unable to continue
     * - Pickpockets all NPCs of the same type in 11x11 radius
     * - 100% success rate
     * - Items are noted
     * - Auto-repeat until inventory full or no valid targets
     */
    private suspend fun attemptPickpocket(task: QueueTask, player: Player, npc: Npc, entry: PickpocketEntry) {
        val npcName = npc.name.ifBlank { "target" }.lowercase()
        val level = player.getSkills().getCurrentLevel(Skills.THIEVING)

        if (level < entry.level) {
            player.message("You need a Thieving level of ${entry.level} to pickpocket this ${npcName}.")
            return
        }

        // Get all NPCs of the same type in 12x12 area (6 tiles in each direction from center)
        val centerTile = npc.tile
        val radius = 6 // 12x12 square = 6 tiles radius (center + 6 in each direction)
        val targetNpcs = mutableListOf<Npc>()
        
        // Iterate through all NPCs and find matching ones in radius
        val service = player.world.getService(PickpocketService::class.java) ?: return
        for (i in 0 until player.world.npcs.capacity) {
            val checkNpc = player.world.npcs[i] ?: continue
            if (!checkNpc.isSpawned()) continue
            
            // Check if NPC is of the same type (same entry) - NPCs in the same entry share the same pickpocket config
            val checkEntry = service.lookup(checkNpc.id) ?: continue
            if (checkEntry != entry) continue
            
            // Check if within 12x12 radius (6 tiles in each direction = 12x12 square)
            // Calculate Manhattan distance for square area
            val dx = kotlin.math.abs(checkNpc.tile.x - centerTile.x)
            val dz = kotlin.math.abs(checkNpc.tile.z - centerTile.z)
            if (checkNpc.tile.height == centerTile.height && dx <= radius && dz <= radius) {
                targetNpcs.add(checkNpc)
            }
        }
        
        // Ensure the original NPC is included (it should be, but just in case)
        if (!targetNpcs.contains(npc)) {
            targetNpcs.add(0, npc) // Add at beginning to prioritize original target
        }

        // Continuously pickpocket until we can't anymore
        var pickpocketedCount = 0
        var consecutiveFailures = 0
        val maxConsecutiveFailures = 10 // Stop if we fail 10 times in a row
        val startTile = player.tile // Track starting position to detect movement
        
        // Helper function to check if we should stop pickpocketing
        fun shouldStop(): Boolean {
            return !player.isOnline || 
                   player.hasMoveDestination() || 
                   !player.tile.sameAs(startTile)
        }
        
        // Helper function to interrupt and stop pickpocketing
        fun interruptPickpocketing() {
            player.interruptQueues() // Interrupt any queued tasks to stop pickpocketing
        }
        
        while (true) {
            // Check if player has moved or has movement queued - allow breaking out
            if (shouldStop()) {
                interruptPickpocketing()
                break
            }
            
            var foundValidTarget = false
            
            // Filter out dead/removed NPCs before each iteration
            val validNpcs = targetNpcs.filter { it.isSpawned() }
            if (validNpcs.isEmpty()) {
                break // No valid NPCs left, stop pickpocketing
            }
            
            // Try to pickpocket each NPC in the list
            for (targetNpc in validNpcs) {
                // Check for interruption before each pickpocket - if moving, stop immediately
                if (shouldStop()) {
                    interruptPickpocketing()
                    return
                }
                
                // Double-check NPC is still spawned (might have died between checks)
                if (!targetNpc.isSpawned()) {
                    continue
                }
                
                // Check if we can receive loot
                if (!canReceiveLoot(player, entry)) {
                    if (pickpocketedCount == 0) {
                        player.message("You need some inventory space to pickpocket.")
                    }
                    return
                }
                
                // Check one more time before proceeding - if player is trying to move, stop
                if (shouldStop()) {
                    interruptPickpocketing()
                    return
                }
                
                // Perform pickpocket (100% success) - NO LOCKING to allow movement interruption
                player.facePawn(targetNpc)
                
                // Check again right before animation
                if (shouldStop() || !targetNpc.isSpawned()) {
                    break
                }
                
                player.animate(Animation.THIEVING_PICKPOCKET)
                
                // Check during animation delay - allow breaking out every tick
                var interrupted = false
                for (i in 0 until 2) {
                    if (shouldStop() || !targetNpc.isSpawned()) {
                        interrupted = true
                        break
                    }
                    task.wait(1)
                }
                
                if (interrupted) {
                    break
                }
                
                // Final check before rewarding
                if (shouldStop() || !targetNpc.isSpawned()) {
                    break
                }
                
                // Always succeed in new system
                newOnSuccess(player, targetNpc, entry, targetNpc.name.ifBlank { "target" }.lowercase())
                pickpocketedCount++
                foundValidTarget = true
                consecutiveFailures = 0
                
                // Small delay between pickpockets with interruption check (unlocked)
                if (shouldStop()) {
                    interruptPickpocketing()
                    return
                }
                task.wait(1)
                
                // Break out of NPC loop if we should stop
                if (shouldStop()) {
                    interruptPickpocketing()
                    return
                }
            }
            
            // If we didn't find any valid targets, increment failure counter
            if (!foundValidTarget) {
                consecutiveFailures++
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    break
                }
                // Wait a bit before retrying, but check for interruption
                for (i in 0 until 2) {
                    if (shouldStop()) {
                        interruptPickpocketing()
                        return
                    }
                    task.wait(1)
                }
            } else {
                consecutiveFailures = 0
            }
            
            // Check if inventory is full
            if (player.inventory.isFull && !canReceiveLoot(player, entry)) {
                break
            }
        }
        
        if (pickpocketedCount > 0) {
            player.message("You pickpocketed ${pickpocketedCount} ${if (pickpocketedCount == 1) "NPC" else "NPCs"}.")
        }
    }

    private fun computeSuccessChance(level: Int, entry: PickpocketEntry): Double {
        val difference = level - entry.level
        val bonus = difference.coerceAtLeast(0) * entry.successBonusPerLevel
        return (entry.baseSuccess + bonus).coerceIn(0.05, 0.95)
    }

    /**
     * Original success handler - used by old pickpocketing system.
     */
    private fun onSuccess(player: Player, npc: Npc, entry: PickpocketEntry, npcName: String) {
        npc.resetFacePawn()

        player.addXp(Skills.THIEVING, entry.experience)

        val loot = rollLoot(entry, player.world)
        val amount = if (loot.min == loot.max) loot.min else player.world.random(loot.min..loot.max)
        val transaction = player.inventory.add(item = loot.item, amount = amount)
        if (transaction.hasFailed()) {
            player.world.spawn(
                GroundItem(
                    item = getRSCM(loot.item),
                    amount = amount,
                    tile = player.tile,
                    owner = player,
                ),
            )
        }

        player.message("You pick the ${npcName}'s pocket.")
    }

    /**
     * New success handler - notes items and grants XP.
     * Coins are multiplied by 100x, other items by 3x.
     */
    private fun newOnSuccess(player: Player, npc: Npc, entry: PickpocketEntry, npcName: String) {
        npc.resetFacePawn()

        player.addXp(Skills.THIEVING, entry.experience)

        val loot = rollLoot(entry, player.world)
        val baseAmount = if (loot.min == loot.max) loot.min else player.world.random(loot.min..loot.max)
        
        // Check if the loot item is coins - multiply by 100x, otherwise 3x
        val isCoins = loot.item.equals("item.coins_995", ignoreCase = true)
        val multiplier = if (isCoins) 100 else 3
        val amount = baseAmount * multiplier
        
        // Convert item to noted version
        val itemId = getRSCM(loot.item)
        val item = Item(itemId, amount)
        val notedItem = item.toNoted()
        
        val transaction = player.inventory.add(item = notedItem.id, amount = notedItem.amount)
        if (transaction.hasFailed()) {
            player.world.spawn(
                GroundItem(
                    item = notedItem.id,
                    amount = notedItem.amount,
                    tile = player.tile,
                    owner = player,
                ),
            )
        }
    }

    private fun onFailure(player: Player, npc: Npc, entry: PickpocketEntry, npcName: String) {
        player.message("You fail to pick the ${npcName}'s pocket.")
        npc.forceChat("Hands off!")

        if (entry.stun.ticks > 0) {
            npc.facePawn(player)
        } else {
            npc.resetFacePawn()
        }

        val damageRange = entry.stun.damage
        val damage =
            if (damageRange.max == damageRange.min) {
                damageRange.min
            } else {
                player.world.random(damageRange.min..damageRange.max)
            }

        if (entry.stun.ticks > 0) {
            player.stun(entry.stun.ticks)
        }
        if (damage > 0) {
            player.hit(damage)
        }
    }

    private fun rollLoot(entry: PickpocketEntry, world: World): PickpocketLoot {
        if (entry.loot.size == 1) {
            return entry.loot.first()
        }
        val total = entry.loot.sumOf { it.weight }
        val roll = world.randomDouble() * total
        var cumulative = 0.0
        entry.loot.forEach { loot ->
            cumulative += loot.weight
            if (roll < cumulative) {
                return loot
            }
        }
        return entry.loot.last()
    }

    private fun canReceiveLoot(player: Player, entry: PickpocketEntry): Boolean {
        if (!player.inventory.isFull) {
            return true
        }
        return entry.loot.any { loot ->
            val itemId = getRSCM(loot.item)
            val def = getItem(itemId)
            def.stackable && player.inventory.getItemCount(itemId) > 0
        }
    }
}
