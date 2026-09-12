package com.sweak.qralarm.core.data.backup.dto

import kotlinx.serialization.Serializable

/**
 * manifest.json - the entry that makes a zip a QRAlarm backup.
 *
 * [backupFormatVersion] is the only field without a default: a file that does not carry it is not
 * a backup, and that is how a backup is told apart from any other zip the user might pick.
 */
@Serializable
data class ManifestDto(
    val backupFormatVersion: Int,
    val createdAtEpochMillis: Long = 0,
    val app: AppDto = AppDto(),
    val databaseSchemaVersion: Int = 0
)

@Serializable
data class AppDto(
    val applicationId: String = "",
    val flavor: String = "",
    val versionCode: Int = 0,
    val versionName: String = ""
)
