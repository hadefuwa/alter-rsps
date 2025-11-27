package org.alter.plugins.content.areas.godwars.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    init {
        /* NPC IDs
        spiritual_warrior:2210
        spiritual_ranger:2211
        spiritual_mage:2212
        knight_of_saradomin:2213
        knight_of_saradomin_2214:2214
        */
        // Saradomin Spawns (halved)
        spawnNpc(npc = "npc.spiritual_warrior", x = 2909, z = 5299, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        
        spawnNpc(npc = "npc.spiritual_mage", x = 2906, z = 5301, height = 2, walkRadius = 5, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.spiritual_ranger", x = 2902, z = 5302, height = 2, walkRadius = 5, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.knight_of_saradomin", x = 2901, z = 5303, height = 2, walkRadius = 5, direction = Direction.SOUTH)


        /* NPC IDs
        spiritual_ranger_2242:2242
        spiritual_warrior_2243:2243
        spiritual_mage_2244:2244
        goblin_2245:2245
        goblin_2246:2246
        */
        // Bandos Spawns (halved)
        spawnNpc(npc = "npc.spiritual_warrior_2243", x = 2854, z = 5333, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        
        spawnNpc(npc = "npc.spiritual_mage_2244", x = 2856, z = 5331, height = 2, walkRadius = 5, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.spiritual_ranger_2242", x = 2857, z = 5329, height = 2, walkRadius = 5, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.goblin_2245", x = 2901, z = 5303, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin_2246", x = 2889, z = 5304, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin_2245", x = 2886, z = 5327, height = 2, walkRadius = 5, direction = Direction.SOUTH)



        /* NPC IDs
        spiritual_warrior_3166:3166
        spiritual_ranger_3167:3167
        spiritual_mage_3168:3168
        aviansie:3169 (use "npc.aviansie" not "npc.aviansie_3169")
        */
        // Armadyl Spawns (halved)

        spawnNpc(npc = "npc.spiritual_warrior_3166", x = 2871, z = 5281, height = 2, walkRadius = 10, direction = Direction.SOUTH)
        
        spawnNpc(npc = "npc.spiritual_mage_3168", x = 2869, z = 5284, height = 2, walkRadius = 10, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.spiritual_ranger_3167", x = 2871, z = 5287, height = 2, walkRadius = 10, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.aviansie", x = 2870, z = 5284, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aviansie", x = 2868, z = 5289, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aviansie", x = 2863, z = 5289, height = 2, walkRadius = 5, direction = Direction.SOUTH)


         /* NPC IDs
        spiritual_warrior_3159:3159
        spiritual_ranger_3160:3160
        spiritual_mage_3161:3161
        imp:3134
        */
        // Zamarok Spawns (halved)

        spawnNpc(npc = "npc.spiritual_warrior_3159", x = 2885, z = 5328, height = 2, walkRadius = 10, direction = Direction.SOUTH)
        
        spawnNpc(npc = "npc.spiritual_mage_3161", x = 2880, z = 5331, height = 2, walkRadius = 10, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.spiritual_ranger_3160", x = 2872, z = 5329, height = 2, walkRadius = 10, direction = Direction.SOUTH)

        spawnNpc(npc = "npc.imp_3134", x = 2901, z = 5303, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.imp_3134", x = 2889, z = 5304, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.imp_3134", x = 2886, z = 5327, height = 2, walkRadius = 5, direction = Direction.SOUTH)


        // Item spawns
        spawnItem(item = "item.hammer", amount = 1, x = 2852, z = 5334, height = 2)
        spawnItem(item = "item.super_combat_potion4", amount = 1, x = 2852, z = 5334, height = 2)
        spawnItem(item = "item.ranging_potion4", amount = 1, x = 2869, z = 5280, height = 2)
        spawnItem(item = "item.ranging_potion4", amount = 1, x = 2911, z = 5297, height = 2)
        spawnItem(item = "item.super_combat_potion4", amount = 1, x = 2882, z = 5328, height = 2)
        spawnItem(item = "item.manta_ray", amount = 1, x = 2878, z = 5311, height = 2)
    

        
    }
}