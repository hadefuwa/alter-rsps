package org.alter.plugins.content.raids.tob

import org.alter.api.HitType
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Tile
import org.alter.game.model.TileGraphic
import org.alter.game.model.World
import org.alter.game.model.entity.Npc
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.*

class TobBosses(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

    init {
        // --- Maiden of Sugadinti ---
        onNpcSpawn(TobConstants.MAIDEN_NPC_ID) {
            npc.combatDef = npc.combatDef.copy(attackSpeed = 6)
        }

        onNpcCombat(TobConstants.MAIDEN_NPC_ID) {
            val npc = npc
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null

            npc.queue {
                while (canEngageCombat(npc)) {
                    val target = npc.getCombatTarget()
                    if (target != null) {
                        npc.facePawn(target)

                        // Blood Pool Mechanic
                        if (raid != null && world.percentChance(15.0)) {
                            // Target a random player
                            val bloodTarget = raid.party.random()
                            val poolTile = bloodTarget.tile
                            // named args: tile, id, height, delay
                            world.spawn(TileGraphic(tile = poolTile, id = 1579, height = 0))
                            raid.bloodPools.add(poolTile)
                            npc.forceChat("Bleed for me!")
                        }

                        // Basic Attack
                        npc.animate(8092)
                        if (target.tile.isWithinRadius(npc.tile, 10)) {
                            // random(range) from ext
                            target.hit(random(10..30), type = HitType.HIT)
                        }

                        // Check blood pools healing
                        if (raid != null && raid.bloodPools.isNotEmpty()) {
                            val poolsToRemove = mutableListOf<Tile>()
                            raid.bloodPools.forEach { pool ->
                                val isSoaked = world.players.any { it?.tile?.sameAs(pool) == true }
                                if (!isSoaked) {
                                    // Heal boss, use setCurrentHp
                                    npc.setCurrentHp(minOf(npc.getMaxHp(), npc.getCurrentHp() + 10))
                                    poolsToRemove.add(pool)
                                } else {
                                    // Damage players standing on it
                                    world.players.forEach { p ->
                                        if (p != null && p.tile.sameAs(pool)) {
                                            p.hit(5, type = HitType.POISON)
                                        }
                                    }
                                }
                            }
                            if (poolsToRemove.isNotEmpty()) {
                                raid.bloodPools.removeAll(poolsToRemove)
                                npc.graphic(123)
                            }
                        }

                        wait(npc.combatDef.attackSpeed)
                    } else {
                        wait(1)
                    }
                }
            }
        }

        // --- Pestilent Bloat ---
        onNpcCombat(TobConstants.BLOAT_NPC_ID) {
            val npc = npc
            npc.queue {
                while (canEngageCombat(npc)) {
                    if (world.percentChance(25.0)) {
                        // Stomp
                        npc.animate(8082)
                        npc.forceChat("STOMP!")
                        world.players.forEach { p ->
                            if (p != null && p.tile.isWithinRadius(npc.tile, 4)) {
                                p.hit(random(20..50), type = HitType.HIT)
                                p.animate(424)
                            }
                        }
                        wait(6)
                    } else {
                        val target = npc.getCombatTarget()
                        if (target != null) {
                            npc.facePawn(target)
                            npc.animate(8082)
                            target.hit(random(10..30), type = HitType.HIT)
                        }
                        wait(4)
                    }
                }
            }
        }

        // --- Verzik Vitur ---
        onNpcCombat(TobConstants.VERZIK_NPC_ID) {
            val npc = npc
            npc.queue {
                while (canEngageCombat(npc)) {
                    val target = npc.getCombatTarget()
                    if (target != null) {
                        npc.facePawn(target)
                        npc.animate(8123)
                        // Multi-target attack
                        npc.world.players.forEach { p ->
                            if (p != null && p.tile.isWithinRadius(npc.tile, 15)) {
                                // Named args for createProjectile
                                // createProjectile(target, gfx, startHeight, endHeight, delay,
                                // angle, lifespan)
                                val proj =
                                        npc.createProjectile(
                                                target = p,
                                                gfx = 1560,
                                                startHeight = 40,
                                                endHeight = 30,
                                                delay = 20,
                                                angle = 0,
                                                lifespan = -1
                                        )
                                world.spawn(proj)
                                p.hit(random(0..40), type = HitType.HIT)
                            }
                        }
                        wait(5)
                    } else {
                        wait(1)
                    }
                }
            }
        }

        // --- Death Logic for Progression ---
        onNpcDeath(TobConstants.MAIDEN_NPC_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }

        onNpcDeath(TobConstants.BLOAT_NPC_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }

        onNpcDeath(TobConstants.NYLOCAS_BOSS_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }

        onNpcDeath(TobConstants.SOTETSEG_NPC_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }

        onNpcDeath(TobConstants.XARPUS_NPC_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }

        onNpcDeath(TobConstants.VERZIK_NPC_ID) {
            val instance = world.instanceAllocator.getMap(npc.tile)
            val raid = if (instance != null) TobService.activeRaids[instance] else null
            raid?.nextRoom()
        }
    }

    private fun canEngageCombat(npc: Npc): Boolean {
        // Simple checking to avoid reference issues
        return !npc.isDead() && npc.isSpawned()
    }
}
