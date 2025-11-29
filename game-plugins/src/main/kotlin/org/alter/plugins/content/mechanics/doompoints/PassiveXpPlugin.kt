package org.alter.plugins.content.mechanics.doompoints

import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.game.Server
import org.alter.game.model.World

/**
 * Passive XP Plugin
 * 
 * Hooks into XP gains to trigger passive XP with a chance based on perk level.
 * Similar to how agility XP from running works - triggers when XP is gained.
 */
class PassiveXpPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Hook into skill level ups (this fires when XP is gained and a level up occurs)
        // We'll also hook into combat XP gains directly
    }
}

/**
 * Extension function to add XP and check for passive XP perk
 * This should be used instead of player.addXp() in combat strategies
 */
fun Player.addXpWithPassiveCheck(skill: Int, xp: Double) {
    val oldXp = getSkills().getCurrentXp(skill)
    
    // Add the XP normally
    addXp(skill, xp)
    
    // Check for passive XP perk (only if XP was actually added)
    if (oldXp < getSkills().getCurrentXp(skill)) {
        DoomPoints.tryApplyPassiveXp(this, xp)
    }
}


