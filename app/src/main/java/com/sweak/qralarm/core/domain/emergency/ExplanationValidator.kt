package com.sweak.qralarm.core.domain.emergency

import java.text.BreakIterator
import java.util.Locale

/**
 * Checks that a written emergency explanation is a genuine piece of prose rather than filler.
 * Words and sentences are split with [BreakIterator], so languages without spaces between words
 * are counted correctly. Checks that only make sense for English are applied to English only.
 */
object ExplanationValidator {
    const val MIN_WORDS = 200
    const val MAX_WORDS = 250

    const val MIN_UNIQUE_WORD_RATIO = 0.40
    const val MAX_SINGLE_WORD_SHARE = 0.07
    const val MAX_REPEATED_PHRASE_SHARE = 0.10
    const val PHRASE_LENGTH = 5
    const val MIN_SENTENCES = 4
    const val MAX_UNREADABLE_WORD_SHARE = 0.05
    const val MIN_ENGLISH_FUNCTION_WORD_SHARE = 0.20
    const val MIN_FILLER_WORDS = 5

    sealed interface Issue {
        data class TooShort(val wordCount: Int) : Issue
        data class TooLong(val wordCount: Int) : Issue
        data object TooRepetitive : Issue
        data class OverusedWord(val word: String) : Issue
        data object RepeatedPhrases : Issue
        data object TooFewSentences : Issue
        data object UnreadableWords : Issue
        data object NotProse : Issue
        data object FillerText : Issue
    }

    data class Result(val wordCount: Int, val issues: List<Issue>) {
        val isValid: Boolean get() = issues.isEmpty()
    }

    fun countWords(text: String, locale: Locale = Locale.getDefault()): Int =
        words(text, locale).size

    fun validate(text: String, locale: Locale = Locale.getDefault()): Result {
        val words = words(text, locale)
        val count = words.size
        val issues = mutableListOf<Issue>()

        if (count < MIN_WORDS) issues += Issue.TooShort(count)
        if (count > MAX_WORDS) issues += Issue.TooLong(count)
        // Quality checks need a meaningful sample; until then only the length is reported.
        if (count < MIN_WORDS / 2) return Result(count, issues)

        val isEnglish = locale.language == Locale.ENGLISH.language

        if (words.toSet().size.toDouble() / count < MIN_UNIQUE_WORD_RATIO) {
            issues += Issue.TooRepetitive
        }

        words.filter { word -> if (isEnglish) word !in ENGLISH_FUNCTION_WORDS else word.length > 3 }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }
            ?.takeIf { it.value.toDouble() / count > MAX_SINGLE_WORD_SHARE }
            ?.let { issues += Issue.OverusedWord(it.key) }

        if (repeatedPhraseShare(words) > MAX_REPEATED_PHRASE_SHARE) issues += Issue.RepeatedPhrases

        if (sentenceCount(text, locale) < MIN_SENTENCES) issues += Issue.TooFewSentences

        val latinWords = words.filter { word -> word.all { it.isLatinLetterOrApostrophe() } }
        if (latinWords.size >= count / 2) {
            val unreadable = latinWords.count(::looksLikeKeyboardMash)
            if (unreadable.toDouble() / latinWords.size > MAX_UNREADABLE_WORD_SHARE) {
                issues += Issue.UnreadableWords
            }
        }

        if (isEnglish &&
            words.count { it in ENGLISH_FUNCTION_WORDS }.toDouble() / count < MIN_ENGLISH_FUNCTION_WORD_SHARE
        ) {
            issues += Issue.NotProse
        }

        if (words.toSet().count { it in FILLER_WORDS } >= MIN_FILLER_WORDS) issues += Issue.FillerText

        return Result(count, issues)
    }

    private fun words(text: String, locale: Locale): List<String> {
        val iterator = BreakIterator.getWordInstance(locale).apply { setText(text) }
        val result = mutableListOf<String>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            val token = text.substring(start, end)
            if (token.any { it.isLetter() }) {
                result += token.lowercase(locale).replace('’', '\'')
            }
            start = end
            end = iterator.next()
        }
        return result
    }

    private fun sentenceCount(text: String, locale: Locale): Int {
        val iterator = BreakIterator.getSentenceInstance(locale).apply { setText(text.trim()) }
        var sentences = 0
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            if (text.trim().substring(start, end).count { it.isLetter() } >= 10) sentences++
            start = end
            end = iterator.next()
        }
        return sentences
    }

    /** Share of words that belong to a [PHRASE_LENGTH]-word phrase which occurs more than once. */
    private fun repeatedPhraseShare(words: List<String>): Double {
        if (words.size < PHRASE_LENGTH) return 0.0
        val phrases = words.windowed(PHRASE_LENGTH)
        val counts = phrases.groupingBy { it }.eachCount()
        val covered = BooleanArray(words.size)
        phrases.forEachIndexed { index, phrase ->
            if (counts.getValue(phrase) > 1) {
                for (offset in 0 until PHRASE_LENGTH) covered[index + offset] = true
            }
        }
        return covered.count { it }.toDouble() / words.size
    }

    private fun looksLikeKeyboardMash(word: String): Boolean {
        val letters = word.filter { it.isLetter() }
        if (letters.length < 4) return false
        if (letters.none { it in VOWELS }) return true
        var run = 0
        for (letter in letters) {
            run = if (letter in VOWELS) 0 else run + 1
            if (run >= 5) return true
        }
        return letters.length >= 6 && letters.toSet().size <= 2
    }

    private fun Char.isLatinLetterOrApostrophe() =
        this == '\'' || (isLetter() && Character.UnicodeScript.of(code) == Character.UnicodeScript.LATIN)

    private const val VOWELS = "aeiouyàáâäãåāèéêëēìíîïòóôöõøōùúûüūæœ"

    private val FILLER_WORDS = setOf(
        "lorem", "ipsum", "dolor", "amet", "consectetur", "adipiscing", "elit", "eiusmod",
        "tempor", "incididunt", "labore", "dolore", "aliqua", "veniam", "nostrud", "exercitation",
        "ullamco", "laboris", "aliquip", "commodo", "consequat"
    )

    private val ENGLISH_FUNCTION_WORDS = setOf(
        "a", "about", "after", "again", "all", "also", "am", "an", "and", "any", "are", "as", "at",
        "be", "because", "been", "before", "being", "but", "by", "can", "can't", "cannot", "could",
        "did", "didn't", "do", "does", "doesn't", "don't", "down", "for", "from", "had", "has",
        "have", "haven't", "he", "her", "here", "him", "his", "how", "i", "i'm", "i've", "i'll",
        "i'd", "if", "in", "into", "is", "isn't", "it", "it's", "its", "just", "me", "my", "no",
        "not", "now", "of", "off", "on", "once", "only", "or", "other", "our", "out", "over", "own",
        "should", "so", "some", "such", "than", "that", "that's", "the", "their", "them", "then",
        "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up",
        "very", "was", "wasn't", "we", "were", "what", "when", "where", "which", "while", "who",
        "why", "will", "with", "won't", "would", "you", "your"
    )
}
