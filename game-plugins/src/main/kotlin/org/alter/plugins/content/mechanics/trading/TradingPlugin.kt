package org.alter.plugins.content.mechanics.trading

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
import org.alter.plugins.content.mechanics.trading.impl.TradeSession
import org.alter.plugins.content.mechanics.trading.impl.TradeSession.Companion.ACCEPT_INTERFACE
import org.alter.plugins.content.mechanics.trading.impl.TradeSession.Companion.OVERLAY_INTERFACE
import org.alter.plugins.content.mechanics.trading.impl.TradeSession.Companion.PLAYER_TRADE_CHILD
import org.alter.plugins.content.mechanics.trading.impl.TradeSession.Companion.TRADE_INTERFACE

class TradingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    /**
     * The number of trade requests
     */
    val REQUEST_CAPACITY = 10

    /**
     * The message sent when a player requests to trade.
     */
    val TRADE_REQ_STRING = "%s wishes to trade with you."

    init {

        /**
         * Initiate the set of trade requests
         */
        onLogin { player.attr[TRADE_REQUESTS] = HashSet(REQUEST_CAPACITY) }

        /**
         * When a player requests to trade a user, we should first check to see if the player they
         * are interacting with has recently sent them a trade request. If so, we should progress the trade request
         * rather than sending out a new request.
         */
        onPlayerOption(option = "Trade with") {
            // The trade partner instance - safely get the interacting player
            val partner = player.attr[INTERACTING_PLAYER_ATTR]?.get() ?: run {
                player.message("Unable to find trade partner.")
                return@onPlayerOption
            }

            // Don't allow trading with yourself
            if (partner == player) {
                return@onPlayerOption
            }

            // Verify partner is still in the world
            if (!world.players.contains(partner)) {
                player.message("That player is no longer available.")
                return@onPlayerOption
            }

            // Check if players are close enough to trade (within 15 tiles, same level)
            val distance = player.tile.getDistance(partner.tile)
            val sameLevel = player.tile.height == partner.tile.height
            if (distance > 15 || !sameLevel) {
                player.message("You need to be closer to trade with that player.")
                return@onPlayerOption
            }

            // If the player is already in a trade
            if (partner.getTradeSession() != null || partner.isLocked()) {
                player.message("Other player is busy at the moment.")
                return@onPlayerOption
            }

            // Check if the current player is already in a trade
            if (player.getTradeSession() != null || player.isLocked()) {
                player.message("You are busy at the moment.")
                return@onPlayerOption
            }

            // The set of players who have requested the player
            val requests = player.getTradeRequests()

            // If the partner hasn't recently requested a trade
            if (!requests.contains(partner)) {

                // Add the player to the partner's requests
                partner.getTradeRequests().add(player)

                // Send the trade request
                player.message("Sending trade request...")
                partner.message(TRADE_REQ_STRING.format(player.username), ChatMessageType.TRADE_REQ, player.username)
            } else {

                // Remove the requests
                player.getTradeRequests().remove(partner)
                partner.getTradeRequests().remove(player)

                // Initiate the trade
                initiate(player, partner)
            }
        }

        // Item Offer Event
        onButton(OVERLAY_INTERFACE, 0) {
            player.getTradeSession()?.let { trade ->

                // The player's inventory
                val inventory = player.inventory

                // The item slot, and the option that was pressed
                val slot = player.getInteractingSlot()
                val opt = player.getInteractingOption()

                // The item being traded
                val item = inventory[slot] ?: return@onButton
                
                // Safety check: skip corrupted items (negative amounts except -2)
                if (item.amount < 0 && item.amount != -2) {
                    player.message("Unable to trade corrupted item.")
                    return@onButton
                }

                // Queue the action, as we might need to access queued dialogue
                player.queue(TaskPriority.WEAK) {

                    // The amount being traded
                    val amount =
                        when (opt) {
                            2 -> 5
                            3 -> 10
                            4 -> inventory.getItemCount(item.id)
                            5 -> inputInt(player, "Enter amount:")
                            else -> 1
                        }
                    
                    // Safety check: ensure amount is valid
                    if (amount <= 0) {
                        return@queue
                    }

                    // Try to offer using the slot directly first (should match since inventory is copied at trade start)
                    // If that fails, find the item by ID starting from the slot
                    val tradeItem = trade.inventory[slot]
                    if (tradeItem != null && tradeItem.id == item.id && tradeItem.amount > 0) {
                        // Slot matches, use it directly
                        trade.offer(slot, amount)
                    } else {
                        // Slot doesn't match (inventory changed or corrupted), find by ID starting from slot
                        var tradeSlot = -1
                        for (i in slot until trade.inventory.capacity) {
                            val it = trade.inventory[i]
                            if (it != null && it.id == item.id && it.amount > 0) {
                                tradeSlot = i
                                break
                            }
                        }
                        
                        if (tradeSlot == -1) {
                            // Try searching from the beginning if not found from slot
                            for (i in 0 until trade.inventory.capacity) {
                                val it = trade.inventory[i]
                                if (it != null && it.id == item.id && it.amount > 0) {
                                    tradeSlot = i
                                    break
                                }
                            }
                            if (tradeSlot != -1) {
                                trade.offer(tradeSlot, amount)
                            } else {
                                player.message("Item not found in trade inventory.")
                            }
                        } else {
                            trade.offer(tradeSlot, amount)
                        }
                    }
                }
            }
        }

        // Item Remove Event
        onButton(TRADE_INTERFACE, PLAYER_TRADE_CHILD) {
            player.getTradeSession()?.let { trade ->

                // The player's trade container
                val container = trade.container

                // The item slot, and the option that was pressed
                val slot = player.getInteractingSlot()
                val opt = player.getInteractingOption()

                // The item being traded
                val item = container[slot] ?: return@onButton

                // Queue the action, as we might need to access queued dialogue
                player.queue(TaskPriority.WEAK) {

                    // The amount being traded
                    val amount =
                        when (opt) {
                            2 -> 5
                            3 -> 10
                            4 -> container.getItemCount(item.id)
                            5 -> inputInt(player, "Enter amount:")
                            else -> 1
                        }

                    // Offer the amount to the trade
                    trade.remove(slot, amount)
                }
            }
        }

        // Accept buttons
        onButton(TRADE_INTERFACE, 10) { player.getTradeSession()?.progress() }
        onButton(ACCEPT_INTERFACE, 13) { player.getTradeSession()?.progress() }

        // Decline buttons
        onButton(TRADE_INTERFACE, 11) { player.getTradeSession()?.decline() }
        onButton(ACCEPT_INTERFACE, 14) { player.getTradeSession()?.decline() }

        // Interface close events
        onInterfaceClose(TRADE_INTERFACE) {

            if (player.hasTradeSession() && !player.hasAcceptedTrade()) {
                player.getTradeSession()?.decline()
            }
        }

        onInterfaceClose(ACCEPT_INTERFACE) {

            if (player.hasTradeSession() && !player.hasAcceptedTrade()) {
                player.getTradeSession()?.decline()
            }
        }

        // Decline the trade when a player logs out
        onLogout { player.getTradeSession()?.decline() }
    }

    /**
     * Initiates a trade between two players
     *
     * @param player    The first player
     * @param partner   The partner player
     */
    fun initiate(
        player: Player,
        partner: Player,
    ) {
        // The trade session instances
        val playerSession = TradeSession(player, partner)
        val partnerSession = TradeSession(partner, player)

        // Define the session attribute for both players
        player.attr[TRADE_SESSION_ATTR] = playerSession
        partner.attr[TRADE_SESSION_ATTR] = partnerSession

        // Initialise the sessions
        playerSession.open()
        partnerSession.open()
    }



}
