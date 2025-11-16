package org.alter.plugins.content.skills.woodcutting

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gg.rsmod.util.ServerProperties
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.alter.api.ext.appendToString
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.service.Service
import org.alter.rscm.RSCM.getRSCM
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Loads the woodcutting configuration so that entries can be retrieved at runtime.
 */
class WoodcuttingService : Service {

    private val gson = Gson()

    val entries: ObjectArrayList<WoodcuttingEntry> = ObjectArrayList()

    private val entriesByObject: Int2ObjectOpenHashMap<WoodcuttingEntry> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("woodcutting.trees") ?: "data/cfg/woodcutting/trees.json")

        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<WoodcuttingEntry>>() {}.type
            val loaded: List<WoodcuttingEntry> = gson.fromJson(reader, listType)
            entries.addAll(loaded)
        }

        entries.forEach { entry ->
            entry.objectIds = entry.objects.map { getRSCM(it) }.toIntArray()
            entry.stumpObjectId = getRSCM(entry.stumpObject)
            entry.logsId = getRSCM(entry.logs)
            entry.objectIds.forEach { id ->
                entriesByObject.put(id, entry)
            }
        }

        Server.logger.info { "Loaded ${entries.size.appendToString("woodcutting tree definition")}." }
    }

    fun lookup(objectId: Int): WoodcuttingEntry? = entriesByObject[objectId]
}

