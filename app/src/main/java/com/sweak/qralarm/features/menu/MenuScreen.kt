package com.sweak.qralarm.features.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.menu.components.AssignDefaultCodeBottomSheet
import com.sweak.qralarm.features.menu.components.DefaultCodeEntry
import com.sweak.qralarm.features.menu.components.MenuEntry

@Composable
fun MenuScreen(
    onBackClicked: () -> Unit,
    onIntroductionClicked: () -> Unit,
    onOptimizationGuideClicked: () -> Unit,
    onQRAlarmProClicked: () -> Unit,
    onRateQRAlarmClicked: () -> Unit
) {
    val menuViewModel = hiltViewModel<MenuViewModel>()
    val menuScreenState by menuViewModel.state.collectAsStateWithLifecycle()

    MenuScreenContent(
        state = menuScreenState,
        onEvent = { event ->
            when (event) {
                is MenuScreenUserEvent.OnBackClicked -> onBackClicked()
                is MenuScreenUserEvent.OnIntroductionClicked -> onIntroductionClicked()
                is MenuScreenUserEvent.OnOptimizationGuideClicked -> onOptimizationGuideClicked()
                is MenuScreenUserEvent.OnQRAlarmProClicked -> onQRAlarmProClicked()
                is MenuScreenUserEvent.OnRateQRAlarmClicked -> onRateQRAlarmClicked()
                is MenuScreenUserEvent.TryScanSpecificDefaultCode -> {
                    // TODO
                }
                else -> menuViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenContent(
    state: MenuScreenState,
    onEvent: (MenuScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(MenuScreenUserEvent.OnBackClicked) }
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
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
                MenuEntry(
                    title = stringResource(R.string.introduction),
                    onClick = { onEvent(MenuScreenUserEvent.OnIntroductionClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.optimization_guide),
                    onClick = { onEvent(MenuScreenUserEvent.OnOptimizationGuideClicked) }
                )

                DefaultCodeEntry(
                    onClick = {
                        onEvent(
                            MenuScreenUserEvent.AssignDefaultCodeDialogVisible(isVisible = true)
                        )
                    },
                    assignedCode = state.defaultAlarmCode
                )

                MenuEntry(
                    title = stringResource(R.string.qralarm_pro),
                    onClick = { onEvent(MenuScreenUserEvent.OnQRAlarmProClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.rate_qralarm),
                    onClick = { onEvent(MenuScreenUserEvent.OnRateQRAlarmClicked) }
                )
            }
        }
    }

    if (state.isAssignDefaultCodeDialogVisible) {
        AssignDefaultCodeBottomSheet(
            onScanCodeClicked = {
                onEvent(MenuScreenUserEvent.TryScanSpecificDefaultCode)
            },
            availableCodes = state.previouslySavedCodes,
            onChooseCodeFromList = { chosenCode ->
                onEvent(MenuScreenUserEvent.DefaultCodeChosenFromList(code = chosenCode))
            },
            onDismissRequest = {
                onEvent(MenuScreenUserEvent.AssignDefaultCodeDialogVisible(isVisible = false))
            }
        )
    }
}

@Preview
@Composable
private fun MenuScreenContentPreview() {
    QRAlarmTheme {
        MenuScreenContent(
            state = MenuScreenState(),
            onEvent = {}
        )
    }
}