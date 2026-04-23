# 反欺诈助手 Android APP

基于 `antifraud-rag` Python 库构建的 Android 反欺诈检测应用，支持 Android 5.0+ (API 21)，兼容小米/华为/OPPO/vivo/荣耀等主流中国安卓手机。

---

## 架构概览

```
antichet_RAG-app/
├── antichet_RAG-main/   ← 原始 Python RAG 库
├── backend/             ← FastAPI HTTP 服务（后端）
│   ├── main.py
│   ├── requirements.txt
│   ├── Dockerfile
│   └── docker-compose.yml
└── android-app/         ← Android 客户端（Kotlin + Compose）
    ├── app/src/main/
    │   ├── java/com/antifraud/app/
    │   │   ├── MainActivity.kt
    │   │   ├── ui/           ← HomeScreen, ResultScreen, SettingsScreen, HistoryScreen
    │   │   ├── viewmodel/    ← AnalysisViewModel
    │   │   ├── network/      ← Retrofit ApiService, RetrofitClient, 数据模型
    │   │   └── data/         ← PreferencesManager, HistoryRepository
    │   └── res/
    └── scripts/
        └── generate_icons.py  ← 生成 PNG 启动图标
```

---

## 第一步：启动后端服务

### 前提条件
- Docker & Docker Compose
- Embedding API Key（如 OpenAI / 阿里云文本嵌入 等）

### 配置

```bash
cd backend
cp .env.example .env
# 编辑 .env，填写以下字段：
# EMBEDDING_MODEL_URL=https://api.openai.com/v1/embeddings
# EMBEDDING_MODEL_API_KEY=sk-your-key
# EMBEDDING_MODEL_NAME=text-embedding-ada-002
```

### 启动

```bash
cd backend
docker-compose up -d
```

初始化数据库（首次运行）：

```bash
cd antichet_RAG-main
docker-compose up -d db
python scripts/init_db.py \
  --db-url postgresql+asyncpg://user:pass@localhost:5432/antifraud \
  --embedding-dimension 1536
```

验证后端：

```bash
curl http://localhost:8000/health
# {"status":"ok","version":"1.0.0"}
```

---

## 第二步：构建 Android APP

### 前提条件

| 工具 | 版本 |
|------|------|
| Android Studio | Hedgehog (2023.1.1) 或更新 |
| JDK | 17+ |
| Gradle | 8.4（自动下载） |
| compileSdk | 34 |
| minSdk | **21** (Android 5.0+) |

### 2.1 生成启动图标

```bash
pip install Pillow
python android-app/scripts/generate_icons.py
```

将在各 `mipmap-*` 目录下生成 `ic_launcher.png` 和 `ic_launcher_round.png`。

### 2.2 用 Android Studio 打开

1. 打开 Android Studio → **Open** → 选择 `android-app/` 目录
2. 等待 Gradle 同步完成
3. 如遇 Gradle 下载慢，可在 `settings.gradle` 中已配置阿里云镜像加速

### 2.3 配置服务器地址

- **模拟器**：默认已配置 `http://10.0.2.2:8000`（指向宿主机 localhost）
- **真机调试**：打开 APP → 设置 → 输入服务器 IP，如 `http://192.168.1.100:8000`
- **生产部署**：将后端部署到公网服务器，填入域名/公网 IP

### 2.4 编译运行

```
Android Studio → Run → Run 'app'
```

### 2.5 打包 APK（发布版）

```
Build → Generate Signed Bundle / APK → APK → release
```

或命令行：

```bash
cd android-app
./gradlew assembleRelease
# APK 输出：app/build/outputs/apk/release/app-release.apk
```

---

## APP 功能

| 功能 | 描述 |
|------|------|
| 🛡️ 文本分析 | 输入可疑短信/聊天记录，AI 自动分析诈骗风险 |
| 🚨 风险等级 | 高危（直接命中）/ 中风险 / 低风险，颜色区分 |
| 📂 匹配案例 | 显示相似历史诈骗案例及相似度 |
| 💡 反诈提示 | 展示相关防诈知识 |
| 📋 历史记录 | 本地保存最近 50 条分析记录 |
| ⚙️ 设置 | 配置 API 服务器地址，支持连接测试 |

---

## 中国手机兼容性

- **minSdk 21**：覆盖 Android 5.0+，支持 99%+ 中国在用安卓设备
- **无 Google 服务依赖**：不依赖 Firebase/GMS，可在无 Google 框架的华为/鸿蒙设备上运行
- **阿里云 Maven 镜像**：`settings.gradle` 已配置，国内网络环境下 Gradle 同步更快
- **明文 HTTP 支持**：`AndroidManifest.xml` 已设置 `usesCleartextTraffic=true`，支持内网 HTTP

---

## API 接口说明

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| POST | `/api/v1/analyze` | 分析文本风险 |
| POST | `/api/v1/cases` | 添加诈骗案例 |
| POST | `/api/v1/tips` | 添加反诈知识 |
| POST | `/api/v1/search` | 混合检索 |

---

## 紧急联系

> 全国反诈热线：**96110**
> 报警电话：**110**
