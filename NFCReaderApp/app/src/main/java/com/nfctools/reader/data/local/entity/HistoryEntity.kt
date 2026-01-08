package com.nfctools.reader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: HistoryType,
    val tagId: String,
    val content: String,
    val contentType: ContentType,
    val timestamp: Long,
    val tagType: String? = null,
    val capacity: String? = null
)

enum class HistoryType {
    READ, WRITE
}

enum class ContentType {
    TEXT, URL, FILE, HEX
}
