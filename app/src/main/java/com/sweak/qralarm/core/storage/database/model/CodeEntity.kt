package com.sweak.qralarm.core.storage.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "code",
    indices = [Index(value = ["value"], unique = true)]
)
data class CodeEntity(
    @PrimaryKey(autoGenerate = true)
    val codeId: Long = 0,
    val value: String,
    val name: String?
)
