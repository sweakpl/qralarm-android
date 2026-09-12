package com.sweak.qralarm.features.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.R
import com.sweak.qralarm.core.domain.backup.BackupError
import com.sweak.qralarm.core.domain.backup.CreateBackup
import com.sweak.qralarm.core.domain.backup.RestoreBackup
import com.sweak.qralarm.core.domain.util.Result
import com.sweak.qralarm.core.ui.compose_util.UiText
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
                    currentState.withoutMessages()
                }
            }

            is BackupScreenUserEvent.BackupDestinationPicked -> viewModelScope.launch {
                if (isWorkInProgress()) return@launch

                _state.update { currentState ->
                    currentState.withoutMessages().copy(isBackupInProgress = true)
                }

                val result = createBackup(destinationUriString = event.destinationUriString)

                _state.update { currentState ->
                    currentState.copy(
                        isBackupInProgress = false,
                        exportMessage = when (result) {
                            is Result.Success -> BackupScreenMessage.Success(
                                text = UiText.StringResource(resId = R.string.backup_created)
                            )

                            is Result.Error -> BackupScreenMessage.Failure(
                                text = UiText.StringResource(
                                    resId = R.string.backup_creation_failed
                                )
                            )
                        }
                    )
                }
            }

            is BackupScreenUserEvent.OnImportBackupClicked -> {
                _state.update { currentState ->
                    currentState.withoutMessages().copy(
                        isImportConfirmationDialogVisible = true
                    )
                }
            }

            is BackupScreenUserEvent.ImportConfirmationDialogVisible -> {
                _state.update { currentState ->
                    currentState.withoutMessages().copy(
                        isImportConfirmationDialogVisible = event.isVisible
                    )
                }
            }

            is BackupScreenUserEvent.OnImportBackupConfirmed -> {
                _state.update { currentState ->
                    currentState.withoutMessages().copy(
                        isImportConfirmationDialogVisible = false
                    )
                }
            }

            is BackupScreenUserEvent.BackupSourcePicked -> viewModelScope.launch {
                if (isWorkInProgress()) return@launch

                _state.update { currentState ->
                    currentState.withoutMessages().copy(isImportInProgress = true)
                }

                val result = restoreBackup(sourceUriString = event.sourceUriString)

                _state.update { currentState ->
                    currentState.copy(
                        isImportInProgress = false,
                        importMessage = when (result) {
                            is Result.Success -> BackupScreenMessage.Success(
                                text = UiText.StringResource(resId = R.string.backup_imported)
                            )

                            is Result.Error -> BackupScreenMessage.Failure(
                                text = UiText.StringResource(
                                    resId = when (result.error) {
                                        is BackupError.NotAQRAlarmBackup ->
                                            R.string.backup_not_recognized

                                        is BackupError.UnsupportedFormatVersion ->
                                            R.string.backup_too_new

                                        is BackupError.IoFailure ->
                                            R.string.backup_import_failed
                                    }
                                )
                            )
                        }
                    )
                }
            }

            else -> { /* no-op */
            }
        }
    }

    private fun isWorkInProgress(): Boolean =
        state.value.isBackupInProgress || state.value.isImportInProgress

    private fun BackupScreenState.withoutMessages(): BackupScreenState =
        copy(exportMessage = null, importMessage = null)
}
