package org.alter.plugins.content.areas.slayertower.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**
 * Slayer Tower Spawn Plugin
 * 
 * This plugin spawns NPCs in the Slayer Tower located in Morytania.
 * The Slayer Tower is a multi-floor structure housing various slayer monsters:
 * 
 * Ground Floor (height 0):
 * - Crawling Hands: Low-level slayer monsters found upon entering
 * - Banshees: Require earmuffs or slayer helmet to prevent stat reductions
 * 
 * First Floor (height 1):
 * - Aberrant Spectres: Require nose peg or slayer helmet
 * - Bloodvelds: Large, aggressive monsters
 * - Infernal Mages: Magic-using slayer monsters
 * 
 * Top Floor (height 2):
 * - Gargoyles: Stone creatures that require a rock hammer to finish
 * - Nechryael: High-level slayer monsters
 * 
 * Coordinates are based on NPCList_OSRS.json
 */
class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        /**
         * Ground Floor (Height 0) - Crawling Hands
         * 
         * Crawling hands are low-level slayer monsters found on the ground floor.
         * They come in various sizes and combat levels.
         */
        spawnNpc(npc = "npc.crawling_hand_448", x = 3420, z = 3551, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_453", x = 3421, z = 3544, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_453", x = 3423, z = 3555, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_448", x = 3424, z = 3558, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_454", x = 3427, z = 3548, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_448", x = 3428, z = 3544, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.crawling_hand_448", x = 3428, z = 3553, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Ground Floor (Height 0) - Banshees
         * 
         * Banshees are aggressive monsters that require earmuffs or a slayer helmet
         * to prevent stat reductions when attacked. They are found on the ground floor.
         */
        spawnNpc(npc = "npc.banshee_414", x = 3433, z = 3552, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3436, z = 3559, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3439, z = 3539, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3439, z = 3544, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3440, z = 3560, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3443, z = 3546, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.banshee_414", x = 3444, z = 3537, height = 0, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * First Floor (Height 1) - Aberrant Spectres
         * 
         * Aberrant spectres are aggressive monsters that require a nose peg or
         * slayer helmet to prevent stat reductions. They use magic attacks.
         */
        spawnNpc(npc = "npc.aberrant_spectre_5", x = 3420, z = 3537, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_4", x = 3423, z = 3542, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_2", x = 3424, z = 3551, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_5", x = 3427, z = 3539, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_2", x = 3428, z = 3543, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_3", x = 3428, z = 3551, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_5", x = 3431, z = 3548, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_4", x = 3435, z = 3545, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_3", x = 3438, z = 3549, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_5", x = 3442, z = 3544, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.aberrant_spectre_4", x = 3442, z = 3550, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * First Floor (Height 1) - Bloodvelds
         * 
         * Bloodvelds are large, aggressive monsters that use melee attacks.
         * They are found on the first floor of the Slayer Tower.
         */
        spawnNpc(npc = "npc.bloodveld_484", x = 3424, z = 3560, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.bloodveld_484", x = 3426, z = 3557, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * First Floor (Height 1) - Infernal Mages
         * 
         * Infernal mages are magic-using slayer monsters found on the first floor.
         * They come in various color variants.
         */
        spawnNpc(npc = "npc.infernal_mage_443", x = 3433, z = 3556, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.infernal_mage_446", x = 3435, z = 3559, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.infernal_mage_445", x = 3438, z = 3555, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.infernal_mage_447", x = 3442, z = 3556, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.infernal_mage_445", x = 3443, z = 3560, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * First Floor (Height 1) - Mysterious Ghost
         *
         * A mysterious ghost that appears on the first floor.
         * Note: This NPC uses null_6122 in RSCM, which may require special handling.
         */
        spawnNpc(npc = "npc.null_6122", x = 3448, z = 3550, height = 1, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Top Floor (Height 2) - Gargoyles
         * 
         * Gargoyles are stone creatures found on the top floor of the Slayer Tower.
         * They require a rock hammer to finish off when their health is low.
         */
        spawnNpc(npc = "npc.gargoyle_412", x = 3432, z = 3540, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3435, z = 3548, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3437, z = 3537, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3439, z = 3542, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3439, z = 3548, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3443, z = 3548, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3445, z = 3541, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.gargoyle_412", x = 3446, z = 3535, height = 2, walkRadius = 5, direction = Direction.SOUTH)
        
        /**
         * Top Floor (Height 2) - Nechryael
         * 
         * Nechryael are high-level slayer monsters found on the top floor.
         * They can summon death spawns during combat.
         */
        spawnNpc(npc = "npc.nechryael_8", x = 3445, z = 3560, height = 2, walkRadius = 5, direction = Direction.SOUTH)
    }
}

