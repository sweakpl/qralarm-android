package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.Mobster
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

private const val MAX_CODE_NAME_LENGTH = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCodeNameBottomSheet(
    initialCodeName: String?,
    onDismissRequest: (newCodeName: String?) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initialCodeName ?: "") }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(name.takeIf { it.isNotBlank() }) },
        sheetState = modalBottomSheetState
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.mediumLarge,
                    end = MaterialTheme.space.mediumLarge,
                    bottom = MaterialTheme.space.xLarge
                )
        ) {
            Text(
                text = stringResource(R.string.edit_code_name),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            )

            Text(
                text = stringResource(R.string.edit_code_name_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.space.mediumLarge)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (MaterialTheme.isQRAlarmTheme) Color.White
                    else Color.Unspecified,
                    contentColor = if (MaterialTheme.isQRAlarmTheme) Color.Black
                    else Color.Unspecified
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.space.medium)
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Label,
                        contentDescription = stringResource(R.string.content_description_label_icon),
                        modifier = Modifier.padding(end = MaterialTheme.space.medium)
                    )

                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= MAX_CODE_NAME_LENGTH) name = it },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = LocalContentColor.current
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(LocalContentColor.current),
                        decorationBox = { innerTextField ->
                            if (name.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.enter_code_name),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Mobster
                                    )
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditCodeNameBottomSheetPreview() {
    var previewName by remember { mutableStateOf("Coffee bag code") }

    QRAlarmTheme {
        EditCodeNameBottomSheet(
            initialCodeName = previewName,
            onDismissRequest = { previewName = it ?: "" }
        )
    }
}
