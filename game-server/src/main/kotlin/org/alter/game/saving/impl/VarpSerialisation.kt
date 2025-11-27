package org.alter.game.saving.impl

import dev.openrune.cache.CacheManager.getVarbitOrDefault
import org.alter.game.model.entity.Client
import org.alter.game.saving.DocumentHandler
import org.bson.Document

class VarpSerialisation(override val name: String = "varps") : DocumentHandler {

    // Cache the keybinding and bank tab varp IDs on first use
    private val alwaysSaveVarpIds: Set<Int> by lazy {
        // Keybinding varbit IDs from Hotkey.kt
        val keybindingVarbits = listOf(
            4675, // COMBAT (ATTACK_KEYBIND)
            4676, // SKILLS
            4677, // QUESTS
            4678, // INVENTORY
            4679, // EQUIPMENT
            4680, // PRAYERS
            4681, // ESC_CLOSES_INTERFACES
            4682, // MAGIC
            4683, // CLAN_CHAT
            4684, // SOCIAL (FRIENDS)
            4686, // SETTINGS
            4687, // EMOTES
            4688, // MUSIC
            4689, // LOG_OUT
            4690, // FOCUSED_HOTKEY_VARBIT
            6517  // ACCOUNT_MANAGEMENT (PROFILE)
        )

        // Bank tab varbit IDs - these must always be saved to preserve bank organization
        val bankTabVarbits = listOf(
            4150, // BANK_SELECTED_TAB (SELECTED_TAB_VARBIT)
            4170, // BANK_DISPLAY_TYPE (BANK_TAB_ROOT_VARBIT base)
            4171, // BANK_TAB_SIZE_1
            4172, // BANK_TAB_SIZE_2
            4173, // BANK_TAB_SIZE_3
            4174, // BANK_TAB_SIZE_4
            4175, // BANK_TAB_SIZE_5
            4176, // BANK_TAB_SIZE_6
            4177, // BANK_TAB_SIZE_7
            4178, // BANK_TAB_SIZE_8
            4179  // BANK_TAB_SIZE_9
        )

        // Get the varp IDs that these varbits map to
        (keybindingVarbits + bankTabVarbits).mapNotNull { varbitId ->
            try {
                val varbitDef = getVarbitOrDefault(varbitId)
                if (varbitDef.id != -1) varbitDef.varp else null
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    // Shift Drop varbit varp ID - always enabled, must always be saved as 1
    private val shiftDropVarpId: Int? by lazy {
        try {
            val varbitDef = getVarbitOrDefault(5542) // SHIFT_CLICK_TO_DROP_ITEMS
            if (varbitDef.id != -1) varbitDef.varp else null
        } catch (e: Exception) {
            null
        }
    }

    override fun fromDocument(client: Client, doc: Document) = doc.forEach { idKey, stateValue ->
        idKey.toIntOrNull()?.let { id ->
            stateValue?.toString()?.toIntOrNull()?.let { state ->
                client.varps.setState(id, state)
            }
        }
    }

    override fun asDocument(client: Client): Document {
        return Document().apply {
            val varpsToSave = client.varps.getAll()
                .filter { varp ->
                    // Always save varps that should persist (keybindings and bank tabs), even if they're 0
                    // These are the varps that the keybinding and bank tab varbits map to
                    varp.state != 0 || varp.id in alwaysSaveVarpIds
                }
                .associate { it.id.toString() to it.state.toString() }
                .toMutableMap()

            // Always save Shift Drop as enabled (1), regardless of current state
            shiftDropVarpId?.let { varpId ->
                varpsToSave[varpId.toString()] = "1"
            }

            putAll(varpsToSave)
        }
    }

}