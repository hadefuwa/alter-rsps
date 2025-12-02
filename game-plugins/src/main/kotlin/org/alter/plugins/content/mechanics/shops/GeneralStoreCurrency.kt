package org.alter.plugins.content.mechanics.shops

import dev.openrune.cache.CacheManager.getItem
import org.alter.game.model.World
import org.alter.game.model.item.Item
import org.alter.rscm.RSCM.getRSCM

/**
 * Currency for general stores that buy items using a tiered pricing strategy
 * based on item properties rather than GE values.
 * 
 * Pricing strategy:
 * - Uses base item cost from cache as starting point
 * - Applies tiered percentage based on value ranges
 * - Adjusts for tradeable status and item characteristics
 * 
 * @author Auto-generated
 */
class GeneralStoreCurrency : ItemCurrency(getRSCM("item.coins_995"), singularCurrency = "coin", pluralCurrency = "coins") {
    
    override fun getBuyPrice(
        world: World,
        item: Int,
    ): Int {
        // Convert to unnoted item
        val unnoted = Item(item).toUnnoted().id
        val itemDef = getItem(unnoted)
        
        // Special handling: All gilded items sell for 1,000,000 coins
        if (itemDef.name.lowercase().contains("gilded")) {
            return 1_000_000
        }
        
        // Base cost from cache
        var baseCost = itemDef.cost
        
        // Special handling for tradeable items with very low base cost (likely valuable items)
        // Many rare/unique items have base cost of 1 but are actually very valuable
        if (itemDef.isTradeable && baseCost <= 10) {
            // Estimate value based on item characteristics for tradeable items with low base cost
            baseCost = estimateValueForLowCostTradeable(itemDef)
        }
        
        // If item has no base cost and couldn't be estimated, it can't be sold
        if (baseCost == 0) {
            return 0
        }
        
        // Calculate buy price using tiered percentage system
        val buyPrice = calculateBuyPrice(itemDef, baseCost)
        
        // Ensure minimum value of 1 for items that can be sold
        return Math.max(1, buyPrice)
    }
    
    /**
     * Estimates a reasonable value for tradeable items that have a very low base cost.
     * This handles cases where valuable items have base cost of 1 in the cache.
     */
    private fun estimateValueForLowCostTradeable(itemDef: dev.openrune.cache.filestore.definition.data.ItemType): Int {
        // Start with a base minimum for tradeable items
        var estimatedValue = 1000
        
        // Adjust based on item characteristics
        // Members items are typically more valuable
        if (itemDef.members) {
            estimatedValue *= 2
        }
        
        // Items with equipment slots (weapons/armor) are typically valuable
        if (itemDef.equipSlot >= 0) {
            estimatedValue *= 3
        }
        
        // Items with bonuses (equipment with stats) are valuable
        if (itemDef.bonuses != null && itemDef.bonuses.isNotEmpty()) {
            estimatedValue *= 2
        }
        
        // Items with weight > 0 are typically equipment or valuable items
        if (itemDef.weight > 0.0) {
            estimatedValue = (estimatedValue * (1 + itemDef.weight)).toInt()
        }
        
        // Cap the estimated value to prevent abuse (max 10M estimate)
        return estimatedValue.coerceIn(1000, 10_000_000)
    }
    
    /**
     * Calculates the buy price using a tiered percentage system.
     * Higher value items get a lower percentage to prevent abuse.
     */
    private fun calculateBuyPrice(itemDef: dev.openrune.cache.filestore.definition.data.ItemType, baseCost: Int): Int {
        // Tiered percentage based on item value
        val percentage = when {
            baseCost <= 100 -> 0.85  // Very cheap items: 85% (makes low-value items worthwhile)
            baseCost <= 1_000 -> 0.75  // Cheap items: 75%
            baseCost <= 10_000 -> 0.70  // Mid-value items: 70%
            baseCost <= 100_000 -> 0.65  // High-value items: 65%
            baseCost <= 1_000_000 -> 0.60  // Very high-value items: 60%
            else -> 0.55  // Extremely high-value items: 55%
        }
        
        // Adjust percentage based on item characteristics
        var adjustedPercentage = percentage
        
        // Tradeable items get a small bonus (they're more liquid)
        if (itemDef.isTradeable) {
            adjustedPercentage += 0.05
        }
        
        // Stackable items get a small bonus (easier to handle)
        if (itemDef.stackable) {
            adjustedPercentage += 0.02
        }
        
        // Cap the percentage between 50% and 90%
        adjustedPercentage = adjustedPercentage.coerceIn(0.50, 0.90)
        
        // Calculate final price
        return (baseCost * adjustedPercentage).toInt()
    }
}
