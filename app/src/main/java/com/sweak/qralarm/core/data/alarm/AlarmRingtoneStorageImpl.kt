package com.sweak.qralarm.core.data.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import com.sweak.qralarm.core.domain.alarm.AlarmRingtoneStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import androidx.core.net.toUri

@SuppressLint("SetWorldReadable")
class AlarmRingtoneStorageImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AlarmRingtoneStorage {

    private val storageContext =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else context

    override fun saveContentUriForAlarm(contentUriString: String, alarmId: Long): String {
        val file = ringtoneFile(alarmId = alarmId)
        file.createNewFile()
        // Setting world-readable due to: https://stackoverflow.com/a/11977292/14037302
        file.setReadable(true, false)

        FileOutputStream(file).use { outputStream ->
            context.contentResolver.openInputStream(contentUriString.toUri()).use { inputStream ->
                if (inputStream == null) {
                    throw IOException()
                }

                copyStream(inputStream, outputStream)
                outputStream.flush()
            }
        }

        deleteLegacyRingtoneFile(alarmId = alarmId)

        return Uri.fromFile(file).toString()
    }

    override fun duplicateForAlarm(sourceAlarmId: Long, newAlarmId: Long): String? {
        val sourceFile = existingRingtoneFile(alarmId = sourceAlarmId) ?: return null

        return try {
            val newFile = ringtoneFile(alarmId = newAlarmId)
            sourceFile.copyTo(target = newFile, overwrite = true)
            newFile.setReadable(true, false)
            Uri.fromFile(newFile).toString()
        } catch (exception: Exception) {
            if (exception is IOException ||
                exception is SecurityException ||
                exception is NullPointerException
            ) {
                null
            } else {
                throw exception
            }
        }
    }

    override fun deleteForAlarm(alarmId: Long) {
        ringtoneFile(alarmId = alarmId).apply {
            if (exists()) delete()
        }

        deleteLegacyRingtoneFile(alarmId = alarmId)
    }

    override fun exists(alarmId: Long): Boolean = existingRingtoneFile(alarmId = alarmId) != null

    override fun migrateToDeviceProtectedStorage(alarmId: Long): String? {
        return try {
            val file = ringtoneFile(alarmId = alarmId)

            if (file.exists()) {
                deleteLegacyRingtoneFile(alarmId = alarmId)
                return Uri.fromFile(file).toString()
            }

            val legacyFile = legacyRingtoneFile(alarmId = alarmId)

            if (!legacyFile.exists()) return null

            legacyFile.copyTo(target = file, overwrite = true)
            file.setReadable(true, false)
            legacyFile.delete()

            Uri.fromFile(file).toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun ringtoneFile(alarmId: Long): File =
        File(storageContext.filesDir, alarmId.toString())

    private fun legacyRingtoneFile(alarmId: Long): File =
        File(context.filesDir, alarmId.toString())

    private fun existingRingtoneFile(alarmId: Long): File? {
        ringtoneFile(alarmId = alarmId).let { if (it.exists()) return it }
        return legacyRingtoneFile(alarmId = alarmId).takeIf { it.exists() }
    }

    private fun deleteLegacyRingtoneFile(alarmId: Long) {
        val legacyFile = legacyRingtoneFile(alarmId = alarmId)

        if (legacyFile != ringtoneFile(alarmId = alarmId) && legacyFile.exists()) {
            legacyFile.delete()
        }
    }

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int

        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }
}
