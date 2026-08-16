package com.sweak.qralarm.core.data.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
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

    override fun saveContentUriForAlarm(contentUriString: String, alarmId: Long): String {
        val file = File(context.filesDir, alarmId.toString())
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

        return Uri.fromFile(file).toString()
    }

    override fun duplicateForAlarm(sourceAlarmId: Long, newAlarmId: Long): String? {
        val sourceFile = File(context.filesDir, sourceAlarmId.toString())

        if (!sourceFile.exists()) return null

        return try {
            val newFile = File(context.filesDir, newAlarmId.toString())
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
        File(context.filesDir, alarmId.toString()).apply {
            if (exists()) delete()
        }
    }

    override fun exists(alarmId: Long): Boolean =
        File(context.filesDir, alarmId.toString()).exists()

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int

        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }
}
