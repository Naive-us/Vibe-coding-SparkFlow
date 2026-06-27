import { useRef, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { useSparkStore } from '../store/useSparkStore'
import { getDisplayQueries } from '../utils/conversation'

export default function QueryThread() {
  const { currentSession, removeInspiration, loading } = useSparkStore()
  const [confirmIndex, setConfirmIndex] = useState<number | null>(null)
  const [collapsed, setCollapsed] = useState(true)
  const dragBoundsRef = useRef<HTMLDivElement>(null)
  const queries = getDisplayQueries(currentSession)

  if (queries.length === 0) return null

  const handleDelete = async (index: number) => {
    if (confirmIndex !== index) {
      setConfirmIndex(index)
      return
    }
    await removeInspiration(index)
    setConfirmIndex(null)
  }

  return (
    <div ref={dragBoundsRef} className="pointer-events-none fixed inset-0 z-20">
      <motion.aside
        drag
        dragConstraints={dragBoundsRef}
        dragElastic={0.04}
        dragMomentum={false}
        initial={{ opacity: 0, x: 24, y: 92, scale: 0.96 }}
        animate={{
          opacity: 1,
          x: collapsed ? 24 : 80,
          y: collapsed ? 92 : 80,
          scale: 1,
        }}
        className={`pointer-events-auto absolute left-0 top-0 ${
          collapsed ? 'h-16 w-16' : 'w-80 max-w-[calc(100vw-2rem)]'
        }`}
      >
        <AnimatePresence mode="wait" initial={false}>
          {collapsed ? (
            <motion.button
              key="orb"
              type="button"
              initial={{ opacity: 0, scale: 0.86 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              whileHover={{ scale: 1.04 }}
              whileTap={{ scale: 0.96 }}
              onClick={() => setCollapsed(false)}
              className="flex h-16 w-16 cursor-grab flex-col items-center justify-center rounded-full border border-oat-300/70 bg-white/90 text-oat-500 shadow-lg backdrop-blur-md active:cursor-grabbing"
              title="展开累计灵感"
            >
              <span className="text-lg font-semibold leading-none text-warm-wood">{queries.length}</span>
              <span className="mt-0.5 text-[10px] leading-none">灵感</span>
            </motion.button>
          ) : (
            <motion.div
              key="panel"
              initial={{ opacity: 0, scale: 0.96 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.96 }}
              className="overflow-hidden rounded-2xl border border-oat-200/80 bg-white/85 shadow-lg backdrop-blur-md"
            >
              <div className="cursor-grab border-b border-oat-100/80 px-4 py-3 active:cursor-grabbing">
                <div className="flex items-center justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-xs font-semibold text-warm-wood">累计灵感</p>
                    <p className="mt-0.5 truncate text-[10px] text-oat-400">
                      {queries.length} 条 · 拖动调整位置
                    </p>
                  </div>
                  <div className="flex shrink-0 items-center gap-1">
                    <span className="rounded-full bg-oat-100 px-2 py-0.5 text-[10px] font-medium text-oat-500">
                      {queries.length}
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        setCollapsed(true)
                        setConfirmIndex(null)
                      }}
                      className="flex h-7 w-7 items-center justify-center rounded-full text-oat-500 transition-colors hover:bg-oat-100"
                      title="收成小球"
                    >
                      ●
                    </button>
                  </div>
                </div>
              </div>

              <div className="max-h-[42vh] overflow-y-auto px-3 py-3">
                <div className="space-y-2">
                  {queries.map((query, index) => (
                    <div
                      key={`${index}-${query.content.slice(0, 16)}`}
                      className="group flex items-start gap-2 rounded-xl px-2 py-2 transition-colors hover:bg-oat-50/80"
                    >
                      <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-oat-200/80 text-[10px] text-oat-500">
                        {index + 1}
                      </span>
                      <p className="flex-1 text-sm leading-relaxed text-warm-wood">
                        {query.content}
                      </p>
                      <button
                        type="button"
                        onClick={() => handleDelete(index)}
                        disabled={loading}
                        title={confirmIndex === index ? '再次点击确认删除' : '删除此条灵感'}
                        className={`shrink-0 rounded-md px-1.5 py-0.5 text-xs opacity-0 transition-opacity focus:opacity-100 disabled:opacity-30 group-hover:opacity-100 ${
                          confirmIndex === index
                            ? 'bg-red-50 text-red-600'
                            : 'text-oat-400 hover:bg-red-50/60 hover:text-red-500'
                        }`}
                      >
                        {confirmIndex === index ? '确认' : '×'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.aside>
    </div>
  )
}
