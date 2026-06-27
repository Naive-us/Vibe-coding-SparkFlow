import type { ConversationEntry, SessionDetail } from '../types'

export function parseConversationLog(log?: string | null): ConversationEntry[] {
  if (!log) return []
  try {
    const parsed = JSON.parse(log) as ConversationEntry[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function getDisplayQueries(session: SessionDetail | null): ConversationEntry[] {
  if (!session) return []
  const fromLog = parseConversationLog(session.conversationLog).filter((e) => e.role === 'user')
  if (fromLog.length > 0) return fromLog
  if (session.rawInput) {
    return [{ role: 'user', content: session.rawInput }]
  }
  return []
}
