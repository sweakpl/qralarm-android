package com.sweak.qralarm.app.activity

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sweak.qralarm.core.navigation.NavigationState
import com.sweak.qralarm.core.navigation.Navigator
import com.sweak.qralarm.core.navigation.SharedViewModelStoreNavEntryDecorator
import com.sweak.qralarm.core.navigation.routes.AddEditAlarmRoute
import com.sweak.qralarm.core.navigation.routes.AdvancedAlarmSettingsRoute
import com.sweak.qralarm.core.navigation.routes.AlarmsChainSettingsRoute
import com.sweak.qralarm.core.navigation.routes.CustomCodeScannerRoute
import com.sweak.qralarm.core.navigation.routes.DisableAlarmScannerRoute
import com.sweak.qralarm.core.navigation.routes.EmergencyRoute
import com.sweak.qralarm.core.navigation.routes.EmergencySettingsRoute
import com.sweak.qralarm.core.navigation.routes.EmergencyTaskPreviewRoute
import com.sweak.qralarm.core.navigation.routes.HomeRoute
import com.sweak.qralarm.core.navigation.routes.IntroductionRoute
import com.sweak.qralarm.core.navigation.routes.MenuRoute
import com.sweak.qralarm.core.navigation.routes.OptimizationRoute
import com.sweak.qralarm.core.navigation.routes.QRAlarmProRoute
import com.sweak.qralarm.core.navigation.routes.RateRoute
import com.sweak.qralarm.core.navigation.routes.SpecialAlarmSettingsRoute
import com.sweak.qralarm.core.navigation.routes.ThemeRoute
import com.sweak.qralarm.core.navigation.toEntries
import com.sweak.qralarm.features.add_edit_alarm.AddEditAlarmViewModel
import com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit.AddEditAlarmScreen
import com.sweak.qralarm.features.add_edit_alarm.destinations.advanced.AdvancedAlarmSettingsScreen
import com.sweak.qralarm.features.add_edit_alarm.destinations.alarms_chain.AlarmsChainSettingsScreen
import com.sweak.qralarm.features.add_edit_alarm.destinations.special.SpecialAlarmSettingsScreen
import com.sweak.qralarm.features.custom_code_scanner.CustomCodeScannerScreen
import com.sweak.qralarm.features.disable_alarm_scanner.DisableAlarmScannerScreen
import com.sweak.qralarm.features.emergency.settings.EmergencySettingsScreen
import com.sweak.qralarm.features.emergency.task.EmergencyScreen
import com.sweak.qralarm.features.home.HomeScreen
import com.sweak.qralarm.features.introduction.IntroductionScreen
import com.sweak.qralarm.features.menu.MenuScreen
import com.sweak.qralarm.features.optimization.OptimizationScreen
import com.sweak.qralarm.features.qralarm_pro.QRAlarmProScreen
import com.sweak.qralarm.features.rate.RateScreen
import com.sweak.qralarm.features.theme.ThemeScreen

private const val ADD_EDIT_ALARM_CONTENT_KEY = "AddEditAlarm"

@Composable
fun MainNavContent(
    navigationState: NavigationState,
    navigator: Navigator,
    onAlarmSaved: () -> Unit
) {
    val mainEntryProvider = entryProvider {
        entry<AddEditAlarmRoute>(
            clazzContentKey = { ADD_EDIT_ALARM_CONTENT_KEY }
        ) { route ->
            val addEditAlarmViewModel =
                hiltViewModel<AddEditAlarmViewModel, AddEditAlarmViewModel.Factory> {
                    it.create(route.idOfAlarmToEdit)
                }

            AddEditAlarmScreen(
                addEditAlarmViewModel = addEditAlarmViewModel,
                onCancelClicked = { navigator.goBack() },
                onAlarmSaved = {
                    navigator.goBack()
                    onAlarmSaved()
                },
                onScanCustomCodeClicked = {
                    navigator.navigate(CustomCodeScannerRoute(shouldScanForDefaultCode = false))
                },
                onAdvancedSettingsClicked = {
                    navigator.navigate(AdvancedAlarmSettingsRoute(route.idOfAlarmToEdit))
                },
                onSpecialSettingsClicked = {
                    navigator.navigate(SpecialAlarmSettingsRoute(route.idOfAlarmToEdit))
                },
                onAlarmsChainSettingsClicked = {
                    navigator.navigate(AlarmsChainSettingsRoute(route.idOfAlarmToEdit))
                },
                onAlarmDeleted = { navigator.goBack() }
            )
        }

        entry<AdvancedAlarmSettingsRoute>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(ADD_EDIT_ALARM_CONTENT_KEY)
        ) { route ->
            val addEditAlarmViewModel =
                hiltViewModel<AddEditAlarmViewModel, AddEditAlarmViewModel.Factory> {
                    it.create(route.idOfAlarmToEdit)
                }

            AdvancedAlarmSettingsScreen(
                addEditAlarmViewModel = addEditAlarmViewModel,
                onCancelClicked = { navigator.goBack() }
            )
        }

        entry<AlarmsChainSettingsRoute>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(ADD_EDIT_ALARM_CONTENT_KEY)
        ) { route ->
            val addEditAlarmViewModel =
                hiltViewModel<AddEditAlarmViewModel, AddEditAlarmViewModel.Factory> {
                    it.create(route.idOfAlarmToEdit)
                }

            AlarmsChainSettingsScreen(
                addEditAlarmViewModel = addEditAlarmViewModel,
                onCancelClicked = { navigator.goBack() },
                onRedirectToQRAlarmPro = { navigator.navigate(QRAlarmProRoute) }
            )
        }

        entry<IntroductionRoute> { route ->
            IntroductionScreen(
                onContinueClicked = {
                    if (route.isLaunchedFromMenu) {
                        navigator.goBack()
                    } else {
                        navigator.navigateToTopLevelAndClear(HomeRoute)
                    }
                }
            )
        }

        entry<HomeRoute> {
            HomeScreen(
                onAddNewAlarm = { navigator.navigate(AddEditAlarmRoute(0L)) },
                onEditAlarm = { alarmId -> navigator.navigate(AddEditAlarmRoute(alarmId)) },
                onMenuClicked = { navigator.navigate(MenuRoute) },
                onRedirectToScanner = { alarmId ->
                    navigator.navigate(
                        DisableAlarmScannerRoute(
                            idOfAlarm = alarmId,
                            isDisablingBeforeAlarmFired = true
                        )
                    )
                },
                onRedirectToEmergency = { alarmId ->
                    navigator.navigate(EmergencyRoute(idOfAlarmToCancel = alarmId))
                },
                onGoToOptimizationClicked = {
                    navigator.navigate(OptimizationRoute(isLaunchedFromMenu = false))
                }
            )
        }

        entry<SpecialAlarmSettingsRoute> {
            SpecialAlarmSettingsScreen(
                onCancelClicked = { navigator.goBack() },
                onRedirectToQRAlarmPro = { navigator.navigate(QRAlarmProRoute) }
            )
        }

        entry<CustomCodeScannerRoute> { route ->
            CustomCodeScannerScreen(
                shouldScanForDefaultCode = route.shouldScanForDefaultCode,
                onCustomCodeSaved = { navigator.goBack() },
                onCloseClicked = { navigator.goBack() }
            )
        }

        entry<DisableAlarmScannerRoute> { route ->
            DisableAlarmScannerScreen(
                idOfAlarm = route.idOfAlarm,
                isDisablingBeforeAlarmFired = route.isDisablingBeforeAlarmFired,
                onAlarmDisabled = { _ -> navigator.goBack() },
                onCloseClicked = { navigator.goBack() }
            )
        }

        entry<MenuRoute> {
            MenuScreen(
                onBackClicked = { navigator.goBack() },
                onIntroductionClicked = {
                    navigator.navigate(IntroductionRoute(isLaunchedFromMenu = true))
                },
                onOptimizationGuideClicked = {
                    navigator.navigate(OptimizationRoute(isLaunchedFromMenu = true))
                },
                onEmergencyTaskSettingsClicked = { navigator.navigate(EmergencySettingsRoute) },
                onQRAlarmProClicked = { navigator.navigate(QRAlarmProRoute) },
                onRateQRAlarmClicked = { navigator.navigate(RateRoute) },
                onScanDefaultCodeClicked = {
                    navigator.navigate(CustomCodeScannerRoute(shouldScanForDefaultCode = true))
                },
                onThemeClicked = { navigator.navigate(ThemeRoute) }
            )
        }

        entry<OptimizationRoute> { route ->
            OptimizationScreen(
                isLaunchedFromMenu = route.isLaunchedFromMenu,
                onBackClicked = { navigator.goBack() }
            )
        }

        entry<ThemeRoute> {
            ThemeScreen(
                onBackClicked = { navigator.goBack() },
                onGoToQRAlarmProCheckout = { navigator.navigate(QRAlarmProRoute) }
            )
        }

        entry<EmergencySettingsRoute> {
            EmergencySettingsScreen(
                onBackClicked = { navigator.goBack() },
                onPreviewEmergencyTaskClicked = {
                    navigator.navigate(EmergencyTaskPreviewRoute)
                }
            )
        }

        entry<EmergencyTaskPreviewRoute> {
            EmergencyScreen(
                idOfAlarmToCancel = 0L,
                onCloseClicked = { navigator.goBack() },
                onEmergencyTaskCompleted = { navigator.goBack() }
            )
        }

        entry<QRAlarmProRoute> {
            QRAlarmProScreen(onNotNowClicked = { navigator.goBack() })
        }

        entry<RateRoute> {
            RateScreen(onExit = { navigator.goBack() })
        }

        entry<EmergencyRoute> { route ->
            EmergencyScreen(
                idOfAlarmToCancel = route.idOfAlarmToCancel,
                onCloseClicked = { navigator.goBack() },
                onEmergencyTaskCompleted = { navigator.goBack() }
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(
            entryProvider = { key -> mainEntryProvider(key) }
        ),
        onBack = { navigator.goBack() },
        transitionSpec = { fadeIn() togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith fadeOut() },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith fadeOut() }
    )
}
