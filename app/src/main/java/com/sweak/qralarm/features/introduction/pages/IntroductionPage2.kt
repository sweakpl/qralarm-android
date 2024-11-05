package com.sweak.qralarm.features.introduction.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun IntroductionPage2(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.get_up_catchphrase),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
        )

        Text(
            text = stringResource(R.string.get_up_catchphrase_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
        )

        Image(
            painter = painterResource(R.drawable.img_get_up_catchphrase),
            contentDescription = stringResource(R.string.content_description_get_up_catchphrase_image),
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.height(256.dp)
        )
    }
}

@Preview
@Composable
private fun IntroductionPage2Preview() {
    QRAlarmTheme {
        IntroductionPage2(modifier = Modifier.fillMaxWidth())
    }
}