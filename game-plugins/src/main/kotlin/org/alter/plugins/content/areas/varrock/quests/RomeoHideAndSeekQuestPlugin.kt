package org.alter.plugins.content.areas.varrock.quests

import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.Direction
import org.alter.game.model.Tile
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import org.alter.game.model.move.moveTo
import org.alter.game.model.queue.QueueTask
import org.alter.game.model.timer.TimerKey
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository
import org.alter.rscm.RSCM.getRSCM

/**
 * Romeo's Hide and Seek Quest
 *
 * A fun minigame where Romeo challenges the player to find him around Varrock.
 * Romeo starts at Varrock center and teleports to random locations.
 *
 * Rewards:
 * - Stage 1: 500,000 coins
 * - Stage 2: 1,000,000 coins
 * - Stage 3: 2,000,000 coins
 * - Stage 4: 5,000,000 coins
 * - Stage 5: 10,000,000 coins (Quest Complete)
 *
 * Romeo teleports to a new random location every 2 minutes to ensure accessibility.
 */
class RomeoHideAndSeekQuestPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    companion object {
        // Quest progress attribute (persistent)
        private val QUEST_STAGE_ATTR = AttributeKey<Int>(persistenceKey = "romeo_hide_seek_stage")

        // Timer for Romeo's teleportation (2 minutes = 200 cycles at 600ms/cycle)
        private val ROMEO_TELEPORT_TIMER = TimerKey()

        // Attribute to track current location index
        private val ROMEO_LOCATION_ATTR = AttributeKey<Int>()

        // Romeo NPC identifier
        private const val ROMEO_NPC = "npc.romeo"

        // Varrock center spawn location (where Romeo starts the quest)
        private val VARROCK_CENTER = Tile(x = 3211, z = 3424, height = 0)

        // Lowe's Archery Shop location (where Romeo stays after quest completion)
        private val ARCHERY_SHOP = Tile(x = 3232, z = 3425, height = 0)

        // Random locations around Varrock where Romeo can hide
        private val HIDING_LOCATIONS = listOf(
            // Varrock Square area
            Tile(x = 3211, z = 3424, height = 0), // Center
            Tile(x = 3214, z = 3428, height = 0), // Near fountain
            Tile(x = 3206, z = 3428, height = 0), // West side square
            Tile(x = 3216, z = 3421, height = 0), // East side square

            // Varrock Palace area
            Tile(x = 3210, z = 3457, height = 0), // Palace entrance
            Tile(x = 3222, z = 3473, height = 0), // Behind palace

            // Varrock East Bank
            Tile(x = 3253, z = 3420, height = 0), // East bank
            Tile(x = 3256, z = 3424, height = 0), // East bank side

            // Varrock West Bank
            Tile(x = 3185, z = 3436, height = 0), // West bank
            Tile(x = 3189, z = 3440, height = 0), // West bank side

            // General Store area
            Tile(x = 3217, z = 3412, height = 0), // General store
            Tile(x = 3229, z = 3408, height = 0), // Near sword shop

            // Church area
            Tile(x = 3254, z = 3483, height = 0), // Church
            Tile(x = 3250, z = 3476, height = 0), // Near church

            // Blue Moon Inn
            Tile(x = 3222, z = 3400, height = 0), // Blue Moon Inn
            Tile(x = 3228, z = 3399, height = 0), // Inn side

            // Varrock Museum area
            Tile(x = 3255, z = 3447, height = 0), // Museum
            Tile(x = 3260, z = 3453, height = 0), // Museum side

            // Tea stall area
            Tile(x = 3270, z = 3411, height = 0), // Tea stall

            // South gate
            Tile(x = 3237, z = 3390, height = 0), // South gate
            Tile(x = 3210, z = 3390, height = 0), // South gate west

            // North area
            Tile(x = 3212, z = 3471, height = 0), // North street
            Tile(x = 3203, z = 3471, height = 0), // Northwest area

            // Park area (east)
            Tile(x = 3283, z = 3432, height = 0), // Park area
            Tile(x = 3291, z = 3435, height = 0), // East park
        )

        // Reward amounts for each stage
        private val STAGE_REWARDS = mapOf(
            1 to 500_000,
            2 to 1_000_000,
            3 to 2_000_000,
            4 to 5_000_000,
            5 to 10_000_000
        )
    }

    private var romeoNpc: Npc? = null

    init {
        // Spawn Romeo at Varrock center (quest giver)
        spawnNpc(ROMEO_NPC, VARROCK_CENTER, walkRadius = 0, direction = Direction.SOUTH)

        // Spawn Romeo at Archery Shop (post-quest)
        spawnNpc(ROMEO_NPC, ARCHERY_SHOP, walkRadius = 3, direction = Direction.SOUTH)

        // Handle Romeo spawn - set up teleport timer only for the quest-active Romeo
        onGlobalNpcSpawn {
            if (npc.id == getRSCM(ROMEO_NPC)) {
                // Check if this is the Romeo at Varrock center (quest giver)
                if (npc.tile == VARROCK_CENTER) {
                    romeoNpc = npc
                    // Set initial random location
                    val randomIndex = world.random(HIDING_LOCATIONS.size - 1)
                    npc.attr[ROMEO_LOCATION_ATTR] = randomIndex

                    // Start the teleport timer (200 cycles = 2 minutes)
                    npc.timers[ROMEO_TELEPORT_TIMER] = 200
                }
            }
        }

        // Handle timer expiration - teleport Romeo to new location
        onTimer(ROMEO_TELEPORT_TIMER) {
            val npc = ctx as? Npc ?: return@onTimer
            if (npc.id != getRSCM(ROMEO_NPC)) return@onTimer

            // Get a new random location
            val newLocationIndex = world.random(HIDING_LOCATIONS.size - 1)
            npc.attr[ROMEO_LOCATION_ATTR] = newLocationIndex
            val newLocation = HIDING_LOCATIONS[newLocationIndex]

            // Teleport Romeo
            npc.moveTo(newLocation)

            // Reset timer for another 2 minutes
            npc.timers[ROMEO_TELEPORT_TIMER] = 200
        }

        // Handle talk-to option for Romeo
        onNpcOption(ROMEO_NPC, option = "talk-to") {
            player.queue { handleRomeoDialogue(player) }
        }
    }

    /**
     * Main dialogue handler for Romeo
     */
    private suspend fun QueueTask.handleRomeoDialogue(player: Player) {
        val currentStage = player.attr[QUEST_STAGE_ATTR] ?: 0
        val interactingNpc = player.getInteractingNpc()

        // Check if player is talking to Romeo at the Archery Shop (post-quest)
        // Check if NPC is within 5 tiles of the archery shop location
        val distanceToShop = Math.abs(interactingNpc.tile.x - ARCHERY_SHOP.x) + Math.abs(interactingNpc.tile.z - ARCHERY_SHOP.z)
        if (distanceToShop <= 5) {
            if (currentStage >= 5) {
                // Quest already complete
                postQuestDialogue(player)
            } else {
                // Player found Romeo before completing the quest
                chatNpc(player, "Hello there! I'm just browsing for arrows.", animation = 591)
                chatNpc(player, "If you want to play hide and seek, you'll need to find me at Varrock Square first!", animation = 588)
            }
            return
        }

        // Otherwise, handle normal quest dialogue
        when {
            currentStage == 0 -> startQuest(player)
            currentStage in 1..4 -> continueQuest(player, currentStage)
            currentStage >= 5 -> {
                // Quest complete, direct player to archery shop
                chatNpc(player, "Thanks again for playing with me! I'll be at Lowe's Archery Shop if you want to chat!", animation = 588)
            }
        }
    }

    /**
     * Quest start dialogue
     */
    private suspend fun QueueTask.startQuest(player: Player) {
        chatNpc(player, "Greetings, adventurer! I am Romeo, and I have a proposition for you!", animation = 588)
        chatPlayer(player, "What kind of proposition?", animation = 554)
        chatNpc(player, "I've grown quite bored lately... Juliet is busy, and I need some entertainment!", animation = 591)
        chatNpc(player, "How about a game of hide and seek? I'll hide around Varrock, and you try to find me!", animation = 588)

        when (options(
            player,
            "Sure, that sounds fun!",
            "What do I get if I find you?",
            "No thanks, I'm busy."
        )) {
            1 -> {
                chatPlayer(player, "Sure, that sounds fun! Let's do it!", animation = 588)
                startQuestRewards(player)
            }
            2 -> {
                chatPlayer(player, "What do I get if I find you?", animation = 554)
                chatNpc(player, "Ah, of course! I'll reward you handsomely each time you find me!", animation = 591)
                chatNpc(player, "500,000 coins for the first find, then 1 million, 2 million, 5 million...", animation = 588)
                chatNpc(player, "And if you can find me all 5 times, I'll give you 10 million coins!", animation = 588)

                when (options(player, "Alright, I'm in!", "That's not enough for me.")) {
                    1 -> {
                        chatPlayer(player, "Alright, I'm in! Let's start!", animation = 588)
                        startQuestRewards(player)
                    }
                    2 -> {
                        chatPlayer(player, "That's not enough for me.", animation = 589)
                        chatNpc(player, "Suit yourself! Come back if you change your mind.", animation = 592)
                    }
                }
            }
            3 -> {
                chatPlayer(player, "No thanks, I'm busy right now.", animation = 589)
                chatNpc(player, "No problem! Come find me if you change your mind.", animation = 591)
            }
        }
    }

    /**
     * Initialize the quest and teleport Romeo
     */
    private suspend fun QueueTask.startQuestRewards(player: Player) {
        chatNpc(player, "Excellent! Let the game begin!", animation = 588)
        chatNpc(player, "I'll teleport somewhere in Varrock. Come find me!", animation = 591)
        chatNpc(player, "Don't worry - if you can't find me, I'll move to a new spot every 2 minutes!", animation = 588)

        player.attr[QUEST_STAGE_ATTR] = 1

        // Teleport Romeo immediately
        romeoNpc?.let { npc ->
            val randomIndex = world.random(HIDING_LOCATIONS.size - 1)
            npc.attr[ROMEO_LOCATION_ATTR] = randomIndex
            val newLocation = HIDING_LOCATIONS[randomIndex]
            npc.graphic(343) // Teleport graphic
            npc.moveTo(newLocation)
            // Reset timer
            npc.timers[ROMEO_TELEPORT_TIMER] = 200
        }

        player.message("Romeo has teleported somewhere in Varrock! Go find him!")
    }

    /**
     * Continue quest - player found Romeo
     */
    private suspend fun QueueTask.continueQuest(player: Player, stage: Int) {
        val reward = STAGE_REWARDS[stage] ?: 0

        chatNpc(player, "You found me! Well done!", animation = 588)
        chatPlayer(player, "That was fun! What's my reward?", animation = 588)

        // Give reward
        if (!player.inventory.isFull) {
            player.inventory.add(getRSCM("item.coins_995"), reward)
            chatNpc(player, "Here's your reward: ${reward.formatNumber()} coins!", animation = 591)
            player.message("Romeo gives you ${reward.formatNumber()} coins!")
        } else {
            chatNpc(player, "Oh dear, your inventory is full! Make some space and come back!", animation = 589)
            return
        }

        // Check if quest is complete
        if (stage >= 4) {
            // Final stage
            chatNpc(player, "You've found me 5 times! You're amazing at this game!", animation = 588)
            chatNpc(player, "Here's your final reward - 10 million coins!", animation = 591)

            if (!player.inventory.isFull) {
                player.inventory.add(getRSCM("item.coins_995"), STAGE_REWARDS[5]!!)
                player.message("Romeo gives you ${STAGE_REWARDS[5]!!.formatNumber()} coins!")
                player.attr[QUEST_STAGE_ATTR] = 5
                chatNpc(player, "Thanks for playing with me! That was incredibly fun!", animation = 588)
                chatPlayer(player, "Thanks for all the coins!", animation = 588)
            } else {
                chatNpc(player, "Your inventory is full! Make space for your final reward!", animation = 589)
            }
        } else {
            // Continue to next stage
            chatNpc(player, "Ready for round ${stage + 1}? I'll hide again!", animation = 588)

            when (options(player, "Yes, let's continue!", "I need a break.")) {
                1 -> {
                    chatPlayer(player, "Yes, let's continue!", animation = 588)
                    chatNpc(player, "Here I go! Find me again!", animation = 591)

                    player.attr[QUEST_STAGE_ATTR] = stage + 1

                    // Teleport Romeo again
                    romeoNpc?.let { npc ->
                        val randomIndex = world.random(HIDING_LOCATIONS.size - 1)
                        npc.attr[ROMEO_LOCATION_ATTR] = randomIndex
                        val newLocation = HIDING_LOCATIONS[randomIndex]
                        npc.graphic(343)
                        npc.moveTo(newLocation)
                        npc.timers[ROMEO_TELEPORT_TIMER] = 200
                    }

                    player.message("Romeo has teleported again! Go find him!")
                }
                2 -> {
                    chatPlayer(player, "I need a break for now.", animation = 589)
                    chatNpc(player, "No problem! Come back when you're ready to continue.", animation = 591)
                    player.attr[QUEST_STAGE_ATTR] = stage + 1
                }
            }
        }
    }

    /**
     * Post-quest dialogue (at Archery Shop)
     */
    private suspend fun QueueTask.postQuestDialogue(player: Player) {
        chatNpc(player, "Hello again, friend! Thanks for playing hide and seek with me!", animation = 588)
        chatNpc(player, "It was such great fun! I decided to stay here at the archery shop.", animation = 591)
        chatPlayer(player, "It was a fun quest! Thanks for all the coins!", animation = 588)
        chatNpc(player, "You're very welcome! Perhaps we can play again another time!", animation = 591)

        when (options(player, "Why are you at the archery shop?", "Maybe we can play again?", "See you later!")) {
            1 -> {
                chatPlayer(player, "Why are you at the archery shop?", animation = 554)
                chatNpc(player, "Well, after all that running around, I thought I'd pick up some arrows!", animation = 588)
                chatNpc(player, "Plus, Lowe is a good friend. We have great conversations about love and adventure!", animation = 591)
            }
            2 -> {
                chatPlayer(player, "Maybe we can play again?", animation = 554)
                chatNpc(player, "Hmm, that would be fun, but I think one adventure is enough for now!", animation = 591)
                chatNpc(player, "Besides, Juliet would not be happy if I disappeared again!", animation = 589)
            }
            3 -> {
                chatPlayer(player, "See you later, Romeo!", animation = 588)
                chatNpc(player, "Take care, friend! Thanks again for the fun times!", animation = 591)
            }
        }
    }

    /**
     * Extension function to format numbers with commas
     */
    private fun Int.formatNumber(): String {
        return String.format("%,d", this)
    }
}
