package org.alter.game.service.cache

import dev.openrune.cache.tools.Builder
import dev.openrune.cache.tools.tasks.TaskType
import java.io.File

object CacheDownloader {
    @JvmStatic
    fun main(args: Array<String>) {
        val cacheDir = if (args.isNotEmpty()) {
            File(args[0])
        } else {
            File("data/cache")
        }
        
        val revision = if (args.size > 1) {
            args[1].toIntOrNull() ?: 228
        } else {
            228
        }
        
        println("Downloading OSRS cache (revision $revision) to: ${cacheDir.absolutePath}")
        
        // Ensure cache directory exists
        cacheDir.mkdirs()
        
        val builder = Builder(
            type = TaskType.FRESH_INSTALL,
            revision = revision,
            cacheLocation = cacheDir
        )
        
        val tool = builder.build()
        tool.initialize()
        
        // Copy xteas.json to data/ directory (server expects it there)
        val xteasInCache = File(cacheDir, "xteas.json")
        val dataDir = cacheDir.parentFile ?: File("data")
        val xteasInData = File(dataDir, "xteas.json")
        
        if (xteasInCache.exists()) {
            xteasInCache.copyTo(xteasInData, overwrite = true)
            println("Copied xteas.json to ${xteasInData.absolutePath}")
        } else {
            println("Warning: xteas.json not found in cache directory!")
        }
        
        println("Cache download completed!")
    }
}

