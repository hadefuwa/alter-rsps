package org.alter.plugins.content.mechanics.prayer

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.cfg.Animation
import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.BURY_BONE_DELAY
import org.alter.game.plugin.*
import org.alter.rscm.RSCM.getRSCM

class PrayersPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        onPlayerDeath {
            Prayers.deactivateAll(player)
        }

        /**
         * Deactivate all prayers on log out.
         */
        onLogout {
            Prayers.deactivateAll(player)
        }

        /**
         * Activate prayers.
         */
        Prayer.values.forEach { prayer ->
            onButton(interfaceId = 541, component = prayer.child) {
                player.queue {
                    Prayers.toggle(player, this, prayer)
                }
            }
        }

        /**
         * Prayer drain.
         */
        onLogin {
            player.timers[Prayers.PRAYER_DRAIN] = 1
        }

        onTimer(Prayers.PRAYER_DRAIN) {
            player.timers[Prayers.PRAYER_DRAIN] = 1
            Prayers.drainPrayer(player)
        }

        /**
         * Toggle quick-prayers.
         */
        onButton(interfaceId = 160, component = 19) {
            val opt = player.getInteractingOption()
            Prayers.toggleQuickPrayers(player, opt)
        }

        /**
         * Select quick-prayer.
         */
        onButton(interfaceId = 77, component = 4) {
            val slot = player.getInteractingSlot()
            val prayer = Prayer.values.firstOrNull { prayer -> prayer.quickPrayerSlot == slot } ?: return@onButton
            Prayers.selectQuickPrayer(this, prayer)
        }

        /**
         * Accept selected quick-prayer.
         */
        onButton(interfaceId = 77, component = 5) {
            player.openInterface(InterfaceDestination.PRAYER)
        }
        
        /**
         * Bone burying - bind to option 1 for all bone types
         */
        // Regular bones: XP 4.5
        onItemOption(item = "item.bones", option = 1) {
            buryBone(player, "item.bones", 4.5)
        }
        onItemOption(item = "item.bones", option = 2) {
            buryAllBones(player, "item.bones", 4.5)
        }
        
        // Burnt bones: XP 4.5
        onItemOption(item = "item.burnt_bones", option = 1) {
            buryBone(player, "item.burnt_bones", 4.5)
        }
        
        // Bat bones: XP 5.3
        onItemOption(item = "item.bat_bones", option = 1) {
            buryBone(player, "item.bat_bones", 5.3)
        }
        
        // Big bones: XP 15
        onItemOption(item = "item.big_bones", option = 1) {
            buryBone(player, "item.big_bones", 15.0)
        }
        onItemOption(item = "item.big_bones", option = 2) {
            buryAllBones(player, "item.big_bones", 15.0)
        }
        
        // Baby dragon bones: XP 30
        onItemOption(item = "item.babydragon_bones", option = 1) {
            buryBone(player, "item.babydragon_bones", 30.0)
        }
        
        // Dragon bones: XP 72
        onItemOption(item = "item.dragon_bones", option = 1) {
            buryBone(player, "item.dragon_bones", 72.0)
        }
        onItemOption(item = "item.dragon_bones", option = 2) {
            buryAllBones(player, "item.dragon_bones", 72.0)
        }
        
        // Wolf bones: XP 4.5
        onItemOption(item = "item.wolf_bones", option = 1) {
            buryBone(player, "item.wolf_bones", 4.5)
        }
        
        // Shaikahan bones: XP 25
        onItemOption(item = "item.shaikahan_bones", option = 1) {
            buryBone(player, "item.shaikahan_bones", 25.0)
        }
        
        // Jogre bones: XP 15
        onItemOption(item = "item.jogre_bones", option = 1) {
            buryBone(player, "item.jogre_bones", 15.0)
        }
        
        // Burnt jogre bones: XP 15
        onItemOption(item = "item.burnt_jogre_bones", option = 1) {
            buryBone(player, "item.burnt_jogre_bones", 15.0)
        }
        
        // Zogre bones: XP 22.5
        onItemOption(item = "item.zogre_bones", option = 1) {
            buryBone(player, "item.zogre_bones", 22.5)
        }
        
        // Fayrg bones: XP 84
        onItemOption(item = "item.fayrg_bones", option = 1) {
            buryBone(player, "item.fayrg_bones", 84.0)
        }
        
        // Raurg bones: XP 96
        onItemOption(item = "item.raurg_bones", option = 1) {
            buryBone(player, "item.raurg_bones", 96.0)
        }
        
        // Ourg bones: XP 140
        onItemOption(item = "item.ourg_bones", option = 1) {
            buryBone(player, "item.ourg_bones", 140.0)
        }
        
        // Dagannoth bones: XP 125
        onItemOption(item = "item.dagannoth_bones", option = 1) {
            buryBone(player, "item.dagannoth_bones", 125.0)
        }
        
        // Wyvern bones: XP 72
        onItemOption(item = "item.wyvern_bones", option = 1) {
            buryBone(player, "item.wyvern_bones", 72.0)
        }
        
        // Lava dragon bones: XP 85
        onItemOption(item = "item.lava_dragon_bones", option = 1) {
            buryBone(player, "item.lava_dragon_bones", 85.0)
        }
        
        // Superior dragon bones: XP 150
        onItemOption(item = "item.superior_dragon_bones", option = 1) {
            buryBone(player, "item.superior_dragon_bones", 150.0)
        }
        onItemOption(item = "item.superior_dragon_bones", option = 2) {
            buryAllBones(player, "item.superior_dragon_bones", 150.0)
        }
        
        // Wyrm bones: XP 50
        onItemOption(item = "item.wyrm_bones", option = 1) {
            buryBone(player, "item.wyrm_bones", 50.0)
        }
        
        // Drake bones: XP 80
        onItemOption(item = "item.drake_bones", option = 1) {
            buryBone(player, "item.drake_bones", 80.0)
        }
        
        // Hydra bones: XP 110
        onItemOption(item = "item.hydra_bones", option = 1) {
            buryBone(player, "item.hydra_bones", 110.0)
        }
        
        // Monkey bones: XP 5
        onItemOption(item = "item.monkey_bones", option = 1) {
            buryBone(player, "item.monkey_bones", 5.0)
        }
        
        // Small ninja monkey bones: XP 16
        onItemOption(item = "item.small_ninja_monkey_bones", option = 1) {
            buryBone(player, "item.small_ninja_monkey_bones", 16.0)
        }
        
        // Medium ninja monkey bones: XP 18
        onItemOption(item = "item.medium_ninja_monkey_bones", option = 1) {
            buryBone(player, "item.medium_ninja_monkey_bones", 18.0)
        }
        
        // Gorilla bones: XP 18
        onItemOption(item = "item.gorilla_bones", option = 1) {
            buryBone(player, "item.gorilla_bones", 18.0)
        }
        
        // Bearded gorilla bones: XP 18
        onItemOption(item = "item.bearded_gorilla_bones", option = 1) {
            buryBone(player, "item.bearded_gorilla_bones", 18.0)
        }
        
        // Small zombie monkey bones: XP 5
        onItemOption(item = "item.small_zombie_monkey_bones", option = 1) {
            buryBone(player, "item.small_zombie_monkey_bones", 5.0)
        }
        
        // Large zombie monkey bones: XP 5
        onItemOption(item = "item.large_zombie_monkey_bones", option = 1) {
            buryBone(player, "item.large_zombie_monkey_bones", 5.0)
        }
        
        // Wyrmling bones: XP 50
        onItemOption(item = "item.wyrmling_bones", option = 1) {
            buryBone(player, "item.wyrmling_bones", 50.0)
        }
        
        /**
         * Noted bones support - convert to unnoted and bury
         */
        // Regular bones noted: XP 4.5
        onItemOption(item = "item.bones_noted", option = 1) {
            buryNotedBone(player, "item.bones_noted", "item.bones", 4.5)
        }
        
        // Burnt bones noted: XP 4.5
        onItemOption(item = "item.burnt_bones_noted", option = 1) {
            buryNotedBone(player, "item.burnt_bones_noted", "item.burnt_bones", 4.5)
        }
        
        // Bat bones noted: XP 5.3
        onItemOption(item = "item.bat_bones_noted", option = 1) {
            buryNotedBone(player, "item.bat_bones_noted", "item.bat_bones", 5.3)
        }
        
        // Big bones noted: XP 15
        onItemOption(item = "item.big_bones_noted", option = 1) {
            buryNotedBone(player, "item.big_bones_noted", "item.big_bones", 15.0)
        }
        
        // Baby dragon bones noted: XP 30
        onItemOption(item = "item.babydragon_bones_noted", option = 1) {
            buryNotedBone(player, "item.babydragon_bones_noted", "item.babydragon_bones", 30.0)
        }
        
        // Dragon bones noted: XP 72
        onItemOption(item = "item.dragon_bones_noted", option = 1) {
            buryNotedBone(player, "item.dragon_bones_noted", "item.dragon_bones", 72.0)
        }
        
        // Wolf bones noted: XP 4.5
        onItemOption(item = "item.wolf_bones_noted", option = 1) {
            buryNotedBone(player, "item.wolf_bones_noted", "item.wolf_bones", 4.5)
        }
        
        // Shaikahan bones noted: XP 25
        onItemOption(item = "item.shaikahan_bones_noted", option = 1) {
            buryNotedBone(player, "item.shaikahan_bones_noted", "item.shaikahan_bones", 25.0)
        }
        
        // Jogre bones noted: XP 15
        onItemOption(item = "item.jogre_bones_noted", option = 1) {
            buryNotedBone(player, "item.jogre_bones_noted", "item.jogre_bones", 15.0)
        }
        
        // Zogre bones noted: XP 22.5
        onItemOption(item = "item.zogre_bones_noted", option = 1) {
            buryNotedBone(player, "item.zogre_bones_noted", "item.zogre_bones", 22.5)
        }
        
        // Fayrg bones noted: XP 84
        onItemOption(item = "item.fayrg_bones_noted", option = 1) {
            buryNotedBone(player, "item.fayrg_bones_noted", "item.fayrg_bones", 84.0)
        }
        
        // Raurg bones noted: XP 96
        onItemOption(item = "item.raurg_bones_noted", option = 1) {
            buryNotedBone(player, "item.raurg_bones_noted", "item.raurg_bones", 96.0)
        }
        
        // Ourg bones noted: XP 140
        onItemOption(item = "item.ourg_bones_noted", option = 1) {
            buryNotedBone(player, "item.ourg_bones_noted", "item.ourg_bones", 140.0)
        }
        
        // Dagannoth bones noted: XP 125
        onItemOption(item = "item.dagannoth_bones_noted", option = 1) {
            buryNotedBone(player, "item.dagannoth_bones_noted", "item.dagannoth_bones", 125.0)
        }
        
        // Wyvern bones noted: XP 72
        onItemOption(item = "item.wyvern_bones_noted", option = 1) {
            buryNotedBone(player, "item.wyvern_bones_noted", "item.wyvern_bones", 72.0)
        }
        
        // Lava dragon bones noted: XP 85
        onItemOption(item = "item.lava_dragon_bones_noted", option = 1) {
            buryNotedBone(player, "item.lava_dragon_bones_noted", "item.lava_dragon_bones", 85.0)
        }
        
        // Superior dragon bones noted: XP 150
        onItemOption(item = "item.superior_dragon_bones_noted", option = 1) {
            buryNotedBone(player, "item.superior_dragon_bones_noted", "item.superior_dragon_bones", 150.0)
        }
        
        // Wyrm bones noted: XP 50
        onItemOption(item = "item.wyrm_bones_noted", option = 1) {
            buryNotedBone(player, "item.wyrm_bones_noted", "item.wyrm_bones", 50.0)
        }
        
        // Drake bones noted: XP 80
        onItemOption(item = "item.drake_bones_noted", option = 1) {
            buryNotedBone(player, "item.drake_bones_noted", "item.drake_bones", 80.0)
        }
        
        // Hydra bones noted: XP 110
        onItemOption(item = "item.hydra_bones_noted", option = 1) {
            buryNotedBone(player, "item.hydra_bones_noted", "item.hydra_bones", 110.0)
        }
        
        // Monkey bones noted: XP 5
        onItemOption(item = "item.monkey_bones_noted", option = 1) {
            buryNotedBone(player, "item.monkey_bones_noted", "item.monkey_bones", 5.0)
        }
        
        // Wyrmling bones noted: XP 50
        onItemOption(item = "item.wyrmling_bones_noted", option = 1) {
            buryNotedBone(player, "item.wyrmling_bones_noted", "item.wyrmling_bones", 50.0)
        }
    }
    
    /**
     * Helper function to bury bones
     */
    private fun buryBone(player: Player, boneItem: String, xp: Double) {
        val inventorySlot = player.getInteractingItemSlot()
        val item = player.inventory[inventorySlot] ?: return
        
        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }
        
        // Check if player is already burying bones (delay)
        if (player.timers.has(BURY_BONE_DELAY)) {
            return
        }
        
        // Verify the item at the slot matches what we expect
        val expectedItemId = getRSCM(boneItem)
        if (item.id != expectedItemId) {
            return
        }
        
        player.queue {
            // Play animation and sound
            player.animate(Animation.BURY_BONE_ANIM)
            player.playSound(Sound.BURYING_BONE)
            
            // Wait for animation
            this.wait(cycles = 2)
            
            // Remove the bone from inventory
            val remove = player.inventory.remove(item = expectedItemId, amount = 1, beginSlot = inventorySlot, assureFullRemoval = false)
            if (remove.hasSucceeded() && remove.completed > 0) {
                // Add prayer experience
                player.addXp(Skills.PRAYER, xp)
                
                // Get bone name and display message
                val boneName = getItem(expectedItemId).name.lowercase()
                val bonePlural = if (boneName.endsWith("s")) boneName else "$boneName bones"
                player.message("You bury the $bonePlural.")
            } else {
                // Failed to remove bone - probably doesn't have it anymore
                player.message("You don't have any bones to bury.")
            }
            
            // Set delay timer
            player.timers[BURY_BONE_DELAY] = 3 // 3 game ticks (1.8 seconds)
        }
    }
    
    /**
     * Helper function to bury noted bones (converts them to unnoted first)
     */
    private fun buryNotedBone(player: Player, notedBoneItem: String, unnotedBoneItem: String, xp: Double) {
        val inventorySlot = player.getInteractingItemSlot()
        val item = player.inventory[inventorySlot] ?: return
        
        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }
        
        // Check if player is already burying bones (delay)
        if (player.timers.has(BURY_BONE_DELAY)) {
            return
        }
        
        // Verify the item at the slot matches what we expect
        val expectedNotedItemId = getRSCM(notedBoneItem)
        if (item.id != expectedNotedItemId) {
            return
        }
        
        // Check if player has at least one noted bone
        if (item.amount < 1) {
            return
        }
        
        player.queue {
            // Play animation and sound
            player.animate(Animation.BURY_BONE_ANIM)
            player.playSound(Sound.BURYING_BONE)
            
            // Wait for animation
            this.wait(cycles = 2)
            
            // Remove one noted bone from inventory
            val remove = player.inventory.remove(item = expectedNotedItemId, amount = 1, beginSlot = inventorySlot, assureFullRemoval = false)
            if (remove.hasSucceeded() && remove.completed > 0) {
                // Add prayer experience
                player.addXp(Skills.PRAYER, xp)
                
                // Get bone name and display message
                val unnotedItemId = getRSCM(unnotedBoneItem)
                val boneName = getItem(unnotedItemId).name.lowercase()
                val bonePlural = if (boneName.endsWith("s")) boneName else "$boneName bones"
                player.message("You bury the $bonePlural.")
            } else {
                // Failed to remove bone - probably doesn't have it anymore
                player.message("You don't have any bones to bury.")
            }
            
            // Set delay timer
            player.timers[BURY_BONE_DELAY] = 3 // 3 game ticks (1.8 seconds)
        }
    }
    
    /**
     * Helper function to bury all bones of the specified type in inventory
     */
    private fun buryAllBones(player: Player, boneItem: String, xpPerBone: Double) {
        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }
        
        // Check if player is already burying bones (delay)
        if (player.timers.has(BURY_BONE_DELAY)) {
            return
        }
        
        val expectedItemId = getRSCM(boneItem)
        val totalBones = player.inventory.getItemCount(expectedItemId)
        
        if (totalBones <= 0) {
            player.message("You don't have any bones to bury.")
            return
        }
        
        player.queue {
            var bonesBuried = 0
            
            while (player.inventory.contains(expectedItemId) && bonesBuried < totalBones) {
                // Play animation and sound
                player.animate(Animation.BURY_BONE_ANIM)
                player.playSound(Sound.BURYING_BONE)
                
                // Wait for animation (shorter delay for mass burying)
                this.wait(cycles = 2)
                
                // Remove one bone from inventory (any slot)
                val remove = player.inventory.remove(item = expectedItemId, amount = 1, assureFullRemoval = false)
                if (remove.hasSucceeded() && remove.completed > 0) {
                    // Add prayer experience
                    player.addXp(Skills.PRAYER, xpPerBone)
                    bonesBuried++
                } else {
                    // Failed to remove bone - probably doesn't have it anymore
                    break
                }
            }
            
            if (bonesBuried > 0) {
                // Get bone name and display message
                val boneName = getItem(expectedItemId).name.lowercase()
                val bonePlural = if (boneName.endsWith("s")) boneName else "$boneName bones"
                
                if (bonesBuried == 1) {
                    player.message("You bury the $bonePlural.")
                } else {
                    player.message("You bury $bonesBuried $bonePlural.")
                }
            }
            
            // Set delay timer
            player.timers[BURY_BONE_DELAY] = 3 // 3 game ticks (1.8 seconds)
        }
    }
}
