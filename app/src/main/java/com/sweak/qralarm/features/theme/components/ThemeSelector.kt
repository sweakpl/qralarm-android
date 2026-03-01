package com.sweak.qralarm.features.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.theme.model.ThemeUi
import com.sweak.qralarm.features.theme.util.THEME_1

@Composable
fun ThemeSelector(
    theme: ThemeUi.Custom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        modifier = modifier.size(MaterialTheme.space.xxLarge)
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .padding(MaterialTheme.space.xSmall)
                        .clip(CircleShape)
                        .background(Color(theme.primaryColor.toColorInt()))
                        .fillMaxSize()
                )
            }
        }
    }
}

@Preview
@Composable
private fun ThemeSelectorPreview() {
    QRAlarmTheme {
        ThemeSelector(
            theme = THEME_1,
            onClick = {},
        )
    }
}
