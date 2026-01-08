package com.nfctools.reader.domain.model

data class NfcTagData(
    val id: String,                      // 标签ID，如 "04:8A:9F:3D"
    val type: String,                    // NFC-A, NFC-B, NFC-F
    val capacity: String,                // "504 / 888 bytes"
    val technologies: List<String>,      // ["NfcA", "MifareUltralight", "Ndef"]
    val textContent: String?,            // 文本内容
    val hexContent: String?,             // 十六进制内容
    val atqa: String?,                   // ATQA值
    val sak: String?,                    // SAK值
    val isWritable: Boolean,
    val isLocked: Boolean
)
