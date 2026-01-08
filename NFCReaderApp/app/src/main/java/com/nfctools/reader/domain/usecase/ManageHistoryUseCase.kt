package com.nfctools.reader.domain.usecase

import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.data.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    fun getAllHistory(): Flow<List<HistoryEntity>> {
        return historyRepository.getAllHistory()
    }
    
    fun getHistoryByType(type: HistoryType): Flow<List<HistoryEntity>> {
        return historyRepository.getHistoryByType(type)
    }
    
    suspend fun deleteHistory(id: Long) {
        historyRepository.deleteHistory(id)
    }
    
    suspend fun clearAllHistory() {
        historyRepository.clearAllHistory()
    }
    
    suspend fun cleanupOldHistory(retentionDays: Int) {
        historyRepository.deleteHistoryOlderThan(retentionDays)
    }
}
