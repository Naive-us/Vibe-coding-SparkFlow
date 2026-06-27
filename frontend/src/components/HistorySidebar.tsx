import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useSparkStore } from '../store/useSparkStore'

function formatTime(iso: string) {
  const d = new Date(iso)
  return d.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function HistorySidebar() {
  const {
    historyOpen,
    setHistoryOpen,
    sessions,
    currentSession,
    loadSession,
    loadSessions,
    startNewSession,
    removeSession,
    loading,
  } = useSparkStore()

  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null)

  const toggle = () => {
    const next = !historyOpen
    setHistoryOpen(next)
    if (next) loadSessions()
  }

  const handleNewSession = () => {
    startNewSession()
    setHistoryOpen(false)
  }

  const handleDeleteSession = async (e: React.MouseEvent, sessionId: string) => {
    e.stopPropagation()
    if (confirmDeleteId !== sessionId) {
      setConfirmDeleteId(sessionId)
      return
    }
    await removeSession(sessionId)
    setConfirmDeleteId(null)
  }

  return (
    <>
      <button
        onClick={toggle}
        className="fixed left-4 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-oat-100/80 backdrop-blur-sm border border-oat-200 shadow-sm flex items-center justify-center text-oat-500 hover:bg-oat-200/80 transition-colors"
        title="时光记录"
      >
        <span className="text-lg">⏳</span>
      </button>

      <AnimatePresence>
        {historyOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/10 z-40"
              onClick={() => setHistoryOpen(false)}
            />
            <motion.aside
              initial={{ x: -320 }}
              animate={{ x: 0 }}
              exit={{ x: -320 }}
              transition={{ type: 'spring', damping: 28, stiffness: 260 }}
              className="fixed left-0 top-0 bottom-0 w-80 z-50 bg-oat-50/85 backdrop-blur-xl border-r border-oat-200/60 shadow-xl flex flex-col"
            >
              <div className="px-5 py-5 border-b border-oat-200/50">
                <h2 className="text-lg font-semibold text-warm-wood">时光记录</h2>
                <p className="text-xs text-oat-400 mt-1">每个会话可累计多条灵感</p>
                <button
                  onClick={handleNewSession}
                  className="mt-4 w-full py-2.5 rounded-xl bg-oat-400 text-white text-sm font-medium hover:bg-oat-500 transition-colors flex items-center justify-center gap-2"
                >
                  <span>✦</span> 开启新灵感
                </button>
              </div>
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {!currentSession && (
                  <p className="text-xs text-oat-400 text-center py-2 px-3 rounded-lg bg-oat-100/50">
                    当前为空白画布，输入第一条灵感开始
                  </p>
                )}
                {sessions.length === 0 && (
                  <p className="text-sm text-oat-400 text-center py-6">还没有历史记录</p>
                )}
                {sessions.map((s) => (
                  <div
                    key={s.id}
                    className={`relative group rounded-xl border transition-all ${
                      currentSession?.id === s.id
                        ? 'bg-oat-100/80 border-oat-300 shadow-sm'
                        : 'bg-white/60 border-oat-200/50 hover:bg-white/90 hover:shadow-sm'
                    }`}
                  >
                    <button
                      type="button"
                      onClick={() => loadSession(s.id)}
                      className="w-full text-left p-4 pr-12"
                    >
                      <p className="text-sm font-medium text-warm-wood truncate">{s.title}</p>
                      <p className="text-xs text-oat-400 mt-1">{formatTime(s.createdAt)}</p>
                      <p className="text-xs text-oat-300 mt-0.5">{s.provider} · {s.modelName}</p>
                    </button>
                    <button
                      type="button"
                      onClick={(e) => handleDeleteSession(e, s.id)}
                      disabled={loading}
                      title={confirmDeleteId === s.id ? '再次点击确认删除整个会话' : '删除此会话'}
                      className={`absolute right-3 top-1/2 -translate-y-1/2 text-xs px-2 py-1 rounded-lg transition-all disabled:opacity-30 ${
                        confirmDeleteId === s.id
                          ? 'opacity-100 text-red-600 bg-red-50'
                          : 'opacity-0 group-hover:opacity-100 text-oat-400 hover:text-red-500 hover:bg-red-50/70'
                      }`}
                    >
                      {confirmDeleteId === s.id ? '确认' : '删除'}
                    </button>
                  </div>
                ))}
              </div>
            </motion.aside>
          </>
        )}
      </AnimatePresence>
    </>
  )
}
