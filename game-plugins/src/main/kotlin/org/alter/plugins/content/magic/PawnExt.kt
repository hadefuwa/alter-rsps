package org.alter.plugins.content.magic

import dev.openrune.cache.CacheManager.getAnim
import org.alter.api.ext.*
import org.alter.game.model.LockState
import org.alter.game.model.Tile
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.TaskPriority
import org.alter.plugins.content.combat.Combat

fun Player.canTeleport(type: TeleportType): Boolean {
    val currWildLvl = tile.getWildernessLevel()
    val wildLvlRestriction = type.wildLvlRestriction

    if (!lock.canTeleport()) {
        return false
    }

    if (currWildLvl > wildLvlRestriction) {
        message("A mysterious force blocks your teleport spell!")
        message("You can't use this teleport after level $wildLvlRestriction wilderness.")
        return false
    }

    return true
}

fun Pawn.prepareForTeleport() {
    resetInteractions()
    clearHits()
}

fun Pawn.teleport(
    endTile: Tile,
    type: TeleportType,
) {
    lock = LockState.FULL_WITH_DAMAGE_IMMUNITY

    queue(TaskPriority.STRONG) {
        prepareForTeleport()

        animate(type.animation)
        type.graphic?.let {
            graphic(it)
        }

        wait(type.teleportDelay)

        moveTo(endTile)

        type.endAnimation?.let {
            animate(it)
        }

        type.endGraphic?.let {
            graphic(it)
        }

        type.endAnimation?.let {
            val def = getAnim(it)
            wait(def.cycleLength)
        }

        animate(-1)
        unlock()
        
        // Clear autocast after teleporting (home teleport shouldn't trigger autocast)
        if (this is Player) {
            this.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, 0)
            this.attr.remove(Combat.CASTING_SPELL)
            this.attr.remove(Combat.DEFENSIVE_AUTOCAST_SELECTION)
        }
    }
}
