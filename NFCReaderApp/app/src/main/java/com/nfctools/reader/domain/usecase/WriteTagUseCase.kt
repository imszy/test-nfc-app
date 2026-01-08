package com.nfctools.reader.domain.usecase

import android.nfc.Tag
import com.nfctools.reader.data.local.entity.ContentType
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.data.repository.NfcRepository
import com.nfctools.reader.domain.model.WriteData
import com.nfctools.reader.domain.model.WriteType
import javax.inject.Inject

class WriteTagUseCase @Inject constructor(
    private val nfcRepository: NfcRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(tag: Tag, data: WriteData): Result<Boolean> {
        return nfcRepository.writeTag(tag, data).also { result ->
            if (result.isSuccess) {
                val tagId = tag.id.joinToString(":") { "%02X".format(it) }
                historyRepository.insertHistory(
                    HistoryEntity(
                        type = HistoryType.WRITE,
                        tagId = tagId,
                        content = data.content,
                        contentType = when (data.type) {
                            WriteType.TEXT -> ContentType.TEXT
                            WriteType.URL -> ContentType.URL
                            WriteType.FILE -> ContentType.FILE
                            WriteType.CUSTOM -> ContentType.HEX
                        },
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
