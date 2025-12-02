package org.alter.plugins.content.objects.bankbooth

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.plugins.content.interfaces.bank.openBank

class BankBoothsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        val BOOTHS =
            setOf(
                "object.bank_booth",
                "object.bank_booth_10355",
                "object.bank_booth_10357",
                "object.bank_booth_10517",
                "object.bank_booth_10583",
                "object.bank_booth_10584",
                "object.bank_booth_11338",
                "object.bank_booth_12798",
                "object.bank_booth_14367",
                "object.bank_booth_32666",
                "object.bank_booth_16642",
                "object.bank_booth_16700",
                "object.bank_booth_18491",
                "object.bank_booth_20325",
                "object.bank_booth_22819",
                "object.bank_booth_24101",
                "object.bank_booth_24347",
                "object.bank_booth_25808",
                "object.bank_booth_27254",
                "object.bank_booth_27260",
                "object.bank_booth_27263",
                "object.bank_booth_27265",
                "object.bank_booth_27267",
                "object.bank_booth_27292",
                "object.bank_booth_27718",
                "object.bank_booth_27719",
                "object.bank_booth_27720",
                "object.bank_booth_27721",
                "object.bank_booth_28430",
                "object.bank_booth_28431",
                "object.bank_booth_28432",
                "object.bank_booth_28433",
                "object.bank_booth_28546",
                "object.bank_booth_28547",
                "object.bank_booth_28548",
                "object.bank_booth_28549",
                "object.bank_booth_36559",
                // BANK_BOOTH_37959,// Has "use" option
                "object.bank_booth_39238",
                "object.bank_booth_42837", // has only "bank option"
                // "object.bank_booth_26711", // Handled separately below (no RSCM name)
                // "object.bank_chest_26707", // Mage bank - handled separately below
            )

        BOOTHS.forEach { booth ->
            if (objHasOption(booth, "bank")) {
                onObjOption(obj = booth, option = "bank") {
                    player.openBank()
                }
            }
            if (objHasOption(booth, "Collect")) {
                onObjOption(obj = booth, option = "Collect") {
                    open_collect(player)
                }
            }
        }
        
        // Handle Mage Bank chest (26707) separately - uses "Use" option instead of "bank"
        onObjOption(obj = "object.bank_chest_26707", option = "Use") {
            player.openBank()
        }
        onObjOption(obj = "object.bank_chest_26707", option = "Collect") {
            open_collect(player)
        }
        
        // Handle bank 26711 separately - uses numeric ID since RSCM name doesn't exist
        try {
            // Try common bank options with numeric ID
            onObjOption(obj = 26711, option = "bank") {
                player.openBank()
            }
        } catch (e: Exception) {
            // "bank" option might not exist, try "Use" option
            try {
                onObjOption(obj = 26711, option = "Use") {
                    player.openBank()
                }
            } catch (e2: Exception) {
                // Could not register
            }
        }
        
        // Try "Collect" option for bank 26711
        try {
            onObjOption(obj = 26711, option = "Collect") {
                open_collect(player)
            }
        } catch (e: Exception) {
            // "Collect" option might not exist
        }
        
        // Handle bank 46223 separately - uses numeric ID since RSCM name is null_46223
        try {
            // Try common bank options with numeric ID
            onObjOption(obj = 46223, option = "bank") {
                player.openBank()
            }
        } catch (e: Exception) {
            // "bank" option might not exist, try "Use" option
            try {
                onObjOption(obj = 46223, option = "Use") {
                    player.openBank()
                }
            } catch (e2: Exception) {
                // Could not register
            }
        }
        
        // Try "Collect" option for bank 46223
        try {
            onObjOption(obj = 46223, option = "Collect") {
                open_collect(player)
            }
        } catch (e: Exception) {
            // "Collect" option might not exist
        }
        
        // Also try using RSCM name as backup
        try {
            if (objHasOption("object.null_46223", "bank")) {
                onObjOption(obj = "object.null_46223", option = "bank") {
                    player.openBank()
                }
            }
            if (objHasOption("object.null_46223", "Collect")) {
                onObjOption(obj = "object.null_46223", option = "Collect") {
                    open_collect(player)
                }
            }
        } catch (e: Exception) {
            // RSCM name might not work, that's okay
        }
    }

fun open_collect(p: Player) {
    p.setInterfaceUnderlay(color = -1, transparency = -1)
    p.openInterface(interfaceId = 402, dest = InterfaceDestination.MAIN_SCREEN)
}

}
