package org.alter.plugins.content.skills.slayer

enum class SlayerMaster(val id: Int, val requiredCombatLevel: Int) {
    KRYSTILLIA(id = 7663, requiredCombatLevel = 0),
    NIEVE(id = 6797, requiredCombatLevel = 85),
    KONAR(id = 8623, requiredCombatLevel = 75);

    companion object {
        val values = enumValues<SlayerMaster>()
        fun get(id: Int): SlayerMaster? = values.find { it.id == id }
    }
}
