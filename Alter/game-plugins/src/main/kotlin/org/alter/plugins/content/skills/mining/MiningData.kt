package org.alter.plugins.content.skills.mining

/**
 * Immutable configuration representing a mineable rock loaded from JSON.
 */
data class MiningEntry(
    val objects: List<String>,
    val emptyObject: String,
    val respawnTicks: Int = 10,
    val level: Int,
    val experience: Double,
    val ore: String,
    val oreAmount: Int = 1,
) {
    @Transient
    var objectIds: IntArray = intArrayOf()

    @Transient
    var emptyObjectId: Int = -1

    @Transient
    var oreId: Int = -1

    init {
        require(objects.isNotEmpty()) { "Mining entry must define at least one object id." }
        require(emptyObject.isNotBlank()) { "Mining entry must define the empty rock object id." }
        require(respawnTicks >= 1) { "Mining respawn ticks must be at least 1." }
        require(level >= 1) { "Mining level requirement must be >= 1." }
        require(experience >= 0.0) { "Mining experience cannot be negative." }
        require(ore.isNotBlank()) { "Mining entry must define the ore item." }
        require(oreAmount >= 1) { "Ore amount must be at least 1." }
    }
}

