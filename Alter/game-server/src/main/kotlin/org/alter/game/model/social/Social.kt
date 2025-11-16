package org.alter.game.model.social

import io.github.oshai.kotlinlogging.KotlinLogging
import net.rsprot.protocol.game.outgoing.social.FriendListLoaded
import org.alter.game.model.entity.Player
import java.nio.file.Files
import java.nio.file.Paths

class Social {
    private val friends = mutableListOf<String>()
    private val ignores = mutableListOf<String>()

    /**
     * @TODO
     * Add support for old display name current isn't
     * Need to create actl tutorial island for it -> So that player starts without a name then he can choose w.e he wants
     * And it does exist: displayName shit actl we can make it into String Array
     * [0] = Current one, And move [0] => [1] When user changes name, Just need verification on how many users does Gagex store.
     */
    fun pushFriends(player: Player) {
        // Remove temporary fix entry if it exists
        friends.remove("TEMP_FIX_DONT_REMOVE")
        
        val world = player.world

        // Always send FriendListLoaded first to initialize the list
        player.write(FriendListLoaded)
        
        // TODO: Implement friend list updates once protocol messages are available
        // The commented code below shows the intended implementation:
        // friends.forEach {
        //     val user = world.getPlayerForName(it)
        //     if (user != null && !user.social.ignores.contains(player.username)) {
        //         // Friend is online - send online status
        //         // player.write(UpdateFriendListMessage(0, user.username, "", 304, 0, 0))
        //     } else {
        //         // Friend is offline - send offline status
        //         // player.write(UpdateFriendListMessage(0, it, "", 0, 0, 0))
        //     }
        // }
        // NOTE: UpdateFriendListMessage protocol message needs to be implemented/imported
    }

    /**
     * TODO Add support for old display name if current isn't previous/original one
     */
    fun pushIgnores(player: Player) {
        // TODO: Implement ignore list updates once protocol messages are available
        // The commented code below shows the intended implementation:
        // ignores.forEach {
        //     player.write(UpdateIgnoreListMessage(0, it, ""))
        // }
        // NOTE: UpdateIgnoreListMessage protocol message needs to be implemented/imported
    }

    fun addFriend(
        player: Player,
        name: String,
    ) {
        if (friends.contains(name)) {
            return
        }
        val path = Paths.get("data/saves/")
        val save = path.resolve(name)
        if (!Files.exists(save)) {
            player.writeMessage("Unable to add player; user with this username doesn't exist.")
            return
        }
        friends.add(name)
        pushFriends(player)
        updateStatus(player)
    }

    fun addIgnore(
        player: Player,
        name: String,
    ) {
        if (ignores.contains(name)) {
            return
        }
        val path = Paths.get("data/saves/")
        val save = path.resolve(name)
        if (!Files.exists(save)) {
            player.writeMessage("Unable to ignore player; user with this username doesn't exist.")
            return
        }
        ignores.add(name)
        pushIgnores(player)
        updateStatus(player)
    }

    fun deleteIgnore(
        player: Player,
        name: String,
    ) {
        ignores.remove(name)
        pushIgnores(player)
        updateStatus(player)
    }

    fun deleteFriend(
        player: Player,
        name: String,
    ) {
        friends.remove(name)
        pushFriends(player)
        updateStatus(player)
    }

    // TODO Add support for having private off/friends/etc...
    fun updateStatus(player: Player) {
        player.world.players.forEach {
            if (it == player) {
                return@forEach
            }
            if (it.social.ignores.contains(player.username)) {
                return@forEach
            }
            if (it.social.friends.contains(player.username)) {
                it.social.pushFriends(it)
            }
        }
    }

    fun sendPrivateMessage(
        player: Player,
        target: Player,
        unpacked: String,
    ) {
        logger.info { "${player.username} is attempting to message: ${target.username} with message: $unpacked" }
        // TODO: Implement private messaging once protocol messages are available
        // The commented code below shows the intended implementation:
        // target.write(MessagePrivate(
        //     sender = player.username,
        //     worldId = 1,
        //     worldMessageCounter = 0,
        //     chatCrownType = player.privilege.icon,
        //     message = unpacked,
        // ))
        // player.write(MessagePrivate(
        //     sender = target.username,
        //     worldId = 1,
        //     worldMessageCounter = 0,
        //     chatCrownType = target.privilege.icon,
        //     message = unpacked,
        // ))
        // NOTE: MessagePrivate and MessagePrivateReceiverMessage protocol messages need to be implemented/imported
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
