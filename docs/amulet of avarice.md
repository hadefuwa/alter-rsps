amulet of avarice

1. What the Amulet of Avarice actually is

It is basically:

Salve amulet (ei)
+
Wilderness skull effect
+
Automatic revenant aggression

All combined into one item.

In OSRS, it is untradeable and created by using a salve amulet (ei) on revenant ether.
It gives the full undead damage/accuracy boost exactly like a salve (ei), but only inside the Revenant Caves.

2. Core Effects You Need to Implement
Effect 1: Always treated as if the player is skulled

The moment you equip it, you become skulled.

Being skulled stays as long as it is worn.

Removing the amulet immediately removes the skull (OSRS does this).

Skull duration is irrelevant; the amulet overrides it.

Your combat engine logic:

onEquip(AmuletOfAvarice):
    player.setSkulled(true)
onUnequip(AmuletOfAvarice):
    player.setSkulled(false)


No timer, no decay.

Effect 2: Revenants become permanently aggressive

Revenants treat you like a free target as long as you wear it.

Blocked only if:

You have a charged Bracelet of ethereum equipped

And the bracelet has charges

Otherwise:

They will attack even if you attack someone else

They ignore the normal “aggro cooldown”

Game logic:

function revenantShouldAggro(player):
    if player.isWearing(AmuletOfAvarice):
        return true    // force-aggro
    if player.wearingChargedEthereumBracelet():
        return false   // tolerance override
    return normalAggroLogic()

Effect 3: Acts as a Salve amulet (ei)

Only inside the Revenant Caves.

Boost (same as Salve (ei)):

+20 percent melee damage

+20 percent melee accuracy

+20 percent ranged damage

+20 percent ranged accuracy

+20 percent magic damage

+20 percent magic accuracy

In your engine:

if player.isInRevenantCaves and player.isWearing(AmuletOfAvarice):
    if target.isUndead:
        modifyDamage(damage * 1.20)
        modifyAccuracy(accuracy * 1.20)


The bonus never works outside the Rev Caves.

Note: Revenants count as undead.

Effect 4: Always lost on death, cannot be protected

Same behaviour as the bracelet:

Always dropped on death in the Wilderness.

Protection prayers do nothing.

Untradeable, so death converts it to coins (in OSRS).
In your RSPS or game you can choose:

Drop the amulet directly

Destroy it and spawn coins

Convert to x ether

Whatever fits your design

OSRS example logic:

onPlayerDeath(player):
    if player.hasEquipped(AmuletOfAvarice):
        dropItemOnGround(AMULET_OF_AVARICE)

3. Optional: Ether interaction

In OSRS:

Using the amulet with revenant ether corrupts the salve amulet (ei) into the amulet of avarice.

The amulet itself does not store ether and does not drain ether.

You can skip this step in your game or include it:

Crafting formula:

salve amulet (ei) + 1000 ether = amulet of avarice

4. Combined behaviour with Bracelet of Ethereum

These two items are designed to interact:

Case 1:

Amulet of avarice equipped + charged Bracelet of ethereum equipped

You are skulled

Revenants are not aggressive to you (bracelet overrides)

You take no or reduced damage from revenants

You get the Salve (ei) boosts inside Rev Caves

Case 2:

Amulet of avarice equipped + no bracelet or empty bracelet

You are skulled

Revenants are aggressively attacking you

You take full damage from them

You still get Salve (ei) boosts

Case 3:

Bracelet only

No skull

Revenants passive

You take no or reduced damage

No Salve bonuses

Implementing this cleanly means making both items check each other’s state.

5. Implementation summary for your game engine

Here’s the quick spec you can drop into your RSPS/game server code:

Item: AMULET_OF_AVARICE
Type: amulet
Tradable: false
AlwaysLostOnDeath: true

On Equip:
    player.skull = true

On Unequip:
    player.skull = false

Passive:
    if player.isInRevCaves and target.isUndead:
        player.damageMultiplier *= 1.20
        player.accuracyMultiplier *= 1.20

Aggro Rule:
    if wearing Amulet:
        revenants always aggro unless wearing charged Ethereum bracelet