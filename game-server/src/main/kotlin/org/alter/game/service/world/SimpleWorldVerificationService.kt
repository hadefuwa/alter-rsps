package org.alter.game.service.world

import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import org.alter.game.model.PlayerUID
import org.alter.game.model.World

/**
 * @author Tom <rspsmods@gmail.com>
 */
class SimpleWorldVerificationService : WorldVerificationService {
    override fun interceptLoginResult(
        world: World,
        uid: PlayerUID,
        displayName: String,
        loginName: String,
    ): LoginResponse? {
        // Check for existing player with same name
        val existingPlayer = world.getPlayerForName(displayName)
        if (existingPlayer != null) {
            // If existing player is not fully initialized (stuck in failed login), remove them
            if (!existingPlayer.initiated) {
                world.unregister(existingPlayer)
            } else {
                // Player is already logged in and active
                return LoginResponse.Duplicate
            }
        }
        
        return when {
            world.rebootTimer != -1 && world.rebootTimer < World.REJECT_LOGIN_REBOOT_THRESHOLD -> LoginResponse.UpdateInProgress
            world.players.count() >= world.players.capacity -> LoginResponse.IPLimit
            else -> null
        }
    }
}
