package com.nfctools.reader.data.repository

import android.nfc.Tag
import com.nfctools.reader.domain.model.NfcTagData
import com.nfctools.reader.domain.model.WriteData

interface NfcRepository {
    suspend fun readTag(tag: Tag): Result<NfcTagData>
    suspend fun writeTag(tag: Tag, data: WriteData): Result<Boolean>
    suspend fun eraseTag(tag: Tag): Result<Boolean>
    suspend fun formatTag(tag: Tag): Result<Boolean>
}
