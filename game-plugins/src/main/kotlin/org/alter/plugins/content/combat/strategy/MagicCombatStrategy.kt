package org.alter.plugins.content.combat.strategy

import org.alter.api.EquipmentType
import org.alter.api.ProjectileType
import org.alter.api.Skills
import org.alter.api.ext.*
import org.alter.game.model.Graphic
import org.alter.game.model.Tile
import org.alter.game.model.combat.XpMode
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.CombatConfigs
import org.alter.plugins.content.combat.createProjectile
import org.alter.plugins.content.combat.dealHit
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.magic.MagicSpells
import org.alter.plugins.content.mechanics.poison.Poison
import org.alter.plugins.content.mechanics.poison.poison
import org.alter.plugins.content.mechanics.doompoints.addXpWithPassiveCheck
import org.alter.rscm.RSCM.getRSCM

/**
 * @author Tom <rspsmods@gmail.com>
 */
object MagicCombatStrategy : CombatStrategy {
    override fun getAttackRange(pawn: Pawn): Int = 10

    override fun canAttack(
        pawn: Pawn,
        target: Pawn,
    ): Boolean {
        if (pawn is Player) {
            val spell = pawn.attr[Combat.CASTING_SPELL] ?: return false
            val requirements = MagicSpells.getMetadata(spell.id)
            if (requirements != null && !MagicSpells.canCast(pawn, requirements.lvl, requirements.items, requirements.spellbook)) {
                return false
            }
        }
        return true
    }

    override fun attack(
        pawn: Pawn,
        target: Pawn,
    ) {
        val world = pawn.world

        val spell = pawn.attr[Combat.CASTING_SPELL] ?: CombatSpell.WIND_STRIKE
        val projectile =
            pawn.createProjectile(
                target,
                gfx = spell.projectile,
                type = ProjectileType.MAGIC,
                endHeight = spell.projectilEndHeight,
            )

        pawn.animate(spell.castAnimation)
        spell.castGfx?.let { gfx -> pawn.graphic(gfx) }
        spell.impactGfx?.let { gfx -> target.graphic(Graphic(gfx.id, gfx.height, projectile.lifespan)) }
        if (spell.projectile > 0) {
            world.spawn(projectile)
        }

        if (pawn is Player) {
            if (spell.castSound != -1) {
                pawn.playSound(id = spell.castSound, volume = 1, delay = 0)
            }
            MagicSpells.getMetadata(spell.id)?.let { requirement -> MagicSpells.removeRunes(pawn, requirement.items) }
        }

        val formula = MagicCombatFormula
        val hitDelay = getHitDelay(pawn.getCentreTile(), target.getCentreTile())

        // Check if spell has AoE (Burst or Barrage) - only Burst and Barrage have AoE
        if (spell.hasAoE()) {
            // AoE spell - hit multiple targets
            handleAoESpell(pawn, target, spell, formula, hitDelay)
        } else {
            // Single-target spell (including Rush and Blitz)
            val accuracy = formula.getAccuracy(pawn, target)
            val maxHit = formula.getMaxHit(pawn, target)
            val landHit = accuracy >= world.randomDouble()

            val hitResult = pawn.dealHit(
                target = target,
                maxHit = maxHit,
                landHit = landHit,
                delay = hitDelay
            ) { hit ->
                // Apply spell-specific effects when hit lands
                if (hit.landed && pawn is Player) {
                    val damage = hit.hit.hitmarks.sumOf { it.damage }
                    val healing = applySpellEffects(pawn, target, spell, damage, false)
                    
                    // Apply healing for Blood spells (single-target)
                    if (healing > 0) {
                        pawn.heal(healing, capValue = 0)
                    }
                }
            }
            val damage = hitResult.hit.hitmarks.sumOf { it.damage }

            if (damage >= 0 && pawn is Player) {
                addCombatXp(pawn, target, damage, spell)
            }
        }
    }

    /**
     * Handle AoE spells (Burst and Barrage) that hit multiple targets
     */
    private fun handleAoESpell(
        pawn: Pawn,
        mainTarget: Pawn,
        spell: CombatSpell,
        formula: MagicCombatFormula,
        hitDelay: Int,
    ) {
        val world = pawn.world
        val mainTargetTile = mainTarget.getCentreTile()
        
        // Get all potential targets within radius
        val aoeTargets = mutableListOf<Pawn>()
        aoeTargets.add(mainTarget) // Always include main target
        
        // Determine AoE radius using Chebyshev distance (for square areas)
        // Burst = 1-tile radius = 3x3 area (max distance = 1)
        // Barrage = 1.5-tile radius = 5x5 area (max distance = 2)
        val maxChebyshevDistance = if (spell.isBarrage()) 2 else 1
        
        // Check all tiles in a 5x5 area (sufficient for both Burst and Barrage)
        for (x in -2..2) {
            for (z in -2..2) {
                val checkTile = mainTargetTile.transform(x, z)
                
                // Calculate Chebyshev distance (max of dx, dz) for square area
                val dx = kotlin.math.abs(x)
                val dz = kotlin.math.abs(z)
                val chebyshevDistance = kotlin.math.max(dx, dz)
                
                // Skip if outside radius
                if (chebyshevDistance > maxChebyshevDistance) continue
                
                // Check players in this tile
                world.players.forEach { player ->
                    if (player != pawn && player.isOnline && 
                        player.getCentreTile().x == checkTile.x && 
                        player.getCentreTile().z == checkTile.z &&
                        player.getCentreTile().height == checkTile.height &&
                        player.getCurrentHp() > 0) {
                        if (player != mainTarget && !aoeTargets.contains(player)) {
                            aoeTargets.add(player)
                        }
                    }
                }
                
                // Check NPCs in this tile
                world.npcs.forEach { npc ->
                    if (npc.isSpawned() && npc.getCurrentHp() > 0) {
                        val npcTile = npc.getCentreTile()
                        // Check if NPC is on this tile (using tile comparison)
                        if (npcTile.x == checkTile.x && 
                            npcTile.z == checkTile.z &&
                            npcTile.height == checkTile.height) {
                            // Add NPC if it's not the caster and not already in the list
                            if (npc != pawn && !aoeTargets.contains(npc)) {
                                aoeTargets.add(npc)
                            }
                        }
                    }
                }
            }
        }
        
        // Hit main target first (required for AoE to work)
        val mainAccuracy = formula.getAccuracy(pawn, mainTarget)
        val mainMaxHit = formula.getMaxHit(pawn, mainTarget)
        val mainLandHit = mainAccuracy >= world.randomDouble()
        
        var totalHealing = 0
        
        // Hit main target
        val mainHitResult = pawn.dealHit(
            target = mainTarget,
            maxHit = mainMaxHit,
            landHit = mainLandHit,
            delay = hitDelay
        ) { hit ->
            if (hit.landed && pawn is Player) {
                val damage = hit.hit.hitmarks.sumOf { it.damage }
                val healing = applySpellEffects(pawn, mainTarget, spell, damage, true)
                totalHealing += healing
                addCombatXp(pawn, mainTarget, damage, spell)
            }
        }
        
        // Hit all other targets in AoE (even if main target splashed)
        aoeTargets.forEach { aoeTarget ->
            if (aoeTarget == mainTarget) return@forEach // Already handled
            
            val aoeAccuracy = formula.getAccuracy(pawn, aoeTarget)
            val aoeMaxHit = formula.getMaxHit(pawn, aoeTarget)
            val aoeLandHit = aoeAccuracy >= world.randomDouble()
            
            pawn.dealHit(
                target = aoeTarget,
                maxHit = aoeMaxHit,
                landHit = aoeLandHit,
                delay = hitDelay
            ) { hit ->
                if (hit.landed && pawn is Player) {
                    val damage = hit.hit.hitmarks.sumOf { it.damage }
                    val healing = applySpellEffects(pawn, aoeTarget, spell, damage, true)
                    totalHealing += healing
                    addCombatXp(pawn, aoeTarget, damage, spell)
                }
            }
        }
        
        // Apply total healing from all targets (Blood spells)
        if (totalHealing > 0 && pawn is Player) {
            pawn.heal(totalHealing, capValue = 0)
        }
    }

    fun getHitDelay(
        start: Tile,
        target: Tile,
    ): Int {
        val distance = start.getDistance(target)
        return 2 + Math.floor((1.0 + distance) / 3.0).toInt()
    }

    /**
     * Apply spell-specific effects based on the spell type and equipped sceptre
     * @return healing amount (for Blood spells) to be accumulated across all targets
     */
    private fun applySpellEffects(
        player: Player,
        target: Pawn,
        spell: CombatSpell,
        damage: Int,
        isAoE: Boolean = false,
    ): Int {
        var healing = 0
        
        // Ice spells: Freeze target (only on successful hit)
        if (spell.isIce() && damage > 0) {
            // Freeze durations in cycles (1 cycle = 0.6 seconds)
            // Ice Burst = 10 seconds = ~17 cycles, Ice Barrage = 19.2 seconds = ~32 cycles
            val baseFreezeCycles = when (spell) {
                CombatSpell.ICE_RUSH -> 5
                CombatSpell.ICE_BURST -> 17 // 10 seconds
                CombatSpell.ICE_BLITZ -> 10
                CombatSpell.ICE_BARRAGE -> 32 // 19.2 seconds
                else -> 0
            }
            
            if (baseFreezeCycles > 0) {
                var freezeCycles = baseFreezeCycles
                
                // Apply sceptre multiplier
                if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.enhanced_ice_ancient_sceptre")) {
                    freezeCycles = (freezeCycles * 1.15).toInt()
                } else if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.ice_ancient_sceptre")) {
                    freezeCycles = (freezeCycles * 1.10).toInt()
                }
                
                target.freeze(cycles = freezeCycles) {
                    if (target is Player) {
                        target.message("You have been frozen.")
                    }
                }
            }
        }
        
        // Blood spells: Calculate healing (returned to be accumulated)
        if (spell.isBlood() && damage > 0) {
            var healAmount = (damage * 0.25).toInt() // Base: 25% of damage
            
            // Apply sceptre multiplier
            if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.enhanced_blood_ancient_sceptre")) {
                healAmount = (healAmount * 1.15).toInt()
            } else if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.blood_ancient_sceptre")) {
                healAmount = (healAmount * 1.10).toInt()
            }
            
            healing = healAmount
        }
        
        // Smoke spells: Apply poison
        if (spell.isSmoke() && damage > 0) {
            // Burst/Barrage: 20% chance, 6 damage
            // Rush/Blitz: Different values
            val (basePoisonChance, basePoisonDamage) = when (spell) {
                CombatSpell.SMOKE_RUSH -> Pair(0.10, 4) // 10% chance, 4 damage
                CombatSpell.SMOKE_BURST -> Pair(0.20, 6) // 20% chance, 6 damage
                CombatSpell.SMOKE_BLITZ -> Pair(0.20, 4) // 20% chance, 4 damage
                CombatSpell.SMOKE_BARRAGE -> Pair(0.20, 6) // 20% chance, 6 damage
                else -> Pair(0.0, 0)
            }
            
            if (basePoisonChance > 0.0) {
                var poisonChance = basePoisonChance
                var poisonDamage = basePoisonDamage
                
                // Apply sceptre multiplier
                if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.enhanced_smoke_ancient_sceptre")) {
                    poisonChance *= 3.0
                    poisonDamage += 2
                } else if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.smoke_ancient_sceptre")) {
                    poisonChance *= 2.0
                    poisonDamage += 1
                }
                
                // Cap poison chance at 100%
                poisonChance = poisonChance.coerceAtMost(1.0)
                
                if (player.world.randomDouble() < poisonChance && !Poison.isImmune(target)) {
                    target.poison(initialDamage = poisonDamage) {
                        if (target is Player) {
                            target.message("You have been poisoned.")
                        }
                    }
                }
            }
        }
        
        // Shadow spells: Drain Attack level (Burst/Barrage only) or reduce accuracy
        if (spell.isShadow() && damage > 0) {
            if (isAoE && (spell.isBurst() || spell.isBarrage())) {
                // Burst/Barrage: Drain Attack level
                val drainPercent = if (spell.isBarrage()) 0.15 else 0.10 // Barrage: 15%, Burst: 10%
                
                if (target is Player) {
                    val currentAttack = target.getSkills().getCurrentLevel(Skills.ATTACK)
                    if (currentAttack > 1) {
                        val drainAmount = (currentAttack * drainPercent).toInt().coerceAtLeast(1)
                        val newAttack = (currentAttack - drainAmount).coerceAtLeast(1)
                        target.getSkills().setCurrentLevel(Skills.ATTACK, newAttack)
                        target.message("Your Attack level has been drained!")
                    }
                } else if (target is Npc) {
                    // NPCs don't have attack level to drain, so just apply accuracy reduction
                    val accuracyMultiplier = if (spell.isBarrage()) 0.85 else 0.90
                    target.attr[Combat.ACCURACY_MULTIPLIER] = accuracyMultiplier
                }
            } else {
                // Rush/Blitz: Reduce accuracy
                var accuracyMultiplier = 0.90 // Base: 10% reduction (0.90 multiplier)
                
                // Apply sceptre multiplier
                if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.enhanced_shadow_ancient_sceptre")) {
                    accuracyMultiplier = 0.75 // 25% reduction
                } else if (hasEquippedSafely(player, EquipmentType.WEAPON, "item.shadow_ancient_sceptre")) {
                    accuracyMultiplier = 0.80 // 20% reduction
                }
                
                target.attr[Combat.ACCURACY_MULTIPLIER] = accuracyMultiplier
            }
        }
        
        return healing
    }

    private fun addCombatXp(
        player: Player,
        target: Pawn,
        damage: Int,
        spell: CombatSpell,
    ) {
        val modDamage = if (target.entityType.isNpc) Math.min(target.getCurrentHp(), damage) else damage
        val mode = CombatConfigs.getXpMode(player)
        val multiplier = if (target is Npc) Combat.getNpcXpMultiplier(target) else 1.0
        val baseXp = spell.baseXp

        if (mode == XpMode.MAGIC) {
            val defensive =
                player.getVarbit(
                    Combat.SELECTED_AUTOCAST_VARBIT,
                ) != 0 && player.getVarbit(Combat.DEFENSIVE_MAGIC_CAST_VARBIT) != 0
            if (!defensive) {
                player.addXpWithPassiveCheck(Skills.MAGIC, (modDamage * 2.0 * multiplier) + baseXp)
                player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
            } else {
                player.addXpWithPassiveCheck(Skills.MAGIC, (modDamage * 1.33 * multiplier) + baseXp)
                player.addXpWithPassiveCheck(Skills.DEFENCE, modDamage * multiplier)
                player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
            }
        } else if (mode == XpMode.SHARED) {
            player.addXpWithPassiveCheck(Skills.MAGIC, (modDamage * 1.33 * multiplier) + baseXp)
            player.addXpWithPassiveCheck(Skills.DEFENCE, modDamage * multiplier)
            player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
        } else {
            player.addXpWithPassiveCheck(Skills.MAGIC, (modDamage * 2.0 * multiplier) + baseXp)
            player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
        }
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
