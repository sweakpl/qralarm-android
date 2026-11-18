package com.sweak.qralarm.features.emergency.task

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.QRAlarmManager
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.core.domain.alarm.Alarm
import com.sweak.qralarm.core.domain.alarm.AlarmsRepository
import com.sweak.qralarm.core.domain.alarm.DisableAlarm
import com.sweak.qralarm.core.domain.alarm.DismissalMethod
import com.sweak.qralarm.core.domain.alarm.RingAgainLater
import com.sweak.qralarm.core.domain.custom_object.CustomObjectsRepository
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanation
import com.sweak.qralarm.core.domain.emergency.EmergencyExplanationsRepository
import com.sweak.qralarm.core.domain.emergency.ExplanationValidator
import com.sweak.qralarm.core.domain.recognition.ObjectCategories
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.domain.user.model.EmergencyTaskType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = EmergencyViewModel.Factory::class)
class EmergencyViewModel @AssistedInject constructor(
    @Assisted private val idOfAlarmToCancel: Long,
    @param:ApplicationContext private val context: Context,
    private val disableAlarm: DisableAlarm,
    private val alarmsRepository: AlarmsRepository,
    private val userDataRepository: UserDataRepository,
    private val qrAlarmManager: QRAlarmManager,
    private val explanationsRepository: EmergencyExplanationsRepository,
    private val customObjectsRepository: CustomObjectsRepository,
    private val ringAgainLater: RingAgainLater
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(idOfAlarmToCancel: Long): EmergencyViewModel
    }

    private var alarm: Alarm? = null

    private val _state = MutableStateFlow(EmergencyScreenState())
    val state = _state.asStateFlow()

    private val backendEventsChannel = Channel<EmergencyScreenBackendEvent>(Channel.BUFFERED)
    val backendEvents = backendEventsChannel.receiveAsFlow()

    private var isScreenVisible = false
    private var lastInteractionMillis = System.currentTimeMillis()
    private var lastKeystrokeMillis = 0L
    private var lastHoldMillis = 0L
    private var isFinished = false

    init {
        viewModelScope.launch {
            alarm = alarmsRepository.getAlarm(alarmId = idOfAlarmToCancel)
            val valueRange = userDataRepository.emergencySliderRange.first()
            val targetValue = Random.nextInt(range = valueRange)
            var currentValue = Random.nextInt(range = valueRange)
            while (currentValue == targetValue) currentValue = Random.nextInt(range = valueRange)
            val taskType = userDataRepository.emergencyTaskType.first()
            _state.update { currentState ->
                currentState.copy(
                    taskType = taskType,
                    emergencyTaskConfig = currentState.emergencyTaskConfig.copy(
                        valueRange = valueRange,
                        targetValue = targetValue,
                        currentValue = currentValue,
                        remainingMatches = userDataRepository.emergencyRequiredMatches.first(),
                        isCompleted = false
                    )
                )
            }
        }
        viewModelScope.launch { tick() }
    }

    /** Once a second: keeps the ringing alarm silent while the user is active, and counts typing time. */
    private suspend fun tick() {
        while (!isFinished) {
            val now = System.currentTimeMillis()
            val idleMillis = now - lastInteractionMillis
            val isRinging = AlarmService.isRunning && idOfAlarmToCancel != 0L
            val shouldHold = isRinging && isScreenVisible && idleMillis < IDLE_TIMEOUT_MILLIS
            if (shouldHold && now - lastHoldMillis >= HOLD_INTERVAL_MILLIS) {
                backendEventsChannel.send(EmergencyScreenBackendEvent.HoldAlarmSilent)
                lastHoldMillis = now
            } else if (!shouldHold && state.value.isAlarmHeldSilent) {
                backendEventsChannel.send(EmergencyScreenBackendEvent.ReleaseAlarm)
                lastHoldMillis = 0L
            }
            val typingActive = state.value.taskType == EmergencyTaskType.WRITTEN_EXPLANATION &&
                state.value.isTaskStarted && now - lastKeystrokeMillis < TYPING_ACTIVITY_WINDOW_MILLIS
            _state.update {
                it.copy(
                    isAlarmHeldSilent = shouldHold,
                    idleSecondsRemaining = if (shouldHold && IDLE_TIMEOUT_MILLIS - idleMillis <= IDLE_WARNING_MILLIS) {
                        ((IDLE_TIMEOUT_MILLIS - idleMillis) / 1000).toInt().coerceAtLeast(0)
                    } else null,
                    explanation = if (typingActive) it.explanation.copy(
                        typingSecondsRemaining = (it.explanation.typingSecondsRemaining - 1).coerceAtLeast(0)
                    ) else it.explanation
                )
            }
            delay(1.seconds)
        }
    }

    private fun onInteraction() {
        val wasIdle = System.currentTimeMillis() - lastInteractionMillis >= IDLE_TIMEOUT_MILLIS
        lastInteractionMillis = System.currentTimeMillis()
        // Silence again at once rather than on the next tick.
        if (wasIdle) lastHoldMillis = 0L
    }

    fun onEvent(event: EmergencyScreenUserEvent) {
        if (event !is EmergencyScreenUserEvent.OnScreenVisibilityChanged) onInteraction()
        when (event) {
            is EmergencyScreenUserEvent.OnScreenVisibilityChanged -> {
                isScreenVisible = event.isVisible
                if (event.isVisible) {
                    lastInteractionMillis = System.currentTimeMillis()
                    lastHoldMillis = 0L
                } else if (state.value.isAlarmHeldSilent && !isFinished) {
                    _state.update { it.copy(isAlarmHeldSilent = false) }
                    backendEventsChannel.trySend(EmergencyScreenBackendEvent.ReleaseAlarm)
                }
            }

            is EmergencyScreenUserEvent.OnTaskStarted -> _state.update { it.copy(isTaskStarted = true) }

            is EmergencyScreenUserEvent.OnTaskValueChanged -> _state.update {
                it.copy(emergencyTaskConfig = it.emergencyTaskConfig.copy(currentValue = event.value))
            }

            is EmergencyScreenUserEvent.OnTaskValueSelected -> onSliderValueSelected()

            is EmergencyScreenUserEvent.OnExplanationChanged -> onExplanationChanged(event.text)

            is EmergencyScreenUserEvent.OnSubmitExplanation -> submitExplanation()

            is EmergencyScreenUserEvent.OnRingAgainInMinutes ->
                ringAgain(RingAgainLater.afterMinutes(event.minutes))

            is EmergencyScreenUserEvent.OnRingAgainAt ->
                ringAgain(RingAgainLater.nextOccurrence(event.hourOfDay, event.minute))

            is EmergencyScreenUserEvent.OnRingAgainDeclined -> finish()

            is EmergencyScreenUserEvent.OnCloseClicked -> { /* handled by the screen */ }
        }
    }

    private fun onExplanationChanged(text: String) {
        val current = state.value.explanation
        if (current.isCompleted) return
        // Typing, including keyboard suggestions and swipe typing, adds at most a word at a time.
        if (text.length - current.text.length > MAX_INSERTION_CHARS) {
            _state.update { it.copy(explanation = current.copy(pasteRejected = true)) }
            return
        }
        lastKeystrokeMillis = System.currentTimeMillis()
        _state.update {
            it.copy(
                explanation = current.copy(
                    text = text,
                    wordCount = ExplanationValidator.countWords(text),
                    pasteRejected = false,
                    // Keep showing problems from a failed submit only until they are fixed.
                    issues = if (current.issues.isEmpty()) emptyList()
                    else ExplanationValidator.validate(text).issues
                )
            )
        }
    }

    private fun submitExplanation() {
        val current = state.value.explanation
        if (!current.canSubmit) return
        val result = ExplanationValidator.validate(current.text)
        if (!result.isValid) {
            // A failed attempt changes nothing else: the text stays and the alarm stays silent.
            _state.update { it.copy(explanation = current.copy(issues = result.issues)) }
            return
        }
        _state.update { it.copy(explanation = current.copy(issues = emptyList(), isCompleted = true)) }
        viewModelScope.launch {
            if (idOfAlarmToCancel != 0L) {
                explanationsRepository.addExplanation(
                    EmergencyExplanation(
                        alarmId = idOfAlarmToCancel,
                        alarmLabel = alarm?.alarmLabel,
                        alarmHourOfDay = alarm?.alarmHourOfDay,
                        alarmMinute = alarm?.alarmMinute,
                        dismissalSummary = dismissalSummary(),
                        createdAtMillis = System.currentTimeMillis(),
                        text = current.text.trim()
                    )
                )
            }
            completeTask()
        }
    }

    private fun onSliderValueSelected() {
        val config = state.value.emergencyTaskConfig
        if (config.isCompleted || config.currentValue != config.targetValue) return
        val remainingMatches = config.remainingMatches - 1
        if (remainingMatches <= 0) {
            _state.update {
                it.copy(emergencyTaskConfig = it.emergencyTaskConfig.copy(isCompleted = true, remainingMatches = 0))
            }
            viewModelScope.launch { completeTask() }
            return
        }
        var targetValue = Random.nextInt(range = config.valueRange)
        while (targetValue == config.currentValue) targetValue = Random.nextInt(range = config.valueRange)
        _state.update {
            it.copy(
                emergencyTaskConfig = it.emergencyTaskConfig.copy(
                    targetValue = targetValue,
                    remainingMatches = remainingMatches
                )
            )
        }
    }

    private suspend fun completeTask() {
        val isAlarmRepeating = alarm?.repeatingMode is Alarm.RepeatingMode.Days
        val shouldDisableRepeatingAlarms =
            userDataRepository.isEmergencyDisableRepeatingAlarmsEnabled.first()
        if (idOfAlarmToCancel != 0L && (!isAlarmRepeating || shouldDisableRepeatingAlarms)) {
            disableAlarm(idOfAlarmToCancel)
        }
        delay(1500.milliseconds)
        if (isAlarmRepeating && shouldDisableRepeatingAlarms) {
            qrAlarmManager.notifyAboutEmergencyDisabledRepeatingAlarm()
        }
        if (idOfAlarmToCancel != 0L && alarm != null) {
            _state.update { it.copy(showRingAgainChoice = true) }
        } else {
            finish()
        }
    }

    private fun ringAgain(timeInMillis: Long) = viewModelScope.launch {
        if (ringAgainLater(idOfAlarmToCancel, timeInMillis)) {
            backendEventsChannel.send(EmergencyScreenBackendEvent.RingAgainScheduled)
        }
        finish()
    }

    private fun finish() {
        if (isFinished) return
        isFinished = true
        _state.update { it.copy(showRingAgainChoice = false) }
        backendEventsChannel.trySend(EmergencyScreenBackendEvent.EmergencyTaskCompleted)
    }

    private suspend fun dismissalSummary(): String? {
        val alarm = alarm ?: return null
        return when (alarm.dismissalMethod) {
            DismissalMethod.CUSTOM_OBJECT -> alarm.customObjectId
                ?.let { customObjectsRepository.getObject(it)?.name }
                ?.let { context.getString(R.string.custom_object_alarm_summary, it) }
            DismissalMethod.OBJECT -> alarm.objectCategoryId
                ?.let { context.getString(R.string.object_alarm_summary, ObjectCategories.displayName(it)) }
            DismissalMethod.CODE -> context.getString(R.string.emergency_history_code_summary)
            DismissalMethod.NONE -> null
        }
    }

    companion object {
        const val IDLE_TIMEOUT_MILLIS = 90_000L
        const val IDLE_WARNING_MILLIS = 20_000L
        const val HOLD_INTERVAL_MILLIS = 15_000L
        const val TYPING_ACTIVITY_WINDOW_MILLIS = 10_000L
        /** Longer single insertions are treated as pasting. */
        const val MAX_INSERTION_CHARS = 30
    }
}
