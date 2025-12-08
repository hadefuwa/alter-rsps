package org.alter.plugins.content.raids.tob

object TobConstants {
    // Regions calculated from user provided coordinates
    // room 1: 3292, 4443 -> Region 13125
    const val REGION_MAIDEN = 13125
    // room 2: 3171, 4386 -> Region 12612
    const val REGION_BLOAT = 12612
    // room 3: 3173, 4449 -> Region 12613
    const val REGION_NYLOCAS = 12613
    // room 4: 3278, 4313 -> Region 13123
    const val REGION_SOTETSEG = 13123
    // room 5: 3296, 4294 -> Region 13123 (Same region as Sote)
    const val REGION_XARPUS = 13123
    // final room: 3169, 4312 -> Region 12611
    const val REGION_VERZIK = 12611
    // reward room: 3234, 4321 -> Region 12867
    const val REGION_REWARD = 12867

    const val EXIT_TILE_X = 3677
    const val EXIT_TILE_Z = 3219

    // NPC IDs
    const val MAIDEN_NPC_ID = 8360
    const val BLOAT_NPC_ID = 8359
    const val NYLOCAS_BOSS_ID = 10807 // Vasilias
    const val SOTETSEG_NPC_ID = 8388
    const val XARPUS_NPC_ID = 8338
    const val VERZIK_NPC_ID = 8370

    // NPC Keys (String identifiers for legacy/debugging, though we use Int now)
    const val MAIDEN_KEY = "the_maiden_of_sugadinti"
    const val BLOAT_KEY = "pestilent_bloat"
    const val NYLOCAS_KEY = "nylocas_vasilias_10807"
    const val SOTETSEG_KEY = "sotetseg_8388"
    const val XARPUS_KEY = "xarpus"
    const val VERZIK_KEY = "verzik_vitur_8370"

    // Objects
    const val ENTER_RAID_OBJECT = 32653
    const val BARRIER_OBJECT = 32755
    const val TREASURE_CHEST = 32990

    // Exact spawn/center tiles provided by user (Global coordinates)
    // We use these to calculate relative offsets in the instance
    val MAIDEN_SPAWN = Pair(3292, 4443)
    val BLOAT_SPAWN = Triple(3171, 4386, 1) // Height 1
    val NYLOCAS_SPAWN = Pair(3173, 4449)
    val SOTETSEG_SPAWN = Pair(3278, 4313)
    val XARPUS_SPAWN = Pair(3296, 4294)
    val VERZIK_SPAWN = Pair(3169, 4312)
    val REWARD_SPAWN = Pair(3234, 4321)
}
