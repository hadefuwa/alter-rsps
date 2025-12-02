package org.alter.plugins.content.minigames.lastmanstanding

import org.alter.api.*
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
import org.alter.game.plugin.*
import org.alter.api.EquipmentType
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.*
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.combat.strategy.magic.CombatSpell
import org.alter.plugins.content.mechanics.prayer.Prayer

/**
 * Last Man Standing Minigame Plugin
 * 
 * This plugin creates a PvP-style minigame where NPCs fight players
 * using protection prayers like PKers. The NPCs will switch prayers
 * based on the player's combat style.
 * 
 * Location: 3459, 5829
 * NPC: R4ng3rNo0b889 (ID 2643)
 */
class LastManStandingPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Spawn multiple LMS NPCs at the location
        // Spawn them in a spread pattern around the center
        val centerX = 3459
        val centerZ = 5829
        
        // Spawn 10 NPCs in a spread pattern
        spawnNpc("npc.r4ng3rno0b889", x = centerX, z = centerZ, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX + 2, z = centerZ, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX - 2, z = centerZ, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX, z = centerZ + 2, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX, z = centerZ - 2, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX + 3, z = centerZ + 3, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX - 3, z = centerZ - 3, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX + 3, z = centerZ - 3, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX - 3, z = centerZ + 3, height = 0, walkRadius = 8, direction = Direction.SOUTH)
        spawnNpc("npc.r4ng3rno0b889", x = centerX + 1, z = centerZ + 1, height = 0, walkRadius = 8, direction = Direction.SOUTH)

        // Set combat definition for LMS NPCs
        setCombatDef("npc.r4ng3rno0b889") {
            configs {
                attackSpeed = 4
                respawnDelay = 25
            }

            aggro {
                radius = 10  // Detection radius
                searchDelay = 1
                alwaysAggro()
            }

            stats {
                hitpoints = 99  // High HP like a maxed player
                attack = 99
                strength = 99
                defence = 99
                magic = 99
                ranged = 99
            }

            bonuses {
                attackStab = 100
                attackSlash = 100
                attackCrush = 100
                attackMagic = 100
                attackRanged = 100

                defenceStab = 100
                defenceSlash = 100
                defenceCrush = 100
                defenceMagic = 100
                defenceRanged = 100

                attackBonus = 100
                strengthBonus = 100
                rangedStrengthBonus = 100
                magicDamageBonus = 100
            }

            anims {
                attack = 422  // Ranged attack animation
                block = 424
                death = 836
            }

            drops {
                always {
                    add("item.bones", 1)
                }
                
                main(weight = 100) {
                    add("item.coins_995", min = 1000, max = 5000, weight = 30)
                    add("item.shark", min = 1, max = 5, weight = 20)
                    add("item.prayer_potion4", min = 1, max = 3, weight = 15)
                    add("item.super_restore4", min = 1, max = 2, weight = 10)
                }
            }
        }
        
        // Set NPCs to use ranged combat by default
        onNpcSpawn(npc = "npc.r4ng3rno0b889") {
            npc.combatClass = CombatClass.RANGED
        }
        
        // Handle combat with prayer switching
        onNpcCombat("npc.r4ng3rno0b889") {
            npc.queue {
                lmsCombat()
            }
        }
    }
    
    /**
     * LMS NPC combat with protection prayer switching
     * NPCs will switch prayers based on what the player is attacking with
     */
    private suspend fun QueueTask.lmsCombat() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() ?: return
        
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            // Detect player's combat style and switch prayer accordingly
            if (target is Player) {
                val combatStyle = detectPlayerCombatStyle(target)
                
                // Switch prayer based on player's combat style
                when (combatStyle) {
                    CombatClass.MELEE -> {
                        // Use Protect from Melee
                        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id
                    }
                    CombatClass.RANGED -> {
                        // Use Protect from Missiles
                        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MISSILES.id
                    }
                    CombatClass.MAGIC -> {
                        // Use Protect from Magic
                        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MAGIC.id
                    }
                    else -> {
                        // Default to melee protection
                        npc.prayerIcon = PrayerIcon.PROTECT_FROM_MELEE.id
                    }
                }
            }
            
            // Move to attack range and perform ranged attack
            if (npc.moveToAttackRange(this, target, distance = 7, projectile = true) && npc.isAttackDelayReady()) {
                npc.rangedAttack(target)
            }
            
            wait(1)
            target = npc.getCombatTarget() ?: break
        }
        
        // Clear prayer icon when combat ends
        npc.prayerIcon = -1
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
    
    /**
     * Ranged attack for LMS NPCs
     */
    private fun Npc.rangedAttack(target: Pawn) {
        prepareAttack(CombatClass.RANGED, CombatStyle.RANGED, AttackStyle.ACCURATE)
        animate(426) // Ranged attack animation
        
        // Create ranged projectile
        val projectile = createProjectile(
            target,
            gfx = 249, // Arrow projectile graphic
            startHeight = 43,
            endHeight = 31,
            delay = 51,
            angle = 10,
            steepness = 11
        )
        this.world.spawn(projectile)
        
        // Calculate hit delay based on distance
        val hitDelay = RangedCombatStrategy.getHitDelay(
            getFrontFacingTile(target),
            target.getCentreTile()
        )
        
        // Deal damage after projectile hits
        this.world.queue {
            wait(hitDelay - 1)
            
            if (target.isAlive()) {
                val hit = dealHit(
                    target = target,
                    formula = RangedCombatFormula,
                    delay = 1
                ) { hit ->
                    if (hit.landed() && target is Player) {
                        // Hit landed
                    }
                }
            }
        }
    }
    
    /**
     * Detects what combat style a player is using
     */
    private fun detectPlayerCombatStyle(player: Player): CombatClass {
        // Check if player is casting a spell
        val castingSpell = player.attr[Combat.CASTING_SPELL] as? CombatSpell
        if (castingSpell != null) {
            return CombatClass.MAGIC
        }
        
        // Check equipped weapon to determine combat style
        val weapon = player.getEquipment(EquipmentType.WEAPON) ?: return CombatClass.MELEE
        
        // Check if it's a ranged weapon (bow, crossbow, etc.)
        val weaponName = weapon.getDef().name.lowercase()
        if (weaponName.contains("bow") || weaponName.contains("crossbow") || 
            weaponName.contains("dart") || weaponName.contains("knife") ||
            weaponName.contains("throwing") || weaponName.contains("javelin")) {
            return CombatClass.RANGED
        }
        
        // Check if it's a magic weapon (staff, wand, etc.)
        if (weaponName.contains("staff") || weaponName.contains("wand") || 
            weaponName.contains("tome") || weaponName.contains("book")) {
            return CombatClass.MAGIC
        }
        
        // Default to melee
        return CombatClass.MELEE
    }
}


