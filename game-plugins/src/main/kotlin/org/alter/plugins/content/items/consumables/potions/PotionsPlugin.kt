package org.alter.plugins.content.items.consumables.potions

import dev.openrune.cache.CacheManager.getItem
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.POISON_TICKS_LEFT_ATTR
import org.alter.game.model.entity.Player
import org.alter.game.model.timer.*
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.mechanics.poison.Poison
import org.alter.rscm.RSCM.getRSCM

class PotionsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // List of all potions that can be drunk
        val potions = listOf(
            // Antipoison potions
            "item.antipoison4", "item.antipoison3", "item.antipoison2", "item.antipoison1",
            "item.superantipoison4", "item.superantipoison3", "item.superantipoison2", "item.superantipoison1",
            // Anti-venom potions
            "item.anti_venom4", "item.anti_venom3", "item.anti_venom2", "item.anti_venom1",
            "item.anti_venom_plus4", "item.anti_venom_plus3", "item.anti_venom_plus2", "item.anti_venom_plus1",
            // Prayer potions
            "item.prayer_potion4", "item.prayer_potion3", "item.prayer_potion2", "item.prayer_potion1",
            // Super restore potions
            "item.super_restore4", "item.super_restore3", "item.super_restore2", "item.super_restore1",
            // Saradomin brew
            "item.saradomin_brew4", "item.saradomin_brew3", "item.saradomin_brew2", "item.saradomin_brew1",
            // Ranging potions
            "item.ranging_potion4", "item.ranging_potion3", "item.ranging_potion2", "item.ranging_potion1",
            // Antifire potions
            "item.antifire_potion4", "item.antifire_potion3", "item.antifire_potion2", "item.antifire_potion1",
            // Super attack potions
            "item.super_attack4", "item.super_attack3", "item.super_attack2", "item.super_attack1",
            // Super strength potions
            "item.super_strength4", "item.super_strength3", "item.super_strength2", "item.super_strength1",
            // Super defence potions
            "item.super_defence4", "item.super_defence3", "item.super_defence2", "item.super_defence1",
            // Super combat potions
            "item.super_combat_potion4", "item.super_combat_potion3", "item.super_combat_potion2", "item.super_combat_potion1",
            // Magic potions
            "item.magic_potion4", "item.magic_potion3", "item.magic_potion2", "item.magic_potion1",
            // Agility potions
            "item.agility_potion4", "item.agility_potion3", "item.agility_potion2", "item.agility_potion1",
            // Strength potions
            "item.strength_potion4", "item.strength_potion3", "item.strength_potion2", "item.strength_potion1",
        )

        // Bind all potions to option 2 (Drink)
        potions.forEach { potion ->
            try {
                onItemOption(potion, 2) {
                    handleDrink(player, potion)
                }
            } catch (e: Exception) {
                // If option 2 doesn't exist, try option 1
                try {
                    onItemOption(potion, 1) {
                        handleDrink(player, potion)
                    }
                } catch (e2: Exception) {
                    // Potion not found or option doesn't exist, skip
                }
            }
        }
    }

    /**
     * Handler for drinking any potion
     */
    private fun handleDrink(player: Player, potion: String) {
        val inventorySlot = player.getInteractingItemSlot()
        val item = player.inventory[inventorySlot]

        if (item == null) {
            return
        }

        // Check if player can interact with items
        if (!player.lock.canItemInteract()) {
            return
        }

        // Check if player can drink (timer check)
        if (player.timers.has(POTION_DELAY)) {
            return
        }

        // Remove the potion from inventory
        val itemId = getRSCM(potion)
        val removeResult = player.inventory.remove(item = itemId, beginSlot = inventorySlot, assureFullRemoval = false)
        if (removeResult.hasSucceeded() && removeResult.completed > 0) {
            // Determine dose and replacement
            val (dose, replacement) = getDoseAndReplacement(potion)
            
            // Apply potion effects
            applyPotionEffect(player, potion, dose)
            
            // Add empty vial or lower dose potion
            if (replacement != null) {
                player.inventory.add(item = replacement, beginSlot = inventorySlot)
            }
            
            // Set potion delay timer
            player.timers[POTION_DELAY] = 3
            player.timers[ATTACK_DELAY] = 3
            
            val potionName = getItem(itemId).name
            player.message("You drink some of your ${potionName.lowercase()}.")
        }
    }

    /**
     * Get the dose number and replacement item for a potion
     */
    private fun getDoseAndReplacement(potion: String): Pair<Int, String?> {
        return when {
            potion.endsWith("4") -> Pair(4, potion.replace("4", "3"))
            potion.endsWith("3") -> Pair(3, potion.replace("3", "2"))
            potion.endsWith("2") -> Pair(2, potion.replace("2", "1"))
            potion.endsWith("1") -> Pair(1, "item.vial")
            else -> Pair(4, null)
        }
    }

    /**
     * Apply the effect of the potion
     */
    private fun applyPotionEffect(player: Player, potion: String, dose: Int) {
        when {
            // Antipoison potions - cure poison
            potion.contains("antipoison") && !potion.contains("super") -> {
                val ticksLeft = player.attr[POISON_TICKS_LEFT_ATTR] ?: 0
                if (ticksLeft > 0) {
                    player.attr[POISON_TICKS_LEFT_ATTR] = 0
                    player.timers.remove(POISON_TIMER)
                    Poison.setHpOrb(player, Poison.OrbState.NONE)
                    player.message("You are cured of poison.")
                }
            }
            // Super antipoison potions - cure poison and provide immunity
            potion.contains("superantipoison") -> {
                val ticksLeft = player.attr[POISON_TICKS_LEFT_ATTR] ?: 0
                if (ticksLeft > 0) {
                    player.attr[POISON_TICKS_LEFT_ATTR] = 0
                    player.timers.remove(POISON_TIMER)
                    Poison.setHpOrb(player, Poison.OrbState.NONE)
                    player.message("You are cured of poison.")
                }
                // TODO: Add poison immunity timer
            }
            // Anti-venom potions - cure venom
            potion.contains("anti_venom") -> {
                val ticksLeft = player.attr[POISON_TICKS_LEFT_ATTR] ?: 0
                if (ticksLeft < 0) { // Negative ticks indicate venom
                    player.attr[POISON_TICKS_LEFT_ATTR] = 0
                    player.timers.remove(POISON_TIMER)
                    Poison.setHpOrb(player, Poison.OrbState.NONE)
                    player.message("You are cured of venom.")
                }
            }
            // Prayer potions - restore prayer points
            potion.contains("prayer_potion") -> {
                val restore = 7 + (player.getSkills().getBaseLevel(Skills.PRAYER) / 4)
                val current = player.getSkills().getCurrentLevel(Skills.PRAYER)
                val max = player.getSkills().getBaseLevel(Skills.PRAYER)
                val newPrayer = (current + restore).coerceAtMost(max)
                player.getSkills().setCurrentLevel(Skills.PRAYER, newPrayer)
            }
            // Super restore potions - restore all stats and prayer
            potion.contains("super_restore") -> {
                val restore = 8 + (player.getSkills().getBaseLevel(Skills.PRAYER) / 4)
                val currentPrayer = player.getSkills().getCurrentLevel(Skills.PRAYER)
                val maxPrayer = player.getSkills().getBaseLevel(Skills.PRAYER)
                val newPrayer = (currentPrayer + restore).coerceAtMost(maxPrayer)
                player.getSkills().setCurrentLevel(Skills.PRAYER, newPrayer)
                
                // Restore all combat stats
                val combatSkills = listOf(
                    Skills.ATTACK,
                    Skills.STRENGTH,
                    Skills.DEFENCE,
                    Skills.RANGED,
                    Skills.MAGIC
                )
                combatSkills.forEach { skill ->
                    val current = player.getSkills().getCurrentLevel(skill)
                    val base = player.getSkills().getBaseLevel(skill)
                    if (current < base) {
                        val restoreAmount = (base / 4) + 8
                        val newLevel = (current + restoreAmount).coerceAtMost(base)
                        player.getSkills().setCurrentLevel(skill, newLevel)
                    }
                }
            }
            // Saradomin brew - boost defence, restore HP, lower attack/strength/magic
            potion.contains("saradomin_brew") -> {
                val baseDef = player.getSkills().getBaseLevel(Skills.DEFENCE)
                val currentDef = player.getSkills().getCurrentLevel(Skills.DEFENCE)
                val boost = (baseDef / 5) + 2
                player.getSkills().setCurrentLevel(Skills.DEFENCE, (currentDef + boost).coerceAtMost(baseDef + boost))
                
                val baseHp = player.getSkills().getBaseLevel(Skills.HITPOINTS)
                val currentHp = player.getSkills().getCurrentLevel(Skills.HITPOINTS)
                val heal = (baseHp / 5) + 2
                player.heal(heal, 0)
                
                // Lower attack, strength, magic
                listOf(Skills.ATTACK, Skills.STRENGTH, Skills.MAGIC).forEach { skill ->
                    val base = player.getSkills().getBaseLevel(skill)
                    val current = player.getSkills().getCurrentLevel(skill)
                    val reduction = (base / 10) + 2
                    player.getSkills().setCurrentLevel(skill, (current - reduction).coerceAtLeast((base * 9) / 10))
                }
            }
            // Ranging potions - boost ranged
            potion.contains("ranging_potion") -> {
                val base = player.getSkills().getBaseLevel(Skills.RANGED)
                val current = player.getSkills().getCurrentLevel(Skills.RANGED)
                val boost = 4 + (base / 10)
                player.getSkills().setCurrentLevel(Skills.RANGED, (current + boost).coerceAtMost(base + boost))
            }
            // Super attack potions - boost attack
            potion.contains("super_attack") -> {
                val base = player.getSkills().getBaseLevel(Skills.ATTACK)
                val current = player.getSkills().getCurrentLevel(Skills.ATTACK)
                val boost = 5 + (base / 5)
                player.getSkills().setCurrentLevel(Skills.ATTACK, (current + boost).coerceAtMost(base + boost))
            }
            // Super strength potions - boost strength
            potion.contains("super_strength") -> {
                val base = player.getSkills().getBaseLevel(Skills.STRENGTH)
                val current = player.getSkills().getCurrentLevel(Skills.STRENGTH)
                val boost = 5 + (base / 5)
                player.getSkills().setCurrentLevel(Skills.STRENGTH, (current + boost).coerceAtMost(base + boost))
            }
            // Super defence potions - boost defence
            potion.contains("super_defence") && !potion.contains("super_combat_potion") -> {
                val base = player.getSkills().getBaseLevel(Skills.DEFENCE)
                val current = player.getSkills().getCurrentLevel(Skills.DEFENCE)
                val boost = 5 + (base / 5)
                player.getSkills().setCurrentLevel(Skills.DEFENCE, (current + boost).coerceAtMost(base + boost))
            }
            // Super combat potions - boost attack, strength, and defence
            potion.contains("super_combat_potion") -> {
                // Attack boost
                run {
                    val base = player.getSkills().getBaseLevel(Skills.ATTACK)
                    val current = player.getSkills().getCurrentLevel(Skills.ATTACK)
                    val boost = 5 + (base / 5)
                    player.getSkills().setCurrentLevel(Skills.ATTACK, (current + boost).coerceAtMost(base + boost))
                }
                // Strength boost
                run {
                    val base = player.getSkills().getBaseLevel(Skills.STRENGTH)
                    val current = player.getSkills().getCurrentLevel(Skills.STRENGTH)
                    val boost = 5 + (base / 5)
                    player.getSkills().setCurrentLevel(Skills.STRENGTH, (current + boost).coerceAtMost(base + boost))
                }
                // Defence boost
                run {
                    val base = player.getSkills().getBaseLevel(Skills.DEFENCE)
                    val current = player.getSkills().getCurrentLevel(Skills.DEFENCE)
                    val boost = 5 + (base / 5)
                    player.getSkills().setCurrentLevel(Skills.DEFENCE, (current + boost).coerceAtMost(base + boost))
                }
            }
            // Magic potions - boost magic
            potion.contains("magic_potion") -> {
                val base = player.getSkills().getBaseLevel(Skills.MAGIC)
                val current = player.getSkills().getCurrentLevel(Skills.MAGIC)
                val boost = 4 + (base / 10)
                player.getSkills().setCurrentLevel(Skills.MAGIC, (current + boost).coerceAtMost(base + boost))
            }
            // Agility potions - boost agility
            potion.contains("agility_potion") -> {
                val base = player.getSkills().getBaseLevel(Skills.AGILITY)
                val current = player.getSkills().getCurrentLevel(Skills.AGILITY)
                val boost = 3
                player.getSkills().setCurrentLevel(Skills.AGILITY, (current + boost).coerceAtMost(base + boost))
            }
            // Strength potions - boost strength
            potion.contains("strength_potion") && !potion.contains("super") -> {
                val base = player.getSkills().getBaseLevel(Skills.STRENGTH)
                val current = player.getSkills().getCurrentLevel(Skills.STRENGTH)
                val boost = 3 + (base / 10)
                player.getSkills().setCurrentLevel(Skills.STRENGTH, (current + boost).coerceAtMost(base + boost))
            }
            // Antifire potions - provide fire immunity
            potion.contains("antifire") -> {
                // TODO: Add antifire immunity timer
                player.message("You are now immune to dragonfire.")
            }
        }
    }
}

