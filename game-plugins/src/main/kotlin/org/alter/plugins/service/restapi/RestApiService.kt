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
        try {
            println("========================================")
            println("[REST API] RestApiService.init() STARTING")
            println("========================================")
            
            val port = serviceProperties.getOrDefault("port", 8080)
            // serverIp is used for client configuration (what IP clients connect to), not for binding
            val serverIp = serviceProperties.getOrDefault("server-ip", "127.0.0.1")
            val serverHostname = serviceProperties.getOrDefault("server-hostname", serverIp)
            println("[REST API] Setting port to $port")
            println("[REST API] Setting server IP to $serverIp")
            println("[REST API] Setting server hostname to $serverHostname")
            
            // CRITICAL: Bind to all interfaces (0.0.0.0) to accept connections from LAN
            println("[REST API] Binding to all interfaces (0.0.0.0)...")
            ipAddress("0.0.0.0")
            
            // CRITICAL: Set port FIRST (before any routes are registered)
            println("[REST API] Calling port($port)...")
            port(port)
            println("[REST API] Port set successfully!")
            logger.info { "REST API Service starting on port $port with server IP $serverIp, hostname $serverHostname" }
            
            // Initialize CORS
            println("[REST API] Initializing CORS...")
            CorsRoute(
                serviceProperties.getOrDefault("origin", "*"),
                serviceProperties.getOrDefault("methods", "GET, POST"),
                serviceProperties.getOrDefault("headers", "X-PINGOTHER, Content-Type"),
            )
            println("[REST API] CORS initialized!")
            
            // Register routes AFTER port is set
            println("[REST API] Initializing routes...")
            RestApiRoutes().init(world, serviceProperties.getOrDefault("auth", false), serverIp, port, serverHostname)
            println("[REST API] Routes initialized!")
            
            // Ensure Spark is ready
            println("[REST API] Waiting for Spark initialization...")
            awaitInitialization()
            println("[REST API] Spark server initialized and ready!")
            logger.info { "REST API Service initialized - bootstrap.json endpoint available at http://$serverIp:$port/bootstrap.json" }
            
            println("========================================")
            println("[REST API] RestApiService.init() COMPLETE")
            println("========================================")
        } catch (e: Exception) {
            println("========================================")
            println("[REST API] ERROR in RestApiService.init()!")
            println("[REST API] Exception: ${e.javaClass.simpleName}")
            println("[REST API] Message: ${e.message}")
            println("========================================")
            e.printStackTrace()
            throw e
        }
    }

    override fun terminate(
        server: Server,
        world: World,
    ) {
        stop()
    }
}
