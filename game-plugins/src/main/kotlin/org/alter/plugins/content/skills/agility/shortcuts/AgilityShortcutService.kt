package org.alter.plugins.content.skills.agility.shortcuts

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gg.rsmod.util.ServerProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.alter.api.ext.appendToString
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.service.Service
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Service for loading agility shortcut configurations from JSON.
 * @author Auto-generated
 */
class AgilityShortcutService : Service {
    val shortcuts = ObjectArrayList<AgilityShortcut>()

    override fun init(
        server: Server,
        world: World,
        serviceProperties: ServerProperties,
    ) {
        val file = Paths.get(serviceProperties.get("agility-shortcuts") ?: "data/cfg/agility/shortcuts.json")
        if (Files.exists(file)) {
            Files.newBufferedReader(file).use { reader ->
                val shortcuts = Gson().fromJson<ObjectArrayList<AgilityShortcut>>(reader, object : TypeToken<ObjectArrayList<AgilityShortcut>>() {}.type)
                this.shortcuts.addAll(shortcuts)
            }
            logger.info { "Loaded ${this.shortcuts.size.appendToString("agility shortcut")}." }
        } else {
            logger.warn { "Agility shortcut configuration file not found: $file" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

