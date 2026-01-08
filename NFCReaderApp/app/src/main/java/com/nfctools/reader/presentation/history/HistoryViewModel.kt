package com.nfctools.reader.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.data.repository.HistoryRepository
import com.nfctools.reader.util.isThisMonth
import com.nfctools.reader.util.isThisWeek
import com.nfctools.reader.util.isToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeFilter(val label: String) {
    ALL("全部"),
    TODAY("今天"),
    WEEK("本周"),
    MONTH("本月")
}

data class HistoryUiState(
    val showFilter: Boolean = false,
    val filterType: HistoryType? = null,
    val filterTime: TimeFilter = TimeFilter.ALL,
    val selectedHistory: HistoryEntity? = null,
    val showDeleteDialog: Boolean = false,
    val showClearAllDialog: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()
    
    val historyList: StateFlow<List<HistoryEntity>> = combine(
        historyRepository.getAllHistory(),
        _uiState
    ) { list, state ->
        list.filter { history ->
            // 类型筛选
            val typeMatch = state.filterType == null || history.type == state.filterType
            
            // 时间筛选
            val timeMatch = when (state.filterTime) {
                TimeFilter.ALL -> true
                TimeFilter.TODAY -> history.timestamp.isToday()
                TimeFilter.WEEK -> history.timestamp.isThisWeek()
                TimeFilter.MONTH -> history.timestamp.isThisMonth()
            }
            
            typeMatch && timeMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun toggleFilter() {
        _uiState.update { it.copy(showFilter = !it.showFilter) }
    }
    
    fun setFilterType(type: HistoryType?) {
        _uiState.update { it.copy(filterType = type) }
    }
    
    fun setFilterTime(time: TimeFilter) {
        _uiState.update { it.copy(filterTime = time) }
    }
    
    fun showHistoryDetail(history: HistoryEntity) {
        _uiState.update { it.copy(selectedHistory = history) }
    }
    
    fun hideHistoryDetail() {
        _uiState.update { it.copy(selectedHistory = null) }
    }
    
    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
            _snackbarMessage.emit("已删除")
        }
    }
    
    fun showClearAllDialog() {
        _uiState.update { it.copy(showClearAllDialog = true) }
    }
    
    fun hideClearAllDialog() {
        _uiState.update { it.copy(showClearAllDialog = false) }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            _uiState.update { it.copy(showClearAllDialog = false) }
            _snackbarMessage.emit("历史记录已清空")
        }
    }
}
