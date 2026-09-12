package com.sweak.qralarm.core.domain.backup

import com.sweak.qralarm.core.domain.util.Error

sealed class BackupError : Error {

    /** The chosen file is not a QRAlarm backup, or is too damaged to be read as one. */
    data object NotAQRAlarmBackup : BackupError()

    /** The backup was made by a newer version of QRAlarm than the one restoring it. */
    data class UnsupportedFormatVersion(
        val fileFormatVersion: Int,
        val supportedFormatVersion: Int
    ) : BackupError()

    /** The backup file could not be read or written. */
    data object IoFailure : BackupError()
}
