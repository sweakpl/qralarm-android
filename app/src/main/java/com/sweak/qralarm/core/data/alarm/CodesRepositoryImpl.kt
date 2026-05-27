package com.sweak.qralarm.core.data.alarm

import com.sweak.qralarm.core.domain.alarm.Code
import com.sweak.qralarm.core.domain.alarm.CodesRepository
import com.sweak.qralarm.core.storage.database.dao.CodesDao
import com.sweak.qralarm.core.storage.database.model.CodeEntity
import com.sweak.qralarm.core.storage.datastore.QRAlarmPreferencesDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CodesRepositoryImpl @Inject constructor(
    private val codesDao: CodesDao,
    private val preferencesDataSource: QRAlarmPreferencesDataSource
) : CodesRepository {

    override fun getCodesFlow(): Flow<List<Code>> =
        codesDao.getAllCodes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCode(codeId: Long): Code? =
        codesDao.getCode(codeId)?.toDomain()

    override fun getCodeFlow(codeId: Long): Flow<Code?> =
        codesDao.getCodeFlow(codeId).map { it?.toDomain() }

    override suspend fun findOrCreateCode(value: String): Long {
        val inserted = codesDao.insertCode(CodeEntity(value = value, name = null))
        return if (inserted != -1L) inserted
        else codesDao.getCodeByValue(value)!!.codeId
    }

    override suspend fun updateCodeName(codeId: Long, name: String?) {
        val existing = codesDao.getCode(codeId) ?: return
        codesDao.updateCode(existing.copy(name = name))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDefaultAlarmCodeFlow(): Flow<Code?> =
        preferencesDataSource.getDefaultAlarmCodeId().flatMapLatest { id ->
            if (id == null) flowOf(null)
            else codesDao.getCodeFlow(id).map { it?.toDomain() }
        }

    override suspend fun setDefaultAlarmCode(value: String?, name: String?) {
        if (value == null) {
            preferencesDataSource.setDefaultAlarmCodeId(null)
        } else {
            val id = findOrCreateCode(value)
            if (name != null) updateCodeName(id, name)
            preferencesDataSource.setDefaultAlarmCodeId(id)
        }
        cleanupUnreferencedCodes()
    }

    override suspend fun setDefaultAlarmCodeById(codeId: Long?) {
        preferencesDataSource.setDefaultAlarmCodeId(codeId)
        cleanupUnreferencedCodes()
    }

    override suspend fun cleanupUnreferencedCodes() {
        val defaultCodeId = preferencesDataSource.getDefaultAlarmCodeId().first()
        codesDao.deleteUnreferencedCodes(defaultCodeId)
    }

    override suspend fun migrateLegacyDefaultAlarmCode() {
        if (preferencesDataSource.getHasMigratedDefaultAlarmCode().first()) return

        val legacyValue = preferencesDataSource.getLegacyDefaultAlarmCodeValue()
        if (legacyValue != null) {
            val codeId = findOrCreateCode(legacyValue)
            preferencesDataSource.setDefaultAlarmCodeId(codeId)
            preferencesDataSource.clearLegacyDefaultAlarmCode()
        }
        preferencesDataSource.setHasMigratedDefaultAlarmCode(migrated = true)
    }

    private fun CodeEntity.toDomain() = Code(codeId = codeId, value = value, name = name)
}
