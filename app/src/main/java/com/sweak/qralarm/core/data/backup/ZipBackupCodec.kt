package com.sweak.qralarm.core.data.backup

import android.content.Context
import androidx.core.net.toUri
import com.sweak.qralarm.BuildConfig
import com.sweak.qralarm.core.data.backup.dto.AlarmsDto
import com.sweak.qralarm.core.data.backup.dto.CodesDto
import com.sweak.qralarm.core.data.backup.dto.ManifestDto
import com.sweak.qralarm.core.data.backup.dto.PreferencesDto
import com.sweak.qralarm.core.domain.backup.BackupBundle
import com.sweak.qralarm.core.domain.backup.BackupCodec
import com.sweak.qralarm.core.domain.backup.BackupFormatException
import com.sweak.qralarm.core.domain.backup.BackupMetadata
import com.sweak.qralarm.core.domain.backup.BackupReader
import com.sweak.qralarm.core.domain.backup.CURRENT_BACKUP_FORMAT_VERSION
import com.sweak.qralarm.core.storage.database.QRALARM_DATABASE_VERSION
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Keeps a backup in a zip file: one JSON entry per kind of data, plus the ringtone files as they
 * are. Reading stages the picked file into the cache directory first, since a zip has to be read
 * back and forth and what the user picked can be anywhere - on a memory card, in a cloud drive.
 */
class ZipBackupCodec @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BackupCodec {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun write(
        bundle: BackupBundle,
        destinationUriString: String,
        openRingtone: (alarmId: Long) -> InputStream?
    ) {
        val outputStream = context.contentResolver.openOutputStream(destinationUriString.toUri())
            ?: throw IOException("Could not open the destination file for writing")

        ZipOutputStream(outputStream.buffered()).use { zipOutputStream ->
            zipOutputStream.writeJsonEntry(
                entryName = MANIFEST_ENTRY_NAME,
                value = currentBackupMetadata().toManifestDto()
            )
            zipOutputStream.writeJsonEntry(
                entryName = ALARMS_ENTRY_NAME,
                value = AlarmsDto(alarms = bundle.alarms.map { it.toAlarmDto() })
            )
            zipOutputStream.writeJsonEntry(
                entryName = CODES_ENTRY_NAME,
                value = CodesDto(codes = bundle.codes.map { it.toCodeDto() })
            )
            zipOutputStream.writeJsonEntry(
                entryName = PREFERENCES_ENTRY_NAME,
                value = bundle.preferences.toPreferencesDto()
            )

            bundle.alarms.filter { it.hasCustomRingtoneFile }.forEach { backupAlarm ->
                openRingtone(backupAlarm.alarmId)?.use { ringtoneStream ->
                    zipOutputStream.putNextEntry(
                        ZipEntry(ringtoneEntryName(alarmId = backupAlarm.alarmId))
                    )
                    ringtoneStream.copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            }
        }
    }

    override fun open(sourceUriString: String): BackupReader {
        val stagedFile = File.createTempFile(
            STAGED_FILE_PREFIX,
            STAGED_FILE_SUFFIX,
            context.cacheDir
        )

        try {
            context.contentResolver.openInputStream(sourceUriString.toUri()).use { inputStream ->
                if (inputStream == null) {
                    throw IOException("Could not open the chosen file for reading")
                }

                FileOutputStream(stagedFile).use { inputStream.copyTo(it) }
            }

            val zipFile = try {
                ZipFile(stagedFile)
            } catch (_: ZipException) {
                throw BackupFormatException.NotAQRAlarmBackup()
            }

            try {
                // The manifest is what tells a backup apart from any other file the user can pick.
                val manifest = zipFile.readJsonEntry<ManifestDto>(entryName = MANIFEST_ENTRY_NAME)
                    ?: throw BackupFormatException.NotAQRAlarmBackup()

                if (manifest.backupFormatVersion > CURRENT_BACKUP_FORMAT_VERSION) {
                    throw BackupFormatException.UnsupportedFormatVersion(
                        fileFormatVersion = manifest.backupFormatVersion
                    )
                }

                val alarmsDto = zipFile.readJsonEntry<AlarmsDto>(entryName = ALARMS_ENTRY_NAME)
                    ?: AlarmsDto()
                val codesDto = zipFile.readJsonEntry<CodesDto>(entryName = CODES_ENTRY_NAME)
                    ?: CodesDto()
                val preferencesDto =
                    zipFile.readJsonEntry<PreferencesDto>(entryName = PREFERENCES_ENTRY_NAME)
                        ?: PreferencesDto()

                return ZipBackupReader(
                    zipFile = zipFile,
                    stagedFile = stagedFile,
                    metadata = manifest.toBackupMetadata(),
                    bundle = BackupBundle(
                        alarms = alarmsDto.alarms.map { it.toBackupAlarm() },
                        // A code with no value could not be scanned and, being what the code is
                        // recognized by, could also collide with another one.
                        codes = codesDto.codes
                            .filter { it.value.isNotBlank() }
                            .map { it.toBackupCode() },
                        preferences = preferencesDto.toBackupPreferences()
                    )
                )
            } catch (throwable: Throwable) {
                zipFile.close()
                throw throwable
            }
        } catch (throwable: Throwable) {
            stagedFile.delete()
            throw throwable
        }
    }

    private fun currentBackupMetadata(): BackupMetadata = BackupMetadata(
        backupFormatVersion = CURRENT_BACKUP_FORMAT_VERSION,
        createdAtEpochMillis = System.currentTimeMillis(),
        applicationId = BuildConfig.APPLICATION_ID,
        flavor = BACKUP_FLAVOR,
        versionCode = BuildConfig.VERSION_CODE,
        versionName = BuildConfig.VERSION_NAME,
        databaseSchemaVersion = QRALARM_DATABASE_VERSION
    )

    private inline fun <reified T> ZipOutputStream.writeJsonEntry(entryName: String, value: T) {
        putNextEntry(ZipEntry(entryName))
        write(json.encodeToString(value).toByteArray())
        closeEntry()
    }

    private inline fun <reified T> ZipFile.readJsonEntry(entryName: String): T? {
        val entry = getEntry(entryName) ?: return null
        val entryContent = getInputStream(entry).use { it.readBytes().decodeToString() }

        return try {
            json.decodeFromString<T>(entryContent)
        } catch (_: IllegalArgumentException) {
            // Everything kotlinx.serialization throws on content it cannot read comes from here.
            throw BackupFormatException.NotAQRAlarmBackup()
        }
    }
}

private class ZipBackupReader(
    private val zipFile: ZipFile,
    private val stagedFile: File,
    override val metadata: BackupMetadata,
    override val bundle: BackupBundle
) : BackupReader {

    override fun openRingtone(oldAlarmId: Long): InputStream? {
        val entry = zipFile.getEntry(ringtoneEntryName(alarmId = oldAlarmId)) ?: return null

        return try {
            zipFile.getInputStream(entry)
        } catch (_: IOException) {
            null
        }
    }

    override fun close() {
        try {
            zipFile.close()
        } finally {
            stagedFile.delete()
        }
    }
}

private fun ringtoneEntryName(alarmId: Long): String = "$RINGTONES_ENTRY_NAME_PREFIX$alarmId"

private const val MANIFEST_ENTRY_NAME = "manifest.json"
private const val ALARMS_ENTRY_NAME = "alarms.json"
private const val CODES_ENTRY_NAME = "codes.json"
private const val PREFERENCES_ENTRY_NAME = "preferences.json"
private const val RINGTONES_ENTRY_NAME_PREFIX = "ringtones/"

private const val STAGED_FILE_PREFIX = "backup_"
private const val STAGED_FILE_SUFFIX = ".zip"

private const val BACKUP_FLAVOR = "master"
