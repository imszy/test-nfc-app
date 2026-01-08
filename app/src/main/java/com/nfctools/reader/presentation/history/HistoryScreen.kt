package com.nfctools.reader.presentation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.data.local.entity.HistoryEntity
import com.nfctools.reader.data.local.entity.HistoryType
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*
import com.nfctools.reader.util.toDateGroup
import com.nfctools.reader.util.toTimeString
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        topBar = {
            NFCTopAppBar(
                title = "历史记录",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilter() }) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = "筛选"
                        )
                    }
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = { viewModel.showClearAllDialog() }) {
                            Icon(
                                Icons.Outlined.DeleteSweep,
                                contentDescription = "清空"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选栏（可折叠）
            AnimatedVisibility(visible = uiState.showFilter) {
                FilterBar(
                    selectedType = uiState.filterType,
                    selectedTime = uiState.filterTime,
                    onTypeSelected = { viewModel.setFilterType(it) },
                    onTimeSelected = { viewModel.setFilterTime(it) }
                )
            }
            
            // 历史列表
            if (historyList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.History,
                    message = "暂无历史记录"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.large, vertical = Spacing.small)
                ) {
                    // 按日期分组
                    val groupedHistory = historyList.groupBy { 
                        it.timestamp.toDateGroup() 
                    }
                    
                    groupedHistory.forEach { (dateGroup, items) ->
                        item(key = "header_$dateGroup") {
                            Text(
                                text = dateGroup,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = Spacing.small)
                            )
                        }
                        
                        items(
                            items = items,
                            key = { it.id }
                        ) { history ->
                            HistoryItem(
                                history = history,
                                onClick = { viewModel.showHistoryDetail(history) },
                                onDelete = { viewModel.deleteHistory(history.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 历史详情对话框
    uiState.selectedHistory?.let { history ->
        HistoryDetailDialog(
            history = history,
            onDismiss = { viewModel.hideHistoryDetail() },
            onDelete = {
                viewModel.deleteHistory(history.id)
                viewModel.hideHistoryDetail()
            }
        )
    }
    
    // 清空确认对话框
    if (uiState.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearAllDialog() },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text("清空历史记录") },
            text = { Text("确定要清空所有历史记录吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearAllDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun FilterBar(
    selectedType: HistoryType?,
    selectedTime: TimeFilter,
    onTypeSelected: (HistoryType?) -> Unit,
    onTimeSelected: (TimeFilter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        shape = RoundedCornerShape(Radius.card)
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            // 类型筛选
            Text(
                text = "类型",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = Spacing.small)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = selectedType == HistoryType.READ,
                    onClick = { onTypeSelected(HistoryType.READ) },
                    label = { Text("读取") },
                    leadingIcon = if (selectedType == HistoryType.READ) {
                        { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedType == HistoryType.WRITE,
                    onClick = { onTypeSelected(HistoryType.WRITE) },
                    label = { Text("写入") },
                    leadingIcon = if (selectedType == HistoryType.WRITE) {
                        { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.large))
            
            // 时间筛选
            Text(
                text = "时间",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = Spacing.small)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                TimeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedTime == filter,
                        onClick = { onTimeSelected(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    history: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (history.type == HistoryType.READ)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    SecondaryContainer
            ) {
                Icon(
                    imageVector = if (history.type == HistoryType.READ)
                        Icons.Outlined.Label
                    else
                        Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(Spacing.small),
                    tint = if (history.type == HistoryType.READ)
                        MaterialTheme.colorScheme.primary
                    else
                        Secondary
                )
            }
            
            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.tagId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = history.content.ifEmpty { "无内容" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 时间
            Text(
                text = history.timestamp.toTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 删除按钮
            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun HistoryDetailDialog(
    history: HistoryEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Icon(
                    imageVector = if (history.type == HistoryType.READ)
                        Icons.Outlined.Label
                    else
                        Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = if (history.type == HistoryType.READ)
                        MaterialTheme.colorScheme.primary
                    else
                        Secondary
                )
                Text(
                    text = if (history.type == HistoryType.READ) "读取记录" else "写入记录"
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                DetailRow("标签ID", history.tagId)
                DetailRow("内容类型", history.contentType.name)
                if (history.content.isNotEmpty()) {
                    DetailRow("内容", history.content)
                }
                history.tagType?.let { DetailRow("标签类型", it) }
                history.capacity?.let { DetailRow("容量", it) }
                DetailRow("时间", history.timestamp.toTimeString())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}
