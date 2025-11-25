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
import androidx.compose.ui.graphics.vector.ImageVector
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
        QRAlarmProFeature(
            title = stringResource(R.string.do_not_leave_alarm),
            imageVector = QRAlarmIcons.DoNotLeaveAlarm,
            contentDescription = stringResource(R.string.content_description_do_not_leave_alarm_icon)
        )

        QRAlarmProFeature(
            title = stringResource(R.string.power_off_guard),
            imageVector = QRAlarmIcons.PowerOffGuard,
            contentDescription = stringResource(R.string.content_description_power_off_guard_icon)
        )

        QRAlarmProFeature(
            title = stringResource(R.string.block_volume_down),
            imageVector = QRAlarmIcons.Sound,
            contentDescription = stringResource(R.string.content_description_sound_icon)
        )

        QRAlarmProFeature(
            title = stringResource(R.string.alarms_chain),
            imageVector = QRAlarmIcons.Chain,
            contentDescription = stringResource(R.string.content_description_chain_icon)
        )

        QRAlarmProFeature(
            title = stringResource(R.string.no_ads_experience),
            imageVector = QRAlarmIcons.NoAds,
            contentDescription = stringResource(R.string.content_description_no_ads_icon)
        )
    }
}

@Composable
private fun QRAlarmProFeature(
    title: String,
    imageVector: ImageVector,
    contentDescription: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(size = MaterialTheme.space.large)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = MaterialTheme.space.mediumLarge)
        )
    }
}

@Preview
@Composable
private fun PowerWiseProFeaturesListPreview() {
    QRAlarmTheme {
        QRAlarmProFeaturesList()
    }
}