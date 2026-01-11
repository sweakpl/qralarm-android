package com.sweak.qralarm.features.qralarm_pro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.qralarm_pro.components.ProductPlanCard
import com.sweak.qralarm.features.qralarm_pro.components.QRAlarmProFeaturesCarousel
import com.sweak.qralarm.features.qralarm_pro.components.QRAlarmProFeaturesRows
import com.sweak.qralarm.features.qralarm_pro.model.QRAlarmProDistributionSource

@Composable
fun QRAlarmProScreen(
    onNotNowClicked: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    QRAlarmProScreenContent(
        onEvent = { event ->
            when (event) {
                is QRAlarmProScreenUserEvent.GetQRAlarmProClicked -> {
                    if (event.qrAlarmProDistributionSource is QRAlarmProDistributionSource.ItchIo) {
                        val qralarmProItchIoUrl =
                            resources.getString(R.string.qralarm_pro_itch_io_url)

                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    qralarmProItchIoUrl.toUri()
                                )
                            )
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                resources.getString(R.string.issue_opening_the_page),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (event.qrAlarmProDistributionSource is QRAlarmProDistributionSource.GooglePlay) {
                        val qralarmProPackageName =
                            resources.getString(R.string.qralarm_pro_package_name)

                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "market://details?id=$qralarmProPackageName".toUri()
                                )
                            )
                        } catch (_: ActivityNotFoundException) {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://play.google.com/store/apps/details?id=$qralarmProPackageName".toUri()
                                    )
                                )
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.issue_opening_the_page),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                is QRAlarmProScreenUserEvent.NotNowClicked -> onNotNowClicked()
            }
        }
    )
}

@Composable
fun QRAlarmProScreenContent(onEvent: (QRAlarmProScreenUserEvent) -> Unit) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Spacer(modifier = Modifier.height(height = MaterialTheme.space.large))

                Image(
                    painter = painterResource(R.drawable.img_qralarm_pro),
                    contentDescription = stringResource(
                        R.string.content_description_qralarm_pro_image
                    ),
                    modifier = Modifier
                        .size(size = MaterialTheme.space.run { xxLarge + large })
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = stringResource(R.string.qralarm_pro),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            start = MaterialTheme.space.large,
                            top = MaterialTheme.space.medium,
                            end = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.large
                        )
                )

                QRAlarmProFeaturesCarousel(
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.medium
                    )
                )

                QRAlarmProFeaturesRows(
                    modifier = Modifier.padding(bottom = MaterialTheme.space.xLarge)
                )

                var selectedQRAlarmProDistributionSource by remember {
                    mutableStateOf<QRAlarmProDistributionSource>(QRAlarmProDistributionSource.GooglePlay)
                }

                ProductPlanCard(
                    title = stringResource(R.string.standalone_app),
                    price = {
                        Text(
                            text = stringResource(R.string.google_play),
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.basicMarquee()
                        )
                    },
                    selected = selectedQRAlarmProDistributionSource is QRAlarmProDistributionSource.GooglePlay,
                    enabled = true,
                    onClick = { selectedQRAlarmProDistributionSource = QRAlarmProDistributionSource.GooglePlay },
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                )

                ProductPlanCard(
                    title = stringResource(R.string.standalone_app),
                    price = {
                        Text(
                            text = stringResource(R.string.itch_io),
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.basicMarquee()
                        )
                    },
                    selected = selectedQRAlarmProDistributionSource is QRAlarmProDistributionSource.ItchIo,
                    enabled = true,
                    onClick = { selectedQRAlarmProDistributionSource = QRAlarmProDistributionSource.ItchIo },
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.xLarge
                        )
                )

                Button(
                    onClick = {
                        onEvent(
                            QRAlarmProScreenUserEvent.GetQRAlarmProClicked(
                                qrAlarmProDistributionSource = selectedQRAlarmProDistributionSource
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier
                        .height(height = MaterialTheme.space.xxLarge)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.space.large)
                ) {
                    Text(
                        text = stringResource(R.string.get_it_now),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                TextButton(
                    onClick = { onEvent(QRAlarmProScreenUserEvent.NotNowClicked) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            top = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.xxLarge
                        )
                ) {
                    Text(
                        text = stringResource(R.string.not_now),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun QRAlarmProScreenContentPreview() {
    QRAlarmTheme {
        QRAlarmProScreenContent(onEvent = {})
    }
}