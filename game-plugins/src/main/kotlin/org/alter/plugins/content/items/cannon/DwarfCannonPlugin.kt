package org.alter.plugins.content.items.cannon

import org.alter.api.Skills
import org.alter.api.cfg.Sound
import org.alter.api.cfg.Varp
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.combat.XpMode
import org.alter.game.model.entity.DynamicObject
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.TaskPriority
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.CombatConfigs
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import org.alter.plugins.content.mechanics.doompoints.addXpWithPassiveCheck
import org.alter.plugins.content.skills.slayer.Slayer
import org.alter.rscm.RSCM.getRSCM
import dev.openrune.cache.CacheManager.getNpc

class DwarfCannonPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        val CANNON_BASE_ITEM = getRSCM("item.cannon_base") // 6
        val CANNON_STAND_ITEM = getRSCM("item.cannon_stand") // 8
        val CANNON_BARRELS_ITEM = getRSCM("item.cannon_barrels") // 10
        val CANNON_FURNACE_ITEM = getRSCM("item.cannon_furnace") // 12
        val CANNONBALL_ITEM = getRSCM("item.cannonball") // 2

        val CANNON_BASE_OBJ = getRSCM("object.cannon_base_7") // 7
        val CANNON_STAND_OBJ = getRSCM("object.cannon_stand_8") // 8
        val CANNON_BARRELS_OBJ = getRSCM("object.cannon_barrels_9") // 9
        val CANNON_COMPLETE_OBJ = getRSCM("object.dwarf_multicannon") // 6

        val CANNON_OWNER_KEY = AttributeKey<org.alter.game.model.PlayerUID>("cannon_owner")
        val CANNON_BALLS_KEY = AttributeKey<Int>("cannon_balls")
        val CANNON_STATE_KEY = AttributeKey<String>("cannon_state") // "base", "stand", "barrels", "complete"
    }

    init {
        // Register option 2 (what the client sends when clicking "set-up")
        onItemOption(item = "item.cannon_base", option = 2) {
            handleSetupCannonBase(player)
        }

        onItemOnObj(item = "item.cannon_stand", obj = "object.cannon_base_7") {
            handleAddStand(player)
        }

        onItemOnObj(item = "item.cannon_barrels", obj = "object.cannon_stand_8") {
            handleAddBarrels(player)
        }

        onItemOnObj(item = "item.cannon_furnace", obj = "object.cannon_barrels_9") {
            handleAddFurnace(player)
        }

        onItemOnObj(item = "item.cannonball", obj = "object.dwarf_multicannon") {
            handleLoadCannonballs(player)
        }

        onObjOption(obj = "object.dwarf_multicannon", option = "Fire") {
            handleFireCannon(player)
        }

        onObjOption(obj = "object.dwarf_multicannon", option = "Pick-up") {
            handlePickupCannon(player)
        }
    }

    private fun handleSetupCannonBase(player: Player) {
        if (!player.inventory.contains(CANNON_BASE_ITEM)) {
            player.message("You need a cannon base to set up a cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_STAND_ITEM)) {
            player.message("You need all 4 cannon pieces (base, stand, barrels, and furnace) to set up a cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_BARRELS_ITEM)) {
            player.message("You need all 4 cannon pieces (base, stand, barrels, and furnace) to set up a cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_FURNACE_ITEM)) {
            player.message("You need all 4 cannon pieces (base, stand, barrels, and furnace) to set up a cannon.")
            return
        }

        val targetTile = player.tile

        val chunk = world.chunks.getOrCreate(targetTile)
        val existingCannon = chunk.getEntities<org.alter.game.model.entity.GameObject>(
            targetTile,
            org.alter.game.model.EntityType.STATIC_OBJECT,
            org.alter.game.model.EntityType.DYNAMIC_OBJECT
        ).firstOrNull { it.id == CANNON_BASE_OBJ || it.id == CANNON_STAND_OBJ || it.id == CANNON_BARRELS_OBJ || it.id == CANNON_COMPLETE_OBJ }
        
        if (existingCannon != null) {
            player.message("There's already a cannon here.")
            return
        }

        if (!player.lock.canItemInteract()) {
            player.message("You can't do that right now.")
            return
        }

        val cannonballsInInventory = player.inventory.getItemCount(CANNONBALL_ITEM)
        val ballsToLoad = if (cannonballsInInventory > 0) {
            minOf(cannonballsInInventory, 30)
        } else {
            0
        }

        player.lock()
        player.animate(827) // Setting up animation
        player.message("You set up the cannon.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            
            // Remove all 4 cannon pieces
            val removedBase = player.inventory.remove(CANNON_BASE_ITEM, 1).hasSucceeded()
            val removedStand = player.inventory.remove(CANNON_STAND_ITEM, 1).hasSucceeded()
            val removedBarrels = player.inventory.remove(CANNON_BARRELS_ITEM, 1).hasSucceeded()
            val removedFurnace = player.inventory.remove(CANNON_FURNACE_ITEM, 1).hasSucceeded()
            
            if (removedBase && removedStand && removedBarrels && removedFurnace) {
                // Spawn the complete cannon directly
                val cannonComplete = DynamicObject(CANNON_COMPLETE_OBJ, 10, 0, targetTile)
                cannonComplete.attr[CANNON_OWNER_KEY] = player.uid
                cannonComplete.attr[CANNON_STATE_KEY] = "complete"
                
                // Load cannonballs if player has them
                if (ballsToLoad > 0) {
                    player.inventory.remove(CANNONBALL_ITEM, ballsToLoad)
                    cannonComplete.attr[CANNON_BALLS_KEY] = ballsToLoad
                    player.message("The cannon is now complete and loaded with $ballsToLoad cannonball${if (ballsToLoad > 1) "s" else ""}!")
                } else {
                    cannonComplete.attr[CANNON_BALLS_KEY] = 0
                    player.message("The cannon is now complete and ready to fire!")
                }
                
                world.spawn(cannonComplete)
                updateCannonVarps(player, targetTile, ballsToLoad)
            } else {
                player.message("Failed to set up the cannon. Make sure you have all 4 pieces.")
            }
            player.unlock()
        }
    }

    private fun handleAddStand(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_STATE_KEY] != "base") {
            player.message("You need to set up the cannon base first.")
            return
        }

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_STAND_ITEM)) {
            player.message("You need a cannon stand.")
            return
        }

        player.lock()
        player.animate(827)
        player.message("You add the cannon stand.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            if (player.inventory.remove(CANNON_STAND_ITEM, 1).hasSucceeded()) {
                world.remove(obj)
                val cannonStand = DynamicObject(CANNON_STAND_OBJ, 10, 0, obj.tile)
                cannonStand.attr[CANNON_OWNER_KEY] = player.uid
                cannonStand.attr[CANNON_STATE_KEY] = "stand"
                cannonStand.attr[CANNON_BALLS_KEY] = 0
                world.spawn(cannonStand)
                player.message("The cannon stand is now in place.")
            }
            player.unlock()
        }
    }

    private fun handleAddBarrels(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_STATE_KEY] != "stand") {
            player.message("You need to add the cannon stand first.")
            return
        }

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_BARRELS_ITEM)) {
            player.message("You need cannon barrels.")
            return
        }

        player.lock()
        player.animate(827)
        player.message("You add the cannon barrels.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            if (player.inventory.remove(CANNON_BARRELS_ITEM, 1).hasSucceeded()) {
                world.remove(obj)
                val cannonBarrels = DynamicObject(CANNON_BARRELS_OBJ, 10, 0, obj.tile)
                cannonBarrels.attr[CANNON_OWNER_KEY] = player.uid
                cannonBarrels.attr[CANNON_STATE_KEY] = "barrels"
                cannonBarrels.attr[CANNON_BALLS_KEY] = 0
                world.spawn(cannonBarrels)
                player.message("The cannon barrels are now in place.")
            }
            player.unlock()
        }
    }

    private fun handleAddFurnace(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_STATE_KEY] != "barrels") {
            player.message("You need to add the cannon barrels first.")
            return
        }

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        if (!player.inventory.contains(CANNON_FURNACE_ITEM)) {
            player.message("You need a cannon furnace.")
            return
        }

        player.lock()
        player.animate(827)
        player.message("You add the cannon furnace.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            if (player.inventory.remove(CANNON_FURNACE_ITEM, 1).hasSucceeded()) {
                world.remove(obj)
                val cannonComplete = DynamicObject(CANNON_COMPLETE_OBJ, 10, 0, obj.tile)
                cannonComplete.attr[CANNON_OWNER_KEY] = player.uid
                cannonComplete.attr[CANNON_STATE_KEY] = "complete"
                cannonComplete.attr[CANNON_BALLS_KEY] = 0
                world.spawn(cannonComplete)
                player.message("The cannon is now complete and ready to fire!")
            }
            player.unlock()
        }
    }

    private fun handleLoadCannonballs(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_STATE_KEY] != "complete") {
            player.message("The cannon is not complete.")
            return
        }

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        val cannonballsInInventory = player.inventory.getItemCount(CANNONBALL_ITEM)
        if (cannonballsInInventory == 0) {
            player.message("You don't have any cannonballs.")
            return
        }

        val currentBalls = obj.attr[CANNON_BALLS_KEY] ?: 0
        val maxBalls = 30
        val spaceAvailable = maxBalls - currentBalls

        if (spaceAvailable <= 0) {
            player.message("The cannon is already fully loaded with $maxBalls cannonballs.")
            return
        }

        val ballsToLoad = minOf(cannonballsInInventory, spaceAvailable)

        player.lock()
        player.animate(827)
        player.message("You load $ballsToLoad cannonball${if (ballsToLoad > 1) "s" else ""} into the cannon.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            if (player.inventory.remove(CANNONBALL_ITEM, ballsToLoad).hasSucceeded()) {
                val newBallCount = currentBalls + ballsToLoad
                obj.attr[CANNON_BALLS_KEY] = newBallCount
                player.message("The cannon now has $newBallCount cannonball${if (newBallCount > 1) "s" else ""} loaded.")
                updateCannonVarps(player, obj.tile, newBallCount)
            }
            player.unlock()
        }
    }

    private fun handleFireCannon(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_STATE_KEY] != "complete") {
            player.message("The cannon is not complete.")
            return
        }

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        val cannonballs = obj.attr[CANNON_BALLS_KEY] ?: 0
        if (cannonballs <= 0) {
            player.message("The cannon has no cannonballs loaded.")
            return
        }

        player.message("You fire the cannon!")
        startCannonFiring(player, obj)
    }

    private fun handlePickupCannon(player: Player) {
        val obj = player.getInteractingGameObj() as? DynamicObject ?: return

        if (obj.attr[CANNON_OWNER_KEY] != player.uid) {
            player.message("This is not your cannon.")
            return
        }

        val cannonballs = obj.attr[CANNON_BALLS_KEY] ?: 0
        val requiredSlots = if (cannonballs > 0) 5 else 4

        if (player.inventory.freeSlotCount < requiredSlots) {
            player.message("You need at least $requiredSlots free inventory space${if (requiredSlots > 1) "s" else ""} to pick up the cannon.")
            return
        }

        player.lock()
        player.animate(827)
        player.message("You pick up the cannon.")

        player.queue(TaskPriority.STRONG) {
            wait(2)
            world.remove(obj)

            player.inventory.add(CANNON_BASE_ITEM, 1)
            player.inventory.add(CANNON_STAND_ITEM, 1)
            player.inventory.add(CANNON_BARRELS_ITEM, 1)
            player.inventory.add(CANNON_FURNACE_ITEM, 1)

            if (cannonballs > 0) {
                player.inventory.add(CANNONBALL_ITEM, cannonballs)
                player.message("You retrieve $cannonballs cannonball${if (cannonballs > 1) "s" else ""} from the cannon.")
            }

            player.message("You pick up all the cannon pieces.")
            resetCannonVarps(player)
            player.unlock()
        }
    }

    private fun startCannonFiring(player: Player, cannon: DynamicObject) {
        val cannonTile = cannon.tile
        player.queue(TaskPriority.WEAK) {
            var ballsRemaining = cannon.attr[CANNON_BALLS_KEY] ?: 0

            while (true) {
                // Get the current cannon object from the world (in case it was rotated)
                val currentCannon = world.getObject(cannonTile, type = cannon.type) as? DynamicObject
                if (currentCannon == null || currentCannon.attr[CANNON_OWNER_KEY] != player.uid) {
                    player.message("The cannon is no longer available.")
                    break
                }
                
                // Auto-load cannonballs if the cannon is empty
                if (ballsRemaining <= 0) {
                    ballsRemaining = autoLoadCannonballs(player, currentCannon)
                    if (ballsRemaining <= 0) {
                        player.message("The cannon has run out of cannonballs and you have no more in your inventory.")
                        break
                    }
                }
                
                val nearbyNpcs = mutableListOf<org.alter.game.model.entity.Npc>()
                world.npcs.forEach { npc ->
                    if (npc.tile.getDistance(cannonTile) <= 10 && 
                        npc.isSpawned() && 
                        !npc.isDead() && 
                        !npc.isLocked() &&
                        isCombatNpc(npc)) {
                        nearbyNpcs.add(npc)
                    }
                }

                if (nearbyNpcs.isEmpty()) {
                    player.message("The cannon stops firing - no targets in range.")
                    break
                }

                val target = nearbyNpcs.random()
                fireCannonball(currentCannon, target, player)

                ballsRemaining--
                currentCannon.attr[CANNON_BALLS_KEY] = ballsRemaining
                updateCannonVarps(player, cannonTile, ballsRemaining)

                wait(3)
            }
        }
    }
    
    private suspend fun autoLoadCannonballs(player: Player, cannon: DynamicObject): Int {
        val cannonballsInInventory = player.inventory.getItemCount(CANNONBALL_ITEM)
        if (cannonballsInInventory == 0) {
            return 0
        }

        val currentBalls = cannon.attr[CANNON_BALLS_KEY] ?: 0
        val maxBalls = 30
        val spaceAvailable = maxBalls - currentBalls

        if (spaceAvailable <= 0) {
            return currentBalls
        }

        val ballsToLoad = minOf(cannonballsInInventory, spaceAvailable)
        
        if (player.inventory.remove(CANNONBALL_ITEM, ballsToLoad).hasSucceeded()) {
            val newBallCount = currentBalls + ballsToLoad
            cannon.attr[CANNON_BALLS_KEY] = newBallCount
            updateCannonVarps(player, cannon.tile, newBallCount)
            player.message("The cannon automatically loads $ballsToLoad cannonball${if (ballsToLoad > 1) "s" else ""} from your inventory.")
            return newBallCount
        }
        
        return currentBalls
    }

    private fun fireCannonball(cannon: DynamicObject, target: org.alter.game.model.entity.Npc, owner: Player) {
        val cannonTile = cannon.tile
        // Calculate direction to target and rotate cannon
        val direction = Direction.between(cannonTile, target.getCentreTile())
        val newRotation = directionToRotation(direction)
        
        // Rotate the cannon to face the target
        val oldRot = cannon.rot
        if (oldRot != newRotation) {
            // Save attributes before removing
            val ownerUid = cannon.attr[CANNON_OWNER_KEY]
            val state = cannon.attr[CANNON_STATE_KEY]
            val balls = cannon.attr[CANNON_BALLS_KEY]
            
            world.remove(cannon)
            val rotatedCannon = DynamicObject(
                id = cannon.id,
                type = cannon.type,
                rot = newRotation,
                tile = cannonTile
            )
            // Restore attributes
            if (ownerUid != null) rotatedCannon.attr[CANNON_OWNER_KEY] = ownerUid
            if (state != null) rotatedCannon.attr[CANNON_STATE_KEY] = state
            if (balls != null) rotatedCannon.attr[CANNON_BALLS_KEY] = balls
            
            world.spawn(rotatedCannon)
        }
        
        // Play cannon fire sound
        owner.playSound(Sound.CF_CANNONFIRE)
        
        val projectile = org.alter.game.model.entity.Projectile.Builder()
            .setTiles(start = cannonTile, target = target)
            .setGfx(53)
            .setHeights(startHeight = 100, endHeight = 0)
            .setSlope(angle = 10, steepness = 127)
            .setTimes(delay = 50, lifespan = -1)
            .build()

        world.spawn(projectile)

        val hitDelay = RangedCombatStrategy.getHitDelay(
            cannonTile,
            target.getCentreTile()
        )

        world.queue {
            wait(hitDelay)
            val damage = world.random(1..30)
            // Attribute damage to the cannon owner so they get slayer XP and kill credit
            val hit = target.hit(damage = damage, type = org.alter.api.HitType.HIT, attackersIndex = owner.index)
            target.graphic(52)
            // Play cannon hit sound
            owner.playSound(Sound.CF_CANNONHIT)
            
            // Add damage to damageMap so killcount is tracked
            // This ensures the player gets kill credit when the NPC dies
            if (damage > 0) {
                val actualDamage = Math.min(target.getCurrentHp(), damage)
                target.damageMap.add(owner, actualDamage)
            }
            
            // Award ranged XP for cannon hits (use the damage we dealt, clamped to target's current HP)
            if (damage > 0) {
                addCannonXp(owner, target, damage)
                // Award slayer XP if this NPC is part of the player's slayer task
                addCannonSlayerXp(owner, target, damage)
            }
            
            owner.message("The cannon hits ${target.def.name} for $damage damage.")
        }
    }
    
    private fun addCannonXp(player: Player, target: Npc, damage: Int) {
        val modDamage = Math.min(target.getCurrentHp(), damage)
        val mode = CombatConfigs.getXpMode(player)
        val multiplier = Combat.getNpcXpMultiplier(target)
        
        // Cannon always gives ranged XP, but respect player's XP mode setting
        if (mode == XpMode.RANGED) {
            player.addXpWithPassiveCheck(Skills.RANGED, modDamage * 4.0 * multiplier)
            player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
        } else if (mode == XpMode.SHARED) {
            player.addXpWithPassiveCheck(Skills.RANGED, modDamage * 2.0 * multiplier)
            player.addXpWithPassiveCheck(Skills.DEFENCE, modDamage * 2.0 * multiplier)
            player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
        } else {
            // If player is in melee/magic mode, still give ranged XP since cannon is a ranged weapon
            player.addXpWithPassiveCheck(Skills.RANGED, modDamage * 4.0 * multiplier)
            player.addXpWithPassiveCheck(Skills.HITPOINTS, modDamage * 1.33 * multiplier)
        }
    }
    
    private fun addCannonSlayerXp(player: Player, target: Npc, damage: Int) {
        val taskNpcId = player.attr[Slayer.SLAYER_TASK_ATTR] ?: return
        
        // Get the task NPC definition to compare names
        val taskNpcDef = try {
            getNpc(taskNpcId)
        } catch (e: Exception) {
            null
        }
        
        // Check if the hit NPC matches the assigned NPC ID
        val idMatches = target.id == taskNpcId
        val nameMatches = taskNpcDef != null && target.name.lowercase() == taskNpcDef.name.lowercase()
        
        // Special case: If task is a TzHaar NPC, allow any TzHaar NPC to count
        val tzhaarMatches = if (taskNpcDef != null) {
            val taskNameLower = taskNpcDef.name.lowercase()
            val killedNameLower = target.name.lowercase()
            (taskNameLower.contains("tzhaar") || taskNameLower.contains("tz-haar")) &&
            (killedNameLower.contains("tzhaar") || killedNameLower.contains("tz-haar"))
        } else {
            false
        }
        
        if (idMatches || nameMatches || tzhaarMatches) {
            // Award slayer XP based on damage dealt (proportional to NPC's max HP)
            // Use slayerXp from combat def if available, otherwise use hitpoints
            val slayerXp = if (target.combatDef.slayerXp > 0) {
                target.combatDef.slayerXp
            } else {
                target.combatDef.hitpoints.toDouble().coerceAtLeast(1.0)
            }
            
            // Award XP proportional to damage dealt
            val modDamage = Math.min(target.getCurrentHp(), damage)
            val xpGain = (slayerXp * modDamage) / target.getMaxHp().toDouble()
            if (xpGain > 0) {
                player.addXpWithPassiveCheck(Skills.SLAYER, xpGain)
            }
        }
    }
    
    private fun directionToRotation(direction: Direction): Int {
        return when (direction) {
            Direction.WEST -> 0
            Direction.NORTH -> 1
            Direction.EAST -> 2
            Direction.SOUTH -> 3
            Direction.NORTH_WEST -> 0 // Closest to West
            Direction.NORTH_EAST -> 1 // Closest to North
            Direction.SOUTH_EAST -> 2 // Closest to East
            Direction.SOUTH_WEST -> 3 // Closest to South
            else -> 0
        }
    }

    private fun updateCannonVarps(player: Player, cannonTile: Tile, cannonballCount: Int) {
        player.setVarp(Varp.CANNON_STAGE, 4)
        player.setVarp(Varp.CANNON_BALLS_AMOUNT, cannonballCount)
        player.setVarp(4, cannonTile.as30BitInteger)
    }

    private fun resetCannonVarps(player: Player) {
        player.setVarp(Varp.CANNON_STAGE, 0)
        player.setVarp(Varp.CANNON_BALLS_AMOUNT, 0)
        player.setVarp(4, 0)
    }

    private fun isCombatNpc(npc: org.alter.game.model.entity.Npc): Boolean {
        // Check if NPC has a custom combat definition registered (meaning it's configured for combat)
        val hasCustomCombatDef = world.plugins.npcCombatDefs.containsKey(npc.id)
        
        // Allow targeting if:
        // 1. NPC has custom combat def (configured via setCombatDef), OR
        // 2. NPC is attackable according to its definition (has "Attack" option and combat level > 0)
        // AND hitpoints is not -1 (which means the NPC is disabled)
        return (hasCustomCombatDef || npc.def.isAttackable()) && npc.combatDef.hitpoints != -1
    }

}

