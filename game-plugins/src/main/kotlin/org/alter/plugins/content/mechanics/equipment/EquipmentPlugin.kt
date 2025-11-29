package org.alter.plugins.content.mechanics.equipment

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.EquipmentType.Companion.EQUIPMENT_INTERFACE_ID
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.action.EquipAction
import org.alter.game.fs.ObjectExamineHolder
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
import org.alter.plugins.content.combat.Combat

class EquipmentPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        for (equipment in EquipmentType.values) {
            onEquipToSlot(equipment.id) {
                if (equipment == EquipmentType.WEAPON) {
                    player.sendWeaponComponentInformation()
                    
                    // Clear autocast when switching to a non-magic weapon or when weapon slot is empty
                    // Only keep autocast if the new weapon is a magic staff, trident, or regular staff
                    val weaponType = player.getWeaponType()
                    val isMagicWeapon = weaponType == WeaponType.MAGIC_STAFF.id ||
                                       weaponType == WeaponType.STAFF.id ||
                                       weaponType == WeaponType.TRIDENT.id
                    
                    if (!isMagicWeapon) {
                        // Clear autocast varbit
                        player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, 0)
                        // Clear casting spell attribute
                        player.attr.remove(Combat.CASTING_SPELL)
                        // Clear defensive autocast selection
                        player.attr.remove(Combat.DEFENSIVE_AUTOCAST_SELECTION)
                    }
                }
            }
        }
        
        // Configure wilderness sceptres to auto-cast when equipped
        // Accursed Sceptre and Thammaron's Sceptre auto-cast Fire Wave (no runes required)
        onItemEquip("item.accursed_sceptre") {
            player.setVarbit(org.alter.plugins.content.combat.Combat.SELECTED_AUTOCAST_VARBIT, org.alter.plugins.content.combat.strategy.magic.CombatSpell.FIRE_WAVE.autoCastId)
        }
        
        onItemEquip("item.thammarons_sceptre") {
            player.setVarbit(org.alter.plugins.content.combat.Combat.SELECTED_AUTOCAST_VARBIT, org.alter.plugins.content.combat.strategy.magic.CombatSpell.FIRE_WAVE.autoCastId)
        }
        
        // Clear auto-cast when wilderness sceptres are unequipped
        onItemUnequip("item.accursed_sceptre") {
            player.setVarbit(org.alter.plugins.content.combat.Combat.SELECTED_AUTOCAST_VARBIT, 0)
        }
        
        onItemUnequip("item.thammarons_sceptre") {
            player.setVarbit(org.alter.plugins.content.combat.Combat.SELECTED_AUTOCAST_VARBIT, 0)
        }

        bind_unequip(EquipmentType.HEAD, child = 15)
        bind_unequip(EquipmentType.CAPE, child = 16)
        bind_unequip(EquipmentType.AMULET, child = 17)
        bind_unequip(EquipmentType.WEAPON, child = 18)
        bind_unequip(EquipmentType.CHEST, child = 19)
        bind_unequip(EquipmentType.SHIELD, child = 20)
        bind_unequip(EquipmentType.LEGS, child = 21)
        bind_unequip(EquipmentType.GLOVES, child = 22)
        bind_unequip(EquipmentType.BOOTS, child = 23)
        bind_unequip(EquipmentType.RING, child = 24)
        bind_unequip(EquipmentType.AMMO, child = 25)
    }


    fun bind_unequip(
        equipment: EquipmentType,
        child: Int,
    ) {
        onButton(interfaceId = EQUIPMENT_INTERFACE_ID, component = child) {
            val opt = player.getInteractingOption()
            when (opt) {
                1 -> {
                    val result = EquipAction.unequip(player, equipment.id)
                    if (equipment == EquipmentType.WEAPON && result == EquipAction.Result.SUCCESS) {
                        player.sendWeaponComponentInformation()
                        // Clear autocast when weapon is unequipped (weapon type becomes NONE)
                        // Only clear if it's not one of the wilderness sceptres (they have their own handlers)
                        val unequippedItemId = player.getInteractingItem()?.id
                        if (unequippedItemId != null) {
                            try {
                                val itemName = getItem(unequippedItemId).name.lowercase()
                                // Don't clear if it's a wilderness sceptre (they have their own handlers above)
                                if (!itemName.contains("accursed") && !itemName.contains("thammaron")) {
                                    player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, 0)
                                    player.attr.remove(Combat.CASTING_SPELL)
                                    player.attr.remove(Combat.DEFENSIVE_AUTOCAST_SELECTION)
                                }
                            } catch (e: Exception) {
                                // If we can't get item info, clear autocast anyway
                                player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, 0)
                                player.attr.remove(Combat.CASTING_SPELL)
                                player.attr.remove(Combat.DEFENSIVE_AUTOCAST_SELECTION)
                            }
                        } else {
                            // No item info available, clear autocast anyway
                            player.setVarbit(Combat.SELECTED_AUTOCAST_VARBIT, 0)
                            player.attr.remove(Combat.CASTING_SPELL)
                            player.attr.remove(Combat.DEFENSIVE_AUTOCAST_SELECTION)
                        }
                    }
                }
                10 -> {
                    val item = player.equipment[equipment.id] ?: return@onButton
                    world.sendExamine(player, item.id, ExamineEntityType.ITEM)
                }
                else -> {
                    val item = player.equipment[equipment.id] ?: return@onButton
                    val menuOpt = opt
                    if (!world.plugins.executeEquipmentOption(player, item.id, menuOpt) && world.devContext.debugItemActions) {
                        val action = ObjectExamineHolder.EQUIPMENT_MENU.get(item.id).equipmentMenu[menuOpt]
                        player.message("Unhandled equipment action: [item=${item.id}, option=$menuOpt, action=$action]")
                    }
                }
            }
        }
    }
}
