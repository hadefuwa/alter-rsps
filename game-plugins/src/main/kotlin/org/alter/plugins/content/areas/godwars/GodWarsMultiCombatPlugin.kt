package org.alter.plugins.content.areas.godwars

import org.alter.api.dsl.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * God Wars Dungeon Multi-Combat Plugin
 * 
 * Sets the God Wars Dungeon as a multi-combat area.
 * This allows:
 * - Multiple NPCs to attack the same target
 * - Faction fighting between NPCs
 * - Players to be attacked by multiple NPCs
 */
class GodWarsMultiCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        // God Wars Dungeon regions (height = 2 for main dungeon floors)
        // Region calculations: (floor(x/64) * 256) + floor(z/64)
        
        // Saradomin area: x~2888-2909, z~5295-5304
        // Region: (45 * 256) + 82 = 11602
        setMultiCombatRegion(11602)
        
        // Zamorak area: x~2872-2896, z~5321-5334
        // Region: (45 * 256) + 83 = 11603
        setMultiCombatRegion(11603)
        
        // Armadyl area: x~2861-2879, z~5281-5294
        // Region: (44 * 256) + 82 = 11346
        setMultiCombatRegion(11346)
        
        // Bandos area: x~2854-2886, z~5321-5333
        // Region: (44 * 256) + 83 = 11347
        setMultiCombatRegion(11347)
        
        // Additional adjacent regions to cover walkable areas
        setMultiCombatRegion(11345)  // Adjacent region
        setMultiCombatRegion(11601)  // Adjacent region
        setMultiCombatRegion(11604)  // Adjacent region
        setMultiCombatRegion(11858)  // Adjacent region
        setMultiCombatRegion(11859)  // Adjacent region
    }
}

