package org.alter.plugins.content.skills.smithing

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

class SmithingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    private val furnaceObjects = setOf(
        "object.furnace",
    )

    private val anvilObjects = setOf(
        "object.anvil",
        "object.anvil_2097",
    )

    init {
        // Smelt ores at furnace
        furnaceObjects.forEach { furnace ->
            onItemOnObj(obj = furnace, item = "item.copper_ore") {
                player.queue { smeltOre(this, player, "item.copper_ore", "item.tin_ore", "item.bronze_bar", 1, 6.2) }
            }
            onItemOnObj(obj = furnace, item = "item.tin_ore") {
                player.queue { smeltOre(this, player, "item.tin_ore", "item.copper_ore", "item.bronze_bar", 1, 6.2) }
            }
            onItemOnObj(obj = furnace, item = "item.iron_ore") {
                player.queue { smeltOre(this, player, "item.iron_ore", null, "item.iron_bar", 15, 12.5) }
            }
            onItemOnObj(obj = furnace, item = "item.silver_ore") {
                player.queue { smeltOre(this, player, "item.silver_ore", null, "item.silver_bar", 20, 13.7) }
            }
            onItemOnObj(obj = furnace, item = "item.gold_ore") {
                player.queue { smeltOre(this, player, "item.gold_ore", null, "item.gold_bar", 40, 22.5) }
            }
            onItemOnObj(obj = furnace, item = "item.coal") {
                // Coal is used with other ores, not smelted alone
                player.message("You need to combine coal with other ores to smelt them.")
            }
            onItemOnObj(obj = furnace, item = "item.mithril_ore") {
                player.queue { smeltOre(this, player, "item.mithril_ore", "item.coal", "item.mithril_bar", 50, 30.0, coalNeeded = 4) }
            }
            onItemOnObj(obj = furnace, item = "item.adamantite_ore") {
                player.queue { smeltOre(this, player, "item.adamantite_ore", "item.coal", "item.adamantite_bar", 70, 37.5, coalNeeded = 6) }
            }
            onItemOnObj(obj = furnace, item = "item.runite_ore") {
                player.queue { smeltOre(this, player, "item.runite_ore", "item.coal", "item.runite_bar", 85, 50.0, coalNeeded = 8) }
            }
        }
    }

    private suspend fun smeltOre(
        task: QueueTask,
        player: Player,
        primaryOre: String,
        secondaryOre: String?,
        barName: String,
        level: Int,
        experience: Double,
        coalNeeded: Int = 0
    ) {
        val smithingLevel = player.getSkills().getCurrentLevel(Skills.SMITHING)
        val primaryOreId = getRSCM(primaryOre)
        val secondaryOreId = secondaryOre?.let { getRSCM(it) }
        val barId = getRSCM(barName)
        val coalId = getRSCM("item.coal")

        if (smithingLevel < level) {
            player.message("You need a Smithing level of $level to smelt this ore.")
            return
        }

        if (!player.inventory.contains(primaryOreId)) {
            return
        }

        if (secondaryOreId != null && !player.inventory.contains(secondaryOreId)) {
            val oreName = if (secondaryOre == "item.coal") "coal" else "secondary ore"
            player.message("You need $oreName to smelt this.")
            return
        }

        if (coalNeeded > 0) {
            val coalCount = player.inventory.getItemCount(coalId)
            if (coalCount < coalNeeded) {
                player.message("You need $coalNeeded coal to smelt this ore.")
                return
            }
        }

        if (player.inventory.isFull && !player.inventory.contains(barId)) {
            player.message("You don't have enough inventory space.")
            return
        }

        player.lock()
        try {
            player.animate(Animation.SMITHING_SMELT)
            task.wait(3)

            if (!player.inventory.contains(primaryOreId)) {
                return
            }

            // Remove ores
            player.inventory.remove(primaryOreId, 1)
            if (secondaryOreId != null) {
                player.inventory.remove(secondaryOreId, 1)
            }
            if (coalNeeded > 0) {
                player.inventory.remove(coalId, coalNeeded)
            }

            // Add bar
            player.inventory.add(barId, 1)
            player.addXp(Skills.SMITHING, experience)
            player.message("You smelt the ore into a bar.")
        } finally {
            player.unlock()
        }
    }
}

