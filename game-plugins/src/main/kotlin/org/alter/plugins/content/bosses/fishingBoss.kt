package org.alter.plugins.content.bosses.example

import org.alter.api.*
import org.alter.api.cfg.*
import org.alter.api.dsl.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.model.combat.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.rscm.RSCM.getRSCM

class FishingBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        spawnNpc("npc.muttadile_7562", x = 1369, z = 3629, walkRadius = 5)

        setCombatDef("npc.muttadile_7562") {
            configs {
                attackSpeed = 4
                
                respawnDelay = 30
            }
            
            aggro {
                radius = 10
                
                searchDelay = 2
                
                alwaysAggro()
            }
            
            stats {
                hitpoints = 150
                
                attack = 250
                
                strength = 300
                
                defence = 200
                
                magic = 1
                
                ranged = 175
            }
            
            bonuses {
                defenceStab = 100
                
                defenceSlash = 100
                
                defenceCrush = 100
                
                defenceMagic = 150
                
                defenceRanged = 99
            }
            
            anims {
                attack = 3353
                
                block = 424
                
                death = 836
            }
            
            sound {
                attackSound = Sound.SEA_TROLL_QUEEN_ATTACK
                
                attackArea = true
                attackVolume = 50
                attackRadius = 10
                
                blockSound = Sound.SEA_TROLL_QUEEN_HIT
                
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.SEA_TROLL_QUEEN_DEATH
                
                deathArea = true
                deathVolume = 60
                deathRadius = 12
            }
            
            drops {
                always { 
                    add("item.big_bones", 1)
                }
                
                main(128) {
                    
                    add("item.coins", 5000, 10000, 300)
                    add("item.raw_anglerfish_noted", 20, 25, 300)
                    add("item.anglerfish_noted", 15, 20, 300)
                    add("item.sand_worm", 5000, 10000, 300)
                    add("item.raw_lobster_noted", 30, 60, 300)
                    add("item.lobster_noted", 25, 55, 300)
                    add("item.raw_shark_noted", 20, 30, 300)
                    add("item.shark_noted", 16, 26, 300)
                    add("item.raw_tuna_noted", 40, 65, 300)
                }
            }
        }

        onNpcCombat("npc.muttadile_7562") { npc.queue { combatLoop() } }
    }

    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && 
                npc.isAttackDelayReady()) {
                
                BossAttacks.melee(npc, target, maxHit = 25, anim = 422)
                
                npc.postAttackLogic(target)
            }
            
            wait(1)
            target = npc.getCombatTarget() as? Player ?: break
        }
        
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
