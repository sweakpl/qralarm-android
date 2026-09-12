package com.sweak.qralarm.features.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.backup.BackupError
import com.sweak.qralarm.core.domain.backup.CreateBackup
import com.sweak.qralarm.core.domain.backup.RestoreBackup
import com.sweak.qralarm.core.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val createBackup: CreateBackup,
    private val restoreBackup: RestoreBackup
) : ViewModel() {

    private val _state = MutableStateFlow(BackupScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: BackupScreenUserEvent) {
        when (event) {
            is BackupScreenUserEvent.OnExportBackupClicked -> {
                _state.update { currentState ->
                    currentState.copy(message = null)
                }
            }

            is BackupScreenUserEvent.BackupDestinationPicked -> viewModelScope.launch {
                if (isWorkInProgress()) return@launch

                _state.update { currentState ->
                    currentState.copy(isBackupInProgress = true, message = null)
                }

                val result = createBackup(destinationUriString = event.destinationUriString)

                _state.update { currentState ->
                    currentState.copy(
                        isBackupInProgress = false,
                        message = when (result) {
                            is Result.Success -> BackupScreenMessage.BackupCreated
                            is Result.Error -> BackupScreenMessage.BackupCreationFailed
                        }
                    )
                }
            }

            is BackupScreenUserEvent.OnImportBackupClicked -> {
                _state.update { currentState ->
                    currentState.copy(
                        isImportConfirmationDialogVisible = true,
                        message = null
                    )
                }
            }

            is BackupScreenUserEvent.ImportConfirmationDialogVisible -> {
                _state.update { currentState ->
                    currentState.copy(isImportConfirmationDialogVisible = event.isVisible)
                }
            }

            is BackupScreenUserEvent.OnImportBackupConfirmed -> {
                _state.update { currentState ->
                    currentState.copy(isImportConfirmationDialogVisible = false)
                }
            }

            is BackupScreenUserEvent.BackupSourcePicked -> viewModelScope.launch {
                if (isWorkInProgress()) return@launch

                _state.update { currentState ->
                    currentState.copy(isImportInProgress = true, message = null)
                }

                val result = restoreBackup(sourceUriString = event.sourceUriString)

                _state.update { currentState ->
                    currentState.copy(
                        isImportInProgress = false,
                        message = when (result) {
                            is Result.Success -> BackupScreenMessage.BackupImported
                            is Result.Error -> when (result.error) {
                                is BackupError.NotAQRAlarmBackup ->
                                    BackupScreenMessage.BackupNotRecognized

                                is BackupError.UnsupportedFormatVersion ->
                                    BackupScreenMessage.BackupTooNew

                                is BackupError.IoFailure ->
                                    BackupScreenMessage.BackupImportFailed
                            }
                        }
                    )
                }
            }

            else -> { /* no-op */ }
        }
    }

    private fun isWorkInProgress(): Boolean =
        state.value.isBackupInProgress || state.value.isImportInProgress
}
