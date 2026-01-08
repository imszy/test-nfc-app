package com.nfctools.reader.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nfctools.reader.presentation.components.*
import com.nfctools.reader.presentation.theme.*

data class FunctionCardData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color,
    val route: String
)

val functionCards = listOf(
    FunctionCardData(
        icon = Icons.Outlined.Label,
        title = "读取标签",
        description = "扫描NFC标签信息",
        color = CardBlue,
        route = "read"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Edit,
        title = "写入文本",
        description = "向标签写入文本",
        color = CardGreen,
        route = "write?mode=text"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Link,
        title = "写入URL",
        description = "向标签写入链接",
        color = CardOrange,
        route = "write?mode=url"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Description,
        title = "写入文件",
        description = "向标签写入文件数据",
        color = CardPurple,
        route = "write?mode=file"
    ),
    FunctionCardData(
        icon = Icons.Outlined.History,
        title = "历史记录",
        description = "查看操作历史",
        color = CardGray,
        route = "history"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Settings,
        title = "应用设置",
        description = "权限与配置",
        color = CardDarkBlue,
        route = "settings"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.checkNFCStatus()
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("NFC读写工具") },
                navigationIcon = {
                    IconButton(onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("菜单功能即将上线")
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "菜单")
                    }
                },
                actions = {
                    // NFC状态指示器
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (uiState.nfcEnabled) 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Nfc,
                                contentDescription = "NFC状态",
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.nfcEnabled) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (uiState.nfcEnabled) "ON" else "OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.nfcEnabled) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // NFC状态卡片
                NFCStatusCard(
                    enabled = uiState.nfcEnabled,
                    onToggle = { viewModel.toggleNFC() }
                )
                
                // 功能网格 (2列)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.large),
                    modifier = Modifier.weight(1f)
                ) {
                    items(functionCards) { card ->
                        FunctionCard(
                            icon = card.icon,
                            title = card.title,
                            description = card.description,
                            color = card.color,
                            onClick = { navController.navigate(card.route) }
                        )
                    }
                }
            }
            
            // 底部提示 - 固定在底部
            BottomActionBar(
                text = "将手机靠近NFC标签开始读取",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
