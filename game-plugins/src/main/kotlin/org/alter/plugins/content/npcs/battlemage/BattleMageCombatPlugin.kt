package org.alter.plugins.content.npcs.battlemage

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MagicCombatFormula

class BattleMageCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Saradomin Mage
        onNpcCombat("npc.battle_mage") { npc.queue { npc.mageCombat(this, 1610) } }
        // Guthix Mage
        onNpcCombat("npc.battle_mage_1611") { npc.queue { npc.mageCombat(this, 1611) } }
        // Zamorak Mage
        onNpcCombat("npc.battle_mage_1612") { npc.queue { npc.mageCombat(this, 1612) } }
    }

    private suspend fun Npc.mageCombat(it: QueueTask, npcId: Int) {
        var target = getCombatTarget() ?: return

        while (canEngageCombat(target)) {
            facePawn(target)
            if (moveToAttackRange(it, target, distance = 8, projectile = true) && isAttackDelayReady()) {
                this.mageAttack(target, npcId)
                postAttackLogic(target)
            }
            it.wait(1)
            target = getCombatTarget() ?: break
        }

        resetFacePawn()
        removeCombatTarget()
    }

    private fun Npc.mageAttack(target: Pawn, npcId: Int) {
        val (anim, gfx) = when (npcId) {
            1610 -> Pair(811, 76) // Saradomin Strike
            1611 -> Pair(811, 77) // Claws of Guthix
            1612 -> Pair(811, 78) // Flames of Zamorak
            else -> Pair(711, -1)
        }

        prepareAttack(CombatClass.MAGIC, CombatStyle.MAGIC, AttackStyle.ACCURATE)
        animate(anim)
        
        // God spells usually have the GFX on the target
        if (gfx != -1) {
            target.graphic(gfx)
        }

        // Calculate accuracy using standard formula (which accounts for magic level, defence, etc.)
        // We do NOT use MagicCombatFormula.getMaxHit because that checks for protection prayers and sets damage to 0.
        val accuracy = MagicCombatFormula.getAccuracy(this, target)
        
        if (accuracy >= this.world.randomDouble()) {
            // Max hit for God Spells is 20 (30 with Charge, but let's stick to 20 for NPCs)
            val maxHit = 20
            val damage = this.world.random(maxHit)
            target.hit(damage, type = HitType.HIT, delay = 2)
        } else {
            target.hit(0, type = HitType.BLOCK, delay = 2)
        }
    }
}
