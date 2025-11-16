package org.alter.plugins.content.objects.trapdoor

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
 * Service for loading trapdoor configurations from JSON.
 * @author Auto-generated
 */
class TrapdoorService : Service {
    val trapdoors = ObjectArrayList<Trapdoor>()

    override fun init(
        server: Server,
        world: World,
        serviceProperties: ServerProperties,
    ) {
        val file = Paths.get(serviceProperties.get("trapdoors") ?: "data/cfg/trapdoors/trapdoors.json")
        if (Files.exists(file)) {
            Files.newBufferedReader(file).use { reader ->
                val trapdoors = Gson().fromJson<ObjectArrayList<Trapdoor>>(reader, object : TypeToken<ObjectArrayList<Trapdoor>>() {}.type)
                this.trapdoors.addAll(trapdoors)
            }
            logger.info { "Loaded ${this.trapdoors.size.appendToString("trapdoor")}." }
        } else {
            logger.warn { "Trapdoor configuration file not found: $file" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

