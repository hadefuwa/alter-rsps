package org.alter.plugins.content.bosses.zulrah

import kotlin.random.Random
import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.mechanics.poison.poison

/**
 * Zulrah Boss Plugin
 *
 * Spawns Zulrah at the specified shrine location and handles its combat mechanics. Zulrah uses a
 * mix of Ranged and Magic attacks with special effects.
 *
 * Location: 2268, 3076, 0
 */
class ZulrahBossPlugin(r: PluginRepository, world: World, server: Server) :
        KotlinPlugin(r, world, server) {

    companion object {
        // Zulrah's Shrine Location
        private val ZULRAH_SPAWN_TILE = Tile(x = 2268, z = 3076, height = 0)
        private const val ZULRAH_NPC_ID = "npc.zulrah"

        // Attack Styles
        private const val STYLE_RANGED = 0
        private const val STYLE_MAGIC = 1
    }

    init {
        // Spawn Zulrah
        spawnZulrah()

        // Setup Combat
        setupZulrahCombat()
    }

    private fun spawnZulrah() {
        spawnNpc(
                npc = ZULRAH_NPC_ID,
                x = ZULRAH_SPAWN_TILE.x,
                z = ZULRAH_SPAWN_TILE.z,
                height = ZULRAH_SPAWN_TILE.height,
                walkRadius = 5,
                direction = Direction.SOUTH
        )
    }

    private fun setupZulrahCombat() {
        onNpcSpawn(ZULRAH_NPC_ID) { npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = 1.25 }

        onNpcCombat(ZULRAH_NPC_ID) { npc.queue { npc.zulrahCombatLogic(this) } }

        onNpcDeath(ZULRAH_NPC_ID) {
            npc.world.players.forEach { player ->
                if (player.tile.getDistance(npc.tile) <= 30) {
                    player.message("<col=006600>The great serpent Zulrah falls!</col>")
                }
            }
        }
    }

    private suspend fun Npc.zulrahCombatLogic(task: QueueTask) {
        var target = getCombatTarget() ?: return
        var currentStyle = STYLE_RANGED
        var attacksInPhase = 0

        while (canEngageCombat(target)) {
            facePawn(target)

            // Switch phases logic
            if (attacksInPhase >= Random.nextInt(5, 10)) {
                currentStyle = if (currentStyle == STYLE_RANGED) STYLE_MAGIC else STYLE_RANGED
                attacksInPhase = 0
                val styleName = if (currentStyle == STYLE_MAGIC) "Magic" else "Ranged"
                forceChat("Hiss! I switch to $styleName form!")
                // animate(5069) // Switch anim
                task.wait(2)
            }

            if (isAttackDelayReady()) {
                if (currentStyle == STYLE_MAGIC) {
                    performMagicAttack(this, target)
                } else {
                    performRangedAttack(this, target)
                }
                attacksInPhase++
                postAttackLogic(target)
            }

            task.wait(1)
            target = getCombatTarget() ?: break
        }

        resetFacePawn()
        removeCombatTarget()
    }

    private fun performRangedAttack(npc: Npc, target: Pawn) {
        npc.animate(5063)

        val projectile =
                npc.createProjectile(
                        target = target,
                        gfx = 1044,
                        startHeight = 43,
                        endHeight = 31,
                        delay = 51,
                        angle = 15,
                        lifespan = -1
                )
        npc.world.spawn(projectile)

        val hitDelay = (projectile.lifespan / 30)

        npc.world.queue {
            wait(hitDelay)
            if (target.isAlive()) {
                val damage =
                        if (target is Player &&
                                        target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MISSILES)
                        ) {
                            0
                        } else {
                            Random.nextInt(0, 30)
                        }

                target.hit(damage, type = HitType.HIT)
            }
        }
    }

    private fun performMagicAttack(npc: Npc, target: Pawn) {
        npc.animate(5069)

        val projectile =
                npc.createProjectile(
                        target = target,
                        gfx = 1046,
                        startHeight = 60,
                        endHeight = 31,
                        delay = 51,
                        angle = 15,
                        lifespan = -1
                )
        npc.world.spawn(projectile)

        val hitDelay = (projectile.lifespan / 30)

        npc.world.queue {
            wait(hitDelay)
            if (target.isAlive()) {
                val damage =
                        if (target is Player && target.hasPrayerIcon(PrayerIcon.PROTECT_FROM_MAGIC)
                        ) {
                            0
                        } else {
                            Random.nextInt(0, 35)
                        }

                target.hit(damage, type = HitType.HIT)

                // Poison effect
                if (damage > 0 && Random.nextInt(4) == 0 && target is Player) {
                    target.poison(initialDamage = 6) {
                        target.message("<col=006600>You have been infected with venom!</col>")
                    }
                }
            }
        }
    }
}
