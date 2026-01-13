package com.sweak.qralarm.features.menu.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun DefaultCodeEntry(
    onClick: () -> Unit,
    assignedCode: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = MaterialTheme.space.smallMedium,
                    horizontal = MaterialTheme.space.medium
                )
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.default_alarm_code),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                )

                Text(
                    text = assignedCode ?: stringResource(R.string.no_assigned_code),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Icon(
                imageVector = QRAlarmIcons.ForwardArrow,
                contentDescription =
                    stringResource(R.string.content_description_forward_arrow_icon),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
        )
    }
}

@Preview
@Composable
private fun DefaultCodeEntryPreview() {
    QRAlarmTheme {
        DefaultCodeEntry(
            onClick = {},
            assignedCode = null
        )
    }
}