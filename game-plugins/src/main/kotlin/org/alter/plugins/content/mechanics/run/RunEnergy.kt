package org.alter.plugins.content.mechanics.run

import org.alter.api.EquipmentType
import org.alter.api.Skills
import org.alter.api.cfg.Varp
import org.alter.api.ext.*
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.bits.INFINITE_VARS_STORAGE
import org.alter.game.model.bits.InfiniteVarsType
import org.alter.game.model.entity.Player
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.timer.TimerKey
import kotlin.math.max
import kotlin.math.min

/**
 * @author Tom <rspsmods@gmail.com>
 */
object RunEnergy {
    val RUN_DRAIN = TimerKey()

    /**
     * Reduces run energy depletion by 70%
     */
    val STAMINA_BOOST = TimerKey("stamina_boost", tickOffline = false)

    /**
     * Attribute key for tracking Agility XP grant intervals (every 4 ticks)
     * Using attribute instead of timer to avoid ConcurrentModificationException
     */
    val AGILITY_XP_COUNTER = AttributeKey<Int>()

    const val RUN_ENABLED_VARP = Varp.RUN_MODE_VARP

    fun toggle(p: Player) {
        if (p.runEnergy >= 100.0) {
            p.toggleVarp(RUN_ENABLED_VARP)
        } else {
            p.setVarp(RUN_ENABLED_VARP, 0)
            p.message("You don't have enough run energy left.")
        }
    }

    fun drain(p: Player) {
        if (p.isRunning() && p.hasMoveDestination()) {
            // Grant Agility XP based on Agility level while running (every 4 ticks)
            val agilityLevel = p.getSkills().getCurrentLevel(Skills.AGILITY)
            if (agilityLevel > 0) {
                // Initialize counter if it doesn't exist
                val currentCounter = p.attr[AGILITY_XP_COUNTER] ?: 4
                
                // Decrement counter
                val newCounter = currentCounter - 1
                p.attr[AGILITY_XP_COUNTER] = newCounter
                
                // Grant XP only when counter reaches 0, then reset to 4
                if (newCounter <= 0) {
                    val baseXp = agilityLevel * 0.05 // XP per occurrence = Agility level * 0.05
                    val gracefulPieces = countGracefulPieces(p)
                    val bonusMultiplier = 1.0 + (gracefulPieces * 0.50) // 50% bonus per piece
                    val xpGained = baseXp * bonusMultiplier
                    p.addXp(Skills.AGILITY, xpGained)
                    p.attr[AGILITY_XP_COUNTER] = 4 // Reset counter to 4 ticks
                }
            }
            
            if (!p.hasStorageBit(INFINITE_VARS_STORAGE, InfiniteVarsType.RUN)) {
                val weight = max(0.0, p.weight)
                var decrement = (min(weight, 6400.0) / 10000.0) + 64.0
                if (p.timers.has(STAMINA_BOOST)) {
                    decrement *= 0.3
                }
                p.runEnergy = max(0.0, (p.runEnergy - decrement))
                if (p.runEnergy <= 0) {
                    p.varps.setState(RUN_ENABLED_VARP, 0)
                }
                p.sendRunEnergy(p.runEnergy.toInt())
            }
        } else if (p.runEnergy < 10000.0 && p.lock.canRestoreRunEnergy()) {
            var recovery = (800.0 + (p.getSkills().getCurrentLevel(Skills.AGILITY) * 100 / 600.0)) / 10000.0
            if (isWearingFullGrace(p)) {
                recovery *= 130
            }
            p.runEnergy = min(10000.0, (p.runEnergy + 500))
            p.sendRunEnergy(p.runEnergy.toInt())
        }
    }

    private fun isWearingFullGrace(p: Player): Boolean =
        (p.equipment[EquipmentType.HEAD.id]?.id ?: -1) in GRACEFUL_HOODS &&
            (p.equipment[EquipmentType.CAPE.id]?.id ?: -1) in GRACEFUL_CAPE &&
            (p.equipment[EquipmentType.CHEST.id]?.id ?: -1) in GRACEFUL_TOP &&
            (p.equipment[EquipmentType.LEGS.id]?.id ?: -1) in GRACEFUL_LEGS &&
            (p.equipment[EquipmentType.GLOVES.id]?.id ?: -1) in GRACEFUL_GLOVES &&
            (p.equipment[EquipmentType.BOOTS.id]?.id ?: -1) in GRACEFUL_BOOTS

    /**
     * Counts how many graceful pieces are currently equipped by the player.
     * Each piece gives 50% bonus agility XP when running.
     */
    private fun countGracefulPieces(p: Player): Int {
        var count = 0
        if ((p.equipment[EquipmentType.HEAD.id]?.id ?: -1) in GRACEFUL_HOODS) count++
        if ((p.equipment[EquipmentType.CAPE.id]?.id ?: -1) in GRACEFUL_CAPE) count++
        if ((p.equipment[EquipmentType.CHEST.id]?.id ?: -1) in GRACEFUL_TOP) count++
        if ((p.equipment[EquipmentType.LEGS.id]?.id ?: -1) in GRACEFUL_LEGS) count++
        if ((p.equipment[EquipmentType.GLOVES.id]?.id ?: -1) in GRACEFUL_GLOVES) count++
        if ((p.equipment[EquipmentType.BOOTS.id]?.id ?: -1) in GRACEFUL_BOOTS) count++
        return count
    }


    /**
     * @TODO Magic Numbers
     */
    private val GRACEFUL_HOODS = intArrayOf(11850, 13579, 13591, 13603, 13615, 13627, 13667, 21061, 24743, 24745, 25069, 25071, 27444, 27446, 30045, 30047)

    private val GRACEFUL_CAPE = intArrayOf(11852, 13581, 13593, 13605, 13617, 13629, 13669, 21064, 24746, 24748, 25072, 25074, 27447, 27449, 30048, 30050)

    private val GRACEFUL_TOP = intArrayOf(11854, 13583, 13595, 13607, 13619, 13631, 13671, 21067, 24749, 24751, 25075, 25077, 27450, 27452, 30051, 30053)

    private val GRACEFUL_LEGS = intArrayOf(11856, 13585, 13597, 13609, 13621, 13633, 13673, 21070, 24752, 24754, 25078, 25080, 27453, 27455, 30054, 30056)

    private val GRACEFUL_GLOVES = intArrayOf(11858, 13587, 13599, 13611, 13623, 13635, 13675, 21073, 24755, 24757, 25081, 25083, 27456, 27458, 30057, 30059)

    private val GRACEFUL_BOOTS = intArrayOf(11860, 13589, 13601, 13613, 13625, 13637, 13677, 21076, 24758, 24760, 25084, 25086, 27459, 27461, 30060, 30062)
}
