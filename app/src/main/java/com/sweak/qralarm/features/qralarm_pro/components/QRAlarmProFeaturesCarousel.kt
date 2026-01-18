package com.sweak.qralarm.features.qralarm_pro.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.qralarm_pro.model.qrAlarmProCarouselFeatures
import kotlinx.coroutines.delay

@Composable
fun QRAlarmProFeaturesCarousel(modifier: Modifier = Modifier) {
    val endlessPagerMultiplier = 1000
    val featureItemsCount = qrAlarmProCarouselFeatures.size
    val pageCount = endlessPagerMultiplier * featureItemsCount
    val initialPage = pageCount / 2

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        initialPageOffsetFraction = 0f,
        pageCount = { pageCount },
    )

    LaunchedEffect(pagerState.settledPage) {
        val resolvedPageContentIndex = pagerState.currentPage % featureItemsCount
        val featureAnimationDuration =
            qrAlarmProCarouselFeatures[resolvedPageContentIndex].animationDurationMillis

        delay(featureAnimationDuration)
        val nextPage = (pagerState.currentPage + 1) % pageCount
        pagerState.animateScrollToPage(page = nextPage)
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) {
        val context = LocalContext.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val gifLoader = ImageLoader.Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()

            val resolvedPageContentIndex = it % featureItemsCount
            val carouselFeature = qrAlarmProCarouselFeatures[resolvedPageContentIndex]

            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(data = carouselFeature.animatedResourceId)
                        .build(),
                    imageLoader = gifLoader
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(size = MaterialTheme.space.run { xxxLarge + xxLarge })
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = MaterialTheme.space.medium)
            )

            Text(
                text = stringResource(carouselFeature.titleResourceId),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MaterialTheme.space.medium)
            )

            Text(
                text = stringResource(carouselFeature.descriptionResourceId),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                minLines = 3
            )
        }
    }
}

@Preview
@Composable
private fun QRAlarmProFeaturesCarouselPreview() {
    QRAlarmTheme {
        QRAlarmProFeaturesCarousel()
    }
}