package com.nfctools.reader.domain.usecase

import android.nfc.Tag
import com.nfctools.reader.data.local.entity.ContentType
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.data.repository.NfcRepository
import com.nfctools.reader.domain.model.NfcTagData
import javax.inject.Inject

class ReadTagUseCase @Inject constructor(
    private val nfcRepository: NfcRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(tag: Tag, autoSave: Boolean = true): Result<NfcTagData> {
        return nfcRepository.readTag(tag).also { result ->
            if (result.isSuccess && autoSave) {
                val tagData = result.getOrNull()!!
                historyRepository.insertHistory(
                    HistoryEntity(
                        type = HistoryType.READ,
                        tagId = tagData.id,
                        content = tagData.textContent ?: tagData.hexContent ?: "",
                        contentType = if (tagData.textContent != null) ContentType.TEXT else ContentType.HEX,
                        timestamp = System.currentTimeMillis(),
                        tagType = tagData.type,
                        capacity = tagData.capacity
                    )
                )
            }
        }
    }
}
