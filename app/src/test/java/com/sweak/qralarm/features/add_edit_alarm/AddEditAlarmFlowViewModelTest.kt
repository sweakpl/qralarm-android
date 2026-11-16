package com.sweak.qralarm.features.add_edit_alarm

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.sweak.qralarm.core.domain.fake.FakeAlarmsRepository
import com.sweak.qralarm.core.domain.fixture.alarm
import com.sweak.qralarm.core.domain.fixture.fixedClock
import com.sweak.qralarm.core.ui.model.Code
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmDraftMapper
import com.sweak.qralarm.features.add_edit_alarm.util.AlarmScheduleCalculator
import com.sweak.qralarm.fixture.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import com.sweak.qralarm.core.domain.alarm.Code as DomainCode

class AddEditAlarmFlowViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alarmsRepository = FakeAlarmsRepository()

    private fun flowViewModel(
        idOfAlarmToEdit: Long,
        now: LocalDateTime,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        savedCodesFlow: Flow<List<DomainCode>> = flowOf(emptyList())
    ): AddEditAlarmFlowViewModel {
        val clock = fixedClock(now)

        return AddEditAlarmFlowViewModel(
            idOfAlarmToEdit = idOfAlarmToEdit,
            savedStateHandle = savedStateHandle,
            alarmRingtonePlayer = mockk {
                every { isSystemDefaultAlarmRingtoneAvailable() } returns true
            },
            alarmsRepository = alarmsRepository,
            codesRepository = mockk { every { getCodesFlow() } returns savedCodesFlow },
            alarmDraftMapper = AlarmDraftMapper(
                alarmScheduleCalculator = AlarmScheduleCalculator(clock = clock),
                clock = clock
            )
        )
    }

    private fun SavedStateHandle.afterProcessDeath() =
        SavedStateHandle(keys().associateWith { key -> get(key) })

    @Test
    fun `given a new alarm, when loading finishes, then the draft is preset and the saved codes are available`() {
        // Given
        val savedCode = DomainCode(codeId = 3L, value = "kitchen-qr", name = "Kitchen")

        // When
        val viewModel = flowViewModel(
            idOfAlarmToEdit = 0L,
            now = SATURDAY.atTime(21, 44),
            savedCodesFlow = flowOf(listOf(savedCode))
        )

        // Then
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.draft.value.alarmHourOfDay).isEqualTo(21)
        assertThat(viewModel.draft.value.alarmMinute).isEqualTo(45)
        assertThat(viewModel.previouslySavedCodes.value)
            .containsExactly(Code(id = 3L, value = "kitchen-qr", name = "Kitchen"))
        assertThat(viewModel.hasUnsavedChanges).isFalse()
    }

    @Test
    fun `given an existing alarm, when loading finishes, then the draft holds the alarm's settings`() =
        runTest {
            // Given
            val alarmId = alarmsRepository.addOrEditAlarm(
                alarm(alarmHourOfDay = 6, alarmMinute = 30, cancelLockDurationInMinutes = 15)
            )

            // When
            val viewModel = flowViewModel(idOfAlarmToEdit = alarmId, now = SATURDAY.atTime(21, 44))

            // Then
            assertThat(viewModel.isLoading.value).isFalse()
            assertThat(viewModel.draft.value.alarmHourOfDay).isEqualTo(6)
            assertThat(viewModel.draft.value.alarmMinute).isEqualTo(30)
            assertThat(viewModel.draft.value.cancelLockDurationInMinutes).isEqualTo(15)
            assertThat(viewModel.hasUnsavedChanges).isFalse()
        }

    @Test
    fun `given a loaded draft, when the user changes a setting, then the draft has unsaved changes`() {
        // Given
        val viewModel = flowViewModel(idOfAlarmToEdit = 0L, now = SATURDAY.atTime(21, 44))
        viewModel.updateDraft(countsAsUnsavedChange = false) { it.copy(alarmLabel = "Gym") }

        // When
        viewModel.updateDraft { it.copy(alarmLabel = "Work") }

        // Then
        assertThat(viewModel.draft.value.alarmLabel).isEqualTo("Work")
        assertThat(viewModel.hasUnsavedChanges).isTrue()
    }

    @Test
    fun `given a loaded draft, when a setting is set to the value it already has, then the draft has no unsaved changes`() {
        // Given
        val viewModel = flowViewModel(idOfAlarmToEdit = 0L, now = SATURDAY.atTime(21, 44))
        viewModel.updateDraft(countsAsUnsavedChange = false) { it.copy(alarmLabel = "Gym") }

        // When
        viewModel.updateDraft { it.copy(alarmLabel = "Gym") }

        // Then
        assertThat(viewModel.hasUnsavedChanges).isFalse()
    }

    @Test
    fun `given a loaded draft, when a change the user did not make is applied, then the draft has no unsaved changes`() {
        // Given
        val viewModel = flowViewModel(idOfAlarmToEdit = 0L, now = SATURDAY.atTime(21, 44))
        viewModel.updateDraft(countsAsUnsavedChange = false) { it.copy(alarmLabel = "Gym") }

        // When
        viewModel.updateDraft(countsAsUnsavedChange = false) { it.copy(alarmLabel = "Work") }

        // Then
        assertThat(viewModel.draft.value.alarmLabel).isEqualTo("Work")
        assertThat(viewModel.hasUnsavedChanges).isFalse()
    }

    @Test
    fun `given a code assigned while a new alarm is loading, when loading finishes, then the code is kept`() =
        runTest {
            // Given
            val savedCodesFlow = MutableSharedFlow<List<DomainCode>>()
            val viewModel = flowViewModel(
                idOfAlarmToEdit = 0L,
                now = SATURDAY.atTime(21, 44),
                savedCodesFlow = savedCodesFlow
            )
            assertThat(viewModel.isLoading.value).isTrue()
            viewModel.updateDraft { it.copy(temporaryAssignedCode = Code(value = "scanned-qr")) }

            // When
            savedCodesFlow.emit(emptyList())

            // Then
            assertThat(viewModel.isLoading.value).isFalse()
            assertThat(viewModel.draft.value.temporaryAssignedCode)
                .isEqualTo(Code(value = "scanned-qr"))
            assertThat(viewModel.draft.value.alarmHourOfDay).isEqualTo(21)
            assertThat(viewModel.draft.value.alarmMinute).isEqualTo(45)
        }

    @Test
    fun `given a loaded draft with unsaved changes, when the process is recreated, then the draft is restored instead of reloaded`() {
        // Given
        val savedStateHandle = SavedStateHandle()
        val viewModel = flowViewModel(
            idOfAlarmToEdit = 0L,
            now = SATURDAY.atTime(21, 44),
            savedStateHandle = savedStateHandle,
            savedCodesFlow = flowOf(
                listOf(DomainCode(codeId = 3L, value = "kitchen-qr", name = "Kitchen"))
            )
        )
        viewModel.updateDraft { it.copy(alarmHourOfDay = 6, alarmMinute = 30) }

        // When
        val restoredViewModel = flowViewModel(
            idOfAlarmToEdit = 0L,
            now = SATURDAY.atTime(23, 10),
            savedStateHandle = savedStateHandle.afterProcessDeath(),
            savedCodesFlow = flowOf(emptyList())
        )

        // Then
        assertThat(restoredViewModel.isRestoredFromSavedState).isTrue()
        assertThat(restoredViewModel.isLoading.value).isFalse()
        assertThat(restoredViewModel.draft.value.alarmHourOfDay).isEqualTo(6)
        assertThat(restoredViewModel.draft.value.alarmMinute).isEqualTo(30)
        assertThat(restoredViewModel.previouslySavedCodes.value)
            .containsExactly(Code(id = 3L, value = "kitchen-qr", name = "Kitchen"))
        assertThat(restoredViewModel.hasUnsavedChanges).isTrue()
    }

    @Test
    fun `given the process died while the alarm was loading, when the process is recreated, then the alarm is loaded again`() {
        // Given
        val savedStateHandle = SavedStateHandle()
        flowViewModel(
            idOfAlarmToEdit = 0L,
            now = SATURDAY.atTime(21, 44),
            savedStateHandle = savedStateHandle,
            savedCodesFlow = MutableSharedFlow()
        )

        // When
        val restoredViewModel = flowViewModel(
            idOfAlarmToEdit = 0L,
            now = SATURDAY.atTime(23, 10),
            savedStateHandle = savedStateHandle.afterProcessDeath()
        )

        // Then
        assertThat(restoredViewModel.isRestoredFromSavedState).isFalse()
        assertThat(restoredViewModel.isLoading.value).isFalse()
        assertThat(restoredViewModel.draft.value.alarmHourOfDay).isEqualTo(23)
        assertThat(restoredViewModel.draft.value.alarmMinute).isEqualTo(11)
    }

    private companion object {
        val SATURDAY: LocalDate = LocalDate.of(2026, 11, 14)
    }
}
