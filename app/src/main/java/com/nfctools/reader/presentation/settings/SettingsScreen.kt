package com.nfctools.reader.presentation.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.R
import com.nfctools.reader.domain.model.Encoding
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*
import com.nfctools.reader.util.PlayStoreUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
            
            // 隐私与法律
            item {
                SettingsSection(title = "隐私与法律") {
                    ClickableItem(
                        title = stringResource(R.string.privacy_policy),
                        icon = Icons.Outlined.PrivacyTip,
                        onClick = {
                            PlayStoreUtils.openPrivacyPolicy(
                                context,
                                context.getString(R.string.privacy_policy_url)
                            )
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = Spacing.large))
                    ClickableItem(
                        title = stringResource(R.string.terms_of_service),
                        icon = Icons.Outlined.Description,
                        onClick = {
                            PlayStoreUtils.openTermsOfService(
                                context,
                                context.getString(R.string.terms_url)
                            )
                        }
                    )
                }
            }
            
            // 数据安全说明
            item {
                DataSafetyCard()
            }
            
            // 操作按钮
            item {
                Column(
                    modifier = Modifier.padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    // 为应用评分
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                (context as? Activity)?.let { activity ->
                                    PlayStoreUtils.requestInAppReview(activity)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(stringResource(R.string.rate_app))
                    }
                    
                    // 分享应用
                    OutlinedButton(
                        onClick = { PlayStoreUtils.shareApp(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(stringResource(R.string.share_app))
                    }
                    
                    // 帮助与反馈
                    OutlinedButton(
                        onClick = { PlayStoreUtils.sendFeedback(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Help,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(stringResource(R.string.help_feedback))
                    }
                    
                    // 清除历史记录
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
                        Text(stringResource(R.string.clear_history))
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

@Composable
private fun ClickableItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DataSafetyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large, vertical = Spacing.small),
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "数据安全",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            DataSafetyItem(
                icon = Icons.Outlined.PersonOff,
                text = stringResource(R.string.no_account_required)
            )
            DataSafetyItem(
                icon = Icons.Outlined.PhoneAndroid,
                text = stringResource(R.string.data_stored_locally)
            )
            DataSafetyItem(
                icon = Icons.Outlined.Lock,
                text = stringResource(R.string.no_data_shared)
            )
        }
    }
}

@Composable
private fun DataSafetyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
