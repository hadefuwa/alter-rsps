package org.alter.plugins.content.npcs.kalphitequeen

import org.alter.api.*
import org.alter.api.ext.*
import org.alter.game.*
import org.alter.game.model.*
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.attr.COMBAT_TARGET_FOCUS_ATTR
import org.alter.game.model.combat.CombatStyle
import org.alter.game.model.entity.*
import org.alter.game.model.queue.*
import org.alter.game.plugin.*
import org.alter.plugins.content.combat.*
import org.alter.plugins.content.combat.formula.*
import org.alter.plugins.content.death.NpcLootDropPlugin
import org.alter.rscm.RSCM.getRSCM

/**
 * Kalphite Queen Two-Phase System Plugin
 * 
 * Implements the two-phase Kalphite Queen system:
 * 
 * Form 1 (Green Ground Form):
 * - 255 HP
 * - Overhead prayers: Protect from Ranged ON, Protect from Magic ON, Protect from Melee OFF
 * - Weakness: Melee
 * - Heavy damage reduction to Ranged and Magic hits
 * - On death: Transforms to Form 2 (no despawn, no respawn timer)
 * 
 * Form 2 (Orange Flying Form):
 * - 255 HP
 * - Overhead prayers: Protect from Melee ON, Protect from Ranged ON, Protect from Magic OFF
 * - Weakness: Magic
 * - Heavy damage reduction to Melee and Ranged hits
 * - On death: Actual kill, drops loot, starts respawn timer, respawns as Form 1
 */
class KalphiteQueenPhasePlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    // Enum for Kalphite Queen forms
    enum class KQForm {
        FORM1_GREEN,    // Green ground form
        FORM2_ORANGE    // Orange flying form
    }

    // Attribute keys
    private val KQ_FORM_ATTR = AttributeKey<KQForm>()
    private val KQ_ORIGINAL_SPAWN_TILE_ATTR = AttributeKey<Tile>()
    
    companion object {
        // Shared attribute key accessible to combat formulas
        @JvmStatic
        val KQ_FORM_ATTR_SHARED = AttributeKey<KQForm>()
        
        /**
         * Check if NPC is Form 2 (Orange)
         * This can be called from combat formulas
         */
        @JvmStatic
        fun isForm2(npc: Npc): Boolean {
            // Check if this is a Kalphite Queen
            val isKQ = npc.id == 963 || npc.id == 964 || npc.def.name.lowercase().contains("kalphite queen")
            if (!isKQ) return false
            
            // Check form from shared attribute
            val form = npc.attr[KQ_FORM_ATTR_SHARED]
            if (form != null) {
                return form == KQForm.FORM2_ORANGE
            }
            
            // Fallback: if no attribute set, check by ID
            // Form 2 = 964, Form 1 = 963
            return npc.id == 964
        }
    }
    
    // NPC IDs - initialized in init block
    private var KQ_FORM1_ID: Int = 963
    private var KQ_FORM2_ID: Int = 964

    init {
        // Initialize NPC IDs
        try {
            KQ_FORM1_ID = getRSCM("npc.kalphite_queen_963")
            if (KQ_FORM1_ID == -1) {
                KQ_FORM1_ID = 963 // Fallback to hardcoded ID
            }
        } catch (e: Exception) {
            KQ_FORM1_ID = 963 // Fallback to hardcoded ID
        }
        
        try {
            KQ_FORM2_ID = getRSCM("npc.kalphite_queen_964")
            if (KQ_FORM2_ID == -1) {
                // Form 2 NPC not found in RSCM, try using hardcoded ID 964
                // If that doesn't work, we'll fall back to Form 1 ID
                KQ_FORM2_ID = 964
            }
        } catch (e: Exception) {
            // Form 2 NPC not in RSCM, try using hardcoded ID 964
            // If NPC 964 doesn't exist in the cache, it will fail when we try to spawn it
            KQ_FORM2_ID = 964
        }
        
        // Set up damage reduction system for Form 1
        onNpcSpawn("npc.kalphite_queen_963") {
            // Only set to Form 1 if not already set (to allow transformations)
            if (!npc.attr.has(KQ_FORM_ATTR) && !npc.attr.has(KQ_FORM_ATTR_SHARED)) {
                npc.attr[KQ_FORM_ATTR] = KQForm.FORM1_GREEN
                npc.attr[KQ_FORM_ATTR_SHARED] = KQForm.FORM1_GREEN
            }
        }

        // Set up damage reduction system for Form 2
        // If Form 2 uses the same NPC ID as Form 1, we'll handle it via attributes
        if (KQ_FORM2_ID != KQ_FORM1_ID) {
            // Form 2 has a different NPC ID
            try {
                onNpcSpawn("npc.kalphite_queen_964") {
                    npc.attr[KQ_FORM_ATTR] = KQForm.FORM2_ORANGE
                    npc.attr[KQ_FORM_ATTR_SHARED] = KQForm.FORM2_ORANGE
                }
            } catch (e: Exception) {
                // Form 2 NPC doesn't exist, will handle via attribute only
            }
        }
        
        // Handle Form 1 death - transform to Form 2
        fullNpcDeath("npc.kalphite_queen_963") {
            val npc = ctx as Npc
            val form = npc.attr[KQ_FORM_ATTR] ?: KQForm.FORM1_GREEN
            
            if (form == KQForm.FORM1_GREEN) {
                // Transform to Form 2
                npc.queue(TaskPriority.STRONG) {
                    transformToForm2(npc)
                }
                true // Indicate we handled the death
            } else {
                false // Let normal death proceed
            }
        }

        // Handle Form 2 death - spawn Form 1 after respawn delay
        // Handle both cases: Form 2 with separate ID or same ID as Form 1
        if (KQ_FORM2_ID == KQ_FORM1_ID) {
            // Form 2 uses same ID, check by attribute
            onNpcDeath("npc.kalphite_queen_963") {
                val npc = ctx as Npc
                val form = npc.attr[KQ_FORM_ATTR] ?: KQForm.FORM1_GREEN
                
                if (form == KQForm.FORM2_ORANGE) {
                    handleForm2Death(npc)
                }
            }
        } else {
            // Form 2 has separate ID
            try {
                onNpcDeath("npc.kalphite_queen_964") {
                    val npc = ctx as Npc
                    val form = npc.attr[KQ_FORM_ATTR] ?: KQForm.FORM2_ORANGE
                    
                    if (form == KQForm.FORM2_ORANGE) {
                        handleForm2Death(npc)
                    }
                }
            } catch (e: Exception) {
                // Form 2 NPC doesn't exist
            }
        }
    }
    
    /**
     * Handle Form 2 death - spawn Form 1 after respawn delay
     */
    private fun handleForm2Death(npc: Npc) {
        // Form 2 death - this is the actual kill
        // Loot has been dropped by the normal death system
        // Now spawn Form 1 after respawn delay
        // Use the original spawn tile stored in the attribute, or fall back to spawnTile
        val spawnTile = npc.attr[KQ_ORIGINAL_SPAWN_TILE_ATTR] ?: npc.spawnTile ?: npc.tile
        val respawnDelay = 50  // Same as configured respawn delay
        
        npc.world.queue {
            wait(respawnDelay)
            
            // Spawn Form 1 at the spawn location
            // spawnTile is automatically set in the constructor
            val form1Npc = Npc(KQ_FORM1_ID, spawnTile, npc.world)
            form1Npc.respawns = true
            form1Npc.walkRadius = npc.walkRadius
            form1Npc.setActive(true)
            form1Npc.attr[KQ_FORM_ATTR] = KQForm.FORM1_GREEN
            form1Npc.attr[KQ_FORM_ATTR_SHARED] = KQForm.FORM1_GREEN  // Also set shared attribute for combat formulas
            npc.world.spawn(form1Npc)
        }
    }

    /**
     * Transform Form 1 to Form 2 on the same tile
     */
    private suspend fun QueueTask.transformToForm2(form1Npc: Npc) {
        val world = form1Npc.world
        val transformTile = form1Npc.tile
        val spawnTile = form1Npc.spawnTile
        
        // Play transformation animation
        form1Npc.animate(6243) // Transformation animation
        form1Npc.graphic(id = 1055, height = 0, delay = 0) // Transformation graphic
        
        // Message to nearby players
        world.players.forEach { player ->
            if (player.tile.getDistance(transformTile) <= 15) {
                player.message("The Kalphite Queen transforms into her flying form!")
            }
        }
        
        // Wait for animation
        wait(3)
        
        // Store players who were attacking Form 1 before removing it
        val attackingPlayers = mutableListOf<Player>()
        world.players.forEach { player ->
            val combatTarget = player.getCombatTarget()
            if (combatTarget == form1Npc) {
                attackingPlayers.add(player)
            }
        }
        
        // Remove Form 1 NPC
        world.remove(form1Npc)
        
        // Wait a cycle to ensure Form 1 is fully removed
        wait(1)
        
        // Try to spawn Form 2 NPC at the transformation location
        // If Form 2 NPC doesn't exist, we'll use Form 1 ID but with Form 2 attributes
        val form2Npc = try {
            Npc(KQ_FORM2_ID, transformTile, world)
        } catch (e: Exception) {
            // Form 2 NPC doesn't exist in cache, use Form 1 ID
            println("Warning: Form 2 NPC (ID $KQ_FORM2_ID) doesn't exist, using Form 1 ID (${KQ_FORM1_ID}) with Form 2 attributes")
            Npc(KQ_FORM1_ID, transformTile, world)
        }
        
        form2Npc.respawns = false  // Form 2 doesn't respawn - we'll manually spawn Form 1
        // Store the original spawn tile in an attribute so we can use it when Form 2 dies
        form2Npc.attr[KQ_ORIGINAL_SPAWN_TILE_ATTR] = spawnTile
        form2Npc.walkRadius = form1Npc.walkRadius
        form2Npc.setActive(true)
        form2Npc.attr[KQ_FORM_ATTR] = KQForm.FORM2_ORANGE
        form2Npc.attr[KQ_FORM_ATTR_SHARED] = KQForm.FORM2_ORANGE  // Also set shared attribute for combat formulas
        
        // Spawn Form 2 FIRST (this will automatically call setNpcDefaults)
        world.spawn(form2Npc)
        
        // Wait a few cycles to ensure Form 2 is fully spawned, initialized, and accessible
        wait(3)
        
        // Verify Form 2 is spawned and accessible
        if (!form2Npc.isSpawned()) {
            println("Error: Form 2 NPC failed to spawn properly")
            return@transformToForm2
        }
        
        // Ensure Form 2 HP is set correctly (after spawn, defaults might have been applied)
        form2Npc.setCurrentHp(255)
        
        // Ensure Form 2 is active and attackable
        form2Npc.setActive(true)
        
        // Verify the NPC is attackable (has valid combat def)
        // If Form 2 uses the same ID as Form 1, the combat def should already be set
        // If Form 2 has a different ID but no combat def, try to use Form 1's def
        if (form2Npc.combatDef.hitpoints == -1) {
            println("Warning: Form 2 NPC has invalid combat def (hitpoints = -1), trying to fix...")
            // Try to get combat def from Form 1's config if available
            try {
                val form1CombatDef = world.plugins.npcCombatDefs[KQ_FORM1_ID]
                if (form1CombatDef != null) {
                    // Create a copy with updated hitpoints
                    val updatedDef = form1CombatDef.copy(hitpoints = 255)
                    // Note: combatDef is a val, so we can't directly reassign it
                    // But setNpcDefaults should have already set it, so this shouldn't happen
                    println("Form 1 combat def found, but cannot reassign (combatDef is val)")
                }
            } catch (e: Exception) {
                println("Could not fix Form 2 combat def: ${e.message}")
            }
        }
        
        // Now transfer combat targets from Form 1 to Form 2
        attackingPlayers.forEach { player ->
            if (player.isOnline && !player.isDead() && form2Npc.isSpawned() && !form2Npc.isDead()) {
                // Reset the player's combat state first
                player.resetInteractions()
                // Clear any existing combat target
                player.attr.remove(COMBAT_TARGET_FOCUS_ATTR)
                // Wait a moment before attacking to ensure everything is ready
                wait(1)
                // Then attack Form 2
                try {
                    player.attack(form2Npc)
                } catch (e: Exception) {
                    println("Error attacking Form 2: ${e.message}")
                }
            }
        }
    }
}
