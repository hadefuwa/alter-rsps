package org.alter.plugins.content.skills.fletching

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.OTHER_ITEM_SLOT_ATTR
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

class FletchingPlugin(
    r: PluginRepository,
    world: World,
    server: Server,
) : KotlinPlugin(r, world, server) {

    // Data class for bolt fletching recipes
    private data class BoltRecipe(
        val unfinishedBoltName: String,
        val finishedBoltName: String,
        val level: Int,
        val experience: Double
    )

    // Data class for dart fletching recipes
    private data class DartRecipe(
        val dartTipName: String,
        val finishedDartName: String,
        val level: Int,
        val experience: Double
    )

    // Bolt recipes: unfinished bolt -> finished bolt
    private val boltRecipes = listOf(
        BoltRecipe("item.bronze_bolts_unf", "item.bronze_bolts", 9, 0.5),
        BoltRecipe("item.blurite_bolts_unf", "item.blurite_bolts", 24, 1.0),
        BoltRecipe("item.iron_bolts_unf", "item.iron_bolts", 39, 1.5),
        BoltRecipe("item.steel_bolts_unf", "item.steel_bolts", 46, 3.75),
        BoltRecipe("item.mithril_bolts_unf", "item.mithril_bolts", 54, 5.0),
        BoltRecipe("item.adamant_boltsunf", "item.adamant_bolts", 61, 7.0),
        BoltRecipe("item.runite_bolts_unf", "item.runite_bolts", 69, 10.0),
        BoltRecipe("item.silver_bolts_unf", "item.silver_bolts", 20, 1.0),
        BoltRecipe("item.dragon_bolts_unf", "item.dragon_bolts", 84, 12.0),
    )

    // Dart recipes: dart tip -> finished dart
    private val dartRecipes = listOf(
        DartRecipe("item.bronze_dart_tip", "item.bronze_dart", 1, 1.8),
        DartRecipe("item.iron_dart_tip", "item.iron_dart", 22, 3.8),
        DartRecipe("item.steel_dart_tip", "item.steel_dart", 37, 7.5),
        DartRecipe("item.mithril_dart_tip", "item.mithril_dart", 52, 11.2),
        DartRecipe("item.adamant_dart_tip", "item.adamant_dart", 67, 15.0),
        DartRecipe("item.rune_dart_tip", "item.rune_dart", 81, 18.8),
        DartRecipe("item.dragon_dart_tip", "item.dragon_dart", 95, 25.0),
        DartRecipe("item.amethyst_dart_tip", "item.amethyst_dart", 90, 21.0),
    )

    private val FEATHER = getRSCM("item.feather")

    init {
        // Register feather + unfinished bolts -> finished bolts
        boltRecipes.forEach { recipe ->
            val unfinishedBoltId = getRSCM(recipe.unfinishedBoltName)
            val finishedBoltId = getRSCM(recipe.finishedBoltName)
            
            // Register both ways (feather on bolt, bolt on feather)
            onItemOnItem(item1 = "item.feather", item2 = recipe.unfinishedBoltName) {
                val sourceSlot = player.getInteractingItemSlot()
                val targetSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: return@onItemOnItem
                
                val source = player.inventory[sourceSlot] ?: return@onItemOnItem
                val target = player.inventory[targetSlot] ?: return@onItemOnItem
                
                // Verify we have both feather and unfinished bolt
                val hasFeather = source.id == FEATHER || target.id == FEATHER
                val hasUnfinishedBolt = source.id == unfinishedBoltId || target.id == unfinishedBoltId
                
                if (!hasFeather || !hasUnfinishedBolt) {
                    return@onItemOnItem
                }
                
                // Check fletching level
                val fletchingLevel = player.getSkills().getCurrentLevel(Skills.FLETCHING)
                if (fletchingLevel < recipe.level) {
                    player.message("You need a Fletching level of ${recipe.level} to fletch these bolts.")
                    return@onItemOnItem
                }
                
                // Get the quantities
                val featherCount = if (source.id == FEATHER) source.amount else target.amount
                val boltCount = if (source.id == unfinishedBoltId) source.amount else target.amount
                val quantityToMake = minOf(featherCount, boltCount)
                
                if (quantityToMake <= 0) {
                    return@onItemOnItem
                }
                
                // Check inventory space
                val freeSlots = player.inventory.freeSlotCount
                val hasExistingBolts = player.inventory.contains(finishedBoltId)
                if (!hasExistingBolts && freeSlots == 0) {
                    player.message("You don't have enough inventory space.")
                    return@onItemOnItem
                }
                
                // Remove items and add finished bolts
                player.inventory.remove(FEATHER, quantityToMake)
                player.inventory.remove(unfinishedBoltId, quantityToMake)
                player.inventory.add(finishedBoltId, quantityToMake)
                
                // Add experience
                player.addXp(Skills.FLETCHING, recipe.experience * quantityToMake)
                
                // Show message
                val boltName = getItem(finishedBoltId).name
                if (quantityToMake == 1) {
                    player.message("You attach a feather to the bolt and create a $boltName.")
                } else {
                    player.message("You attach feathers to the bolts and create $quantityToMake $boltName.")
                }
            }
        }

        // Register feather + dart tips -> finished darts
        dartRecipes.forEach { recipe ->
            val dartTipId = getRSCM(recipe.dartTipName)
            val finishedDartId = getRSCM(recipe.finishedDartName)
            
            // Register both ways (feather on dart tip, dart tip on feather)
            onItemOnItem(item1 = "item.feather", item2 = recipe.dartTipName) {
                val sourceSlot = player.getInteractingItemSlot()
                val targetSlot = player.attr[OTHER_ITEM_SLOT_ATTR] ?: return@onItemOnItem
                
                val source = player.inventory[sourceSlot] ?: return@onItemOnItem
                val target = player.inventory[targetSlot] ?: return@onItemOnItem
                
                // Verify we have both feather and dart tip
                val hasFeather = source.id == FEATHER || target.id == FEATHER
                val hasDartTip = source.id == dartTipId || target.id == dartTipId
                
                if (!hasFeather || !hasDartTip) {
                    return@onItemOnItem
                }
                
                // Check fletching level
                val fletchingLevel = player.getSkills().getCurrentLevel(Skills.FLETCHING)
                if (fletchingLevel < recipe.level) {
                    player.message("You need a Fletching level of ${recipe.level} to fletch these darts.")
                    return@onItemOnItem
                }
                
                // Get the quantities
                val featherCount = if (source.id == FEATHER) source.amount else target.amount
                val dartTipCount = if (source.id == dartTipId) source.amount else target.amount
                val quantityToMake = minOf(featherCount, dartTipCount)
                
                if (quantityToMake <= 0) {
                    return@onItemOnItem
                }
                
                // Check inventory space
                val freeSlots = player.inventory.freeSlotCount
                val hasExistingDarts = player.inventory.contains(finishedDartId)
                if (!hasExistingDarts && freeSlots == 0) {
                    player.message("You don't have enough inventory space.")
                    return@onItemOnItem
                }
                
                // Remove items and add finished darts
                player.inventory.remove(FEATHER, quantityToMake)
                player.inventory.remove(dartTipId, quantityToMake)
                player.inventory.add(finishedDartId, quantityToMake)
                
                // Add experience
                player.addXp(Skills.FLETCHING, recipe.experience * quantityToMake)
                
                // Show message
                val dartName = getItem(finishedDartId).name
                if (quantityToMake == 1) {
                    player.message("You attach a feather to the dart tip and create a $dartName.")
                } else {
                    player.message("You attach feathers to the dart tips and create $quantityToMake $dartName.")
                }
            }
        }
    }
}

