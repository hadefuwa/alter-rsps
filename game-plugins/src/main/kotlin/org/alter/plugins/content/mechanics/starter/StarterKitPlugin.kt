package org.alter.plugins.content.mechanics.starter

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.attr.NEW_ACCOUNT_ATTR
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

class StarterKitPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        onLogin {
            if (player.attr[NEW_ACCOUNT_ATTR] ?: return@onLogin) {
                with(player.inventory) {
                    add(getRSCM("item.logs"), 5)
                    add(getRSCM("item.tinderbox"))
                    add(getRSCM("item.bread"), 5)
                    add(getRSCM("item.bronze_pickaxe"))
                    add(getRSCM("item.bronze_dagger"))
                    add(getRSCM("item.knife"))
                }

                // Add starter pack to bank
                with(player.bank) {
                    // 3 Barrows items
                    add(getRSCM("item.dharoks_helm"))
                    add(getRSCM("item.guthans_helm"))
                    add(getRSCM("item.veracs_helm"))

                    // 5 Rune items
                    add(getRSCM("item.rune_platelegs"))
                    add(getRSCM("item.rune_platebody"))
                    add(getRSCM("item.rune_full_helm"))
                    add(getRSCM("item.rune_kiteshield"))
                    add(getRSCM("item.rune_scimitar"))

                    // 3 Adamant items
                    add(getRSCM("item.adamant_platelegs"))
                    add(getRSCM("item.adamant_platebody"))
                    add(getRSCM("item.adamant_full_helm"))

                    // 20m coins
                    add(getRSCM("item.coins_995"), 20000000)

                    // Food and prayer potions
                    add(getRSCM("item.shark"), 100)
                    add(getRSCM("item.lobster"), 100)
                    add(getRSCM("item.monkfish"), 100)
                    add(getRSCM("item.prayer_potion4"), 50)
                }

                // Set default F key bindings (simple layout)
                player.setVarbit(4675, 1)   // Combat -> F1
                player.setVarbit(4676, 0)   // Skills -> None
                player.setVarbit(4677, 0)   // Quests -> None
                player.setVarbit(4678, 2)   // Inventory -> F2
                player.setVarbit(4679, 0)   // Equipment -> None
                player.setVarbit(4680, 3)   // Prayer -> F3
                player.setVarbit(4682, 4)   // Magic -> F4
                player.setVarbit(4684, 0)   // Social -> None
                player.setVarbit(6517, 0)   // Account Management -> None
                player.setVarbit(4689, 0)   // Logout -> None
                player.setVarbit(4686, 0)   // Settings -> None
                player.setVarbit(4687, 0)   // Emotes -> None
                player.setVarbit(4683, 0)   // Clan Chat -> None
                player.setVarbit(4688, 0)   // Music -> None
            }
        }

    }
}
