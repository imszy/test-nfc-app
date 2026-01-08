# NFC读写工具 Android App 开发规格说明书

## 项目概述

### 应用名称
NFC读写工具 (NFC Reader & Writer)

### 应用简介
一款功能完整的Android NFC读写工具，支持读取NFC标签信息、写入多种数据类型（文本、URL、文件）、历史记录管理等核心功能。

### 目标平台
- **最低SDK版本**: Android 5.0 (API 21)
- **目标SDK版本**: Android 14 (API 34)
- **必需硬件**: NFC芯片
- **开发语言**: Kotlin
- **架构模式**: MVVM + Clean Architecture

---

## 技术栈规范

### 核心框架
- **UI框架**: Jetpack Compose (推荐) 或 XML布局
- **架构组件**: 
  - ViewModel
  - LiveData / StateFlow
  - Room Database (历史记录存储)
  - Navigation Component
- **NFC**: Android NFC API (android.nfc.*)
- **依赖注入**: Hilt / Koin
- **异步处理**: Kotlin Coroutines + Flow

### 推荐第三方库
```gradle
// UI
implementation "androidx.compose.ui:ui:1.5.4"
implementation "androidx.compose.material3:material3:1.2.0"

// Architecture
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
implementation "androidx.navigation:navigation-compose:2.7.6"

// Database
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// Dependency Injection
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// NFC
// 使用系统内置 android.nfc 包
```

---

## 设计规范

### Material Design 3 主题配置

#### 颜色方案
```kotlin
// Theme.kt
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),        // 科技蓝
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF1976D2),
    
    secondary = Color(0xFF4CAF50),       // 成功绿
    tertiary = Color(0xFFFF9800),        // 警告橙
    error = Color(0xFFF44336),           // 错误红
    
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    onSurfaceVariant = Color(0xFF757575),
    
    outline = Color(0xFFE0E0E0),
)
```

#### 自定义颜色扩展
```kotlin
val AccentPurple = Color(0xFF9C27B0)
val AccentGray = Color(0xFF757575)
```

#### 字体规范
```kotlin
val Typography = Typography(
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
)
```

#### 圆角规范
- **卡片**: 12.dp
- **按钮**: 8.dp
- **筛选标签**: 16.dp
- **对话框**: 16.dp
- **头像/图标容器**: 50%圆角（圆形）

#### 间距规范
```kotlin
object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val huge = 32.dp
}
```

---

## 应用架构

### 项目结构
```
app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── HistoryDao.kt
│   │   ├── entity/
│   │   │   └── HistoryEntity.kt
│   │   └── database/
│   │       └── AppDatabase.kt
│   ├── repository/
│   │   ├── NfcRepository.kt
│   │   └── HistoryRepository.kt
│   └── preferences/
│       └── AppPreferences.kt
├── domain/
│   ├── model/
│   │   ├── NfcTag.kt
│   │   ├── TagData.kt
│   │   └── WriteData.kt
│   └── usecase/
│       ├── ReadTagUseCase.kt
│       ├── WriteTagUseCase.kt
│       └── ManageHistoryUseCase.kt
├── presentation/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── read/
│   │   ├── ReadScreen.kt
│   │   └── ReadViewModel.kt
│   ├── write/
│   │   ├── WriteScreen.kt
│   │   └── WriteViewModel.kt
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── components/
│   │   ├── NFCStatusCard.kt
│   │   ├── FunctionCard.kt
│   │   ├── BottomNavigationBar.kt
│   │   └── DialogComponents.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
└── util/
    ├── NFCUtil.kt
    ├── PermissionUtil.kt
    └── Extensions.kt
```

### 架构层级说明

#### 1. Data Layer (数据层)
负责数据的获取、存储和管理

**Room数据库实体**:
```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: HistoryType, // READ or WRITE
    val tagId: String,
    val content: String,
    val contentType: ContentType, // TEXT, URL, FILE, HEX
    val timestamp: Long,
    val tagType: String? = null,
    val capacity: String? = null
)

enum class HistoryType { READ, WRITE }
enum class ContentType { TEXT, URL, FILE, HEX }
```

**Repository接口**:
```kotlin
interface NfcRepository {
    suspend fun readTag(tag: Tag): Result<NfcTagData>
    suspend fun writeTag(tag: Tag, data: WriteData): Result<Boolean>
    suspend fun eraseTag(tag: Tag): Result<Boolean>
}

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntity>>
    fun getHistoryByType(type: HistoryType): Flow<List<HistoryEntity>>
    suspend fun insertHistory(history: HistoryEntity)
    suspend fun deleteHistory(id: Long)
    suspend fun clearAllHistory()
}
```

#### 2. Domain Layer (业务逻辑层)
定义核心业务模型和用例

**核心数据模型**:
```kotlin
data class NfcTagData(
    val id: String,              // 标签ID，如 "04:8A:9F:3D"
    val type: String,            // NFC-A, NFC-B, NFC-F
    val capacity: String,        // "504 / 888 bytes"
    val technologies: List<String>, // ["NfcA", "MifareUltralight", "Ndef"]
    val textContent: String?,    // 文本内容
    val hexContent: String?,     // 十六进制内容
    val atqa: String?,           // ATQA值
    val sak: String?,            // SAK值
    val isWritable: Boolean,
    val isLocked: Boolean
)

data class WriteData(
    val type: WriteType,
    val content: String,
    val encoding: Encoding = Encoding.UTF_8,
    val lockTag: Boolean = false,
    val password: String? = null
)

enum class WriteType { TEXT, URL, FILE, CUSTOM }
enum class Encoding { UTF_8, GBK, ASCII }
```

**UseCase示例**:
```kotlin
class ReadTagUseCase @Inject constructor(
    private val nfcRepository: NfcRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(tag: Tag): Result<NfcTagData> {
        return nfcRepository.readTag(tag).also { result ->
            if (result.isSuccess) {
                // 自动保存到历史记录
                val tagData = result.getOrNull()!!
                historyRepository.insertHistory(
                    HistoryEntity(
                        type = HistoryType.READ,
                        tagId = tagData.id,
                        content = tagData.textContent ?: "",
                        contentType = ContentType.TEXT,
                        timestamp = System.currentTimeMillis(),
                        tagType = tagData.type,
                        capacity = tagData.capacity
                    )
                )
            }
        }
    }
}
```

#### 3. Presentation Layer (表现层)
UI组件和状态管理

**ViewModel状态管理模式**:
```kotlin
// UI State
data class ReadUiState(
    val isScanning: Boolean = true,
    val tagData: NfcTagData? = null,
    val selectedTab: Int = 0,
    val error: String? = null
)

// ViewModel
class ReadViewModel @Inject constructor(
    private val readTagUseCase: ReadTagUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReadUiState())
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()
    
    fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            
            readTagUseCase(tag)
                .onSuccess { tagData ->
                    _uiState.update { 
                        it.copy(
                            isScanning = false,
                            tagData = tagData
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            error = exception.message
                        )
                    }
                }
        }
    }
}
```

---

## 功能模块详细规范

## 页面1: 主页 (HomeScreen)

### 功能需求
- 展示NFC状态
- 提供6个功能入口
- 底部显示操作提示

### UI组件结构
```kotlin
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val nfcEnabled by viewModel.nfcEnabled.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC读写工具") },
                navigationIcon = { MenuIcon() },
                actions = { NFCStatusIndicator(enabled = nfcEnabled) }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // NFC状态卡片
            NFCStatusCard(
                enabled = nfcEnabled,
                onToggle = { viewModel.toggleNFC() }
            )
            
            // 功能网格 (2列)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部提示
            BottomActionBar(
                text = "将手机靠近NFC标签开始读取"
            )
        }
    }
}
```

### 功能卡片数据
```kotlin
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
        color = Color(0xFF2196F3),
        route = "read"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Edit,
        title = "写入文本",
        description = "向标签写入文本",
        color = Color(0xFF4CAF50),
        route = "write/text"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Link,
        title = "写入URL",
        description = "向标签写入链接",
        color = Color(0xFFFF9800),
        route = "write/url"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Description,
        title = "写入文件",
        description = "向标签写入文件数据",
        color = Color(0xFF9C27B0),
        route = "write/file"
    ),
    FunctionCardData(
        icon = Icons.Outlined.History,
        title = "历史记录",
        description = "查看操作历史",
        color = Color(0xFF757575),
        route = "history"
    ),
    FunctionCardData(
        icon = Icons.Outlined.Settings,
        title = "应用设置",
        description = "权限与配置",
        color = Color(0xFF1976D2),
        route = "settings"
    )
)
```

### NFC状态管理
```kotlin
class HomeViewModel @Inject constructor(
    private val context: Application
) : AndroidViewModel(context) {
    
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    
    private val _nfcEnabled = MutableStateFlow(false)
    val nfcEnabled: StateFlow<Boolean> = _nfcEnabled.asStateFlow()
    
    init {
        checkNFCStatus()
    }
    
    private fun checkNFCStatus() {
        _nfcEnabled.value = nfcAdapter?.isEnabled == true
    }
    
    fun toggleNFC() {
        // 跳转到系统NFC设置
        if (nfcAdapter?.isEnabled != true) {
            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            context.startActivity(intent.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
    
    // 监听NFC状态变化
    fun onNFCStateChanged(enabled: Boolean) {
        _nfcEnabled.value = enabled
    }
}
```

---

## 页面2: 标签读取页 (ReadScreen)

### 功能需求
- 显示扫描动画
- 读取并展示标签信息
- 提供三种视图：文本、十六进制、技术详情
- 支持复制、保存、分享、擦除操作

### UI结构
```kotlin
@Composable
fun ReadScreen(
    navController: NavController,
    viewModel: ReadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("读取标签") },
                navigationIcon = { BackButton(navController) },
                actions = {
                    IconButton(onClick = { viewModel.shareData() }) {
                        Icon(Icons.Outlined.Share, "分享")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                    ErrorView(message = uiState.error!!)
                }
            }
        }
    }
}
```

### 扫描动画组件
```kotlin
@Composable
fun ScanningAnimation() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 脉冲动画
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Icon(
            imageVector = Icons.Outlined.Nfc,
            contentDescription = "NFC扫描",
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .alpha(alpha),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "正在搜索标签...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### 标签数据展示
```kotlin
@Composable
fun TagDataView(
    tagData: NfcTagData,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCopy: (String) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onErase: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 标签信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "标签信息",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标签ID
                TagInfoRow(
                    label = "标签ID",
                    value = tagData.id,
                    showCopyButton = true,
                    onCopy = { onCopy(tagData.id) }
                )
                
                // 标签类型
                TagInfoRow(
                    label = "标签类型",
                    value = tagData.type,
                    isBadge = true
                )
                
                // 容量信息
                TagInfoRow(
                    label = "容量",
                    value = tagData.capacity
                )
            }
        }
        
        // 选项卡
        TabRow(selectedTabIndex = selectedTab) {
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
        when (selectedTab) {
            0 -> TextContentView(tagData.textContent ?: "")
            1 -> HexContentView(tagData.hexContent ?: "")
            2 -> TechnicalDetailsView(tagData)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("保存到历史")
            }
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Text("分享")
            }
        }
        
        Button(
            onClick = onErase,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("擦除标签")
        }
    }
}
```

### NFC标签读取实现
```kotlin
class ReadViewModel @Inject constructor(
    private val readTagUseCase: ReadTagUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReadUiState())
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()
    
    fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            
            delay(2000) // 模拟扫描过程
            
            readTagUseCase(tag)
                .onSuccess { tagData ->
                    _uiState.update { 
                        it.copy(
                            isScanning = false,
                            tagData = tagData
                        )
                    }
                    // 显示成功提示
                    _snackbarMessage.emit("成功读取NFC标签")
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            error = exception.message ?: "读取失败"
                        )
                    }
                }
        }
    }
    
    fun copyToClipboard(text: String) {
        // 实现复制功能
        _snackbarMessage.tryEmit("已复制到剪贴板")
    }
    
    fun saveToHistory() {
        viewModelScope.launch {
            uiState.value.tagData?.let { tagData ->
                historyRepository.insertHistory(
                    HistoryEntity(
                        type = HistoryType.READ,
                        tagId = tagData.id,
                        content = tagData.textContent ?: "",
                        contentType = ContentType.TEXT,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _snackbarMessage.emit("已保存到历史记录")
            }
        }
    }
}
```

### NFC读取核心代码
```kotlin
class NfcRepositoryImpl @Inject constructor() : NfcRepository {
    
    override suspend fun readTag(tag: Tag): Result<NfcTagData> = withContext(Dispatchers.IO) {
        try {
            val tagId = tag.id.toHexString()
            val techList = tag.techList.map { it.substringAfterLast('.') }
            
            // 尝试读取NDEF数据
            val ndef = Ndef.get(tag)
            val ndefMessage = ndef?.cachedNdefMessage
            
            var textContent: String? = null
            var hexContent: String? = null
            
            ndefMessage?.records?.firstOrNull()?.let { record ->
                when (record.tnf) {
                    NdefRecord.TNF_WELL_KNOWN -> {
                        if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                            textContent = String(record.payload, Charset.forName("UTF-8"))
                        } else if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
                            textContent = String(record.payload, Charset.forName("UTF-8"))
                        }
                    }
                }
                hexContent = record.payload.toHexString()
            }
            
            // 获取技术细节
            val nfcA = NfcA.get(tag)
            val atqa = nfcA?.atqa?.toHexString()
            val sak = nfcA?.sak?.toString(16)
            
            Result.success(
                NfcTagData(
                    id = tagId,
                    type = detectTagType(techList),
                    capacity = "${ndef?.maxSize ?: 0} bytes",
                    technologies = techList,
                    textContent = textContent,
                    hexContent = hexContent,
                    atqa = atqa,
                    sak = sak,
                    isWritable = ndef?.isWritable ?: false,
                    isLocked = false
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun detectTagType(techList: List<String>): String {
        return when {
            techList.contains("NfcA") -> "NFC-A"
            techList.contains("NfcB") -> "NFC-B"
            techList.contains("NfcF") -> "NFC-F"
            else -> "Unknown"
        }
    }
}

// 扩展函数：字节数组转十六进制字符串
fun ByteArray.toHexString(): String {
    return joinToString(":") { "%02X".format(it) }
}
```

---

## 页面3: 数据写入页 (WriteScreen)

### 功能需求
- 支持4种写入模式：文本、URL、文件、自定义
- 动态表单切换
- 写入选项配置（锁定、密码）
- 写入过程动画和进度显示

### UI结构
```kotlin
@Composable
fun WriteScreen(
    navController: NavController,
    initialMode: WriteType = WriteType.TEXT,
    viewModel: WriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(initialMode) {
        viewModel.setWriteMode(initialMode)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("写入数据") },
                navigationIcon = { BackButton(navController) }
            )
        }
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
                WriteType.TEXT -> TextModeForm(viewModel)
                WriteType.URL -> UrlModeForm(viewModel)
                WriteType.FILE -> FileModeForm(viewModel)
                WriteType.CUSTOM -> CustomModeForm(viewModel)
            }
            
            // 写入选项
            WriteOptionsCard(
                lockTag = uiState.lockTag,
                onLockTagChanged = { viewModel.setLockTag(it) },
                password = uiState.password,
                onPasswordChanged = { viewModel.setPassword(it) }
            )
            
            // 写入动画（写入时显示）
            if (uiState.isWriting) {
                WriteAnimation(
                    status = uiState.writeStatus,
                    progress = uiState.writeProgress
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 写入按钮
            Button(
                onClick = { viewModel.startWrite() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = !uiState.isWriting && uiState.canWrite,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
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
```

### 分段控件
```kotlin
@Composable
fun SegmentedControl(
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
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
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
```

### 文本模式表单
```kotlin
@Composable
fun TextModeForm(viewModel: WriteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "输入文本内容",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = uiState.textContent,
            onValueChange = { viewModel.setTextContent(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("请输入要写入的文本内容...") },
            maxLines = 8
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "字符编码",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        var expanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.encoding.name,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Encoding.values().forEach { encoding ->
                    DropdownMenuItem(
                        text = { Text(encoding.name) },
                        onClick = {
                            viewModel.setEncoding(encoding)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
```

### URL模式表单
```kotlin
@Composable
fun UrlModeForm(viewModel: WriteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "输入网址",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = uiState.urlContent,
            onValueChange = { viewModel.setUrlContent(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://example.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // URL预览卡片
        if (uiState.urlContent.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "链接预览",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = uiState.urlContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
```

### 写入动画组件
```kotlin
@Composable
fun WriteAnimation(
    status: String,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 手机图标
            Icon(
                imageVector = Icons.Outlined.PhoneAndroid,
                contentDescription = "手机",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // 连接线动画
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            // NFC标签图标
            Icon(
                imageVector = Icons.Outlined.Nfc,
                contentDescription = "NFC标签",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (progress > 0f) {
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

### 写入ViewModel
```kotlin
data class WriteUiState(
    val writeMode: WriteType = WriteType.TEXT,
    val textContent: String = "",
    val urlContent: String = "",
    val encoding: Encoding = Encoding.UTF_8,
    val lockTag: Boolean = false,
    val password: String = "",
    val isWriting: Boolean = false,
    val writeStatus: String = "",
    val writeProgress: Float = 0f,
    val canWrite: Boolean = false
)

class WriteViewModel @Inject constructor(
    private val writeTagUseCase: WriteTagUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()
    
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
    
    fun startWrite() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isWriting = true,
                    writeStatus = "请将手机靠近标签",
                    writeProgress = 0f
                )
            }
            
            // 模拟等待标签靠近
            delay(2000)
            
            _uiState.update { it.copy(writeStatus = "正在写入...") }
            
            // 模拟写入进度
            for (i in 1..10) {
                delay(150)
                _uiState.update { it.copy(writeProgress = i / 10f) }
            }
            
            _uiState.update { 
                it.copy(
                    writeStatus = "写入成功！",
                    writeProgress = 1f
                )
            }
            
            _snackbarMessage.emit("数据已成功写入标签")
            
            delay(2000)
            // 返回主页
        }
    }
    
    private fun validateCanWrite(): Boolean {
        val canWrite = when (uiState.value.writeMode) {
            WriteType.TEXT -> uiState.value.textContent.isNotEmpty()
            WriteType.URL -> uiState.value.urlContent.isNotEmpty()
            WriteType.FILE -> true // 检查是否选择了文件
            WriteType.CUSTOM -> true
        }
        _uiState.update { it.copy(canWrite = canWrite) }
        return canWrite
    }
}
```

### NFC写入核心实现
```kotlin
override suspend fun writeTag(tag: Tag, data: WriteData): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
        val ndef = Ndef.get(tag) ?: return@withContext Result.failure(
            Exception("标签不支持NDEF格式")
        )
        
        ndef.connect()
        
        if (!ndef.isWritable) {
            ndef.close()
            return@withContext Result.failure(Exception("标签不可写"))
        }
        
        // 创建NDEF记录
        val ndefRecord = when (data.type) {
            WriteType.TEXT -> createTextRecord(data.content, data.encoding)
            WriteType.URL -> createUriRecord(data.content)
            WriteType.FILE -> createMimeRecord(data.content)
            WriteType.CUSTOM -> createCustomRecord(data.content)
        }
        
        val ndefMessage = NdefMessage(arrayOf(ndefRecord))
        
        // 检查容量
        if (ndefMessage.byteArrayLength > ndef.maxSize) {
            ndef.close()
            return@withContext Result.failure(Exception("数据超出标签容量"))
        }
        
        // 写入数据
        ndef.writeNdefMessage(ndefMessage)
        
        // 如果需要锁定标签
        if (data.lockTag) {
            ndef.makeReadOnly()
        }
        
        ndef.close()
        Result.success(true)
        
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun createTextRecord(text: String, encoding: Encoding): NdefRecord {
    val languageCode = "en"
    val languageCodeBytes = languageCode.toByteArray(Charset.forName("US-ASCII"))
    val textBytes = text.toByteArray(Charset.forName(encoding.name))
    
    val payload = ByteArray(1 + languageCodeBytes.size + textBytes.size)
    payload[0] = languageCodeBytes.size.toByte()
    System.arraycopy(languageCodeBytes, 0, payload, 1, languageCodeBytes.size)
    System.arraycopy(textBytes, 0, payload, 1 + languageCodeBytes.size, textBytes.size)
    
    return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
}

private fun createUriRecord(uri: String): NdefRecord {
    return NdefRecord.createUri(uri)
}
```

---

## 页面4: 历史记录页 (HistoryScreen)

### 功能需求
- 显示所有读取/写入历史
- 支持筛选（类型、时间）
- 按日期分组展示
- 支持删除单条或批量删除

### UI结构
```kotlin
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                navigationIcon = { BackButton(navController) },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilter() }) {
                        Icon(Icons.Outlined.FilterList, "筛选")
                    }
                }
            )
        }
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 按日期分组
                    val groupedHistory = historyList.groupBy { 
                        it.timestamp.toDateGroup() 
                    }
                    
                    groupedHistory.forEach { (dateGroup, items) ->
                        item {
                            Text(
                                text = dateGroup,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(items, key = { it.id }) { history ->
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
}
```

### 筛选栏组件
```kotlin
@Composable
fun FilterBar(
    selectedType: HistoryType?,
    selectedTime: TimeFilter,
    onTypeSelected: (HistoryType?) -> Unit,
    onTimeSelected: (TimeFilter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 类型筛选
            Text(
                text = "类型",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = selectedType == HistoryType.READ,
                    onClick = { onTypeSelected(HistoryType.READ) },
                    label = { Text("读取") }
                )
                FilterChip(
                    selected = selectedType == HistoryType.WRITE,
                    onClick = { onTypeSelected(HistoryType.WRITE) },
                    label = { Text("写入") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 时间筛选
            Text(
                text = "时间",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeFilter.values().forEach { filter ->
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

enum class TimeFilter(val label: String) {
    ALL("全部"),
    TODAY("今天"),
    WEEK("本周"),
    MONTH("本月")
}
```

### 历史项组件
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(
    history: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart) {
                onDelete()
                true
            } else false
        }
    )
    
    SwipeToDismiss(
        state = dismissState,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = Color.White
                )
            }
        },
        dismissContent = {
            Card(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 类型图标
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (history.type == HistoryType.READ)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color(0xFFC8E6C9)
                    ) {
                        Icon(
                            imageVector = if (history.type == HistoryType.READ)
                                Icons.Outlined.Label
                            else
                                Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = if (history.type == HistoryType.READ)
                                MaterialTheme.colorScheme.primary
                            else
                                Color(0xFF4CAF50)
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
                            text = history.content,
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
                }
            }
        },
        directions = setOf(DismissDirection.EndToStart)
    )
}
```

### 历史管理ViewModel
```kotlin
data class HistoryUiState(
    val showFilter: Boolean = false,
    val filterType: HistoryType? = null,
    val filterTime: TimeFilter = TimeFilter.ALL
)

class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
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
    
    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
            _snackbarMessage.emit("已删除")
        }
    }
}
```

---

## 页面5: 设置页 (SettingsScreen)

### 功能需求
- NFC状态显示与快速跳转
- 权限管理
- 应用设置（自动读取、反馈、编码等）
- 关于信息
- 清除历史记录

### UI结构
```kotlin
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = { BackButton(navController) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // NFC状态卡片
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 状态指示器
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (uiState.nfcEnabled)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NFC状态",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (uiState.nfcEnabled) "已就绪" else "未开启",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (!uiState.nfcEnabled) {
                            Button(onClick = { viewModel.openNFCSettings() }) {
                                Text("开启NFC")
                            }
                        }
                    }
                }
            }
            
            // 权限管理
            item {
                SettingsSection(title = "权限管理") {
                    PermissionItem(
                        title = "NFC权限",
                        description = "允许应用使用NFC功能",
                        enabled = uiState.nfcPermissionGranted,
                        onToggle = { viewModel.requestNFCPermission() }
                    )
                    PermissionItem(
                        title = "存储权限",
                        description = "用于保存历史记录",
                        enabled = uiState.storagePermissionGranted,
                        onToggle = { viewModel.requestStoragePermission() }
                    )
                    PermissionItem(
                        title = "通知权限",
                        description = "接收读写操作通知",
                        enabled = uiState.notificationPermissionGranted,
                        onToggle = { viewModel.requestNotificationPermission() }
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
                    SwitchItem(
                        title = "振动反馈",
                        checked = preferences.vibrationFeedback,
                        onCheckedChange = { viewModel.setVibrationFeedback(it) }
                    )
                    SwitchItem(
                        title = "声音提示",
                        checked = preferences.soundFeedback,
                        onCheckedChange = { viewModel.setSoundFeedback(it) }
                    )
                    
                    DropdownItem(
                        title = "默认编码",
                        selectedValue = preferences.defaultEncoding.name,
                        options = Encoding.values().map { it.name },
                        onValueSelected = { viewModel.setDefaultEncoding(Encoding.valueOf(it)) }
                    )
                    
                    NumberInputItem(
                        title = "历史记录保留天数",
                        value = preferences.historyRetentionDays,
                        onValueChanged = { viewModel.setHistoryRetentionDays(it) }
                    )
                }
            }
            
            // 关于应用
            item {
                SettingsSection(title = "关于应用") {
                    InfoItem(label = "应用版本", value = "v1.0.0")
                    InfoItem(label = "构建日期", value = "2025-01-07")
                    InfoItem(label = "开发者", value = "NFC Tools Team")
                }
            }
            
            // 操作按钮
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.openHelp() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("帮助与反馈")
                    }
                    
                    Button(
                        onClick = { viewModel.showClearHistoryDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("清除历史记录")
                    }
                }
            }
        }
    }
}
```

### 设置组件
```kotlin
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

@Composable
fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
```

### 数据持久化（SharedPreferences）
```kotlin
data class AppPreferences(
    val autoRead: Boolean = true,
    val vibrationFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val defaultEncoding: Encoding = Encoding.UTF_8,
    val historyRetentionDays: Int = 30
)

class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    fun getPreferences(): Flow<AppPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(loadPreferences())
        }
        
        prefs.registerOnSharedPreferenceChangeListener(listener)
        send(loadPreferences())
        
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    private fun loadPreferences() = AppPreferences(
        autoRead = prefs.getBoolean("auto_read", true),
        vibrationFeedback = prefs.getBoolean("vibration_feedback", true),
        soundFeedback = prefs.getBoolean("sound_feedback", false),
        defaultEncoding = Encoding.valueOf(
            prefs.getString("default_encoding", Encoding.UTF_8.name)!!
        ),
        historyRetentionDays = prefs.getInt("history_retention_days", 30)
    )
    
    fun setAutoRead(enabled: Boolean) {
        prefs.edit().putBoolean("auto_read", enabled).apply()
    }
    
    fun setVibrationFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_feedback", enabled).apply()
    }
    
    // ... 其他设置方法
}
```

---

## 核心功能实现

### NFC Activity配置

#### AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 权限声明 -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <!-- 必需NFC硬件 -->
    <uses-feature
        android:name="android.hardware.nfc"
        android:required="true" />
    
    <application
        android:name=".NFCApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.NFCReader">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:screenOrientation="portrait">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- NFC Intent过滤器 -->
            <intent-filter>
                <action android:name="android.nfc.action.NDEF_DISCOVERED" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
            
            <intent-filter>
                <action android:name="android.nfc.action.TAG_DISCOVERED" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
            
            <intent-filter>
                <action android:name="android.nfc.action.TECH_DISCOVERED" />
            </intent-filter>
            
            <meta-data
                android:name="android.nfc.action.TECH_DISCOVERED"
                android:resource="@xml/nfc_tech_filter" />
        </activity>
    </application>
</manifest>
```

#### res/xml/nfc_tech_filter.xml
```xml
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <tech-list>
        <tech>android.nfc.tech.Ndef</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NdefFormatable</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcA</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcB</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcF</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareClassic</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareUltralight</tech>
    </tech-list>
</resources>
```

### MainActivity NFC处理
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // 创建PendingIntent
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        
        setContent {
            NFCReaderTheme {
                NFCApp()
            }
        }
        
        // 处理启动Intent
        handleIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // 启用前台调度
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            null,
            null
        )
    }
    
    override fun onPause() {
        super.onPause()
        // 禁用前台调度
        nfcAdapter?.disableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.let { handleNFCTag(it) }
            }
        }
    }
    
    private fun handleNFCTag(tag: Tag) {
        // 发送标签到当前活动的ViewModel
        // 使用EventBus、LiveData或SharedFlow通知
        NFCEventBus.post(NFCTagEvent(tag))
    }
}
```

### NFC事件总线
```kotlin
object NFCEventBus {
    private val _tagEvents = MutableSharedFlow<NFCTagEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tagEvents: SharedFlow<NFCTagEvent> = _tagEvents.asSharedFlow()
    
    fun post(event: NFCTagEvent) {
        _tagEvents.tryEmit(event)
    }
}

data class NFCTagEvent(val tag: Tag)
```

### 在ViewModel中监听NFC事件
```kotlin
class ReadViewModel @Inject constructor(
    private val readTagUseCase: ReadTagUseCase
) : ViewModel() {
    
    init {
        // 监听NFC标签事件
        viewModelScope.launch {
            NFCEventBus.tagEvents.collect { event ->
                onTagDetected(event.tag)
            }
        }
    }
    
    private fun onTagDetected(tag: Tag) {
        // 处理标签
    }
}
```

---

## 底部导航实现

```kotlin
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute?.startsWith(item.route) == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Outlined.Home, "首页"),
    BottomNavItem("read", Icons.Outlined.Label, "读取"),
    BottomNavItem("write", Icons.Outlined.Edit, "写入"),
    BottomNavItem("history", Icons.Outlined.History, "历史"),
    BottomNavItem("settings", Icons.Outlined.Settings, "设置")
)
```

### 导航图配置
```kotlin
@Composable
fun NFCApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        composable("read") {
            ReadScreen(navController)
        }
        composable(
            route = "write?mode={mode}",
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "text"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "text"
            WriteScreen(
                navController = navController,
                initialMode = WriteType.valueOf(mode.uppercase())
            )
        }
        composable("history") {
            HistoryScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
}
```

---

## 测试策略

### 单元测试
```kotlin
@Test
fun `read NFC tag returns correct data`() = runTest {
    // Given
    val mockTag = mockk<Tag>()
    val repository = NfcRepositoryImpl()
    
    // When
    val result = repository.readTag(mockTag)
    
    // Then
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull()?.id)
}
```

### UI测试
```kotlin
@Test
fun homeScreen_displaysAllFunctionCards() {
    composeTestRule.setContent {
        HomeScreen(navController = rememberNavController())
    }
    
    composeTestRule.onNodeWithText("读取标签").assertIsDisplayed()
    composeTestRule.onNodeWithText("写入文本").assertIsDisplayed()
    composeTestRule.onNodeWithText("历史记录").assertIsDisplayed()
}
```

---

## 性能优化建议

1. **延迟初始化**: 使用`lazy`初始化重对象
2. **协程优化**: 合理使用`Dispatchers.IO`处理NFC操作
3. **内存管理**: 及时释放NFC连接
4. **列表优化**: 使用`LazyColumn`虚拟滚动
5. **图片资源**: 使用矢量图标减少APK体积

---

## 发布清单

### 构建配置 (build.gradle.kts)
```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.nfctools.reader"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

### ProGuard规则
```proguard
# NFC相关类不混淆
-keep class android.nfc.** { *; }
-keep class com.nfctools.reader.data.local.entity.** { *; }

# Room数据库
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```

---

## 附录

### 常见NFC标签类型
| 类型 | 容量 | 用途 |
|------|------|------|
| NTAG213 | 144 bytes | 通用标签 |
| NTAG215 | 504 bytes | 游戏、门禁 |
| NTAG216 | 888 bytes | 大容量应用 |
| Mifare Classic 1K | 1KB | 交通卡 |
| Mifare Ultralight | 64 bytes | 一次性标签 |

### 错误码参考
| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| NFC_NOT_ENABLED | NFC未开启 | 引导用户开启NFC |
| TAG_NOT_WRITABLE | 标签只读 | 提示用户标签已锁定 |
| CAPACITY_EXCEEDED | 容量不足 | 减少写入数据量 |
| CONNECTION_LOST | 连接中断 | 提示重新靠近标签 |
| UNSUPPORTED_TYPE | 不支持的标签类型 | 提示更换标签 |

### 参考资源
- Android NFC官方文档: https://developer.android.com/guide/topics/connectivity/nfc
- Material Design 3: https://m3.material.io/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- NFC标准: ISO/IEC 14443

---

## 开发时间估算

| 模块 | 预计工时 |
|------|----------|
| 项目搭建与配置 | 4小时 |
| UI主题与组件 | 8小时 |
| 主页开发 | 4小时 |
| 读取页开发 | 8小时 |
| 写入页开发 | 12小时 |
| 历史记录页开发 | 6小时 |
| 设置页开发 | 6小时 |
| NFC核心功能 | 16小时 |
| 数据库与持久化 | 6小时 |
| 测试与调试 | 8小时 |
| 优化与打包 | 4小时 |
| **总计** | **82小时** |

---

**文档版本**: v1.0  
**最后更新**: 2025-01-08  
**维护者**: 开发团队

**注意**: 本文档基于HTML原型生成，实际开发中可根据具体需求进行调整。