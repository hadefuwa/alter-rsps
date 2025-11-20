package org.alter.game.saving.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.alter.game.model.appearance.Appearance
import org.alter.game.model.appearance.Gender
import org.alter.game.model.appearance.Looks
import org.alter.game.model.entity.Client
import org.alter.game.saving.DocumentHandler
import org.bson.Document

private val logger = KotlinLogging.logger {}

class AppearanceSerialisation(override val name: String = "appearance") : DocumentHandler {

    override fun fromDocument(client: Client, doc: Document) {
        val appearance = Appearance.fromDocument(doc)
        // Validate and fix corrupted appearance data
        client.appearance = validateAndFixAppearance(appearance, client.loginUsername)
    }

    override fun asDocument(client: Client): Document {
        return client.appearance.asDocument()
    }
    
    /**
     * Validates and fixes corrupted appearance data by ensuring all indices are within valid bounds.
     */
    private fun validateAndFixAppearance(appearance: Appearance, username: String): Appearance {
        val fixedLooks = appearance.looks.copyOf()
        val fixedColors = appearance.colors.copyOf()
        var needsFix = false
        
        when (appearance.gender) {
            Gender.MALE -> {
                // MALE has 7 looks: HEAD, JAW, TORSO, ARMS, HANDS, LEGS, FEET
                if (fixedLooks.size < 7) {
                    logger.warn { "Player $username has invalid MALE looks array size (${fixedLooks.size}), resetting to default" }
                    return Appearance.DEFAULT_MALE
                }
                
                // Validate each look index
                val heads = Looks.getHeads(Gender.MALE)
                if (fixedLooks[0] !in heads.indices) {
                    fixedLooks[0] = 0
                    needsFix = true
                }
                
                val jaws = Looks.getJaws(Gender.MALE)
                if (fixedLooks[1] !in jaws.indices) {
                    fixedLooks[1] = 0
                    needsFix = true
                }
                
                val torsos = Looks.getTorsos(Gender.MALE)
                if (fixedLooks[2] !in torsos.indices) {
                    fixedLooks[2] = 0
                    needsFix = true
                }
                
                val arms = Looks.getArms(Gender.MALE)
                if (fixedLooks[3] !in arms.indices) {
                    fixedLooks[3] = 0
                    needsFix = true
                }
                
                val hands = Looks.getHands(Gender.MALE)
                if (fixedLooks[4] !in hands.indices) {
                    fixedLooks[4] = 0
                    needsFix = true
                }
                
                val legs = Looks.getLegs(Gender.MALE)
                if (fixedLooks[5] !in legs.indices) {
                    fixedLooks[5] = 0
                    needsFix = true
                }
                
                val feets = Looks.getFeets(Gender.MALE)
                if (fixedLooks[6] !in feets.indices) {
                    fixedLooks[6] = 0
                    needsFix = true
                }
            }
            Gender.FEMALE -> {
                // FEMALE has 6 looks: HEAD, TORSO, ARMS, HANDS, LEGS, FEET (no JAW)
                if (fixedLooks.size < 6) {
                    logger.warn { "Player $username has invalid FEMALE looks array size (${fixedLooks.size}), resetting to default" }
                    return Appearance.DEFAULT_FEMALE
                }
                
                // Validate each look index
                val heads = Looks.getHeads(Gender.FEMALE)
                if (fixedLooks[0] !in heads.indices) {
                    fixedLooks[0] = 0
                    needsFix = true
                }
                
                val torsos = Looks.getTorsos(Gender.FEMALE)
                if (fixedLooks[1] !in torsos.indices) {
                    fixedLooks[1] = 0
                    needsFix = true
                }
                
                val arms = Looks.getArms(Gender.FEMALE)
                if (fixedLooks[2] !in arms.indices) {
                    fixedLooks[2] = 0
                    needsFix = true
                }
                
                val hands = Looks.getHands(Gender.FEMALE)
                if (fixedLooks[3] !in hands.indices) {
                    fixedLooks[3] = 0
                    needsFix = true
                }
                
                val legs = Looks.getLegs(Gender.FEMALE)
                if (fixedLooks[4] !in legs.indices) {
                    fixedLooks[4] = 0
                    needsFix = true
                }
                
                val feets = Looks.getFeets(Gender.FEMALE)
                if (fixedLooks[5] !in feets.indices) {
                    fixedLooks[5] = 0
                    needsFix = true
                }
            }
        }
        
        // Validate colors (should be 5 colors)
        if (fixedColors.size < 5) {
            logger.warn { "Player $username has invalid colors array size (${fixedColors.size}), resetting to default" }
            return when (appearance.gender) {
                Gender.MALE -> Appearance.DEFAULT_MALE
                Gender.FEMALE -> Appearance.DEFAULT_FEMALE
            }
        }
        
        // Colors are typically 0-27 for most, 0-23 for hair, 0-6 for skin, 0-4 for feet
        // Just ensure they're non-negative and reasonable
        for (i in fixedColors.indices) {
            if (fixedColors[i] < 0) {
                fixedColors[i] = 0
                needsFix = true
            }
        }
        
        if (needsFix) {
            logger.warn { "Fixed corrupted appearance data for player $username" }
        }
        
        return Appearance(fixedLooks, fixedColors, appearance.gender)
    }

}