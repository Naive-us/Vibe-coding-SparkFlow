import { useEffect } from 'react'
import HistorySidebar from './components/HistorySidebar'
import ModelSelector from './components/ModelSelector'
import SparkCanvas from './components/SparkCanvas'
import ConfigBanner from './components/ConfigBanner'
import QueryThread from './components/QueryThread'
import DemoPanel from './components/DemoPanel'
import { useSparkStore } from './store/useSparkStore'

export default function App() {
  const loadSessions = useSparkStore((s) => s.loadSessions)

  useEffect(() => {
    loadSessions()
  }, [loadSessions])

  return (
    <div className="w-full h-full relative">
      <header className="absolute top-0 left-0 right-0 z-10 pointer-events-none">
        <div className="px-6 py-5">
          <h1 className="text-xl font-light text-warm-wood tracking-wide">
            SparkFlow <span className="text-oat-400 text-sm font-normal">· 灵感微光</span>
          </h1>
        </div>
      </header>
      <HistorySidebar />
      <ModelSelector />
      <ConfigBanner />
      <QueryThread />
      <DemoPanel />
      <SparkCanvas />
    </div>
  )
}
