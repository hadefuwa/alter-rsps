package org.alter.game.model.appearance

import org.alter.game.model.appearance.Looks.getArms
import org.alter.game.model.appearance.Looks.getFeets
import org.alter.game.model.appearance.Looks.getHands
import org.alter.game.model.appearance.Looks.getHeads
import org.alter.game.model.appearance.Looks.getJaws
import org.alter.game.model.appearance.Looks.getLegs
import org.alter.game.model.appearance.Looks.getTorsos
import org.alter.game.model.item.Item
import org.bson.Document


/**
 * @author Tom <rspsmods@gmail.com>
 */
data class Appearance(val looks: IntArray, val colors: IntArray, var gender: Gender) {

    /**
     * @param option - the specified look to select from the [Appearance]'s [looks]
     *      with valid options explicitly as follows:
     *      0 -> HEAD
     *      1 -> JAW
     *      2 -> TORSO
     *      3 -> ARMS
     *      4 -> HANDS
     *      5 -> LEGS
     *      6 -> FEET
     * Note| the JAW option is currently not provided for [Gender.FEMALE]
     *
     * @returns the appropriate look model value for current appearance
     *      based on the supplies option
     */
    fun getLook(option: Int): Int {
        return when (gender) {
            Gender.MALE -> {
                when (option) {
                    0 -> {
                        val heads = getHeads(gender)
                        if (looks.size > 0 && looks[0] in heads.indices) heads[looks[0]] else heads[0]
                    }
                    1 -> {
                        val jaws = getJaws(gender)
                        if (looks.size > 1 && looks[1] in jaws.indices) jaws[looks[1]] else jaws[0]
                    }
                    2 -> {
                        val torsos = getTorsos(gender)
                        if (looks.size > 2 && looks[2] in torsos.indices) torsos[looks[2]] else torsos[0]
                    }
                    3 -> {
                        val arms = getArms(gender)
                        if (looks.size > 3 && looks[3] in arms.indices) arms[looks[3]] else arms[0]
                    }
                    4 -> {
                        val hands = getHands(gender)
                        if (looks.size > 4 && looks[4] in hands.indices) hands[looks[4]] else hands[0]
                    }
                    5 -> {
                        val legs = getLegs(gender)
                        if (looks.size > 5 && looks[5] in legs.indices) legs[looks[5]] else legs[0]
                    }
                    6 -> {
                        val feets = getFeets(gender)
                        if (looks.size > 6 && looks[6] in feets.indices) feets[looks[6]] else feets[0]
                    }
                    else -> -1
                }
            }
            Gender.FEMALE -> {
                when (option) {
                    0 -> {
                        val heads = getHeads(gender)
                        if (looks.size > 0 && looks[0] in heads.indices) heads[looks[0]] else heads[0]
                    }
                    2 -> {
                        val torsos = getTorsos(gender)
                        if (looks.size > 1 && looks[1] in torsos.indices) torsos[looks[1]] else torsos[0]
                    }
                    3 -> {
                        val arms = getArms(gender)
                        if (looks.size > 2 && looks[2] in arms.indices) arms[looks[2]] else arms[0]
                    }
                    4 -> {
                        val hands = getHands(gender)
                        if (looks.size > 3 && looks[3] in hands.indices) hands[looks[3]] else hands[0]
                    }
                    5 -> {
                        val legs = getLegs(gender)
                        if (looks.size > 4 && looks[4] in legs.indices) legs[looks[4]] else legs[0]
                    }
                    6 -> {
                        val feets = getFeets(gender)
                        if (looks.size > 5 && looks[5] in feets.indices) feets[looks[5]] else feets[0]
                    }
                    else -> -1
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Appearance

        if (!looks.contentEquals(other.looks)) return false
        if (!colors.contentEquals(other.colors)) return false
        if (gender != other.gender) return false

        return true
    }

    override fun hashCode(): Int {
        var result = looks.contentHashCode()
        result = 31 * result + colors.contentHashCode()
        result = 31 * result + gender.hashCode()
        return result
    }


    //TODO MAP Appearance of HEAD:VALUE
    fun asDocument(): Document {
        return Document()
            .append("looks", looks.toList())
            .append("colors", colors.toList())
            .append("gender", gender.name)
    }

    companion object {

        fun fromDocument(doc: Document): Appearance {
            return Appearance(
                doc.getList("looks", Integer::class.java).map { it.toInt() }.toIntArray(),
                doc.getList("colors", Integer::class.java).map { it.toInt() }.toIntArray(),
                Gender.valueOf(doc.getString("gender") ?: "MALE")
            )
        }

        private val DEFAULT_COLORS = intArrayOf(0, 27, 9, 0, 0)

        private val DEFAULT_MALE_LOOKS = intArrayOf(15, 9, 3, 8, 0, 3, 1) // 133, 113, 21, 86, 33, 39, 43
        val DEFAULT_MALE = Appearance(DEFAULT_MALE_LOOKS, DEFAULT_COLORS, Gender.MALE)

        private val DEFAULT_FEMALE_LOOKS = intArrayOf(0, 0, 0, 0, 0, 0) // 45, 56, 61, 67, 70, 79
        val DEFAULT_FEMALE = Appearance(DEFAULT_FEMALE_LOOKS, DEFAULT_COLORS, Gender.FEMALE)
    }
}
