package com.sweak.qralarm.core.storage.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-photographed item. Reference photos and features live in files keyed by [objectId]. */
@Entity(tableName = "custom_object")
data class CustomObjectEntity(
    @PrimaryKey(autoGenerate = true)
    val objectId: Long = 0,
    val name: String,
    val createdAtMillis: Long,
    val referenceCount: Int,
    val embeddingThreshold: Float
)
