package com.sweak.qralarm.features.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.backup.defaultBackupFileName

@Composable
fun BackupScreen(onBackClicked: () -> Unit) {
    val backupViewModel = hiltViewModel<BackupViewModel>()
    val backupScreenState by backupViewModel.state.collectAsStateWithLifecycle()

    // A custom extension would have the provider append ".zip" to it if the type was zip.
    val createBackupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { destinationUri ->
        if (destinationUri != null) {
            backupViewModel.onEvent(
                BackupScreenUserEvent.BackupDestinationPicked(
                    destinationUriString = destinationUri.toString()
                )
            )
        }
    }

    BackupScreenContent(
        state = backupScreenState,
        onEvent = { event ->
            when (event) {
                is BackupScreenUserEvent.OnBackClicked -> onBackClicked()
                is BackupScreenUserEvent.OnExportBackupClicked -> {
                    backupViewModel.onEvent(event)
                    createBackupFileLauncher.launch(defaultBackupFileName())
                }

                else -> backupViewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreenContent(
    state: BackupScreenState,
    onEvent: (BackupScreenUserEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.backup),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(BackupScreenUserEvent.OnBackClicked) }
                    ) {
                        Icon(
                            imageVector = QRAlarmIcons.BackArrow,
                            contentDescription =
                                stringResource(R.string.content_description_back_arrow_icon)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (MaterialTheme.isQRAlarmTheme)
                        Modifier.background(
                            brush = Brush.verticalGradient(listOf(Jacarta, BlueZodiac))
                        )
                    else Modifier
                )
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.space.medium,
                            top = MaterialTheme.space.mediumLarge,
                            end = MaterialTheme.space.medium
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.space.medium),
                        modifier = Modifier.padding(all = MaterialTheme.space.medium)
                    ) {
                        Text(
                            text = stringResource(R.string.backup_description),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val isQRAlarmTheme = MaterialTheme.isQRAlarmTheme

                        Button(
                            onClick = { onEvent(BackupScreenUserEvent.OnExportBackupClicked) },
                            enabled = !state.isBackupInProgress,
                            colors = if (isQRAlarmTheme)
                                ButtonDefaults.buttonColors(containerColor = Jacarta)
                            else
                                ButtonDefaults.buttonColors(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isBackupInProgress) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                                )
                            } else {
                                Text(text = stringResource(R.string.export_backup))
                            }
                        }

                        if (state.message != null) {
                            Text(
                                text = stringResource(
                                    id = when (state.message) {
                                        is BackupScreenMessage.BackupCreated ->
                                            R.string.backup_created

                                        is BackupScreenMessage.BackupCreationFailed ->
                                            R.string.backup_creation_failed
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = when (state.message) {
                                    is BackupScreenMessage.BackupCreated ->
                                        MaterialTheme.colorScheme.onSurfaceVariant

                                    is BackupScreenMessage.BackupCreationFailed ->
                                        MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BackupScreenContentPreview() {
    QRAlarmTheme {
        BackupScreenContent(
            state = BackupScreenState(),
            onEvent = {}
        )
    }
}
