package com.sweak.qralarm.features.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweak.qralarm.core.domain.backup.CreateBackup
import com.sweak.qralarm.core.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val createBackup: CreateBackup
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
                if (state.value.isBackupInProgress) return@launch

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

            else -> { /* no-op */ }
        }
    }
}
