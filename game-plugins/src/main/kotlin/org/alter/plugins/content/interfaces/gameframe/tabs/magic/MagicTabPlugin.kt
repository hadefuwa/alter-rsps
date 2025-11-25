package org.alter.plugins.content.interfaces.gameframe.tabs.magic

import org.alter.api.Skills
import org.alter.api.ext.message
import org.alter.api.ext.player
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.magic.MagicSpells
import org.alter.plugins.content.magic.canTeleport
import org.alter.plugins.content.magic.on_magic_spell_button
import org.alter.plugins.content.magic.teleport
import org.alter.plugins.content.magic.teleports.TeleportSpell

class MagicTabPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Track which buttons we've already registered to avoid duplicates
        val registeredButtons = mutableSetOf<Int>()
        
        TeleportSpell.values.forEach { teleport ->
            try {
                // Get the spell metadata to check the button hash
                if (!MagicSpells.isLoaded()) {
                    MagicSpells.loadSpellRequirements(world)
                }
                val spell = MagicSpells.getMetadata(teleport.spellName) ?: return@forEach
                
                // Calculate button hash
                val buttonHash = (spell.interfaceId shl 16) or spell.component
                
                // Skip if this button is already registered
                if (registeredButtons.contains(buttonHash)) {
                    println("Skipping duplicate button registration for spell '${teleport.spellName}' [parent=${spell.interfaceId}, child=${spell.component}]")
                    return@forEach
                }
                
                // Register the button
                on_magic_spell_button(teleport.spellName) { metadata ->
                    if (MagicSpells.canCast(player, metadata.lvl, metadata.items, metadata.spellbook)) {
                        if (player.canTeleport(teleport.type)) {
                            MagicSpells.removeRunes(player, metadata.items)
                            val area = teleport.endArea
                            val randomX = world.random(area.bottomLeftX..area.topRightX)
                            val randomZ = world.random(area.bottomLeftY..area.topRightY)
                            player.teleport(org.alter.game.model.Tile(randomX, randomZ), teleport.type)
                            player.addXp(Skills.MAGIC, teleport.xp)
                        }
                    }
                }
                
                // Mark this button as registered
                registeredButtons.add(buttonHash)
            } catch (e: Exception) {
                // If spell not found or registration fails, skip it
                println("Warning: Failed to register spell '${teleport.spellName}': ${e.message}")
            }
        }
    }
}
