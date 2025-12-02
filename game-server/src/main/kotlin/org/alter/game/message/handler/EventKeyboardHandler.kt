package org.alter.game.message.handler

import dev.openrune.cache.CacheManager
import dev.openrune.cache.CacheManager.getVarbit
import net.rsprot.protocol.game.incoming.events.EventKeyboard
import net.rsprot.protocol.game.outgoing.misc.player.RunClientScript
import org.alter.game.message.MessageHandler
import org.alter.game.model.entity.Client

/**
 * @author Tom <rspsmods@gmail.com>
 */
class EventKeyboardHandler : MessageHandler<EventKeyboard> {
    override fun consume(
        client: Client,
        message: EventKeyboard,
    ) {
        // F1-F10 key codes: 112-121
        // F1 = 112, F2 = 113, F3 = 114, F4 = 115, F5 = 116, F6 = 117, F7 = 118, F8 = 119, F9 = 120, F10 = 121
        // Try to get the key code - EventKeyboard might have different property names
        val keyCode = try {
            // Try common property names
            message.javaClass.getMethod("getKey").invoke(message) as? Int
                ?: message.javaClass.getMethod("getKeyCode").invoke(message) as? Int
                ?: message.javaClass.getMethod("getCode").invoke(message) as? Int
                ?: (message.javaClass.getDeclaredField("key")?.apply { isAccessible = true }?.get(message) as? Int)
                ?: (message.javaClass.getDeclaredField("keyCode")?.apply { isAccessible = true }?.get(message) as? Int)
                ?: (message.javaClass.getDeclaredField("code")?.apply { isAccessible = true }?.get(message) as? Int)
                ?: -1
        } catch (e: Exception) {
            -1
        }
        
        if (keyCode == -1) {
            return // Could not determine key code
        }
        
        // Map F keys to their corresponding varbit IDs
        // Varbit IDs: ATTACK=4675, SKILLS=4676, QUEST=4677, INVENTORY=4678, EQUIPMENT=4679,
        // PRAYER=4680, MAGEBOOK=4682, FRIENDS=4684, PROFILE=6517, LOGOUT=4689
        val fKeyToVarbit = mapOf(
            112 to 4675,  // F1 -> Combat (ATTACK_KEYBIND)
            113 to 4676,  // F2 -> Skills (SKILLS_KEYBIND)
            114 to 4677,  // F3 -> Quests (QUEST_KEYBIND)
            115 to 4678,  // F4 -> Inventory (INVENTORY_KEYBIND)
            116 to 4679,  // F5 -> Equipment (EQUIPMENT_KEYBIND)
            117 to 4680,  // F6 -> Prayer (PRAYER_KEYBIND)
            118 to 4682,  // F7 -> Magic (MAGEBOOK_KEYBIND)
            119 to 4684,  // F8 -> Friends (FRIENDS_KEYBIND)
            120 to 6517,  // F9 -> Account Management (PROFILE_KEYBIND)
            121 to 4689,  // F10 -> Logout (LOGOUT_KEYBIND)
        )
        
        val varbitId = fKeyToVarbit[keyCode] ?: return
        
        // Get the varbit definition to access varp, startBit, and endBit
        val varbitDef = getVarbit(varbitId)
        
        // Get the tab ID from the varbit (0-13) using varps directly
        val tabId = client.varps.getBit(varbitDef.varp, varbitDef.startBit, varbitDef.endBit)
        
        // Tab IDs: 0=ATTACK, 1=SKILLS, 2=QUEST, 3=INVENTORY, 4=EQUIPMENT, 5=PRAYER,
        // 6=MAGIC, 7=CLAN_CHAT, 8=FRIENDS, 9=IGNORES, 10=LOG_OUT, 11=SETTINGS, 12=EMOTES, 13=MUSIC
        if (tabId < 0 || tabId > 13) {
            return // Invalid tab ID
        }
        
        // Get the script ID for FOCUS_TAB ("toplevel_sidebutton_switch")
        val focusTabScriptId = CacheManager.findScriptId("toplevel_sidebutton_switch")
        if (focusTabScriptId == -1) {
            return // Script not found
        }
        
        // Focus the appropriate tab by running the client script
        client.write(RunClientScript(focusTabScriptId, listOf(tabId)))
    }
}
