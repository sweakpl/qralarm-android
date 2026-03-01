package com.sweak.qralarm.features.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.collections.emptyList

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QRAlarmWidgetEntryPoint {
    fun alarmsRepository(): AlarmsRepository
}

class QRAlarmWidgetUpdater {

    @Inject
    @ApplicationContext
    lateinit var appContext: Context

    val entryPoint = EntryPointAccessors.fromApplication(
        appContext.applicationContext,
        QRAlarmWidgetEntryPoint::class.java
    )

    // fetch alarmsRepository using Hilt
    val alarmsRepository = entryPoint.alarmsRepository()


    // TODO: debounce

    // TODO: calculate

    // TODO: broadcast

    // returns nextAlarm based on retrieved list from repository
    suspend fun getNextAlarm(alarmsRepository: AlarmsRepository): Alarm? {
        val alarmsList = alarmsRepository.getAllAlarms().first()

        val nextAlarm: Alarm? = alarmsList
            .filter { it.isAlarmEnabled }
            .minByOrNull { it.nextAlarmTimeInMillis }

        return nextAlarm
    }
}