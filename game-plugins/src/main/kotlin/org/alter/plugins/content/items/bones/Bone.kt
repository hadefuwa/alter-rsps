package org.alter.plugins.content.items.bones

/**
 * Bone types and their prayer experience values.
 * Based on OSRS bone burying XP values.
 */
enum class Bone(
    val item: String,
    val xp: Double,
) {
    REGULAR_BONES(item = "item.bones", xp = 4.5),
    BAT_BONES(item = "item.bat_bones", xp = 5.3),
    BIG_BONES(item = "item.big_bones", xp = 15.0),
    BABY_DRAGON_BONES(item = "item.babydragon_bones", xp = 30.0),
    DRAGON_BONES(item = "item.dragon_bones", xp = 72.0),
    WYVERN_BONES(item = "item.wyvern_bones", xp = 50.0),
    LAVA_DRAGON_BONES(item = "item.lava_dragon_bones", xp = 85.0),
    SUPERIOR_DRAGON_BONES(item = "item.superior_dragon_bones", xp = 150.0),
    DAGANNOTH_BONES(item = "item.dagannoth_bones", xp = 125.0),
    OURG_BONES(item = "item.ourg_bones", xp = 140.0),
    FAYRG_BONES(item = "item.fayrg_bones", xp = 84.0),
    RAURG_BONES(item = "item.raurg_bones", xp = 96.0),
    HYDRA_BONES(item = "item.hydra_bones", xp = 110.0),
    WOLF_BONES(item = "item.wolf_bones", xp = 4.5),
    MONKEY_BONES(item = "item.monkey_bones", xp = 5.0),
    JOGRE_BONES(item = "item.jogre_bones", xp = 15.0),
    ZOGRE_BONES(item = "item.zogre_bones", xp = 22.5),
    SHAIKAHAN_BONES(item = "item.shaikahan_bones", xp = 25.0),
    WYRM_BONES(item = "item.wyrm_bones", xp = 50.0),
    DRAKE_BONES(item = "item.drake_bones", xp = 80.0),
    ;

    companion object {
        val values = enumValues<Bone>()
    }
}

