# AListCloud

面向 Android 的 AList 网盘客户端，使用 Kotlin、Jetpack Compose 和 Material 3 构建。连接你自己的 AList 服务，浏览、打开和上传文件。

本项目为社区项目，与 AList 或百度网盘没有官方关联。当前版本为测试版，欢迎通过 Issues 反馈问题、通过 Pull Requests 参与改进。

## 下载与使用

从 [Releases](https://github.com/kmua123/AListCloud/releases) 下载 APK，支持 Android 8.0（API 26）及以上。

1. 安装 APK，打开应用。
2. 输入 AList 服务的完整地址（推荐 `https://`）、用户名和密码。
3. 登录后浏览目录，在顶部切换文件分类，或使用上传按钮选择本地文件。
4. 在“传输”页面查看下载和上传任务。

测试版 APK 使用开发调试签名，不是正式发行签名。以后切换签名可能需要卸载重装；卸载会清除应用配置。源码自行构建的 APK 也可能无法直接覆盖此安装包。

## 已实现功能

- 目录浏览、按文件名筛选，以及全盘图片、视频、文档、音乐和压缩包分类扫描。
- 图片缩放预览；音乐与视频播放、暂停和拖动进度。
- 文档通过系统文件 URI 交给手机已安装的阅读器打开。
- 系统下载管理及任务列表，支持取消、清理记录和相关下载文件操作。
- 多文件上传、队列进度、取消和失败重试；同名文件不主动覆盖。
- 压缩包目录浏览与解压：优先使用服务器接口，缺少接口时支持标准 ZIP 的客户端兼容处理。
- 分享链接、文件属性，以及浅色和深色主题。

## 支持范围

| 功能 | 当前限制 |
| --- | --- |
| 分类 | 逐目录扫描可访问内容，最多 5000 个目录；大网盘扫描需要时间，可停止；搜索筛选当前已加载结果 |
| 音视频 | 依赖 Android 系统解码器；不提供后台音乐服务 |
| 文档 | 依赖手机已安装的阅读器，不支持应用内编辑 |
| 上传 | 应用进程需保持运行，不支持进程结束后的队列恢复 |
| ZIP 兼容处理 | 非加密 ZIP，压缩文件不超过 256 MB，解压内容不超过 1 GB，条目不超过 10000 |
| RAR / 7z / 加密归档 | 需要服务器支持相应压缩包接口与格式 |
| 取消传输或解压 | 服务器可能保留部分已完成内容，需要刷新目录确认 |

服务端 API、存储驱动权限、直链访问及手机格式支持都会影响功能。没有对所有 AList 版本和存储驱动做完整兼容验证。

## 数据与隐私

应用会在本机 Preferences DataStore 中保存服务地址、账号、密码和登录令牌，当前未提供应用层加密。不要共享应用数据或含这些信息的备份。HTTP 连接受支持，但会以明文传输，请优先使用 HTTPS。

应用不内置公共网盘账号。网络请求正文日志已关闭；反馈问题时请隐去密码、令牌、私人地址、文件名和分享链接。详情见 [SECURITY.md](SECURITY.md)。

## 从源码构建

需要 JDK 21、Android SDK Platform 37 和能够支持本项目 Android Gradle Plugin 9.2.1 的 Android Studio。项目使用 Gradle Wrapper 9.4.1；首次构建需要网络下载依赖。

```bash
git clone https://github.com/kmua123/AListCloud.git
cd AListCloud
```

通过 Android Studio 打开项目并设置 SDK 路径，或在本机创建不提交的 `local.properties`：

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Windows：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

macOS / Linux：

```bash
sh ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

APK 输出为 `app/build/outputs/apk/debug/app-debug.apk`。无需填入任何服务器密码即可编译。

## 验证与参与

当前构建和 4 项单元测试通过。Android 实机已验证图片预览、音视频播放、多格式文件上传和 ZIP 兼容解压；这些结果不代表所有设备与服务器均兼容。

实机集成测试默认跳过。启用时需先在测试设备登录，并显式提供 `runLiveCloudTests=true` 和可写的 `cloudRoot` 参数。测试会上传样本并创建独立目录，完成后不会自动删除。请使用专用测试账号和目录。补充的防重复任务及分类自动检查尚未完成实机运行。

欢迎改进后台传输、队列持久化、凭据保护、播放兼容性和服务器兼容性。提交前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证与致谢

本项目源码按 [MIT License](LICENSE) 提供。第三方依赖保留各自的许可证与版权。感谢 AndroidX、Jetpack Compose、Retrofit、OkHttp、Coil、ZoomImage 和 Zoomable 等项目。
