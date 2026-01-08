package com.nfctools.reader.presentation.home

import android.app.Application
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val nfcEnabled: Boolean = false,
    val nfcSupported: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(application)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        checkNFCStatus()
    }
    
    fun checkNFCStatus() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                nfcEnabled = nfcAdapter?.isEnabled == true,
                nfcSupported = nfcAdapter != null
            )
        }
    }
    
    fun toggleNFC() {
        if (nfcAdapter?.isEnabled != true) {
            val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        }
    }
    
    fun onNFCStateChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(nfcEnabled = enabled)
    }
}
