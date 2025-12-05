package org.alter.plugins.content.combat

import org.alter.api.ProjectileType
import org.alter.api.HitType
import org.alter.api.ext.createProjectile
import org.alter.api.ext.prepareAttack
import org.alter.api.ext.hit
import org.alter.api.ext.stun
import org.alter.api.ext.playSound
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.Hit
import org.alter.game.model.combat.AttackStyle
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.combat.PawnHit
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Pawn
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.plugins.content.combat.formula.MagicCombatFormula
import org.alter.plugins.content.combat.formula.MeleeCombatFormula
import org.alter.plugins.content.combat.formula.RangedCombatFormula
import org.alter.plugins.content.combat.strategy.MagicCombatStrategy
import org.alter.plugins.content.combat.strategy.RangedCombatStrategy
import net.rsprot.protocol.game.outgoing.sound.SynthSound

/**
 * A universal utility object for implementing boss attacks and mechanics.
 * This object abstracts away the complexities of combat formulas, animations, projectiles,
 * and hit delays, providing a clean and consistent API for creating engaging boss encounters.
 *
 * Usage:
 * Simply call the desired function from your NPC's combat logic.
 * Example: `BossAttacks.melee(npc, target)`
 */
object BossAttacks {

    /**
     * Performs a standard melee attack.
     *
     * This function handles the entire melee attack sequence:
     * 1. Sets the NPC's combat class and style.
     * 2. Plays the attack animation.
     * 3. Calculates accuracy and max hit using standard formulas (unless overridden).
     * 4. Deals damage after a standard 1-tick delay.
     * 5. Invokes an optional callback when the hit lands.
     *
     * @param npc The NPC performing the attack.
     * @param target The target Pawn (Player or NPC) being attacked.
     * @param anim The animation ID to play. Defaults to -1, which uses the NPC's combat definition animation.
     * @param combatStyle The specific melee style (e.g., CRUSH, SLASH, STAB). Defaults to CRUSH.
     * @param attackStyle The attack style (e.g., AGGRESSIVE, ACCURATE). Defaults to AGGRESSIVE.
     * @param maxHit An optional fixed max hit. If provided, this value is used instead of the calculated max hit.
     * @param accuracyMultiplier A multiplier applied to the NPC's accuracy. Default is 1.0. Use > 1.0 for more accurate attacks.
     * @param damageMultiplier A multiplier applied to the calculated max hit. Default is 1.0. Use > 1.0 for harder hitting attacks.
     * @param onHit An optional callback function that is executed when the hit is processed.
     *              This is useful for applying status effects, graphics, or secondary mechanics upon a successful hit.
     *              The callback receives the `PawnHit` object, allowing you to check if the hit landed (`hit.landHit`).
     */
    fun melee(
        npc: Npc,
        target: Pawn,
        anim: Int = -1,
        combatStyle: CombatStyle = CombatStyle.CRUSH,
        attackStyle: AttackStyle = AttackStyle.AGGRESSIVE,
        maxHit: Int? = null,
        accuracyMultiplier: Double = 1.0,
        damageMultiplier: Double = 1.0,
        onHit: ((PawnHit) -> Unit)? = null
    ) {
        // Prepare the NPC's combat state for a melee attack, setting class and styles.
        npc.prepareAttack(CombatClass.MELEE, combatStyle, attackStyle)
        
        // Play the attack animation. If anim is -1, use the NPC's default attack animation.
        if (anim != -1) npc.animate(anim) else npc.animate(npc.combatDef.attackAnimation)
        
        // Get the melee combat formula to calculate accuracy and damage.
        val formula = MeleeCombatFormula
        // Calculate accuracy based on stats and apply the multiplier.
        val accuracy = formula.getAccuracy(npc, target) * accuracyMultiplier
        // Calculate max hit. If a fixed maxHit is provided, use it; otherwise calculate it and apply multiplier.
        val calculatedMaxHit = maxHit ?: (formula.getMaxHit(npc, target) * damageMultiplier).toInt()
        // Determine if the hit lands by comparing accuracy to a random roll.
        val landHit = accuracy >= npc.world.randomDouble()
        
        // Deal the hit with a 1-tick delay (standard for melee).
        npc.dealHit(target = target, maxHit = calculatedMaxHit, landHit = landHit, delay = 1) { hit ->
            // Invoke the onHit callback if it was provided, passing the hit object.
            onHit?.invoke(hit)
        }
    }

    /**
     * Performs a standard ranged attack with a projectile.
     *
     * This function manages the ranged attack sequence:
     * 1. Sets the NPC's combat class to RANGED.
     * 2. Plays the attack animation.
     * 3. Creates and spawns a projectile traveling from the NPC to the target.
     * 4. Calculates the correct hit delay based on distance.
     * 5. Calculates accuracy and damage using ranged formulas.
     * 6. Deals damage after the calculated delay.
     *
     * @param npc The NPC performing the attack.
     * @param target The target Pawn.
     * @param anim The animation ID to play. Defaults to -1 (uses combat def).
     * @param projectile The graphic ID of the projectile to fire.
     * @param startHeight The starting height of the projectile. Default 43.
     * @param endHeight The ending height of the projectile. Default 31.
     * @param delay The delay before the projectile starts moving. Default 51.
     * @param angle The angle of the projectile arc. Default 15.
     * @param steepness The steepness of the projectile arc. Default 127.
     * @param combatStyle The combat style. Defaults to RANGED.
     * @param attackStyle The attack style. Defaults to ACCURATE.
     * @param maxHit Optional fixed max hit.
     * @param accuracyMultiplier Accuracy multiplier.
     * @param damageMultiplier Damage multiplier.
     * @param onHit Optional callback executed when the hit lands.
     */
    fun ranged(
        npc: Npc,
        target: Pawn,
        anim: Int = -1,
        projectile: Int,
        startHeight: Int = 43,
        endHeight: Int = 31,
        delay: Int = 51,
        angle: Int = 15,
        steepness: Int = 127,
        combatStyle: CombatStyle = CombatStyle.RANGED,
        attackStyle: AttackStyle = AttackStyle.ACCURATE,
        maxHit: Int? = null,
        accuracyMultiplier: Double = 1.0,
        damageMultiplier: Double = 1.0,
        onHit: ((PawnHit) -> Unit)? = null
    ) {
        // Prepare the NPC's combat state for a ranged attack.
        npc.prepareAttack(CombatClass.RANGED, combatStyle, attackStyle)
        
        // Play the attack animation. If anim is -1, use the NPC's default attack animation.
        if (anim != -1) npc.animate(anim) else npc.animate(npc.combatDef.attackAnimation)
        
        // Create the projectile object with the specified parameters.
        val proj = npc.createProjectile(
            target = target,
            gfx = projectile,
            startHeight = startHeight,
            endHeight = endHeight,
            delay = delay,
            angle = angle,
            steepness = steepness
        )
        // Spawn the projectile in the world so it becomes visible.
        npc.world.spawn(proj)
        
        // Calculate how many ticks it takes for the projectile to reach the target.
        val hitDelay = RangedCombatStrategy.getHitDelay(npc.tile, target.tile)
        
        // Get the ranged combat formula.
        val formula = RangedCombatFormula
        // Calculate accuracy based on stats and apply the multiplier.
        val accuracy = formula.getAccuracy(npc, target) * accuracyMultiplier
        // Calculate max hit. If a fixed maxHit is provided, use it; otherwise calculate it and apply multiplier.
        val calculatedMaxHit = maxHit ?: (formula.getMaxHit(npc, target) * damageMultiplier).toInt()
        // Determine if the hit lands by comparing accuracy to a random roll.
        val landHit = accuracy >= npc.world.randomDouble()
        
        // Deal the hit with the calculated delay.
        npc.dealHit(target = target, maxHit = calculatedMaxHit, landHit = landHit, delay = hitDelay) { hit ->
            // Invoke the onHit callback if it was provided.
            onHit?.invoke(hit)
        }
    }

    /**
     * Performs a standard magic attack with a projectile.
     *
     * Similar to the ranged attack, but uses Magic combat formulas and strategies.
     *
     * @param npc The NPC performing the attack.
     * @param target The target Pawn.
     * @param anim The animation ID to play. Defaults to -1.
     * @param projectile The graphic ID of the magic projectile.
     * @param startHeight Starting height of projectile. Default 43.
     * @param endHeight Ending height of projectile. Default 31.
     * @param delay Delay before projectile start. Default 51.
     * @param angle Projectile angle. Default 15.
     * @param steepness Projectile steepness. Default 127.
     * @param combatStyle Combat style. Defaults to MAGIC.
     * @param attackStyle Attack style. Defaults to ACCURATE.
     * @param maxHit Optional fixed max hit.
     * @param accuracyMultiplier Accuracy multiplier.
     * @param damageMultiplier Damage multiplier.
     * @param onHit Optional callback executed when the hit lands.
     */
    fun magic(
        npc: Npc,
        target: Pawn,
        anim: Int = -1,
        projectile: Int,
        startHeight: Int = 43,
        endHeight: Int = 31,
        delay: Int = 51,
        angle: Int = 15,
        steepness: Int = 127,
        combatStyle: CombatStyle = CombatStyle.MAGIC,
        attackStyle: AttackStyle = AttackStyle.ACCURATE,
        maxHit: Int? = null,
        accuracyMultiplier: Double = 1.0,
        damageMultiplier: Double = 1.0,
        onHit: ((PawnHit) -> Unit)? = null
    ) {
        // Prepare the NPC's combat state for a magic attack.
        npc.prepareAttack(CombatClass.MAGIC, combatStyle, attackStyle)
        
        // Play the attack animation. If anim is -1, use the NPC's default attack animation.
        if (anim != -1) npc.animate(anim) else npc.animate(npc.combatDef.attackAnimation)
        
        // Create the projectile object with the specified parameters.
        val proj = npc.createProjectile(
            target = target,
            gfx = projectile,
            startHeight = startHeight,
            endHeight = endHeight,
            delay = delay,
            angle = angle,
            steepness = steepness
        )
        // Spawn the projectile in the world.
        npc.world.spawn(proj)
        
        // Calculate how many ticks it takes for the spell to reach the target.
        val hitDelay = MagicCombatStrategy.getHitDelay(npc.tile, target.tile)
        
        // Get the magic combat formula.
        val formula = MagicCombatFormula
        // Calculate accuracy based on stats and apply the multiplier.
        val accuracy = formula.getAccuracy(npc, target) * accuracyMultiplier
        // Calculate max hit. If a fixed maxHit is provided, use it; otherwise calculate it and apply multiplier.
        val calculatedMaxHit = maxHit ?: (formula.getMaxHit(npc, target) * damageMultiplier).toInt()
        // Determine if the hit lands by comparing accuracy to a random roll.
        val landHit = accuracy >= npc.world.randomDouble()
        
        // Deal the hit with the calculated delay.
        npc.dealHit(target = target, maxHit = calculatedMaxHit, landHit = landHit, delay = hitDelay) { hit ->
            // Invoke the onHit callback if it was provided.
            onHit?.invoke(hit)
        }
    }
    
    /**
     * Performs an unblockable attack.
     *
     * This attack ignores accuracy checks and defensive stats, guaranteeing a hit.
     * Useful for special boss mechanics, "ultimate" attacks, or environmental damage.
     *
     * @param npc The NPC performing the attack.
     * @param target The target Pawn.
     * @param anim Optional animation to play.
     * @param projectile Optional projectile graphic ID. If -1, no projectile is spawned.
     * @param damage The fixed amount of damage to deal.
     * @param delay The delay in ticks before the damage is applied. Default is 1.
     * @param onHit Optional callback executed when the hit lands.
     */
    fun unblockable(
        npc: Npc,
        target: Pawn,
        anim: Int = -1,
        projectile: Int = -1,
        damage: Int,
        delay: Int = 1,
        onHit: ((PawnHit) -> Unit)? = null
    ) {
        // Play the animation if one was provided.
        if (anim != -1) npc.animate(anim)
        
        // If a projectile ID was provided, create and spawn it.
        if (projectile != -1) {
            val proj = npc.createProjectile(
                target = target,
                gfx = projectile,
                startHeight = 43,
                endHeight = 31,
                delay = 51,
                angle = 15,
                steepness = 127
            )
            npc.world.spawn(proj)
        }
        
        // Directly apply a hit to the target with the specified damage and delay.
        // This bypasses the dealHit function which calculates accuracy/defense.
        target.hit(damage = damage, type = HitType.HIT, delay = delay).also { hit ->
            // Add an action to the hit to invoke the callback when the hit is processed.
            hit.addAction { 
                onHit?.invoke(PawnHit(hit, true)) 
            }
        }
    }

    /**
     * Performs an Area of Effect (AoE) attack.
     *
     * Hits all players within a specified radius of a center tile.
     * Can optionally spawn projectiles for each target.
     *
     * @param npc The NPC performing the attack.
     * @param center The center Tile of the AoE.
     * @param radius The radius in tiles to search for targets.
     * @param anim Optional animation to play.
     * @param projectile Optional projectile graphic ID to fire at each target.
     * @param combatClass The combat class (MELEE, RANGED, MAGIC) to determine hit delays and formulas.
     * @param maxHit Optional fixed max hit.
     * @param damageMultiplier Damage multiplier.
     * @param onHit Optional callback executed for EACH target hit.
     */
    fun aoe(
        npc: Npc,
        center: Tile,
        radius: Int,
        anim: Int = -1,
        projectile: Int = -1,
        combatClass: CombatClass = CombatClass.MAGIC,
        maxHit: Int? = null,
        damageMultiplier: Double = 1.0,
        onHit: ((Pawn, PawnHit) -> Unit)? = null
    ) {
        // Play the animation if one was provided.
        if (anim != -1) npc.animate(anim)
        
        // Create a list to hold all targets found within the radius.
        val targets = mutableListOf<Pawn>()
        // Iterate through all players in the world.
        npc.world.players.forEach { player ->
            // Check if the player is within the radius and is alive.
            if (player.tile.isWithinRadius(center, radius) && player.isAlive()) {
                targets.add(player)
            }
        }
        // Note: You can add logic here to include NPCs if needed.
        
        // Iterate through each identified target.
        targets.forEach { target ->
             // If a projectile ID was provided, spawn a projectile for this specific target.
             if (projectile != -1) {
                val proj = npc.createProjectile(
                    target = target,
                    gfx = projectile,
                    startHeight = 43,
                    endHeight = 31,
                    delay = 51,
                    angle = 15,
                    steepness = 127
                )
                npc.world.spawn(proj)
            }
            
            // Determine the hit delay based on the combat class.
            // Melee is instant (1 tick), Magic/Ranged depends on distance.
            val hitDelay = if (combatClass == CombatClass.MELEE) 1 else MagicCombatStrategy.getHitDelay(npc.tile, target.tile)
            
            // Select the appropriate combat formula based on the class.
            val formula = when(combatClass) {
                CombatClass.MELEE -> MeleeCombatFormula
                CombatClass.RANGED -> RangedCombatFormula
                else -> MagicCombatFormula
            }
            
            // Calculate accuracy using the selected formula.
            val accuracy = formula.getAccuracy(npc, target)
            // Calculate max hit using the formula and applying the multiplier.
            val calculatedMaxHit = maxHit ?: (formula.getMaxHit(npc, target) * damageMultiplier).toInt()
            // Determine if the hit lands.
            val landHit = accuracy >= npc.world.randomDouble()
            
            // Deal the hit to the target.
            npc.dealHit(target = target, maxHit = calculatedMaxHit, landHit = landHit, delay = hitDelay) { hit ->
                // Invoke the callback for this specific target and hit.
                onHit?.invoke(target, hit)
            }
        }
    }



    /**
     * Knocks back the target from the NPC.
     *
     * Moves the target away from the NPC by a random distance and deals damage.
     * Useful for "Get off me" mechanics.
     *
     * @param npc The NPC performing the knockback.
     * @param target The target Pawn to knock back.
     * @param distance The range of tiles to knock back (e.g., 3..5).
     * @param damage The range of damage to deal (e.g., 5..15).
     * @param graphic The graphic to play on the target (default 157).
     * @param sound The sound to play on the target (default 247).
     */
    fun knockback(
        npc: Npc,
        target: Pawn,
        distance: IntRange = 3..5,
        damage: IntRange = 5..15,
        graphic: Int = 157,
        sound: Int = 247
    ) {
        // Only players can be knocked back safely in this implementation.
        if (target !is Player) return

        // Play the knockback graphic on the target.
        target.graphic(graphic)
        // Play the sound effect if one was provided.
        if (sound != -1) {
            target.write(SynthSound(id = sound, loops = 1, delay = 0))
        }

        // Get the current tiles of the NPC and the target.
        val npcTile = npc.tile
        val playerTile = target.tile
        // Determine the direction from the NPC to the player.
        val direction = Direction.between(npcTile, playerTile)
        
        // Randomly select a distance to knock back within the provided range.
        val knockbackDist = npc.world.random(distance)
        
        // Calculate the destination tile based on the direction and distance.
        var endTile = when (direction) {
            Direction.NORTH -> Tile(playerTile.x, playerTile.z + knockbackDist, playerTile.height)
            Direction.SOUTH -> Tile(playerTile.x, playerTile.z - knockbackDist, playerTile.height)
            Direction.EAST -> Tile(playerTile.x + knockbackDist, playerTile.z, playerTile.height)
            Direction.WEST -> Tile(playerTile.x - knockbackDist, playerTile.z, playerTile.height)
            Direction.NORTH_EAST -> Tile(playerTile.x + knockbackDist, playerTile.z + knockbackDist, playerTile.height)
            Direction.NORTH_WEST -> Tile(playerTile.x - knockbackDist, playerTile.z + knockbackDist, playerTile.height)
            Direction.SOUTH_EAST -> Tile(playerTile.x + knockbackDist, playerTile.z - knockbackDist, playerTile.height)
            Direction.SOUTH_WEST -> Tile(playerTile.x - knockbackDist, playerTile.z - knockbackDist, playerTile.height)
            else -> Tile(playerTile.x, playerTile.z + knockbackDist, playerTile.height)
        }

        // Safety check: ensure the target tile is in the same region to prevent loading issues.
        if (endTile.regionId != playerTile.regionId) {
            endTile = playerTile
        }

        // Move the player to the calculated end tile.
        target.moveTo(endTile)
        
        // Queue a hit to be dealt after 1 tick (representing the impact).
        target.queue {
            wait(1)
            target.hit(npc.world.random(damage), type = HitType.HIT, delay = 0)
        }
    }

    /**
     * Stuns the target for a specified duration.
     *
     * @param target The target Pawn to stun.
     * @param cycles The number of game ticks to stun the target for.
     * @param graphic The graphic to play on the target (default 80).
     */
    fun stun(target: Pawn, cycles: Int, graphic: Int = 80) {
        // Only players can be stunned.
        if (target is Player) {
            // Apply the stun effect for the specified duration.
            target.stun(cycles)
            // Play the stun graphic.
            target.graphic(graphic)
        }
    }

    /**
     * Teleports the target to a random adjacent tile next to the NPC.
     *
     * @param npc The NPC.
     * @param target The target Pawn to teleport.
     * @param damage The range of damage to deal upon teleporting (e.g., 0..8).
     */
    fun teleportTargetToNpc(npc: Npc, target: Pawn, damage: IntRange = 0..8) {
        // Create a list of all valid tiles surrounding the NPC (3x3 area).
        val surroundingTiles = mutableListOf<Tile>()
        for (x in -1..1) {
            for (z in -1..1) {
                // Skip the center tile (where the NPC is).
                if (x == 0 && z == 0) continue
                surroundingTiles.add(npc.tile.transform(x, z))
            }
        }
        
        // If there are valid tiles available...
        if (surroundingTiles.isNotEmpty()) {
            // Pick a random tile from the list.
            val tile = surroundingTiles.random()
            // Play the "teleport out" graphic on the target.
            target.graphic(1577) 
            // Move the target to the selected tile.
            target.moveTo(tile)
            // Play the "teleport in" graphic on the target after 1 tick.
            target.graphic(1578, delay = 1) 
            
            // Calculate random damage.
            val dmg = npc.world.random(damage)
            // If damage is greater than 0, apply it.
            if (dmg > 0) {
                target.hit(dmg, type = HitType.HIT, delay = 1)
            }
        }
    }
}
