import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Node, Edge } from '@xyflow/react'
import type { ModelConfig, SessionDetail, SessionSummary, ViewMode } from '../types'
import { createSpark, deleteInspiration, deleteSession, fetchSession, fetchSessions } from '../api'
import { getConfigHint } from '../utils/configCheck'
import { DEFAULT_BASE_URLS, DEFAULT_MODELS } from '../utils/providers'

const DEFAULT_CONFIG: ModelConfig = {
  provider: 'qwen',
  modelName: DEFAULT_MODELS.qwen,
  apiKey: '',
  baseUrl: DEFAULT_BASE_URLS.qwen,
  mcpUrl: '',
  skillStyle: 'warm-spark',
}

interface SparkState {
  config: ModelConfig
  sessions: SessionSummary[]
  currentSession: SessionDetail | null
  nodes: Node[]
  edges: Edge[]
  loading: boolean
  error: string | null
  backendOnline: boolean
  configHint: string | null
  historyOpen: boolean
  settingsOpen: boolean
  viewMode: ViewMode
  generateDemo: boolean

  setConfig: (partial: Partial<ModelConfig>) => void
  setHistoryOpen: (open: boolean) => void
  setSettingsOpen: (open: boolean) => void
  setViewMode: (mode: ViewMode) => void
  setGenerateDemo: (enabled: boolean) => void
  startNewSession: () => void
  loadSessions: () => Promise<void>
  loadSession: (id: string) => Promise<void>
  submitSpark: (rawInput: string) => Promise<void>
  removeInspiration: (index: number) => Promise<void>
  removeSession: (sessionId: string) => Promise<void>
  clearError: () => void
}

function sessionToFlow(
  session: SessionDetail,
  previousSignatures?: Set<string>,
): { nodes: Node[]; edges: Edge[] } {
  const positions = buildMindmapLayout(session.nodes, session.edges)
  const nodes: Node[] = session.nodes.map((n) => ({
    id: n.id,
    type: 'spark',
    position: positions[n.id] ?? { x: n.posX, y: n.posY },
    data: {
      title: n.title,
      summary: n.summary,
      detail: n.detail,
      nodeType: n.nodeType,
      isNew: !!previousSignatures && n.nodeType !== 'custom_anchor' && !previousSignatures.has(nodeSignature(n)),
    },
  }))

  const edges: Edge[] = session.edges.map((e) => ({
    id: e.id,
    source: e.sourceId,
    target: e.targetId,
    type: 'smoothstep',
    animated: true,
    style: { stroke: '#BFA882', strokeWidth: 2.2 },
  }))

  return { nodes, edges }
}

function nodeSignature(node: Pick<SessionDetail['nodes'][number], 'title' | 'summary' | 'detail'>): string {
  return [node.title, node.summary, node.detail]
    .map((value) => (value ?? '').trim().toLowerCase())
    .join('|')
}

function buildMindmapLayout(
  nodes: SessionDetail['nodes'],
  edges: SessionDetail['edges'],
): Record<string, { x: number; y: number }> {
  if (nodes.length === 0) return {}

  const center = nodes.find((n) => n.nodeType === 'custom_anchor') ?? nodes[0]
  const adjacency = new Map<string, string[]>()
  edges.forEach((edge) => {
    adjacency.set(edge.sourceId, [...(adjacency.get(edge.sourceId) ?? []), edge.targetId])
    adjacency.set(edge.targetId, [...(adjacency.get(edge.targetId) ?? []), edge.sourceId])
  })

  const seen = new Set<string>([center.id])
  const levels = new Map<string, number>([[center.id, 0]])
  const queue = [center.id]
  while (queue.length > 0) {
    const current = queue.shift()!
    const currentLevel = levels.get(current) ?? 0
    ;(adjacency.get(current) ?? []).forEach((next) => {
      if (seen.has(next)) return
      seen.add(next)
      levels.set(next, currentLevel + 1)
      queue.push(next)
    })
  }

  nodes.forEach((node) => {
    if (!levels.has(node.id)) {
      levels.set(node.id, 1)
    }
  })

  const grouped = new Map<number, string[]>()
  nodes.forEach((node) => {
    const level = Math.min(levels.get(node.id) ?? 1, 2)
    grouped.set(level, [...(grouped.get(level) ?? []), node.id])
  })

  const result: Record<string, { x: number; y: number }> = {
    [center.id]: { x: 420, y: 300 },
  }

  const levelOne = (grouped.get(1) ?? []).filter((id) => id !== center.id)
  const firstRadiusX = 330
  const firstRadiusY = 220
  levelOne.forEach((id, index) => {
    const angle = -Math.PI / 2 + (2 * Math.PI * index) / Math.max(levelOne.length, 1)
    result[id] = {
      x: 420 + Math.cos(angle) * firstRadiusX,
      y: 300 + Math.sin(angle) * firstRadiusY,
    }
  })

  const levelTwo = grouped.get(2) ?? []
  levelTwo.forEach((id, index) => {
    const parentId = edges.find((edge) => edge.sourceId === id || edge.targetId === id)
    const connectedId = parentId ? (parentId.sourceId === id ? parentId.targetId : parentId.sourceId) : center.id
    const parent = result[connectedId] ?? result[center.id]
    const directionX = parent.x - result[center.id].x || (index % 2 === 0 ? 1 : -1)
    const directionY = parent.y - result[center.id].y || (index % 2 === 0 ? 1 : -1)
    const length = Math.hypot(directionX, directionY) || 1
    result[id] = {
      x: parent.x + (directionX / length) * 170,
      y: parent.y + (directionY / length) * 120,
    }
  })

  return result
}

export const useSparkStore = create<SparkState>()(
  persist(
    (set, get) => ({
      config: DEFAULT_CONFIG,
      sessions: [],
      currentSession: null,
      nodes: [],
      edges: [],
      loading: false,
      error: null,
      backendOnline: true,
      configHint: getConfigHint(DEFAULT_CONFIG),
      historyOpen: false,
      settingsOpen: false,
      viewMode: 'mindmap',
      generateDemo: false,

      setConfig: (partial) =>
        set((s) => {
          const config = { ...s.config, ...partial }
          return { config, configHint: getConfigHint(config) }
        }),

      setHistoryOpen: (open) => set({ historyOpen: open }),
      setSettingsOpen: (open) => set({ settingsOpen: open }),
      setViewMode: (mode) => set({ viewMode: mode }),
      setGenerateDemo: (enabled) => set({ generateDemo: enabled }),
      clearError: () => set({ error: null }),

      startNewSession: () =>
        set({
          currentSession: null,
          nodes: [],
          edges: [],
          viewMode: 'mindmap',
          error: null,
          historyOpen: false,
        }),

      loadSessions: async () => {
        try {
          const sessions = await fetchSessions()
          set({ sessions, backendOnline: true })
        } catch {
          set({ backendOnline: false })
        }
      },

      loadSession: async (id) => {
        set({ loading: true, error: null })
        try {
          const session = await fetchSession(id)
          const { nodes, edges } = sessionToFlow(session)
          set({
            currentSession: session,
            nodes,
            edges,
            loading: false,
            historyOpen: false,
            viewMode: session.demoHtml ? 'demo' : 'mindmap',
          })
        } catch {
          set({ loading: false, error: '加载会话失败' })
        }
      },

      submitSpark: async (rawInput) => {
        const hint = getConfigHint(get().config)
        if (hint) {
          set({ configHint: hint, settingsOpen: true })
          return
        }
        set({ loading: true, error: null })
        try {
          const sessionId = get().currentSession?.id
          const previousSignatures = sessionId
            ? new Set(get().currentSession?.nodes.map(nodeSignature) ?? [])
            : undefined
          const session = await createSpark(
            rawInput,
            get().config,
            sessionId,
            { generateDemo: get().generateDemo },
          )
          const { nodes, edges } = sessionToFlow(session, previousSignatures)
          set({
            currentSession: session,
            nodes,
            edges,
            loading: false,
            viewMode: session.demoHtml && get().generateDemo ? 'demo' : 'mindmap',
          })
          await get().loadSessions()
        } catch (err: unknown) {
          const message =
            axiosErrorMessage(err) || '生成失败，请检查模型配置'
          set({ loading: false, error: message })
        }
      },

      removeInspiration: async (index) => {
        const session = get().currentSession
        if (!session) return
        set({ loading: true, error: null })
        try {
          const updated = await deleteInspiration(session.id, index)
          set({ currentSession: updated, loading: false })
        } catch (err: unknown) {
          const message = axiosErrorMessage(err) || '删除失败'
          set({ loading: false, error: message })
        }
      },

      removeSession: async (sessionId) => {
        set({ loading: true, error: null })
        try {
          await deleteSession(sessionId)
          if (get().currentSession?.id === sessionId) {
            get().startNewSession()
          }
          await get().loadSessions()
          set({ loading: false })
        } catch (err: unknown) {
          const message = axiosErrorMessage(err) || '删除会话失败'
          set({ loading: false, error: message })
        }
      },
    }),
    {
      name: 'sparkflow-config',
      partialize: (state) => ({
        config: state.config,
        generateDemo: state.generateDemo,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.configHint = getConfigHint(state.config)
        }
      },
    },
  ),
)

function axiosErrorMessage(err: unknown): string | null {
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const resp = (err as { response?: { data?: { error?: string } } }).response
    return resp?.data?.error ?? null
  }
  return null
}
