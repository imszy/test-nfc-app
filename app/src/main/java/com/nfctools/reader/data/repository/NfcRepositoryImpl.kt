package com.nfctools.reader.data.repository

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import com.nfctools.reader.domain.model.Encoding
import com.nfctools.reader.domain.model.NfcTagData
import com.nfctools.reader.domain.model.WriteData
import com.nfctools.reader.domain.model.WriteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcRepositoryImpl @Inject constructor() : NfcRepository {
    
    override suspend fun readTag(tag: Tag): Result<NfcTagData> = withContext(Dispatchers.IO) {
        try {
            val tagId = tag.id.toHexString()
            val techList = tag.techList.map { it.substringAfterLast('.') }
            
            // 尝试读取NDEF数据
            val ndef = Ndef.get(tag)
            var textContent: String? = null
            var hexContent: String? = null
            var maxSize = 0
            var isWritable = false
            
            if (ndef != null) {
                try {
                    ndef.connect()
                    maxSize = ndef.maxSize
                    isWritable = ndef.isWritable
                    
                    val ndefMessage = ndef.cachedNdefMessage ?: ndef.ndefMessage
                    
                    ndefMessage?.records?.firstOrNull()?.let { record ->
                        when (record.tnf) {
                            NdefRecord.TNF_WELL_KNOWN -> {
                                if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                                    textContent = parseTextRecord(record.payload)
                                } else if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
                                    textContent = parseUriRecord(record)
                                }
                            }
                            NdefRecord.TNF_ABSOLUTE_URI -> {
                                textContent = String(record.payload, Charset.forName("UTF-8"))
                            }
                            NdefRecord.TNF_MIME_MEDIA -> {
                                textContent = String(record.payload, Charset.forName("UTF-8"))
                            }
                        }
                        hexContent = record.payload.toHexString()
                    }
                } finally {
                    try {
                        ndef.close()
                    } catch (e: Exception) {
                        // Ignore close exception
                    }
                }
            }
            
            // 获取技术细节
            var atqa: String? = null
            var sak: String? = null
            
            val nfcA = NfcA.get(tag)
            if (nfcA != null) {
                try {
                    nfcA.connect()
                    atqa = nfcA.atqa?.toHexString()
                    sak = nfcA.sak.toString(16).uppercase()
                } finally {
                    try {
                        nfcA.close()
                    } catch (e: Exception) {
                        // Ignore close exception
                    }
                }
            }
            
            Result.success(
                NfcTagData(
                    id = tagId,
                    type = detectTagType(techList),
                    capacity = "$maxSize bytes",
                    technologies = techList,
                    textContent = textContent,
                    hexContent = hexContent,
                    atqa = atqa,
                    sak = sak,
                    isWritable = isWritable,
                    isLocked = !isWritable
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun writeTag(tag: Tag, data: WriteData): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val ndef = Ndef.get(tag)
            
            if (ndef != null) {
                try {
                    ndef.connect()
                    
                    if (!ndef.isWritable) {
                        return@withContext Result.failure(Exception("标签不可写"))
                    }
                    
                    // 创建NDEF记录
                    val ndefRecord = when (data.type) {
                        WriteType.TEXT -> createTextRecord(data.content, data.encoding)
                        WriteType.URL -> createUriRecord(data.content)
                        WriteType.FILE -> createMimeRecord(data.content, "application/octet-stream")
                        WriteType.CUSTOM -> createTextRecord(data.content, data.encoding)
                    }
                    
                    val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                    
                    // 检查容量
                    if (ndefMessage.byteArrayLength > ndef.maxSize) {
                        return@withContext Result.failure(Exception("数据超出标签容量"))
                    }
                    
                    // 写入数据
                    ndef.writeNdefMessage(ndefMessage)
                    
                    // 如果需要锁定标签
                    if (data.lockTag) {
                        ndef.makeReadOnly()
                    }
                    
                    Result.success(true)
                } finally {
                    try {
                        ndef.close()
                    } catch (e: Exception) {
                        // Ignore close exception
                    }
                }
            } else {
                // 尝试格式化标签
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    try {
                        ndefFormatable.connect()
                        
                        val ndefRecord = when (data.type) {
                            WriteType.TEXT -> createTextRecord(data.content, data.encoding)
                            WriteType.URL -> createUriRecord(data.content)
                            WriteType.FILE -> createMimeRecord(data.content, "application/octet-stream")
                            WriteType.CUSTOM -> createTextRecord(data.content, data.encoding)
                        }
                        
                        val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                        
                        if (data.lockTag) {
                            ndefFormatable.formatReadOnly(ndefMessage)
                        } else {
                            ndefFormatable.format(ndefMessage)
                        }
                        
                        Result.success(true)
                    } finally {
                        try {
                            ndefFormatable.close()
                        } catch (e: Exception) {
                            // Ignore close exception
                        }
                    }
                } else {
                    Result.failure(Exception("标签不支持NDEF格式"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun eraseTag(tag: Tag): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val ndef = Ndef.get(tag) ?: return@withContext Result.failure(
                Exception("标签不支持NDEF格式")
            )
            
            try {
                ndef.connect()
                
                if (!ndef.isWritable) {
                    return@withContext Result.failure(Exception("标签不可写"))
                }
                
                // 创建空的NDEF消息
                val emptyRecord = NdefRecord(
                    NdefRecord.TNF_EMPTY,
                    ByteArray(0),
                    ByteArray(0),
                    ByteArray(0)
                )
                val emptyMessage = NdefMessage(arrayOf(emptyRecord))
                
                ndef.writeNdefMessage(emptyMessage)
                
                Result.success(true)
            } finally {
                try {
                    ndef.close()
                } catch (e: Exception) {
                    // Ignore close exception
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun formatTag(tag: Tag): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val ndefFormatable = NdefFormatable.get(tag) ?: return@withContext Result.failure(
                Exception("标签不支持格式化")
            )
            
            try {
                ndefFormatable.connect()
                
                val emptyRecord = NdefRecord(
                    NdefRecord.TNF_EMPTY,
                    ByteArray(0),
                    ByteArray(0),
                    ByteArray(0)
                )
                val emptyMessage = NdefMessage(arrayOf(emptyRecord))
                
                ndefFormatable.format(emptyMessage)
                
                Result.success(true)
            } finally {
                try {
                    ndefFormatable.close()
                } catch (e: Exception) {
                    // Ignore close exception
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseTextRecord(payload: ByteArray): String {
        if (payload.isEmpty()) return ""
        
        val statusByte = payload[0].toInt()
        val languageCodeLength = statusByte and 0x3F
        val isUtf16 = (statusByte and 0x80) != 0
        
        val charset = if (isUtf16) Charset.forName("UTF-16") else Charset.forName("UTF-8")
        val textStartIndex = 1 + languageCodeLength
        
        return if (textStartIndex < payload.size) {
            String(payload, textStartIndex, payload.size - textStartIndex, charset)
        } else {
            ""
        }
    }
    
    private fun parseUriRecord(record: NdefRecord): String {
        val payload = record.payload
        if (payload.isEmpty()) return ""
        
        val prefixCode = payload[0].toInt()
        val prefix = URI_PREFIXES.getOrElse(prefixCode) { "" }
        val uriPart = String(payload, 1, payload.size - 1, Charset.forName("UTF-8"))
        
        return prefix + uriPart
    }
    
    private fun createTextRecord(text: String, encoding: Encoding): NdefRecord {
        val languageCode = "en"
        val languageCodeBytes = languageCode.toByteArray(Charset.forName("US-ASCII"))
        
        val charset = when (encoding) {
            Encoding.UTF_8 -> Charset.forName("UTF-8")
            Encoding.GBK -> Charset.forName("GBK")
            Encoding.ASCII -> Charset.forName("US-ASCII")
        }
        val textBytes = text.toByteArray(charset)
        
        val payload = ByteArray(1 + languageCodeBytes.size + textBytes.size)
        payload[0] = languageCodeBytes.size.toByte()
        System.arraycopy(languageCodeBytes, 0, payload, 1, languageCodeBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + languageCodeBytes.size, textBytes.size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }
    
    private fun createUriRecord(uri: String): NdefRecord {
        return NdefRecord.createUri(uri)
    }
    
    private fun createMimeRecord(content: String, mimeType: String): NdefRecord {
        return NdefRecord.createMime(mimeType, content.toByteArray(Charset.forName("UTF-8")))
    }
    
    private fun detectTagType(techList: List<String>): String {
        return when {
            techList.contains("NfcA") -> "NFC-A"
            techList.contains("NfcB") -> "NFC-B"
            techList.contains("NfcF") -> "NFC-F"
            techList.contains("NfcV") -> "NFC-V"
            else -> "Unknown"
        }
    }
    
    companion object {
        private val URI_PREFIXES = mapOf(
            0x00 to "",
            0x01 to "http://www.",
            0x02 to "https://www.",
            0x03 to "http://",
            0x04 to "https://",
            0x05 to "tel:",
            0x06 to "mailto:",
            0x07 to "ftp://anonymous:anonymous@",
            0x08 to "ftp://ftp.",
            0x09 to "ftps://",
            0x0A to "sftp://",
            0x0B to "smb://",
            0x0C to "nfs://",
            0x0D to "ftp://",
            0x0E to "dav://",
            0x0F to "news:",
            0x10 to "telnet://",
            0x11 to "imap:",
            0x12 to "rtsp://",
            0x13 to "urn:",
            0x14 to "pop:",
            0x15 to "sip:",
            0x16 to "sips:",
            0x17 to "tftp:",
            0x18 to "btspp://",
            0x19 to "btl2cap://",
            0x1A to "btgoep://",
            0x1B to "tcpobex://",
            0x1C to "irdaobex://",
            0x1D to "file://",
            0x1E to "urn:epc:id:",
            0x1F to "urn:epc:tag:",
            0x20 to "urn:epc:pat:",
            0x21 to "urn:epc:raw:",
            0x22 to "urn:epc:",
            0x23 to "urn:nfc:"
        )
    }
}

// 扩展函数：字节数组转十六进制字符串
fun ByteArray.toHexString(): String {
    return joinToString(":") { "%02X".format(it) }
}
