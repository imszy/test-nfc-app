package com.nfctools.reader.presentation.read

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfctools.reader.data.local.entity.ContentType
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.data.preferences.PreferencesManager
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.data.repository.NfcRepository
import com.nfctools.reader.domain.model.NfcTagData
import com.nfctools.reader.domain.usecase.ReadTagUseCase
import com.nfctools.reader.util.NFCEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReadUiState(
    val isScanning: Boolean = true,
    val tagData: NfcTagData? = null,
    val currentTag: Tag? = null,  // 保存当前标签引用，用于擦除操作
    val selectedTab: Int = 0,
    val error: String? = null,
    val showEraseDialog: Boolean = false
)

@HiltViewModel
class ReadViewModel @Inject constructor(
    application: Application,
    private val readTagUseCase: ReadTagUseCase,
    private val nfcRepository: NfcRepository,
    private val historyRepository: HistoryRepository,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ReadUiState())
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()
    
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()
    
    private val preferences = preferencesManager.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        // 监听NFC标签事件
        viewModelScope.launch {
            NFCEventBus.tagEvents.collect { event ->
                onTagDetected(event.tag)
            }
        }
    }
    
    fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            
            // 触发振动反馈
            if (preferences.value?.vibrationFeedback == true) {
                vibrate()
            }
            
            readTagUseCase(tag, autoSave = preferences.value?.autoRead ?: true)
                .onSuccess { tagData ->
                    _uiState.update { 
                        it.copy(
                            isScanning = false,
                            tagData = tagData,
                            currentTag = tag,  // 保存当前标签引用
                            error = null
                        )
                    }
                    _snackbarMessage.emit("成功读取NFC标签")
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            error = exception.message ?: "读取失败"
                        )
                    }
                    _snackbarMessage.emit("读取失败: ${exception.message}")
                }
        }
    }
    
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
    
    fun copyToClipboard(text: String) {
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("NFC Data", text)
        clipboard.setPrimaryClip(clip)
        
        viewModelScope.launch {
            _snackbarMessage.emit("已复制到剪贴板")
        }
    }
    
    fun saveToHistory() {
        viewModelScope.launch {
            uiState.value.tagData?.let { tagData ->
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
                _snackbarMessage.emit("已保存到历史记录")
            }
        }
    }
    
    fun shareData() {
        uiState.value.tagData?.let { tagData ->
            val shareText = buildString {
                appendLine("NFC标签信息")
                appendLine("标签ID: ${tagData.id}")
                appendLine("类型: ${tagData.type}")
                appendLine("容量: ${tagData.capacity}")
                if (!tagData.textContent.isNullOrEmpty()) {
                    appendLine("内容: ${tagData.textContent}")
                }
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            getApplication<Application>().startActivity(
                Intent.createChooser(intent, "分享NFC标签信息").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }
    
    fun showEraseDialog() {
        _uiState.update { it.copy(showEraseDialog = true) }
    }
    
    fun hideEraseDialog() {
        _uiState.update { it.copy(showEraseDialog = false) }
    }
    
    fun eraseCurrentTag() {
        val tag = _uiState.value.currentTag
        if (tag == null) {
            viewModelScope.launch {
                _uiState.update { it.copy(showEraseDialog = false) }
                _snackbarMessage.emit("擦除失败: 请重新扫描标签")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(showEraseDialog = false) }
            
            nfcRepository.eraseTag(tag)
                .onSuccess {
                    _snackbarMessage.emit("标签已擦除")
                    resetToScanning()
                }
                .onFailure { exception ->
                    _snackbarMessage.emit("擦除失败: ${exception.message}")
                }
        }
    }
    
    fun resetToScanning() {
        _uiState.update { 
            ReadUiState(isScanning = true)
        }
    }
    
    private fun vibrate() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }
}
