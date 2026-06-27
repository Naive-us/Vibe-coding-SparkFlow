import type { ModelConfig } from '../types'
import { needsApiKey, PROVIDER_LABELS } from './providers'

export function getConfigHint(config: ModelConfig): string | null {
  if (!config.modelName?.trim()) {
    return '请先填写模型名称'
  }

  if (needsApiKey(config.provider) && !config.apiKey?.trim()) {
    return `使用 ${PROVIDER_LABELS[config.provider]} 需要先配置 API Key，点击右上角 ⚙ 设置`
  }

  if (config.provider === 'ollama') {
    if (!config.baseUrl?.trim()) {
      return '请先配置 Ollama 地址，点击右上角 ⚙ 设置（默认 http://localhost:11434）'
    }
  }

  return null
}

export function isConfigReady(config: ModelConfig): boolean {
  return getConfigHint(config) === null
}
