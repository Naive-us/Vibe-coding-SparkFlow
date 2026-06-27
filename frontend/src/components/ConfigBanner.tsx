import { motion, AnimatePresence } from 'framer-motion'
import { useSparkStore } from '../store/useSparkStore'

export default function ConfigBanner() {
  const { configHint, backendOnline, setSettingsOpen } = useSparkStore()

  const messages: { text: string; action?: () => void; actionLabel?: string }[] = []

  if (configHint) {
    messages.push({
      text: configHint,
      action: () => setSettingsOpen(true),
      actionLabel: '去配置',
    })
  }

  if (!backendOnline) {
    messages.push({
      text: '后端未连接，请确认 Spring Boot 已启动（端口 8080）。历史记录暂不可用，但不影响配置模型。',
    })
  }

  if (messages.length === 0) return null

  return (
    <div className="fixed top-16 left-1/2 -translate-x-1/2 z-20 w-full max-w-lg px-4 space-y-2">
      <AnimatePresence>
        {messages.map((msg, i) => (
          <motion.div
            key={i}
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            className="flex items-center gap-3 px-4 py-3 rounded-xl bg-amber-50/95 border border-amber-200/80 text-amber-800 text-sm shadow-sm backdrop-blur-sm"
          >
            <span className="flex-1">{msg.text}</span>
            {msg.action && (
              <button
                onClick={msg.action}
                className="shrink-0 px-3 py-1 rounded-lg bg-amber-200/80 hover:bg-amber-300/80 text-amber-900 text-xs font-medium transition-colors"
              >
                {msg.actionLabel}
              </button>
            )}
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  )
}
