package org.alter.game.util

import org.mindrot.jbcrypt.BCrypt

object PasswordHashGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val password = if (args.isNotEmpty()) args[0] else "ummah123"
        val hash = BCrypt.hashpw(password, BCrypt.gensalt(16))
        println(hash)
    }
}






