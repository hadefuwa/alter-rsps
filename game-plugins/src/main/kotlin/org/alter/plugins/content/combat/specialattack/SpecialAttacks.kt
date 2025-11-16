package org.alter.plugins.content.combat.specialattack

import org.alter.api.EquipmentType
import org.alter.api.ext.getEquipment
import org.alter.game.model.World
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.interfaces.attack.AttackTab

/**
 * @author Tom <rspsmods@gmail.com>
 */
object SpecialAttacks {

    fun register(
        item: String,
        energy: Int,
        executeInstantly: Boolean = false,
        attack: CombatContext.() -> Unit,
    ) {
        register(
            getRSCM(item),
            energy,
            executeInstantly,
            attack
        )
    }

    fun register(
        item: Int,
        energy: Int,
        executeInstantly: Boolean = false,
        attack: CombatContext.() -> Unit,
    ) {
        attacks[item] = SpecialAttack(energy, executeInstantly, attack)
    }

    fun executeOnEnable(item: Int): Boolean {
        if (attacks.containsKey(item)) {
            return attacks[item]!!.executeOnSpecBar
        }
        return false
    }

    fun execute(
        player: Player,
        target: Pawn?,
        world: World,
    ): Boolean {
        val weaponItem = player.getEquipment(EquipmentType.WEAPON) ?: return false
        val special = attacks[weaponItem.id] ?: return false

        if (AttackTab.getEnergy(player) < special.energyRequired) {
            return false
        }

        // Consume energy before executing the attack
        AttackTab.setEnergy(player, AttackTab.getEnergy(player) - special.energyRequired)

        val combatContext = CombatContext(world, player)
        combatContext.target = target
        
        // Execute the special attack
        // If it requires a target and target is null, it will return early
        try {
            special.attack(combatContext)
        } catch (e: Exception) {
            // If the special attack throws an exception, log it for debugging
            e.printStackTrace()
        }

        return true
    }

    val attacks = mutableMapOf<Int, SpecialAttack>()
}
