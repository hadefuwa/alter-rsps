package org.alter.plugins.content.mechanics.appearance

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.appearance.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.info.PlayerInfo
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.shop.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*

class AppearanceInterfacePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    private val APPEARANCE_INTERFACE_ID = 679
    
    init {
        // Initialize interface when opened - set varbit and save original appearance
        onInterfaceOpen(APPEARANCE_INTERFACE_ID) {
            // Set varbit to match current gender (0 = male, 1 = female)
            player.setVarbit(11697, if (player.appearance.gender == Gender.MALE) 0 else 1)
            // Save original appearance so we can restore it if player cancels
            player.attr[ORIGINAL_APPEARANCE_ATTR] = Appearance(
                looks = player.appearance.looks.copyOf(),
                colors = player.appearance.colors.copyOf(),
                gender = player.appearance.gender
            )
        }
        
        // Handle interface close - restore original appearance if not confirmed
        onInterfaceClose(APPEARANCE_INTERFACE_ID) {
            // Only restore if the appearance wasn't confirmed (APPEARANCE_SET_ATTR wasn't set)
            // We check this by seeing if the original appearance is still stored
            player.attr[ORIGINAL_APPEARANCE_ATTR]?.let { originalAppearance ->
                // Restore original appearance
                player.appearance = Appearance(
                    looks = originalAppearance.looks.copyOf(),
                    colors = originalAppearance.colors.copyOf(),
                    gender = originalAppearance.gender
                )
                PlayerInfo(player).syncAppearance()
                // Clear the stored original appearance
                player.attr.remove(ORIGINAL_APPEARANCE_ATTR)
            }
        }
        
        // Change gender to MALE
        onButton(APPEARANCE_INTERFACE_ID, 65) {
            player.setVarbit(11697, 0)
            // Reset looks to default (males have 7 looks, females have 6, so structure is different)
            // But preserve colors since they're the same for both genders
            player.appearance = Appearance.DEFAULT_MALE.copy(
                colors = player.appearance.colors.copyOf()
            )
            PlayerInfo(player).syncAppearance()
        }
        
        // Change gender to FEMALE
        onButton(APPEARANCE_INTERFACE_ID, 66) {
            player.setVarbit(11697, 1)
            // Reset looks to default (males have 7 looks, females have 6, so structure is different)
            // But preserve colors since they're the same for both genders
            player.appearance = Appearance.DEFAULT_FEMALE.copy(
                colors = player.appearance.colors.copyOf()
            )
            PlayerInfo(player).syncAppearance()
        }
        
        // Cancel button - restore original appearance and close interface
        onButton(APPEARANCE_INTERFACE_ID, 67) {
            player.attr[ORIGINAL_APPEARANCE_ATTR]?.let { originalAppearance ->
                // Restore original appearance
                player.appearance = Appearance(
                    looks = originalAppearance.looks.copyOf(),
                    colors = originalAppearance.colors.copyOf(),
                    gender = originalAppearance.gender
                )
                PlayerInfo(player).syncAppearance()
                // Clear the stored original appearance
                player.attr.remove(ORIGINAL_APPEARANCE_ATTR)
            }
            player.closeInterface(APPEARANCE_INTERFACE_ID)
        }
        
        // Confirm appearance selection and close interface
        onButton(APPEARANCE_INTERFACE_ID, 68) {
            player.attr[APPEARANCE_SET_ATTR] = true
            // Clear the stored original appearance since we're confirming
            player.attr.remove(ORIGINAL_APPEARANCE_ATTR)
            player.closeInterface(APPEARANCE_INTERFACE_ID)
        }
        
        // Handle all appearance customization buttons
        AppearanceOps.values().filterNot { it == AppearanceOps.NONE }.forEach { op ->
            // Decrement option (previous)
            onButton(APPEARANCE_INTERFACE_ID, op.component + 2) {
                val opt = op.component
                when {
                    AppearanceOps.isLookOp(opt) -> {
                        val pos = (opt - 10) / 4
                        val current = when (player.appearance.gender) {
                            Gender.MALE -> player.appearance.looks[pos]
                            Gender.FEMALE -> {
                                if (pos == 0) player.appearance.looks[pos]
                                else if (pos == 1) return@onButton // Skip jaw for females
                                else player.appearance.looks[pos - 1]
                            }
                        }
                        
                        val looks = getLooks(opt, player.appearance.gender)
                        if (looks.isEmpty() || looks[0] == -1) return@onButton
                        val clampedCurrent = current.coerceIn(0, looks.size - 1)
                        val previous = if (clampedCurrent - 1 < 0) looks.size - 1 else clampedCurrent - 1
                        
                        when (player.appearance.gender) {
                            Gender.MALE -> {
                                player.appearance = player.appearance.copy(
                                    looks = player.appearance.looks.copyOf().apply { this[pos] = previous }
                                )
                            }
                            Gender.FEMALE -> {
                                if (pos == 0) {
                                    player.appearance = player.appearance.copy(
                                        looks = player.appearance.looks.copyOf().apply { this[pos] = previous }
                                    )
                                } else if (pos != 1) {
                                    player.appearance = player.appearance.copy(
                                        looks = player.appearance.looks.copyOf().apply { this[pos - 1] = previous }
                                    )
                                }
                            }
                        }
                        PlayerInfo(player).syncAppearance()
                    }
                    AppearanceOps.isColourOp(opt) -> {
                        val pos = (opt - 41) / 4
                        val current = player.appearance.colors[pos]
                        val colors = getColours(opt)
                        if (colors.isEmpty() || colors[0] == -1) return@onButton
                        val clampedCurrent = current.coerceIn(0, colors.size - 1)
                        val previous = if (clampedCurrent - 1 < 0) colors.size - 1 else clampedCurrent - 1
                        player.appearance = player.appearance.copy(
                            colors = player.appearance.colors.copyOf().apply { this[pos] = previous }
                        )
                        PlayerInfo(player).syncAppearance()
                    }
                }
            }
            
            // Increment option (next)
            onButton(APPEARANCE_INTERFACE_ID, op.component + 3) {
                val opt = op.component
                when {
                    AppearanceOps.isLookOp(opt) -> {
                        val pos = (opt - 10) / 4
                        val current = when (player.appearance.gender) {
                            Gender.MALE -> player.appearance.looks[pos]
                            Gender.FEMALE -> {
                                if (pos == 0) player.appearance.looks[pos]
                                else if (pos == 1) return@onButton // Skip jaw for females
                                else player.appearance.looks[pos - 1]
                            }
                        }
                        
                        val looks = getLooks(opt, player.appearance.gender)
                        if (looks.isEmpty() || looks[0] == -1) return@onButton
                        val clampedCurrent = current.coerceIn(0, looks.size - 1)
                        val next = if (clampedCurrent + 1 >= looks.size) 0 else clampedCurrent + 1
                        
                        when (player.appearance.gender) {
                            Gender.MALE -> {
                                player.appearance = player.appearance.copy(
                                    looks = player.appearance.looks.copyOf().apply { this[pos] = next }
                                )
                            }
                            Gender.FEMALE -> {
                                if (pos == 0) {
                                    player.appearance = player.appearance.copy(
                                        looks = player.appearance.looks.copyOf().apply { this[pos] = next }
                                    )
                                } else if (pos != 1) {
                                    player.appearance = player.appearance.copy(
                                        looks = player.appearance.looks.copyOf().apply { this[pos - 1] = next }
                                    )
                                }
                            }
                        }
                        PlayerInfo(player).syncAppearance()
                    }
                    AppearanceOps.isColourOp(opt) -> {
                        val pos = (opt - 41) / 4
                        val current = player.appearance.colors[pos]
                        val colors = getColours(opt)
                        if (colors.isEmpty() || colors[0] == -1) return@onButton
                        val clampedCurrent = current.coerceIn(0, colors.size - 1)
                        val next = if (clampedCurrent + 1 >= colors.size) 0 else clampedCurrent + 1
                        player.appearance = player.appearance.copy(
                            colors = player.appearance.colors.copyOf().apply { this[pos] = next }
                        )
                        PlayerInfo(player).syncAppearance()
                    }
                }
            }
        }
    }
    
    enum class AppearanceOps(val component: Int) {
        HEAD(10),
        JAW(14),
        TORSO(18),
        ARMS(22),
        HANDS(26),
        LEGS(30),
        FEET(34),
        HAIR_COLOUR(41),
        TORSO_COLOUR(45),
        LEGS_COLOUR(49),
        FEET_COLOUR(53),
        SKIN_COLOUR(57),
        NONE(0);
        
        companion object {
            fun isValidOp(option: Int): Boolean = (option >= 10 && option <= 60)
            
            fun isLookOp(option: Int): Boolean = (option in 10..37)
            
            fun isColourOp(option: Int): Boolean = (option in 41..60)
            
            fun getOp(option: Int): AppearanceOps {
                return when {
                    !isValidOp(option) -> NONE
                    isLookOp(option) -> getLookOp(option)
                    isColourOp(option) -> getColourOp(option)
                    else -> NONE
                }
            }
            
            private fun getLookOp(option: Int): AppearanceOps {
                return when ((option - 10) / 4) {
                    0 -> HEAD
                    1 -> JAW
                    2 -> TORSO
                    3 -> ARMS
                    4 -> HANDS
                    5 -> LEGS
                    6 -> FEET
                    else -> NONE
                }
            }
            
            private fun getColourOp(option: Int): AppearanceOps {
                return when ((option - 41) / 4) {
                    0 -> HAIR_COLOUR
                    1 -> TORSO_COLOUR
                    2 -> LEGS_COLOUR
                    3 -> FEET_COLOUR
                    4 -> SKIN_COLOUR
                    else -> NONE
                }
            }
        }
    }
    
    private fun getLooks(option: Int, gender: Gender): Array<Int> {
        return when (gender) {
            Gender.MALE -> {
                when ((option - 10) / 4) {
                    0 -> Looks.getHeads(gender)
                    1 -> Looks.getJaws(gender)
                    2 -> Looks.getTorsos(gender)
                    3 -> Looks.getArms(gender)
                    4 -> Looks.getHands(gender)
                    5 -> Looks.getLegs(gender)
                    6 -> Looks.getFeets(gender)
                    else -> arrayOf(-1)
                }
            }
            Gender.FEMALE -> {
                when ((option - 10) / 4) {
                    0 -> Looks.getHeads(gender)
                    2 -> Looks.getTorsos(gender)
                    3 -> Looks.getArms(gender)
                    4 -> Looks.getHands(gender)
                    5 -> Looks.getLegs(gender)
                    6 -> Looks.getFeets(gender)
                    else -> arrayOf(-1)
                }
            }
        }
    }
    
    private fun getColours(option: Int): Array<Int> {
        return when ((option - 41) / 4) {
            0 -> Colours.HAIR_COLOURS
            1 -> Colours.TORSO_COLOURS
            2 -> Colours.LEG_COLOURS
            3 -> Colours.FEET_COLOURS
            4 -> Colours.SKIN_COLOURS
            else -> arrayOf(-1)
        }
    }
}
