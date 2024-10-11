package com.sweak.qralarm.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.ui.compose_util.OnResume
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.home.components.AlarmCard
import com.sweak.qralarm.features.home.components.model.AlarmWrapper

@Composable
fun HomeScreen(
    onAddNewAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit
) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val homeScreenState by homeViewModel.state.collectAsStateWithLifecycle()

    OnResume { homeViewModel.refresh() }

    HomeScreenContent(
        state = homeScreenState,
        onEvent = { event ->
            when (event) {
                is HomeScreenUserEvent.AddNewAlarm -> onAddNewAlarm()
                is HomeScreenUserEvent.EditAlarm -> onEditAlarm(event.alarmId)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    onEvent: (HomeScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* TODO */ }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.Menu,
                            contentDescription =
                            stringResource(R.string.content_description_menu_icon)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = QRAlarmIcons.Add,
                    contentDescription = stringResource(R.string.content_description_add_icon),
                    modifier = Modifier.size(size = MaterialTheme.space.large)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            LazyColumn(modifier = Modifier.padding(paddingValues = paddingValues)) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                top = MaterialTheme.space.medium,
                                end = MaterialTheme.space.small,
                                bottom = MaterialTheme.space.mediumLarge
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.alarms),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        IconButton(
                            onClick = { onEvent(HomeScreenUserEvent.AddNewAlarm) }
                        ) {
                            Icon(
                                imageVector = QRAlarmIcons.Add,
                                contentDescription = stringResource(R.string.content_description_add_icon),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(size = MaterialTheme.space.large)
                            )
                        }
                    }
                }

                items(state.alarmWrappers) {
                    AlarmCard(
                        alarmWrapper = it,
                        onClick = { alarmId ->
                            onEvent(HomeScreenUserEvent.EditAlarm(alarmId = alarmId))
                        },
                        onAlarmEnabledChanged = { alarmId, enabled ->
                            // TODO
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.space.medium,
                                end = MaterialTheme.space.medium,
                                bottom = MaterialTheme.space.medium
                            )
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(
                            MaterialTheme.space.run { xxLarge + small }
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    QRAlarmTheme {
        HomeScreenContent(
            state = HomeScreenState(
                alarmWrappers = listOf(
                    AlarmWrapper(
                        alarmId = 0,
                        alarmHourOfDay = 8,
                        alarmMinute = 0,
                        alarmRepeatingScheduleWrapper = AlarmRepeatingScheduleWrapper(),
                        isAlarmEnabled = true,
                        isQRCOdeEnabled = false
                    )
                )
            ),
            onEvent = {}
        )
    }
}