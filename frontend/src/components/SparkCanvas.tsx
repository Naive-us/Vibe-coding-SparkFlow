import { useCallback, useEffect, useState } from 'react'
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  type NodeTypes,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { motion } from 'framer-motion'
import SparkNode from './SparkNode'
import { useSparkStore } from '../store/useSparkStore'

const nodeTypes: NodeTypes = { spark: SparkNode }

export default function SparkCanvas() {
  const {
    nodes: storeNodes,
    edges: storeEdges,
    loading,
    submitSpark,
    error,
    clearError,
    currentSession,
    viewMode,
    generateDemo,
    setGenerateDemo,
    setViewMode,
  } = useSparkStore()
  const [input, setInput] = useState('')
  const [nodes, setNodes, onNodesChange] = useNodesState(storeNodes)
  const [edges, setEdges, onEdgesChange] = useEdgesState(storeEdges)

  useEffect(() => {
    setNodes(storeNodes)
    setEdges(storeEdges)
  }, [storeNodes, storeEdges, setNodes, setEdges])

  const handleSubmit = useCallback(async () => {
    if (!input.trim() || loading) return
    await submitSpark(input.trim())
    setInput('')
  }, [input, loading, submitSpark])

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && (event.metaKey || event.ctrlKey)) {
      handleSubmit()
    }
  }

  const hasDemo = !!currentSession?.demoHtml

  return (
    <div className="relative h-full w-full">
      <div className={`h-full w-full ${viewMode === 'demo' ? 'invisible' : 'visible'}`}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          nodeTypes={nodeTypes}
          fitView
          fitViewOptions={{ padding: 0.22 }}
          minZoom={0.35}
          maxZoom={1.4}
          defaultEdgeOptions={{
            type: 'smoothstep',
            animated: true,
            style: { stroke: '#BFA882', strokeWidth: 2.2 },
          }}
          proOptions={{ hideAttribution: true }}
        >
          <Background color="#E8DFD0" gap={24} size={1} />
          <Controls className="!border-oat-200 !bg-oat-50/90 !shadow-sm" />
          <MiniMap
            nodeColor={(node) => {
              const nodeType = String(node.data?.nodeType ?? '')
              if (nodeType === 'custom_anchor') return '#A68B5B'
              if (nodeType === 'custom_value') return '#8B7355'
              return '#D4C4A8'
            }}
            maskColor="rgba(253, 251, 247, 0.7)"
            className="!border-oat-200 !bg-oat-50/80"
          />
        </ReactFlow>
      </div>

      {hasDemo && viewMode === 'mindmap' && (
        <button
          type="button"
          onClick={() => setViewMode('demo')}
          className="absolute right-24 top-20 z-20 rounded-full border border-oat-200 bg-oat-100/90 px-3 py-1.5 text-xs text-oat-500 hover:bg-oat-200/80"
        >
          查看灵感动画
        </button>
      )}

      <div className="absolute bottom-8 left-1/2 z-20 w-full max-w-2xl -translate-x-1/2 px-6">
        {error && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-3 flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600"
          >
            <span>{error}</span>
            <button type="button" onClick={clearError} className="ml-2 text-red-400 hover:text-red-600">
              关闭
            </button>
          </motion.div>
        )}
        <div className="rounded-2xl border border-oat-200 bg-white/80 p-3 shadow-lg backdrop-blur-md">
          <div className="mb-1 flex items-center gap-3 border-b border-oat-100 px-2 pb-2">
            <label className="flex cursor-pointer select-none items-center gap-2 text-xs text-oat-500">
              <input
                type="checkbox"
                checked={generateDemo}
                onChange={(event) => setGenerateDemo(event.target.checked)}
                className="rounded border-oat-300 text-oat-400 focus:ring-oat-300"
              />
              同时生成灵感动画
            </label>
          </div>
          <div className="flex gap-2">
            <textarea
              value={input}
              onChange={(event) => setInput(event.target.value)}
              onKeyDown={handleKeyDown}
              placeholder={
                currentSession
                  ? '追加灵感到当前会话，Ctrl+Enter 发送'
                  : '写下第一条灵感，Ctrl+Enter 发送'
              }
              rows={2}
              disabled={loading}
              className="flex-1 resize-none bg-transparent px-3 py-2 text-sm text-warm-wood outline-none placeholder:text-oat-400 disabled:opacity-50"
            />
            <button
              type="button"
              onClick={handleSubmit}
              disabled={loading || !input.trim()}
              className="min-w-[80px] self-end rounded-xl bg-oat-400 px-6 py-3 text-sm font-medium text-white transition-all hover:bg-oat-500 disabled:cursor-not-allowed disabled:opacity-40"
            >
              {loading ? (
                <span className="inline-block animate-spin">⌁</span>
              ) : currentSession ? (
                '追加'
              ) : (
                '微光'
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
