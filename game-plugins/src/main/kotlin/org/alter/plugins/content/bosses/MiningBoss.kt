package org.alter.plugins.content.bosses.mining

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.attr.GROUNDITEM_PICKUP_TRANSACTION
import org.alter.game.model.combat.*
import org.alter.game.model.entity.*
import org.alter.plugins.content.mechanics.prayer.Prayers
import org.alter.game.model.queue.*
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.rscm.RSCM.getRSCM

// 👉 CHANGE "MyBossPlugin" to your boss name!
class MiningBossPlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

    companion object {
        private val TRAP_CHECK_TIMER = TimerKey()
        private const val TRAP_TILE_X = 2977
        private const val TRAP_TILE_Z = 3241
        private const val TRAP_CHECK_INTERVAL = 1 // Check every cycle for immediate response
        private const val TRAP_COOLDOWN_MS = 2000 // 2 second cooldown between trap triggers per player
        private val TRAP_LAST_TRIGGER_ATTR = AttributeKey<Long>("mining_boss_trap_last_trigger")
    }

    init {
        // 1. SPAWN THE BOSS 📍
        // This makes the boss appear in the game at coordinates (2977, 3238)
        spawnNpc("npc.rock_925", x = 2977, z = 3238, walkRadius = 5)

        // Spawn a decoration item nearby (a pickaxe)
        // This is a trap item - players cannot pick it up
        spawnItem("item.gilded_pickaxe", 1, 2977, 3241)
        
        // Prevent players from picking up the gilded pickaxe (it's a trap decoration)
        setGroundItemCondition("item.gilded_pickaxe") {
            val player = ctx as Player
            player.message("You cannot pick up this pickaxe - it's a trap!")
            false // Prevent pickup
        }

        // 2. DEFINE STATS & DROPS 📊
        // This tells the game how strong the boss is and what it drops
        setCombatDef("npc.rock_925") {
            configs {
                attackSpeed = 4      // How fast it attacks
                respawnDelay = 50     // How long before it comes back after dying
            }
            aggro {
                radius = 10          // How far it can see players
                searchDelay = 2       // How often it looks for players
                alwaysAggro()         // Attacks players even if they're high level
            }
            stats {
                hitpoints = 100      // How much health it has
                attack = 50          // How accurate it is
                strength = 6000      // How hard it hits
                defence = 150         // How well it blocks
                magic = 300
                ranged = 1
            }
            bonuses {
                defenceStab = 50
                defenceSlash = 50
                defenceCrush = 50
                defenceMagic = 100
                defenceRanged = 50
            }
            anims {
                attack = 3353        // Animation when it attacks
                block = 424          // Animation when it blocks
                death = 836          // Animation when it dies
            }
            
            // 🔊 SOUNDS: Make the boss sound epic!
            sound {
                attackSound = Sound.ROCK_CRAB_ATTACK  // Rock attack sound
                attackArea = true                    // All nearby players hear it
                attackVolume = 50                    // Volume level (0-100)
                attackRadius = 10                    // How far the sound travels
                
                blockSound = Sound.ROCK_CRAB_HIT     // Rock hit sound when blocking
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.ROCK_CRAB_DEATH   // Rock death sound
                deathArea = true                     // Everyone nearby hears the death
                deathVolume = 60
                deathRadius = 12
            }
            drops {
                // Always drops this item when killed
                always { add("item.runite_ore_noted", 1) }
                
                // Main drops - these are random
                main(weight = 500) {
                    // Common ores
                    add("item.copper_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.tin_ore_noted", min = 50, max = 200, weight = 40)
                    add("item.iron_ore_noted", min = 30, max = 150, weight = 35)
                    add("item.coal_noted", min = 25, max = 100, weight = 35)

                    // Better ores
                    add("item.silver_ore_noted", min = 20, max = 80, weight = 30)
                    add("item.gold_ore_noted", min = 15, max = 60, weight = 28)
                    add("item.mithril_ore_noted", min = 10, max = 50, weight = 25)
                    add("item.adamantite_ore_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_ore_noted", min = 3, max = 20, weight = 15)

                    // Bars
                    add("item.iron_bar_noted", min = 20, max = 100, weight = 30)
                    add("item.mithril_bar_noted", min = 10, max = 50, weight = 25)
                    add("item.adamantite_bar_noted", min = 5, max = 30, weight = 20)
                    add("item.runite_bar_noted", min = 3, max = 20, weight = 15)

                    // Pickaxes
                    add("item.bronze_pickaxe_noted", min = 5, max = 25, weight = 35)
                    add("item.iron_pickaxe_noted", min = 5, max = 20, weight = 32)
                    add("item.steel_pickaxe_noted", min = 3, max = 15, weight = 28)
                    add("item.mithril_pickaxe_noted", min = 2, max = 10, weight = 25)
                    add("item.adamant_pickaxe_noted", min = 1, max = 8, weight = 20)
                    add("item.rune_pickaxe_noted", min = 1, max = 5, weight = 15)
                    add("item.dragon_pickaxe", min = 1, max = 2, weight = 5)      // Rare!
                    add("item.gilded_pickaxe", min = 1, max = 1, weight = 3)      // Very rare!
                    add("item.infernal_pickaxe", min = 1, max = 1, weight = 2)    // Ultra rare!

                    // Other drops
                    add("item.coins", min = 5000, max = 10000, weight = 30)
                    add("item.dragon_scimitar", min = 1, max = 1, weight = 8)      // Rare weapon
                }
            }
        }

        // 3. COMBAT LOGIC ⚔️
        // When the boss starts fighting, run the combat loop
        // Also check if player is attacking with a pickaxe
        onNpcCombat("npc.rock_925") { npc.queue { combatLoop() } }
        
        // Set up trap check timer on world initialization
        // This ensures the trap works independently of boss state
        onWorldInit {
            world.timers[TRAP_CHECK_TIMER] = TRAP_CHECK_INTERVAL
        }
        
        // Trap check: Periodically check all players on the trap tile
        // This works even when the boss is dead or not spawned
        onTimer(TRAP_CHECK_TIMER) {
            // Check all players in the world
            world.players.forEach { player ->
                if (player.initiated && !player.isDead()) {
                    // Check if player is standing on the trap tile
                    if (player.tile.x == TRAP_TILE_X && player.tile.z == TRAP_TILE_Z) {
                        // Check cooldown to prevent spam
                        val currentTime = System.currentTimeMillis()
                        val lastTrigger = player.attr[TRAP_LAST_TRIGGER_ATTR] ?: 0L
                        
                        if (currentTime - lastTrigger >= TRAP_COOLDOWN_MS) {
                            // Trigger the trap!
                            player.hit(135)
                            player.graphic(100)
                            player.message("IT'S A TRAP! The pickaxe was fake!")
                            player.attr[TRAP_LAST_TRIGGER_ATTR] = currentTime
                        }
                    }
                }
            }
            // Reset timer to check again next cycle
            world.timers[TRAP_CHECK_TIMER] = TRAP_CHECK_INTERVAL
        }
        
        // 4. HANDLE PICKAXE ON DEATH 🪦
        // If a player dies with the gilded pickaxe, remove it (they shouldn't have it)
        onPlayerDeath {
            val player = this.player
            val pickaxeId = getRSCM("item.gilded_pickaxe")
            
            // Check inventory
            val inventoryCount = player.inventory.getItemCount(pickaxeId)
            if (inventoryCount > 0) {
                player.inventory.remove(pickaxeId, inventoryCount)
                player.message("The gilded pickaxe was lost upon death - it was a trap!")
            }
            
            // Check equipment
            val weapon = player.getEquipment(EquipmentType.WEAPON)
            if (weapon?.id == pickaxeId) {
                player.equipment[EquipmentType.WEAPON.id] = null
                player.equipment.dirty = true
                player.message("The gilded pickaxe was lost upon death - it was a trap!")
            }
        }
        
        // 5. SPECIAL FUNCTIONALITY 🎁
        // Give mining XP when the boss is killed to all players who dealt damage
        onNpcDeath("npc.rock_925") {
            val npc = this.npc
            
            // Get all players who dealt damage to the boss
            val playersWhoDamaged = mutableListOf<Player>()
            npc.world.players.forEach { player ->
                if (player.initiated && !player.isDead() && npc.damageMap.getDamageFrom(player) > 0) {
                    playersWhoDamaged.add(player)
                }
            }
            
            if (playersWhoDamaged.isEmpty()) {
                return@onNpcDeath // No players dealt damage
            }
            
            // Give mining XP to all players who dealt damage (1000 XP for 100 HP boss)
            val miningXp = npc.combatDef.hitpoints * 10.0
            playersWhoDamaged.forEach { player ->
                player.addXp(Skills.MINING, miningXp)
                player.message("<col=00ff00>You gain ${miningXp.toInt()} Mining experience for defeating the Rock!</col>")
            }
        }
    }

    // This is the brain of the boss! 🧠
    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return
        var lastMessageTime = 0L

        // Keep fighting while boss is alive
        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)

            // Turn off player's prayers
            Prayers.deactivateAll(target)

            // Check if player is attacking with a pickaxe
            // If they're not using a pickaxe, they deal 0 damage
            val hasPickaxe = target.hasWeaponType(WeaponType.PICKAXE)
            
            if (!hasPickaxe) {
                // Show message above boss's head (with cooldown to avoid spam)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastMessageTime > 3000) { // 3 second cooldown
                    npc.forceChat("Haha! Only a pickaxe can hurt me!")
                    lastMessageTime = currentTime
                }
                // Set damage multiplier to 0 so player deals no damage
                npc.attr[Combat.DAMAGE_TAKE_MULTIPLIER] = 0.0
            } else {
                // Reset damage multiplier if they have a pickaxe
                npc.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
            }

            // Try to attack - use ranged/magic if line of sight is blocked by obstacles
            if (npc.isAttackDelayReady()) {
                val targetDistance = npc.tile.getDistance(target.tile)
                val inMeleeRange = targetDistance <= 1
                
                // Check if we can use melee (close enough and have line of sight for melee)
                val canMelee = if (inMeleeRange) {
                    // Check line of sight for melee attacks (projectile = false)
                    npc.hasLineOfSightTo(target, projectile = false, maximumDistance = 1)
                } else {
                    // Try to move into melee range
                    npc.moveToAttackRange(this, target, distance = 1, projectile = false)
                }
                
                if (canMelee) {
                    // Attack with melee! Can hit up to 110 damage
                    BossAttacks.melee(npc, target, maxHit = 110, anim = 422)
                    npc.postAttackLogic(target)
                } else {
                    // Line of sight blocked for melee (e.g., by a rock) - use magic attack
                    // Magic projectiles can sometimes go through obstacles better than ranged
                    // Check if we're within magic attack range (10 tiles)
                    val inMagicRange = targetDistance <= 10
                    
                    if (inMagicRange) {
                        // Check if we have line of sight for magic attacks (projectile = true)
                        val canMagicSight = npc.hasLineOfSightTo(target, projectile = true, maximumDistance = 10)
                        
                        if (canMagicSight) {
                            // Use magic attack that can go through obstacles
                            BossAttacks.magic(
                                npc = npc,
                                target = target,
                                projectile = 100,  // Magic projectile graphic
                                maxHit = 110,
                                anim = 1979  // Magic attack animation
                            )
                            npc.postAttackLogic(target)
                        } else {
                            // Even magic line of sight is blocked - use AoE attack centered on player
                            // This bypasses line of sight requirements
                            BossAttacks.aoe(
                                npc = npc,
                                center = target.tile,  // Explode on player's location
                                radius = 1,            // Small radius to hit just the player
                                combatClass = CombatClass.MAGIC,
                                maxHit = 110,
                                projectile = 100       // Magic explosion graphic
                            )
                            npc.postAttackLogic(target)
                        }
                    } else {
                        // Too far away - try to move closer
                        npc.moveToAttackRange(this, target, distance = 10, projectile = true)
                    }
                }
            }

            wait(1)
            target = npc.getCombatTarget() as? Player ?: break
        }

        // Clean up when combat ends
        npc.attr.remove(Combat.DAMAGE_TAKE_MULTIPLIER)
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
