package com.sweak.qralarm.features.rate

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents

@Composable
fun RateScreen(
    onExit: () -> Unit
) {
    val rateViewModel = hiltViewModel<RateViewModel>()
    val rateScreenState by rateViewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        rateViewModel.onEvent(RateScreenUserEvent.NotNowClicked)
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    ObserveAsEvents(
        flow = rateViewModel.backendEvents,
        onEvent = { event ->
            when (event) {
                is RateScreenBackendEvent.RateMeClickProcessed -> {
                    try {
                        uriHandler.openUri(context.getString(R.string.qralarm_github_full_uri))
                        onExit()
                    } catch (exception: Exception) {
                        if (exception is ActivityNotFoundException ||
                            exception is IllegalArgumentException
                        ) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.issue_opening_the_page),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                is RateScreenBackendEvent.SomethingWrongClickProcessed -> {
                    try {
                        uriHandler.openUri(
                            context.getString(R.string.qralarm_github_issues_full_uri)
                        )
                        onExit()
                    } catch (exception: Exception) {
                        if (exception is ActivityNotFoundException ||
                            exception is IllegalArgumentException
                        ) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.issue_opening_the_page),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                is RateScreenBackendEvent.NotNowClickProcessed -> onExit()
            }
        }
    )

    RateScreenContent(
        state = rateScreenState,
        onEvent = rateViewModel::onEvent
    )
}

@Composable
fun RateScreenContent(
    state: RateScreenState,
    onEvent: (RateScreenUserEvent) -> Unit
) {
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.do_you_enjoy_qralarm),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.large,
                            top = MaterialTheme.space.xxLarge,
                            end = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.medium
                        )
                )

                Text(
                    text = stringResource(R.string.im_working_hard),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.large,
                            end = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.large
                        )
                )

                Image(
                    painter = painterResource(R.drawable.img_five_stars),
                    contentDescription = stringResource(
                        R.string.content_description_five_stars_image
                    ),
                    modifier = Modifier.height(MaterialTheme.space.large)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.space.mediumLarge))

                Button(
                    onClick = { onEvent(RateScreenUserEvent.RateMeClicked) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier
                        .height(height = MaterialTheme.space.xxLarge)
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.space.large)
                ) {
                    Text(
                        text = stringResource(R.string.i_love_it_rate_me),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = stringResource(R.string.did_something_go_wrong),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(
                            top = MaterialTheme.space.large,
                            start = MaterialTheme.space.large,
                            end = MaterialTheme.space.large,
                            bottom = MaterialTheme.space.medium
                        )
                )

                OutlinedButton(
                    onClick = { onEvent(RateScreenUserEvent.SomethingWrongClicked) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.large)
                ) {
                    Text(
                        text = stringResource(R.string.tell_me_what_is_wrong),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = MaterialTheme.space.xxLarge,
                        start = MaterialTheme.space.large,
                        end = MaterialTheme.space.large,
                        bottom = MaterialTheme.space.xSmall
                    )
                ) {
                    Checkbox(
                        checked = state.isNeverShowAgainChecked,
                        onCheckedChange = {
                            onEvent(
                                RateScreenUserEvent.IsNeverShowAgainCheckedChanged(checked = it)
                            )
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.tertiary
                        )
                    )

                    Text(
                        text = stringResource(R.string.do_not_show_again),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                OutlinedButton(
                    onClick = { onEvent(RateScreenUserEvent.NotNowClicked) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.space.large)
                ) {
                    Text(
                        text = stringResource(R.string.not_now),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.space.xxLarge))
            }
        }
    }
}

@Preview
@Composable
private fun RateScreenContentPreview() {
    QRAlarmTheme {
        RateScreenContent(
            state = RateScreenState(),
            onEvent = {}
        )
    }
}