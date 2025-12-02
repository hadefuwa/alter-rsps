package org.alter.plugins.content.npcs.kalphitequeen

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*

/**
 * Kalphite Queen Single-Phase System Plugin
 * 
 * Modified to be a single phase fight with 510 HP.
 * Swaps "prayers" (defensive weaknesses) at 50% HP (255 HP).
 */
class KalphiteQueenPhasePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Check if NPC is in "Form 2" state (HP <= 50%)
         * This is called from combat formulas to determine damage reduction.
         */
        @JvmStatic
        fun isForm2(npc: Npc): Boolean {
            // Check if this is a Kalphite Queen
            val isKQ = npc.id == 963 || npc.id == 964 || npc.def.name.lowercase().contains("kalphite queen")
            if (!isKQ) return false
            
            // "Form 2" logic applies when HP is at or below 50% (255 HP)
            // Max HP is 510
            return npc.getCurrentHp() <= 255
        }
    }

    init {
        // Set up Kalphite Queen (ID 963)
        onNpcSpawn("npc.kalphite_queen_963") {
            // Set HP to 510 (Double of original 255)
            // We check if it's not 510 to avoid resetting it if it's already correct (though onSpawn happens once per spawn)
            // But we definitely need to override the default 255 from config
            
            // Create a mutable list for bonuses
            val bonuses = MutableList(14) { 0 }
            bonuses[BonusSlot.ATTACK_STAB.id] = 120
            bonuses[BonusSlot.ATTACK_SLASH.id] = 120
            bonuses[BonusSlot.ATTACK_CRUSH.id] = 120
            bonuses[BonusSlot.ATTACK_MAGIC.id] = 120
            bonuses[BonusSlot.ATTACK_RANGED.id] = 120
            bonuses[BonusSlot.DEFENCE_STAB.id] = 120
            bonuses[BonusSlot.DEFENCE_SLASH.id] = 120
            bonuses[BonusSlot.DEFENCE_CRUSH.id] = 120
            bonuses[BonusSlot.DEFENCE_MAGIC.id] = 100
            bonuses[BonusSlot.DEFENCE_RANGED.id] = 120

            val newDef = npc.combatDef.copy(
                hitpoints = 510,
                attackSpeed = 4,
                respawnDelay = 50,
                attack = 300,
                strength = 300,
                defence = 300,
                magic = 300,
                ranged = 300,
                bonuses = bonuses
            )
            npc.combatDef = newDef
            
            npc.stats.setMaxLevel(NpcSkills.ATTACK, 300)
            npc.stats.setCurrentLevel(NpcSkills.ATTACK, 300)
            npc.stats.setMaxLevel(NpcSkills.STRENGTH, 300)
            npc.stats.setCurrentLevel(NpcSkills.STRENGTH, 300)
            npc.stats.setMaxLevel(NpcSkills.DEFENCE, 300)
            npc.stats.setCurrentLevel(NpcSkills.DEFENCE, 300)
            npc.stats.setMaxLevel(NpcSkills.MAGIC, 300)
            npc.stats.setCurrentLevel(NpcSkills.MAGIC, 300)
            npc.stats.setMaxLevel(NpcSkills.RANGED, 300)
            npc.stats.setCurrentLevel(NpcSkills.RANGED, 300)
            
            npc.setCurrentHp(510)
        }
    }
}
