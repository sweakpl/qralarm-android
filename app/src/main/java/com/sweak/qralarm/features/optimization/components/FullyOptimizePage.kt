package com.sweak.qralarm.features.optimization.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun FullyOptimizePage(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.fully_optimize_for_your_system),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.medium,
                    end = MaterialTheme.space.medium,
                    bottom = MaterialTheme.space.xSmall
                )
        )

        Text(
            text = stringResource(R.string.fully_optimize_for_your_system_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(
                    start = MaterialTheme.space.medium,
                    end = MaterialTheme.space.medium,
                    bottom = MaterialTheme.space.medium
                )
        )

        ElevatedCard(
            onClick = onClick,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = MaterialTheme.space.xSmall
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.space.medium,
                    end = MaterialTheme.space.medium,
                    bottom = MaterialTheme.space.large
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
            ) {
                Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                Icon(
                    imageVector = QRAlarmIcons.AppSettings,
                    contentDescription = stringResource(
                        R.string.content_description_app_settings_icon
                    ),
                    modifier = Modifier.size(size = MaterialTheme.space.xLarge)
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.space.medium)
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.dontkilmyapp_com),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF009CFF)
                        ),
                        modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                    )

                    Text(
                        text = stringResource(
                            R.string.select_manufacturer_and_follow_instructions
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Icon(
                    imageVector = QRAlarmIcons.ForwardArrow,
                    contentDescription = stringResource(
                        R.string.content_description_forward_arrow_icon
                    ),
                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.space.smallMedium))
            }
        }
    }
}
