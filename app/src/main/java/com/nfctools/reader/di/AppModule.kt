package com.nfctools.reader.di

import android.content.Context
import androidx.room.Room
import com.nfctools.reader.data.local.dao.HistoryDao
import com.nfctools.reader.data.local.database.AppDatabase
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.data.repository.HistoryRepositoryImpl
import com.nfctools.reader.data.repository.NfcRepository
import com.nfctools.reader.data.repository.NfcRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nfc_reader_db"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
    
    @Binds
    @Singleton
    abstract fun bindNfcRepository(impl: NfcRepositoryImpl): NfcRepository
}
