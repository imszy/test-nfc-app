package com.nfctools.reader.data.repository

import com.nfctools.reader.data.local.dao.HistoryDao
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {
    
    override fun getAllHistory(): Flow<List<HistoryEntity>> {
        return historyDao.getAllHistory()
    }
    
    override fun getHistoryByType(type: HistoryType): Flow<List<HistoryEntity>> {
        return historyDao.getHistoryByType(type)
    }
    
    override suspend fun getHistoryById(id: Long): HistoryEntity? {
        return historyDao.getHistoryById(id)
    }
    
    override suspend fun insertHistory(history: HistoryEntity): Long {
        return historyDao.insertHistory(history)
    }
    
    override suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }
    
    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }
    
    override suspend fun deleteHistoryOlderThan(days: Int) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        historyDao.deleteHistoryOlderThan(cutoffTime)
    }
}
