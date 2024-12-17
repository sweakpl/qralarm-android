package com.sweak.qralarm.features.optimization

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.features.optimization.components.BackgroundWorkPage
import com.sweak.qralarm.features.optimization.components.BestSettingsPage
import com.sweak.qralarm.features.optimization.components.FullyOptimizePage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("BatteryLife")
@Composable
fun OptimizationScreen(onBackClicked: () -> Unit) {
    val optimizationViewModel = hiltViewModel<OptimizationViewModel>()
    val optimizationScreenState by optimizationViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    OnResume { optimizationViewModel.refresh() }

    OptimizationScreenContent(
        state = optimizationScreenState,
        onEvent = { event ->
            when (event) {
                is OptimizationScreenUserEvent.OnBackClicked -> onBackClicked()
                is OptimizationScreenUserEvent.EnableBackgroundWork -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        } catch (exception: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.setting_unavailable_refer_to_the_next_step
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                is OptimizationScreenUserEvent.BackgroundWorkWebsiteClicked -> {
                    try {
                        uriHandler.openUri(context.getString(R.string.dontkilmyapp_com_full_uri))
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

                is OptimizationScreenUserEvent.ApplicationSettingsClicked -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizationScreenContent(
    state: OptimizationScreenState,
    onEvent: (OptimizationScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.optimize_qralarm),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(OptimizationScreenUserEvent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                            stringResource(R.string.content_description_back_arrow_icon)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Text(
                text = stringResource(R.string.ensure_the_best_performance),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.small
                    )
            )

            Text(
                text = stringResource(R.string.optimization_screen_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.mediumLarge
                    )
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
            )

            val pagerState = rememberPagerState(pageCount = { 3 })
            val composableScope = rememberCoroutineScope()

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) { page ->
                Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    when (page) {
                        0 -> BackgroundWorkPage(
                            isIgnoringBatteryOptimizations = state.isIgnoringBatteryOptimizations,
                            onClick = {
                                onEvent(OptimizationScreenUserEvent.EnableBackgroundWork)
                            },
                            modifier = Modifier.padding(all = MaterialTheme.space.medium)
                        )
                        1 -> BestSettingsPage(
                            onClick = {
                                onEvent(OptimizationScreenUserEvent.ApplicationSettingsClicked)
                            },
                            modifier = Modifier.padding(all = MaterialTheme.space.medium)
                        )
                        2 -> FullyOptimizePage(
                            onClick = {
                                onEvent(OptimizationScreenUserEvent.BackgroundWorkWebsiteClicked)
                            },
                            modifier = Modifier.padding(all = MaterialTheme.space.medium)
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
            )

            Row(
                modifier = Modifier.padding(all = MaterialTheme.space.medium)
            ) {
                var buttonsDisabledTimeout by remember { mutableIntStateOf(0) }

                LaunchedEffect(true) {
                    if (state.shouldDelayInstructionsTransitions) {
                        buttonsDisabledTimeout = 5

                        repeat(5) {
                            delay(1000)
                            buttonsDisabledTimeout -= 1
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (pagerState.currentPage != 0) {
                            composableScope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    enabled = pagerState.currentPage != 0 && buttonsDisabledTimeout <= 0,
                    modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                ) {
                    Text(text = stringResource(R.string.previous))
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == 2) {
                            onEvent(OptimizationScreenUserEvent.OnBackClicked)
                        } else {
                            composableScope.launch {
                                if (state.shouldDelayInstructionsTransitions) {
                                    buttonsDisabledTimeout = 5
                                }

                                pagerState.animateScrollToPage(page = pagerState.currentPage + 1)

                                if (state.shouldDelayInstructionsTransitions) {
                                    repeat(5) {
                                        delay(1000)
                                        buttonsDisabledTimeout -= 1
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    enabled = buttonsDisabledTimeout <= 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(
                            if (pagerState.currentPage == 2) R.string.lets_go
                            else R.string.next_step
                        ) + if (buttonsDisabledTimeout != 0) " ($buttonsDisabledTimeout)" else ""
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun OptimizationScreenContentPreview() {
    QRAlarmTheme {
        OptimizationScreenContent(
            state = OptimizationScreenState(),
            onEvent = {}
        )
    }
}