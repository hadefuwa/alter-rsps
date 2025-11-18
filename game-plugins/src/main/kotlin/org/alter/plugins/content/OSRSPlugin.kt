package org.alter.plugins.content

import org.alter.api.*
import org.alter.api.CommonClientScripts
import org.alter.api.InterfaceDestination
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
import org.alter.game.model.move.MovementQueue.StepType
import org.alter.game.model.move.hasMoveDestination
import org.alter.game.model.move.walkRoute
import org.alter.game.model.move.toTileQueue
import org.alter.game.plugin.*
import java.lang.ref.WeakReference

class OSRSPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        /**
         * Closing main modal for players.
         */
        setModalCloseLogic {
            val modal = player.interfaces.getModal()
            if (modal != -1) {
                player.closeInterface(modal)
                player.interfaces.setModal(-1)
            }
        }
        /**
         * Check if the player has a menu opened.
         */
        setMenuOpenCheck {
            player.getInterfaceAt(dest = InterfaceDestination.MAIN_SCREEN) != -1
        }

        /**
         * Execute when a player logs in.
         */
        onLogin {
            with(player) {
                /**
                 * @TODO Inspect, uhh seems that this logic is being repeated, not removing it yet as im unsure rn if it's needed or not
                 */
                // Skill-related logic.
                calculateAndSetCombatLevel()
                if (getSkills().getBaseLevel(Skills.HITPOINTS) < 10) {
                    getSkills().setBaseLevel(Skills.HITPOINTS, 10)
                }
                calculateAndSetCombatLevel()
                sendWeaponComponentInformation()
                sendCombatLevelText()
                setInterfaceEvents(
                    interfaceId = 149,
                    component = 0,
                    range = 0..27,
                    setting =
                        arrayOf(
                            InterfaceEvent.ClickOp2,
                            InterfaceEvent.ClickOp3,
                            InterfaceEvent.ClickOp4,
                            InterfaceEvent.ClickOp6,
                            InterfaceEvent.ClickOp7,
                            InterfaceEvent.ClickOp10,
                            InterfaceEvent.UseOnGroundItem,
                            InterfaceEvent.UseOnNpc,
                            InterfaceEvent.UseOnObject,
                            InterfaceEvent.UseOnPlayer,
                            InterfaceEvent.UseOnInventory,
                            InterfaceEvent.UseOnComponent,
                            InterfaceEvent.DRAG_DEPTH1,
                            InterfaceEvent.DragTargetable,
                            InterfaceEvent.ComponentTargetable,
                        ),
                )
                player.openDefaultInterfaces()
                setVarbit(Varbit.COMBAT_LEVEL_VARBIT, combatLevel)
                setVarbit(Varbit.CHATBOX_UNLOCKED, 1)
                setVarbit(Varbit.HIDE_ROOFS, 1) // Disable all roofs in the game
                runClientScript(CommonClientScripts.INTRO_MUSIC_RESTORE)
                if (getVarp(Varp.PLAYER_HAS_DISPLAY_NAME) == 0 && username.isNotBlank()) {
                    syncVarp(Varp.PLAYER_HAS_DISPLAY_NAME)
                }
                // Sync attack priority options.
                syncVarp(Varp.NPC_ATTACK_PRIORITY_VARP)
                syncVarp(Varp.PLAYER_ATTACK_PRIORITY_VARP)
                // Send player interaction options.
                // Note: Option slot 1 is typically reserved for "Attack" in PvP/wilderness
                // So we use slots 2, 3, 4 for Follow, Trade, Report
                sendOption("Follow", 2)
                sendOption("Trade with", 3)
                sendOption("Report", 4)
                // Game-related logic.
                sendRunEnergy(player.runEnergy.toInt())
                message("Welcome to ${world.gameContext.name}.", ChatMessageType.GAME_MESSAGE)
                // player.social.pushFriends(player)
                // player.social.pushIgnores(player)
                setVarbit(Varbit.ESC_CLOSES_CURRENT_INTERFACE, 1)

                /**
                 * @TODO
                 * As for now these varbit's disable Black bar on right side for Native client,
                 * The black bar is for loot tracker n whatnot
                 */
                setVarbit(13982, 1)
                setVarbit(13981, 1)
            }
        }

        /**
         * Handle the "Follow" player option.
         * When a player clicks "Follow" on another player, they will continuously follow them.
         */
        onPlayerOption("Follow") {
            // Get the target from INTERACTING_PLAYER_ATTR (set by walk plugin)
            val target = player.attr[INTERACTING_PLAYER_ATTR]?.get() ?: run {
                player.message("Unable to find player to follow.")
                return@onPlayerOption
            }
            
            // Don't allow following yourself
            if (target == player) {
                return@onPlayerOption
            }
            
            // Verify target is still in the world
            if (!world.players.contains(target)) {
                player.message("That player is no longer available.")
                return@onPlayerOption
            }
            
            // Stop any existing follow
            if (player.attr.has(FOLLOWING_TARGET_ATTR)) {
                player.attr.remove(FOLLOWING_TARGET_ATTR)
            }
            
            // Set the new follow target
            player.attr[FOLLOWING_TARGET_ATTR] = WeakReference(target)
            player.message("Following ${target.username}.")
            
            // Start the continuous follow loop with higher priority to run after walk completes
            player.queue(TaskPriority.WEAK) {
                terminateAction = {
                    // Clean up when follow is stopped
                    player.attr.remove(FOLLOWING_TARGET_ATTR)
                }
                
                // Wait for the initial walk to target to complete
                while (player.hasMoveDestination()) {
                    wait(1)
                }
                
                // Start continuous following
                while (true) {
                    val followTarget = player.attr[FOLLOWING_TARGET_ATTR]?.get()
                    
                    // If no target or target is invalid, stop following
                    if (followTarget == null || !world.players.contains(followTarget)) {
                        player.attr.remove(FOLLOWING_TARGET_ATTR)
                        break
                    }
                    
                    // Check if player is already close enough (within 1 tile)
                    val distance = player.tile.getDistance(followTarget.tile)
                    val sameLevel = player.tile.height == followTarget.tile.height
                    
                    if (distance <= 1 && sameLevel) {
                        // Already close, just wait and check again
                        wait(2)
                        continue
                    }
                    
                    // If target is too far away (more than 15 tiles), stop following
                    if (distance > 15 || !sameLevel) {
                        player.message("You can't reach that.")
                        player.attr.remove(FOLLOWING_TARGET_ATTR)
                        break
                    }
                    
                    // Walk towards the target
                    val route = player.world.smartRouteFinder.findRoute(
                        level = player.tile.height,
                        srcX = player.tile.x,
                        srcZ = player.tile.z,
                        destX = followTarget.tile.x,
                        destZ = followTarget.tile.z,
                        locShape = -2,
                    )
                    
                    // Only walk if we have a valid route
                    if (route.success) {
                        player.walkRoute(route.toTileQueue(), stepType = StepType.NORMAL)
                        
                        // Wait for movement to complete, checking periodically if we should stop
                        while (player.hasMoveDestination()) {
                            // Check if we should stop following (target moved too far, player manually moved, etc.)
                            val currentTarget = player.attr[FOLLOWING_TARGET_ATTR]?.get()
                            if (currentTarget == null || !world.players.contains(currentTarget)) {
                                player.attr.remove(FOLLOWING_TARGET_ATTR)
                                return@queue
                            }
                            
                            val currentDistance = player.tile.getDistance(currentTarget.tile)
                            if (currentDistance > 15 || player.tile.height != currentTarget.tile.height) {
                                player.attr.remove(FOLLOWING_TARGET_ATTR)
                                return@queue
                            }
                            
                            wait(1)
                        }
                    } else {
                        // No valid route, wait a bit before trying again
                        wait(2)
                    }
                    
                    // Small delay before next follow check
                    wait(1)
                }
            }
        }

        // TODO Whats this for:?
        onButton(245, 20) {
            player.openInterface(interfaceId = 626, dest = InterfaceDestination.MAIN_SCREEN)
        }
    }

    fun Player.openDefaultInterfaces() {
        openOverlayInterface(interfaces.displayMode)
        openModals(this)
        setInterfaceEvents(interfaceId = 239, component = 3, range = 0..665, setting = 6) // enable music buttons
        initInterfaces(interfaces.displayMode)
    }

    fun openModals(
        player: Player,
        fullscreen: Boolean = false,
    ) {
        InterfaceDestination.getModals().forEach { pane ->
            if (pane == InterfaceDestination.XP_COUNTER && player.getVarbit(Varbit.XP_DROPS_VISIBLE_VARBIT) == 0) {
                return@forEach
            } else if (pane == InterfaceDestination.MINI_MAP && player.getVarbit(Varbit.HIDE_DATA_ORBS_VARBIT) == 1) {
                return@forEach
            }
            player.openInterface(pane.interfaceId, pane, fullscreen)
        }
    }

}
