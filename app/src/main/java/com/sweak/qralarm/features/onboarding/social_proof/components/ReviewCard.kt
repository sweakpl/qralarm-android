package com.sweak.qralarm.features.onboarding.social_proof.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space

@Composable
fun ReviewCard(
    @StringRes contentResourceId: Int,
    @StringRes authorResourceId: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val author = stringResource(authorResourceId)

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(MaterialTheme.space.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(MaterialTheme.space.mediumLarge)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.img_five_stars),
                    contentDescription =
                        stringResource(R.string.content_description_five_stars_image)
                )
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    progress = { 1f - progress },
                    color = LocalContentColor.current,
                    trackColor = LocalContentColor.current.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(MaterialTheme.space.mediumLarge)
                )
            }

            Text(
                text = stringResource(contentResourceId),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = MaterialTheme.space.medium)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.space.mediumLarge)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(MaterialTheme.space.large)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                ) {
                    Text(
                        text = author.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(modifier = Modifier.padding(start = MaterialTheme.space.smallMedium)) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.verified_user),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ReviewCardShortPreview() {
    QRAlarmTheme {
        ReviewCard(
            contentResourceId = R.string.review_5,
            authorResourceId = R.string.review_5_author,
            progress = 0.4f,
            modifier = Modifier.padding(MaterialTheme.space.medium)
        )
    }
}

@Preview
@Composable
private fun ReviewCardLongPreview() {
    QRAlarmTheme {
        ReviewCard(
            contentResourceId = R.string.review_2,
            authorResourceId = R.string.review_2_author,
            progress = 0.4f,
            modifier = Modifier.padding(MaterialTheme.space.medium)
        )
    }
}
