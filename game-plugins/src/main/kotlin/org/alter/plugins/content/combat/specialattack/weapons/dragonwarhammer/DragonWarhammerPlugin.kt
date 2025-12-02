package org.alter.plugins.content.combat.specialattack.weapons.dragonwarhammer

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.entity.AreaSound
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.specialattack.SpecialAttacks
import org.alter.plugins.content.combat.dealHit
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server
import org.alter.game.model.World

class DragonWarhammerPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        SpecialAttacks.register("item.dragon_warhammer", 50) {
            val target = this.target ?: return@register
            
            player.animate(id = 1378)
            player.graphic(id = 1292)
            world.spawn(AreaSound(tile = player.tile, id = 2537, radius = 10, volume = 1))

            val maxHit = MeleeCombatFormula.getMaxHit(player, target, specialAttackMultiplier = 1.5)
            val accuracy = MeleeCombatFormula.getAccuracy(player, target)
            val landHit = accuracy >= world.randomDouble()
            
            val hit = player.dealHit(target = target, maxHit = maxHit, landHit = landHit, delay = 1)
            
            if (hit.landed) {
                if (target is Player) {
                    val currentDef = target.getSkills().getCurrentLevel(Skills.DEFENCE)
                    val reduction = (currentDef * 0.30).toInt()
                    target.getSkills().alterCurrentLevel(Skills.DEFENCE, -reduction, 0)
                    target.message("You feel your defence being drained.")
                } else if (target is Npc) {
                    val currentDef = target.stats.getCurrentLevel(2) // 2 is Defence stat for NPC
                    val reduction = (currentDef * 0.30).toInt()
                    target.stats.alterCurrentLevel(2, -reduction, 0)
                }
            }
        }
    }
}
