package com.sweak.qralarm.alarm.service.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.sweak.qralarm.alarm.ACTION_EMERGENCY_TASK_ALARM_MUTE
import com.sweak.qralarm.alarm.ACTION_TEMPORARY_ALARM_MUTE
import com.sweak.qralarm.alarm.EMERGENCY_TASK_ALARM_MUTE_DURATION_SECONDS
import com.sweak.qralarm.alarm.EXTRA_TEMPORARY_MUTE_DURATION_SECONDS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AlarmMuteController @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    @Volatile
    private var session: Session? = null

    private val muteReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_TEMPORARY_ALARM_MUTE -> mute(
                        MuteRequest.Temporary(
                            durationSeconds = intent.getIntExtra(
                                EXTRA_TEMPORARY_MUTE_DURATION_SECONDS,
                                DEFAULT_TEMPORARY_MUTE_DURATION_SECONDS
                            )
                        )
                    )
                    ACTION_EMERGENCY_TASK_ALARM_MUTE -> mute(MuteRequest.EmergencyTask)
                }
            }
        }
    }

    fun start(
        stopRinging: () -> Unit,
        resumeRinging: () -> Unit
    ) {
        startSession(stopRinging, resumeRinging)

        ContextCompat.registerReceiver(
            context,
            muteReceiver,
            IntentFilter().apply {
                addAction(ACTION_TEMPORARY_ALARM_MUTE)
                addAction(ACTION_EMERGENCY_TASK_ALARM_MUTE)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun stop() {
        if (session == null) return

        stopSession()
        context.unregisterReceiver(muteReceiver)
    }

    @VisibleForTesting
    internal fun startSession(
        stopRinging: () -> Unit,
        resumeRinging: () -> Unit,
        dispatcher: CoroutineDispatcher = Dispatchers.Main
    ) {
        session = Session(
            scope = CoroutineScope(SupervisorJob() + dispatcher),
            stopRinging = stopRinging,
            resumeRinging = resumeRinging
        )
    }

    @VisibleForTesting
    internal fun stopSession() {
        session?.scope?.cancel()
        session = null
    }

    @VisibleForTesting
    internal fun mute(request: MuteRequest) {
        val session = session ?: return

        if (request is MuteRequest.Temporary) {
            if (session.hasBeenTemporarilyMuted) return
            session.hasBeenTemporarilyMuted = true
        }

        session.resumeRingingJob?.cancel()
        session.stopRinging()

        session.resumeRingingJob = session.scope.launch {
            delay(request.durationSeconds.seconds)
            session.resumeRinging()
        }
    }

    @VisibleForTesting
    internal sealed class MuteRequest(val durationSeconds: Int) {
        class Temporary(durationSeconds: Int) : MuteRequest(durationSeconds)

        data object EmergencyTask : MuteRequest(EMERGENCY_TASK_ALARM_MUTE_DURATION_SECONDS)
    }

    private class Session(
        val scope: CoroutineScope,
        val stopRinging: () -> Unit,
        val resumeRinging: () -> Unit
    ) {
        var resumeRingingJob: Job? = null
        var hasBeenTemporarilyMuted = false
    }

    private companion object {
        const val DEFAULT_TEMPORARY_MUTE_DURATION_SECONDS = 15
    }
}
