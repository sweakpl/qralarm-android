package com.sweak.qralarm.features.menu.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmSwitch
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.user.model.Theme

@Composable
fun ThemeToggleEntry(
    theme: Theme,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onThemeToggle() }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = MaterialTheme.space.medium,
                    horizontal = MaterialTheme.space.medium
                )
        ) {
            Text(
                text = stringResource(R.string.dynamic_theme),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            QRAlarmSwitch(
                checked = theme is Theme.Dynamic,
                onCheckedChange = { onThemeToggle() }
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = LocalContentColor.current,
            modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
        )
    }
}

@Preview
@Composable
private fun ThemeToggleEntryPreview() {
    QRAlarmTheme {
        ThemeToggleEntry(
            theme = Theme.Default,
            onThemeToggle = {}
        )
    }
}
