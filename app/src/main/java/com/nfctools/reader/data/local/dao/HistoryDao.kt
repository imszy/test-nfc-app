package com.nfctools.reader.data.local.dao

import androidx.room.*
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE type = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: HistoryType): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: Long): HistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long
    
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)
    
    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM history WHERE timestamp < :timestamp")
    suspend fun deleteHistoryOlderThan(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM history")
    suspend fun getHistoryCount(): Int
}
