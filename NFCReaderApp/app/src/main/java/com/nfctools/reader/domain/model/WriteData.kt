package com.nfctools.reader.domain.model

data class WriteData(
    val type: WriteType,
    val content: String,
    val encoding: Encoding = Encoding.UTF_8,
    val lockTag: Boolean = false,
    val password: String? = null
)

enum class WriteType {
    TEXT, URL, FILE, CUSTOM
}

enum class Encoding {
    UTF_8, GBK, ASCII
}
