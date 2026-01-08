package com.nfctools.reader.presentation.settings

import android.app.Application
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfctools.reader.data.preferences.PreferencesManager
import com.nfctools.reader.data.preferences.UserPreferences
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.domain.model.Encoding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val nfcEnabled: Boolean = false,
    val nfcSupported: Boolean = true,
    val nfcPermissionGranted: Boolean = true,
    val storagePermissionGranted: Boolean = true,
    val notificationPermissionGranted: Boolean = true,
    val showClearHistoryDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val preferencesManager: PreferencesManager,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {
    
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(application)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    val preferences: StateFlow<UserPreferences> = preferencesManager.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )
    
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()
    
    init {
        checkNFCStatus()
    }
    
    fun checkNFCStatus() {
        _uiState.update {
            it.copy(
                nfcEnabled = nfcAdapter?.isEnabled == true,
                nfcSupported = nfcAdapter != null
            )
        }
    }
    
    fun openNFCSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }
    
    fun setAutoRead(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoRead(enabled)
        }
    }
    
    fun setVibrationFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVibrationFeedback(enabled)
        }
    }
    
    fun setSoundFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundFeedback(enabled)
        }
    }
    
    fun setDefaultEncoding(encoding: Encoding) {
        viewModelScope.launch {
            preferencesManager.setDefaultEncoding(encoding)
        }
    }
    
    fun setHistoryRetentionDays(days: Int) {
        viewModelScope.launch {
            preferencesManager.setHistoryRetentionDays(days)
        }
    }
    
    fun showClearHistoryDialog() {
        _uiState.update { it.copy(showClearHistoryDialog = true) }
    }
    
    fun hideClearHistoryDialog() {
        _uiState.update { it.copy(showClearHistoryDialog = false) }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            _uiState.update { it.copy(showClearHistoryDialog = false) }
            _snackbarMessage.emit("历史记录已清空")
        }
    }
    
    fun openHelp() {
        // TODO: 打开帮助页面或发送反馈邮件
        viewModelScope.launch {
            _snackbarMessage.emit("帮助功能即将上线")
        }
    }
}
