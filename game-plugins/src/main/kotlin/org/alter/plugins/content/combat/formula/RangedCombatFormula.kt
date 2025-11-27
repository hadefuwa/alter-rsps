package org.alter.plugins.content.combat.formula

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.CombatConfigs
import org.alter.plugins.content.mechanics.prayer.Prayer
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.plugins.content.skills.slayer.Slayer
import dev.openrune.cache.CacheManager.getNpc
import org.alter.rscm.RSCM.getRSCM

/**
 * @author Tom <rspsmods@gmail.com>
 */
object RangedCombatFormula : CombatFormula {
    private val BLACK_MASKS =
        arrayOf(
            "item.black_mask",
            "item.black_mask_1",
            "item.black_mask_2",
            "item.black_mask_3",
            "item.black_mask_4",
            "item.black_mask_5",
            "item.black_mask_6",
            "item.black_mask_7",
            "item.black_mask_8",
            "item.black_mask_9",
            "item.black_mask_10",
        )

    private val BLACK_MASKS_I =
        arrayOf(
            "item.black_mask_i",
            "item.black_mask_1_i",
            "item.black_mask_2_i",
            "item.black_mask_3_i",
            "item.black_mask_4_i",
            "item.black_mask_5_i",
            "item.black_mask_6_i",
            "item.black_mask_7_i",
            "item.black_mask_8_i",
            "item.black_mask_9_i",
            "item.black_mask_10_i",
        )

    private val SLAYER_HELMETS = arrayOf(
        "item.slayer_helmet",
        "item.slayer_helmet_i", 
        "item.black_slayer_helmet",
        "item.black_slayer_helmet_i",
        "item.green_slayer_helmet",
        "item.green_slayer_helmet_i",
        "item.red_slayer_helmet",
        "item.red_slayer_helmet_i",
        "item.purple_slayer_helmet",
        "item.purple_slayer_helmet_i",
        "item.turquoise_slayer_helmet",
        "item.turquoise_slayer_helmet_i",
        "item.hydra_slayer_helmet",
        "item.hydra_slayer_helmet_i",
        "item.twisted_slayer_helmet",
        "item.twisted_slayer_helmet_i",
        "item.purple_slayer_helmet_i_25185",
        "item.turquoise_slayer_helmet_i_25187",
        "item.hydra_slayer_helmet_i_25189",
        "item.twisted_slayer_helmet_i_25191",
        "item.purple_slayer_helmet_i_26678",
        "item.turquoise_slayer_helmet_i_26679",
        "item.hydra_slayer_helmet_i_26680",
        "item.twisted_slayer_helmet_i_26681"
    )

    private val RANGED_VOID = arrayOf("item.void_ranger_helm", "item.void_knight_top", "item.void_knight_robe", "item.void_knight_gloves")

    private val RANGED_ELITE_VOID =
        arrayOf("item.void_ranger_helm", "item.elite_void_top", "item.elite_void_robe", "item.void_knight_gloves")

    override fun getAccuracy(
        pawn: Pawn,
        target: Pawn,
        specialAttackMultiplier: Double,
    ): Double {
        val attack = getAttackRoll(pawn, target, specialAttackMultiplier)
        val defence = getDefenceRoll(pawn, target)

        val accuracy: Double
        if (attack > defence) {
            accuracy = 1.0 - (defence + 2.0) / (2.0 * (attack + 1.0))
        } else {
            accuracy = attack / (2.0 * (defence + 1))
        }
        return accuracy
    }

    override fun getMaxHit(
        pawn: Pawn,
        target: Pawn,
        specialAttackMultiplier: Double,
        specialPassiveMultiplier: Double,
    ): Int {
        val a =
            if (pawn is Player) {
                getEffectiveRangedLevel(pawn)
            } else if (pawn is Npc) {
                getEffectiveRangedLevel(pawn)
            } else {
                0.0
            }
        val b = getEquipmentRangedBonus(pawn)

        var base = Math.floor(0.5 + a * (b + 64.0) / 640.0).toInt()
        if (pawn is Player) {
            base = applyRangedSpecials(pawn, target, base, specialAttackMultiplier, specialPassiveMultiplier)
        } else if (pawn is Npc && target is Player) {
            // Apply protection prayer reduction for NPC attacks, with 50% bypass chance for wilderness NPCs
            var hit = base.toDouble()
            if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
                val isWildernessNpc = pawn.tile.getWildernessLevel() > 0
                val isRevenant = pawn.def.name.lowercase().contains("revenant") || 
                                (pawn.tile.z >= 10000 && pawn.tile.z <= 10300 && pawn.tile.x >= 3100 && pawn.tile.x <= 3300)
                // 50% chance to bypass protection prayer for wilderness NPCs and revenants
                if ((!isWildernessNpc && !isRevenant) || !pawn.world.chance(1, 2)) {
                    hit *= 0.6
                    hit = Math.floor(hit)
                }
            }
            // Apply damage multiplier for NPCs (e.g., revenants, wilderness NPCs)
            hit *= getDamageDealMultiplier(pawn)
            // Apply 6x base damage multiplier for revenants
            val isRevenant = pawn.def.name.lowercase().contains("revenant") || 
                            (pawn.tile.z >= 10000 && pawn.tile.z <= 10300 && pawn.tile.x >= 3100 && pawn.tile.x <= 3300)
            if (isRevenant) {
                hit *= 6.0
            }
            hit = Math.floor(hit)

            // Apply damage take multiplier (for items like Bracelet of Ethereum)
            hit *= getDamageTakeMultiplier(target)
            hit = Math.floor(hit)

            // Cap damage at 30 if target is wearing Bracelet of Ethereum and attacker is Revenant
            if (isRevenant && target.hasEquipped(EquipmentType.GLOVES, "item.bracelet_of_ethereum")) {
                 if (hit > 30.0) {
                     hit = 30.0
                 }
            }

            base = hit.toInt()
        }
        return base
    }

    private fun getAttackRoll(
        pawn: Pawn,
        target: Pawn,
        specialAttackMultiplier: Double,
    ): Int {
        val a =
            if (pawn is Player) {
                getEffectiveAttackLevel(pawn)
            } else if (pawn is Npc) {
                getEffectiveAttackLevel(pawn)
            } else {
                0.0
            }
        var b = getEquipmentAttackBonus(pawn)

        // Ensure throwing weapons have a minimum attack bonus for reasonable accuracy
        // This prevents extremely low accuracy when using throwing weapons with no other equipment
        // Negative attack bonuses can occur from certain equipment, so we ensure at least 0 bonus
        if (pawn is Player && pawn.hasWeaponType(WeaponType.THROWN) && b < 0) {
            b = 0.0
        }

        var maxRoll = a * (b + 64.0)
        
        // Ensure minimum attack roll for throwing weapons to prevent extremely low accuracy
        // This ensures reasonable base accuracy even with minimal equipment and low levels
        if (pawn is Player && pawn.hasWeaponType(WeaponType.THROWN) && a > 0) {
            val minAttackRoll = a * 64.0 // Minimum roll with 0 attack bonus
            maxRoll = Math.max(maxRoll, minAttackRoll)
        }
        if (pawn is Player) {
            maxRoll = applyAttackSpecials(pawn, target, maxRoll, specialAttackMultiplier)
        }
        // Apply accuracy multiplier for wilderness NPCs and revenants
        // Revenants: 15x accuracy (tripled from 5x)
        // Other wilderness NPCs: 10x accuracy
        if (pawn is Npc) {
            val isWildernessNpc = pawn.tile.getWildernessLevel() > 0
            val isRevenant = pawn.def.name.lowercase().contains("revenant") || 
                            (pawn.tile.z >= 10000 && pawn.tile.z <= 10300 && pawn.tile.x >= 3100 && pawn.tile.x <= 3300)
            if (isRevenant) {
                maxRoll *= 15.0
            } else if (isWildernessNpc) {
                maxRoll *= 10.0
            }
        }
        return maxRoll.toInt()
    }

    private fun getDefenceRoll(
        pawn: Pawn,
        target: Pawn,
    ): Int {
        val a =
            if (pawn is Player) {
                getEffectiveDefenceLevel(pawn)
            } else if (pawn is Npc) {
                getEffectiveDefenceLevel(pawn)
            } else {
                0.0
            }
        val b = getEquipmentDefenceBonus(target)

        var maxRoll = a * (b + 64.0)
        maxRoll = applyDefenceSpecials(target, maxRoll)
        return maxRoll.toInt()
    }

    private fun applyRangedSpecials(
        player: Player,
        target: Pawn,
        base: Int,
        specialAttackMultiplier: Double,
        specialPassiveMultiplier: Double,
    ): Int {
        var hit = base.toDouble()

        hit *= getEquipmentMultiplier(player, target)
        hit = Math.floor(hit)

        if (specialAttackMultiplier == 1.0) {
            val multiplier =
                when {
                    player.hasEquipped(EquipmentType.WEAPON, "item.dragon_hunter_crossbow") && isDragon(target) -> 1.3
                    player.hasEquipped(EquipmentType.WEAPON, "item.twisted_bow") && target.entityType.isNpc -> {
                        // TODO: cap inside Chambers of Xeric is 350
                        val cap = 250.0
                        val magic =
                            when (target) {
                                is Player -> target.getSkills().getCurrentLevel(Skills.MAGIC)
                                is Npc -> target.stats.getCurrentLevel(NpcSkills.MAGIC)
                                else -> throw IllegalStateException("Invalid pawn type. [$target]")
                            }
                        val modifier =
                            Math.min(
                                cap,
                                250.0 + (((magic * 3.0) - 14.0) / 100.0) - (Math.pow((((magic * 3.0) / 10.0) - 140.0), 2.0) / 100.0),
                            )
                        modifier
                    }
                    else -> 1.0
                }
            hit *= multiplier
            hit = Math.floor(hit)
        } else {
            hit *= specialAttackMultiplier
            hit = Math.floor(hit)
        }

        if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)) {
            hit *= 0.6
            hit = Math.floor(hit)
        }

        if (specialPassiveMultiplier == 1.0) {
            hit = applyPassiveMultiplier(player, target, hit)
            hit = Math.floor(hit)
        } else {
            hit *= specialPassiveMultiplier
            hit = Math.floor(hit)
        }

        // Wilderness weapon bonus: 200% damage increase (4.0x) in wilderness against wilderness NPCs/revenants
        if (target is Npc && isWildernessWeaponBonus(player, target)) {
            hit *= 4.0
            hit = Math.floor(hit)
        }

        hit *= getDamageDealMultiplier(player)
        hit = Math.floor(hit)

        hit *= getDamageTakeMultiplier(target)
        hit = Math.floor(hit)
        
        // Apply Kalphite Queen form-based damage reduction
        if (target is Npc) {
            hit *= getKQDamageMultiplier(target, CombatStyle.RANGED)
            hit = Math.floor(hit)
        }
        
        // Apply Corporeal Beast damage reduction (50% for all non-spear/hasta weapons)
        if (target is Npc && pawn is Player) {
            hit *= getCorporealBeastDamageMultiplier(pawn, target)
            hit = Math.floor(hit)
        }

        return hit.toInt()
    }

    private fun applyAttackSpecials(
        player: Player,
        target: Pawn,
        base: Double,
        specialAttackMultiplier: Double,
    ): Double {
        var hit = base

        hit *= getEquipmentMultiplier(player, target)
        hit = Math.floor(hit)

        if (specialAttackMultiplier == 1.0) {
            val multiplier =
                when {
                    player.hasEquipped(EquipmentType.WEAPON, "item.dragon_hunter_crossbow") && isDragon(target) -> 1.3
                    player.hasEquipped(EquipmentType.WEAPON, "item.twisted_bow") && target.entityType.isNpc -> {
                        // TODO: cap inside Chambers of Xeric is 250
                        val cap = 140.0
                        val magic =
                            when (target) {
                                is Player -> target.getSkills().getCurrentLevel(Skills.MAGIC)
                                is Npc -> target.stats.getCurrentLevel(NpcSkills.MAGIC)
                                else -> throw IllegalStateException("Invalid pawn type. [$target]")
                            }
                        val modifier =
                            Math.min(
                                cap,
                                140.0 + (((magic * 3.0) - 10.0) / 100.0) - (Math.pow((((magic * 3.0) / 10.0) - 100.0), 2.0) / 100.0),
                            )
                        modifier
                    }
                    else -> 1.0
                }
            hit *= multiplier
            hit = Math.floor(hit)
        } else {
            hit *= specialAttackMultiplier
            hit = Math.floor(hit)
        }

        // Wilderness weapon bonus: 200% accuracy increase (4.0x) in wilderness against wilderness NPCs/revenants
        if (target is Npc && isWildernessWeaponBonus(player, target)) {
            hit *= 4.0
            hit = Math.floor(hit)
        }

        return hit
    }

    private fun applyDefenceSpecials(
        target: Pawn,
        base: Double,
    ): Double {
        var hit = base

        if (target is Player && isWearingTorag(target) && target.hasEquipped(EquipmentType.AMULET, "item.amulet_of_the_damned_full")) {
            val lost = (target.getMaxHp() - target.getCurrentHp()) / 100.0
            val max = target.getMaxHp() / 100.0
            hit *= (1.0 + (lost * max))
            hit = Math.floor(hit)
        }

        return hit
    }

    private fun getEquipmentRangedBonus(pawn: Pawn): Double =
        when (pawn) {
            is Player -> pawn.getRangedStrengthBonus().toDouble()
            is Npc -> pawn.getRangedStrengthBonus().toDouble()
            else -> throw IllegalArgumentException("Invalid pawn type. $pawn")
        }

    private fun getEquipmentAttackBonus(pawn: Pawn): Double {
        return pawn.getBonus(BonusSlot.ATTACK_RANGED).toDouble()
    }

    private fun getEquipmentDefenceBonus(target: Pawn): Double {
        return target.getBonus(BonusSlot.DEFENCE_RANGED).toDouble()
    }

    private fun getEffectiveRangedLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.RANGED) * getPrayerRangedMultiplier(player))

        effectiveLevel +=
            when (CombatConfigs.getAttackStyle(player)) {
                AttackStyle.ACCURATE -> 3.0
                else -> 0.0
            }

        effectiveLevel += 8.0

        if (player.hasEquipped(RANGED_VOID)) {
            effectiveLevel *= 1.10
            effectiveLevel = Math.floor(effectiveLevel)
        } else if (player.hasEquipped(RANGED_ELITE_VOID)) {
            effectiveLevel *= 1.125
            effectiveLevel = Math.floor(effectiveLevel)
        }

        return Math.floor(effectiveLevel)
    }

    private fun getEffectiveAttackLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.RANGED) * getPrayerAttackMultiplier(player))

        effectiveLevel +=
            when (CombatConfigs.getAttackStyle(player)) {
                AttackStyle.ACCURATE -> 3.0
                else -> 0.0
            }

        effectiveLevel += 8.0

        if (player.hasEquipped(RANGED_VOID) || player.hasEquipped(RANGED_ELITE_VOID)) {
            effectiveLevel *= 1.10
            effectiveLevel = Math.floor(effectiveLevel)
        }

        return Math.floor(effectiveLevel)
    }

    private fun getEffectiveDefenceLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.DEFENCE) * getPrayerDefenceMultiplier(player))

        effectiveLevel +=
            when (CombatConfigs.getAttackStyle(player)) {
                AttackStyle.DEFENSIVE -> 3.0
                AttackStyle.CONTROLLED -> 1.0
                AttackStyle.LONG_RANGE -> 3.0
                else -> 0.0
            }

        effectiveLevel += 8.0

        return Math.floor(effectiveLevel)
    }

    private fun getEffectiveRangedLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.RANGED).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEffectiveAttackLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.RANGED).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEffectiveDefenceLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.DEFENCE).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getPrayerRangedMultiplier(player: Player): Double =
        when {
            Prayers.isActive(player, Prayer.SHARP_EYE) -> 1.05
            Prayers.isActive(player, Prayer.HAWK_EYE) -> 1.10
            Prayers.isActive(player, Prayer.EAGLE_EYE) -> 1.15
            Prayers.isActive(player, Prayer.RIGOUR) -> 1.23
            else -> 1.0
        }

    private fun getPrayerAttackMultiplier(player: Player): Double =
        when {
            Prayers.isActive(player, Prayer.SHARP_EYE) -> 1.05
            Prayers.isActive(player, Prayer.HAWK_EYE) -> 1.10
            Prayers.isActive(player, Prayer.EAGLE_EYE) -> 1.15
            Prayers.isActive(player, Prayer.RIGOUR) -> 1.20
            else -> 1.0
        }

    private fun getPrayerDefenceMultiplier(player: Player): Double =
        when {
            Prayers.isActive(player, Prayer.THICK_SKIN) -> 1.05
            Prayers.isActive(player, Prayer.ROCK_SKIN) -> 1.10
            Prayers.isActive(player, Prayer.STEEL_SKIN) -> 1.15
            Prayers.isActive(player, Prayer.CHIVALRY) -> 1.20
            Prayers.isActive(player, Prayer.PIETY) -> 1.25
            Prayers.isActive(player, Prayer.RIGOUR) -> 1.25
            Prayers.isActive(player, Prayer.AUGURY) -> 1.25
            else -> 1.0
        }

    private fun getEquipmentMultiplier(player: Player, target: Pawn? = null): Double =
        when {
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet") -> 7.0 / 6.0
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet_e") -> 1.2
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amuleti") -> 1.15
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amuletei") -> 1.2
            player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice") && isRevenantCaves(player.tile) -> 1.2
            // TODO: this should only apply when target is slayer task?
            player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS) -> 7.0 / 6.0
            player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS_I) -> 1.15
            player.hasEquipped(EquipmentType.HEAD, *SLAYER_HELMETS) && target is Npc && isOnSlayerTaskFor(player, target) -> 1.5 // 50% damage bonus
            else -> 1.0
        }

    private fun isRevenantCaves(tile: org.alter.game.model.Tile): Boolean {
        return tile.z >= 10000 && tile.z <= 10300 && tile.x >= 3100 && tile.x <= 3300
    }
    
    /**
     * Checks if a player has a slayer task for the given NPC
     * @param player The player to check
     * @param npc The NPC that is being fought
     * @return true if the player has a slayer task for this NPC type, false otherwise
     */
    private fun isOnSlayerTaskFor(player: Player, npc: Npc): Boolean {
        val taskNpcId = player.attr[Slayer.SLAYER_TASK_ATTR] ?: return false
        
        // Get the task NPC definition to compare names
        val taskNpcDef = try {
            getNpc(taskNpcId)
        } catch (e: Exception) {
            // If we can't get the task NPC definition, just compare IDs
            null
        }
        
        // Check if the target NPC matches the assigned NPC ID
        // Also check by name to handle NPC variants (e.g., crawling_hand_448 vs crawling_hand_453)
        val idMatches = npc.id == taskNpcId
        val nameMatches = taskNpcDef != null && npc.name.lowercase() == taskNpcDef.name.lowercase()
        
        // Special case: If task is a TzHaar NPC, allow any TzHaar NPC to count
        val tzhaarMatches = if (taskNpcDef != null) {
            val taskNameLower = taskNpcDef.name.lowercase()
            val targetNameLower = npc.name.lowercase()
            // Check if both are TzHaar NPCs (name contains "tzhaar")
            (taskNameLower.contains("tzhaar") || taskNameLower.contains("tz-haar")) &&
            (targetNameLower.contains("tzhaar") || targetNameLower.contains("tz-haar"))
        } else {
            false
        }
        
        return idMatches || nameMatches || tzhaarMatches
    }

    private fun applyPassiveMultiplier(
        player: Player,
        target: Pawn,
        base: Double,
    ): Double {
        when {
            player.hasWeaponType(WeaponType.CROSSBOW) && player.attr.has(Combat.BOLT_ENCHANTMENT_EFFECT) -> {
                val dragonstone =
                    player.hasEquipped(
                        EquipmentType.AMMO,
                        "item.dragonstone_bolts",
                        "item.dragonstone_bolts_e",
                        "item.dragonstone_dragon_bolts",
                        "item.dragonstone_dragon_bolts_e",
                    )
                val opal =
                    player.hasEquipped(
                        EquipmentType.AMMO,
                        "item.opal_bolts",
                        "item.opal_bolts_e",
                        "item.opal_dragon_bolts",
                        "item.opal_dragon_bolts_e",
                    )
                val pearl =
                    player.hasEquipped(
                        EquipmentType.AMMO,
                        "item.pearl_bolts",
                        "item.pearl_bolts_e",
                        "item.pearl_dragon_bolts",
                        "item.pearl_dragon_bolts_e",
                    )

                when {
                    dragonstone -> return base + Math.floor(player.getSkills().getCurrentLevel(Skills.RANGED) / 5.0)
                    opal -> return base + Math.floor(player.getSkills().getCurrentLevel(Skills.RANGED) / 10.0)
                    pearl ->
                        return base +
                            Math.floor(
                                player.getSkills().getCurrentLevel(Skills.RANGED) / (if (isFiery(target)) 15.0 else 20.0),
                            )
                }
            }
        }
        return base
    }

    private fun getDamageDealMultiplier(pawn: Pawn): Double = pawn.attr[Combat.DAMAGE_DEAL_MULTIPLIER] ?: 1.0

    private fun getDamageTakeMultiplier(pawn: Pawn): Double = pawn.attr[Combat.DAMAGE_TAKE_MULTIPLIER] ?: 1.0
    
    /**
     * Get Corporeal Beast damage multiplier - 50% reduction for all non-spear/hasta weapons
     * (Ranged and Magic always get 50% reduction since they don't use spears/hastae)
     */
    private fun getCorporealBeastDamageMultiplier(player: Player, target: Npc): Double {
        // Check if this is the Corporeal Beast
        val isCorporealBeast = target.id == getRSCM("npc.corporeal_beast") || 
                              target.def.name.lowercase().contains("corporeal beast")
        if (!isCorporealBeast) return 1.0
        
        // Ranged and Magic always get 50% reduction (they don't use spears/hastae)
        return 0.5
    }
    
    /**
     * Get Kalphite Queen damage multiplier based on form and attack style
     */
    private fun getKQDamageMultiplier(npc: Npc, attackStyle: CombatStyle): Double {
        // Check if this is a Kalphite Queen
        val isKQ = npc.id == 963 || npc.id == 964 || npc.def.name.lowercase().contains("kalphite queen")
        if (!isKQ) return 1.0
        
        // Try to get the form from the phase plugin
        val isForm2 = try {
            org.alter.plugins.content.npcs.kalphitequeen.KalphiteQueenPhasePlugin.isForm2(npc)
        } catch (e: Exception) {
            // Fallback to ID check if plugin not loaded
            npc.id == 964
        }
        
        // Both forms reduce Ranged damage
        return when (attackStyle) {
            CombatStyle.RANGED -> 0.25
            else -> 1.0
        }
    }

    private fun isDragon(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.DRACONIC)
        } else {
            false
        }
    }

    private fun isFiery(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.FIERY)
        } else {
            false
        }
    }

    private fun isWearingTorag(player: Player): Boolean {
        return player.hasEquipped(
            EquipmentType.HEAD,
            "item.torags_helm",
            "item.torags_helm_25",
            "item.torags_helm_50",
            "item.torags_helm_75",
            "item.torags_helm_100",
        ) &&
            player.hasEquipped(
                EquipmentType.WEAPON,
                "item.torags_hammers",
                "item.torags_hammers_25",
                "item.torags_hammers_50",
                "item.torags_hammers_75",
                "item.torags_hammers_100",
            ) &&
            player.hasEquipped(
                EquipmentType.CHEST,
                "item.torags_platebody",
                "item.torags_platebody_25",
                "item.torags_platebody_50",
                "item.torags_platebody_75",
                "item.torags_platebody_100",
            ) &&
            player.hasEquipped(
                EquipmentType.LEGS,
                "item.torags_platelegs",
                "item.torags_platelegs_25",
                "item.torags_platelegs_50",
                "item.torags_platelegs_75",
                "item.torags_platelegs_100",
            )
    }

    /**
     * Check if wilderness weapon bonus applies (100% damage/accuracy bonus)
     * Conditions:
     * 1. Player must be in wilderness or Revenant Caves
     * 2. Player must have a wilderness weapon equipped (Craw's Bow or Webweaver Bow for ranged)
     * 3. Target must be a wilderness NPC or revenant
     */
    private fun isWildernessWeaponBonus(player: Player, target: Npc): Boolean {
        // Check if player is in wilderness or Revenant Caves
        val playerWildernessLevel = player.tile.getWildernessLevel()
        val playerInRevenantCaves = player.tile.z >= 10000 && player.tile.z <= 10300 && 
                                   player.tile.x >= 3100 && player.tile.x <= 3300
        
        if (playerWildernessLevel <= 0 && !playerInRevenantCaves) {
            return false
        }
        
        // Check if player has a wilderness ranged weapon equipped
        val hasWildernessWeapon = player.hasEquipped(
            EquipmentType.WEAPON,
            "item.craws_bow",
            "item.craws_bow_u",
            "item.webweaver_bow"
        )
        
        if (!hasWildernessWeapon) {
            return false
        }
        
        // Check if target is in wilderness or is a revenant
        val isWildernessNpc = target.tile.getWildernessLevel() > 0
        val isRevenant = target.def.name.lowercase().contains("revenant") || 
                        (target.tile.z >= 10000 && target.tile.z <= 10300 && target.tile.x >= 3100 && target.tile.x <= 3300)
        
        return isWildernessNpc || isRevenant
    }
}
