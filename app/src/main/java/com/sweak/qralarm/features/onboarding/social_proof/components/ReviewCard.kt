package com.sweak.qralarm.features.onboarding.social_proof.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun ReviewCard(
    @StringRes contentResourceId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.img_five_stars),
            contentDescription = stringResource(R.string.content_description_five_stars_image),
            modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
        )

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "\"",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(end = MaterialTheme.space.xSmall)
            )
            Text(
                text = stringResource(contentResourceId),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "\"",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(start = MaterialTheme.space.xSmall)
            )
        }
    }
}

@Preview
@Composable
private fun ReviewCardPreview() {
    QRAlarmTheme {
        ReviewCard(
            contentResourceId = R.string.review_1,
            modifier = Modifier.padding(MaterialTheme.space.medium)
        )
    }
}
