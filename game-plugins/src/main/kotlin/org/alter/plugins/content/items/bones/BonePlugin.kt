package org.alter.plugins.content.items.bones

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.*
import org.alter.game.model.container.*
import org.alter.game.model.container.key.*
import org.alter.game.model.entity.*
import org.alter.game.model.item.*
import org.alter.game.model.queue.*
import org.alter.game.model.timer.*
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

class BonePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Helper function to handle bone burying
        fun handleBuryBone(bone: Bone, boneItemId: Int, optionNum: Int) {
            try {
                println("BonePlugin: Attempting to bind ${bone.item} (ID: $boneItemId) to option $optionNum")
                onItemOption(item = bone.item, option = optionNum) {
                    println("BonePlugin: Item option $optionNum clicked for ${bone.item}")
                    val inventorySlot = player.getInteractingItemSlot()
                    val item = player.inventory[inventorySlot] ?: return@onItemOption
                    
                    println("BonePlugin: Item in slot: ID=${item.id}, expected=${boneItemId}")
                    
                    // Only process if this is actually a bone item
                    if (item.id != boneItemId) {
                        println("BonePlugin: Item ID mismatch, returning")
                        return@onItemOption
                    }
                    
                    println("BonePlugin: Processing bury action")
                    
                    // Check if player can interact with items
                    if (!player.lock.canItemInteract()) {
                        return@onItemOption
                    }
                    
                    // Check if player is on cooldown for burying bones
                    if (player.timers.has(BURY_BONE_DELAY)) {
                        return@onItemOption
                    }
                    
                    // Bury the bone
                    player.queue {
                        player.animate(Animation.BURY_BONE_ANIM)
                        player.playSound(Sound.BURYING_BONE)
                        
                        // Wait for animation to complete (2 ticks)
                        wait(2)
                        
                        // Remove the bone from inventory after animation
                        val remove = player.inventory.remove(item = bone.item, beginSlot = inventorySlot, assureFullRemoval = false)
                        if (remove.hasSucceeded() && remove.completed > 0) {
                            // Give prayer experience
                            player.addXp(Skills.PRAYER, bone.xp)
                            
                            // Send message
                            val boneName = getItem(boneItemId).name.lowercase()
                            player.message("You bury the $boneName.")
                        }
                        
                        // Set cooldown timer (1 tick delay between burying)
                        player.timers[BURY_BONE_DELAY] = 1
                    }
                }
            } catch (e: Exception) {
                // Option binding failed, skip this option
                println("BonePlugin: Failed to bind ${bone.item} to option $optionNum: ${e.message}")
                e.printStackTrace()
            }
        }
        
        Bone.values.forEach { bone ->
            val boneItemId = getRSCM(bone.item)
            val itemDef = getItem(boneItemId)
            
            // Debug: Print item options
            println("BonePlugin: Registering ${bone.item} (ID: $boneItemId)")
            println("BonePlugin: Interface options: ${itemDef.interfaceOptions.joinToString(", ")}")
            
            // Try to find "Bury" option in the item's interface options
            val buryOptionIndex = itemDef.interfaceOptions.indexOfFirst { 
                it?.lowercase()?.contains("bury") == true 
            }
            
            if (buryOptionIndex != -1) {
                // Found "Bury" option, use it
                val optionToUse = buryOptionIndex + 1
                println("BonePlugin: Found 'Bury' at option index $buryOptionIndex (option $optionToUse)")
                handleBuryBone(bone, boneItemId, optionToUse)
            } else {
                // Try option 1 first (most common for "Bury" in OSRS)
                println("BonePlugin: 'Bury' option not found, trying option 1")
                handleBuryBone(bone, boneItemId, 1)
            }
        }
    }
}

