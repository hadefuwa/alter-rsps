package org.alter.plugins.content.areas.lumbridge.spawns

import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

/**Example
 *spawnNpc(npc = "npc.ID", x = xxxx, y = zzzz, height = 0, walk = 0, direction = Direction.NORTH)
 */


class SpawnPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    init {
        spawnNpc(npc = "npc.man_3106", x = 3206, z = 3219, walkRadius = 20, height = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3106", x = 3216, z = 3219, walkRadius = 20, direction = Direction.EAST)
        spawnNpc(npc = "npc.man_3106", x = 3207, z = 3227, walkRadius = 20, direction = Direction.EAST)
        spawnNpc(npc = "npc.man_3108", x = 3209, z = 3215, walkRadius = 20, height = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3108", x = 3221, z = 3219, walkRadius = 20, direction = Direction.EAST)
        spawnNpc(npc = "npc.woman_3111", x = 3211, z = 3213, walkRadius = 20, height = 1, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.woman_3111", x = 3217, z = 3205, walkRadius = 20, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3207, z = 3202, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3205, z = 3204, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3206, z = 3202, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3207, z = 3203, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.rat_2854", x = 3205, z = 3209, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.rat_2854", x = 3207, z = 3209, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.imp_5007", x = 3217, z = 3226, walkRadius = 20, direction = Direction.EAST)
        spawnNpc(npc = "npc.sheep_2789", x = 3196, z = 3263, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.sheep_2789", x = 3199, z = 3261, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.sheep_2789", x = 3201, z = 3272, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.sheep_2789", x = 3202, z = 3268, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.sheep_2789", x = 3206, z = 3266, walkRadius = 10, direction = Direction.WEST)
        spawnNpc(npc = "npc.ram_1265", x = 3201, z = 3263, walkRadius = 10, direction = Direction.NORTH)
        spawnNpc(npc = "npc.ram_1265", x = 3207, z = 3271, walkRadius = 10, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.ram_1265", x = 3195, z = 3271, walkRadius = 10, direction = Direction.EAST)
        spawnNpc(npc = "npc.huge_spider_134", x = 3168, z = 3243, walkRadius = 7, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_spider", x = 3165, z = 3251, walkRadius = 7, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_spider_3018", x = 3246, z = 3248, walkRadius = 7, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_spider_3017", x = 3241, z = 3245, walkRadius = 7, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.giant_spider_3017", x = 3253, z = 3243, walkRadius = 7, direction = Direction.WEST)
        spawnNpc(npc = "npc.giant_spider_3017", x = 3245, z = 3235, walkRadius = 7, direction = Direction.NORTH)
        spawnNpc(npc = "npc.giant_spider_3017", x = 3253, z = 3234, walkRadius = 7, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin_3028", x = 3264, z = 3232, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin_2246", x = 3247, z = 3244, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.goblin_2248", x = 3244, z = 3244, walkRadius = 8, direction = Direction.NORTH)
        spawnNpc(npc = "npc.goblin_2484", x = 3241, z = 3242, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.goblin_3028", x = 3253, z = 3245, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin_3039", x = 3255, z = 3236, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin_3054", x = 3256, z = 3230, walkRadius = 8, direction = Direction.WEST)
        spawnNpc(npc = "npc.goblin_3028", x = 3221, z = 3271, walkRadius = 8, direction = Direction.WEST)
        // TODO: Add more goblin spawns
        spawnNpc(npc = "npc.drunken_man", x = 3230, z = 3241, walkRadius = 3, direction = Direction.EAST)
        spawnNpc(npc = "npc.man_3109", x = 3228, z = 3239, walkRadius = 3, direction = Direction.WEST)
        spawnNpc(npc = "npc.woman_3112", x = 3229, z = 3238, walkRadius = 3, direction = Direction.SOUTH)
        spawnNpc(npc = "npc.man_3014", x = 3231, z = 3236, walkRadius = 3, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat_3970", x = 3246, z = 3198, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3239, z = 3198, walkRadius = 5, direction = Direction.WEST)

        //Moosa
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3180, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3205, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3060, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3138, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3168, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.zombie_rat", x = 3218, z = 3178, walkRadius = 5, direction = Direction.WEST)
        spawnNpc(npc = "npc.giant_frog", x = 3218, z = 3128, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_frog", x = 3218, z = 3158, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_frog", x = 3218, z = 3148, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_frog", x = 3218, z = 3188, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.giant_frog", x = 3218, z = 3118, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3202, z = 3292, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3203, z = 3293, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3204, z = 3294, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3205, z = 3295, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3206, z = 3296, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3207, z = 3297, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3208, z = 3298, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.cow", x = 3209, z = 3299, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3117, z = 3276, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3118, z = 3277, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3119, z = 3278, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3120, z = 3279, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3121, z = 3280, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3122, z = 3281, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3123, z = 3282, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3124, z = 3283, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3125, z = 3284, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3126, z = 3285, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3127, z = 3286, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3128, z = 3287, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3129, z = 3288, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3130, z = 3289, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3131, z = 3290, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3132, z = 3291, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3133, z = 3292, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.chicken_1173", x = 3134, z = 3293, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard", x = 3117, z = 3245, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard", x = 3119, z = 3247, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard", x = 3121, z = 3249, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.guard", x = 3123, z = 3251, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3091, z = 3235, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3092, z = 3236, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3093, z = 3237, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3094, z = 3238, walkRadius = 5, direction = Direction.EAST)
        spawnNpc(npc = "npc.dark_wizard", x = 3095, z = 3239, walkRadius = 5, direction = Direction.EAST)






        // Item spawns
        spawnItem(item = "item.logs", amount = 1, x = 3205, z = 3224, height = 2)
        spawnItem(item = "item.logs", amount = 1, x = 3205, z = 3226, height = 2)
        spawnItem(item = "item.logs", amount = 1, x = 3208, z = 3225, height = 2)
        spawnItem(item = "item.logs", amount = 1, x = 3209, z = 3224, height = 2)
        spawnItem(item = "item.mind_rune", amount = 1, x = 3206, z = 3208)
        spawnItem(item = "item.bronze_arrow", amount = 1, x = 3205, z = 3227)
        spawnItem(item = "item.bronze_dagger", amount = 1, x = 3213, z = 3216, height = 1)
        spawnItem(item = "item.knife", amount = 1, x = 3205, z = 3212)
        spawnItem(item = "item.knife", amount = 1, x = 3224, z = 3202)
        spawnItem(item = "item.pot", amount = 1, x = 3209, z = 3214)
        spawnItem(item = "item.bowl", amount = 1, x = 3208, z = 3214)
        spawnItem(item = "item.jug", amount = 1, x = 3211, z = 3212)

        spawnObj(obj = "object.altar_409", x = 3222, z = 3215, rot = 6)
    }
}