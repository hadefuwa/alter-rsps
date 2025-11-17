package org.alter.plugins.content.skills.herblore

import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class HerblorePlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    // Herb cleaning data: grimy herb -> clean herb
    private val herbCleaningData = mapOf(
        "item.grimy_guam_leaf" to HerbData("item.guam_leaf", 1, 2.5),
        "item.grimy_marrentill" to HerbData("item.marrentill", 5, 3.8),
        "item.grimy_tarromin" to HerbData("item.tarromin", 11, 5.0),
        "item.grimy_harralander" to HerbData("item.harralander", 20, 6.3),
        "item.grimy_ranarr_weed" to HerbData("item.ranarr_weed", 25, 7.5),
        "item.grimy_irit_leaf" to HerbData("item.irit_leaf", 40, 8.8),
        "item.grimy_avantoe" to HerbData("item.avantoe", 48, 10.0),
        "item.grimy_kwuarm" to HerbData("item.kwuarm", 54, 11.3),
        "item.grimy_cadantine" to HerbData("item.cadantine", 65, 12.5),
        "item.grimy_dwarf_weed" to HerbData("item.dwarf_weed", 70, 13.1),
        "item.grimy_torstol" to HerbData("item.torstol", 75, 15.0),
    )

    // Unfinished potion data: clean herb -> unfinished potion
    private val unfinishedPotionData = mapOf(
        "item.guam_leaf" to "item.guam_potion_unf",
        "item.marrentill" to "item.marrentill_potion_unf",
        "item.tarromin" to "item.tarromin_potion_unf",
        "item.harralander" to "item.harralander_potion_unf",
        "item.ranarr_weed" to "item.ranarr_potion_unf",
        "item.irit_leaf" to "item.irit_potion_unf",
        "item.avantoe" to "item.avantoe_potion_unf",
        "item.kwuarm" to "item.kwuarm_potion_unf",
        "item.cadantine" to "item.cadantine_potion_unf",
        "item.dwarf_weed" to "item.dwarf_weed_potion_unf",
        "item.torstol" to "item.torstol_potion_unf",
    )

    // Potion making data: (herb, secondary) -> (unfinished potion, finished potion, level, experience)
    private val potionData = mapOf(
        Pair("item.guam_leaf", "item.eye_of_newt") to PotionData(
            "item.guam_potion_unf",
            "item.attack_potion3",
            1,
            25.0
        ),
        Pair("item.marrentill", "item.eye_of_newt") to PotionData(
            "item.marrentill_potion_unf",
            "item.antipoison3",
            5,
            37.5
        ),
        Pair("item.tarromin", "item.limpwurt_root") to PotionData(
            "item.tarromin_potion_unf",
            "item.strength_potion3",
            12,
            50.0
        ),
        Pair("item.harralander", "item.chocolate_dust") to PotionData(
            "item.harralander_potion_unf",
            "item.restore_potion3",
            22,
            62.5
        ),
        Pair("item.harralander", "item.red_spiders_eggs") to PotionData(
            "item.harralander_potion_unf",
            "item.energy_potion3",
            26,
            67.5
        ),
        Pair("item.ranarr_weed", "item.snape_grass") to PotionData(
            "item.ranarr_potion_unf",
            "item.prayer_potion3",
            38,
            87.5
        ),
        Pair("item.irit_leaf", "item.eye_of_newt") to PotionData(
            "item.irit_potion_unf",
            "item.super_attack3",
            45,
            100.0
        ),
        Pair("item.avantoe", "item.mort_myre_fungus") to PotionData(
            "item.avantoe_potion_unf",
            "item.super_energy3",
            50,
            117.5
        ),
        Pair("item.kwuarm", "item.limpwurt_root") to PotionData(
            "item.kwuarm_potion_unf",
            "item.super_strength3",
            55,
            125.0
        ),
        Pair("item.cadantine", "item.white_berries") to PotionData(
            "item.cadantine_potion_unf",
            "item.super_defence3",
            66,
            150.0
        ),
        Pair("item.dwarf_weed", "item.wine_of_zamorak") to PotionData(
            "item.dwarf_weed_potion_unf",
            "item.ranging_potion3",
            72,
            162.5
        ),
        Pair("item.torstol", "item.jangerberries") to PotionData(
            "item.torstol_potion_unf",
            "item.zamorak_brew3",
            78,
            175.0
        ),
    )

    private val vialOfWaterName = "item.vial_of_water"

    init {
        // Clean grimy herbs (use grimy herb on itself or just click)
        herbCleaningData.keys.forEach { grimyHerb ->
            onItemOption(item = grimyHerb, option = "clean") {
                player.queue { cleanHerb(this, player, grimyHerb) }
            }
        }

        // Make unfinished potions (herb on vial of water)
        // Note: bindItemOnItem normalizes order, so we only need to bind once per pair
        unfinishedPotionData.keys.forEach { cleanHerb ->
            onItemOnItem(item1 = cleanHerb, item2 = vialOfWaterName) {
                player.queue { makeUnfinishedPotion(this, player, cleanHerb) }
            }
        }

        // Make finished potions (secondary ingredient on unfinished potion)
        // Note: bindItemOnItem normalizes order, so we only need to bind once per pair
        potionData.forEach { (ingredients, data) ->
            val (herb, secondary) = ingredients
            val unfinishedPotion = data.unfinishedPotion
            
            onItemOnItem(item1 = secondary, item2 = unfinishedPotion) {
                player.queue { makeFinishedPotion(this, player, secondary, unfinishedPotion, data) }
            }
        }
    }

    private suspend fun cleanHerb(task: QueueTask, player: Player, grimyHerb: String) {
        val data = herbCleaningData[grimyHerb] ?: return
        val level = player.getSkills().getCurrentLevel(Skills.HERBLORE)
        val grimyHerbId = getRSCM(grimyHerb)

        if (level < data.level) {
            player.message("You need a Herblore level of ${data.level} to clean this herb.")
            return
        }

        if (!player.inventory.contains(grimyHerbId)) {
            return
        }

        player.lock()
        try {
            player.animate(Animation.HERBLORE_CLEAN_HERB)
            task.wait(2)

            if (!player.inventory.contains(grimyHerbId)) {
                return
            }

            player.inventory.remove(grimyHerbId, 1)
            player.inventory.add(getRSCM(data.cleanHerb), 1)
            player.addXp(Skills.HERBLORE, data.experience)
            player.message("You clean the herb.")
        } finally {
            player.unlock()
        }
    }

    private suspend fun makeUnfinishedPotion(task: QueueTask, player: Player, cleanHerb: String) {
        val level = player.getSkills().getCurrentLevel(Skills.HERBLORE)
        val cleanHerbId = getRSCM(cleanHerb)
        val vialOfWaterId = getRSCM(vialOfWaterName)

        if (!player.inventory.contains(cleanHerbId)) {
            return
        }

        if (!player.inventory.contains(vialOfWaterId)) {
            player.message("You need a vial of water to make a potion.")
            return
        }

        // Find the herb data
        val herbData = herbCleaningData.values.firstOrNull { it.cleanHerb == cleanHerb }
        if (herbData == null) {
            return
        }

        if (level < herbData.level) {
            player.message("You need a Herblore level of ${herbData.level} to make this potion.")
            return
        }

        // Find unfinished potion for this herb
        val unfinishedPotionName = unfinishedPotionData[cleanHerb]
        if (unfinishedPotionName == null) {
            player.message("You don't know how to make a potion with this herb.")
            return
        }

        player.lock()
        try {
            player.animate(Animation.HERBLORE_POTION_MAKING)
            task.wait(2)

            if (!player.inventory.contains(cleanHerbId) || !player.inventory.contains(vialOfWaterId)) {
                return
            }

            player.inventory.remove(cleanHerbId, 1)
            player.inventory.remove(vialOfWaterId, 1)
            player.inventory.add(getRSCM(unfinishedPotionName), 1)
            player.addXp(Skills.HERBLORE, 0.0) // No XP for unfinished potions
            player.message("You put the ${getHerbName(cleanHerb)} into the vial of water.")
        } finally {
            player.unlock()
        }
    }

    private suspend fun makeFinishedPotion(
        task: QueueTask,
        player: Player,
        secondary: String,
        unfinishedPotion: String,
        data: PotionData
    ) {
        val level = player.getSkills().getCurrentLevel(Skills.HERBLORE)
        val secondaryId = getRSCM(secondary)
        val unfinishedPotionId = getRSCM(unfinishedPotion)

        if (level < data.level) {
            player.message("You need a Herblore level of ${data.level} to make this potion.")
            return
        }

        if (!player.inventory.contains(secondaryId)) {
            return
        }

        if (!player.inventory.contains(unfinishedPotionId)) {
            return
        }

        player.lock()
        try {
            player.animate(Animation.HERBLORE_POTION_MAKING)
            task.wait(2)

            if (!player.inventory.contains(secondaryId) || !player.inventory.contains(unfinishedPotionId)) {
                return
            }

            player.inventory.remove(secondaryId, 1)
            player.inventory.remove(unfinishedPotionId, 1)
            player.inventory.add(getRSCM(data.finishedPotion), 1)
            player.addXp(Skills.HERBLORE, data.experience)
            player.message("You add the ${getSecondaryName(secondary)} to the potion.")
        } finally {
            player.unlock()
        }
    }

    private fun getHerbName(herbName: String): String {
        return when (herbName) {
            "item.guam_leaf" -> "guam leaf"
            "item.marrentill" -> "marrentill"
            "item.tarromin" -> "tarromin"
            "item.harralander" -> "harralander"
            "item.ranarr_weed" -> "ranarr weed"
            "item.irit_leaf" -> "irit leaf"
            "item.avantoe" -> "avantoe"
            "item.kwuarm" -> "kwuarm"
            "item.cadantine" -> "cadantine"
            "item.dwarf_weed" -> "dwarf weed"
            "item.torstol" -> "torstol"
            else -> "herb"
        }
    }

    private fun getSecondaryName(secondaryName: String): String {
        return when (secondaryName) {
            "item.eye_of_newt" -> "eye of newt"
            "item.limpwurt_root" -> "limpwurt root"
            "item.chocolate_dust" -> "chocolate dust"
            "item.red_spiders_eggs" -> "red spiders' eggs"
            "item.snape_grass" -> "snape grass"
            "item.white_berries" -> "white berries"
            "item.mort_myre_fungus" -> "mort myre fungus"
            "item.wine_of_zamorak" -> "wine of zamorak"
            "item.jangerberries" -> "jangerberries"
            else -> "ingredient"
        }
    }

    private data class HerbData(
        val cleanHerb: String,
        val level: Int,
        val experience: Double
    )

    private data class PotionData(
        val unfinishedPotion: String,
        val finishedPotion: String,
        val level: Int,
        val experience: Double
    )
}

