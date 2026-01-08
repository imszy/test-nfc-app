package com.nfctools.reader.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.domain.model.Encoding
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.checkNFCStatus()
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        topBar = {
            NFCTopAppBar(
                title = "设置",
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = Spacing.large)
        ) {
            // NFC状态卡片
            item {
                NFCStatusSettingsCard(
                    nfcEnabled = uiState.nfcEnabled,
                    onOpenSettings = { viewModel.openNFCSettings() }
                )
            }
            
            // 权限管理
            item {
                SettingsSection(title = "权限管理") {
                    PermissionItem(
                        title = "NFC权限",
                        description = "允许应用使用NFC功能",
                        enabled = uiState.nfcPermissionGranted
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    PermissionItem(
                        title = "存储权限",
                        description = "用于保存历史记录",
                        enabled = uiState.storagePermissionGranted
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    PermissionItem(
                        title = "通知权限",
                        description = "接收读写操作通知",
                        enabled = uiState.notificationPermissionGranted
                    )
                }
            }
            
            // 应用设置
            item {
                SettingsSection(title = "应用设置") {
                    SwitchItem(
                        title = "自动读取",
                        checked = preferences.autoRead,
                        onCheckedChange = { viewModel.setAutoRead(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    SwitchItem(
                        title = "振动反馈",
                        checked = preferences.vibrationFeedback,
                        onCheckedChange = { viewModel.setVibrationFeedback(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    SwitchItem(
                        title = "声音提示",
                        checked = preferences.soundFeedback,
                        onCheckedChange = { viewModel.setSoundFeedback(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    
                    EncodingDropdownItem(
                        selectedEncoding = preferences.defaultEncoding,
                        onEncodingSelected = { viewModel.setDefaultEncoding(it) }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    
                    RetentionDaysItem(
                        days = preferences.historyRetentionDays,
                        onDaysChanged = { viewModel.setHistoryRetentionDays(it) }
                    )
                }
            }
            
            // 关于应用
            item {
                SettingsSection(title = "关于应用") {
                    InfoItem(label = "应用版本", value = "v1.0.0")
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    InfoItem(label = "构建日期", value = "2025-01-08")
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    InfoItem(label = "开发者", value = "NFC Tools Team")
                }
            }
            
            // 操作按钮
            item {
                Column(
                    modifier = Modifier.padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.openHelp() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Help,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text("帮助与反馈")
                    }
                    
                    Button(
                        onClick = { viewModel.showClearHistoryDialog() },
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
                        Text("清除历史记录")
                    }
                }
            }
        }
    }
    
    // 清除历史确认对话框
    if (uiState.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearHistoryDialog() },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text("清除历史记录") },
            text = { Text("确定要清除所有历史记录吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearHistoryDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun NFCStatusSettingsCard(
    nfcEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.small),
        shape = RoundedCornerShape(Radius.card)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (nfcEnabled)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NFC状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (nfcEnabled) "已就绪" else "未开启",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!nfcEnabled) {
                Button(onClick = onOpenSettings) {
                    Text("开启NFC")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = Spacing.small)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = Spacing.large, vertical = Spacing.small)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large),
            shape = RoundedCornerShape(Radius.card)
        ) {
            Column(modifier = Modifier.padding(vertical = Spacing.small)) {
                content()
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = if (enabled) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
            contentDescription = null,
            tint = if (enabled)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncodingDropdownItem(
    selectedEncoding: Encoding,
    onEncodingSelected: (Encoding) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "默认编码",
            style = MaterialTheme.typography.bodyMedium
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Surface(
                modifier = Modifier.menuAnchor(),
                shape = RoundedCornerShape(Radius.button),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedEncoding.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Encoding.entries.forEach { encoding ->
                    DropdownMenuItem(
                        text = { Text(encoding.name) },
                        onClick = {
                            onEncodingSelected(encoding)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RetentionDaysItem(
    days: Int,
    onDaysChanged: (Int) -> Unit
) {
    val options = listOf(7, 14, 30, 60, 90)
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "历史记录保留天数",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(Radius.button),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${days}天",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option}天") },
                        onClick = {
                            onDaysChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
