package com.sweak.qralarm.core.data.backup

import com.sweak.qralarm.core.domain.backup.BackupFormatException
import com.sweak.qralarm.core.data.backup.dto.AlarmsDto
import com.sweak.qralarm.core.data.backup.dto.AlarmDto
import com.sweak.qralarm.core.domain.recognition.ObjectCategories
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DismissalBackupTest {
    @Test fun readsLegacyCodeAndButtonAlarms() {
        val code = Json.decodeFromString<AlarmDto>("""{"isUsingCode":true}""").toBackupAlarm()
        val button = Json.decodeFromString<AlarmDto>("""{"isUsingCode":false}""").toBackupAlarm()
        assertEquals("CODE", code.dismissalMethod)
        assertEquals("NONE", button.dismissalMethod)
        assertNull(code.objectCategoryId)
    }

    @Test fun objectAlarmRoundTripsThroughJsonAndStorage() {
        val original = AlarmDto(dismissalMethod = "OBJECT", objectCategoryId = "toothbrush")
        val backup = original.toBackupAlarm()
        val restored = Json.decodeFromString<AlarmDto>(Json.encodeToString(backup.toAlarmDto()))
            .toBackupAlarm().toAlarmEntity(null)
        assertEquals("OBJECT", restored.dismissalMethod)
        assertEquals("toothbrush", restored.objectCategoryId)
        assertFalse(restored.isAlarmEnabled)
        assertFalse(restored.isAlarmRunning)
        assertFalse(restored.isAlarmSnoozed)
    }

    @Test fun formatTwoCannotSilentlyFallBackToLegacyFields() {
        assertThrows(BackupFormatException.NotAQRAlarmBackup::class.java) {
            AlarmsDto(listOf(AlarmDto(isUsingCode = true))).toValidatedBackupAlarms(2)
        }
        assertEquals("CODE", AlarmsDto(listOf(AlarmDto(isUsingCode = true)))
            .toValidatedBackupAlarms(1).single().dismissalMethod)
        assertThrows(BackupFormatException.NotAQRAlarmBackup::class.java) {
            AlarmsDto(listOf(AlarmDto(dismissalMethod = "OBJECT", objectCategoryId = "bathroom")))
                .toValidatedBackupAlarms(2)
        }
    }

    @Test fun malformedSettingsAreRejectedByTheSharedValidator() {
        assertFalse(ObjectCategories.isValid("UNKNOWN", null))
        assertFalse(ObjectCategories.isValid("OBJECT", null))
        assertFalse(ObjectCategories.isValid("OBJECT", "bathroom"))
        assertTrue(ObjectCategories.isValid("OBJECT", "sink"))
        assertTrue(ObjectCategories.isValid("NONE", null))
        assertTrue(ObjectCategories.isValid("CODE", null))
    }

    @Test
    fun customObjectAlarmsRoundTripAndFallBackWithoutTheirItem() {
        val original = AlarmDto(dismissalMethod = "CUSTOM_OBJECT", customObjectIds = listOf(7, 9))
        val restored = AlarmsDto(listOf(original)).toValidatedBackupAlarms(3).single()
        assertEquals("CUSTOM_OBJECT", restored.dismissalMethod)
        assertEquals(listOf(7L, 9L), restored.customObjectIds)
        assertEquals(listOf(7L, 9L), restored.toAlarmDto().customObjectIds)
        // At least one item came back.
        assertEquals("CUSTOM_OBJECT", restored.toAlarmEntity(assignedCodeId = null, restoredCustomObjectCount = 1).dismissalMethod)
        // None of the items' files were in the archive.
        assertEquals("NONE", restored.toAlarmEntity(assignedCodeId = null, restoredCustomObjectCount = 0).dismissalMethod)
    }

    @Test
    fun customObjectWithoutIdIsStillReadable() {
        val restored = AlarmsDto(listOf(AlarmDto(dismissalMethod = "CUSTOM_OBJECT"))).toValidatedBackupAlarms(3)
        assertEquals("NONE", restored.single().toAlarmEntity(assignedCodeId = null).dismissalMethod)
    }
}
