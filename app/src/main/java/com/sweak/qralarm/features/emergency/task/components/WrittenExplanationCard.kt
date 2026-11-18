package com.sweak.qralarm.features.emergency.task.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.emergency.ExplanationValidator
import com.sweak.qralarm.features.emergency.task.EmergencyScreenState

@Composable
fun WrittenExplanationCard(
    explanation: EmergencyScreenState.ExplanationState,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.explanation_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.explanation_instructions), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = explanation.text,
                onValueChange = onTextChanged,
                enabled = !explanation.isCompleted,
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 320.dp)
            )
            val inRange = explanation.wordCount in ExplanationValidator.MIN_WORDS..ExplanationValidator.MAX_WORDS
            Text(
                stringResource(
                    R.string.explanation_word_count, explanation.wordCount,
                    ExplanationValidator.MIN_WORDS, ExplanationValidator.MAX_WORDS
                ),
                color = if (inRange) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (explanation.typingSecondsRemaining > 0) {
                Text(
                    stringResource(R.string.explanation_typing_time, explanation.typingSecondsRemaining),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (explanation.pasteRejected) {
                Text(stringResource(R.string.explanation_paste_rejected), color = MaterialTheme.colorScheme.error)
            }
            explanation.issues.forEach { issue ->
                Text(issueText(issue), color = MaterialTheme.colorScheme.error)
            }
            if (explanation.isCompleted) {
                Text(stringResource(R.string.explanation_accepted), color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = onSubmit, enabled = explanation.canSubmit) {
                Text(stringResource(R.string.explanation_submit))
            }
        }
    }
}

@Composable
private fun issueText(issue: ExplanationValidator.Issue): String = when (issue) {
    is ExplanationValidator.Issue.TooShort ->
        pluralStringResource(R.plurals.explanation_too_short, ExplanationValidator.MIN_WORDS - issue.wordCount,
            ExplanationValidator.MIN_WORDS - issue.wordCount)
    is ExplanationValidator.Issue.TooLong ->
        pluralStringResource(R.plurals.explanation_too_long, issue.wordCount - ExplanationValidator.MAX_WORDS,
            issue.wordCount - ExplanationValidator.MAX_WORDS)
    ExplanationValidator.Issue.TooRepetitive -> stringResource(R.string.explanation_issue_repetitive)
    is ExplanationValidator.Issue.OverusedWord -> stringResource(R.string.explanation_issue_overused_word, issue.word)
    ExplanationValidator.Issue.RepeatedPhrases -> stringResource(R.string.explanation_issue_repeated_phrases)
    ExplanationValidator.Issue.TooFewSentences -> stringResource(R.string.explanation_issue_sentences)
    ExplanationValidator.Issue.UnreadableWords -> stringResource(R.string.explanation_issue_unreadable)
    ExplanationValidator.Issue.NotProse -> stringResource(R.string.explanation_issue_not_prose)
    ExplanationValidator.Issue.FillerText -> stringResource(R.string.explanation_issue_filler)
}
