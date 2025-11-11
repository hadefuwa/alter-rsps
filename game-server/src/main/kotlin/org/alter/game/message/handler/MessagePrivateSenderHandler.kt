package org.alter.game.message.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import net.rsprot.protocol.game.incoming.messaging.MessagePrivate
import org.alter.game.message.MessageHandler
import org.alter.game.model.entity.Client

/**
 * Handler for private messaging between players.
 * 
 * NOTE: This handler appears to be functional but has a TODO comment.
 * The implementation seems complete - it receives a private message, finds the target player,
 * and sends it via the social system. The TODO may be outdated or refer to future enhancements.
 * 
 * Potential improvements:
 * - Add message filtering/validation
 * - Add rate limiting to prevent spam
 * - Add logging for moderation purposes
 * - Add support for message history
 * - Add support for offline messaging (store messages for offline players)
 */
class MessagePrivateSenderHandler : MessageHandler<MessagePrivate> {
    override fun consume(
        client: Client,
        message: MessagePrivate,
    ) {
        logger.info { "Sender: ${client.username} - Target: ${message.name} - Message: ${message.message}" }
        val target = client.world.getPlayerForName(message.name)
        if (target != null) {
            logger.info { "Attempting to send packet to target" }
            client.social.sendPrivateMessage(client, target, message.message)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
