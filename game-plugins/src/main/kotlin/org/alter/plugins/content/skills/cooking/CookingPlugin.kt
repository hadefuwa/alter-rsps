package org.alter.plugins.content.skills.cooking

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

class CookingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val cookingData = mapOf(
        "item.raw_shrimps" to CookingData("item.shrimps", 1, 30.0),
        "item.raw_sardine" to CookingData("item.sardine", 1, 40.0),
        "item.raw_herring" to CookingData("item.herring", 5, 50.0),
        "item.raw_trout" to CookingData("item.trout", 15, 50.0),
        "item.raw_salmon" to CookingData("item.salmon", 25, 90.0),
        "item.raw_tuna" to CookingData("item.tuna", 30, 100.0),
        "item.raw_lobster" to CookingData("item.lobster", 40, 120.0),
        "item.raw_swordfish" to CookingData("item.swordfish", 50, 140.0),
        "item.raw_bass" to CookingData("item.bass", 43, 130.0),
        "item.raw_shark" to CookingData("item.shark", 80, 210.0),
    )

    // Use only cooking ranges - fires are typically dynamic objects spawned by players
    private val rangeObjects = setOf(
        "object.cooking_range",
        "object.range",
    )

    init {
        // Item on cooking range
        cookingData.keys.forEach { rawFish ->
            rangeObjects.forEach { range ->
                onItemOnObj(obj = range, item = rawFish) {
                    player.queue { cookFood(this, player, rawFish, true) }
                }
            }
        }
    }

    private suspend fun cookFood(task: QueueTask, player: Player, rawItem: String, useRange: Boolean) {
        val data = cookingData[rawItem] ?: return
        val level = player.getSkills().getCurrentLevel(Skills.COOKING)
        val rawItemId = getRSCM(rawItem)

        if (level < data.level) {
            player.message("You need a Cooking level of ${data.level} to cook this.")
            return
        }

        if (!player.inventory.contains(rawItemId)) {
            return
        }

        player.lock()
        try {
            player.animate(if (useRange) Animation.COOKING_ON_RANGE else Animation.COOKING_ON_FIRE)
            task.wait(3)

            if (!player.inventory.contains(rawItemId)) {
                return
            }

            // Calculate burn chance (lower with higher level and range)
            val burnChance = calculateBurnChance(level, data.level, useRange)
            val burned = world.randomDouble() < burnChance

            if (burned) {
                player.inventory.remove(rawItemId, 1)
                player.message("You accidentally burn the ${getItemName(rawItem)}.")
            } else {
                player.inventory.remove(rawItemId, 1)
                player.inventory.add(getRSCM(data.cookedItem), 1)
                player.addXp(Skills.COOKING, data.experience)
                player.message("You cook the ${getItemName(rawItem)}.")
            }
        } finally {
            player.unlock()
        }
    }

    private fun calculateBurnChance(level: Int, requiredLevel: Int, useRange: Boolean): Double {
        val baseChance = 0.3
        val levelDiff = level - requiredLevel
        val rangeBonus = if (useRange) 0.1 else 0.0
        
        var chance = baseChance - (levelDiff * 0.02) - rangeBonus
        return chance.coerceIn(0.0, 1.0)
    }

    private fun getItemName(itemName: String): String {
        return itemName.replace("item.", "").replace("_", " ")
    }

    private data class CookingData(
        val cookedItem: String,
        val level: Int,
        val experience: Double
    )
}

