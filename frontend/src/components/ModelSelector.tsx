import { AnimatePresence, motion } from 'framer-motion'
import { useSparkStore } from '../store/useSparkStore'
import type { Provider, SkillStyle } from '../types'
import {
  DEFAULT_BASE_URLS,
  DEFAULT_MODELS,
  getApiKeyPlaceholder,
  needsApiKey,
  PROVIDER_LABELS,
} from '../utils/providers'

const PROVIDERS: { value: Provider; label: string }[] = [
  { value: 'qwen', label: '通义千问' },
  { value: 'deepseek', label: 'DeepSeek' },
  { value: 'openai', label: 'OpenAI' },
  { value: 'anthropic', label: 'Anthropic' },
  { value: 'ollama', label: 'Ollama 本地' },
]

const SKILL_STYLES: { value: SkillStyle; label: string }[] = [
  { value: 'warm-spark', label: '治愈暖光' },
  { value: 'remotion-storyboard', label: 'Remotion Storyboard' },
  { value: 'minimal-motion', label: '极简动效' },
  { value: 'tech-blueprint', label: '技术蓝图' },
]

export default function ModelSelector() {
  const { config, setConfig, settingsOpen, setSettingsOpen } = useSparkStore()

  const handleProviderChange = (provider: Provider) => {
    setConfig({
      provider,
      modelName: DEFAULT_MODELS[provider],
      baseUrl: DEFAULT_BASE_URLS[provider],
    })
  }

  return (
    <>
      <div className="fixed top-5 right-5 z-30 flex items-center gap-2">
        <div className="flex items-center bg-oat-100/90 backdrop-blur-sm border border-oat-300/60 rounded-full shadow-sm overflow-hidden">
          <select
            value={config.provider}
            onChange={(e) => handleProviderChange(e.target.value as Provider)}
            className="bg-transparent text-sm text-warm-wood px-4 py-2 outline-none cursor-pointer appearance-none"
          >
            {PROVIDERS.map((p) => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
          <div className="w-px h-5 bg-oat-300/60" />
          <input
            type="text"
            value={config.modelName}
            onChange={(e) => setConfig({ modelName: e.target.value })}
            placeholder="模型名称"
            className="bg-transparent text-sm text-warm-wood px-4 py-2 w-44 outline-none placeholder:text-oat-400"
          />
        </div>
        <button
          onClick={() => setSettingsOpen(true)}
          className="w-9 h-9 rounded-full bg-oat-200/80 border border-oat-300/50 flex items-center justify-center text-oat-500 hover:bg-oat-300/60 transition-colors"
          title="配置"
        >
          ⚙
        </button>
      </div>

      <AnimatePresence>
        {settingsOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/15 z-50"
              onClick={() => setSettingsOpen(false)}
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: -10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: -10 }}
              className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-md bg-oat-50 rounded-2xl border border-oat-200 shadow-2xl p-6 max-h-[85vh] overflow-y-auto"
            >
              <h3 className="text-lg font-semibold text-warm-wood mb-1">模型配置</h3>
              <p className="text-xs text-oat-400 mb-5">
                当前：{PROVIDER_LABELS[config.provider]} · 密钥仅保存在本地浏览器
              </p>

              {needsApiKey(config.provider) && (
                <label className="block mb-4">
                  <span className="text-sm text-oat-500">API Key</span>
                  <input
                    type="password"
                    value={config.apiKey}
                    onChange={(e) => setConfig({ apiKey: e.target.value })}
                    className="mt-1 w-full px-3 py-2 rounded-lg border border-oat-200 bg-white text-sm outline-none focus:border-oat-400"
                    placeholder={getApiKeyPlaceholder(config.provider)}
                  />
                </label>
              )}

              <label className="block mb-4">
                <span className="text-sm text-oat-500">
                  {config.provider === 'ollama' ? 'Ollama Base URL' : 'API Base URL（可选）'}
                </span>
                <input
                  type="text"
                  value={config.baseUrl}
                  onChange={(e) => setConfig({ baseUrl: e.target.value })}
                  className="mt-1 w-full px-3 py-2 rounded-lg border border-oat-200 bg-white text-sm outline-none focus:border-oat-400"
                  placeholder={DEFAULT_BASE_URLS[config.provider]}
                />
              </label>

              <label className="block mb-4">
                <span className="text-sm text-oat-500">动画 Skill 风格</span>
                <select
                  value={config.skillStyle}
                  onChange={(e) => setConfig({ skillStyle: e.target.value as SkillStyle })}
                  className="mt-1 w-full px-3 py-2 rounded-lg border border-oat-200 bg-white text-sm outline-none focus:border-oat-400"
                >
                  {SKILL_STYLES.map((s) => (
                    <option key={s.value} value={s.value}>{s.label}</option>
                  ))}
                </select>
              </label>

              <label className="block mb-6">
                <span className="text-sm text-oat-500">MCP HTML 生成器（可选，本地部署）</span>
                <input
                  type="text"
                  value={config.mcpUrl}
                  onChange={(e) => setConfig({ mcpUrl: e.target.value })}
                  className="mt-1 w-full px-3 py-2 rounded-lg border border-oat-200 bg-white text-sm outline-none focus:border-oat-400"
                  placeholder="http://localhost:3100/generate"
                />
                <p className="text-[10px] text-oat-400 mt-1">
                  留空则直接用 LLM 生成。本地 MCP：cd mcp/html-generator && node server.js
                </p>
              </label>

              <button
                onClick={() => setSettingsOpen(false)}
                className="w-full py-2.5 rounded-xl bg-oat-400 text-white text-sm font-medium hover:bg-oat-500 transition-colors"
              >
                保存
              </button>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  )
}
