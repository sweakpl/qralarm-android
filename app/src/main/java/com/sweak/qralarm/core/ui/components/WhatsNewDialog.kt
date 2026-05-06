package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

private data class WhatsNewEntry(
    val text: String,
    val notes: List<String> = emptyList()
)

@Composable
fun WhatsNewDialog(onDismissRequest: () -> Unit) {
    val entries = listOf(
        WhatsNewEntry(
            text = stringResource(R.string.whats_new_entry_1),
            notes = listOf(
                stringResource(R.string.whats_new_entry_1_note_1),
                stringResource(R.string.whats_new_entry_1_note_2)
            )
        ),
        WhatsNewEntry(text = stringResource(R.string.whats_new_entry_2))
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            color = with(MaterialTheme) {
                if (isQRAlarmTheme) colorScheme.surfaceContainerHighest else colorScheme.surface
            },
            modifier = Modifier.clip(MaterialTheme.shapes.medium)
        ) {
            Column(modifier = Modifier.wrapContentSize()) {
                Text(
                    text = stringResource(R.string.whats_new_title),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(all = MaterialTheme.space.medium)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.small,
                            end = MaterialTheme.space.medium
                        )
                ) {
                    entries.forEach { entry ->
                        WhatsNewEntryItem(entry = entry)
                    }
                }

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = with(MaterialTheme) {
                            if (isQRAlarmTheme) Jacarta else colorScheme.primary
                        },
                        contentColor = with(MaterialTheme) {
                            if (isQRAlarmTheme) contentColorFor(Jacarta)
                            else colorScheme.onPrimary
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = MaterialTheme.space.medium)
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        modifier = Modifier.padding(MaterialTheme.space.xSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsNewEntryItem(entry: WhatsNewEntry) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
        modifier = Modifier.padding(vertical = MaterialTheme.space.xSmall)
    ) {
        Text(
            text = "✅",
            style = MaterialTheme.typography.titleLarge
        )

        Column {
            Text(
                text = entry.text,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
            )

            if (entry.notes.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.xSmall)
                ) {
                    entry.notes.forEach { note ->
                        Text(
                            text = "• $note",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun WhatsNewDialogPreview() {
    QRAlarmTheme {
        WhatsNewDialog(onDismissRequest = { /* no-op */ })
    }
}
