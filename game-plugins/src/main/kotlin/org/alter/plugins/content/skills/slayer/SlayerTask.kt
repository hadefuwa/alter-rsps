package org.alter.plugins.content.skills.slayer

import org.alter.rscm.RSCM

enum class SlayerTask(
    val npcIds: IntArray,
    val requiredLevel: Int,
    val minAmount: Int,
    val maxAmount: Int,
    val xp: Double = 0.0
) {
    CRAWLING_HAND(
        npcIds = intArrayOf(1648, 1649, 1650),
        requiredLevel = 5,
        minAmount = 15,
        maxAmount = 30
    ),
    BANSHEE(
        npcIds = intArrayOf(1618),
        requiredLevel = 15,
        minAmount = 20,
        maxAmount = 40
    ),
    GOBLIN(
        // Using common goblin IDs. In a real scenario, use RSCM or Npcs references.
        npcIds = intArrayOf(3029, 3030, 3031, 3032, 3033, 3034, 3035, 3036, 3037, 3038, 3039, 3040, 3041, 3042, 3043, 3044, 3045, 3046, 3047, 3048),
        requiredLevel = 1,
        minAmount = 10,
        maxAmount = 20
    ),
    BLUE_DRAGON(
        npcIds = intArrayOf(265, 266, 267, 268, 269),
        requiredLevel = 1,
        minAmount = 10,
        maxAmount = 20
    );

    companion object {
        val values = enumValues<SlayerTask>()
    }
}
