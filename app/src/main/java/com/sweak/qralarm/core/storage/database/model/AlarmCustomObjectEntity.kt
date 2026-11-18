package com.sweak.qralarm.core.storage.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** The user's own items an alarm requires; every one has to be shown to stop it. */
@Entity(
    tableName = "alarm_custom_object",
    primaryKeys = ["alarmId", "objectId"],
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["alarmId"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomObjectEntity::class,
            parentColumns = ["objectId"],
            childColumns = ["objectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("objectId")]
)
data class AlarmCustomObjectEntity(
    val alarmId: Long,
    val objectId: Long,
    /** Order the items were chosen in, for display. */
    val position: Int
)
