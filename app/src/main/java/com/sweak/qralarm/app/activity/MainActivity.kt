package com.sweak.qralarm.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sweak.qralarm.alarm.activity.AlarmActivity
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.navigation.routes.HomeRoute
import com.sweak.qralarm.core.navigation.routes.IntroductionRoute
import com.sweak.qralarm.core.navigation.Navigator
import com.sweak.qralarm.core.navigation.routes.RateRoute
import com.sweak.qralarm.core.navigation.rememberNavigationState
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { viewModel.state.value.shouldShowSplashScreen }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onEvent(MainActivityUserEvent.ObserveActiveAlarms)
            }
        }

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val isIntroductionFinished = state.isIntroductionFinished

            if (isIntroductionFinished != null) {
                val startRoute = remember(isIntroductionFinished) {
                    if (isIntroductionFinished) HomeRoute else IntroductionRoute(false)
                }
                val navigationState = rememberNavigationState(
                    startRoute = startRoute,
                    topLevelRoutes = setOf(HomeRoute, IntroductionRoute(false))
                )
                val navigator = remember(navigationState) { Navigator(navigationState) }

                ObserveAsEvents(
                    flow = viewModel.backendEvents,
                    onEvent = { event ->
                        when (event) {
                            is MainActivityBackendEvent.NavigateToActiveAlarm -> {
                                finish()
                                startActivity(
                                    Intent(applicationContext, AlarmActivity::class.java).apply {
                                        putExtra(AlarmActivity.EXTRA_ALARM_ID, event.alarmId)
                                        putExtra(
                                            AlarmActivity.EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY,
                                            true
                                        )
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                )
                            }

                            is MainActivityBackendEvent.ShowRatePrompt -> {
                                navigator.navigate(RateRoute)
                            }
                        }
                    }
                )

                QRAlarmTheme(theme = state.theme) {
                    MainNavContent(
                        navigationState = navigationState,
                        navigator = navigator,
                        onAlarmSaved = { viewModel.onEvent(MainActivityUserEvent.OnAlarmSaved) }
                    )
                }
            }
        }
    }
}
