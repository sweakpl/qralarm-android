package com.sweak.qralarm.features.optimization.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun BackgroundWorkPage(
    isIgnoringBatteryOptimizations: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.enable_background_work),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
        )

        Text(
            text = stringResource(R.string.enable_background_work_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
        )

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = MaterialTheme.space.xSmall
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.space.medium)
                .clickable(
                    enabled = !isIgnoringBatteryOptimizations,
                    onClick = onClick
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
            ) {
                Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                Icon(
                    imageVector = QRAlarmIcons.AutomaticSettings,
                    contentDescription = stringResource(
                        R.string.content_description_automatic_settings_icon
                    ),
                    modifier = Modifier.size(size = MaterialTheme.space.xLarge)
                )

                Text(
                    text = stringResource(
                        if (!isIgnoringBatteryOptimizations) {
                            R.string.work_in_background_limited_click_to_enable
                        } else R.string.work_in_background_enabled
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.space.medium)
                        .weight(1f)
                )

                Icon(
                    imageVector =
                    if (!isIgnoringBatteryOptimizations) {
                        QRAlarmIcons.ForwardArrow
                    } else QRAlarmIcons.Done,
                    contentDescription = stringResource(
                        if (!isIgnoringBatteryOptimizations) {
                            R.string.content_description_forward_arrow_icon
                        } else R.string.content_description_done_icon
                    ),
                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.space.smallMedium))
            }
        }
    }
}