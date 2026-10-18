package com.sweak.qralarm.core.domain.backup

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import java.time.LocalDate

class BackupFormatTest {

    @Test
    fun `given a date, when building the default backup file name, then it holds the ISO date and the backup extension`() {
        // Given
        val date = LocalDate.of(2026, 3, 5)

        // When
        val result = defaultBackupFileName(date = date)

        // Then
        assertThat(result).isEqualTo("qralarm_backup_2026-03-05.qralarmbackup")
    }
}
