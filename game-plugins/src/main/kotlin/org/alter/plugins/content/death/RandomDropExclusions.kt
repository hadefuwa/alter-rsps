package org.alter.plugins.content.death

/**
 * ========================================================================
 * EXCLUDED RANDOM DROP ITEMS LIST
 * ========================================================================
 * 
 * This is where you add items that should NOT be part of random item drops.
 * 
 * The random item drop system applies to ALL NPCs based on their combat level.
 * When an NPC dies, there's a chance (scaled by combat level) that it will
 * drop a random item from the entire game item table. This exclusion list
 * prevents specific items from appearing in those random drops.
 * 
 * WHY USE THIS LIST:
 * - Some items are too rare/valuable to drop randomly (e.g., spirit shields)
 * - Some items should only come from specific NPC drop tables
 * - Prevents devaluing rare items by making them too common
 * 
 * HOW IT WORKS:
 * - Items in this list are filtered out when building the valid item pool
 * - The exclusion applies globally to ALL NPCs that use random drops
 * - Items can still drop from their configured drop tables (always/main/tertiary)
 * 
 * HOW TO ADD ITEMS:
 * - Add the item's RSCM name (e.g., "item.elysian_spirit_shield")
 * - Use the exact RSCM name format: "item.item_name"
 * - Items are automatically converted to item IDs at plugin initialization
 * 
 * EXAMPLES:
 * - Spirit shields (already added)
 * - Rare boss-specific drops
 * - Quest items that shouldn't drop randomly
 * - Any item you want to keep exclusive to specific drop tables
 * 
 * ========================================================================
 */
object RandomDropExclusions {
    /**
     * Set of item RSCM names that should NOT drop as part of random item drops.
     * Add items here to exclude them from all random drop systems.
     */
    val EXCLUDED_RANDOM_DROP_ITEMS = setOf(
        "item.elysian_spirit_shield",
        "item.spectral_spirit_shield",
        "item.arcane_spirit_shield",
        "item.bandos_godsword_20782"
        // Add more excluded items here using the format: "item.item_name"
    )
}

