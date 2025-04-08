package com.sweak.qralarm.features.disable_alarm_scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun Toast(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(insets = WindowInsets.navigationBars)
            .padding(
                start = MaterialTheme.space.xxLarge,
                end = MaterialTheme.space.xxLarge,
                bottom = MaterialTheme.space.large
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(MaterialTheme.space.small),
                )
                .padding(
                    horizontal = MaterialTheme.space.medium,
                    vertical = MaterialTheme.space.smallMedium,
                )
        )
    }
}