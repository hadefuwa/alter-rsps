package org.alter.plugins.content.areas.rellekka.spawns

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
        // Height 0
        // 
        // MISSING MAPPING for ID 1172 ()
        // spawnNpc(npc = "npc.1172", x = 2678, z = 3670, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Agnar
        spawnNpc(npc = "npc.agnar", x = 2642, z = 3677, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Askeladden
        spawnNpc(npc = "npc.null_3927", x = 2658, z = 3660, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Bjorn
        spawnNpc(npc = "npc.bjorn", x = 2655, z = 3673, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Blanin
        spawnNpc(npc = "npc.blanin", x = 2675, z = 3671, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Borrokar
        spawnNpc(npc = "npc.borrokar", x = 2679, z = 3690, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Brundt the Chieftain
        spawnNpc(npc = "npc.null_3926", x = 2659, z = 3669, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Chicken
        spawnNpc(npc = "npc.chicken_1174", x = 2679, z = 3663, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chicken_1173", x = 2679, z = 3665, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chicken_1173", x = 2680, z = 3664, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.chicken_1174", x = 2681, z = 3663, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Eldgrim
        spawnNpc(npc = "npc.eldgrim", x = 2658, z = 3674, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Fish monger
        spawnNpc(npc = "npc.fish_monger", x = 2646, z = 3675, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Fishing spot
        spawnNpc(npc = "npc.fishing_spot_3913", x = 2633, z = 3687, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.fishing_spot_3913", x = 2633, z = 3690, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Freidir
        spawnNpc(npc = "npc.freidir", x = 2674, z = 3675, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Fridgeir
        spawnNpc(npc = "npc.fridgeir", x = 2659, z = 3678, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Fur trader
        spawnNpc(npc = "npc.fur_trader_3948", x = 2640, z = 3675, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Guard
        spawnNpc(npc = "npc.null_6714", x = 2640, z = 3656, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.null_6714", x = 2643, z = 3679, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3928", x = 2657, z = 3663, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.guard_3929", x = 2660, z = 3663, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.null_6714", x = 2663, z = 3656, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.null_6714", x = 2670, z = 3675, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.null_6714", x = 2684, z = 3657, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Inga
        spawnNpc(npc = "npc.inga", x = 2674, z = 3677, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Ingrid Hradson
        spawnNpc(npc = "npc.ingrid_hradson", x = 2670, z = 3662, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Jennella
        spawnNpc(npc = "npc.jennella", x = 2640, z = 3651, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Lanzig
        spawnNpc(npc = "npc.lanzig", x = 2676, z = 3665, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Lensa
        spawnNpc(npc = "npc.lensa_3943", x = 2655, z = 3652, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Longhall Bouncer
        spawnNpc(npc = "npc.longhall_bouncer", x = 2667, z = 3684, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Manni the Reveller
        spawnNpc(npc = "npc.manni_the_reveller", x = 2660, z = 3673, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Market Guard
        spawnNpc(npc = "npc.market_guard_3949", x = 2635, z = 3676, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.market_guard_3949", x = 2644, z = 3670, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.market_guard_3949", x = 2644, z = 3677, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.market_guard_3949", x = 2644, z = 3683, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.market_guard_3949", x = 2650, z = 3676, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Olaf the Bard
        spawnNpc(npc = "npc.olaf_the_bard", x = 2673, z = 3683, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Ospak
        spawnNpc(npc = "npc.ospak", x = 2660, z = 3680, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Peer the Seer
        spawnNpc(npc = "npc.peer_the_seer", x = 2634, z = 3668, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Pontak
        spawnNpc(npc = "npc.pontak", x = 2666, z = 3652, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Rooster
        spawnNpc(npc = "npc.rooster", x = 2680, z = 3662, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Sigli the Huntsman
        spawnNpc(npc = "npc.sigli_the_huntsman", x = 2660, z = 3653, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Sigmund The Merchant
        spawnNpc(npc = "npc.sigmund_the_merchant", x = 2641, z = 3680, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Styrmir
        spawnNpc(npc = "npc.styrmir", x = 2657, z = 3680, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Swensen the Navigator
        spawnNpc(npc = "npc.swensen_the_navigator", x = 2646, z = 3660, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Thora the Barkeep
        spawnNpc(npc = "npc.thora_the_barkeep", x = 2662, z = 3673, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Torbrund
        spawnNpc(npc = "npc.torbrund", x = 2658, z = 3679, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Town Guard
        spawnNpc(npc = "npc.town_guard", x = 2660, z = 3646, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.town_guard_3931", x = 2664, z = 3646, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Warrior
        spawnNpc(npc = "npc.warrior_3950", x = 2630, z = 3676, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2634, z = 3651, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2643, z = 3674, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2650, z = 3652, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2655, z = 3691, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2666, z = 3678, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2668, z = 3658, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2668, z = 3670, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.warrior_3950", x = 2685, z = 3653, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Wolf
        spawnNpc(npc = "npc.wolf_3912", x = 2630, z = 3631, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.wolf_3912", x = 2630, z = 3637, height = 0, walkRadius = 5, direction = Direction.SOUTH)

        // Height 3
        // BigRedJapan
        spawnNpc(npc = "npc.bigredjapan", x = 2656, z = 3676, height = 3, walkRadius = 5, direction = Direction.SOUTH)

    }
}
