package org.alter.plugins.content.skills.woodcutting

/**
 * Immutable configuration representing a choppable tree loaded from JSON.
 */
data class WoodcuttingEntry(
    val objects: List<String>,
    val stumpObject: String,
    val respawnTicks: Int = 10,
    val level: Int,
    val experience: Double,
    val logs: String,
    val logsAmount: Int = 1,
) {
    @Transient
    var objectIds: IntArray = intArrayOf()

    @Transient
    var stumpObjectId: Int = -1

    @Transient
    var logsId: Int = -1

    init {
        require(objects.isNotEmpty()) { "Woodcutting entry must define at least one object id." }
        require(stumpObject.isNotBlank()) { "Woodcutting entry must define the stump object id." }
        require(respawnTicks >= 1) { "Tree respawn ticks must be at least 1." }
        require(level >= 1) { "Woodcutting level requirement must be >= 1." }
        require(experience >= 0.0) { "Woodcutting experience cannot be negative." }
        require(logs.isNotBlank()) { "Woodcutting entry must define the logs item." }
        require(logsAmount >= 1) { "Logs amount must be at least 1." }
    }
}

