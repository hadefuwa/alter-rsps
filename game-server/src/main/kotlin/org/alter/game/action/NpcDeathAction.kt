package org.alter.game.action

import dev.openrune.cache.CacheManager.getAnim

import org.alter.game.info.NpcInfo
import org.alter.game.model.LockState
import org.alter.game.model.attr.KILLER_ATTR
import org.alter.game.model.entity.AreaSound
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.move.stopMovement
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.TaskPriority
import org.alter.game.model.weightedTableBuilder.roll
import org.alter.game.model.attr.COMBAT_TARGET_FOCUS_ATTR
import org.alter.game.plugin.Plugin
import org.alter.game.service.log.LoggerService
import java.lang.ref.WeakReference

/**
 * This class is responsible for handling npc death events.
 *
 * @author Tom <rspsmods@gmail.com>
 */
object NpcDeathAction {
    var deathPlugin: Plugin.() -> Unit = {
        val npc = ctx as Npc
        if (!npc.world.plugins.executeNpcFullDeath(npc)) {
            npc.interruptQueues()
            npc.stopMovement()
            npc.lock()
            // Reset combat state immediately to stop ghost combat
            npc.resetInteractions()
            // Reset combat for all pawns targeting this NPC (players and other NPCs)
            // This ensures attackers stop targeting the dead NPC and can attack again
            resetCombatForTarget(npc)
            npc.queue(TaskPriority.STRONG) {
                death(npc)
            }
        }
    }

    suspend fun QueueTask.death(npc: Npc) {
        val world = npc.world
        val deathAnimation = npc.combatDef.deathAnimation
        val deathSound = npc.combatDef.defaultDeathSound
        val respawnDelay = npc.combatDef.respawnDelay
        var killer: Pawn? = null
        // Use damage percentage to determine the killer (person who did the most damage %)
        val maxHp = npc.getMaxHp()
        npc.damageMap.getMostDamagePercentage(maxHp)?.let {
            if (it is Player) {
                killer = it
                world.getService(LoggerService::class.java, searchSubclasses = true)?.logNpcKill(it, npc)
            }
            npc.attr[KILLER_ATTR] = WeakReference(it)
        }
        NpcInfo(npc).setAllOpsInvisible()
        world.plugins.executeNpcPreDeath(npc)
        npc.resetFacePawn()
        if (npc.combatDef.defaultDeathSoundArea) {
            world.spawn(AreaSound(npc.tile, deathSound, npc.combatDef.defaultDeathSoundRadius, npc.combatDef.defaultDeathSoundVolume))
        } else {
            (killer as? Player)?.playSound(deathSound, npc.combatDef.defaultDeathSoundVolume)
        }

        /**
         * @TODO add interruption for this block if we would want to execute a plugin during it's death animation
         */
        deathAnimation.forEach { anim ->
            val def = getAnim(anim)
            npc.animate(def.id, def.cycleLength)
            wait(def.cycleLength)
        }
        world.plugins.executeNpcDeath(npc)
        world.plugins.anyNpcDeath.forEach {
            npc.executePlugin(it)
        }
        // Check respawnDelay from combat definition instead of npc.respawns flag
        // This ensures NPCs respawn correctly even if the flag was incorrectly set
        if (respawnDelay > 0) {
            NpcInfo(npc).setInaccessible(true)
            npc.reset()
            wait(respawnDelay)
            NpcInfo(npc).setAllOpsVisible()
            NpcInfo(npc).setInaccessible(false)
            npc.unlock()
            world.plugins.executeNpcSpawn(npc)
        } else {
            world.remove(npc)
        }
    }
    private fun Npc.reset() {
        lock()
        stopMovement()
        moveTo(spawnTile)
        attr.clear()
        timers.clear()
        damageMap.clear() // Reset damage tracking for new spawn
        world.setNpcDefaults(this)
    }

    /**
     * Resets combat for all pawns that have [target] as their combat target.
     * This is used when an NPC dies to ensure all attackers stop targeting them.
     */
    private fun resetCombatForTarget(target: Npc) {
        val world = target.world
        
        // Reset combat for all players targeting this NPC
        world.players.forEach { player ->
            val combatTarget = player.attr[COMBAT_TARGET_FOCUS_ATTR]?.get()
            if (combatTarget == target) {
                player.attr.remove(COMBAT_TARGET_FOCUS_ATTR)
                player.resetFacePawn()
                player.interruptQueues()
            }
        }
        
        // Reset combat for all NPCs targeting this NPC
        world.npcs.forEach { npc ->
            val combatTarget = npc.attr[COMBAT_TARGET_FOCUS_ATTR]?.get()
            if (combatTarget == target) {
                npc.attr.remove(COMBAT_TARGET_FOCUS_ATTR)
                npc.resetFacePawn()
                npc.interruptQueues()
            }
        }
    }
}
