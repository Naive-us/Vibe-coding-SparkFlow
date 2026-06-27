import axios from 'axios'
import type { SessionDetail, SessionSummary, SparkOptions } from './types'
import type { ModelConfig } from './types'

const client = axios.create({
  baseURL: '/api',
  timeout: 180000,
})

export async function createSpark(
  rawInput: string,
  config: ModelConfig,
  sessionId?: string | null,
  options?: SparkOptions,
): Promise<SessionDetail> {
  const { data } = await client.post<SessionDetail>('/spark', {
    rawInput,
    provider: config.provider,
    modelName: config.modelName,
    apiKey: config.apiKey || undefined,
    baseUrl: config.baseUrl || undefined,
    sessionId: sessionId || undefined,
    generateDemo: options?.generateDemo ?? false,
    skillStyle: config.skillStyle,
    mcpUrl: config.mcpUrl || undefined,
  })
  return data
}

export async function fetchSessions(): Promise<SessionSummary[]> {
  const { data } = await client.get<SessionSummary[]>('/sessions')
  return data
}

export async function fetchSession(id: string): Promise<SessionDetail> {
  const { data } = await client.get<SessionDetail>(`/sessions/${id}`)
  return data
}

export async function deleteInspiration(sessionId: string, index: number): Promise<SessionDetail> {
  const { data } = await client.delete<SessionDetail>(`/sessions/${sessionId}/inspirations/${index}`)
  return data
}

export async function deleteSession(sessionId: string): Promise<void> {
  await client.delete(`/sessions/${sessionId}`)
}

export async function fetchSkills(): Promise<string[]> {
  const { data } = await client.get<string[]>('/skills')
  return data
}
