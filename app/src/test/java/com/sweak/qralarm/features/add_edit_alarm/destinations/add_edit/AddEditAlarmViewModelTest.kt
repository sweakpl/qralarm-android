package com.sweak.qralarm.features.add_edit_alarm.destinations.add_edit

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sweak.qralarm.core.domain.alarm.Alarm.Ringtone
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.domain.user.UserDataRepository
import com.sweak.qralarm.core.ui.sound.AlarmRingtonePlayer
import com.sweak.qralarm.features.add_edit_alarm.fake.FakeAlarmDraftHandle
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmDraftMapper
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmScheduleCalculator
import com.sweak.qralarm.features.add_edit_alarm.util.RingtonePreviewController
import com.sweak.qralarm.fixture.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditAlarmViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onPreviewCompleted = slot<(Boolean) -> Unit>()
    private val alarmRingtonePlayer = mockk<AlarmRingtonePlayer>(relaxed = true) {
        every {
            playAlarmRingtonePreview(
                ringtone = any(),
                onPreviewCompleted = capture(onPreviewCompleted)
            )
        } returns Unit
    }
    private val userDataRepository = mockk<UserDataRepository>(relaxed = true) {
        every { temporaryScannedCode } returns MutableStateFlow(null)
    }
    private val codesRepository = mockk<CodesRepository> {
        every { getDefaultAlarmCodeFlow() } returns flowOf(null)
    }

    private fun viewModel(
        idOfAlarmToEdit: Long,
        alarmDraftHandle: FakeAlarmDraftHandle
    ): AddEditAlarmViewModel {
        val clock = fixedClock(LocalDateTime.of(2026, 11, 16, 6, 0))
        val alarmScheduleCalculator = AlarmScheduleCalculator(clock)

        return AddEditAlarmViewModel(
            idOfAlarm = idOfAlarmToEdit,
            alarmDraftHandle = alarmDraftHandle,
            ringtonePreviewController = RingtonePreviewController(alarmRingtonePlayer),
            userDataRepository = userDataRepository,
            codesRepository = codesRepository,
            qrAlarmManager = mockk(relaxed = true),
            addOrEditAlarm = mockk(),
            deleteAlarm = mockk(),
            setAlarm = mockk(),
            alarmScheduleCalculator = alarmScheduleCalculator,
            alarmDraftMapper = AlarmDraftMapper(alarmScheduleCalculator, clock),
            requiredPermissionsGate = mockk()
        )
    }

    private fun TestScope.collectBackendEvents(
        viewModel: AddEditAlarmViewModel
    ): List<AddEditAlarmScreenBackendEvent> {
        val backendEvents = mutableListOf<AddEditAlarmScreenBackendEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.backendEvents.toList(backendEvents)
        }
        return backendEvents
    }

    @Test
    fun `given the flow is still loading, when it finishes, then the screen leaves loading with the available ringtones`() {
        // Given
        every { alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable() } returns false
        val alarmDraftHandle = FakeAlarmDraftHandle(AlarmDraft(alarmHourOfDay = 7, alarmMinute = 0))
        alarmDraftHandle.isLoading.value = true
        val viewModel = viewModel(idOfAlarmToEdit = 5, alarmDraftHandle = alarmDraftHandle)
        assertThat(viewModel.state.value.ui.isLoading).isTrue()

        // When
        alarmDraftHandle.isLoading.value = false

        // Then
        val state = viewModel.state.value
        assertThat(state.ui.isLoading).isFalse()
        assertThat(state.ui.isEditingExistingAlarm).isTrue()
        assertThat(state.availableRingtonesWithPlaybackState).isEqualTo(
            (Ringtone.entries - Ringtone.SYSTEM_DEFAULT).associateWith { false }
        )
    }

    @Test
    fun `given an existing alarm with unsaved changes, when cancel is clicked, then the discard dialog is shown`() =
        runTest {
            // Given
            val alarmDraftHandle = FakeAlarmDraftHandle(
                AlarmDraft(alarmHourOfDay = 7, alarmMinute = 0)
            )
            alarmDraftHandle.hasUnsavedChanges = true
            val viewModel = viewModel(idOfAlarmToEdit = 5, alarmDraftHandle = alarmDraftHandle)
            val backendEvents = collectBackendEvents(viewModel)

            // When
            viewModel.onEvent(AddEditAlarmScreenUserEvent.OnCancelClicked)

            // Then
            assertThat(viewModel.state.value.ui.isDiscardAlarmChangesDialogVisible).isTrue()
            assertThat(backendEvents).isEqualTo(emptyList())
        }

    @Test
    fun `given a new alarm with unsaved changes, when cancel is clicked, then the changes are discarded without asking`() =
        runTest {
            // Given
            val alarmDraftHandle = FakeAlarmDraftHandle(
                AlarmDraft(alarmHourOfDay = 7, alarmMinute = 0)
            )
            alarmDraftHandle.hasUnsavedChanges = true
            val viewModel = viewModel(idOfAlarmToEdit = 0, alarmDraftHandle = alarmDraftHandle)
            val backendEvents = collectBackendEvents(viewModel)

            // When
            viewModel.onEvent(AddEditAlarmScreenUserEvent.OnCancelClicked)

            // Then
            assertThat(viewModel.state.value.ui.isDiscardAlarmChangesDialogVisible).isFalse()
            assertThat(backendEvents).containsExactly(
                AddEditAlarmScreenBackendEvent.AlarmChangesDiscarded
            )
        }

    @Test
    fun `given a ringtone preview is playing, when a ringtone config is selected, then the preview stops`() {
        // Given
        every { alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable() } returns true
        val alarmDraftHandle = FakeAlarmDraftHandle(
            AlarmDraft(
                alarmHourOfDay = 7,
                alarmMinute = 0,
                ringtone = Ringtone.GENTLE_GUITAR,
                alarmVolumePercentage = null
            )
        )
        val viewModel = viewModel(idOfAlarmToEdit = 5, alarmDraftHandle = alarmDraftHandle)
        viewModel.onEvent(
            AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback(ringtone = Ringtone.KALIMBA)
        )
        assertThat(viewModel.state.value.availableRingtonesWithPlaybackState[Ringtone.KALIMBA])
            .isEqualTo(true)

        // When
        viewModel.onEvent(
            AddEditAlarmScreenUserEvent.AlarmRingtoneConfigSelected(
                newRingtone = Ringtone.KALIMBA,
                newAlarmVolumePercentage = 80
            )
        )

        // Then
        val state = viewModel.state.value
        assertThat(state.availableRingtonesWithPlaybackState.values.none { it }).isTrue()
        assertThat(state.draft.ringtone).isEqualTo(Ringtone.KALIMBA)
        assertThat(state.draft.alarmVolumePercentage).isEqualTo(80)
    }

    @Test
    fun `given a ringtone preview is playing, when its playback fails, then the error is reported and nothing is playing`() =
        runTest {
            // Given
            every { alarmRingtonePlayer.isSystemDefaultAlarmRingtoneAvailable() } returns true
            val alarmDraftHandle = FakeAlarmDraftHandle(
                AlarmDraft(alarmHourOfDay = 7, alarmMinute = 0)
            )
            val viewModel = viewModel(idOfAlarmToEdit = 5, alarmDraftHandle = alarmDraftHandle)
            val backendEvents = collectBackendEvents(viewModel)
            viewModel.onEvent(
                AddEditAlarmScreenUserEvent.ToggleAlarmRingtonePlayback(ringtone = Ringtone.KALIMBA)
            )

            // When
            onPreviewCompleted.captured(true)

            // Then
            assertThat(backendEvents).containsExactly(
                AddEditAlarmScreenBackendEvent.AlarmRingtonePreviewPlaybackError
            )
            assertThat(viewModel.state.value.availableRingtonesWithPlaybackState.values.none { it })
                .isTrue()
        }
}
