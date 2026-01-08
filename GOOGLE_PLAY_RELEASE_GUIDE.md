# Google Play 上架指南

本文档详细说明如何将 NFC Reader & Writer 应用发布到 Google Play 商店。

## 📋 上架前准备清单

### 1. Google Play 开发者账号
- [ ] 注册 [Google Play Console](https://play.google.com/console) 开发者账号
- [ ] 支付一次性注册费用（$25 USD）
- [ ] 完成身份验证

### 2. 应用签名密钥
- [ ] 创建上传密钥（Upload Key）
- [ ] 配置 Google Play App Signing

#### 创建签名密钥

```bash
keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

#### 配置本地签名

1. 复制 `keystore.properties.template` 为 `keystore.properties`
2. 填入实际的密钥信息：

```properties
storeFile=path/to/upload-keystore.jks
storePassword=your_store_password
keyAlias=upload
keyPassword=your_key_password
```

### 3. 应用资源准备

#### 应用图标
- [ ] 512x512 PNG 高分辨率图标（用于商店展示）
- [ ] 已包含在应用中的自适应图标

#### 截图
需要准备以下尺寸的截图：
- [ ] 手机截图：至少2张，最多8张
  - 推荐尺寸：1080x1920 或 1080x2340
- [ ] 7英寸平板截图（可选）
- [ ] 10英寸平板截图（可选）

#### 特色图片
- [ ] 1024x500 PNG 特色图片（商店顶部展示）

#### 视频（可选）
- [ ] YouTube 预览视频链接

### 4. 商店信息

商店描述已准备在 `fastlane/metadata/android/` 目录：

```
fastlane/metadata/android/
├── en-US/
│   ├── title.txt           # 应用名称（30字符以内）
│   ├── short_description.txt  # 简短描述（80字符以内）
│   ├── full_description.txt   # 完整描述（4000字符以内）
│   └── changelogs/
│       └── 1.txt           # 版本更新日志
└── zh-CN/
    ├── title.txt
    ├── short_description.txt
    ├── full_description.txt
    └── changelogs/
        └── 1.txt
```

### 5. 隐私政策
- [ ] 创建隐私政策页面
- [ ] 更新 `strings.xml` 中的 `privacy_policy_url`

推荐使用以下平台托管隐私政策：
- GitHub Pages
- Google Sites
- 自有网站

隐私政策模板：

```markdown
# 隐私政策

最后更新：[日期]

## 数据收集
NFC Reader & Writer 不会收集任何个人身份信息。

## 本地数据存储
- NFC标签读取历史存储在设备本地
- 用户偏好设置存储在设备本地
- 无数据上传到服务器

## 第三方服务
本应用不使用任何第三方分析或广告服务。

## 权限说明
- NFC权限：用于读写NFC标签
- 振动权限：用于操作反馈
- 通知权限：用于后台操作通知

## 联系方式
如有疑问，请联系：[邮箱地址]
```

## 🚀 发布流程

### 方式一：手动上传

1. **构建 Release APK/AAB**
   ```bash
   ./gradlew bundleRelease
   ```

2. **登录 Google Play Console**

3. **创建应用**
   - 选择默认语言
   - 输入应用名称
   - 选择应用类型（应用）
   - 选择是否免费

4. **填写商店信息**
   - 上传截图和图标
   - 填写应用描述
   - 设置分类（工具类）
   - 添加联系信息

5. **内容分级**
   - 完成内容分级问卷
   - 获取分级证书

6. **定价和分发**
   - 选择免费/付费
   - 选择发布国家/地区

7. **上传 AAB 文件**
   - 进入「发布」>「正式版」
   - 上传 `app/build/outputs/bundle/release/*.aab`

8. **提交审核**

### 方式二：GitHub Actions 自动发布

1. **配置 GitHub Secrets**

   在仓库设置中添加以下 Secrets：

   | Secret 名称 | 说明 |
   |------------|------|
   | `KEYSTORE_BASE64` | Base64 编码的签名密钥 |
   | `KEYSTORE_PASSWORD` | 密钥库密码 |
   | `KEY_ALIAS` | 密钥别名 |
   | `KEY_PASSWORD` | 密钥密码 |
   | `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Google Play API 服务账号 JSON |

   生成 Base64 编码的密钥：
   ```bash
   base64 -i upload-keystore.jks | tr -d '\n'
   ```

2. **创建 Google Play API 服务账号**
   - 在 Google Cloud Console 创建项目
   - 启用 Google Play Android Developer API
   - 创建服务账号并下载 JSON 密钥
   - 在 Google Play Console 中授权服务账号

3. **创建发布标签**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

4. **自动构建和上传**
   - GitHub Actions 将自动构建签名 AAB
   - 自动上传到 Google Play 内部测试轨道

## 📝 版本更新

### 更新版本号

修改 `app/build.gradle.kts`：

```kotlin
defaultConfig {
    versionCode = 2  // 每次发布递增
    versionName = "1.1.0"  // 语义化版本号
}
```

### 添加更新日志

在 `fastlane/metadata/android/en-US/changelogs/` 创建新文件：

```
2.txt  # 对应 versionCode = 2
```

## ⚠️ 常见问题

### Q: 审核被拒怎么办？
A: 查看拒绝原因，常见原因包括：
- 隐私政策链接无效
- 应用描述与实际功能不符
- 截图不符合要求
- 权限使用说明不清晰

### Q: 如何加快审核？
A: 
- 确保所有信息完整准确
- 提供清晰的权限使用说明
- 首次发布通常需要 7 天左右

### Q: AAB vs APK？
A: Google Play 现在要求使用 AAB 格式，它可以：
- 减小下载大小
- 支持动态功能模块
- 自动优化不同设备

## 📞 支持

如有问题，请：
- 查阅 [Google Play Console 帮助](https://support.google.com/googleplay/android-developer)
- 提交 GitHub Issue


---

📋 上架前还需要您完成的事项：
- 创建签名密钥
```bash
   keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
- 准备隐私政策页面 
    - 创建隐私政策网页并更新 strings.xml 中的 URL
- 准备商店素材
    
    - 512x512 高分辨率图标
    - 1024x500 特色图片
    - 手机截图（至少2张）
- 配置 GitHub Secrets（如果使用自动发布）
    - KEYSTORE_BASE64
    - KEYSTORE_PASSWORD
    - KEY_ALIAS
    - KEY_PASSWORD
- 注册 Google Play 开发者账号（$25 一次性费用）