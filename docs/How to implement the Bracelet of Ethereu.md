How to implement the Bracelet of Ethereum in our game.

1. Revenant ether (the resource)

In OSRS:

Dropped by revenants in the Revenant Caves every kill. 
Old School RuneScape Wiki

Used to:

Charge the Bracelet of ethereum.

Charge 3 wilderness weapons (Craw’s bow, Thammaron’s sceptre, Viggora’s chainmace), 1 ether per attack. 
Old School RuneScape Wiki

Ether stored as charges in those items is always dropped on death in the Wilderness, not protected. 
Old School RuneScape Wiki

For your game, you can model this as:

ItemType: REVENANT_ETHER
Properties: stackable, tradable, dropped_by = [revenant_*]

2. Bracelet of ethereum: states

There are effectively two forms:

Uncharged bracelet

Tradable.

Can be dismantled into 250 ether (small profit loss in OSRS). 
Old School RuneScape Wiki
+1

Charged bracelet

Same physical item, but:

Holds up to 16,000 ether as charges. 
Old School RuneScape Wiki
+1

Becomes untradeable while charged. 
Old School RuneScape Wiki

Has a right-click: Uncharge which:

Empties all ether from it.

Returns it to the uncharged, tradable form.

Ether is not refunded in OSRS (it just disappears). 
Old School RuneScape Wiki

Has a right-click: Toggle absorption:

When ON, any ether dropped by revenants goes directly into the bracelet instead of into inventory, up to the capacity. 
Old School RuneScape Wiki
+1

Suggested data model:

BraceletOfEthereum {
    isCharged: bool
    etherCharges: int   // 0..16000
    autoAbsorb: bool
}

3. Charging logic

In OSRS:

To charge:

Use revenant ether on the bracelet.

Each 1 ether consumed becomes 1 charge.

Max 16,000 charges.

In your game, pseudocode:

function addEtherToBracelet(bracelet, etherAmount):
    if bracelet.etherCharges == 0:
        bracelet.isCharged = true   // becomes untradeable

    space = 16000 - bracelet.etherCharges
    toAdd = min(etherAmount, space)

    bracelet.etherCharges += toAdd
    removeFromInventory(player, REVENANT_ETHER, toAdd)


Auto-absorb on a revenant kill:

onRevenantKilled(player, revenant):
    etherDrop = rollEtherDrop(revenant)

    if etherDrop <= 0:
        return

    bracelet = getEquippedBraceletOfEthereum(player)

    if bracelet != null and bracelet.autoAbsorb:
        addEtherToBracelet(bracelet, etherDrop)
    else:
        dropItemOnGround(REVENANT_ETHER, etherDrop)

4. Effect vs revenants in combat

Official wiki description:

When charged and worn:

The bracelet makes the player immune to damage from revenants. 
Old School RuneScape Wiki
+1

1 charge is consumed per attack from a revenant.

Revenants also become tolerant (do not auto-aggro you) while you are wearing a charged bracelet. 
Old School RuneScape Wiki

A lot of modern guides describe it as roughly “75% damage reduction” rather than absolute immunity, but the core mechanic is: on each revenant attack, 1 charge gets spent and the hit is heavily reduced or blocked. 
tonsofxp.com
+1

To emulate OSRS simply, I’d go with:

If charges > 0:

Set damage = 0 (true immunity), or

Set damage = floor(originalDamage * 0.25) (75% reduction).

Pseudocode hook on hit:

onIncomingHit(target, attacker, damage):
    if target.isPlayer and attacker.isRevenant:
        bracelet = getEquippedBraceletOfEthereum(target)

        if bracelet != null and bracelet.isCharged and bracelet.etherCharges > 0:
            // Pick one of these behaviours:

            // A) Full immunity:
            damage = 0

            // or B) 75% reduction:
            // damage = floor(damage * 0.25)

            bracelet.etherCharges -= 1

            if bracelet.etherCharges <= 0:
                bracelet.isCharged = false  // becomes uncharged/tradable again

    applyDamage(target, damage)


Aggro tolerance:

function revenantShouldAggro(player):
    bracelet = getEquippedBraceletOfEthereum(player)
    if bracelet != null and bracelet.isCharged and bracelet.etherCharges > 0:
        return false    // tolerant
    return true

5. Death & loot rules

In OSRS:

The bracelet of ethereum is always lost on death in the Wilderness, even if uncharged, and any ether inside it is dropped as well. 
Old School RuneScape Wiki
+1

Ether stored as charges in the bracelet or in the 3 wilderness weapons is always dropped as items on the ground when you die. 
Old School RuneScape Wiki

You could model this as:

onPlayerDeath(player, killer):
    bracelet = getEquippedBraceletOfEthereum(player)

    if bracelet != null:
        // Drop ether charges
        if bracelet.etherCharges > 0:
            dropItemOnGround(REVENANT_ETHER, bracelet.etherCharges)

        // Bracelet itself is always dropped
        dropItemOnGround(BRACELET_OF_ETHEREUM_UNCHARGED, 1)

        // Remove from player
        removeFromEquipment(player, braceletSlot)


You can then implement usual “killer gets first rights” / loot-ownership rules on those drops.

6. Summary as a spec for your game

If you just want the barebones behaviour to copy:

Resource: A stackable item “Revenant Ether” that drops from revenants and always drops on death.

Item: “Bracelet of ethereum”.

Can hold up to 16,000 ether as charges.

Charged form untradeable, uncharged tradable.

Dismantling uncharged gives a fixed amount of ether.

Charging:

Use ether on bracelet to add charges.

Optional auto-absorb setting for directly filling the bracelet from drops.

Combat effect:

While worn and charged, each revenant attack:

Consumes 1 charge.

Negates or heavily reduces damage.

Prevents revenant aggression while charges exist.

On death:

Bracelet is always dropped.

All ether stored in it is dropped as separate loot.