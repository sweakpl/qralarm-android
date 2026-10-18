package com.sweak.qralarm.core.domain.fake

import com.sweak.qralarm.core.domain.backup.BackupBundle
import com.sweak.qralarm.core.domain.backup.BackupCodec
import com.sweak.qralarm.core.domain.backup.BackupReader
import java.io.InputStream

class FakeBackupCodec : BackupCodec {

    private val _writtenBundles = mutableMapOf<String, BackupBundle>()
    val writtenBundles: Map<String, BackupBundle> get() = _writtenBundles

    var writeFailure: Exception? = null

    private val backups = mutableMapOf<String, BackupReader>()

    var openFailure: Exception? = null

    fun addBackup(sourceUriString: String, backupReader: BackupReader) {
        backups[sourceUriString] = backupReader
    }

    override fun write(
        bundle: BackupBundle,
        destinationUriString: String,
        openRingtone: (alarmId: Long) -> InputStream?
    ) {
        writeFailure?.let { throw it }

        bundle.alarms.filter { it.hasCustomRingtoneFile }.forEach { backupAlarm ->
            openRingtone(backupAlarm.alarmId)?.close()
        }

        _writtenBundles[destinationUriString] = bundle
    }

    override fun open(sourceUriString: String): BackupReader {
        openFailure?.let { throw it }

        return backups[sourceUriString] ?: error("No backup at $sourceUriString")
    }
}
