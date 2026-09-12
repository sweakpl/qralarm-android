package com.sweak.qralarm.core.domain.backup

import java.io.Closeable
import java.io.InputStream

/**
 * Turns a [BackupBundle] into a single backup file and back.
 */
interface BackupCodec {

    /**
     * Writes [bundle] to the file at [destinationUriString]. [openRingtone] is asked for the
     * ringtone file of every alarm that is marked as having one; an alarm whose ringtone cannot be
     * opened simply travels without it. The returned streams are closed here.
     *
     * @throws java.io.IOException if the backup could not be written.
     */
    fun write(
        bundle: BackupBundle,
        destinationUriString: String,
        openRingtone: (alarmId: Long) -> InputStream?
    )

    /**
     * Reads the backup at [sourceUriString] in full before anything is restored from it, so that a
     * file that turns out not to be a usable backup is rejected while everything currently stored
     * is still untouched. The caller owns the returned reader and must close it.
     *
     * @throws BackupFormatException if the file is not a QRAlarm backup, or was written in a
     * format this version of the app cannot read.
     * @throws java.io.IOException if the backup could not be read.
     */
    fun open(sourceUriString: String): BackupReader
}

/**
 * An opened backup, read and ready to restore from.
 */
interface BackupReader : Closeable {

    val metadata: BackupMetadata
    val bundle: BackupBundle

    /**
     * Opens the backed-up ringtone of the alarm that had [oldAlarmId] when the backup was made.
     * Returns null if the backup holds no readable ringtone for that alarm. The caller owns the
     * returned stream and must close it.
     */
    fun openRingtone(oldAlarmId: Long): InputStream?
}

sealed class BackupFormatException(message: String) : Exception(message) {

    class NotAQRAlarmBackup : BackupFormatException("Not a QRAlarm backup")

    class UnsupportedFormatVersion(val fileFormatVersion: Int) : BackupFormatException(
        "Backup format version $fileFormatVersion is newer than " +
                "$CURRENT_BACKUP_FORMAT_VERSION"
    )
}
