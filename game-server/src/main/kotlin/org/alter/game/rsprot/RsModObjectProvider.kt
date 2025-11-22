package org.alter.game.rsprot

import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvFull
import org.alter.game.model.item.Item

class RsModObjectProvider(val items: Array<Item?>) : UpdateInvFull.ObjectProvider {
    override fun provide(slot: Int): Long {
        val item = items[slot] ?: return InventoryObject.NULL
        // Safety check: prevent negative item amounts (corrupted data)
        val amount = if (item.amount < 0) {
            println("WARNING: Item ${item.id} at slot $slot has negative amount: ${item.amount}. Setting to 0.")
            0
        } else {
            item.amount
        }
        return InventoryObject(slot, item.id, amount)
    }
}
