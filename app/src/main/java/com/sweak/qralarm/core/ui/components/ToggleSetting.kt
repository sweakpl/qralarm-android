package com.sweak.qralarm.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun ToggleSetting(
    isChecked: Boolean,
    onCheckedChange: (checked: Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    compactHeight: Boolean = false
) {
    Box(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    horizontal = MaterialTheme.space.medium,
                    vertical =
                        if (compactHeight) MaterialTheme.space.small else MaterialTheme.space.medium
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.space.xSmall),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.space.smallMedium)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )

                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            QRAlarmSwitch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Preview
@Composable
private fun ToggleSettingPreview() {
    QRAlarmTheme {
        ToggleSetting(
            isChecked = true,
            onCheckedChange = {},
            title = "Open code link",
            description = "If the code contains a valid URL the app will open it automatically upon scanning."
        )
    }
}