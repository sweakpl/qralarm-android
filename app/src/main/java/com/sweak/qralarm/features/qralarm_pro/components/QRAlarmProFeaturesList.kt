package com.sweak.qralarm.features.qralarm_pro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun QRAlarmProFeaturesList(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.mediumLarge),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = QRAlarmIcons.DoNotLeaveAlarm,
                contentDescription = stringResource(
                    R.string.content_description_do_not_leave_alarm_icon
                ),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size = MaterialTheme.space.large)
            )

            Text(
                text = stringResource(R.string.do_not_leave_alarm),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = MaterialTheme.space.mediumLarge)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = QRAlarmIcons.PowerOffGuard,
                contentDescription =
                    stringResource(R.string.content_description_power_off_guard_icon),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size = MaterialTheme.space.large)
            )

            Text(
                text = stringResource(R.string.power_off_guard),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = MaterialTheme.space.mediumLarge)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = QRAlarmIcons.Sound,
                contentDescription = stringResource(
                    R.string.content_description_sound_icon
                ),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size = MaterialTheme.space.large)
            )

            Text(
                text = stringResource(R.string.block_volume_down),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = MaterialTheme.space.mediumLarge)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = QRAlarmIcons.AccessForever,
                contentDescription = stringResource(
                    R.string.content_description_access_forever_icon
                ),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size = MaterialTheme.space.large)
            )

            Text(
                text = stringResource(R.string.buy_once_enjoy_forever),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = MaterialTheme.space.mediumLarge)
            )
        }
    }
}

@Preview
@Composable
private fun PowerWiseProFeaturesListPreview() {
    QRAlarmTheme {
        QRAlarmProFeaturesList()
    }
}