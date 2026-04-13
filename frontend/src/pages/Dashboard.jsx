import { useEffect, useState } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { Truck, Users, Gauge, AlertTriangle, TrendingUp } from 'lucide-react'
import StatCard from '../components/StatCard'
import { getDashboard } from '../api'

const ALERT_COLOR = '#ef4444'
const BAR_COLOR   = '#3b82f6'

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboard()
      .then(setStats)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <div className="flex items-center justify-center h-64 text-slate-400">
      <div className="text-center">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm">Cargando datos...</p>
      </div>
    </div>
  )

  if (!stats) return (
    <div className="text-center py-16 text-slate-400">
      <p>No se pudo conectar con el servidor.</p>
      <p className="text-sm mt-1">¿Está corriendo el backend en localhost:7000?</p>
    </div>
  )

  return (
    <div className="max-w-6xl">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white">Dashboard de Flota</h1>
        <p className="text-slate-400 text-sm mt-1">Resumen operativo — Empresa de Transporte Hirata</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Camiones"
          value={stats.totalCamiones}
          icon={Truck}
          color="blue"
          subtitle="en flota"
        />
        <StatCard
          title="Conductores"
          value={stats.totalConductores}
          icon={Users}
          color="green"
          subtitle="registrados"
        />
        <StatCard
          title="Km Totales"
          value={stats.totalKm.toLocaleString('es-CL')}
          icon={Gauge}
          color="purple"
          subtitle="acumulados"
        />
        <StatCard
          title="Alertas"
          value={stats.alertas.length}
          icon={AlertTriangle}
          color={stats.alertas.length > 0 ? 'red' : 'green'}
          subtitle={stats.alertas.length > 0 ? '≥5.000 km sin mantenimiento' : 'Sin alertas pendientes'}
        />
      </div>

      {/* Alertas de mantenimiento */}
      {stats.alertas.length > 0 && (
        <div className="bg-red-950/40 border border-red-500/30 rounded-xl p-4 mb-6">
          <div className="flex items-center gap-2 mb-3">
            <AlertTriangle size={18} className="text-red-400" />
            <h2 className="text-sm font-semibold text-red-300">
              Camiones que requieren mantenimiento preventivo (≥ 5.000 km)
            </h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {stats.alertas.map(a => (
              <div
                key={a.patente}
                className="bg-red-900/40 border border-red-500/20 rounded-lg px-3 py-2 flex items-center gap-2"
              >
                <Truck size={14} className="text-red-400" />
                <span className="font-bold text-red-200 text-sm">{a.patente}</span>
                <span className="text-red-400 text-xs">{a.total.toLocaleString('es-CL')} km</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Gráfico de kilometraje */}
      <div className="bg-slate-800 rounded-xl border border-slate-700 p-6">
        <div className="flex items-center gap-2 mb-5">
          <TrendingUp size={18} className="text-blue-400" />
          <h2 className="text-base font-semibold text-white">Kilometraje Acumulado por Camión</h2>
        </div>

        {stats.kmPorCamion.length === 0 ? (
          <div className="text-center py-12 text-slate-500">
            <Gauge size={36} className="mx-auto mb-2 opacity-30" />
            <p>No hay registros de kilometraje aún.</p>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart
              data={stats.kmPorCamion}
              margin={{ top: 5, right: 20, left: 10, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
              <XAxis
                dataKey="patente"
                stroke="#64748b"
                tick={{ fill: '#94a3b8', fontSize: 12 }}
                axisLine={{ stroke: '#334155' }}
                tickLine={false}
              />
              <YAxis
                stroke="#64748b"
                tick={{ fill: '#94a3b8', fontSize: 12 }}
                axisLine={false}
                tickLine={false}
                tickFormatter={v => v.toLocaleString('es-CL')}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#f1f5f9',
                }}
                formatter={(val) => [`${val.toLocaleString('es-CL')} km`, 'Acumulado']}
                labelFormatter={(label) => `Patente: ${label}`}
              />
              <Bar dataKey="total" name="Km acumulados" radius={[5, 5, 0, 0]} maxBarSize={60}>
                {stats.kmPorCamion.map((entry) => (
                  <Cell
                    key={entry.patente}
                    fill={entry.total >= 5000 ? ALERT_COLOR : BAR_COLOR}
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
        {stats.kmPorCamion.length > 0 && (
          <p className="text-xs text-slate-500 mt-2 text-center">
            Las barras en rojo indican camiones con ≥ 5.000 km acumulados
          </p>
        )}
      </div>
    </div>
  )
}
