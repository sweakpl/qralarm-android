package com.sweak.qralarm.alarm.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.HandleEarlyStoppedAlarm
import com.sweak.qralarm.core.domain.alarm.StartAlarmRinging
import com.sweak.qralarm.core.domain.user.UserDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class AlarmService : Service() {

    // Everything in the service runs on the main thread, so its state needs no synchronisation
    // and a block without suspension points is atomic:
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Inject
    lateinit var alarmsRepository: AlarmsRepository
    @Inject
    lateinit var qrAlarmManager: QRAlarmManager
    @Inject
    lateinit var handleEarlyStoppedAlarm: HandleEarlyStoppedAlarm
    @Inject
    lateinit var startAlarmRinging: StartAlarmRinging
    @Inject
    lateinit var userDataRepository: UserDataRepository
    @Inject
    lateinit var alarmDeliveryValidator: AlarmDeliveryValidator
    @Inject
    lateinit var alarmNotificationFactory: AlarmNotificationFactory
    @Inject
    lateinit var ringingSessionFactory: RingingSession.Factory

    private var ringingSession: RingingSession? = null
    private var foregroundAlarmId: Long? = null
    private var pendingDeliveryCount = 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID)
        var shouldStopService = alarmId == null

        // A delivery must not replace the notification of an alarm that is already ringing until
        // it is known to ring itself, so the foreground requirement is met with the ringing one's:
        val foregroundNotificationAlarmId = ringingSession?.alarm?.alarmId ?: alarmId ?: 0

        if (!enterForeground(alarmId = foregroundNotificationAlarmId)) shouldStopService = true

        pendingDeliveryCount++

        val isSnoozeAlarm = intent.extras?.getBoolean(EXTRA_IS_SNOOZE_ALARM)

        serviceScope.launch {
            handleDelivery(
                alarmId = alarmId ?: 0,
                isSnoozeAlarm = isSnoozeAlarm,
                shouldStopService = shouldStopService
            )
        }

        return START_NOT_STICKY
    }

    private suspend fun handleDelivery(
        alarmId: Long,
        isSnoozeAlarm: Boolean?,
        shouldStopService: Boolean
    ) {
        if (shouldStopService) {
            stopDeliveryEarly(alarmId = alarmId)
            userDataRepository.setAlarmMissedDetected(detected = true)
            return
        }

        val alarm = alarmsRepository.getAlarm(alarmId = alarmId)
        val verdict = alarmDeliveryValidator.validate(alarm = alarm, isSnoozeAlarm = isSnoozeAlarm)

        if (alarm == null || verdict !is AlarmDeliveryValidator.Verdict.Ring) {
            stopDeliveryEarly(alarmId = alarmId)
            handleEarlyStoppedAlarm(alarmId = alarmId)
            return
        }

        // An early delivery is waited out, so that the alarm rings on time and the rescheduling
        // that happens on alarm launch does not schedule the very same occurrence again:
        if (verdict.waitMillis > 0) delay(verdict.waitMillis.milliseconds)

        beginRingingSession(session = ringingSessionFactory.create(alarm = alarm))

        startAlarmRinging(alarmId = alarmId, isSnoozeAlarm = isSnoozeAlarm)
    }

    private fun stopDeliveryEarly(alarmId: Long) {
        pendingDeliveryCount--

        if (ringingSession == null && pendingDeliveryCount == 0) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            foregroundAlarmId = null
        }

        qrAlarmManager.cancelUpcomingAlarmNotification(alarmId = alarmId)
    }

    // The service is a singleton, so an alarm delivered while another one rings takes over from it.
    // isRunning is set before StartAlarmRinging marks the alarm as running, otherwise
    // MainViewModel would see a running alarm while the service isn't running and reset it:
    private fun beginRingingSession(session: RingingSession) {
        pendingDeliveryCount--

        ringingSession?.stop()

        val alarmId = session.alarm.alarmId

        if (foregroundAlarmId != alarmId) enterForeground(alarmId = alarmId)

        ringingSession = session
        isRunning = true

        session.start()
    }

    private fun enterForeground(alarmId: Long): Boolean {
        try {
            ServiceCompat.startForeground(
                this,
                alarmId.toInt(),
                alarmNotificationFactory.create(alarmId = alarmId),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else 0
            )
            foregroundAlarmId = alarmId
            return true
        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                exception is ForegroundServiceStartNotAllowedException
            ) {
                return false
            }

            throw exception
        }
    }

    override fun onDestroy() {
        ringingSession?.stop()
        ringingSession = null

        isRunning = false

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_IS_SNOOZE_ALARM = "isSnoozeAlarm"

        var isRunning = false
    }
}
