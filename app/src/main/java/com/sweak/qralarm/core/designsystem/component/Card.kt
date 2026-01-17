package com.sweak.qralarm.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Deprecated("Use Material3 Card directly instead", replaceWith = ReplaceWith("Card"))
@Composable
fun QRAlarmCard(
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        content = content
    )
}