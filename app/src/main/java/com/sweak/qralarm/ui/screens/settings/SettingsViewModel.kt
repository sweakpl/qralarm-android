package com.sweak.qralarm.ui.screens.settings

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.sweak.qralarm.R
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject


@ExperimentalPermissionsApi
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val resourceProvider: ResourceProvider,
    private val mediaPlayer: MediaPlayer
) : ViewModel() {

    val settingsUiState: MutableState<SettingsUiState> = runBlocking {
        dataStoreManager.let {
            mutableStateOf(
                SettingsUiState(
                    availableAlarmSounds = AVAILABLE_ALARM_SOUNDS.map { alarmSound ->
                        resourceProvider.getString(alarmSound.nameResourceId)
                    },
                    selectedAlarmSoundIndex = AVAILABLE_ALARM_SOUNDS.indexOf(
                        AlarmSound.fromInt(it.getInt(DataStoreManager.ALARM_SOUND).first())
                    ),
                    selectedSnoozeDurationIndex = AVAILABLE_SNOOZE_DURATIONS.indexOf(
                        it.getInt(DataStoreManager.SNOOZE_DURATION_MINUTES).first()
                    ),
                    selectedSnoozeMaxCountIndex = AVAILABLE_SNOOZE_MAX_COUNTS.indexOf(
                        it.getInt(DataStoreManager.SNOOZE_MAX_COUNT).first()
                    ),
                    dismissAlarmCode = it.getString(DataStoreManager.DISMISS_ALARM_CODE).first(),
                    customAlarmURI = it.getString(DataStoreManager.USER_ALARM_SOUND_URI).first(),
                )
            )
        }
    }

    // WARNING: this code is duplicated in QRAlarmService. I guess because OOP is dumb. I don't care enough to fix it
    private fun getPreferredAlarmSoundUri(packageName: String): Uri {
        val alarmSoundOrdinal = runBlocking {
            dataStoreManager.getInt(DataStoreManager.ALARM_SOUND).first()
        }

        val selection = AlarmSound.fromInt(alarmSoundOrdinal) ?: AlarmSound.GENTLE_GUITAR
        val uri = if (selection == AlarmSound.USER_FILE) {
            val customFile = runBlocking {
                dataStoreManager.getString(DataStoreManager.USER_ALARM_SOUND_URI).first()
            }
            Uri.parse(customFile)
        } else {
            Uri.parse("android.resource://" + packageName + "/" + selection.resourceId)
        }

        return uri
    }


    fun playOrStopAlarmPreview(context: Context) {
        if (!settingsUiState.value.alarmPreviewPlaying) {
            mediaPlayer.apply {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(
                    context,
                    getPreferredAlarmSoundUri(context.packageName)
                )
                isLooping = false
                setOnCompletionListener {
                    stopMediaPlayer()
                }
                prepare()
                start()
            }

            settingsUiState.value = settingsUiState.value.copy(alarmPreviewPlaying = true)
        } else {
            stopMediaPlayer()
        }
    }

    fun updateAlarmSoundSelection(newIndex: Int) {
        val newSelectedAlarmSound = AVAILABLE_ALARM_SOUNDS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedAlarmSoundIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.ALARM_SOUND,
                newSelectedAlarmSound.ordinal
            )
        }
    }

    fun updateSnoozeDurationSelection(newIndex: Int) {
        val newSelectedSnoozeDuration = AVAILABLE_SNOOZE_DURATIONS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedSnoozeDurationIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.SNOOZE_DURATION_MINUTES,
                newSelectedSnoozeDuration
            )
        }
    }

    fun updateSnoozeMaxCountSelection(newIndex: Int) {
        val newSelectedSnoozeMaxCount = AVAILABLE_SNOOZE_MAX_COUNTS[newIndex]

        settingsUiState.value = settingsUiState.value.copy(
            selectedSnoozeMaxCountIndex = newIndex
        )

        viewModelScope.launch {
            dataStoreManager.putInt(
                DataStoreManager.SNOOZE_MAX_COUNT,
                newSelectedSnoozeMaxCount
            )
        }
    }

    fun handleDefaultCodeDownloadButton(
        context: Context,
        storagePermissionState: PermissionState
    ) {
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val storageWritePermissionGranted = storagePermissionState.hasPermission || minSdk29

        if (!storageWritePermissionGranted) {
            when {
                !storagePermissionState.permissionRequested ||
                        storagePermissionState.shouldShowRationale -> {
                    settingsUiState.value =
                        settingsUiState.value.copy(showStoragePermissionDialog = true)
                    return
                }
                !storagePermissionState.shouldShowRationale -> {
                    settingsUiState.value =
                        settingsUiState.value.copy(showStoragePermissionRevokedDialog = true)
                    return
                }
            }
        }

        val qrCodeImageBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.qr_code)

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "QRAlarmCode.jpg")
            put(MediaStore.Images.Media.DISPLAY_NAME, "QRAlarmCode.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, qrCodeImageBitmap.width)
            put(MediaStore.Images.Media.HEIGHT, qrCodeImageBitmap.height)
            put(MediaStore.Images.Media.DATE_TAKEN, currentTimeInMillis())
            put(MediaStore.Images.Media.DATE_ADDED, currentTimeInMillis())
        }

        try {
            with(context.contentResolver) {
                insert(imageCollection, contentValues)?.also { uri ->
                    openOutputStream(uri).use { outputStream ->
                        if (
                            !qrCodeImageBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                95,
                                outputStream
                            )
                        ) {
                            throw IOException("Couldn't save the QRCode Bitmap file!")
                        }
                    }
                } ?: throw IOException("Couldn't create a MediaStore entry!")
            }

            Toast.makeText(
                context,
                resourceProvider.getString(R.string.saved_default_qrcode),
                Toast.LENGTH_LONG
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()

            Toast.makeText(
                context,
                resourceProvider.getString(R.string.not_saved_default_qrcode),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setCustomQRCode(code: String) {
        viewModelScope.launch {
            dataStoreManager.putString(DataStoreManager.DISMISS_ALARM_CODE, code)
            settingsUiState.value = settingsUiState.value.copy(
                showDismissCodeAddedDialog = true,
                dismissAlarmCode = code
            )
        }
    }

    fun handleScanCustomDismissCodeButton(
        navController: NavHostController,
        cameraPermissionState: PermissionState
    ) {
        if (!cameraPermissionState.hasPermission) {
            when {
                !cameraPermissionState.permissionRequested ||
                        cameraPermissionState.shouldShowRationale -> {
                    settingsUiState.value =
                        settingsUiState.value.copy(showCameraPermissionDialog = true)
                    return
                }
                !cameraPermissionState.shouldShowRationale -> {
                    settingsUiState.value =
                        settingsUiState.value.copy(showCameraPermissionRevokedDialog = true)
                    return
                }
            }
        }

        navController.navigate(
            Screen.ScannerScreen.withArguments(SCAN_MODE_SET_CUSTOM_CODE)
        )
    }

    fun stopMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (exception: IllegalStateException) {
            Log.e("SettingsViewModel", "mediaPlayer was not initialized! Cannot stop it...")
        }

        settingsUiState.value = settingsUiState.value.copy(alarmPreviewPlaying = false)
    }

    fun updateCustomAlarmURI(uri: Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                dataStoreManager.putString(DataStoreManager.USER_ALARM_SOUND_URI, uri.toString())
            }
        } else {
            viewModelScope.launch {
                dataStoreManager.putInt(
                    DataStoreManager.ALARM_SOUND,
                    AlarmSound.GENTLE_GUITAR.ordinal
                )
            }
        }
    }
}