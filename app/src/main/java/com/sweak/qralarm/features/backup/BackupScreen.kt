package com.sweak.qralarm.features.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.component.QRAlarmDialog
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.designsystem.theme.BlueZodiac
import com.sweak.qralarm.core.designsystem.theme.Gold
import com.sweak.qralarm.core.designsystem.theme.Jacarta
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.isQRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.core.domain.backup.defaultBackupFileName
import com.sweak.qralarm.core.ui.compose_util.UiText

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

    // Anything can be picked on purpose: a backup usually comes back from a drive or an email as a
    // file of no particular type, and a narrower filter would grey out the user's own backup.
    val openBackupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri ->
        if (sourceUri != null) {
            backupViewModel.onEvent(
                BackupScreenUserEvent.BackupSourcePicked(sourceUriString = sourceUri.toString())
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

                is BackupScreenUserEvent.OnImportBackupConfirmed -> {
                    backupViewModel.onEvent(event)
                    openBackupFileLauncher.launch(arrayOf("*/*"))
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
            val isAnythingInProgress = state.isBackupInProgress || state.isImportInProgress

            Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
                BackupOptionCard(
                    icon = QRAlarmIcons.ExportBackup,
                    iconContentDescription =
                        stringResource(R.string.content_description_export_backup_icon),
                    title = stringResource(R.string.export_backup),
                    description = stringResource(R.string.export_backup_description),
                    actionText = stringResource(R.string.export_backup),
                    isActionInProgress = state.isBackupInProgress,
                    isActionEnabled = !isAnythingInProgress,
                    onActionClick = { onEvent(BackupScreenUserEvent.OnExportBackupClicked) },
                    message = state.exportMessage,
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.medium,
                        top = MaterialTheme.space.mediumLarge,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.mediumLarge
                    )
                )

                BackupOptionCard(
                    icon = QRAlarmIcons.ImportBackup,
                    iconContentDescription =
                        stringResource(R.string.content_description_import_backup_icon),
                    title = stringResource(R.string.import_backup),
                    description = stringResource(R.string.import_backup_description),
                    warning = stringResource(R.string.replaces_all_current_data),
                    actionText = stringResource(R.string.import_backup),
                    isActionInProgress = state.isImportInProgress,
                    isActionEnabled = !isAnythingInProgress,
                    onActionClick = { onEvent(BackupScreenUserEvent.OnImportBackupClicked) },
                    message = state.importMessage,
                    modifier = Modifier.padding(
                        start = MaterialTheme.space.medium,
                        end = MaterialTheme.space.medium,
                        bottom = MaterialTheme.space.mediumLarge
                    )
                )
            }
        }
    }

    if (state.isImportConfirmationDialogVisible) {
        QRAlarmDialog(
            title = stringResource(R.string.import_backup_question),
            message = stringResource(R.string.import_backup_question_description),
            onDismissRequest = {
                onEvent(
                    BackupScreenUserEvent.ImportConfirmationDialogVisible(isVisible = false)
                )
            },
            onPositiveClick = { onEvent(BackupScreenUserEvent.OnImportBackupConfirmed) },
            positiveButtonText = stringResource(R.string.import_backup),
            negativeButtonText = stringResource(R.string.cancel)
        )
    }
}

@Composable
private fun BackupOptionCard(
    icon: ImageVector,
    iconContentDescription: String,
    title: String,
    description: String,
    actionText: String,
    isActionInProgress: Boolean,
    isActionEnabled: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    warning: String? = null,
    message: BackupScreenMessage? = null
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(all = MaterialTheme.space.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.smallMedium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = MaterialTheme.space.smallMedium)
            )

            if (warning != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.small),
                    modifier = Modifier.padding(top = MaterialTheme.space.smallMedium)
                ) {
                    Icon(
                        imageVector = QRAlarmIcons.Warning,
                        contentDescription = stringResource(R.string.content_description_warning_icon),
                        tint = Gold,
                        modifier = Modifier.size(size = MaterialTheme.space.medium)
                    )

                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold
                    )
                }
            }

            BackupActionButton(
                text = actionText,
                isInProgress = isActionInProgress,
                isEnabled = isActionEnabled,
                onClick = onActionClick,
                modifier = Modifier.padding(
                    top = if (warning != null) MaterialTheme.space.smallMedium
                    else MaterialTheme.space.medium
                )
            )

            AnimatedContent(
                targetState = message,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Backup action message animation"
            ) { backupScreenMessage ->
                if (backupScreenMessage != null) {
                    Text(
                        text = backupScreenMessage.text.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = when (backupScreenMessage) {
                            is BackupScreenMessage.Success ->
                                MaterialTheme.colorScheme.onSurfaceVariant

                            is BackupScreenMessage.Failure ->
                                MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.space.smallMedium)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupActionButton(
    text: String,
    isInProgress: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isQRAlarmTheme = MaterialTheme.isQRAlarmTheme

    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = if (isQRAlarmTheme) ButtonDefaults.buttonColors(containerColor = Jacarta)
        else ButtonDefaults.buttonColors(),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isInProgress) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(size = MaterialTheme.space.mediumLarge)
            )
        } else {
            Text(text = text)
        }
    }
}

@Preview
@Composable
private fun BackupScreenContentPreview() {
    QRAlarmTheme {
        BackupScreenContent(
            state = BackupScreenState(
                exportMessage = BackupScreenMessage.Success(
                    text = UiText.StringResource(resId = R.string.backup_created)
                )
            ),
            onEvent = {}
        )
    }
}
