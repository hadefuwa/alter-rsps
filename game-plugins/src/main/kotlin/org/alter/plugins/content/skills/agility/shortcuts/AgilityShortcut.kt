package org.alter.plugins.content.skills.agility.shortcuts

/**
 * Represents an agility shortcut.
 * @param objectId The object ID of the shortcut
 * @param option The option name (e.g., "hop", "cross", "jump")
 * @param requiredLevel The required agility level
 * @param endTileX The X coordinate of the destination tile (relative to object or absolute)
 * @param endTileZ The Z coordinate of the destination tile (relative to object or absolute)
 * @param endTileHeight The height of the destination tile (defaults to same as object)
 * @param animationId The animation to play when crossing
 * @param soundId The sound to play when crossing (optional)
 * @param useRelativePosition Whether endTileX/Z are relative to object position (true) or absolute (false)
 * @param directionAngle The direction angle for forced movement (optional, calculated if not provided)
 * @param clientDuration1 First duration for forced movement (default: 33)
 * @param clientDuration2 Second duration for forced movement (default: 60)
 */
data class AgilityShortcut(
    val objectId: Int,
    val option: String,
    val requiredLevel: Int,
    val endTileX: Int,
    val endTileZ: Int,
    val endTileHeight: Int? = null,
    val animationId: Int = 762, // Default hop animation
    val soundId: Int? = null,
    val useRelativePosition: Boolean = true,
    val directionAngle: Int? = null,
    val clientDuration1: Int = 33,
    val clientDuration2: Int = 60
)

