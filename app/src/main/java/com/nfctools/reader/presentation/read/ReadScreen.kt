package com.nfctools.reader.presentation.read

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.domain.model.NfcTagData
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReadScreen(
    navController: NavController,
    viewModel: ReadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        topBar = {
            NFCTopAppBar(
                title = "读取标签",
                onBackClick = { navController.popBackStack() },
                actions = {
                    if (uiState.tagData != null) {
                        IconButton(onClick = { viewModel.shareData() }) {
                            Icon(Icons.Outlined.Share, contentDescription = "分享")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isScanning -> {
                    ScanningAnimation()
                }
                uiState.tagData != null -> {
                    TagDataView(
                        tagData = uiState.tagData!!,
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        onCopy = { text -> viewModel.copyToClipboard(text) },
                        onSave = { viewModel.saveToHistory() },
                        onShare = { viewModel.shareData() },
                        onErase = { viewModel.showEraseDialog() }
                    )
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = { viewModel.resetToScanning() }
                    )
                }
            }
        }
    }
    
    // 擦除确认对话框
    if (uiState.showEraseDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEraseDialog() },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text("擦除标签") },
            text = { Text("确定要擦除此标签的所有数据吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eraseCurrentTag() },  // 修复：调用擦除方法
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("擦除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideEraseDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TagDataView(
    tagData: NfcTagData,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCopy: (String) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onErase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 标签信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            shape = RoundedCornerShape(Radius.card)
        ) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                Text(
                    text = "标签信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(Spacing.large))
                
                // 标签ID
                TagInfoRow(
                    label = "标签ID",
                    value = tagData.id,
                    showCopyButton = true,
                    onCopy = { onCopy(tagData.id) }
                )
                
                Divider(modifier = Modifier.padding(vertical = Spacing.small))
                
                // 标签类型
                TagInfoRow(
                    label = "标签类型",
                    value = tagData.type,
                    isBadge = true
                )
                
                Divider(modifier = Modifier.padding(vertical = Spacing.small))
                
                // 容量信息
                TagInfoRow(
                    label = "容量",
                    value = tagData.capacity
                )
                
                Divider(modifier = Modifier.padding(vertical = Spacing.small))
                
                // 可写状态
                TagInfoRow(
                    label = "状态",
                    value = if (tagData.isWritable) "可写" else "只读",
                    isBadge = true
                )
            }
        }
        
        // 选项卡
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("文本") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("十六进制") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                text = { Text("技术详情") }
            )
        }
        
        // 选项卡内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            when (selectedTab) {
                0 -> TextContentView(
                    content = tagData.textContent ?: "无文本内容",
                    onCopy = { onCopy(tagData.textContent ?: "") }
                )
                1 -> HexContentView(
                    content = tagData.hexContent ?: "无数据",
                    onCopy = { onCopy(tagData.hexContent ?: "") }
                )
                2 -> TechnicalDetailsView(tagData)
            }
        }
        
        // 操作按钮
        Column(
            modifier = Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text("保存到历史")
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text("分享")
                }
            }
            
            Button(
                onClick = onErase,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("擦除标签")
            }
        }
    }
}

@Composable
private fun TextContentView(
    content: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文本内容",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HexContentView(
    content: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "十六进制数据",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun TechnicalDetailsView(tagData: NfcTagData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "技术参数",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // 支持的技术
            Text(
                text = "支持的技术",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(vertical = Spacing.small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                tagData.technologies.forEach { tech ->
                    Surface(
                        shape = RoundedCornerShape(Radius.chip),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = tech,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // ATQA
            if (tagData.atqa != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ATQA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tagData.atqa,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
            
            // SAK
            if (tagData.sak != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SAK",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "0x${tagData.sak}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
