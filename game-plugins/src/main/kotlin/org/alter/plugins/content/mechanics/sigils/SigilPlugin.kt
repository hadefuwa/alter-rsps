package org.alter.plugins.content.mechanics.sigils

import org.alter.game.model.entity.Player
import org.alter.api.ext.hasEquipped

object SigilPlugin {
    const val SIGIL_OF_ADROIT = 29664
    const val SIGIL_OF_RAMPART = 29655
    const val SIGIL_OF_TITANIUM = 28523

    fun getAccuracyMultiplier(player: Player): Double {
        var multiplier = 1.0

        if (player.hasEquipped(intArrayOf(SIGIL_OF_ADROIT))) {
            val maxHp = player.getMaxHp()
            val currentHp = player.getCurrentHp()
            val missingHp = maxHp - currentHp
            
            // Accuracy increased by 1% for each hitpoint missing
            if (missingHp > 0) {
                multiplier += (missingHp * 0.01)
            }
        }

        return multiplier
    }

    fun getDefenceBonusAdd(player: Player): Int {
        var bonus = 0
        if (player.hasEquipped(intArrayOf(SIGIL_OF_RAMPART))) {
            bonus += 100
        }
        return bonus
    }

    fun getDamageReductionMultiplier(player: Player, attacker: org.alter.game.model.entity.Pawn): Double {
        var multiplier = 1.0
        
        // Sigil of Titanium: Reduces damage taken from all monsters by 60%
        if (player.hasEquipped(intArrayOf(SIGIL_OF_TITANIUM)) && attacker is org.alter.game.model.entity.Npc) {
            multiplier *= 0.4
        }
        
        return multiplier
    }
}
