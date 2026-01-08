package com.nfctools.reader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nfctools.reader.data.local.dao.HistoryDao
import com.nfctools.reader.data.local.entity.HistoryEntity

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
