package org.alter.game.message.handler

import net.rsprot.protocol.game.incoming.events.EventMouseClick
import org.alter.game.message.MessageHandler
import org.alter.game.model.entity.Client

/**
 * Handler for mouse click events from the client.
 * 
 * NOTE: This is currently a placeholder. Mouse click events are typically used for:
 * - Custom interface interactions
 * - Mini-map interactions
 * - Special game mechanics that require precise mouse coordinates
 * 
 * If mouse click functionality is not needed for your server, this handler can remain empty.
 * Otherwise, implement the desired mouse click behavior here.
 * 
 * @author Tom <rspsmods@gmail.com>
 */
class EventMouseClickHandler : MessageHandler<EventMouseClick> {
    override fun consume(
        client: Client,
        message: EventMouseClick,
    ) {
        // TODO: Implement mouse event click if needed
        // Mouse click events include coordinates and button information
        // Example: message.x, message.y, message.button, etc.
        // Uncomment and implement when mouse click functionality is required
    }
}
