package com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun AddNewChainedAlarmCard(
    onAddNewChainedAlarmClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val borderColor = LocalContentColor.current

        Image(
            painter = painterResource(R.drawable.ic_arrow_down_dashed),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(color = borderColor),
            modifier = Modifier.height(MaterialTheme.space.xLarge)
        )

        val borderWidth = MaterialTheme.space.xSmall
        val cornerRadius = MaterialTheme.space.smallMedium

        OutlinedCard(
            modifier = Modifier
                .height(MaterialTheme.space.xxLarge)
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.space.xSmall,
                    end = MaterialTheme.space.xSmall,
                    bottom = MaterialTheme.space.xSmall
                )
                .drawBehind {
                    val borderWidthPx = borderWidth.toPx()
                    val stroke = Stroke(
                        width = borderWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(borderWidthPx * 2, borderWidthPx * 2),
                            0f
                        )
                    )
                    val corner = cornerRadius.toPx()
                    drawRoundRect(
                        color = borderColor,
                        size = size,
                        style = stroke,
                        cornerRadius = CornerRadius(corner)
                    )
                }
                .clickable { onAddNewChainedAlarmClicked() },
            border = BorderStroke(0.dp, Transparent),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Transparent
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.add_next_alarm_in_chain),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.space.small)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddNewChainedAlarmCardPreview() {
    QRAlarmTheme {
        AddNewChainedAlarmCard(
            onAddNewChainedAlarmClicked = {},
            modifier = Modifier.width(400.dp)
        )
    }
}