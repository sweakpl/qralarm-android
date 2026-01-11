package com.sweak.qralarm.features.qralarm_pro.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun ProductPlanCard(
    title: String,
    price: @Composable () -> Unit,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondary
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = MaterialTheme.space.medium)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.space.medium)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = MaterialTheme.space.small)
                )

                price()
            }

            Icon(
                imageVector =
                    if (selected) QRAlarmIcons.CheckedCircle else QRAlarmIcons.UncheckedCircle,
                contentDescription = stringResource(
                    if (selected) R.string.content_description_checked_circle_icon
                    else R.string.content_description_unchecked_circle_icon
                )
            )
        }
    }
}

@Preview
@Composable
private fun ProductPlanCardPreview() {
    QRAlarmTheme {
        ProductPlanCard(
            title = "Standalone app",
            price = {
                Text(
                    text = "itch.io",
                    style = MaterialTheme.typography.displaySmall
                )
            },
            selected = false,
            enabled = true,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}