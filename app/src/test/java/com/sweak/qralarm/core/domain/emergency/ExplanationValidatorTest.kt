package com.sweak.qralarm.core.domain.emergency

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import kotlin.random.Random

/**
 * Labelled corpus: every genuine explanation must be accepted, every gaming attempt rejected.
 */
class ExplanationValidatorTest {

    private val genuine: List<String> = javaClass.classLoader!!
        .getResource("explanations/genuine.txt")!!.readText()
        .split("\n---\n").map { it.trim() }.filter { it.isNotEmpty() }

    private val gaming: Map<String, String> by lazy {
        val firstGenuineWords = genuine[0].split(Regex("\\s+"))
        val random = Random(42)
        val dictionary = listOf(
            "window", "orange", "planet", "guitar", "pencil", "garden", "rocket", "silver",
            "bottle", "candle", "forest", "button", "pillow", "mirror", "basket", "ladder",
            "jacket", "carpet", "turtle", "bridge", "castle", "rabbit", "violin", "pepper",
            "monkey", "tomato", "wallet", "kitten", "anchor", "cookie", "dragon", "engine",
            "falcon", "hammer", "island", "jungle", "kettle", "lemon", "magnet", "napkin",
            "oyster", "parrot", "quartz", "riddle", "saddle", "tunnel", "umpire", "velvet",
            "walnut", "yellow", "zipper", "almond", "breeze", "copper", "desert", "fabric"
        )
        val paragraph = genuine[1].split(Regex("\\s+")).take(110).joinToString(" ")
        mapOf(
            "lorem ipsum" to List(5) {
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                    "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                    "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
            }.joinToString(" ").split(" ").take(220).joinToString(" "),
            "one sentence repeated" to
                List(30) { "I cannot find the bottle this morning." }.joinToString(" "),
            "paragraph pasted twice" to "$paragraph $paragraph",
            "keyboard mash" to List(220) {
                val letters = "qwrtpsdfghjklzxcvbnm"
                (1..random.nextInt(4, 9)).map { letters[random.nextInt(letters.length)] }
                    .joinToString("")
            }.chunked(10).joinToString(" ") { it.joinToString(" ") + "." },
            "dictionary word list" to List(220) { dictionary[random.nextInt(dictionary.size)] }
                .chunked(20).joinToString(" ") { it.joinToString(" ") + "." },
            "one word spam" to List(220) { if (it % 9 == 0) "alarm." else "alarm" }.joinToString(" "),
            "no punctuation run-on" to genuine[2].replace(Regex("[.!?]"), "")
                .split(Regex("\\s+")).take(210).joinToString(" "),
            "199 words" to firstGenuineWords.take(199).joinToString(" ") + ".",
            "251 words" to (firstGenuineWords + genuine[3].split(Regex("\\s+")))
                .take(251).joinToString(" ") + ".",
            "empty" to "",
            "repeated short variants" to List(44) { index ->
                listOf("I am tired.", "I want sleep.", "Please stop now.", "No bottle here.")[index % 4]
            }.joinToString(" ") + List(40) { "Stop it." }.joinToString(" "),
            "numbers" to List(220) { (it * 7).toString() }.joinToString(" ")
        )
    }

    @Test
    fun corpusIsLabelledAsExpected() {
        assertTrue("genuine corpus too small", genuine.size >= 12)
        genuine.forEachIndexed { index, text ->
            val result = ExplanationValidator.validate(text, Locale.ENGLISH)
            assertTrue("genuine #$index rejected: ${result.issues}", result.isValid)
        }
        gaming.forEach { (name, text) ->
            val result = ExplanationValidator.validate(text, Locale.ENGLISH)
            assertFalse("gaming attempt '$name' accepted (${result.wordCount} words)", result.isValid)
        }
    }

    @Test
    fun boundaryWordCounts() {
        assertEquals(
            ExplanationValidator.Issue.TooShort(199),
            ExplanationValidator.validate(gaming.getValue("199 words"), Locale.ENGLISH).issues.first()
        )
        assertTrue(
            ExplanationValidator.validate(gaming.getValue("251 words"), Locale.ENGLISH).issues
                .contains(ExplanationValidator.Issue.TooLong(251))
        )
    }

    @Test
    fun countsWordsWithoutSpaces() {
        // Japanese has no spaces between words; whitespace splitting would count one "word".
        assertTrue(ExplanationValidator.countWords("私は今朝薬を飲みました。", Locale.JAPANESE) > 3)
    }

    @Test
    fun englishOnlyChecksAreSkippedForOtherLanguages() {
        val english = genuine[0]
        val result = ExplanationValidator.validate(english, Locale.GERMAN)
        assertFalse(result.issues.contains(ExplanationValidator.Issue.NotProse))
    }
}
