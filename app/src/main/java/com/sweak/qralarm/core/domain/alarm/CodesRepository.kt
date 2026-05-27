package com.sweak.qralarm.core.domain.alarm

import kotlinx.coroutines.flow.Flow

interface CodesRepository {

    fun getCodesFlow(): Flow<List<Code>>
    suspend fun getCode(codeId: Long): Code?
    fun getCodeFlow(codeId: Long): Flow<Code?>

    suspend fun findOrCreateCode(value: String): Long
    suspend fun updateCodeName(codeId: Long, name: String?)

    fun getDefaultAlarmCodeFlow(): Flow<Code?>
    suspend fun setDefaultAlarmCode(value: String?, name: String?)
    suspend fun setDefaultAlarmCodeById(codeId: Long?)

    suspend fun cleanupUnreferencedCodes()
    suspend fun migrateLegacyDefaultAlarmCode()
}
