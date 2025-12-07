package org.alter.plugins.content.bosses.zulrah

import kotlin.random.Random
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

/**
 * Zulrah Loot Plugin
 *
 * Defines the loot table for Zulrah.
 */
class ZulrahLootPlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

        init {
                // Disabled: Zulrah now uses SharedLootDropPlugin with loot tables from combat definition
                // setupZulrahLoot()
        }

        private fun setupZulrahLoot() {
                // Disabled: Zulrah now uses SharedLootDropPlugin with loot tables from combat definition
                // onNpcDeath("npc.zulrah") {
                //         val npc = this.npc
                //         val killer = npc.getDamageDealer() ?: return@onNpcDeath
                //
                //         generateZulrahLoot(npc, killer)
                // }
        }

        /**
         * Helper to get the top damage dealer or last hitter. Reusing logic similar to other loot
         * plugins.
         */
        private fun Npc.getDamageDealer(): Player? {
                val damageMap = this.damageMap
                val players = mutableListOf<Player>()
                world.players.forEach { player ->
                        if (damageMap.getDamageFrom(player) > 0) {
                                players.add(player)
                        }
                }
                return players.maxByOrNull { damageMap.getDamageFrom(it) }
        }

        private fun generateZulrahLoot(npc: Npc, player: Player) {
                // Guaranteed: 100% Scales
                // Assuming item ID key is item.zulrahs_scales or similar.
                // Fallback to coins if failing (try-catch wrapped internally by server usually or
                // we can
                // check)

                try {
                        // Scales
                        world.spawn(
                                GroundItem(
                                        item = getRSCM("item.zulrahs_scales"),
                                        amount = Random.nextInt(100, 300),
                                        tile = npc.tile,
                                        owner = player
                                )
                        )
                } catch (e: Exception) {
                        // Fallback if item config missing
                        world.spawn(
                                GroundItem(
                                        item = getRSCM("item.coins"),
                                        amount = 5000,
                                        tile = npc.tile,
                                        owner = player
                                )
                        )
                }

                // Secondary Main Drop (Resources)
                val resourceTable =
                        listOf(
                                "item.battlestaff" to 10,
                                "item.dragon_bones" to 12,
                                "item.magic_logs" to 35,
                                "item.runite_ore" to 2,
                                "item.raw_shark" to 35,
                                "item.grapes" to 250,
                                "item.coins" to 20000
                        )

                val roll = resourceTable.random()
                try {
                        world.spawn(
                                GroundItem(
                                        item = getRSCM(roll.first),
                                        amount = roll.second,
                                        tile = npc.tile,
                                        owner = player
                                )
                        )
                } catch (e: Exception) {}

                // Rare Drop Table (1/128 approx)
                if (Random.nextInt(128) == 0) {
                        val rares =
                                listOf(
                                        "item.tanzanite_fang",
                                        "item.magic_fang",
                                        "item.serpentine_visage",
                                        "item.uncut_onyx"
                                )
                        val rare = rares.random()
                        try {
                                world.spawn(
                                        GroundItem(
                                                item = getRSCM(rare),
                                                amount = 1,
                                                tile = npc.tile,
                                                owner = player
                                        )
                                )
                                world.players.forEach { p ->
                                        p.message(
                                                "<col=ff0000>${player.username} has received a drop: ${rare.replace("item.", "").replace("_", " ")}</col>"
                                        )
                                }
                        } catch (e: Exception) {}
                }
        }
}
