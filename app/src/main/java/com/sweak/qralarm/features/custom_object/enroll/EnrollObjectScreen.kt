package com.sweak.qralarm.features.custom_object.enroll

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sweak.qralarm.R
import com.sweak.qralarm.core.designsystem.icon.QRAlarmIcons
import com.sweak.qralarm.core.recognition.custom.EnrollmentRules
import com.sweak.qralarm.core.ui.compose_util.ObserveAsEvents
import kotlin.math.min

@Composable
fun EnrollObjectScreen(
    objectIdToReplace: Long,
    onClose: () -> Unit,
    onSaved: (objectId: Long) -> Unit
) {
    val viewModel = hiltViewModel<EnrollObjectViewModel, EnrollObjectViewModel.Factory> {
        it.create(objectIdToReplace)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.runCamera(lifecycleOwner)
        }
    }
    ObserveAsEvents(viewModel.backendEvents) { event ->
        when (event) {
            is EnrollObjectBackendEvent.Saved -> onSaved(event.objectId)
            EnrollObjectBackendEvent.SaveFailed ->
                Toast.makeText(context, R.string.custom_object_save_failed, Toast.LENGTH_LONG).show()
        }
    }
    BackHandler {
        if (state.step == EnrollObjectScreenState.Step.TEST) viewModel.onBackToCaptureClicked() else onClose()
    }

    EnrollObjectContent(
        state = state,
        onClose = onClose,
        onNameChanged = viewModel::onNameChanged,
        onCapture = viewModel::onCaptureClicked,
        onRecord = viewModel::onRecordClicked,
        onRemoveShot = viewModel::onRemoveShot,
        onContinue = { viewModel.onContinueClicked() },
        onBackToCapture = viewModel::onBackToCaptureClicked,
        onSave = { viewModel.onSaveClicked() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollObjectContent(
    state: EnrollObjectScreenState,
    onClose: () -> Unit,
    onNameChanged: (String) -> Unit,
    onCapture: () -> Unit,
    onRecord: () -> Unit,
    onRemoveShot: (Int) -> Unit,
    onContinue: () -> Unit,
    onBackToCapture: () -> Unit,
    onSave: () -> Unit
) {
    val isTest = state.step == EnrollObjectScreenState.Step.TEST
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(
                        if (state.isReEnrolling) R.string.custom_object_retake_title
                        else R.string.custom_object_add_title
                    ))
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(QRAlarmIcons.Close, stringResource(R.string.content_description_close_icon))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
                state.surfaceRequest?.let {
                    CameraXViewfinder(
                        surfaceRequest = it,
                        implementationMode = ImplementationMode.EMBEDDED,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Canvas(Modifier.fillMaxSize()) {
                    if (!isTest) {
                        // Same square the analysis crops: GUIDE_BOX_FRACTION of the short side.
                        val side = min(size.width, size.height) * EnrollmentRules.GUIDE_BOX_FRACTION
                        drawRect(
                            Color.White,
                            Offset((size.width - side) / 2, (size.height - side) / 2),
                            Size(side, side),
                            style = Stroke(3.dp.toPx())
                        )
                    } else state.testBox?.let { box ->
                        val scale = min(size.width / box.imageWidth, size.height / box.imageHeight)
                        val width = box.imageWidth * scale
                        val height = box.imageHeight * scale
                        drawRect(
                            Color.Green,
                            Offset((size.width - width) / 2 + box.left * width, (size.height - height) / 2 + box.top * height),
                            Size((box.right - box.left) * width, (box.bottom - box.top) * height),
                            style = Stroke(3.dp.toPx())
                        )
                    }
                }
            }

            if (state.cameraError) {
                Text(stringResource(R.string.custom_object_camera_error), color = MaterialTheme.colorScheme.error)
            }

            if (!isTest) {
                Text(stringResource(R.string.custom_object_capture_instructions))
                Text(
                    stringResource(
                        R.string.custom_object_photo_count, state.shots.size, EnrollmentRules.MIN_REFERENCES,
                        EnrollmentRules.MAX_REFERENCES
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
                state.shotIssue?.let { issue ->
                    Text(
                        stringResource(when (issue) {
                            EnrollmentRules.ShotIssue.BLURRY -> R.string.custom_object_shot_blurry
                            EnrollmentRules.ShotIssue.TOO_LITTLE_DETAIL -> R.string.custom_object_shot_no_detail
                        }),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (state.inconsistentShotsRemoved > 0) {
                    Text(
                        stringResource(R.string.custom_object_shots_inconsistent, state.inconsistentShotsRemoved),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(state.shots) { index, shot ->
                        Box {
                            Image(
                                shot.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(72.dp).clickable { onRemoveShot(index) }
                            )
                            Icon(
                                QRAlarmIcons.Close,
                                stringResource(R.string.custom_object_remove_photo),
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.TopEnd).size(18.dp)
                            )
                        }
                    }
                }
                if (state.isRecording) {
                    Text(stringResource(R.string.custom_object_recording), color = MaterialTheme.colorScheme.primary)
                }
                if (state.clipSkippedPlainSides) {
                    Text(stringResource(R.string.custom_object_clip_plain_sides))
                }
                if (state.clipFoundNothing) {
                    Text(stringResource(R.string.custom_object_clip_empty), color = MaterialTheme.colorScheme.error)
                }
                val busy = state.isCapturing || state.isRecording
                Button(onClick = onRecord, enabled = state.canCaptureMore && !busy, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.custom_object_record))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCapture, enabled = state.canCaptureMore && !busy) {
                        Text(stringResource(R.string.custom_object_take_photo))
                    }
                    OutlinedButton(onClick = onContinue, enabled = state.canContinue && !busy) {
                        Text(stringResource(R.string.custom_object_continue))
                    }
                }
            } else {
                Text(stringResource(R.string.custom_object_test_instructions))
                LinearProgressIndicator(
                    progress = { if (state.isTestPassed) 1f else state.testProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )
                if (state.isTestPassed) {
                    Text(stringResource(R.string.custom_object_test_passed), color = MaterialTheme.colorScheme.primary)
                }
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.custom_object_name)) },
                    isError = state.showNameError,
                    supportingText = if (state.showNameError) {
                        { Text(stringResource(R.string.custom_object_name_required)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onBackToCapture) {
                        Text(stringResource(R.string.custom_object_retake))
                    }
                    Button(onClick = onSave, enabled = state.isTestPassed && !state.isSaving) {
                        Text(stringResource(R.string.custom_object_save))
                    }
                }
            }
        }
    }
}
