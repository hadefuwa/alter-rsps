package org.alter.plugins.service.restapi

import gg.rsmod.util.ServerProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.service.Service
import org.alter.plugins.service.restapi.routes.CorsRoute
import org.alter.plugins.service.restapi.routes.RestApiRoutes
import spark.Spark.*

class RestApiService : Service {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    override fun init(
        server: Server,
        world: World,
        serviceProperties: ServerProperties,
    ) {
        val port = serviceProperties.getOrDefault("port", 8080)
        println("[REST API] Setting port to $port")
        port(port)
        logger.info { "REST API Service starting on port $port" }
        
        println("[REST API] Initializing CORS...")
        CorsRoute(
            serviceProperties.getOrDefault("origin", "*"),
            serviceProperties.getOrDefault("methods", "GET, POST"),
            serviceProperties.getOrDefault("headers", "X-PINGOTHER, Content-Type"),
        )
        
        println("[REST API] Initializing routes...")
        RestApiRoutes().init(world, serviceProperties.getOrDefault("auth", false))
        println("[REST API] Routes initialized!")
        logger.info { "REST API Service initialized - bootstrap.json endpoint available at http://localhost:$port/bootstrap.json" }
    }

    override fun terminate(
        server: Server,
        world: World,
    ) {
        stop()
    }
}
