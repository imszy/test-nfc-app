package com.nfctools.reader.presentation.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.domain.model.Encoding
import com.nfctools.reader.domain.model.WriteType
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WriteScreen(
    navController: NavController,
    initialMode: String = "text",
    viewModel: WriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(initialMode) {
        val mode = when (initialMode.lowercase()) {
            "url" -> WriteType.URL
            "file" -> WriteType.FILE
            "custom" -> WriteType.CUSTOM
            else -> WriteType.TEXT
        }
        viewModel.setWriteMode(mode)
    }
    
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        topBar = {
            NFCTopAppBar(
                title = "写入数据",
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 模式选择器（分段控件）
            SegmentedControl(
                selectedMode = uiState.writeMode,
                onModeSelected = { viewModel.setWriteMode(it) }
            )
            
            // 动态表单区域
            when (uiState.writeMode) {
                WriteType.TEXT -> TextModeForm(
                    textContent = uiState.textContent,
                    encoding = uiState.encoding,
                    onTextChange = { viewModel.setTextContent(it) },
                    onEncodingChange = { viewModel.setEncoding(it) }
                )
                WriteType.URL -> UrlModeForm(
                    urlContent = uiState.urlContent,
                    onUrlChange = { viewModel.setUrlContent(it) }
                )
                WriteType.FILE -> FileModeForm(
                    fileContent = uiState.fileContent,
                    onFileSelect = { viewModel.setFileContent(it) }
                )
                WriteType.CUSTOM -> CustomModeForm(
                    customContent = uiState.customContent,
                    onContentChange = { viewModel.setCustomContent(it) }
                )
            }
            
            // 写入选项
            WriteOptionsCard(
                lockTag = uiState.lockTag,
                onLockTagChanged = { viewModel.setLockTag(it) }
            )
            
            // 写入动画（写入时显示）
            if (uiState.isWaitingForTag || uiState.isWriting || uiState.writeSuccess) {
                WriteAnimation(
                    status = uiState.writeStatus,
                    progress = uiState.writeProgress
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 写入按钮
            Column(
                modifier = Modifier.padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                if (uiState.isWaitingForTag) {
                    OutlinedButton(
                        onClick = { viewModel.cancelWrite() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("取消")
                    }
                } else if (uiState.writeSuccess) {
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text("继续写入")
                    }
                } else {
                    Button(
                        onClick = { viewModel.startWrite() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isWriting && uiState.canWrite,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardGreen
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = when {
                                uiState.isWriting -> uiState.writeStatus
                                else -> "开始写入"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    selectedMode: WriteType,
    onModeSelected: (WriteType) -> Unit
) {
    val modes = listOf(
        WriteType.TEXT to "文本",
        WriteType.URL to "链接",
        WriteType.FILE to "文件",
        WriteType.CUSTOM to "自定义"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Radius.button)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            modes.forEach { (mode, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (selectedMode == mode)
                                MaterialTheme.colorScheme.surface
                            else
                                Color.Transparent
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedMode == mode)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TextModeForm(
    textContent: String,
    encoding: Encoding,
    onTextChange: (String) -> Unit,
    onEncodingChange: (Encoding) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(horizontal = Spacing.large)) {
        Text(
            text = "输入文本内容",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        OutlinedTextField(
            value = textContent,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("请输入要写入的文本内容...") },
            maxLines = 8
        )
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        Text(
            text = "字符编码",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = encoding.name,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Encoding.entries.forEach { enc ->
                    DropdownMenuItem(
                        text = { Text(enc.name) },
                        onClick = {
                            onEncodingChange(enc)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UrlModeForm(
    urlContent: String,
    onUrlChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.large)) {
        Text(
            text = "输入网址",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        OutlinedTextField(
            value = urlContent,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://example.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        // URL预览卡片
        if (urlContent.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(Spacing.large)) {
                    Text(
                        text = "链接预览",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = Spacing.small)
                    )
                    Text(
                        text = urlContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun FileModeForm(
    fileContent: String,
    onFileSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.large)) {
        Text(
            text = "选择文件",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: 打开文件选择器 */ },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.medium))
                Text(
                    text = if (fileContent.isEmpty()) "点击选择文件" else "已选择文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (fileContent.isNotEmpty()) {
                    Text(
                        text = fileContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Text(
            text = "或手动输入文件内容（Base64编码）",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = Spacing.small)
        )
        
        OutlinedTextField(
            value = fileContent,
            onValueChange = onFileSelect,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("Base64编码的文件内容...") },
            maxLines = 4
        )
    }
}

@Composable
private fun CustomModeForm(
    customContent: String,
    onContentChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.large)) {
        Text(
            text = "自定义数据（十六进制）",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Spacing.small)
        )
        
        OutlinedTextField(
            value = customContent,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("输入十六进制数据，如: 48 65 6C 6C 6F") },
            maxLines = 8
        )
        
        Spacer(modifier = Modifier.height(Spacing.small))
        
        Text(
            text = "提示: 使用空格分隔每个字节",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WriteOptionsCard(
    lockTag: Boolean,
    onLockTagChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        shape = RoundedCornerShape(Radius.card)
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(
                text = "写入选项",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "锁定标签",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "锁定后标签将无法再次写入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = lockTag,
                    onCheckedChange = onLockTagChanged
                )
            }
            
            if (lockTag) {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(Radius.button)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = "警告：锁定后无法撤销！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
