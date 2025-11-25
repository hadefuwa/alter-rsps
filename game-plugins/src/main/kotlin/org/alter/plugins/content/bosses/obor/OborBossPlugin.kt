package org.alter.plugins.content.bosses.obor

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.getCombatTarget
import kotlin.random.Random

/**
 * Obor the Hill Giant Boss Plugin
 * 
 * Obor is a Hill Giant boss located in a special chamber in the Edgeville Dungeon.
 * He requires a Giant Key (dropped by Hill Giants) to access his lair.
 * 
 * Features:
 * - Obor boss spawn in special chamber
 * - Enhanced combat mechanics
 * - Special combat messages
 * 
 * Obor's Lair Location: Near Edgeville Dungeon Hill Giants area
 * Coordinates: (3093, 9842, height = 0) - A secluded chamber
 */
class OborBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        /**
         * Obor's lair location - secluded chamber near Hill Giants
         */
        private val OBOR_LOCATION = Tile(x = 3107, z = 9831, height = 0)
        
        /**
         * Obor's combat level and stats
         */
        private const val OBOR_COMBAT_LEVEL = 106
    }

    init {
        // Spawn Obor in his chamber
        spawnObor()
        
        // Set up combat mechanics
        setupOborCombat()
        
        println("Obor Boss: Initialized at location (${OBOR_LOCATION.x}, ${OBOR_LOCATION.z})")
        println("Obor Boss: Combat Level $OBOR_COMBAT_LEVEL with enhanced mechanics")
    }
    
    /**
     * Spawns Obor the Hill Giant boss in his chamber
     */
    private fun spawnObor() {
        spawnNpc(
            npc = "npc.obor",
            x = OBOR_LOCATION.x,
            z = OBOR_LOCATION.z,
            height = OBOR_LOCATION.height,
            walkRadius = 3, // Limited movement in his chamber
            direction = Direction.SOUTH
        )
    }
    
    /**
     * Sets up Obor's enhanced combat mechanics
     */
    private fun setupOborCombat() {
        // Enhanced combat mechanics for Obor
        onNpcCombat("npc.obor") {
            npc.queue { 
                oborCombat()
            }
        }
        
        // Obor's enhanced stats when spawning
        onNpcSpawn("npc.obor") {
            // Set enhanced damage multiplier for Obor
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = 1.5 // 50% more damage
            println("Obor Boss: Enhanced Obor spawned with increased damage")
        }
        
        // Message nearby players when Obor dies
        onNpcDeath("npc.obor") {
            val npc = this.npc
            
            // Message nearby players about Obor's defeat
            npc.world.players.forEach { player ->
                val distance = player.tile.getDistance(npc.tile)
                if (distance <= 15) {
                    player.message("<col=ff6600>Obor, the Hill Giant, has been defeated!</col>")
                }
            }
        }
    }
    
    /**
     * Obor's enhanced combat mechanics with special abilities
     */
    private suspend fun QueueTask.oborCombat() {
        val npc = ctx as Npc
        val target = npc.getCombatTarget() as? Player ?: return
        
        // Special combat messages
        when (Random.nextInt(15)) {
            0 -> target.message("<col=red>Obor roars with fury!</col>")
            1 -> target.message("<col=red>The ground shakes as Obor charges!</col>")
            2 -> target.message("<col=red>Obor's massive club swings towards you!</col>")
            3 -> npc.forceChat("GRAAAHHH!")
            4 -> npc.forceChat("You dare challenge me?")
        }
        
        // Enhanced regeneration during combat (rare)
        if (npc.getCurrentHp() < npc.getMaxHp() / 2 && Random.nextInt(50) == 0) {
            val healAmount = npc.getMaxHp() / 20
            npc.setCurrentHp(minOf(npc.getMaxHp(), npc.getCurrentHp() + healAmount))
            target.message("<col=red>Obor regenerates some health!</col>")
            npc.forceChat("RAAAARGH!") // Roar when healing
        }
    }
}