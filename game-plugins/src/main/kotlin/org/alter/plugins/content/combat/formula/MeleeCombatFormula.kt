package org.alter.plugins.content.combat.formula

import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.game.model.Tile
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.CombatConfigs
import org.alter.plugins.content.mechanics.prayer.Prayer
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.plugins.content.skills.slayer.Slayer
import dev.openrune.cache.CacheManager.getNpc
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.mechanics.doompoints.DoomPoints

/**
 * @author Tom <rspsmods@gmail.com>
 */
object MeleeCombatFormula : CombatFormula {

    private val BLACK_MASKS = arrayOf("item.black_mask",
            "item.black_mask_1", "item.black_mask_2", "item.black_mask_3", "item.black_mask_4",
            "item.black_mask_5", "item.black_mask_6", "item.black_mask_7", "item.black_mask_8",
            "item.black_mask_9", "item.black_mask_10")

    private val BLACK_MASKS_I = arrayOf("item.black_mask_i",
            "item.black_mask_1_i", "item.black_mask_2_i", "item.black_mask_3_i", "item.black_mask_4_i",
            "item.black_mask_5_i", "item.black_mask_6_i", "item.black_mask_7_i", "item.black_mask_8_i",
            "item.black_mask_9_i", "item.black_mask_10_i")

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

    private val MELEE_VOID = arrayOf("item.void_melee_helm", "item.void_knight_top", "item.void_knight_robe", "item.void_knight_gloves")

    private val MELEE_ELITE_VOID = arrayOf("item.void_melee_helm", "item.elite_void_top", "item.elite_void_robe", "item.void_knight_gloves")

    override fun getAccuracy(pawn: Pawn, target: Pawn, specialAttackMultiplier: Double): Double {
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

    override fun getMaxHit(pawn: Pawn, target: Pawn, specialAttackMultiplier: Double, specialPassiveMultiplier: Double): Int {
        val a = if (pawn is Player) getEffectiveStrengthLevel(pawn) else if (pawn is Npc) getEffectiveStrengthLevel(pawn) else 0.0
        val b = getEquipmentStrengthBonus(pawn)

        var base = Math.floor(0.5 + a * (b + 64.0) / 640.0).toInt()
        if (pawn is Player) {
            base = applyStrengthSpecials(pawn, target, base, specialAttackMultiplier, specialPassiveMultiplier)
        } else if (pawn is Npc && target is Player) {
            // Apply protection prayer reduction for NPC attacks, with 50% bypass chance for wilderness NPCs
            var hit = base.toDouble()
            if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
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

    private fun getAttackRoll(pawn: Pawn, target: Pawn, specialAttackMultiplier: Double): Int {
        val a = if (pawn is Player) getEffectiveAttackLevel(pawn) else if (pawn is Npc) getEffectiveAttackLevel(pawn) else 0.0
        val b = getEquipmentAttackBonus(pawn)

        var maxRoll = a * (b + 64.0)
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

    private fun getDefenceRoll(pawn: Pawn, target: Pawn): Int {
        val a = if (pawn is Player) getEffectiveDefenceLevel(pawn) else if (pawn is Npc) getEffectiveDefenceLevel(pawn) else 0.0
        val b = getEquipmentDefenceBonus(pawn, target)

        var maxRoll = a * (b + 64.0)
        maxRoll = applyDefenceSpecials(target, maxRoll)
        return maxRoll.toInt()
    }

    private fun applyStrengthSpecials(player: Player, target: Pawn, base: Int, specialAttackMultiplier: Double, specialPassiveMultiplier: Double): Int {
        var hit = base.toDouble()

        hit *= getEquipmentMultiplier(player, target)
        hit = Math.floor(hit)

        hit *= specialAttackMultiplier
        hit = Math.floor(hit)

        if (target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MELEE)) {
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

        // Bounty Hunter set bonus: Double damage on Boss Island with full set, 0 damage outside Boss Island
        if (target is Npc) {
            val isOnBossIsland = BountyHunterUtils.isOnBossIsland(player)
            val hasFullSet = BountyHunterUtils.hasFullBountyHunterSet(player)
            val hasAnyBountyItem = BountyHunterUtils.hasAnyBountyHunterItem(player)
            
            if (hasAnyBountyItem) {
                if (isOnBossIsland && hasFullSet) {
                    // Double damage on Boss Island with full set
                    hit *= 2.0
                    hit = Math.floor(hit)
                } else if (!isOnBossIsland) {
                    // Force 0 damage outside Boss Island
                    return 0
                }
            }
        }

        // Wilderness weapon bonus: 200% damage increase (4.0x) in wilderness against wilderness NPCs/revenants
        if (target is Npc && isWildernessWeaponBonus(player, target)) {
            hit *= 4.0
            hit = Math.floor(hit)
        }

        hit *= getDamageDealMultiplier(player)
        hit = Math.floor(hit)
        
        // Apply doom points damage multiplier perk
        val damageMultiplier = DoomPoints.getDamageMultiplier(player)
        if (damageMultiplier > 0) {
            hit *= (1.0 + damageMultiplier / 100.0)
            hit = Math.floor(hit)
        }

        hit *= getDamageTakeMultiplier(target)
        hit = Math.floor(hit)
        
        // Apply Kalphite Queen form-based damage reduction
        if (target is Npc) {
            hit *= getKQDamageMultiplier(target, CombatStyle.SLASH) // Melee uses SLASH as default
            hit = Math.floor(hit)
        }
        
        // Apply Corporeal Beast damage reduction (50% for non-spear/hasta weapons)
        if (target is Npc) {
            hit *= getCorporealBeastDamageMultiplier(player, target)
            hit = Math.floor(hit)
        }

        return hit.toInt()
    }

    private fun applyAttackSpecials(player: Player, target: Pawn, base: Double, specialAttackMultiplier: Double): Double {
        var hit = base

        hit *= getEquipmentMultiplier(player, target)
        hit = Math.floor(hit)

        hit *= (if (player.hasEquipped(EquipmentType.WEAPON, "item.arclight") && isDemon(target)) 1.7 else specialAttackMultiplier)
        hit = Math.floor(hit)

        // Bounty Hunter set bonus: Force 0 accuracy outside Boss Island
        if (target is Npc) {
            val isOnBossIsland = BountyHunterUtils.isOnBossIsland(player)
            val hasAnyBountyItem = BountyHunterUtils.hasAnyBountyHunterItem(player)
            
            if (hasAnyBountyItem && !isOnBossIsland) {
                // Force 0 accuracy outside Boss Island (will result in misses)
                return 0.0
            }
        }

        // Wilderness weapon bonus: 200% accuracy increase (4.0x) in wilderness against wilderness NPCs/revenants
        if (target is Npc && isWildernessWeaponBonus(player, target)) {
            hit *= 4.0
            hit = Math.floor(hit)
        }

        return hit
    }

    private fun applyDefenceSpecials(target: Pawn, base: Double): Double {
        var hit = base

        if (target is Player && isWearingTorag(target) && target.hasEquipped(EquipmentType.AMULET, "item.amulet_of_the_damned_full")) {
            val lost = (target.getMaxHp() - target.getCurrentHp()) / 100.0
            val max = target.getMaxHp() / 100.0
            hit *= (1.0 + (lost * max))
            hit = Math.floor(hit)
        }

        return hit
    }

    private fun getEquipmentStrengthBonus(pawn: Pawn): Double = when (pawn) {
        is Player -> pawn.getStrengthBonus().toDouble()
        is Npc -> pawn.getStrengthBonus().toDouble()
        else -> throw IllegalArgumentException("Invalid pawn type. $pawn")
    }

    private fun getEquipmentAttackBonus(pawn: Pawn): Double {
        val combatStyle = CombatConfigs.getCombatStyle(pawn)
        val bonus = when (combatStyle) {
            CombatStyle.STAB -> BonusSlot.ATTACK_STAB
            CombatStyle.SLASH -> BonusSlot.ATTACK_SLASH
            CombatStyle.CRUSH -> BonusSlot.ATTACK_CRUSH
            else -> return 0.0 // Non-melee combat styles (MAGIC, RANGED, etc.) have no melee attack bonus
        }
        return pawn.getBonus(bonus).toDouble()
    }

    private fun getEquipmentDefenceBonus(pawn: Pawn, target: Pawn): Double {
        val combatStyle = CombatConfigs.getCombatStyle(pawn)
        val bonus = when (combatStyle) {
            CombatStyle.STAB -> BonusSlot.DEFENCE_STAB
            CombatStyle.SLASH -> BonusSlot.DEFENCE_SLASH
            CombatStyle.CRUSH -> BonusSlot.DEFENCE_CRUSH
            else -> return 0.0 // Non-melee combat styles (MAGIC, RANGED, etc.) defend with general defence
        }
        return target.getBonus(bonus).toDouble()
    }

    private fun getEffectiveStrengthLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.STRENGTH) * getPrayerStrengthMultiplier(player))

        effectiveLevel += when (CombatConfigs.getAttackStyle(player)){
            AttackStyle.AGGRESSIVE -> 3.0
            AttackStyle.CONTROLLED -> 1.0
            else -> 0.0
        }

        effectiveLevel += 8.0

        if (player.hasEquipped(MELEE_VOID) || player.hasEquipped(MELEE_ELITE_VOID)) {
            effectiveLevel *= 1.10
            effectiveLevel = Math.floor(effectiveLevel)
        }

        return Math.floor(effectiveLevel)
    }

    private fun getEffectiveAttackLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.ATTACK) * getPrayerAttackMultiplier(player))

        effectiveLevel += when (CombatConfigs.getAttackStyle(player)){
            AttackStyle.ACCURATE -> 3.0
            AttackStyle.CONTROLLED -> 1.0
            else -> 0.0
        }

        effectiveLevel += 8.0

        if (player.hasEquipped(MELEE_VOID) || player.hasEquipped(MELEE_ELITE_VOID)) {
            effectiveLevel *= 1.10
            effectiveLevel = Math.floor(effectiveLevel)
        }

        return effectiveLevel
    }

    private fun getEffectiveDefenceLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.DEFENCE) * getPrayerDefenceMultiplier(player))

        effectiveLevel += when (CombatConfigs.getAttackStyle(player)){
            AttackStyle.DEFENSIVE -> 3.0
            AttackStyle.CONTROLLED -> 1.0
            AttackStyle.LONG_RANGE -> 3.0
            else -> 0.0
        }

        effectiveLevel += 8.0

        return Math.floor(effectiveLevel)
    }

    private fun getEffectiveStrengthLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.STRENGTH).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEffectiveAttackLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.ATTACK).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEffectiveDefenceLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.DEFENCE).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getPrayerStrengthMultiplier(player: Player): Double = when {
        Prayers.isActive(player, Prayer.BURST_OF_STRENGTH) -> 1.05
        Prayers.isActive(player, Prayer.SUPERHUMAN_STRENGTH) -> 1.10
        Prayers.isActive(player, Prayer.ULTIMATE_STRENGTH) -> 1.15
        Prayers.isActive(player, Prayer.CHIVALRY) -> 1.18
        Prayers.isActive(player, Prayer.PIETY) -> 1.23
        else -> 1.0
    }

    private fun getPrayerAttackMultiplier(player: Player): Double = when {
        Prayers.isActive(player, Prayer.CLARITY_OF_THOUGHT) -> 1.05
        Prayers.isActive(player, Prayer.IMPROVED_REFLEXES) -> 1.10
        Prayers.isActive(player, Prayer.INCREDIBLE_REFLEXES) -> 1.15
        Prayers.isActive(player, Prayer.CHIVALRY) -> 1.15
        Prayers.isActive(player, Prayer.PIETY) -> 1.20
        else -> 1.0
    }

    private fun getPrayerDefenceMultiplier(player: Player): Double = when {
        Prayers.isActive(player, Prayer.THICK_SKIN) -> 1.05
        Prayers.isActive(player, Prayer.ROCK_SKIN) -> 1.10
        Prayers.isActive(player, Prayer.STEEL_SKIN) -> 1.15
        Prayers.isActive(player, Prayer.CHIVALRY) -> 1.20
        Prayers.isActive(player, Prayer.PIETY) -> 1.25
        Prayers.isActive(player, Prayer.RIGOUR) -> 1.25
        else -> 1.0
    }

    private fun getEquipmentMultiplier(player: Player, target: Pawn? = null): Double = when {
        player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet") -> 7.0 / 6.0
        player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet_e") -> 1.2
        player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice") && isRevenantCaves(player.tile) -> 1.2
        player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS) || player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS_I) -> 7.0 / 6.0
        player.hasEquipped(EquipmentType.HEAD, *SLAYER_HELMETS) && target is Npc && isOnSlayerTaskFor(player, target) -> 1.5 // 50% damage bonus
        else -> 1.0
    }

    private fun isRevenantCaves(tile: Tile): Boolean {
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

    private fun applyPassiveMultiplier(pawn: Pawn, target: Pawn, base: Double): Double {
        if (pawn is Player) {
            val world = pawn.world
            val multiplier = when {
                pawn.hasEquipped(EquipmentType.AMULET, "item.berserker_necklace") -> 1.2
                isWearingDharok(pawn) -> {
                    val lost = (pawn.getMaxHp() - pawn.getCurrentHp()) / 100.0
                    val max = pawn.getMaxHp() / 100.0
                    1.0 + (lost * max)
                }
                pawn.hasEquipped(EquipmentType.WEAPON, "item.gadderhammer") && isShade(target) -> if (world.chance(1, 20)) 2.0 else 1.25
                pawn.hasEquipped(EquipmentType.WEAPON, "item.keris", "item.kerisp", "item.keris_partisan", "item.keris_partisan_of_breaching", "item.keris_partisan_of_corruption", "item.keris_partisan_of_the_sun") && (isKalphite(target) || isScarab(target)) -> if (world.chance(1, 51)) 3.0 else (4.0 / 3.0)
                else -> 1.0
            }
            if (multiplier == 1.0 && isWearingVerac(pawn)) {
                return base + 1.0
            }
            return base * multiplier
        }
        return base
    }

    private fun getDamageDealMultiplier(pawn: Pawn): Double = pawn.attr[Combat.DAMAGE_DEAL_MULTIPLIER] ?: 1.0

    private fun getDamageTakeMultiplier(pawn: Pawn): Double = pawn.attr[Combat.DAMAGE_TAKE_MULTIPLIER] ?: 1.0
    
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
        
        return if (!isForm2) {
            // Form 1: Heavy reduction to Ranged and Magic, normal to Melee
            // Melee attacks (STAB, SLASH, CRUSH) do normal damage
            1.0
        } else {
            // Form 2: Heavy reduction to Melee and Ranged, normal to Magic
            // Melee attacks (STAB, SLASH, CRUSH) do reduced damage
            when (attackStyle) {
                CombatStyle.STAB, CombatStyle.SLASH, CombatStyle.CRUSH -> 0.25
                else -> 1.0
            }
        }
    }

    /**
     * Get Corporeal Beast damage multiplier - 50% reduction for non-spear/hasta weapons
     */
    private fun getCorporealBeastDamageMultiplier(player: Player, target: Npc): Double {
        // Check if this is the Corporeal Beast
        val isCorporealBeast = target.id == getRSCM("npc.corporeal_beast") || 
                              target.def.name.lowercase().contains("corporeal beast")
        if (!isCorporealBeast) return 1.0
        
        // Check if player is using a spear or hasta
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return 0.5
        val weaponName = weapon.getDef().name.lowercase()
        
        // Check if weapon is a spear or hasta
        val isSpearOrHasta = weaponName.contains("spear") || weaponName.contains("hasta")
        
        return if (isSpearOrHasta) {
            1.0 // Full damage for spears/hastae
        } else {
            0.5 // 50% damage reduction for all other weapons
        }
    }

    private fun isDemon(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.DEMON)
        } else {
            false
        }
    }

    private fun isShade(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.SHADE)
        } else {
            false
        }
    }

    private fun isKalphite(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.KALPHITE)
        } else {
            false
        }
    }

    private fun isScarab(pawn: Pawn): Boolean {
        return if (pawn is Npc) {
            pawn.isSpecies(NpcSpecies.SCARAB)
        } else {
            false
        }
    }

    private fun isWearingDharok(pawn: Pawn): Boolean {
        return if (pawn is Player) {
            pawn.hasEquipped(EquipmentType.HEAD, "item.dharoks_helm", "item.dharoks_helm_25", "item.dharoks_helm_50", "item.dharoks_helm_75", "item.dharoks_helm_100")
                    && pawn.hasEquipped(EquipmentType.WEAPON, "item.dharoks_greataxe", "item.dharoks_greataxe_25", "item.dharoks_greataxe_50", "item.dharoks_greataxe_75", "item.dharoks_greataxe_100")
                    && pawn.hasEquipped(EquipmentType.CHEST, "item.dharoks_platebody", "item.dharoks_platebody_25", "item.dharoks_platebody_50", "item.dharoks_platebody_75", "item.dharoks_platebody_100")
                    && pawn.hasEquipped(EquipmentType.LEGS, "item.dharoks_platelegs", "item.dharoks_platelegs_25", "item.dharoks_platelegs_50", "item.dharoks_platelegs_75", "item.dharoks_platelegs_100")
        } else {
            false
        }
    }

    private fun isWearingVerac(pawn: Pawn): Boolean {
        return if (pawn is Player) {
            pawn.hasEquipped(EquipmentType.HEAD, "item.veracs_helm", "item.veracs_helm_25", "item.veracs_helm_50", "item.veracs_helm_75", "item.veracs_helm_100")
                    && pawn.hasEquipped(EquipmentType.WEAPON, "item.veracs_flail", "item.veracs_flail_25", "item.veracs_flail_50", "item.veracs_flail_75", "item.veracs_flail_100")
                    && pawn.hasEquipped(EquipmentType.CHEST, "item.veracs_brassard", "item.veracs_brassard_25", "item.veracs_brassard_50", "item.veracs_brassard_75", "item.veracs_brassard_100")
                    && pawn.hasEquipped(EquipmentType.LEGS, "item.veracs_plateskirt", "item.veracs_plateskirt_25", "item.veracs_plateskirt_50", "item.veracs_plateskirt_75", "item.veracs_plateskirt_100")
        } else {
            false
        }
    }

    private fun isWearingTorag(player: Player): Boolean {
        return player.hasEquipped(EquipmentType.HEAD, "item.torags_helm", "item.torags_helm_25", "item.torags_helm_50", "item.torags_helm_75", "item.torags_helm_100")
                && player.hasEquipped(EquipmentType.WEAPON, "item.torags_hammers", "item.torags_hammers_25", "item.torags_hammers_50", "item.torags_hammers_75", "item.torags_hammers_100")
                && player.hasEquipped(EquipmentType.CHEST, "item.torags_platebody", "item.torags_platebody_25", "item.torags_platebody_50", "item.torags_platebody_75", "item.torags_platebody_100")
                && player.hasEquipped(EquipmentType.LEGS, "item.torags_platelegs", "item.torags_platelegs_25", "item.torags_platelegs_50", "item.torags_platelegs_75", "item.torags_platelegs_100")
    }

    /**
     * Check if wilderness weapon bonus applies (100% damage/accuracy bonus)
     * Conditions:
     * 1. Player must be in wilderness or Revenant Caves
     * 2. Player must have a wilderness weapon equipped (Viggora's Chainmace or Ursine Chainmace for melee)
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
        
        // Check if player has a wilderness melee weapon equipped
        val hasWildernessWeapon = player.hasEquipped(
            EquipmentType.WEAPON,
            "item.viggoras_chainmace",
            "item.ursine_chainmace"
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