const colorMap = {
  blue:   { bg: 'bg-blue-500/10',   border: 'border-blue-500/20',   icon: 'text-blue-400',   val: 'text-blue-300' },
  green:  { bg: 'bg-green-500/10',  border: 'border-green-500/20',  icon: 'text-green-400',  val: 'text-green-300' },
  purple: { bg: 'bg-purple-500/10', border: 'border-purple-500/20', icon: 'text-purple-400', val: 'text-purple-300' },
  red:    { bg: 'bg-red-500/10',    border: 'border-red-500/20',    icon: 'text-red-400',    val: 'text-red-300' },
  amber:  { bg: 'bg-amber-500/10',  border: 'border-amber-500/20',  icon: 'text-amber-400',  val: 'text-amber-300' },
}

export default function StatCard({ title, value, icon: Icon, color = 'blue', subtitle }) {
  const c = colorMap[color] || colorMap.blue
  return (
    <div className={`rounded-xl border p-5 ${c.bg} ${c.border}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1 min-w-0">
          <p className="text-xs text-slate-400 uppercase tracking-wider font-medium">{title}</p>
          <p className={`text-3xl font-bold mt-1.5 ${c.val}`}>{value}</p>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>
        <div className={`p-2.5 rounded-lg bg-slate-800/60 ${c.icon} shrink-0`}>
          <Icon size={22} />
        </div>
      </div>
    </div>
  )
}
