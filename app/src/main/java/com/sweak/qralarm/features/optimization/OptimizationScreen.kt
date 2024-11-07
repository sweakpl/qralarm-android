package com.sweak.qralarm.features.optimization

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.OnResume

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
                    } catch (exception: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.browser_not_found),
                            Toast.LENGTH_LONG
                        ).show()
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
                            bottom = MaterialTheme.space.large
                        )
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Text(
                        text = stringResource(R.string.enable_background_work),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.xSmall
                            )
                    )

                    Text(
                        text = stringResource(R.string.enable_background_work_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.medium
                            )
                    )

                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = MaterialTheme.space.xSmall
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.large
                            )
                            .clickable(
                                enabled = !state.isIgnoringBatteryOptimizations,
                                onClick = {
                                    onEvent(OptimizationScreenUserEvent.EnableBackgroundWork)
                                }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
                        ) {
                            Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                            Icon(
                                imageVector = QRAlarmIcons.AutomaticSettings,
                                contentDescription = stringResource(
                                    R.string.content_description_automatic_settings_icon
                                ),
                                modifier = Modifier.size(size = MaterialTheme.space.xLarge)
                            )

                            Text(
                                text = stringResource(
                                    if (!state.isIgnoringBatteryOptimizations) {
                                        R.string.work_in_background_limited_click_to_enable
                                    } else R.string.work_in_background_enabled
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .padding(horizontal = MaterialTheme.space.medium)
                                    .weight(1f)
                            )

                            Icon(
                                imageVector =
                                if (!state.isIgnoringBatteryOptimizations) {
                                    QRAlarmIcons.ForwardArrow
                                } else QRAlarmIcons.Done,
                                contentDescription = stringResource(
                                    if (!state.isIgnoringBatteryOptimizations) {
                                        R.string.content_description_forward_arrow_icon
                                    } else R.string.content_description_done_icon
                                ),
                                modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.space.smallMedium))
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.fully_optimize_for_your_system),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.xSmall
                        )
                )

                Text(
                    text = stringResource(R.string.fully_optimize_for_your_system_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                )

                ElevatedCard(
                    onClick = { onEvent(OptimizationScreenUserEvent.BackgroundWorkWebsiteClicked) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = MaterialTheme.space.xSmall
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.large
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
                    ) {
                        Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                        Icon(
                            imageVector = QRAlarmIcons.AppSettings,
                            contentDescription = stringResource(
                                R.string.content_description_app_settings_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.xLarge)
                        )

                        Column(
                            modifier = Modifier
                                .padding(horizontal = MaterialTheme.space.medium)
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.dontkilmyapp_com),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color(0xFF009CFF)
                                ),
                                modifier = Modifier.padding(bottom = MaterialTheme.space.xSmall)
                            )

                            Text(
                                text = stringResource(
                                    R.string.select_manufacturer_and_follow_instructions
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Icon(
                            imageVector = QRAlarmIcons.ForwardArrow,
                            contentDescription = stringResource(
                                R.string.content_description_forward_arrow_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                        )

                        Spacer(modifier = Modifier.width(MaterialTheme.space.smallMedium))
                    }
                }

                Text(
                    text = stringResource(R.string.ensure_the_best_settings_possible),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.xSmall
                        )
                )

                Text(
                    text = stringResource(R.string.ensure_the_best_settings_possible_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.xSmall
                    )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.xSmall),
                    modifier = Modifier
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                ) {
                    Row {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.display_on_lock_screen))
                                }
                                append(" - ")
                                append(stringResource(R.string.display_on_lock_screen_description))
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = MaterialTheme.space.xSmall)
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.autostart))
                                }
                                append(" - ")
                                append(stringResource(R.string.autostart_description))
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                ElevatedCard(
                    onClick = { onEvent(OptimizationScreenUserEvent.ApplicationSettingsClicked) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = MaterialTheme.space.xSmall
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.large
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = MaterialTheme.space.medium)
                    ) {
                        Spacer(modifier = Modifier.width(MaterialTheme.space.medium))

                        Icon(
                            imageVector = QRAlarmIcons.SpecialAppSettings,
                            contentDescription = stringResource(
                                R.string.content_description_special_app_settings_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.xLarge)
                        )

                        Text(
                            text = stringResource(R.string.click_to_go_to_app_settings),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .padding(horizontal = MaterialTheme.space.medium)
                                .weight(1f)
                        )

                        Icon(
                            imageVector = QRAlarmIcons.ForwardArrow,
                            contentDescription = stringResource(
                                R.string.content_description_forward_arrow_icon
                            ),
                            modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                        )

                        Spacer(modifier = Modifier.width(MaterialTheme.space.smallMedium))
                    }
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