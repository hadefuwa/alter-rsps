package org.alter.plugins.content.skills.fishing

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
 * Loads the fishing configuration so that entries can be retrieved at runtime.
 */
class FishingService : Service {

    private val gson = Gson()

    val entries: ObjectArrayList<FishingEntry> = ObjectArrayList()

    private val entriesByObject: Int2ObjectOpenHashMap<ObjectArrayList<FishingEntry>> = Int2ObjectOpenHashMap()

    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        val file = Paths.get(serviceProperties.get("fishing.spots") ?: "data/cfg/fishing/spots.json")

        Files.newBufferedReader(file).use { reader ->
            val listType = object : TypeToken<List<FishingEntry>>() {}.type
            val loaded: List<FishingEntry> = gson.fromJson(reader, listType)
            entries.addAll(loaded)
        }

        entries.forEach { entry ->
            entry.objectIds = entry.objects.map { getRSCM(it) }.toIntArray()
            entry.toolId = getRSCM(entry.tool)
            entry.baitId = entry.bait?.let { getRSCM(it) }
            entry.fish.forEach { fish ->
                fish.itemId = getRSCM(fish.item)
            }
            entry.objectIds.forEach { id ->
                val list = entriesByObject.getOrDefault(id, ObjectArrayList())
                list.add(entry)
                entriesByObject.put(id, list)
            }
        }

        Server.logger.info { "Loaded ${entries.size.appendToString("fishing spot definition")}." }
    }

    fun lookup(objectId: Int): ObjectArrayList<FishingEntry>? = entriesByObject[objectId]
}

