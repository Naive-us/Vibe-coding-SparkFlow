# Warm Spark Skill — 节点逐步展开动画

## 形式
- SVG 思维导图：中心圆 → 连线 draw 动画 → 子节点 pop 显现
- 类似 PPT 逐步展开，一步一个节点
- 底部进度圆点，自动轮播

## 文字
- 每节点 ≤4 字，仅关键词
- 禁止段落、禁止描述性长句
- 无标题区大段文字

## 视觉
- 背景 #FDFBF7，节点 #F5F0E8，描边 #A68B5B
- 连线 #D4C4A8，stroke-dashoffset 动画
- 节点 scale 0.6→1 + opacity 0→1

## 交互
- 可选 hover 高亮当前节点
- setInterval 自动切换高亮步骤
- 点击节点跳到该步（可选）

## 禁止
- 无 CDN、无外链图
- 不用 markdown
