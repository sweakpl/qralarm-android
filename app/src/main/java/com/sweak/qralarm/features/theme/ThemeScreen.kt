package com.sweak.qralarm.features.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.components.NavigationButton
import com.sweak.qralarm.core.ui.components.ToggleSetting
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import com.sweak.qralarm.features.theme.components.ThemeSelector
import com.sweak.qralarm.features.theme.model.ThemeUi
import com.sweak.qralarm.features.theme.util.AVAILABLE_THEMES

@Composable
fun ThemeScreen(
    onBackClicked: () -> Unit,
    onGoToQRAlarmProCheckout: () -> Unit
) {
    val viewModel = hiltViewModel<ThemeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(flow = viewModel.backendEvents) { event ->
        when (event) {
            is ThemeScreenBackendEvent.RedirectToQRAlarmPro -> onGoToQRAlarmProCheckout()
        }
    }

    ThemeScreenContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is ThemeScreenUserEvent.OnBackClicked -> onBackClicked()
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreenContent(
    state: ThemeScreenState,
    onEvent: (ThemeScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.theme),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(ThemeScreenUserEvent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                                stringResource(R.string.content_description_back_arrow_icon)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (MaterialTheme.isQRAlarmTheme)
                        Modifier.background(
                            brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac))
                        )
                    else Modifier
                )
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(paddingValues)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.mediumLarge,
                                end = MaterialTheme.space.medium
                            )
                    ) {
                        ToggleSetting(
                            isChecked = state.theme is ThemeUi.Dynamic,
                            onCheckedChange = {
                                onEvent(ThemeScreenUserEvent.OnDynamicThemeToggled(it))
                            },
                            title = stringResource(R.string.dynamic_theme),
                            compactHeight = true
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.mediumLarge,
                            end = MaterialTheme.space.medium,
                            bottom = MaterialTheme.space.medium
                        )
                ) {
                    ToggleSetting(
                        isChecked = state.theme is ThemeUi.Custom,
                        onCheckedChange = {
                            onEvent(ThemeScreenUserEvent.OnCustomThemeToggled(isChecked = it))
                        },
                        title = stringResource(R.string.custom_theme),
                        compactHeight = true
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = LocalContentColor.current,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.medium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.medium),
                        maxItemsInEachRow = 4,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(
                                horizontal = MaterialTheme.space.medium,
                                vertical = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        state.availableCustomThemes.forEach { theme ->
                            ThemeSelector(
                                theme = theme,
                                onClick = {
                                    onEvent(ThemeScreenUserEvent.OnCustomThemeSelected)
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = LocalContentColor.current,
                        modifier = Modifier.padding(horizontal = MaterialTheme.space.medium)
                    )

                    NavigationButton(
                        text = stringResource(R.string.custom_color),
                        onClick = { onEvent(ThemeScreenUserEvent.OnColorPickerOpened) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.space.medium,
                                vertical = MaterialTheme.space.mediumLarge
                            )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThemeScreenContentPreview() {
    QRAlarmTheme {
        ThemeScreenContent(
            state = ThemeScreenState(
                availableCustomThemes = AVAILABLE_THEMES
            ),
            onEvent = {}
        )
    }
}