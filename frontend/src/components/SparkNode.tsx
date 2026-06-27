import { memo, useState } from 'react'
import { Handle, Position, type NodeProps } from '@xyflow/react'

interface SparkNodeData {
  title: string
  summary: string
  detail: string
  nodeType: string
  isNew?: boolean
}

const typeStyles: Record<string, { shell: string; badge: string; handle: string; label: string }> = {
  custom_anchor: {
    shell: 'min-w-[210px] max-w-[260px] border-oat-500 bg-oat-100 shadow-[0_18px_45px_rgba(166,139,91,0.22)]',
    badge: 'bg-oat-500 text-white',
    handle: '!bg-oat-500',
    label: '核心',
  },
  custom_context: {
    shell: 'min-w-[170px] max-w-[230px] border-oat-300 bg-warm-cream shadow-sm',
    badge: 'bg-oat-200 text-oat-600',
    handle: '!bg-oat-400',
    label: '场景',
  },
  custom_value: {
    shell: 'min-w-[170px] max-w-[230px] border-warm-wood/40 bg-white shadow-sm',
    badge: 'bg-warm-wood text-white',
    handle: '!bg-warm-wood',
    label: '价值',
  },
}

function SparkNodeComponent({ data }: NodeProps) {
  const [expanded, setExpanded] = useState(false)
  const d = data as unknown as SparkNodeData
  const style = typeStyles[d.nodeType] ?? typeStyles.custom_context
  const isAnchor = d.nodeType === 'custom_anchor'
  const newShell = d.isNew
    ? 'border-[#D0643C] bg-[#FFF7F1] shadow-[0_20px_55px_rgba(208,100,60,0.24)] ring-2 ring-[#D0643C]/20'
    : ''
  const newHandle = d.isNew ? '!bg-[#D0643C]' : style.handle

  return (
    <button
      type="button"
      className={`group relative cursor-pointer rounded-2xl border-2 px-4 py-3 text-left transition-all hover:-translate-y-0.5 hover:shadow-lg ${style.shell} ${newShell}`}
      onClick={() => setExpanded(!expanded)}
    >
      {d.isNew && (
        <span className="absolute -right-2 -top-2 rounded-full bg-[#D0643C] px-2 py-0.5 text-[10px] font-semibold text-white shadow-sm">
          新增
        </span>
      )}
      <Handle type="target" position={Position.Top} className={`!h-2.5 !w-2.5 ${newHandle}`} />
      <div className="mb-2 flex items-center justify-between gap-3">
        <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${d.isNew ? 'bg-[#FFE0D2] text-[#A64020]' : style.badge}`}>
          {style.label}
        </span>
        {d.summary && (
          <span className={`max-w-[120px] truncate text-[11px] font-medium ${d.isNew ? 'text-[#A64020]' : 'text-oat-500'}`}>
            {d.summary}
          </span>
        )}
      </div>
      <p className={`${isAnchor ? 'text-base' : 'text-sm'} font-semibold leading-tight text-warm-wood`}>
        {d.title}
      </p>
      {(expanded || isAnchor) && d.detail && (
        <p className="mt-2 border-t border-oat-200 pt-2 text-xs leading-relaxed text-oat-500">
          {d.detail}
        </p>
      )}
      {!expanded && !isAnchor && d.detail && (
        <p className="mt-2 line-clamp-2 text-[11px] leading-relaxed text-oat-400">
          {d.detail}
        </p>
      )}
      <Handle type="source" position={Position.Bottom} className={`!h-2.5 !w-2.5 ${newHandle}`} />
    </button>
  )
}

export default memo(SparkNodeComponent)
