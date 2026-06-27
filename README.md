<div align="center">
  <img src="https://img.shields.io/badge/Status-Active-F59E0B?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Version-1.0-F97316?style=for-the-badge" alt="Version" />
  
  <h1 style="color: #D97706; font-family: 'Georgia', serif;">🌌 SparkFlow · 灵感微光</h1>
  
  <p style="color: #6B7280; font-size: 1.1em; font-style: italic;">
    由 LLM 驱动的“认知放大器”与“灵感放映机”
  </p>
</div>

<br/>

<blockquote style="border-left: 4px solid #F59E0B; background-color: #FEF3C7; padding: 15px; border-radius: 4px; color: #B45309; font-family: '楷体', 'KaiTi', serif;">
  <strong>“不要让转瞬即逝的灵感，死在枯燥的纯文本框里。”</strong>
</blockquote>

欢迎来到 **SparkFlow**。这不是一个普通的思维导图工具，而是一个灵感捕获与放映的容器。

我们通过大语言模型捕捉你脑海中最模糊、最跳跃的想法，将它们瞬间重塑为结构化的 JSON 图谱，并在一块无限延伸的 React Flow 画布上，以径向排布的方式为你展开思维的星辰大海。不仅如此，它还能一键将你的灵感转化为动态的 HTML 视听演示。

---

<h2 style="color: #EA580C;">✨ 核心魔法 (Features)</h2>

* 🧠 <strong style="color: #D97706;">无缝的灵感捕获：</strong> 随便输入几个词或一段毫无逻辑的随想，后端 Spring Boot 会调度 LLM（OpenAI/通义千问/Ollama）为你自动抽丝剥茧，生成包含“核心”、“场景”与“价值”的多维节点。
* 🕸️ <strong style="color: #D97706;">径向星轨布局：</strong> 告别死板的树状图。前端自研的 BFS 径向布局算法，让你的想法像星系一样，以核心节点为引力中心，一层层向外优雅展开。
* ⏳ <strong style="color: #D97706;">时光记录 (QueryThread)：</strong> 灵感是一个演化的过程。我们的可拖拽“累计灵感”浮球和持久化会话机制，让你随时追溯每一次思维的跳跃与推翻。
* 🎬 <strong style="color: #D97706;">一键灵感放映 (Demo Panel)：</strong> 你的想法值得一场发布会。利用 iframe 沙箱与本地生成的内联代码，将思维导图瞬间变成 16:9 或 9:16 的分步演示动画。

---

<h2 style="color: #EA580C;">🚀 未来视界：无限的延展性 (Extensibility)</h2>

SparkFlow 的设计初衷不仅是一个闭环工具，更是一个**开放的创意协议**。未来的 SparkFlow 将把定制权完全交还给你：

### 🔌 1. 跨维度的 MCP 动画引擎接入
目前的灵感动画基于本地 `DemoHtmlBuilder` 构建，但通过系统内置的 **MCP (Model Context Protocol)** 微服务架构，未来的动画生成将拥有无限可能：
* **接入 3D 渲染 MCP：** 将二维的灵感节点转化为 Three.js 构建的立体空间交互漫游。
* **接入叙事引擎 MCP：** 将冰冷的逻辑节点，交给专门的剧情向大模型，生成带有配音和分镜的故事板 (Storyboard)。
* **即插即用：** 只需要在前端面板输入不同的 `MCP URL`，SparkFlow 就能化身完全不同维度的演示神器。

### 🎨 2. Skill 体系：沉淀你的个人“思想滤镜”
你的灵感该长什么样，由你决定。项目后端的 `skills/` 目录采用了 Markdown 规范定义动画与生成风格：
* **多风格切换：** 随时在“暖色 SVG 逐步展开 (`warm-spark`)”、“极简灰阶动效 (`minimal-motion`)”或“深色架构蓝图 (`tech-blueprint`)”中自由切换。
* **沉淀个人专属 Skill：** 随着你的使用，你可以编写专属的 Skill Markdown，定义自己偏好的节点层级习惯、配色美学甚至是特定的动画节奏。让 SparkFlow 逐渐学习并沉淀为只属于你的“个人思考流派”。

---

<h2 style="color: #EA580C;">🛠️ 极速启航 (Quick Start)</h2>

三步即可在本地唤醒你的灵感引擎（确保已安装 JDK 17+ 和 Node 18+）：

**1. 启动后端 (Spring Boot 引擎)**
```bash
cd backend
./mvnw spring-boot:run

**2. 启动前端 (Vite 视窗)**
```bash
cd frontend
npm install
npm run dev

**3. (可选) 唤醒 MCP 动画生成节点**
```bash
cd mcp/html-generator
node server.js

💡 打开 http://localhost:~，输入你的 API Key，开始碰撞灵感吧！
Frontend: React 18, Vite, TypeScript, React Flow, Zustand, Tailwind CSS, Framer Motion
Backend: Spring Boot 3, Java 17, Spring Data JPA, WebFlux
Storage: SQLite (零配置本地单文件，绝对的数据隐私)
AI & Microservices: 多 Provider 抽象层 (OpenAI/Anthropic/Qwen/Ollama) + Node.js MCP (端口 3100)
