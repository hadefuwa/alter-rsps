package org.alter.plugins.content.areas.godwars

import org.alter.api.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.plugin.*

/**
 * Plugin to fix NPCs 2242 and 2243 that are missing Attack options
 */
class BandosSpiritualsFixPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // Fix Spiritual Ranger (2242) - add Attack option and combat level
        onNpcSpawn("npc.spiritual_ranger_2242") {
            // Modify the NPC definition to make it attackable
            npc.def.combatLevel = 83
            npc.def.actions[1] = "Attack" // Set the second action slot to "Attack"
        }
        
        // Fix Spiritual Warrior (2243) - add Attack option and combat level  
        onNpcSpawn("npc.spiritual_warrior_2243") {
            // Modify the NPC definition to make it attackable
            npc.def.combatLevel = 83
            npc.def.actions[1] = "Attack" // Set the second action slot to "Attack"
        }
    }
}