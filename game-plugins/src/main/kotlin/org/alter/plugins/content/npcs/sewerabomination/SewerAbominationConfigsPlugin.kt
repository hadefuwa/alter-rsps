package org.alter.plugins.content.npcs.sewerabomination

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

/**
 * Sewer Abomination Configuration
 *
 * A mid-level boss found in the Varrock sewers
 * Spawns in the center of the sewer network
 */
class SewerAbominationConfigsPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        // Spawn Cerberus (5862) as Sewer Abomination in Varrock sewers
        // Coordinates: x=3237, z=9866 (adjust based on your sewer layout)
        spawnNpc("npc.cerberus", x = 3237, z = 9866, height = 0, walkRadius = 6)

        // Note: Combat definition is set in CerberusConfigsPlugin.kt
        // Since both Cerberus and Sewer Abomination use the same NPC ID (5862),
        // they share the same combat definition. The combat definition can only be set once per NPC ID.
        // If you need different stats for Sewer Abomination, consider using a different NPC ID.

        // Note: Minion combat definitions are not set here because these NPCs
        // (zombie, archer, dark_wizard) already have combat definitions set elsewhere.
        // The guaranteed random item drop for minions is handled in SewerAbominationCombatPlugin
        // via onNpcDeath handlers that check if the NPC doesn't respawn (boss minions).
    }
}
