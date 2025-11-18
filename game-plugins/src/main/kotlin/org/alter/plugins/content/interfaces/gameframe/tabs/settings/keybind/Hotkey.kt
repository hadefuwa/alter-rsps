package org.alter.plugins.content.interfaces.keybind

/**
 * @author Tom <rspsmods@gmail.com>
 */
enum class Hotkey(val id: Int, val child: Int, val varbit: Int, val defaultValue: Int) {
    COMBAT(id = 0, child = 9, varbit = 4675, defaultValue = 1),     // F1
    SKILLS(id = 1, child = 16, varbit = 4676, defaultValue = 0),    // None
    QUESTS(id = 2, child = 23, varbit = 4677, defaultValue = 0),    // None
    INVENTORY(id = 3, child = 30, varbit = 4678, defaultValue = 2), // F2
    EQUIPMENT(id = 4, child = 37, varbit = 4679, defaultValue = 0), // None
    PRAYERS(id = 5, child = 44, varbit = 4680, defaultValue = 3),   // F3
    MAGIC(id = 6, child = 51, varbit = 4682, defaultValue = 4),     // F4
    SOCIAL(id = 7, child = 58, varbit = 4684, defaultValue = 0),    // None
    ACCOUNT_MANAGEMENT(id = 8, child = 65, varbit = 6517, defaultValue = 0),  // None
    LOG_OUT(id = 9, child = 72, varbit = 4689, defaultValue = 0),   // None
    SETTINGS(id = 10, child = 79, varbit = 4686, defaultValue = 0), // None
    EMOTES(id = 11, child = 86, varbit = 4687, defaultValue = 0),   // None
    CLAN_CHAT(id = 12, child = 93, varbit = 4683, defaultValue = 0), // None
    MUSIC(id = 13, child = 100, varbit = 4688, defaultValue = 0),   // None
    ;

    companion object {
        val values = enumValues<Hotkey>()
    }
}
