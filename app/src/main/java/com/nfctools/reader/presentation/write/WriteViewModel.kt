package com.nfctools.reader.presentation.write

import android.app.Application
import android.content.Context
import android.nfc.Tag
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfctools.reader.data.preferences.PreferencesManager
import com.nfctools.reader.domain.model.Encoding
import com.nfctools.reader.domain.model.WriteData
import com.nfctools.reader.domain.model.WriteType
import com.nfctools.reader.domain.usecase.WriteTagUseCase
import com.nfctools.reader.util.NFCEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteUiState(
    val writeMode: WriteType = WriteType.TEXT,
    val textContent: String = "",
    val urlContent: String = "",
    val fileContent: String = "",
    val customContent: String = "",
    val encoding: Encoding = Encoding.UTF_8,
    val lockTag: Boolean = false,
    val password: String = "",
    val isWriting: Boolean = false,
    val isWaitingForTag: Boolean = false,
    val writeStatus: String = "",
    val writeProgress: Float = 0f,
    val canWrite: Boolean = false,
    val writeSuccess: Boolean = false
)

@HiltViewModel
class WriteViewModel @Inject constructor(
    application: Application,
    private val writeTagUseCase: WriteTagUseCase,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()
    
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()
    
    private val preferences = preferencesManager.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        // 设置默认编码（只在初始化时设置一次）
        viewModelScope.launch {
            preferencesManager.preferences.first().let { prefs ->
                _uiState.update { it.copy(encoding = prefs.defaultEncoding) }
            }
        }
        
        // 监听NFC标签事件
        viewModelScope.launch {
            NFCEventBus.tagEvents.collect { event ->
                if (_uiState.value.isWaitingForTag) {
                    onTagDetected(event.tag)
                }
            }
        }
    }
    
    fun setWriteMode(mode: WriteType) {
        _uiState.update { it.copy(writeMode = mode) }
        validateCanWrite()
    }
    
    fun setTextContent(text: String) {
        _uiState.update { it.copy(textContent = text) }
        validateCanWrite()
    }
    
    fun setUrlContent(url: String) {
        _uiState.update { it.copy(urlContent = url) }
        validateCanWrite()
    }
    
    fun setFileContent(content: String) {
        _uiState.update { it.copy(fileContent = content) }
        validateCanWrite()
    }
    
    fun setCustomContent(content: String) {
        _uiState.update { it.copy(customContent = content) }
        validateCanWrite()
    }
    
    fun setEncoding(encoding: Encoding) {
        _uiState.update { it.copy(encoding = encoding) }
    }
    
    fun setLockTag(lock: Boolean) {
        _uiState.update { it.copy(lockTag = lock) }
    }
    
    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }
    
    fun startWrite() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isWaitingForTag = true,
                    isWriting = false,
                    writeStatus = "请将手机靠近标签",
                    writeProgress = 0f,
                    writeSuccess = false
                )
            }
        }
    }
    
    fun cancelWrite() {
        _uiState.update {
            it.copy(
                isWaitingForTag = false,
                isWriting = false,
                writeStatus = "",
                writeProgress = 0f
            )
        }
    }
    
    private fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isWaitingForTag = false,
                    isWriting = true,
                    writeStatus = "正在写入..."
                )
            }
            
            // 触发振动反馈
            if (preferences.value?.vibrationFeedback == true) {
                vibrate()
            }
            
            // 模拟写入进度
            for (i in 1..5) {
                delay(100)
                _uiState.update { it.copy(writeProgress = i / 10f) }
            }
            
            val writeData = createWriteData()
            
            writeTagUseCase(tag, writeData)
                .onSuccess {
                    // 完成进度
                    for (i in 6..10) {
                        delay(50)
                        _uiState.update { it.copy(writeProgress = i / 10f) }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            writeStatus = "写入成功！",
                            writeProgress = 1f,
                            writeSuccess = true,
                            isWriting = false
                        )
                    }
                    _snackbarMessage.emit("数据已成功写入标签")
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            writeStatus = "写入失败",
                            isWriting = false,
                            writeSuccess = false
                        )
                    }
                    _snackbarMessage.emit("写入失败: ${exception.message}")
                }
        }
    }
    
    private fun createWriteData(): WriteData {
        val state = _uiState.value
        val content = when (state.writeMode) {
            WriteType.TEXT -> state.textContent
            WriteType.URL -> state.urlContent
            WriteType.FILE -> state.fileContent
            WriteType.CUSTOM -> state.customContent
        }
        
        return WriteData(
            type = state.writeMode,
            content = content,
            encoding = state.encoding,
            lockTag = state.lockTag,
            password = state.password.takeIf { it.isNotEmpty() }
        )
    }
    
    private fun validateCanWrite() {
        val state = _uiState.value
        val canWrite = when (state.writeMode) {
            WriteType.TEXT -> state.textContent.isNotEmpty()
            WriteType.URL -> state.urlContent.isNotEmpty() && isValidUrl(state.urlContent)
            WriteType.FILE -> state.fileContent.isNotEmpty()
            WriteType.CUSTOM -> state.customContent.isNotEmpty()
        }
        _uiState.update { it.copy(canWrite = canWrite) }
    }
    
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://") || 
               url.startsWith("tel:") || url.startsWith("mailto:")
    }
    
    fun resetState() {
        // 只重置写入状态，保留用户输入的内容
        _uiState.update {
            it.copy(
                isWaitingForTag = false,
                isWriting = false,
                writeStatus = "",
                writeProgress = 0f,
                writeSuccess = false
            )
        }
    }
    
    fun clearAndReset() {
        // 完全清空所有内容并重置状态
        _uiState.update {
            WriteUiState(
                encoding = preferences.value?.defaultEncoding ?: Encoding.UTF_8
            )
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
