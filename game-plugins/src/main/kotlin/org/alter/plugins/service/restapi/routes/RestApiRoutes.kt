package org.alter.plugins.service.restapi.routes

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.netty.buffer.Unpooled
import org.alter.game.model.World
import org.alter.plugins.service.restapi.controllers.OnlinePlayersController
import org.alter.plugins.service.restapi.controllers.PlayerController
import org.alter.plugins.service.worldlist.model.WorldEntry
import org.alter.plugins.service.worldlist.model.WorldLocation
import org.alter.plugins.service.worldlist.model.WorldType
import spark.Spark.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * @TODO Http-api
 */
class RestApiRoutes {
    // These are used for client configuration (what IP clients connect to), not for binding
    private var serverIp: String = "127.0.0.1"
    private var serverPort: Int = 8080
    private var serverHostname: String = "127.0.0.1"
    
    fun init(
        world: World,
        auth: Boolean,
        serverIp: String = "127.0.0.1",
        serverPort: Int = 8080,
        serverHostname: String = "127.0.0.1",
    ) {
        this.serverIp = serverIp
        this.serverPort = serverPort
        this.serverHostname = serverHostname
        println("[REST API] RestApiRoutes.init() called - registering routes...")
        println("[REST API] Server IP: $serverIp, Port: $serverPort, Hostname: $serverHostname")
        
        // Serve bootstrap.json FIRST to ensure it's registered before other routes
        println("[REST API] Registering /bootstrap.json route...")
        try {
            get("/bootstrap.json") { req, res ->
            res.type("application/json")
            try {
                // First try to read from file
                val userDir = System.getProperty("user.dir")
                val possiblePaths = listOf(
                    Paths.get("bootstrap.json"),
                    Paths.get("config", "bootstrap.json"),
                    Paths.get(userDir, "bootstrap.json"),
                    Paths.get(userDir, "config", "bootstrap.json"),
                    Paths.get(userDir ?: ".", "bootstrap.json").toAbsolutePath().normalize(),
                    Paths.get(userDir ?: ".", "config", "bootstrap.json").toAbsolutePath().normalize()
                )
                
                var bootstrapPath: java.nio.file.Path? = null
                for (path in possiblePaths) {
                    val normalizedPath = path.toAbsolutePath().normalize()
                    if (Files.exists(normalizedPath) && Files.isRegularFile(normalizedPath)) {
                        bootstrapPath = normalizedPath
                        break
                    }
                }
                
                // If file exists, serve it
                if (bootstrapPath != null && Files.exists(bootstrapPath)) {
                    val bootstrapContent = Files.readString(bootstrapPath)
                    res.body(bootstrapContent)
                    return@get null
                }
                
                // Otherwise, serve embedded bootstrap.json (fallback)
                val embeddedBootstrap = """
                    {
                        "artifacts": [
                            {
                                "hash": "caf3eb630dc6d5b46a20cd6c075cf7f61e37df79daa4fbb7d7fca156bd37c6ca",
                                "name": "client-1.11.11.jar",
                                "path": "https://repo.runelite.net/net/runelite/client/1.11.11/client-1.11.11.jar",
                                "size": 5203409
                            }
                        ],
                        "clientJvm17Arguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "clientJvm17MacArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500",
                            "--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED"
                        ],
                        "clientJvm9Arguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "clientJvmArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500",
                            "-Xincgc",
                            "-XX:+UseConcMarkSweepGC",
                            "-XX:+UseParNewGC"
                        ],
                        "launcherArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500",
                            "-Xincgc",
                            "-XX:+UseConcMarkSweepGC",
                            "-XX:+UseParNewGC"
                        ],
                        "launcherJvm11Arguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "launcherJvm11WindowsArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "launcherJvm17Arguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "launcherJvm17MacArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500",
                            "--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED"
                        ],
                        "launcherJvm17WindowsArguments": [
                            "-XX:+DisableAttachMechanism",
                            "-Drunelite.launcher.nojvm=true",
                            "-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll",
                            "-Xmx768m",
                            "-Xss2m",
                            "-XX:CompileThreshold=1500"
                        ],
                        "version": "1.11.11"
                    }
                """.trimIndent()
                res.body(embeddedBootstrap)
                null
            } catch (e: Exception) {
                println("[REST API] EXCEPTION in bootstrap.json handler: ${e.message}")
                e.printStackTrace()
                res.status(500)
                res.body("{\"error\": \"Error serving bootstrap.json: ${e.message}\"}")
                null
            }
        }
        println("[REST API] /bootstrap.json route registered successfully!")
        } catch (e: Exception) {
            println("[REST API] ERROR registering /bootstrap.json route: ${e.message}")
            e.printStackTrace()
            throw e
        }

        // RuneLite WorldService endpoint - returns JSON world list
        println("[REST API] Registering /worlds route...")
        try {
            get("/worlds") { req, res ->
            res.type("application/json")
            try {
                val worldJsonPath = Paths.get("data/cfg/world.json")
                val worldEntries: List<Map<String, Any?>> = if (Files.exists(worldJsonPath)) {
                    Files.newBufferedReader(worldJsonPath).use { reader ->
                        val jsonList: List<Map<String, Any>> = Gson().fromJson(reader, object : TypeToken<List<Map<String, Any>>>() {}.type)
                        jsonList.map { json ->
                            // Convert to RuneLite WorldService format
                            mapOf<String, Any?>(
                                "id" to json["id"],
                                "types" to json["types"],
                                "address" to json["address"],
                                "activity" to json["activity"],
                                "location" to json["location"],
                                "players" to json["players"]
                            )
                        }
                    }
                } else {
                    emptyList()
                }
                res.body(Gson().toJson(worldEntries))
            } catch (e: Exception) {
                e.printStackTrace()
                res.status(500)
                res.body("""{"error": "Error loading world list: ${e.message}"}""")
            }
            null
        }
        println("[REST API] /worlds route registered successfully!")
        } catch (e: Exception) {
            println("[REST API] ERROR registering /worlds route: ${e.message}")
            e.printStackTrace()
            throw e
        }

        get("/players") {
                req, res ->
            Gson().toJson(OnlinePlayersController(req, res, false).init(world))
        }

        get("/player/:name") {
                req, res ->
            Gson().toJson(PlayerController(req, res, false).init(world))
        }
        get("/jav_config.ws") { req, res ->
            res.type("application/octet-stream")
            try {
                // RSProx matching: codebase hostname/IP must match world address
                // Blurite.io uses: codebase=http://127.0.0.1/ (no port) for local connections
                // For RSProx compatibility, use IP address without port in codebase
                // World list URL can use full URL with port
                val worldListUrl = "http://$serverIp:$serverPort/world_list.ws"

                // Codebase: Use IP address WITHOUT port (like blurite.io does with 127.0.0.1)
                // RSProx matches codebase host/IP with world address IP
                // Port is handled separately by RSProx via game_server_port config
                val codebaseUrl = "http://$serverIp/"
                
                // Generate custom jav_config.ws that points to our world_list.ws
                val javConfig = buildString {
                    appendLine("title=Alter")
                    appendLine("adverturl=http://www.runescape.com/g=oldscape/bare_advert.ws")
                    appendLine("codebase=$codebaseUrl")
                    appendLine("cachedir=alter")
                    appendLine("storebase=0")
                    appendLine("initial_jar=gamepack_2221869.jar")
                    appendLine("initial_class=client.class")
                    appendLine("termsurl=http://www.jagex.com/g=oldscape/terms/terms.ws")
                    appendLine("privacyurl=http://www.jagex.com/g=oldscape/privacy/privacy.ws")
                    appendLine("viewerversion=124")
                    appendLine("win_sub_version=1")
                    appendLine("mac_sub_version=2")
                    appendLine("other_sub_version=2")
                    appendLine("browsercontrol_win_x86_jar=browsercontrol_0_-1928975093.jar")
                    appendLine("browsercontrol_win_amd64_jar=browsercontrol_1_1674545273.jar")
                    appendLine("gedigesturl=https://secure.runescape.com/m=itemdb_oldschool/g=oldscape/digest.csv")
                    appendLine("download=2503642")
                    appendLine("window_preferredwidth=800")
                    appendLine("window_preferredheight=600")
                    appendLine("advert_height=96")
                    appendLine("applet_minwidth=765")
                    appendLine("applet_minheight=503")
                    appendLine("applet_maxwidth=5760")
                    appendLine("applet_maxheight=2160")
                    appendLine("msg=lang0=English")
                    appendLine("msg=tandc=This game is copyright © 1999 - 2025 Jagex Ltd.\\Use of this game is subject to our [\"https://legal.jagex.com/docs/terms\"Terms and Conditions] and [\"https://legal.jagex.com/docs/policies/privacy\"Privacy Policy]. [\"https://legal.jagex.com/docs/policies/privacy/exercising-your-rights\"Do Not Sell Or Share My Personal Information].")
                    appendLine("msg=options=Options")
                    appendLine("msg=language=Language")
                    appendLine("msg=changes_on_restart=Your changes will take effect when you next start this program.")
                    appendLine("msg=loading_app_resources=Loading application resources")
                    appendLine("msg=err_verify_bc64=Unable to verify browsercontrol64")
                    appendLine("msg=err_verify_bc=Unable to verify browsercontrol")
                    appendLine("msg=err_load_bc=Unable to load browsercontrol")
                    appendLine("msg=loading_app=Loading application")
                    appendLine("msg=err_create_target=Unable to create target applet")
                    appendLine("msg=err_create_advertising=Unable to create advertising")
                    appendLine("msg=err_save_file=Error saving file")
                    appendLine("msg=err_downloading=Error downloading")
                    appendLine("msg=ok=OK")
                    appendLine("msg=cancel=Cancel")
                    appendLine("msg=message=Message")
                    appendLine("msg=copy_paste_url=Please copy and paste the following URL into your web browser")
                    appendLine("msg=information=Information")
                    appendLine("msg=err_get_file=Error getting file")
                    appendLine("msg=new_version=Update available! You can now launch the client directly from the OldSchool website.\\nGet the new version from the link on the OldSchool homepage: http://oldschool.runescape.com/")
                    appendLine("msg=new_version_linktext=Open OldSchool Homepage")
                    appendLine("msg=new_version_link=http://oldschool.runescape.com/")
                    appendLine("param=6=0")
                    appendLine("param=4=1")
                    appendLine("param=11=https://auth.jagex.com/")
                    appendLine("param=20=https://social.auth.jagex.com/")
                    appendLine("param=3=true")
                    appendLine("param=16=false")
                    appendLine("param=2=https://payments.jagex.com/")
                    appendLine("param=12=255")
                    appendLine("param=17=$worldListUrl")  // CRITICAL: Points to our world_list.ws
                    appendLine("param=21=0")
                    appendLine("param=25=228")
                    appendLine("param=9=ElZAIrq5NpKN6D3mDdihco3oPeYN2KFy2DCquj7JMmECPmLrDP3Bnw")
                    appendLine("param=15=0")
                    appendLine("param=18=")
                    appendLine("param=13=.runescape.com")
                    appendLine("param=8=true")
                    appendLine("param=7=0")
                    appendLine("param=19=196515767263-1oo20deqm6edn7ujlihl6rpadk9drhva.apps.googleusercontent.com")
                    appendLine("param=22=https://auth.runescape.com/")
                    appendLine("param=14=0")
                    appendLine("param=28=https://account.jagex.com/")
                    appendLine("param=10=5")
                    appendLine("param=5=1")
                }
                
                res.raw().outputStream.write(javConfig.toByteArray())
                res.raw().outputStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                res.status(500)
                res.body("Error generating jav_config: ${e.message}")
            }
            null
        }






        get("/world_list.ws") { req, res ->
            res.type("application/octet-stream")
            try {
                // Load world list from world.json
                val worldJsonPath = Paths.get("data/cfg/world.json")
                val worldEntries: List<WorldEntry> = if (Files.exists(worldJsonPath)) {
                    Files.newBufferedReader(worldJsonPath).use { reader ->
                        val jsonList: List<Map<String, Any>> = Gson().fromJson(reader, object : TypeToken<List<Map<String, Any>>>() {}.type)
                        jsonList.map { json ->
                            val types = (json["types"] as List<String>).map { typeName ->
                                WorldType.valueOf(typeName)
                            }
                            val location = WorldLocation.valueOf(json["location"] as String)
                            // World address should be IP address - RSProx matches codebase IP with world address IP
                            // Codebase: http://192.168.0.13:8080/ -> IP: 192.168.0.13
                            // World address: 192.168.0.13 -> Direct IP match!
                            WorldEntry(
                                id = (json["id"] as Double).toInt(),
                                types = EnumSet.copyOf(types),
                                address = json["address"] as String,
                                activity = json["activity"] as String,
                                location = location,
                                players = (json["players"] as Double).toInt()
                            )
                        }
                    }
                } else {
                    emptyList()
                }

                // Generate world_list.ws binary format
                val worldListBuffer = this@RestApiRoutes.encodeWorldList(worldEntries)
                
                // Write length prefix + world list data
                val outputBuffer = Unpooled.buffer()
                outputBuffer.writeInt(worldListBuffer.readableBytes())
                outputBuffer.writeBytes(worldListBuffer)
                
                // Send response
                res.raw().outputStream.write(outputBuffer.array(), outputBuffer.arrayOffset(), outputBuffer.readableBytes())
                res.raw().outputStream.flush()
                
                outputBuffer.release()
                worldListBuffer.release()
            } catch (e: Exception) {
                e.printStackTrace()
                res.status(500)
                res.body("Error generating world list: ${e.message}")
            }
            null
        }

        // Test route to verify server is picking up changes
        get("/test-bootstrap") { req, res ->
            res.type("text/plain")
            res.body("Bootstrap route is working! Server has been rebuilt. Timestamp: ${System.currentTimeMillis()}")
            null
        }
        
        // Debug route to check if bootstrap.json route is registered
        get("/debug-routes") { req, res ->
            res.type("application/json")
            val userDir = System.getProperty("user.dir")
            val bootstrapExists = Files.exists(Paths.get("bootstrap.json")) || 
                                 Files.exists(Paths.get("config", "bootstrap.json")) ||
                                 Files.exists(Paths.get(userDir, "bootstrap.json")) ||
                                 Files.exists(Paths.get(userDir, "config", "bootstrap.json"))
            res.body("""{"user.dir": "$userDir", "bootstrap.json.exists": $bootstrapExists, "timestamp": ${System.currentTimeMillis()}}""")
            null
        }

        // Serve bootstrap.json.sha256 signature file
        get("/bootstrap.json.sha256") { req, res ->
            res.type("text/plain")
            try {
                // Try multiple possible locations for bootstrap.json
                val userDir = System.getProperty("user.dir")
                val possiblePaths = listOf(
                    Paths.get("bootstrap.json"),  // Current directory
                    Paths.get("config", "bootstrap.json"),  // Config directory
                    Paths.get("data", "bootstrap.json"),  // Data directory
                    Paths.get(userDir, "bootstrap.json"),  // Project root
                    Paths.get(userDir, "config", "bootstrap.json")  // Config directory from project root
                )
                
                var bootstrapPath: java.nio.file.Path? = null
                for (path in possiblePaths) {
                    if (Files.exists(path)) {
                        bootstrapPath = path
                        break
                    }
                }
                
                if (bootstrapPath == null || !Files.exists(bootstrapPath)) {
                    res.status(404)
                    res.body("bootstrap.json not found")
                    return@get null
                }

                // Calculate SHA256 hash of bootstrap.json
                val bootstrapBytes = Files.readAllBytes(bootstrapPath)
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(bootstrapBytes)
                val hashString = hashBytes.joinToString("") { "%02x".format(it) }
                
                res.body(hashString)
            } catch (e: Exception) {
                e.printStackTrace()
                res.status(500)
                res.body("Error generating bootstrap signature: ${e.message}")
            }
            null
        }

        // Serve the OSRS gamepack JAR file (required by RuneLite-based clients)
        get("/gamepack_2221869.jar") { req, res ->
            res.type("application/java-archive")
            try {
                // Path to the gamepack JAR in your cache
                val gamepackPath = Paths.get("data/cache/gamepack.jar")

                if (!Files.exists(gamepackPath)) {
                    res.status(404)
                    res.body("Gamepack not found. Please ensure gamepack.jar exists in data/cache/")
                    return@get null
                }

                // Read and serve the gamepack
                val gamepackBytes = Files.readAllBytes(gamepackPath)
                res.raw().outputStream.write(gamepackBytes)
                res.raw().outputStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
                res.status(500)
                res.body("Error serving gamepack: ${e.message}")
            }
            null
        }

        // RuneLite Item Prices endpoint - returns empty prices (client will work without prices)
        get("/item/prices.js") { req, res ->
            res.type("application/javascript")
            // Return empty prices object - client will work without item prices
            res.body("""define([], function() { return {}; });""")
            null
        }

        // RuneLite External Plugins manifest - return empty manifest (disable external plugins)
        get("/manifest.js") { req, res ->
            res.type("application/javascript")
            // Return empty manifest - no external plugins
            res.body("""define([], function() { return []; });""")
            null
        }

        // RuneLite Session ping endpoint - return success
        get("/session") { req, res ->
            res.type("application/json")
            // Return a simple success response
            res.body("""{"success":true}""")
            null
        }

        // RuneLite Session ping POST endpoint
        post("/session") { req, res ->
            res.type("application/json")
            res.body("""{"success":true}""")
            null
        }
    }

    /**
     * Encodes world list entries into the binary format expected by the client
     */
    private fun encodeWorldList(list: List<WorldEntry>): io.netty.buffer.ByteBuf {
        val buf = Unpooled.buffer()
        
        buf.writeShort(list.size)
        
        list.forEach { entry ->
            var mask = 0
            entry.types.forEach { type -> mask = mask or type.mask }
            
            buf.writeShort(entry.id)
            buf.writeInt(mask)
            writeString(buf, entry.address)
            writeString(buf, entry.activity)
            buf.writeByte(entry.location.id)
            buf.writeShort(entry.players)
        }
        
        return buf
    }
    
    /**
     * Writes a null-terminated string to the buffer
     * Uses UTF-8 encoding (standard for OSRS protocol)
     */
    private fun writeString(buf: io.netty.buffer.ByteBuf, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        buf.writeBytes(bytes)
        buf.writeByte(0)  // Null terminator
    }
}
