package org.alter.plugins.content.bosses.moonboss

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.MeleeCombatFormula

class MoonBossCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        onNpcCombat("npc.blood_moon") {
            npc.queue {
                moonBossCombat(npc)
            }
        }

        onNpcCombat("npc.blue_moon") {
            npc.queue {
                moonBossCombat(npc)
            }
        }

        onNpcCombat("npc.eclipse_moon") {
            npc.queue {
                moonBossCombat(npc)
            }
        }
    }
}

private suspend fun QueueTask.moonBossCombat(npc: Npc) {
    var target = npc.getCombatTarget() ?: return

    while (npc.canEngageCombat(target)) {
        npc.facePawn(target)
        if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && npc.isAttackDelayReady()) {
            npc.moonBossMeleeAttack(target)
            npc.postAttackLogic(target)
        }
        wait(1)
        target = npc.getCombatTarget() ?: break
    }

    npc.resetFacePawn()
    npc.removeCombatTarget()
}

private fun Npc.moonBossMeleeAttack(target: Pawn) {
    prepareAttack(CombatClass.MELEE, CombatStyle.CRUSH, AttackStyle.AGGRESSIVE)
    animate(combatDef.attackAnimation)
    if (MeleeCombatFormula.getAccuracy(this, target) >= world.randomDouble()) {
        val maxHit = MeleeCombatFormula.getMaxHit(this, target)
        target.hit(world.random(maxHit), type = HitType.HIT, delay = 1)
    } else {
        target.hit(damage = 0, type = HitType.BLOCK, delay = 1)
    }
}
