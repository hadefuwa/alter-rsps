package org.alter.plugins.content.interfaces.kod

import org.alter.api.InterfaceDestination
import org.alter.api.ext.*
import org.alter.game.model.World
import org.alter.game.model.container.ContainerStackType
import org.alter.game.model.container.ItemContainer
import org.alter.game.model.entity.Player

/**
 * @author Tom <rspsmods@gmail.com>
 */
object KeptOnDeath {
    const val KOD_INTERFACE_ID = 4
    const val KOD_COMPONENT_ID = 5

    fun open(
        p: Player,
        world: World,
    ) {
        val dummyContainer = ItemContainer(capacity = 50, stackType = ContainerStackType.NO_STACK)

        dummyContainer.add(368, 9)
        dummyContainer.add(324, 41)

        // TODO: Calculate actual kept items and risk value
        // Currently using dummy container - needs proper implementation to:
        // 1. Calculate which items would be kept on death (3-4 items based on protect item prayer)
        // 2. Calculate total value of items that would be lost
        // 3. Display actual kept items in containers instead of dummy data
        
        p.sendItemContainer(interfaceId = -1, component = 230, key = 584, container = dummyContainer)
        p.sendItemContainer(interfaceId = -1, component = 90, key = 468, container = dummyContainer)
        p.sendItemContainer(interfaceId = -1, component = 209, key = 93, container = dummyContainer)
        p.setInterfaceUnderlay(color = -1, transparency = -1)
        p.openInterface(interfaceId = KOD_INTERFACE_ID, dest = InterfaceDestination.MAIN_SCREEN)
        
        // TODO: Uncomment and fix client script once kept items calculation is implemented
        // p.runClientScript(972, 0, 0, 0, 0, "", if (p.getVarp(599)==1) 4 else 3, keptContainer[0]?.id ?: -1, keptContainer[1]?.id ?: -1, keptContainer[2]?.id ?: -1, if (p.getVarbit(599) == 1) keptContainer[3]!!.id else -1)
        
        p.setInterfaceEvents(interfaceId = KOD_INTERFACE_ID, component = 12, range = 0..3, setting = 1)
        
        // TODO: Calculate actual risk value from player's inventory and equipment
        // Risk value = total value of items that would be lost on death (not kept)
        // Formula: Sum of (item.value * item.amount) for all items not in kept items list
        val riskValue = 0 // Placeholder - needs implementation
        val formattedRiskValue = String.format("%,d", riskValue)
        p.setComponentText(KOD_INTERFACE_ID, 18, "Guide risk value:<br><col=ffffff>$formattedRiskValue</col> gp")
    }
}
