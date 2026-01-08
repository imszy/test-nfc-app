package com.nfctools.reader.data.local.database

import androidx.room.TypeConverter
import com.nfctools.reader.data.local.entity.ContentType
import com.nfctools.reader.data.local.entity.HistoryType

class Converters {
    
    @TypeConverter
    fun fromHistoryType(value: HistoryType): String = value.name
    
    @TypeConverter
    fun toHistoryType(value: String): HistoryType = HistoryType.valueOf(value)
    
    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name
    
    @TypeConverter
    fun toContentType(value: String): ContentType = ContentType.valueOf(value)
}
