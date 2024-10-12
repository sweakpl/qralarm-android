package com.sweak.qralarm.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.home.components.model.AlarmWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmsRepository: AlarmsRepository
): ViewModel() {

    var state = MutableStateFlow(HomeScreenState())

    private var isInitializing = true

    init {
        viewModelScope.launch {
            alarmsRepository.getAllAlarms().collect { allAlarms ->
                if (!isInitializing) {
                    // Delay added for the alarms list animation to be visible as the Flow update
                    // Comes while the HomeScreen is still hidden behind e.g. AddEditAlarmScreen:
                    delay(500)
                }

                state.update { currentState ->
                    currentState.copy(
                        alarmWrappers = allAlarms.mapNotNull { alarm ->
                            val alarmRepeatingScheduleWrapper =
                                convertAlarmRepeatingMode(alarm.repeatingMode)
                                    ?: return@mapNotNull null

                            AlarmWrapper(
                                alarmId = alarm.alarmId,
                                alarmHourOfDay = alarm.alarmHourOfDay,
                                alarmMinute = alarm.alarmMinute,
                                alarmRepeatingScheduleWrapper = alarmRepeatingScheduleWrapper,
                                isAlarmEnabled = alarm.isAlarmEnabled,
                                isQRCOdeEnabled = alarm.isUsingCode
                            )
                        }.sortedWith(
                            compareBy(AlarmWrapper::alarmHourOfDay, AlarmWrapper::alarmMinute)
                        )
                    )
                }

                isInitializing = false
            }
        }
    }

    fun onEvent(event: HomeScreenUserEvent) {
        when (event) {
            is HomeScreenUserEvent.AlarmEnabledChanged -> viewModelScope.launch {
                alarmsRepository.setAlarmEnabled(
                    alarmId = event.alarmId,
                    enabled = event.enabled
                )
            }
            else -> { /* no-op */ }
        }
    }

    private fun convertAlarmRepeatingMode(
        repeatingMode: Alarm.RepeatingMode
    ): AlarmRepeatingScheduleWrapper? {
        if (repeatingMode is Alarm.RepeatingMode.Once) {
            return AlarmRepeatingScheduleWrapper(
                alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.ONLY_ONCE
            )
        } else if (repeatingMode is Alarm.RepeatingMode.Days) {
            val days = repeatingMode.repeatingDaysOfWeek

            if (days.size == 2 && days.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))) {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.SAT_SUN
                )
            } else if (days.size == 5 &&
                days.containsAll(
                    listOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )
                )
            ) {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.MON_FRI
                )
            } else {
                return AlarmRepeatingScheduleWrapper(
                    alarmRepeatingMode = AlarmRepeatingScheduleWrapper.AlarmRepeatingMode.CUSTOM,
                    alarmDaysOfWeek = days
                )
            }
        }

        return null
    }
}