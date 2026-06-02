package com.sweak.qralarm.features.menu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.features.menu.components.MenuEntry

@Composable
fun MenuScreen(
    onBackClicked: () -> Unit,
    onOptimizationGuideClicked: () -> Unit,
    onEmergencyTaskSettingsClicked: () -> Unit,
    onQRAlarmProClicked: () -> Unit,
    onCodesManagementClicked: () -> Unit,
    onThemeClicked: () -> Unit
) {
    val menuViewModel = hiltViewModel<MenuViewModel>()
    val menuScreenState by menuViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    MenuScreenContent(
        state = menuScreenState,
        onEvent = { event ->
            when (event) {
                is MenuScreenUserEvent.OnBackClicked -> onBackClicked()
                is MenuScreenUserEvent.OnOptimizationGuideClicked -> onOptimizationGuideClicked()
                is MenuScreenUserEvent.OnEmergencyTaskSettingsClicked ->
                    onEmergencyTaskSettingsClicked()
                is MenuScreenUserEvent.OnQRAlarmProClicked -> onQRAlarmProClicked()
                is MenuScreenUserEvent.OnRateQRAlarmClicked -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                context.getString(R.string.qralarm_github_full_uri).toUri()
                            )
                        )
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.issue_opening_the_page),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is MenuScreenUserEvent.OnContactSupportClicked -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                context.getString(R.string.qralarm_github_issues_full_uri).toUri()
                            )
                        )
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.issue_opening_the_page),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is MenuScreenUserEvent.OnCodesManagementClicked -> onCodesManagementClicked()
                is MenuScreenUserEvent.OnThemeClicked -> onThemeClicked()
                is MenuScreenUserEvent.GoToApplicationSettingsClicked -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }
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
                        style = MaterialTheme.typography.titleLarge
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
                }
            )
        },
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
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .fillMaxWidth()
            ) {
                MenuEntry(
                    title = stringResource(R.string.optimization_guide),
                    onClick = { onEvent(MenuScreenUserEvent.OnOptimizationGuideClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.codes_management),
                    onClick = { onEvent(MenuScreenUserEvent.OnCodesManagementClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.emergency_task),
                    onClick = { onEvent(MenuScreenUserEvent.OnEmergencyTaskSettingsClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.qralarm_pro),
                    onClick = { onEvent(MenuScreenUserEvent.OnQRAlarmProClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.rate_qralarm),
                    onClick = { onEvent(MenuScreenUserEvent.OnRateQRAlarmClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.tell_me_what_is_wrong),
                    onClick = { onEvent(MenuScreenUserEvent.OnContactSupportClicked) }
                )

                MenuEntry(
                    title = stringResource(R.string.theme),
                    onClick = { onEvent(MenuScreenUserEvent.OnThemeClicked) }
                )
            }
        }
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
