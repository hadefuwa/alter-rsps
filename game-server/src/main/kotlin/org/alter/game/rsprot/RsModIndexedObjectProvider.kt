package org.alter.game.rsprot

import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial
import org.alter.game.model.item.Item

class RsModIndexedObjectProvider(indices: Iterator<Int>, val items: Array<Item?>) : UpdateInvPartial.IndexedObjectProvider(indices) {
    override fun provide(slot: Int): Long {
        val item = items[slot] ?: return InventoryObject(slot, -1, -1)
        
        // Safety check: validate and clamp item amount to prevent corruption/overflow
        // Note: -2 is used for bank placeholders internally, but the protocol doesn't accept negative amounts
        // So we treat -2 as an empty slot when sending to client
        val amount = when {
            item.amount == -2 -> {
                // Bank placeholder - return empty slot to client
                return InventoryObject(slot, -1, -1)
            }
            item.amount < 0 -> 0  // Negative amounts are invalid (except -2)
            item.amount > Int.MAX_VALUE -> Int.MAX_VALUE  // Clamp to max
            else -> item.amount
        }
        
        // Additional safety: ensure amount is within valid range (must be >= 0)
        // The protocol doesn't accept negative amounts
        val clampedAmount = amount.coerceIn(0, Int.MAX_VALUE)
        
        return InventoryObject(slot, item.id, clampedAmount)
    }
}
