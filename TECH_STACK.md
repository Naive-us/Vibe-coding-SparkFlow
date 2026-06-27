# SparkFlow · 灵感微光 — 技术框架说明

> 本文档梳理项目的主要技术栈、架构分层、目录结构与核心数据流，便于开发协作与后续扩展。

---

## 一、整体架构

项目采用 **前后端分离 + 可选 MCP 微服务** 的三层架构：

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (React)                       │
│   Vite · TypeScript · React Flow · Zustand · Tailwind CSS   │
│                      http://localhost:5173                  │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API (/api/*)
                           │ Vite Dev Proxy
┌──────────────────────────▼──────────────────────────────────┐
│                   Backend (Spring Boot)                     │
│   Controller → Service → LLM / Skill / MCP / Repository     │
│                      http://localhost:8080                  │
└──────────┬───────────────────────────────┬──────────────────┘
           │                               │
           │ SQLite                        │ HTTP POST (可选)
           ▼                               ▼
   ┌───────────────┐              ┌─────────────────────┐
   │  sparkflow.db │              │  MCP HTML Generator │
   │   (本地文件)   │              │  Node.js · :3100    │
   └───────────────┘              └─────────────────────┘
                                           │
                                           ▼
                                  ┌─────────────────────┐
                                  │  Ollama (可选本地)   │
                                  │  http://localhost:11434
                                  └─────────────────────┘
```

**核心数据流：**

```
用户输入灵感
  → LLM 生成结构化 JSON 思维导图
  → 持久化到 SQLite（Session / Node / Edge）
  → 前端 React Flow 渲染交互画布
  →（可选）MCP 或 DemoHtmlBuilder 生成灵感动画 HTML
  → iframe 预览播放
```

---

## 二、技术栈总览

| 层级 | 技术 | 版本 / 说明 |
|------|------|-------------|
| **前端框架** | React | 18.3 |
| **前端语言** | TypeScript | 5.6 |
| **构建工具** | Vite | 6.0 |
| **样式** | Tailwind CSS | 3.4 |
| **状态管理** | Zustand (+ persist) | 5.0 |
| **画布引擎** | @xyflow/react (React Flow) | 12.3 |
| **动画** | Framer Motion | 11.15 |
| **HTTP 客户端** | Axios | 1.7 |
| **后端框架** | Spring Boot | 3.3.5 |
| **后端语言** | Java | 17 |
| **ORM** | Spring Data JPA + Hibernate | — |
| **数据库** | SQLite | 本地文件 `sparkflow.db` |
| **HTTP 客户端（后端）** | Spring WebFlux WebClient | 调用 LLM / MCP |
| **JSON 处理** | Jackson | — |
| **MCP 服务** | Node.js (原生 http) | 端口 3100 |
| **LLM 接入** | 多 Provider 抽象 | 见下文 |

---

## 三、项目目录结构

```
vibe coding/
├── frontend/                    # React 前端
│   ├── src/
│   │   ├── App.tsx              # 根组件，组装各 UI 模块
│   │   ├── api.ts               # Axios 封装，对接后端 REST
│   │   ├── types.ts             # TypeScript 类型定义
│   │   ├── store/
│   │   │   └── useSparkStore.ts # Zustand 全局状态 + 持久化
│   │   ├── components/
│   │   │   ├── SparkCanvas.tsx  # React Flow 思维导图画布
│   │   │   ├── SparkNode.tsx    # 自定义节点（核心/场景/价值）
│   │   │   ├── DemoPanel.tsx    # 灵感动画 iframe 预览
│   │   │   ├── QueryThread.tsx  # 累计灵感浮球面板
│   │   │   ├── HistorySidebar.tsx # 时光记录侧边栏
│   │   │   ├── ModelSelector.tsx  # 模型与 Skill 配置
│   │   │   └── ConfigBanner.tsx   # 配置提示横幅
│   │   └── utils/
│   │       ├── providers.ts     # LLM Provider 默认值
│   │       ├── configCheck.ts   # API Key 校验
│   │       └── conversation.ts  # 对话日志解析
│   ├── vite.config.ts           # 开发代理 /api → :8080
│   └── package.json
│
├── backend/                     # Spring Boot 后端
│   └── src/main/
│       ├── java/com/sparkflow/
│       │   ├── SparkFlowApplication.java
│       │   ├── controller/
│       │   │   └── SparkController.java    # REST 入口
│       │   ├── service/
│       │   │   ├── SparkService.java       # 核心业务编排
│       │   │   ├── DemoGenerationService.java
│       │   │   ├── DemoHtmlBuilder.java    # 本地 SVG/HTML 动画构建
│       │   │   ├── SkillLoader.java        # Skill Markdown 加载
│       │   │   ├── McpClient.java          # MCP HTTP 客户端
│       │   │   └── ConversationLogHelper.java
│       │   ├── llm/
│       │   │   ├── BaseLLMProvider.java    # Provider 接口
│       │   │   ├── AbstractLLMProvider.java # 思维导图 Prompt + JSON 解析
│       │   │   ├── OpenAICompatibleProvider.java
│       │   │   ├── LLMFactory.java         # Provider 工厂
│       │   │   ├── OpenAIProvider.java
│       │   │   ├── AnthropicProvider.java
│       │   │   ├── QwenProvider.java
│       │   │   ├── DeepSeekProvider.java
│       │   │   └── OllamaProvider.java
│       │   ├── entity/          # JPA 实体
│       │   ├── dto/             # 请求/响应 DTO
│       │   ├── repository/      # Spring Data 仓库
│       │   ├── config/          # CORS、WebClient 配置
│       │   └── exception/       # 全局异常处理
│       └── resources/
│           ├── application.yml  # 应用配置
│           └── skills/          # 动画 Skill 规范（Markdown）
│               ├── warm-spark.md
│               ├── minimal-motion.md
│               ├── tech-blueprint.md
│               └── remotion-storyboard.md
│
├── mcp/
│   └── html-generator/          # 可选本地 MCP 服务
│       ├── server.js            # HTTP 服务，POST /generate
│       └── package.json
│
├── start.ps1                    # Windows 一键启动脚本
└── start.bat                    # Windows 批处理启动
```

---

## 四、前端架构

### 4.1 组件职责

| 组件 | 职责 |
|------|------|
| `SparkCanvas` | 主画布：React Flow 渲染节点/边，底部输入框，切换「生成灵感动画」 |
| `SparkNode` | 自定义节点：按 `nodeType` 区分样式（核心 / 场景 / 价值），支持展开详情 |
| `DemoPanel` | 灵感动画全屏预览：iframe + 播放/暂停/重播 + 16:9 / 9:16 比例 |
| `QueryThread` | 可拖拽「累计灵感」浮球，展示会话内所有输入，支持单条删除 |
| `HistorySidebar` | 「时光记录」：历史会话列表、新建/加载/删除会话 |
| `ModelSelector` | 模型 Provider 切换、API Key / Base URL / Skill / MCP 配置弹窗 |

### 4.2 状态管理（Zustand）

`useSparkStore` 管理全局状态，并通过 `persist` 中间件将以下字段持久化到 `localStorage`：

- `config` — 模型配置（Provider、API Key、Skill 风格、MCP URL 等）
- `generateDemo` — 是否同时生成灵感动画

其余运行时状态（当前会话、节点/边、loading、viewMode 等）仅在内存中维护。

### 4.3 画布布局算法

前端在 `useSparkStore.ts` 的 `buildMindmapLayout` 中实现 **径向思维导图布局**：

1. 以 `custom_anchor` 节点为圆心
2. BFS 计算节点层级（最多 2 层外圈）
3. 一级节点环绕中心分布，二级节点沿父节点方向外推

追加灵感时，通过 `nodeSignature` 对比标记 **新增节点**（橙色高亮）。

### 4.4 开发代理

```typescript
// vite.config.ts
proxy: {
  '/api': { target: 'http://localhost:8080', changeOrigin: true }
}
```

前端请求 `/api/*` 自动转发到 Spring Boot，无需处理跨域。

---

## 五、后端架构

### 5.1 分层设计

```
SparkController          ← REST 层，参数校验，HTTP 状态码映射
    ↓
SparkService             ← 业务编排：会话管理、思维导图应用、动画生成调度
    ↓
├── LLMFactory           ← 按 provider 名称实例化 LLM Provider
├── DemoGenerationService / DemoHtmlBuilder  ← 本地动画 HTML 构建
├── McpClient            ← 可选外部 MCP 服务调用
├── SkillLoader          ← 加载 Skill Markdown 规范
├── ConversationLogHelper ← 对话日志 JSON 读写
└── SessionRepository    ← JPA 数据访问
```

### 5.2 REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/spark` | 创建/续写灵感，生成思维导图（可选动画） |
| `GET` | `/api/sessions` | 获取会话列表（按创建时间倒序） |
| `GET` | `/api/sessions/{id}` | 获取会话详情（含节点、边、demoHtml） |
| `DELETE` | `/api/sessions/{id}` | 删除整个会话 |
| `DELETE` | `/api/sessions/{id}/inspirations/{index}` | 删除指定条目的累计灵感 |
| `GET` | `/api/skills` | 列出可用 Skill 风格 |
| `GET` | `/api/health` | 健康检查 |

### 5.3 核心业务：`SparkService.createSpark`

```
1. 判断是否续写会话（sessionId 是否存在）
   ├── 是 → 加载已有 Session，拼接上下文（标题 + 累计灵感 + 当前节点 + 新输入）
   └── 否 → 创建新 Session

2. 调用 LLM Provider.generateMindmap() → 返回 MindmapResponse (JSON)

3. applyMindmap() → 清空旧节点/边，写入新图谱

4. 若 generateDemo = true：
   ├── 优先调用 McpClient.generateHtml(mcpUrl) 
   └── 失败或未配置 → DemoHtmlBuilder.build() 本地生成

5. 持久化 Session → 返回 SessionDetailDto
```

---

## 六、数据库设计

使用 **SQLite** 单文件数据库，默认路径 `./sparkflow.db`（可通过环境变量 `SPARKFLOW_DB_PATH` 覆盖）。

### 实体关系

```
Session (sessions)
  ├── 1:N → Node (nodes)
  └── 1:N → Edge (edges)
```

### 主要字段

**Session**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | UUID (36) | 主键 |
| `title` | String | 思维导图标题（LLM 生成） |
| `rawInput` | TEXT | 累计原始灵感文本 |
| `provider` / `modelName` | String | 使用的 LLM |
| `conversationLog` | TEXT | JSON 格式对话记录 |
| `demoHtml` | TEXT | 灵感动画 HTML（完整内联） |
| `createdAt` | DateTime | 创建时间 |

**Node**

| 字段 | 说明 |
|------|------|
| `nodeType` | `custom_anchor` / `custom_context` / `custom_value` |
| `title` / `summary` / `detail` | 节点标题、摘要、详情 |
| `posX` / `posY` | LLM 输出的坐标（前端会重新布局） |

**Edge**

| 字段 | 说明 |
|------|------|
| `sourceId` / `targetId` | 节点间连接关系 |

---

## 七、LLM 接入层

### 7.1 Provider 抽象

```
BaseLLMProvider (interface)
  ├── generateMindmap(prompt, modelName) → MindmapResponse
  └── generateDemoHtml(prompt, modelName) → String

AbstractLLMProvider (abstract)
  ├── SYSTEM_PROMPT — 思维导图 JSON 生成规范
  ├── DEMO_SYSTEM_PROMPT — 动画 HTML 生成规范
  ├── parseResponse() / normalizeMindmap() — JSON 解析与容错
  └── generateMindmapInternal()

OpenAICompatibleProvider — OpenAI Chat Completions 兼容协议
  ├── OpenAIProvider
  ├── QwenProvider (通义千问)
  └── DeepSeekProvider

AnthropicProvider — Anthropic Messages API
OllamaProvider — 本地 Ollama /api/chat
```

### 7.2 LLMFactory 注册机制

```java
registerProvider("openai",   cfg -> new OpenAIProvider(...));
registerProvider("anthropic", cfg -> new AnthropicProvider(...));
registerProvider("ollama",    cfg -> new OllamaProvider(...));
registerProvider("qwen",      cfg -> new QwenProvider(...));
registerProvider("deepseek",  cfg -> new DeepSeekProvider(...));
```

扩展新 Provider：实现 `BaseLLMProvider` 并在 Factory 中注册即可。

### 7.3 思维导图 Prompt 规范

LLM 被要求输出严格 JSON，包含：

- 5–9 个节点（1 中心 + 3–5 一级 + 0–3 二级）
- 三类语义节点：核心 / 场景 / 价值
- 层级清晰的边连接
- 800×600 画布坐标

后端 `AbstractLLMProvider.normalizeMindmap()` 负责容错：补全缺失节点、修正边引用、保证至少有一个 anchor 节点。

---

## 八、灵感动画生成

动画生成有两条路径，按优先级降级：

### 路径 A：MCP HTML Generator（可选）

```
Frontend config.mcpUrl → Backend McpClient
  → POST http://localhost:3100/generate
  → { prompt, style, context }
  → 返回 { html: "..." }
```

MCP 服务内部：

1. 优先调用本地 **Ollama** 生成 HTML
2. 失败则使用 `fallbackHtml()` 模板兜底

### 路径 B：DemoHtmlBuilder（默认本地）

不依赖 LLM，直接由后端 Java 代码根据 `MindmapResponse` 构建完整 HTML：

- 按 Skill 风格切换 Theme（配色、明暗）
- 生成 SVG / CSS 动画的分步演示
- 内联 CSS + JS，无 CDN 依赖
- 支持 `postMessage` 协议（`play` / `pause` / `restart`），供 DemoPanel 控制

### Skill 体系

Skill 以 Markdown 文件定义动画规范，存放于 `backend/src/main/resources/skills/`：

| Skill ID | 文件 | 风格 |
|----------|------|------|
| `warm-spark` | warm-spark.md | 暖色 SVG 逐步展开 |
| `minimal-motion` | minimal-motion.md | 极简灰阶动效 |
| `tech-blueprint` | tech-blueprint.md | 深色架构蓝图 |
| `remotion-storyboard` | remotion-storyboard.md | 16:9 分镜叙事 |

`SkillLoader` 启动时缓存加载，支持运行时扩展。

---

## 九、安全与隐私设计

| 设计点 | 实现方式 |
|--------|----------|
| API Key 存储 | 仅存浏览器 `localStorage`，请求时随 body 传给后端，**不落库** |
| 数据库 | 本地 SQLite 文件，无远程依赖 |
| 本地 LLM | 支持 Ollama，数据可完全不出本机 |
| MCP 服务 | 可选本地部署，默认连接 localhost |
| iframe 沙箱 | `sandbox="allow-scripts"`，限制动画页面权限 |
| CORS | 仅允许 `localhost:5173` / `127.0.0.1:5173` |

---

## 十、环境要求与启动

### 环境依赖

| 依赖 | 最低版本 |
|------|----------|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Ollama（可选） | 本地 LLM / MCP 动画生成 |

### 端口分配

| 服务 | 端口 |
|------|------|
| Frontend (Vite) | 5173 |
| Backend (Spring Boot) | 8080 |
| MCP HTML Generator | 3100 |
| Ollama | 11434 |

### 一键启动

```powershell
# Windows PowerShell
.\start.ps1
```

脚本依次：启动后端 → 安装前端依赖（首次）→ 启动前端。

### 可选：独立启动 MCP

```bash
cd mcp/html-generator
node server.js
# 前端配置 MCP URL: http://localhost:3100/generate
```

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPARKFLOW_DB_PATH` | `./sparkflow.db` | SQLite 数据库路径 |
| `SPARKFLOW_MCP_URL` | 空 | 后端默认 MCP 地址 |
| `PORT` | `3100` | MCP 服务端口 |
| `OLLAMA_URL` | `http://localhost:11434` | MCP 使用的 Ollama 地址 |
| `OLLAMA_MODEL` | `qwen2.5` | MCP 使用的 Ollama 模型 |

---

## 十一、扩展点指南

| 扩展方向 | 建议入口 |
|----------|----------|
| 新增 LLM Provider | 实现 `BaseLLMProvider` → `LLMFactory.registerProvider()` → 前端 `providers.ts` |
| 新增动画 Skill | 添加 `skills/xxx.md` → `SkillLoader.SKILLS` 注册 → 前端 `SKILL_STYLES` |
| 新增节点类型 | 后端 Prompt 规范 → 前端 `SparkNode.typeStyles` |
| 导出功能 | 前端 Canvas 截图 / 后端 HTML → PDF 转换 |
| 团队协作 | Session 增加 userId / 共享链接 → 后端鉴权 |
| 云端部署 | SQLite → PostgreSQL，静态前端 + Docker 化后端 |

---

## 十二、技术选型理由（简要）

| 选型 | 理由 |
|------|------|
| React + Vite | 开发体验好，HMR 快，生态成熟 |
| React Flow | 专业级节点画布，支持自定义节点与交互 |
| Zustand | 轻量状态管理，persist 中间件满足配置持久化 |
| Spring Boot | 企业级 REST + JPA 开箱即用，WebClient 适合 LLM 异步调用 |
| SQLite | 零配置本地持久化，适合 MVP 与个人部署 |
| Provider 抽象 | 模型无关，用户自选 LLM，降低 vendor lock-in |
| Skill Markdown | 动画规范与代码解耦，便于社区贡献新风格 |
| MCP 可选服务 | 重计算（HTML 生成）可卸载到独立进程，支持纯本地链路 |

---

*文档版本：v1.0 · 2026-06-26*
