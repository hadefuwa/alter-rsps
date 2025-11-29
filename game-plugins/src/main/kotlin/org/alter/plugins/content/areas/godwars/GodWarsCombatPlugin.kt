package org.alter.plugins.content.areas.godwars

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.combat.CombatClass
import org.alter.game.model.entity.Npc
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.plugins.content.combat.Combat
import org.alter.plugins.content.combat.strategy.magic.CombatSpell

/**
 * God Wars Dungeon Combat Plugin
 * 
 * Handles combat class assignment for God Wars NPCs:
 * - Sets mages to use MAGIC combat class
 * - Sets rangers to use RANGED combat class
 * - Sets warriors to use MELEE combat class
 * - Applies 2x damage multiplier to all God Wars NPCs
 */
class GodWarsCombatPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {
    
    /**
     * Damage multiplier for all God Wars NPCs (2x = double damage)
     */
    private val GODWARS_DAMAGE_MULTIPLIER = 2.0
    
    init {
        // ======================
        // SPIRITUAL MAGES (All Factions) - Use Magic
        // ======================
        onNpcSpawn(npc = "npc.spiritual_mage") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_mage_3161") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_mage_3168") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_mage_2244") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // SPIRITUAL RANGERS (All Factions) - Use Ranged
        // ======================
        onNpcSpawn(npc = "npc.spiritual_ranger") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_ranger_3160") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_ranger_3167") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_ranger_2242") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // AVIANSIES - Use Ranged
        // ======================
        onNpcSpawn(npc = "npc.aviansie") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // SPIRITUAL WARRIORS (All Factions) - Use Melee
        // Apply damage multiplier to all warriors
        // ======================
        onNpcSpawn(npc = "npc.spiritual_warrior") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_warrior_3159") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_warrior_3166") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.spiritual_warrior_2243") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // OTHER GOD WARS NPCs
        // ======================
        onNpcSpawn(npc = "npc.knight_of_saradomin") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.knight_of_saradomin_2214") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.imp_3134") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.goblin_2245") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.goblin_2246") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // GENERAL GRAARDOR - Bandos Boss
        // Uses both melee and ranged attacks
        // ======================
        onNpcSpawn(npc = "npc.general_graardor") {
            // Graardor uses both melee and ranged, default to melee
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // GENERAL GRAARDOR'S SERGEANTS - Bandos Minions
        // ======================
        onNpcSpawn(npc = "npc.sergeant_strongstack") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.sergeant_steelwill") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.sergeant_grimspike") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // COMMANDER ZILYANA - Saradomin Boss
        // Fast melee boss with dash attack
        // ======================
        onNpcSpawn(npc = "npc.commander_zilyana") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // COMMANDER ZILYANA'S MINIONS - Saradomin Minions
        // ======================
        onNpcSpawn(npc = "npc.starlight") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.growler") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.bree") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // K'RIL TSUTSAROTH - Zamorak Boss
        // Hard-hitting melee boss with typeless special and prayer drain
        // ======================
        onNpcSpawn(npc = "npc.kril_tsutsaroth") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // K'RIL TSUTSAROTH'S MINIONS - Zamorak Minions
        // ======================
        onNpcSpawn(npc = "npc.balfrug_kreeyath") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.tstanon_karlak") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.zakln_gritch") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // NEX - Ancient Prison Boss
        // 5-phase boss with unique mechanics per phase
        // ======================
        onNpcSpawn(npc = "npc.nex") {
            npc.combatClass = CombatClass.MAGIC // Default to magic, changes per phase
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        // ======================
        // NEX MINIONS - Phase Minions
        // ======================
        onNpcSpawn(npc = "npc.fumus") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.umbra") {
            npc.combatClass = CombatClass.RANGED
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.cruor") {
            npc.combatClass = CombatClass.MELEE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
        
        onNpcSpawn(npc = "npc.glacies") {
            npc.combatClass = CombatClass.MAGIC
            npc.attr[Combat.CASTING_SPELL] = CombatSpell.WIND_STRIKE
            npc.attr[Combat.DAMAGE_DEAL_MULTIPLIER] = GODWARS_DAMAGE_MULTIPLIER
        }
    }
}

