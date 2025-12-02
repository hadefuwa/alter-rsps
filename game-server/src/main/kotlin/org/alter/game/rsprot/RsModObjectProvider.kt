package org.alter.game.rsprot

import net.rsprot.protocol.common.game.outgoing.inv.InventoryObject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvFull
import org.alter.game.model.item.Item

class RsModObjectProvider(val items: Array<Item?>) : UpdateInvFull.ObjectProvider {
    override fun provide(slot: Int): Long {
        val item = items[slot] ?: return InventoryObject.NULL
        
        // Safety check: validate and clamp item amount to prevent corruption/overflow
        // Note: -2 is used for bank placeholders internally, but the protocol doesn't accept negative amounts
        // So we treat -2 as an empty slot (NULL) when sending to client
        val amount = when {
            item.amount == -2 -> {
                // Bank placeholder - return NULL to indicate empty slot to client
                return InventoryObject.NULL
            }
            item.amount < 0 -> {
                println("WARNING: Item ${item.id} at slot $slot has negative amount: ${item.amount}. Setting to 0.")
                0
            }
            item.amount > Int.MAX_VALUE -> {
                println("WARNING: Item ${item.id} at slot $slot has amount exceeding Int.MAX_VALUE: ${item.amount}. Clamping to Int.MAX_VALUE.")
                Int.MAX_VALUE
            }
            else -> item.amount
        }
        
        // Additional safety: ensure amount is within valid range for InventoryObject (must be >= 0)
        // The protocol doesn't accept negative amounts
        val clampedAmount = amount.coerceIn(0, Int.MAX_VALUE)
        
        return InventoryObject(slot, item.id, clampedAmount)
    }
}
