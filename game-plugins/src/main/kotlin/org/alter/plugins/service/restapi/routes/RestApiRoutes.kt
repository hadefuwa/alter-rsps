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
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * @TODO Http-api
 */
class RestApiRoutes {
    fun init(
        world: World,
        auth: Boolean,
    ) {
        println("[REST API] RestApiRoutes.init() called - registering routes...")
        
        // Serve bootstrap.json FIRST to ensure it's registered before other routes
        println("[REST API] Registering /bootstrap.json route...")
        get("/bootstrap.json") { req, res ->
            println("[REST API] bootstrap.json endpoint CALLED - request received!")
            res.type("application/json")
            try {
                // Get the project root directory - try multiple methods
                val userDir = System.getProperty("user.dir")
                val projectRoot = if (userDir != null) {
                    Paths.get(userDir)
                } else {
                    Paths.get(".").toAbsolutePath().normalize()
                }
                
                // Try multiple possible locations for bootstrap.json
                val possiblePaths = listOf(
                    projectRoot.resolve("bootstrap.json"),  // Project root (absolute)
                    Paths.get("bootstrap.json").toAbsolutePath().normalize(),  // Current directory (absolute)
                    projectRoot.resolve("data").resolve("bootstrap.json"),  // Data directory (absolute)
                    Paths.get("bootstrap.json"),  // Relative current directory
                    Paths.get("data/bootstrap.json")  // Relative data directory
                )
                
                var bootstrapPath: java.nio.file.Path? = null
                for (path in possiblePaths) {
                    val normalizedPath = path.toAbsolutePath().normalize()
                    if (Files.exists(normalizedPath) && Files.isRegularFile(normalizedPath)) {
                        bootstrapPath = normalizedPath
                        break
                    }
                }
                
                if (bootstrapPath == null || !Files.exists(bootstrapPath)) {
                    res.status(404)
                    val pathsChecked = possiblePaths.map { it.toAbsolutePath().normalize().toString() }.joinToString(", ")
                    res.body("{\"error\": \"bootstrap.json not found. Checked paths: $pathsChecked, user.dir: $userDir\"}")
                    return@get null
                }

                // Read and serve the bootstrap.json file
                val bootstrapContent = Files.readString(bootstrapPath)
                res.body(bootstrapContent)
                null
            } catch (e: Exception) {
                println("[REST API] EXCEPTION in bootstrap.json handler: ${e.message}")
                e.printStackTrace()
                res.status(500)
                res.body("{\"error\": \"Error serving bootstrap.json: ${e.message}\", \"stack\": \"${e.stackTraceToString()}\"}")
                null
            }
        }
        println("[REST API] /bootstrap.json route registered successfully!")

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
                // Use server's LAN IP - clients will connect via this IP
                // For LAN access, use the server's actual LAN IP
                val serverIp = "192.168.0.13"
                val serverPort = 8080
                val worldListUrl = "http://$serverIp:$serverPort/world_list.ws"

                // Codebase: For RuneLite/RuneAvion clients, must include port for gamepack download
                // The codebase URL points to where the gamepack JAR file is hosted
                val codebaseUrl = "http://$serverIp:$serverPort/"
                
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
                                 Files.exists(Paths.get(userDir, "bootstrap.json"))
            res.body("""{"user.dir": "$userDir", "bootstrap.json.exists": $bootstrapExists, "timestamp": ${System.currentTimeMillis()}}""")
            null
        }

        // Serve bootstrap.json.sha256 signature file
        get("/bootstrap.json.sha256") { req, res ->
            res.type("text/plain")
            try {
                // Try multiple possible locations for bootstrap.json
                val possiblePaths = listOf(
                    Paths.get("bootstrap.json"),  // Current directory
                    Paths.get("data/bootstrap.json"),  // Data directory
                    Paths.get(System.getProperty("user.dir"), "bootstrap.json")  // Project root
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
     */
    private fun writeString(buf: io.netty.buffer.ByteBuf, value: String) {
        val bytes = value.toByteArray()
        buf.writeBytes(bytes)
        buf.writeByte(0)
    }
}
