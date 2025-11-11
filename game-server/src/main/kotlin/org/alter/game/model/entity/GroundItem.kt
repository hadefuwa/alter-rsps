package org.alter.game.model.entity

import gg.rsmod.util.toStringHelper
import org.alter.game.model.EntityType
import org.alter.game.model.PlayerUID
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.item.Item
import org.alter.game.model.item.ItemAttribute
import java.util.*

/**
 * An item that is spawned on the ground.
 *
 * @param ownerUID
 * If null, the item will be visible and can be interacted with by any player
 * in the world. Otherwise, it will only be visible to the player who's [Player.uid]
 * matches [ownerUID].
 *
 * @author Tom <rspsmods@gmail.com>
 */
class GroundItem private constructor(val item: Int, var amount: Int, internal var ownerUID: PlayerUID?) : Entity() {
    constructor(item: Int, amount: Int, tile: Tile, owner: Player? = null) : this(item, amount, owner?.uid) {
        this.tile = tile
    }

    /**
     * The ownership type of this ground item.
     * 
     * TODO: Implement proper ownership type logic.
     * 
     * Ownership types:
     * - 0: None (public item, anyone can pick up)
     * - 1: Self Player (only the player who dropped it can see/pick it up initially)
     * - 2: Other Player (visible to other players but not the dropper)
     * - 3: Group Ironman (only visible to group members)
     * 
     * Current implementation:
     * - Always defaults to 0 (None)
     * - Should be set based on:
     *   - Player's game mode (Ironman, Group Ironman, etc.)
     *   - Item value (high-value items may have different ownership rules)
     *   - Server settings
     * 
     * Implementation needed:
     * - Set ownerShipType when item is dropped based on player's status
     * - Use ownerShipType to determine visibility rules
     * - Use ownerShipType to determine pickup permissions
     * - Handle transitions (e.g., when item becomes public after timer expires)
     */
    var ownerShipType = 0

    constructor(item: Item, tile: Tile, owner: Player? = null) : this(item.id, item.amount, tile, owner)

    var currentCycle = 0

    var respawnCycles = -1

    var timeUntilPublic = 0
    var timeUntilDespawn = 0

    internal val attr = EnumMap<ItemAttribute, Int>(ItemAttribute::class.java)

    override val entityType: EntityType = EntityType.GROUND_ITEM

    fun isOwnedBy(p: Player): Boolean = ownerUID != null && p.uid.value == ownerUID!!.value

    fun isPublic(): Boolean = ownerUID == null

    fun canBeViewedBy(p: Player): Boolean = isPublic() || isOwnedBy(p)

    fun removeOwner() {
        ownerUID = null
    }

    fun copyAttr(attributes: Map<ItemAttribute, Int>): GroundItem {
        attr.putAll(attributes)
        return this
    }

    fun isSpawned(world: World): Boolean = world.isSpawned(this)

    override fun toString(): String =
        toStringHelper().add("item", item).add("amount", amount).add("tile", tile.toString()).add("owner", ownerUID).toString()

    companion object {
        /**
         * The default amount of cycles for this ground item to respawn if flagged
         * to do so.
         */
        const val DEFAULT_RESPAWN_CYCLES = 50

        /**
         * @TODO
         * Validate if these values are correct.
         */
        /**
         * The default amount of cycles for this item to be publicly visible.
         */
        const val DEFAULT_PUBLIC_SPAWN_CYCLES = 100

        /**
         * The default amount of cycles for this item to despawn from the world.
         */
        const val DEFAULT_DESPAWN_CYCLES = 300
    }
}
