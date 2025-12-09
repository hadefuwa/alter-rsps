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

class RunecraftBossPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        spawnNpc("npc.balance_elemental", x = 1720, z = 3829, walkRadius = 5)

        setCombatDef("npc.balance_elemental") {
            configs {
                attackSpeed = 1
                
                respawnDelay = 30
            }
            
            aggro {
                radius = 10
                
                searchDelay = 2
                
                alwaysAggro()
            }
            
            stats {
                hitpoints = 250
                
                attack = 3
                
                strength = 3
                
                defence = 1
                
                magic = 4
                
                ranged = 2
            }
            
            bonuses {
                defenceStab = 1000
                
                defenceSlash = 1000
                
                defenceCrush = 1000
                
                defenceMagic = 1
                
                defenceRanged = 1000
            }
            
            anims {
                attack = 3353
                
                block = 424
                
                death = 836
            }
            
            sound {
                attackSound = Sound.DRAGON_DEATH
                
                attackArea = true
                attackVolume = 50
                attackRadius = 10
                
                blockSound = Sound.DRAGON_DEATH
                
                blockArea = true
                blockVolume = 40
                blockRadius = 8
                
                deathSound = Sound.DRAGON_DEATH
                
                deathArea = true
                deathVolume = 60
                deathRadius = 12
            }
            
            drops {
                always { 
                    add("item.big_bones", 1)
                }
                
                main(weight = 128) {
                    
                    add("item.coins", min = 5000, max = 10000, weight = 20)
                    add("item.pure_essence_noted", min = 40, max = 600, weight = 5)
                    
                    add("item.air_rune", min = 40, max = 600, weight = 5)
                    add("item.water_rune", min = 40, max = 600, weight = 5)
                    add("item.earth_rune", min = 40, max = 600, weight = 5)
                    add("item.fire_rune", min = 40, max = 600, weight = 5)
                    add("item.mind_rune", min = 40, max = 600, weight = 5)
                    add("item.wrath_rune", min = 40, max = 600, weight = 5)
                    add("item.chaos_rune", min = 40, max = 600, weight = 5)
                    add("item.death_rune", min = 40, max = 600, weight = 5)
                    add("item.nature_rune", min = 40, max = 600, weight = 5)
                    add("item.soul_rune", min = 40, max = 600, weight = 5)
                    add("item.cosmic_rune", min = 40, max = 600, weight = 5)
                    add("item.blood_rune", min = 40, max = 600, weight = 5)
                }
            }
        }

        onNpcCombat("npc.balance_elemental_13530") { npc.queue { combatLoop() } }
    }

    suspend fun QueueTask.combatLoop() {
        val npc = ctx as Npc
        var target = npc.getCombatTarget() as? Player ?: return

        while (npc.canEngageCombat(target)) {
            npc.facePawn(target)
            
            if (npc.moveToAttackRange(this, target, distance = 1, projectile = false) && 
                npc.isAttackDelayReady()) {
                
                BossAttacks.magic(npc, target, projectile = 100, maxHit = 25, anim = 422)
                
                npc.postAttackLogic(target)
            }
            
            wait(1)
            target = npc.getCombatTarget() as? Player ?: break
        }
        
        npc.resetFacePawn()
        npc.removeCombatTarget()
    }
}
