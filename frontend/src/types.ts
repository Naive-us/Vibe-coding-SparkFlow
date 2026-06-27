export interface SessionSummary {
  id: string
  title: string
  provider: string
  modelName: string
  createdAt: string
}

export interface NodeData {
  id: string
  nodeType: string
  title: string
  summary: string
  detail: string
  posX: number
  posY: number
}

export interface EdgeData {
  id: string
  sourceId: string
  targetId: string
}

export interface ConversationEntry {
  role: 'user' | 'assistant'
  content: string
  timestamp?: string
}

export interface SessionDetail {
  id: string
  title: string
  rawInput: string
  provider: string
  modelName: string
  createdAt: string
  parentSessionId?: string | null
  conversationLog?: string | null
  demoHtml?: string | null
  nodes: NodeData[]
  edges: EdgeData[]
}

export type Provider = 'openai' | 'anthropic' | 'ollama' | 'qwen' | 'deepseek'

export type SkillStyle = 'warm-spark' | 'minimal-motion' | 'tech-blueprint' | 'remotion-storyboard'

export interface ModelConfig {
  provider: Provider
  modelName: string
  apiKey: string
  baseUrl: string
  mcpUrl: string
  skillStyle: SkillStyle
}

export type ViewMode = 'mindmap' | 'demo'

export interface SparkOptions {
  generateDemo?: boolean
}
