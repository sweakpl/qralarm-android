package com.sweak.qralarm.features.add_edit_alarm.destinations.advanced

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.sweak.qralarm.features.add_edit_alarm.fake.FakeAlarmDraftHandle
import com.sweak.qralarm.features.add_edit_alarm.model.AlarmDraft
import com.sweak.qralarm.fixture.MainDispatcherRule
import org.junit.Rule
import org.junit.Test

class AdvancedAlarmSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(alarmDraftHandle: FakeAlarmDraftHandle) =
        AdvancedAlarmSettingsViewModel(alarmDraftHandle = alarmDraftHandle)

    @Test
    fun `given the gentle wake-up dialog is open, when a new duration is selected, then the draft holds it and the dialog closes`() {
        // Given
        val alarmDraftHandle =
            FakeAlarmDraftHandle(AlarmDraft(gentleWakeupDurationInSeconds = 30))
        val viewModel = viewModel(alarmDraftHandle)
        viewModel.onEvent(
            AdvancedAlarmSettingsScreenUserEvent.ChooseGentleWakeUpDurationDialogVisible(
                isVisible = true
            )
        )

        // When
        viewModel.onEvent(
            AdvancedAlarmSettingsScreenUserEvent.GentleWakeUpDurationSelected(
                newGentleWakeUpDurationInSeconds = 60
            )
        )

        // Then
        assertThat(alarmDraftHandle.draft.value.gentleWakeupDurationInSeconds).isEqualTo(60)
        assertThat(viewModel.state.value.draft.gentleWakeupDurationInSeconds).isEqualTo(60)
        assertThat(viewModel.state.value.ui.isChooseGentleWakeUpDurationDialogVisible).isFalse()
    }

    @Test
    fun `given the cancel lock dialog is open, when a new duration is selected, then the draft holds it and the dialog closes`() {
        // Given
        val alarmDraftHandle = FakeAlarmDraftHandle(AlarmDraft(cancelLockDurationInMinutes = 15))
        val viewModel = viewModel(alarmDraftHandle)
        viewModel.onEvent(
            AdvancedAlarmSettingsScreenUserEvent.ChooseCancelLockDurationDialogVisible(
                isVisible = true
            )
        )

        // When
        viewModel.onEvent(
            AdvancedAlarmSettingsScreenUserEvent.CancelLockDurationSelected(
                newCancelLockDurationInMinutes = 30
            )
        )

        // Then
        assertThat(alarmDraftHandle.draft.value.cancelLockDurationInMinutes).isEqualTo(30)
        assertThat(viewModel.state.value.ui.isChooseCancelLockDurationDialogVisible).isFalse()
    }

    @Test
    fun `given the draft is changed on another screen, when the advanced state is read, then it reflects the change`() {
        // Given
        val alarmDraftHandle = FakeAlarmDraftHandle(AlarmDraft(isCodeEnabled = true))
        val viewModel = viewModel(alarmDraftHandle)

        // When
        alarmDraftHandle.updateDraft { it.copy(isCodeEnabled = false) }

        // Then
        assertThat(viewModel.state.value.draft.isCodeEnabled).isFalse()
    }
}
