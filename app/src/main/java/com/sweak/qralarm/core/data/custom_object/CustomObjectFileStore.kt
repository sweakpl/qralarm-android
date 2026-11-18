package com.sweak.qralarm.core.data.custom_object

import android.content.Context
import com.sweak.qralarm.core.recognition.custom.ReferenceFeatures
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reference photos and features, in device-protected storage like the database, so an alarm that
 * rings before the phone is first unlocked after a reboot can still be dismissed.
 */
@Singleton
class CustomObjectFileStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val root: File
        get() = File(context.createDeviceProtectedStorageContext().noBackupFilesDir, "custom_objects")

    fun directory(objectId: Long) = File(root, objectId.toString())

    /** Writes into a temporary directory first so a half-written set never replaces a good one. */
    fun write(objectId: Long, jpegs: List<ByteArray>, features: List<ReferenceFeatures>) {
        val target = directory(objectId)
        val staging = File(root, "$objectId.tmp").apply { deleteRecursively(); mkdirs() }
        jpegs.forEachIndexed { i, bytes -> File(staging, "ref_$i.jpg").writeBytes(bytes) }
        features.forEachIndexed { i, feature ->
            File(staging, "ref_$i.feat").outputStream().use { feature.writeTo(it) }
        }
        target.deleteRecursively()
        if (!staging.renameTo(target)) throw IOException("Could not store object $objectId")
    }

    fun readFeatures(objectId: Long): List<ReferenceFeatures>? = try {
        featureFiles(objectId).map { file ->
            file.inputStream().use { ReferenceFeatures.readFrom(it) } ?: return null
        }.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }

    fun readJpegs(objectId: Long): List<ByteArray> =
        directory(objectId).listFiles { f -> f.extension == "jpg" }.orEmpty()
            .sortedBy { it.nameWithoutExtension.substringAfter('_').toIntOrNull() ?: 0 }
            .map { it.readBytes() }

    /** Every stored file of an object, by file name, for backups. */
    fun readAllFiles(objectId: Long): Map<String, ByteArray> =
        directory(objectId).listFiles().orEmpty().filter { it.isFile }.associate { it.name to it.readBytes() }

    fun writeAllFiles(objectId: Long, files: Map<String, ByteArray>) {
        val target = directory(objectId).apply { deleteRecursively(); mkdirs() }
        files.forEach { (name, bytes) -> File(target, name).writeBytes(bytes) }
    }

    fun deleteAll() {
        root.deleteRecursively()
    }

    fun delete(objectId: Long) {
        directory(objectId).deleteRecursively()
    }

    private fun featureFiles(objectId: Long) =
        directory(objectId).listFiles { f -> f.extension == "feat" }.orEmpty()
            .sortedBy { it.nameWithoutExtension.substringAfter('_').toIntOrNull() ?: 0 }
}
