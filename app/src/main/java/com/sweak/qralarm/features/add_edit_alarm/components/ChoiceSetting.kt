package com.sweak.qralarm.features.add_edit_alarm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun ChoiceSetting(
    onClick: () -> Unit,
    title: String,
    description: String? = null,
    choiceName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clickable { onClick() }) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.medium)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = choiceName,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = MaterialTheme.space.small)
                )

                Icon(
                    imageVector = QRAlarmIcons.ForwardArrow,
                    contentDescription = stringResource(
                        R.string.content_description_forward_arrow_icon
                    ),
                    modifier = Modifier.size(size = MaterialTheme.space.medium)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChoiceSettingPreview() {
    QRAlarmTheme {
        ChoiceSetting(
            onClick = {},
            title = "Gentle Wake Up",
            description = "Alarm volume will increase for the specified time.",
            choiceName = "30 sec"
        )
    }
}