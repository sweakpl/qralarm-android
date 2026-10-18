package com.sweak.qralarm.core.domain.fake

import com.sweak.qralarm.core.domain.backup.BackupBundle
import com.sweak.qralarm.core.domain.backup.BackupMetadata
import com.sweak.qralarm.core.domain.backup.BackupReader
import java.io.ByteArrayInputStream
import java.io.InputStream

class FakeBackupReader(
    override val metadata: BackupMetadata,
    override val bundle: BackupBundle,
    private val ringtones: Map<Long, ByteArray>
) : BackupReader {

    var isClosed = false
        private set

    override fun openRingtone(oldAlarmId: Long): InputStream? =
        ringtones[oldAlarmId]?.let { ByteArrayInputStream(it) }

    override fun close() {
        isClosed = true
    }
}
