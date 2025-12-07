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
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.mechanics.prayer.Prayer
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.api.PrayerIcon
import org.alter.plugins.content.skills.slayer.Slayer
import dev.openrune.cache.CacheManager.getNpc
import org.alter.rscm.RSCM.getRSCM
import org.alter.plugins.content.mechanics.doompoints.DoomPoints
import org.alter.api.NpcSpecies

/**
 * @author Tom <rspsmods@gmail.com>
 */
object MagicCombatFormula : CombatFormula {
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

    private val SLAYER_HELMETS =
        arrayOf(
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
            "item.tztok_slayer_helmet",
            "item.tztok_slayer_helmet_i",
            "item.vampyric_slayer_helmet",
            "item.vampyric_slayer_helmet_i",
            "item.tzkal_slayer_helmet",
            "item.tzkal_slayer_helmet_i",
            "item.araxyte_slayer_helmet",
            "item.araxyte_slayer_helmet_i",
            "item.slayer_helmet_i_25177",
            "item.black_slayer_helmet_i_25179",
            "item.green_slayer_helmet_i_25181",
            "item.red_slayer_helmet_i_25183",
            "item.purple_slayer_helmet_i_25185",
            "item.turquoise_slayer_helmet_i_25187",
            "item.hydra_slayer_helmet_i_25189",
            "item.twisted_slayer_helmet_i_25191",
            "item.tztok_slayer_helmet_i_25902",
            "item.vampyric_slayer_helmet_i_25908",
            "item.tzkal_slayer_helmet_i_25914",
            "item.slayer_helmet_i_26674",
            "item.black_slayer_helmet_i_26675",
            "item.green_slayer_helmet_i_26676",
            "item.red_slayer_helmet_i_26677",
            "item.purple_slayer_helmet_i_26678",
            "item.turquoise_slayer_helmet_i_26679",
            "item.hydra_slayer_helmet_i_26680",
            "item.twisted_slayer_helmet_i_26681",
            "item.tztok_slayer_helmet_i_26682",
            "item.vampyric_slayer_helmet_i_26683",
            "item.tzkal_slayer_helmet_i_26684",
            "item.araxyte_slayer_helmet_i_29820",
            "item.araxyte_slayer_helmet_i_29822"
        )

    private val MAGE_VOID = arrayOf("item.void_mage_helm", "item.void_knight_top", "item.void_knight_robe", "item.void_knight_gloves")

    private val MAGE_ELITE_VOID = arrayOf("item.void_mage_helm", "item.elite_void_top", "item.elite_void_robe", "item.void_knight_gloves")

    private val BOLT_SPELLS = enumSetOf(CombatSpell.WIND_BOLT, CombatSpell.WATER_BOLT, CombatSpell.EARTH_BOLT, CombatSpell.FIRE_BOLT)

    private val FIRE_SPELLS =
        enumSetOf(CombatSpell.FIRE_STRIKE, CombatSpell.FIRE_BOLT, CombatSpell.FIRE_BLAST, CombatSpell.FIRE_WAVE, CombatSpell.FIRE_SURGE)

    override fun getAccuracy(
        pawn: Pawn,
        target: Pawn,
        specialAttackMultiplier: Double,
    ): Double {
        val attack = getAttackRoll(pawn, target)
        val defence =
            if (target is Player) {
                getDefenceRoll(target)
            } else if (target is Npc) {
                getDefenceRoll(pawn, target)
            } else {
                throw IllegalArgumentException("Unhandled pawn.")
            }

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
        val spell = pawn.attr[Combat.CASTING_SPELL]
        var hit = spell?.maxHit?.toDouble() ?: 1.0
        if (pawn is Player) {
            val magic = pawn.getSkills().getCurrentLevel(Skills.MAGIC)
            if (pawn.hasEquipped(
                    EquipmentType.WEAPON,
                    "item.trident_of_the_seas",
                    "item.trident_of_the_seas_e",
                    "item.trident_of_the_seas_full",
                )
            ) {
                hit = (Math.floor(magic / 3.0) - 5.0)
            } else if (pawn.hasEquipped(EquipmentType.WEAPON, "item.trident_of_the_swamp", "item.trident_of_the_swamp_e")) {
                hit = (Math.floor(magic / 3.0) - 2.0)
            }

            if (pawn.hasEquipped(EquipmentType.GLOVES, "item.chaos_gauntlets") && spell != null && spell in BOLT_SPELLS) {
                hit += 3
            }

            var multiplier = 1.0 + (pawn.getMagicDamageBonus() / 100.0)

            if (pawn.hasEquipped(
                    EquipmentType.AMULET,
                    "item.amulet_of_the_damned_full",
                ) &&
                pawn.hasEquipped(
                    EquipmentType.WEAPON,
                    "item.ahrims_staff",
                    "item.ahrims_staff_25",
                    "item.ahrims_staff_50",
                    "item.ahrims_staff_75",
                    "item.ahrims_staff_100",
                ) &&
                pawn.world.chance(1, 4)
            ) {
                multiplier += 0.3
            }

            if (pawn.hasEquipped(EquipmentType.WEAPON, "item.mystic_smoke_staff") && pawn.hasSpellbook(Spellbook.NORMAL)) {
                multiplier += 0.1
            }

            // Ancient Sceptre bonuses (base and enhanced versions)
            if (spell != null && spell.isAncient() && pawn is Player) {
                // Enhanced Ancient Sceptres: +10% damage, +15% accuracy
                if (hasEquippedSafely(pawn, EquipmentType.WEAPON,
                        "item.enhanced_ice_ancient_sceptre",
                        "item.enhanced_blood_ancient_sceptre",
                        "item.enhanced_smoke_ancient_sceptre",
                        "item.enhanced_shadow_ancient_sceptre"
                    )
                ) {
                    multiplier += 0.10
                }
                // Base Ancient Sceptre: +5% damage, +10% accuracy
                else if (hasEquippedSafely(pawn, EquipmentType.WEAPON,
                        "item.ancient_sceptre",
                        "item.ice_ancient_sceptre",
                        "item.blood_ancient_sceptre",
                        "item.smoke_ancient_sceptre",
                        "item.shadow_ancient_sceptre"
                    )
                ) {
                    multiplier += 0.05
                }
            }

            if (pawn.hasEquipped(MAGE_ELITE_VOID)) {
                multiplier += 0.025
            }

            hit *= multiplier
            hit = Math.floor(hit)

            if (pawn.hasEquipped(EquipmentType.SHIELD, "item.tome_of_fire") && spell in FIRE_SPELLS) {
                // TODO: check tome of fire has charges
                hit *= 1.5
                hit = Math.floor(hit)
            }

            if (target is Npc) {
                if (pawn.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS_I)) {
                    // Black mask (i) bonus - always 15% for imbued black masks
                    hit *= 1.15
                    hit = Math.floor(hit)
                } else if (pawn.hasEquipped(EquipmentType.HEAD, *SLAYER_HELMETS) && isOnSlayerTaskFor(pawn, target)) {
                    // Slayer helmet bonus - 100% when on slayer task
                    hit *= 2.0
                    hit = Math.floor(hit)
                } else if (pawn.hasEquipped(EquipmentType.AMULET, "item.salve_amuletei") && target.isSpecies(NpcSpecies.UNDEAD)) {
                    hit *= 1.20
                    hit = Math.floor(hit)
                }
                
                // Bounty Hunter set bonus: Double damage on Boss Island with full set, 0 damage outside Boss Island
                val isOnBossIsland = BountyHunterUtils.isOnBossIsland(pawn)
                val hasFullSet = BountyHunterUtils.hasFullBountyHunterSet(pawn)
                val hasAnyBountyItem = BountyHunterUtils.hasAnyBountyHunterItem(pawn)
                
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
                
                // Wilderness weapon bonus: 100% damage increase (2.0x) in wilderness against wilderness NPCs/revenants
                if (isWildernessWeaponBonus(pawn, target)) {
                    hit *= 2.0
                    hit = Math.floor(hit)
                }
                
                // Check if NPC target has Protect from Magic prayer active - completely blocks magic damage
                if (target is Npc && target.prayerIcon == PrayerIcon.PROTECT_FROM_MAGIC.id) {
                    hit = 0.0
                }
            }
        } else if (pawn is Npc) {
            val multiplier = 1.0 + (pawn.getMagicDamageBonus() / 100.0)
            hit *= multiplier
            hit = Math.floor(hit)
            
            // Apply protection prayer - completely blocks magic damage (OSRS behavior)
            if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)) {
                // Protect from Magic completely blocks magic damage
                hit = 0.0
            }
        }

        hit *= getDamageDealMultiplier(pawn)
        hit = Math.floor(hit)
        
        // Apply doom points damage multiplier perk (only for players)
        if (pawn is Player) {
            val damageMultiplier = DoomPoints.getDamageMultiplier(pawn)
            if (damageMultiplier > 0) {
                hit *= (1.0 + damageMultiplier / 100.0)
                hit = Math.floor(hit)
            }
        }
        
        // Apply 6x base damage multiplier for revenants
        if (pawn is Npc) {
            val isRevenant = pawn.def.name.lowercase().contains("revenant") || 
                            (pawn.tile.z >= 10000 && pawn.tile.z <= 10300 && pawn.tile.x >= 3100 && pawn.tile.x <= 3300)
            if (isRevenant) {
                hit *= 6.0
            }
        }
        hit = Math.floor(hit)
        
        // Apply damage take multiplier (for items like Bracelet of Ethereum)
        hit *= getDamageTakeMultiplier(target)
        hit = Math.floor(hit)

        // Apply Salve amulet damage reduction (40% reduction from undead)
        if (target is Player && pawn is Npc && pawn.isSpecies(NpcSpecies.UNDEAD)) {
            val hasSalveAmulet = target.hasEquipped(EquipmentType.AMULET, "item.salve_amulet") ||
                                 target.hasEquipped(EquipmentType.AMULET, "item.salve_amulet_e") ||
                                 target.hasEquipped(EquipmentType.AMULET, "item.salve_amuleti") ||
                                 target.hasEquipped(EquipmentType.AMULET, "item.salve_amuletei")
            if (hasSalveAmulet) {
                hit *= 0.6  // 40% damage reduction
                hit = Math.floor(hit)
            }
        }

        // Cap damage at 30 if target is wearing Bracelet of Ethereum and attacker is Revenant
        // If also wearing Salve amulet, reduce the cap by 40% (30 * 0.6 = 18)
        if (pawn is Npc) {
            val isRevenant = pawn.def.name.lowercase().contains("revenant") || 
                            (pawn.tile.z >= 10000 && pawn.tile.z <= 10300 && pawn.tile.x >= 3100 && pawn.tile.x <= 3300)
            
            if (isRevenant && target is Player && target.hasEquipped(EquipmentType.GLOVES, "item.bracelet_of_ethereum")) {
                val hasSalveAmulet = pawn.isSpecies(NpcSpecies.UNDEAD) && (
                    target.hasEquipped(EquipmentType.AMULET, "item.salve_amulet") ||
                    target.hasEquipped(EquipmentType.AMULET, "item.salve_amulet_e") ||
                    target.hasEquipped(EquipmentType.AMULET, "item.salve_amuleti") ||
                    target.hasEquipped(EquipmentType.AMULET, "item.salve_amuletei")
                )
                val damageCap = if (hasSalveAmulet) 18.0 else 30.0  // 40% reduction of cap when wearing Salve
                if (hit > damageCap) {
                    hit = damageCap
                }
            }
        }
        
        // Apply Kalphite Queen form-based damage reduction
        if (target is Npc) {
            hit *= getKQDamageMultiplier(target, CombatStyle.MAGIC)
            hit = Math.floor(hit)
        }
        
        // Apply Corporeal Beast damage reduction (50% for all non-spear/hasta weapons)
        if (target is Npc && pawn is Player) {
            hit *= getCorporealBeastDamageMultiplier(pawn, target)
            hit = Math.floor(hit)
        }

        return hit.toInt()
    }

    private fun getAttackRoll(pawn: Pawn, target: Pawn): Int {
        val a =
            if (pawn is Player) {
                getEffectiveAttackLevel(pawn)
            } else if (pawn is Npc) {
                getEffectiveAttackLevel(pawn)
            } else {
                0.0
            }
        val b = getEquipmentAttackBonus(pawn)

        var maxRoll = a * (b + 64.0)
        if (pawn is Player) {
            maxRoll = applyAttackSpecials(pawn, maxRoll, target)
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
        target: Npc,
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

        val maxRoll = a * (b + 64.0)
        return maxRoll.toInt()
    }

    private fun getDefenceRoll(target: Player): Int {
        var effectiveLvl = getEffectiveDefenceLevel(target)

        effectiveLvl *= 0.3
        effectiveLvl = Math.floor(effectiveLvl)

        var magicLvl = target.getSkills().getCurrentLevel(Skills.MAGIC).toDouble()
        magicLvl *= getPrayerAttackMultiplier(target)
        magicLvl = Math.floor(magicLvl)

        magicLvl *= 0.7
        magicLvl = Math.floor(magicLvl)

        val a = Math.floor(effectiveLvl + magicLvl).toInt()
        val b = getEquipmentDefenceBonus(target)

        val maxRoll = a * (b + 64.0)
        return maxRoll.toInt()
    }

    private fun applyAttackSpecials(
        player: Player,
        base: Double,
        target: Pawn,
    ): Double {
        var hit = base

        hit *= getEquipmentMultiplier(player)
        hit = Math.floor(hit)

        if (player.hasEquipped(EquipmentType.WEAPON, "item.mystic_smoke_staff")) {
            hit *= 1.1
            hit = Math.floor(hit)
        }

        // Ancient Sceptre accuracy bonuses (base and enhanced versions)
        val spell = player.attr[Combat.CASTING_SPELL]
        if (spell != null && spell.isAncient()) {
            // Enhanced Ancient Sceptres: +15% accuracy
            if (hasEquippedSafely(player, EquipmentType.WEAPON,
                    "item.enhanced_ice_ancient_sceptre",
                    "item.enhanced_blood_ancient_sceptre",
                    "item.enhanced_smoke_ancient_sceptre",
                    "item.enhanced_shadow_ancient_sceptre"
                )
            ) {
                hit *= 1.15
                hit = Math.floor(hit)
            }
            // Base Ancient Sceptre: +10% accuracy
            else if (hasEquippedSafely(player, EquipmentType.WEAPON,
                    "item.ancient_sceptre",
                    "item.ice_ancient_sceptre",
                    "item.blood_ancient_sceptre",
                    "item.smoke_ancient_sceptre",
                    "item.shadow_ancient_sceptre"
                )
            ) {
                hit *= 1.10
                hit = Math.floor(hit)
            }
        }

        // Bounty Hunter set bonus: Force 0 accuracy outside Boss Island
        if (target is Npc) {
            val isOnBossIsland = BountyHunterUtils.isOnBossIsland(player)
            val hasAnyBountyItem = BountyHunterUtils.hasAnyBountyHunterItem(player)
            
            if (hasAnyBountyItem && !isOnBossIsland) {
                // Force 0 accuracy outside Boss Island (will result in misses)
                return 0.0
            }
        }

        // Wilderness weapon bonus: 100% accuracy increase (2.0x) in wilderness against wilderness NPCs/revenants
        if (target is Npc && isWildernessWeaponBonus(player, target)) {
            hit *= 2.0
            hit = Math.floor(hit)
        }

        return hit
    }

    private fun getEffectiveAttackLevel(player: Player): Double {
        var effectiveLevel = Math.floor(player.getSkills().getCurrentLevel(Skills.MAGIC) * getPrayerAttackMultiplier(player))

        if (player.hasWeaponType(WeaponType.TRIDENT)) {
            effectiveLevel +=
                when (CombatConfigs.getAttackStyle(player)) {
                    AttackStyle.ACCURATE -> 3.0
                    AttackStyle.CONTROLLED -> 1.0
                    else -> 0.0
                }
        }

        effectiveLevel += 8.0

        if (player.hasEquipped(MAGE_VOID) || player.hasEquipped(MAGE_ELITE_VOID)) {
            effectiveLevel *= 1.45
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

    private fun getEffectiveAttackLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.MAGIC).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEffectiveDefenceLevel(npc: Npc): Double {
        var effectiveLevel = npc.stats.getCurrentLevel(NpcSkills.DEFENCE).toDouble()
        effectiveLevel += 8
        return effectiveLevel
    }

    private fun getEquipmentAttackBonus(pawn: Pawn): Double {
        return pawn.getBonus(BonusSlot.ATTACK_MAGIC).toDouble()
    }

    private fun getEquipmentDefenceBonus(target: Pawn): Double {
        return target.getBonus(BonusSlot.DEFENCE_MAGIC).toDouble()
    }

    private fun getEquipmentMultiplier(player: Player): Double =
        when {
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet") -> 7.0 / 6.0
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amulet_e") -> 1.2
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amuleti") -> 1.15
            player.hasEquipped(EquipmentType.AMULET, "item.salve_amuletei") -> 1.2
            player.hasEquipped(EquipmentType.AMULET, "item.amulet_of_avarice") && isRevenantCaves(player.tile) -> 1.2
            // TODO: this should only apply when target is slayer task?
            player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS) -> 7.0 / 6.0
            player.hasEquipped(EquipmentType.HEAD, *BLACK_MASKS_I) -> 1.15
            else -> 1.0
        }

    private fun isRevenantCaves(tile: org.alter.game.model.Tile): Boolean {
        return tile.z >= 10000 && tile.z <= 10300 && tile.x >= 3100 && tile.x <= 3300
    }

    private fun getPrayerAttackMultiplier(player: Player): Double =
        when {
            Prayers.isActive(player, Prayer.MYSTIC_WILL) -> 1.05
            Prayers.isActive(player, Prayer.MYSTIC_LORE) -> 1.10
            Prayers.isActive(player, Prayer.MYSTIC_MIGHT) -> 1.15
            Prayers.isActive(player, Prayer.AUGURY) -> 1.25
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
        
        return if (!isForm2) {
            // Form 1: Heavy reduction to Ranged and Magic, normal to Melee
            // Magic attacks do reduced damage
            when (attackStyle) {
                CombatStyle.MAGIC -> 0.25
                else -> 1.0
            }
        } else {
            // Form 2: Heavy reduction to Melee and Ranged, normal to Magic
            // Magic attacks do normal damage
            1.0
        }
    }

    /**
     * Check if wilderness weapon bonus applies (100% damage/accuracy bonus)
     * Conditions:
     * 1. Player must be in wilderness or Revenant Caves
     * 2. Player must have a wilderness weapon equipped (Thammaron's Sceptre or Accursed Sceptre for magic)
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
        
        // Check if player has a wilderness magic weapon equipped
        val hasWildernessWeapon = player.hasEquipped(
            EquipmentType.WEAPON,
            "item.thammarons_sceptre",
            "item.accursed_sceptre"
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
    
    /**
     * Safely check if a player has an item equipped, catching exceptions if the item doesn't exist in the cache
     */
    private fun hasEquippedSafely(player: Player, slot: EquipmentType, vararg items: String): Boolean {
        if (items.isEmpty()) return false
        return try {
            val itemIds = items.mapNotNull { itemName ->
                try {
                    getRSCM(itemName)
                } catch (e: Exception) {
                    // Item doesn't exist in cache, skip it
                    null
                }
            }
            if (itemIds.isEmpty()) return false
            itemIds.any { player.equipment.hasAt(slot.id, it) }
        } catch (e: Exception) {
            // If anything goes wrong, return false
            false
        }
    }
}
