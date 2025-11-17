package org.alter.plugins.content.skills.fishing

/**
 * Immutable configuration representing a fishable spot loaded from JSON.
 */
data class FishingEntry(
    val objects: List<String>,
    val level: Int,
    val experience: Double,
    val fish: List<FishLoot>,
    val tool: String,
    val bait: String? = null,
    val animation: Int,
) {
    @Transient
    var objectIds: IntArray = intArrayOf()

    @Transient
    var toolId: Int = -1

    @Transient
    var baitId: Int? = null

    init {
        require(objects.isNotEmpty()) { "Fishing entry must define at least one object id." }
        require(level >= 1) { "Fishing level requirement must be >= 1." }
        require(experience >= 0.0) { "Fishing experience cannot be negative." }
        require(fish.isNotEmpty()) { "Fishing entry must define at least one fish." }
        require(tool.isNotBlank()) { "Fishing entry must define the tool item." }
    }
}

data class FishLoot(
    val item: String,
    val weight: Double = 1.0,
) {
    @Transient
    var itemId: Int = -1

    init {
        require(item.isNotBlank()) { "Fish item id cannot be blank." }
        require(weight > 0.0) { "Fish weight must be greater than 0." }
    }
}

