package org.alter.plugins.content.mechanics.shops

/**
 * Shared money constants for shop pricing.
 * 
 * These constants make it easier to read and maintain shop prices across all shop plugins.
 * 
 * Usage examples:
 * - `10 * M` = 10 million
 * - `100 * M` = 100 million  
 * - `1 * B` = 1 billion
 * - `2.5 * B` = 2.5 billion (if using Double)
 */
object ShopConstants {
    // Base money units
    const val M = 1_000_000  // 1 million
    const val B = 1_000_000_000  // 1 billion
    
    // Common price constants (for convenience)
    const val _1M = 1_000_000
    const val _10M = 10_000_000
    const val _100M = 100_000_000
    const val _200M = 200_000_000
    const val _1B = 1_000_000_000
    const val _10B = 10_000_000_000
    const val _100B = 100_000_000_000
    const val _1000B = 1_000_000_000_000
    const val _10000B = 10_000_000_000_000
}

