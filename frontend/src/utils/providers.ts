import type { Provider } from '../types'

export const PROVIDER_LABELS: Record<Provider, string> = {
  openai: 'OpenAI',
  anthropic: 'Anthropic',
  ollama: 'Ollama 本地',
  qwen: '通义千问',
  deepseek: 'DeepSeek',
}

export const DEFAULT_MODELS: Record<Provider, string> = {
  openai: 'gpt-4o',
  anthropic: 'claude-3-5-sonnet-20241022',
  ollama: 'llama3',
  qwen: 'qwen-plus',
  deepseek: 'deepseek-chat',
}

export const DEFAULT_BASE_URLS: Record<Provider, string> = {
  openai: 'https://api.openai.com/v1',
  anthropic: 'https://api.anthropic.com/v1',
  ollama: 'http://localhost:11434',
  qwen: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
  deepseek: 'https://api.deepseek.com/v1',
}

export const API_KEY_PROVIDERS: Provider[] = ['openai', 'anthropic', 'qwen', 'deepseek']

export function needsApiKey(provider: Provider): boolean {
  return API_KEY_PROVIDERS.includes(provider)
}

export function getApiKeyPlaceholder(provider: Provider): string {
  switch (provider) {
    case 'qwen': return 'sk-... (阿里云 DashScope API Key)'
    case 'deepseek': return 'sk-... (DeepSeek API Key)'
    case 'openai': return 'sk-...'
    case 'anthropic': return 'sk-ant-...'
    default: return 'sk-...'
  }
}
