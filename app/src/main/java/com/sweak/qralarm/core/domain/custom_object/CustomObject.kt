package com.sweak.qralarm.core.domain.custom_object

import com.sweak.qralarm.core.recognition.custom.ReferenceFeatures
import kotlinx.coroutines.flow.Flow

data class CustomObject(
    val objectId: Long,
    val name: String,
    val createdAtMillis: Long,
    val referenceCount: Int,
    val embeddingThreshold: Float
)

/** A reference photo as captured (JPEG of the guide-box crop) with its computed features. */
class CustomObjectReference(val jpeg: ByteArray, val features: ReferenceFeatures)

interface CustomObjectsRepository {
    fun getAllObjects(): Flow<List<CustomObject>>
    suspend fun getObject(objectId: Long): CustomObject?
    suspend fun addObject(name: String, references: List<CustomObjectReference>, embeddingThreshold: Float): Long
    suspend fun replaceReferences(objectId: Long, references: List<CustomObjectReference>, embeddingThreshold: Float)
    suspend fun renameObject(objectId: Long, name: String)
    suspend fun deleteObject(objectId: Long)
    suspend fun countAlarmsUsingObject(objectId: Long): Int
    /** Null when the files are missing or unreadable. */
    suspend fun loadReferenceFeatures(objectId: Long): List<ReferenceFeatures>?
    suspend fun loadReferenceJpegs(objectId: Long): List<ByteArray>
}
