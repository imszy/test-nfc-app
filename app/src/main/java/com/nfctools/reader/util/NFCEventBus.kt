package com.nfctools.reader.util

import android.nfc.Tag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NFCTagEvent(val tag: Tag)

object NFCEventBus {
    private val _tagEvents = MutableSharedFlow<NFCTagEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tagEvents: SharedFlow<NFCTagEvent> = _tagEvents.asSharedFlow()
    
    fun post(event: NFCTagEvent) {
        _tagEvents.tryEmit(event)
    }
}
