package com.sweak.qralarm.core.domain.backup

import java.time.LocalDate

/**
 * The backup format this version of the app writes, and the newest one it can read. It goes up
 * only when a backup has to be *understood* differently than before - a field that is merely added
 * or no longer written is already handled, since a reader ignores what it does not know and falls
 * back on what is missing.
 */
const val CURRENT_BACKUP_FORMAT_VERSION = 1

const val BACKUP_FILE_EXTENSION = "qralarmbackup"

private const val BACKUP_FILE_NAME_PREFIX = "qralarm_backup_"

/** The file name proposed to the user, e.g. "qralarm_backup_2026-08-24.qralarmbackup". */
fun defaultBackupFileName(date: LocalDate = LocalDate.now()): String =
    "$BACKUP_FILE_NAME_PREFIX$date.$BACKUP_FILE_EXTENSION"
