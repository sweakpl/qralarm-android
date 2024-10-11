package com.sweak.qralarm.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.ui.model.AlarmRepeatingScheduleWrapper
import com.sweak.qralarm.features.home.components.model.AlarmWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        viewModelScope.launch { refreshInternal() }
    }

    fun refresh() = viewModelScope.launch { refreshInternal() }

    private suspend fun refreshInternal() {
        val allAlarms = alarmsRepository.getAllAlarms()

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
                }
            )
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