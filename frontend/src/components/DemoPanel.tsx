import { useRef, useState } from 'react'
import { motion } from 'framer-motion'
import { useSparkStore } from '../store/useSparkStore'

type PreviewRatio = '16:9' | '9:16'

export default function DemoPanel() {
  const { currentSession, viewMode, setViewMode } = useSparkStore()
  const iframeRef = useRef<HTMLIFrameElement>(null)
  const [playing, setPlaying] = useState(true)
  const [ratio, setRatio] = useState<PreviewRatio>('16:9')
  const demoHtml = currentSession?.demoHtml

  if (!demoHtml) return null

  const sendDemoAction = (action: 'play' | 'pause' | 'restart') => {
    iframeRef.current?.contentWindow?.postMessage({ type: 'sparkflow-demo', action }, '*')
  }

  const togglePlaying = () => {
    const next = !playing
    setPlaying(next)
    sendDemoAction(next ? 'play' : 'pause')
  }

  return (
    <div className={`absolute inset-0 z-10 ${viewMode === 'demo' ? 'block' : 'hidden'}`}>
      <div className="absolute top-20 right-24 z-20 flex max-w-[calc(100vw-7rem)] flex-wrap items-center justify-end gap-1 rounded-full border border-oat-200 bg-oat-100/90 p-1 shadow-sm backdrop-blur-sm">
        <button
          type="button"
          onClick={() => setViewMode('mindmap')}
          className={`rounded-full px-3 py-1 text-xs transition-colors ${
            viewMode === 'mindmap' ? 'bg-oat-400 text-white' : 'text-oat-500 hover:bg-oat-200/60'
          }`}
        >
          思维导图
        </button>
        <button
          type="button"
          onClick={() => setViewMode('demo')}
          className={`rounded-full px-3 py-1 text-xs transition-colors ${
            viewMode === 'demo' ? 'bg-oat-400 text-white' : 'text-oat-500 hover:bg-oat-200/60'
          }`}
        >
          灵感动画
        </button>
        <span className="mx-1 h-4 w-px bg-oat-300/70" />
        <button
          type="button"
          onClick={togglePlaying}
          className="rounded-full px-3 py-1 text-xs text-oat-500 transition-colors hover:bg-oat-200/60"
          title={playing ? '暂停动画' : '播放动画'}
        >
          {playing ? '暂停' : '播放'}
        </button>
        <button
          type="button"
          onClick={() => {
            setPlaying(true)
            sendDemoAction('restart')
          }}
          className="rounded-full px-3 py-1 text-xs text-oat-500 transition-colors hover:bg-oat-200/60"
          title="重新播放"
        >
          重播
        </button>
        <select
          value={ratio}
          onChange={(event) => setRatio(event.target.value as PreviewRatio)}
          className="bg-transparent px-2 py-1 text-xs text-oat-500 outline-none"
          title="预览比例"
        >
          <option value="16:9">16:9</option>
          <option value="9:16">9:16</option>
        </select>
      </div>

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="absolute inset-0 bottom-24 top-14 flex items-center justify-center p-4"
      >
        <div
          className={`max-h-full w-full overflow-hidden rounded-2xl border border-oat-200/80 bg-[#FDFBF7] shadow-lg ${
            ratio === '16:9' ? 'aspect-video max-w-6xl' : 'aspect-[9/16] max-w-[430px]'
          }`}
        >
          <iframe
            ref={iframeRef}
            srcDoc={demoHtml}
            title="灵感动画"
            className="block h-full w-full border-0"
            sandbox="allow-scripts"
          />
        </div>
      </motion.div>
    </div>
  )
}
