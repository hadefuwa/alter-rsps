package org.alter.game.model.combat

import org.alter.game.model.EntityType
import org.alter.game.model.entity.Pawn
import java.util.*

/**
 * Represents a map of hits from different [Pawn]s and their information.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class DamageMap {
    private val map = WeakHashMap<Pawn, DamageStack>(0)

    operator fun get(pawn: Pawn): DamageStack? = map[pawn]

    fun add(
        pawn: Pawn,
        damage: Int,
    ) {
        val total = (map[pawn]?.totalDamage ?: 0) + damage
        map[pawn] = DamageStack(total, System.currentTimeMillis())
    }

    /**
     * Get all [DamageStack]s dealt by [Pawn]s whom meets the criteria
     * [Pawn.entityType] == [type].
     */
    fun getAll(
        type: EntityType,
        timeFrameMs: Long? = null,
    ): Collection<DamageStack> =
        map.filter {
            it.key.entityType == type && (timeFrameMs == null || System.currentTimeMillis() - it.value.lastHit < timeFrameMs)
        }.values

    /**
     * Clears all damage tracking from the map.
     * Used when an NPC respawns to reset damage tracking for the new instance.
     */
    fun clear() {
        map.clear()
    }

    /**
     * Get the total damage from a [pawn].
     *
     * @return
     * 0 if [pawn] has not dealt any damage.
     */
    fun getDamageFrom(pawn: Pawn): Int = map[pawn]?.totalDamage ?: 0

    /**
     * Gets the [Pawn] that has dealt the most damage in this map.
     */
    fun getMostDamage(): Pawn? = map.maxByOrNull { it.value.totalDamage }?.key

    /**
     * Gets the [Pawn] that has dealt the most damage as a percentage of [maxHp].
     * This is used to determine kill attribution based on damage percentage rather than total damage.
     * 
     * @param maxHp The maximum hitpoints of the target (used to calculate damage percentage)
     * @return The [Pawn] with the highest damage percentage, or null if the map is empty
     */
    fun getMostDamagePercentage(maxHp: Int): Pawn? {
        if (maxHp <= 0 || map.isEmpty()) {
            return null
        }
        return map.maxByOrNull { (it.value.totalDamage.toDouble() / maxHp) }?.key
    }

    /**
     * Gets the most damage dealt by a [Pawn] in our map whom meets the criteria
     * [Pawn.entityType] == [type].
     */
    fun getMostDamage(
        type: EntityType,
        timeFrameMs: Long? = null,
    ): Pawn? =
        map.filter {
            it.key.entityType == type && (timeFrameMs == null || System.currentTimeMillis() - it.value.lastHit < timeFrameMs)
        }.maxByOrNull { it.value.totalDamage }?.key

    data class DamageStack(val totalDamage: Int, val lastHit: Long)
}
