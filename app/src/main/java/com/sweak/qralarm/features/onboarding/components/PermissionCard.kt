package com.sweak.qralarm.features.onboarding.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun PermissionCard(
    icon: ImageVector,
    iconContentDescription: String,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    isClickable: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = isClickable) { onClick() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription
            )

            Column(
                modifier = Modifier
                    .padding(all = MaterialTheme.space.medium)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector =
                    if (isGranted) QRAlarmIcons.Done else QRAlarmIcons.ForwardArrow,
                contentDescription = stringResource(
                    if (isGranted) R.string.content_description_done_icon
                    else R.string.content_description_forward_arrow_icon
                )
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = LocalContentColor.current
        )
    }
}

@Preview
@Composable
private fun PermissionCardPreview() {
    QRAlarmTheme {
        PermissionCard(
            icon = QRAlarmIcons.Camera,
            iconContentDescription = "Camera",
            title = "Camera",
            subtitle = "Used for scanning.",
            isGranted = false,
            onClick = {},
            isClickable = true
        )
    }
}
