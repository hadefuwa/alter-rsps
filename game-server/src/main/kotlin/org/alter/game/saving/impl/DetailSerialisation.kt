package org.alter.game.saving.impl

import org.alter.game.model.Tile
import org.alter.game.model.entity.Client
import org.alter.game.model.interf.DisplayMode
import org.alter.game.model.priv.Privilege
import org.alter.game.saving.DocumentHandler
import org.bson.Document

/**
 * Detail Serialisation Handler
 * 
 * This class handles serialization and deserialization of player detail information,
 * including player position (tile), privilege level, run energy, and display mode.
 * 
 * This is a critical component of the save/load system that ensures player data
 * persists across server restarts.
 * 
 * Key Responsibilities:
 * - Save player position (x, z, height coordinates) when player logs out
 * - Load player position when player logs in
 * - Handle missing or corrupted position data gracefully
 * - Save/load privilege level, run energy, and display mode settings
 */
class DetailSerialisation(override val name: String = "details") : DocumentHandler {

    /**
     * Deserialize player details from a saved document.
     * 
     * This method is called when loading a player's saved data. It extracts:
     * - Player position (tile coordinates)
     * - Privilege level
     * - Run energy
     * - Display mode
     * 
     * Important: The tile loading includes error handling to prevent crashes
     * if the saved data is missing or corrupted. If tile data cannot be loaded,
     * the player will spawn at their home location as a fallback.
     * 
     * @param client The client/player being loaded
     * @param doc The BSON document containing saved player data
     */
    override fun fromDocument(client: Client, doc: Document) {
        /**
         * Load player position (tile coordinates)
         * 
         * The tile is stored as a list of 3 integers: [x, z, height]
         * We use try-catch to handle cases where:
         * - The "tile" field is missing from saved data
         * - The field exists but isn't a List
         * - The field is in an unexpected format
         * - The field exists but has wrong type (e.g., String instead of List)
         */
        
        // try-catch is like saying "try to do this, but if it fails, do something else instead"
        // This prevents the server from crashing if the saved data is broken
        val tileData = try {
            // doc.getList() tries to get a list from the saved document
            // "tile" is the name of the field we're looking for
            // Int::class.java tells it we expect a list of integers
            // If this works, tileData will contain the list [x, z, height]
            doc.getList("tile", Int::class.java)
        } catch (e: Exception) {
            // If anything goes wrong (field missing, wrong type, etc.), catch the error
            // and set tileData to null instead of crashing
            // null means "nothing" or "empty" in programming
            null
        }
        
        /**
         * Set the player's tile position
         * 
         * We validate that:
         * 1. tileData is not null (field exists and is correct type)
         * 2. tileData has at least 3 elements (x, z, height)
         * 
         * If validation fails, we fall back to the home location to prevent
         * the player from spawning in an invalid location or crashing the server.
         */
        
        // if-else is like saying "if this is true, do this, otherwise do that"
        // != means "not equal to"
        // && means "and" (both conditions must be true)
        // >= means "greater than or equal to"
        client.tile = if (tileData != null && tileData.size >= 3) {
            // This runs if tileData exists AND has at least 3 numbers in it
            
            // Tile() is a function that creates a location from 3 numbers
            // tileData[0] gets the first number from the list (x coordinate)
            // tileData[1] gets the second number from the list (z coordinate)
            // tileData[2] gets the third number from the list (height level)
            // Lists start counting from 0, so [0] is first, [1] is second, [2] is third
            Tile(tileData[0], tileData[1], tileData[2])
        } else {
            // This runs if tileData is null OR doesn't have enough numbers
            // We send the player to their home location (usually Lumbridge) as a safe fallback
            // client.world.gameContext.home gets the home location from the game settings
            client.world.gameContext.home
        }
        
        /**
         * Load privilege level
         * 
         * Privileges determine what commands and features a player can access.
         * Defaults to DEFAULT privilege if not found in saved data.
         */
        
        // doc.getString("privilege") tries to get a text string from the saved document
        // client.world.privileges.get() looks up the privilege by name
        // ?: is called the "Elvis operator" - it means "if the left side is null, use the right side instead"
        // So if get() returns null (privilege not found), use Privilege.DEFAULT instead
        client.privilege = client.world.privileges.get(doc.getString("privilege"))?: Privilege.DEFAULT
        
        /**
         * Load run energy
         * 
         * Run energy determines how long a player can run before needing to walk.
         * Defaults to 10000 (100%) if not found in saved data.
         */
        
        // doc.getDouble() gets a decimal number from the saved document
        // ?: 10000.00 means if the value is null (missing), use 10000.00 instead
        // 10000 represents 100% run energy (the game uses 10000 as full, not 100)
        client.runEnergy = doc.getDouble("runEnergy") ?: 10000.00
        
        /**
         * Load display mode
         * 
         * Display mode determines the game window layout (Fixed, Resizable, etc.).
         * Defaults to FIXED mode if not found in saved data.
         */
        
        // DisplayMode.values gets all possible display modes (Fixed, Resizable, etc.)
        // firstOrNull { } searches through them and finds the first one that matches
        // it.name == doc.getString("displayMode") checks if the name matches what's saved
        // ?: DisplayMode.FIXED means if nothing matches (null), use FIXED mode
        client.interfaces.displayMode = DisplayMode.values.firstOrNull { it.name == doc.getString("displayMode") } ?: DisplayMode.FIXED
    }

    /**
     * Serialize player details to a document for saving.
     * 
     * This method is called when saving a player's data (on logout).
     * It creates a BSON document containing:
     * - Player position as a list [x, z, height]
     * - Privilege level name
     * - Current run energy
     * - Display mode name
     * 
     * The document is then saved to persistent storage (JSON file or MongoDB)
     * and can be loaded later when the player logs back in.
     * 
     * @param client The client/player being saved
     * @return A BSON document containing all player detail information
     */
    override fun asDocument(client: Client): Document = Document()
        // This function saves the player's data when they log out
        // Document() creates a new empty document (like an empty file to write data into)
        // .append() adds data to the document (like writing a line in a file)
        
        // Save player position as a list of 3 integers: [x, z, height]
        // listOf() creates a list containing the three numbers
        // client.tile.x gets the X coordinate from the player's current position
        // client.tile.z gets the Z coordinate (north-south position)
        // client.tile.height gets the height level (surface or underground)
        .append("tile", listOf(client.tile.x, client.tile.z, client.tile.height))
        
        // Save privilege level as uppercase string (e.g., "DEFAULT", "ADMIN", "MODERATOR")
        // client.privilege.name gets the privilege name (like "DEFAULT")
        // .uppercase() converts it to all capital letters (like "DEFAULT")
        .append("privilege", client.privilege.name.uppercase())
        
        // Save current run energy as a double value (decimal number)
        // client.runEnergy gets the player's current run energy (like 7500 for 75%)
        .append("runEnergy", client.runEnergy)
        
        // Save display mode as uppercase string (e.g., "FIXED", "RESIZABLE")
        // client.interfaces.displayMode.name gets the display mode name
        // .uppercase() converts it to all capital letters
        .append("displayMode", client.interfaces.displayMode.name.uppercase())

}