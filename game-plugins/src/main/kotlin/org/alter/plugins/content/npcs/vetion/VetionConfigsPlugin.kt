package org.alter.plugins.content.npcs.vetion

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
import org.alter.rscm.RSCM.getRSCM
import dev.openrune.cache.CacheManager.getAnim
import org.alter.game.info.NpcInfo
import org.alter.game.model.queue.TaskPriority
import org.alter.plugins.content.combat.Combat
import org.alter.game.model.move.stopMovement
import org.alter.api.cfg.Sound
import org.alter.game.model.weightedTableBuilder.roll
import org.alter.plugins.content.mechanics.bosskillcount.BossKillcountPlugin

class VetionConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
        
    init {
        // Set the spawning area as multi-combat (wilderness region)
        setMultiCombatRegion(region = 12342) // Wilderness region around Vet'ion location

        // Spawn Vet'ion Phase 1 (Purple) at Bone Yard in wilderness  
        spawnNpc("npc.vetion", x = 3229, z = 3788, walkRadius = 3)

        // Configure Phase 1 (Purple) - 6611
        setCombatDef("npc.vetion") {
            configs {
                attackSpeed = 3 // Fast attack speed (3 ticks = 1.8 seconds between attacks)
                respawnDelay = 120 // 2 minute respawn delay
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 100
                attack = 270
                strength = 250
                defence = 270
                magic = 200
                ranged = 200
            }

            bonuses {
                defenceStab = 65
                defenceSlash = 65
                defenceCrush = 65
                defenceMagic = 40
                defenceRanged = 65
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
            }

            anims {
                attack = 5485 // Vet'ion attack animation
                block = -1 // No block animation (prevents weird damage animation)
                death = 5487
            }

            // Sounds for Phase 1
            sound {
                attackSound = Sound.SKELETAL_HELLHOUND_ATTACK // Powerful skeletal attack sound
                attackArea = true // Area sound so all nearby players can hear
                attackVolume = 50
                attackRadius = 10
                
                blockSound = Sound.SKELETAL_HELLHOUND_HIT // Skeletal hit sound
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.SKELETAL_HELLHOUND_DEATH // Skeletal death sound
                deathArea = true // Area sound for death
                deathVolume = 60
                deathRadius = 12
            }

            // Phase 1 does NOT drop loot - only phase 2 drops loot
            drops {
                // No drops for phase 1
            }
        }

        // Configure Phase 2 (Orange/Reborn) - 6612
        setCombatDef("npc.vetion_6612") {
            configs {
                attackSpeed = 3 // Fast attack speed (3 ticks = 1.8 seconds between attacks)
                respawnDelay = 120 // Not used - Phase 2 never respawns
            }

            aggro {
                radius = 16
                searchDelay = 1
            }

            stats {
                hitpoints = 100
                attack = 270
                strength = 250
                defence = 270
                magic = 200
                ranged = 200
            }

            bonuses {
                defenceStab = 65
                defenceSlash = 65
                defenceCrush = 65
                defenceMagic = 40
                defenceRanged = 65
                attackStab = 80
                attackSlash = 80
                attackCrush = 80
            }

            anims {
                attack = 5485 // Vet'ion attack animation
                block = -1 // No block animation (prevents weird damage animation)
                death = 5487
            }

            // Sounds for Phase 2
            sound {
                attackSound = Sound.SKELETAL_HELLHOUND_ATTACK // Powerful skeletal attack sound
                attackArea = true // Area sound so all nearby players can hear
                attackVolume = 50
                attackRadius = 10
                
                blockSound = Sound.SKELETAL_HELLHOUND_HIT // Skeletal hit sound
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.SKELETAL_HELLHOUND_DEATH // Skeletal death sound
                deathArea = true // Area sound for death
                deathVolume = 60
                deathRadius = 12
            }

            // Phase 2 drops all the loot
            drops {
                always {
                    add("item.big_bones", 1)
                    add("item.larrans_key", min = 1, max = 3)
                }
                
                main(weight = 128) {
                    // Vet'ion signature drops
                    add("item.skull_of_vetion", min = 1, weight = 1) // Ultra rare signature drop
                    
                    // Dragon items (rare)
                    add("item.dragon_pickaxe", min = 1, weight = 1)
                    add("item.dragon_2h_sword", min = 1, weight = 1)
                    add("item.dragon_med_helm", min = 1, weight = 2)
                    add("item.dragon_chainbody", min = 1, weight = 2)
                    add("item.dragon_longsword", min = 1, weight = 2)
                    add("item.dragon_battleaxe", min = 1, weight = 2)
                    
                    // Rune equipment (uncommon)
                    add("item.rune_platebody", min = 1, weight = 4)
                    add("item.rune_platelegs", min = 1, weight = 4)
                    add("item.rune_kiteshield", min = 1, weight = 4)
                    add("item.rune_full_helm", min = 1, weight = 4)
                    add("item.rune_scimitar", min = 1, weight = 5)
                    add("item.rune_longsword", min = 1, weight = 5)
                    add("item.rune_battleaxe", min = 1, weight = 4)
                    add("item.rune_2h_sword", min = 1, weight = 3)
                    
                    // Potions and food
                    add("item.shark", min = 5, max = 10, weight = 15)
                    add("item.prayer_potion4", min = 2, max = 4, weight = 8)
                    add("item.super_combat_potion4", min = 1, max = 3, weight = 6)
                    add("item.saradomin_brew4", min = 2, max = 4, weight = 7)
                    add("item.super_restore4", min = 1, max = 3, weight = 6)
                    
                    // Ammunition and supplies
                    add("item.rune_arrow", min = 150, max = 300, weight = 10)
                    add("item.runite_bolts", min = 25, max = 50, weight = 8)
                    add("item.cannonball", min = 100, max = 200, weight = 12)
                    
                    // Runes (death/bone themed)
                    add("item.death_rune", min = 50, max = 100, weight = 12)
                    add("item.blood_rune", min = 25, max = 50, weight = 10)
                    add("item.soul_rune", min = 20, max = 40, weight = 8)
                    add("item.wrath_rune", min = 15, max = 30, weight = 6)
                    add("item.chaos_rune", min = 75, max = 150, weight = 15)
                    add("item.nature_rune", min = 40, max = 80, weight = 12)
                    
                    // Valuable items and coins
                    add("item.coins_995", min = 15000, max = 35000, weight = 20)
                    add("item.uncut_diamond", min = 2, max = 5, weight = 4)
                    add("item.uncut_dragonstone", min = 1, max = 2, weight = 2)
                    add("item.gold_ore", min = 75, max = 150, weight = 10)
                    
                    // Clue scrolls
                    add("item.clue_scroll_hard", min = 1, weight = 3)
                    add("item.clue_scroll_elite", min = 1, weight = 1)
                    
                    // Bones and skeletal items (thematic)
                    add("item.dragon_bones_noted", min = 5, max = 15, weight = 8)
                    add("item.wyvern_bones", min = 3, max = 8, weight = 6)

                    // NEW ITEMS
                    // Salve amulets (unnoted as they are untradeable/special)
                    add("item.salve_amulet", min = 1, weight = 2)
                    add("item.salve_amulet_e", min = 1, weight = 2)
                    
                    // Bonecrusher & Dragonbone necklace
                    add("item.bonecrusher", min = 1, weight = 2)
                    add("item.dragonbone_necklace_noted", min = 1, weight = 2)
                    
                    // Bone weapons
                    add("item.bone_bolts", min = 50, max = 200, weight = 6)
                    add("item.bone_crossbow", min = 1, weight = 4)
                    
                    // Skeletal Armour (Waterbirth) - Noted
                    add("item.skeletal_helm_noted", min = 1, weight = 4)
                    add("item.skeletal_top_noted", min = 1, weight = 4)
                    add("item.skeletal_bottoms_noted", min = 1, weight = 4)
                    add("item.skeletal_boots_noted", min = 1, weight = 4)
                    add("item.skeletal_gloves_noted", min = 1, weight = 4)
                    
                    // Splitbark Armour - Noted
                    add("item.splitbark_helm_noted", min = 1, weight = 4)
                    add("item.splitbark_body_noted", min = 1, weight = 4)
                    add("item.splitbark_legs_noted", min = 1, weight = 4)
                    add("item.splitbark_gauntlets_noted", min = 1, weight = 4)
                    add("item.splitbark_boots_noted", min = 1, weight = 4)
                    
                    // Shade Robes - Noted
                    add("item.shade_robe_top_noted", min = 1, weight = 4)
                    add("item.shade_robe_noted", min = 1, weight = 4) // Bottom/Robe
                    
                    // Rare Accessories
                    add("item.ring_of_the_gods_noted", min = 1, weight = 1)
                    add("item.ancient_crystal_noted", min = 1, weight = 1)
                    add("item.skeleton_champion_scroll", min = 1, weight = 1)
                    
                    // Noted Bones
                    add("item.big_bones_noted", min = 50, max = 100, weight = 8)
                    add("item.ensouled_giant_head_noted", min = 1, max = 3, weight = 4)
                }
            }
        }

        // Phase transition: When Phase 1 (Purple) dies, spawn Phase 2 (Orange)
        // Use fullNpcDeath to completely override the death behavior
        fullNpcDeath("npc.vetion") {
            val phase1 = ctx as Npc
            val phase1Tile = phase1.tile
            val phase1WalkRadius = phase1.walkRadius
            
            // Capture damage map data before NPC is removed
            // Collect players first to avoid concurrent modification
            val damageData = mutableMapOf<Player, Int>()
            val playersToCheck = mutableListOf<Player>()
            world.players.forEach { player ->
                playersToCheck.add(player)
            }
            playersToCheck.forEach { player ->
                val damage = phase1.damageMap.getDamageFrom(player)
                if (damage > 0) {
                    damageData[player] = damage
                }
            }
            
            // Interrupt queues and stop movement
            phase1.interruptQueues()
            phase1.stopMovement()
            phase1.lock()
            phase1.resetInteractions()
            
            // IMPORTANT: Disable respawning for Phase 1 so it doesn't respawn after removal
            // Phase 1 should never respawn - only Phase 2 should exist after Phase 1 dies
            phase1.respawns = false
            
            // Reset combat for all pawns targeting this NPC
            Combat.resetCombatForTarget(phase1)
            
            // Handle death animation and then spawn phase 2
            phase1.queue(TaskPriority.STRONG) {
                // Make NPC invisible IMMEDIATELY - this is critical for it to disappear
                NpcInfo(phase1).setAllOpsInvisible()
                NpcInfo(phase1).setInaccessible(true)
                phase1.resetFacePawn()
                
                // Play death animation (reduced wait for faster transition)
                val deathAnimation = phase1.combatDef.deathAnimation
                deathAnimation.forEach { anim ->
                    val def = getAnim(anim)
                    phase1.animate(def.id, def.cycleLength)
                    wait(def.cycleLength)
                }
                
                // Minimal wait for death animation to complete
                wait(1)
                
                // Make Phase 1 invisible and inaccessible BEFORE removing it
                // This ensures it disappears immediately for all players
                NpcInfo(phase1).setAllOpsInvisible()
                NpcInfo(phase1).setInaccessible(true)
                
                // Remove phase 1 from world - it should not respawn since respawns = false
                world.remove(phase1)
                
                // Double-check it's removed - if still spawned, force remove again
                if (phase1.isSpawned()) {
                    NpcInfo(phase1).setAllOpsInvisible()
                    NpcInfo(phase1).setInaccessible(true)
                    world.remove(phase1)
                }
                
                // Use world.queue instead of phase1.queue to avoid index conflicts
                // Reduced wait time for faster Phase 2 spawn while still ensuring avatar deallocation
                world.queue spawnPhase2@ {
                    // Wait 2 ticks to ensure the NPC index/avatar is fully deallocated
                    // Reduced from 5 to 2 for faster spawning while maintaining safety
                    wait(2)
                    
                    // Verify phase1 is no longer in the world before spawning phase2
                    if (phase1.isSpawned()) {
                        // Phase1 is still spawned somehow, force remove it completely
                        phase1.respawns = false // Ensure it won't respawn
                        NpcInfo(phase1).setAllOpsInvisible()
                        NpcInfo(phase1).setInaccessible(true)
                        world.remove(phase1)
                        wait(1) // Reduced wait for faster spawning
                        
                        // Double-check it's removed
                        if (phase1.isSpawned()) {
                            // Still spawned - this shouldn't happen, but force remove again
                            world.remove(phase1)
                            // No additional wait - proceed immediately
                        }
                    }
                    
                    // Spawn Phase 2 at the same location with retry logic
                    var phase2: Npc? = null
                    var spawnAttempts = 0
                    val maxAttempts = 3
                    
                    while (phase2 == null && spawnAttempts < maxAttempts) {
                        try {
                            // Create Phase 2 NPC
                            val newPhase2 = Npc(getRSCM("npc.vetion_6612"), phase1Tile, world)
                            newPhase2.respawns = false // Phase 2 doesn't respawn - it's the final phase
                            newPhase2.walkRadius = phase1WalkRadius
                            newPhase2.setActive(true)
                            
                            // Transfer damage map from phase 1 to phase 2 so loot is distributed correctly
                            damageData.forEach { (player, damage) ->
                                newPhase2.damageMap.add(player, damage)
                            }
                            
                            // Spawn phase 2 - this is where the avatar allocation happens
                            // If this fails with IllegalArgumentException, the avatar index is still allocated
                            world.spawn(newPhase2)
                            phase2 = newPhase2 // Success, assign and exit loop
                            break
                        } catch (e: IllegalArgumentException) {
                            // Avatar allocation failed (index still allocated), wait and retry
                            spawnAttempts++
                            if (spawnAttempts < maxAttempts) {
                                wait(1) // Reduced wait for faster retry
                            } else {
                                // Max attempts reached - could not spawn Phase 2
                                // This is a non-fatal error - Phase 1 is dead, Phase 2 just won't spawn
                                // The error will be logged by the exception handler
                                return@spawnPhase2
                            }
                        }
                    }
                    
                    // Message players in the area if Phase 2 was successfully spawned
                    phase2?.let {
                        // Final cleanup: Remove any remaining Phase 1 NPCs that might still exist
                        // This ensures Phase 1 is completely gone when Phase 2 spawns
                        // Collect NPCs first to avoid ConcurrentModificationException
                        val phase1Id = getRSCM("npc.vetion")
                        val phase1NpcsToRemove = mutableListOf<Npc>()
                        world.npcs.forEach { npc ->
                            if (npc.id == phase1Id && npc.tile.getDistance(phase1Tile) <= 2) {
                                // Found a Phase 1 NPC near the spawn location - mark for removal
                                phase1NpcsToRemove.add(npc)
                            }
                        }
                        
                        // Remove collected Phase 1 NPCs after iteration
                        phase1NpcsToRemove.forEach { npc ->
                            npc.respawns = false
                            try {
                                if (npc.isSpawned()) {
                                    NpcInfo(npc).setAllOpsInvisible()
                                    NpcInfo(npc).setInaccessible(true)
                                }
                            } catch (e: Exception) {
                                // Ignore if avatar is not initialized or other errors during cleanup
                            }
                            world.remove(npc)
                        }
                        
                        // Collect players first to avoid concurrent modification
                        val playersToMessage = mutableListOf<Player>()
                        world.players.forEach { player ->
                            playersToMessage.add(player)
                        }
                        playersToMessage.forEach { player ->
                            if (player.tile.getDistance(phase1Tile) <= 10) {
                                player.message("Vet'ion has been reborn in his orange form!")
                            }
                        }
                    }
                }
            }
        }
        
        // Phase 2 Death Handler - Use fullNpcDeath to completely override default behavior
        // This prevents the "bugging" issue where the NPC doesn't properly despawn
        fullNpcDeath("npc.vetion_6612") {
            val phase2 = ctx as Npc
            val phase2Tile = phase2.tile
            
            // Interrupt queues and stop movement
            phase2.interruptQueues()
            phase2.stopMovement()
            phase2.lock()
            phase2.resetInteractions()
            
            // Disable respawning for Phase 2
            phase2.respawns = false
            
            // Reset combat for all pawns targeting this NPC
            Combat.resetCombatForTarget(phase2)
            
            // Handle death animation, drop loot, and remove Phase 2
            phase2.queue(TaskPriority.STRONG) {
                // Make NPC invisible IMMEDIATELY
                NpcInfo(phase2).setAllOpsInvisible()
                NpcInfo(phase2).setInaccessible(true)
                phase2.resetFacePawn()
                
                // Play death animation
                val deathAnimation = phase2.combatDef.deathAnimation
                deathAnimation.forEach { anim ->
                    val def = getAnim(anim)
                    phase2.animate(def.id, def.cycleLength)
                    wait(def.cycleLength)
                }
                
                wait(1)
                
                // Drop loot for all players who dealt damage (shared loot system)
                // Get the loot tables from the NPC's combat definition
                val lootTables = phase2.combatDef.LootTables
                if (lootTables != null && lootTables.isNotEmpty()) {
                    // Get all players who dealt damage
                    val playersWhoDamaged = mutableListOf<Player>()
                    world.players.forEach { player ->
                        if (player.initiated && !player.isDead() && phase2.damageMap.getDamageFrom(player) > 0) {
                            playersWhoDamaged.add(player)
                        }
                    }
                    
                    // Give each player their own loot roll
                    playersWhoDamaged.forEach { player ->
                        val droppedItems = roll(player, lootTables)
                        
                        // Spawn each dropped item on the ground
                        droppedItems.forEach { groundItem ->
                            val newGroundItem = GroundItem(
                                item = groundItem.item,
                                amount = groundItem.amount,
                                tile = phase2Tile,
                                owner = player
                            )
                            
                            // Set timers: player sees for 1 minute, then everyone for 3 minutes
                            newGroundItem.timeUntilPublic = TimeConstants.CYCLES_PER_MINUTE
                            newGroundItem.timeUntilDespawn = TimeConstants.CYCLES_PER_MINUTE * 4
                            newGroundItem.ownerShipType = 1
                            
                            world.spawn(newGroundItem)
                        }
                        
                        if (droppedItems.isNotEmpty()) {
                            player.message("You receive loot from ${phase2.def.name}!")
                        }
                    }
                }

                // Handle Boss Kill Count
                val killer = phase2.damageMap.getMostDamage() as? Player
                if (killer != null) {
                    val killcounts = killer.attr[BossKillcountPlugin.BOSS_KILLCOUNT_ATTR] ?: run {
                        val newMap = java.util.concurrent.ConcurrentHashMap<String, Int>()
                        killer.attr[BossKillcountPlugin.BOSS_KILLCOUNT_ATTR] = newMap
                        newMap
                    }
                    
                    val bossName = phase2.def.name
                    val currentKc = killcounts.getOrDefault(bossName.lowercase(), 0)
                    val newKc = currentKc + 1
                    killcounts[bossName.lowercase()] = newKc
                    
                    killer.message("<col=ff6600>Your $bossName killcount is now: $newKc</col>")
                }
                
                // Make Phase 2 invisible and inaccessible BEFORE removing it
                NpcInfo(phase2).setAllOpsInvisible()
                NpcInfo(phase2).setInaccessible(true)
                
                // Remove phase 2 from world
                world.remove(phase2)
                
                // Double-check removal
                if (phase2.isSpawned()) {
                    world.remove(phase2)
                }
                
                    // Spawn Phase 1 back at original location
                world.queue spawnPhase1@ {
                    // Notify killer about respawn
                    killer?.message("<col=ff0000>Vet'ion will respawn in 5 seconds.</col>")
                    
                    // Wait for the respawn delay (5 seconds)
                    wait(9)
                    
                    // Verify phase2 is no longer in the world
                    if (phase2.isSpawned()) {
                        world.remove(phase2)
                        wait(1)
                    }
                    
                    // Original spawn coordinates for Phase 1
                    val originalSpawnTile = Tile(3229, 3788, 0)
                    val phase1Id = getRSCM("npc.vetion")
                    
                    // Check if Phase 1 already exists
                    var phase1Exists = false
                    world.npcs.forEach { npc ->
                        if (npc.id == phase1Id && npc.tile.sameAs(originalSpawnTile)) {
                            phase1Exists = true
                            return@forEach
                        }
                    }
                    
                    // Only spawn if Phase 1 doesn't already exist
                    if (!phase1Exists) {
                        var spawned = false
                        var spawnAttempts = 0
                        val maxAttempts = 5
                        
                        while (!spawned && spawnAttempts < maxAttempts) {
                            try {
                                val newPhase1 = Npc(phase1Id, originalSpawnTile, world)
                                newPhase1.respawns = true
                                newPhase1.walkRadius = 3
                                newPhase1.setActive(true)
                                world.spawn(newPhase1)
                                spawned = true
                            } catch (e: IllegalArgumentException) {
                                // Avatar allocation failed (index still allocated), wait and retry
                                spawnAttempts++
                                if (spawnAttempts < maxAttempts) {
                                    wait(1)
                                } else {
                                    e.printStackTrace()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}