package com.nfctools.reader.data.repository

import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntity>>
    fun getHistoryByType(type: HistoryType): Flow<List<HistoryEntity>>
    suspend fun getHistoryById(id: Long): HistoryEntity?
    suspend fun insertHistory(history: HistoryEntity): Long
    suspend fun deleteHistory(id: Long)
    suspend fun clearAllHistory()
    suspend fun deleteHistoryOlderThan(days: Int)
}
