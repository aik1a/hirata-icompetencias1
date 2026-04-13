import { useState, useEffect } from 'react'
import { Gauge, CheckCircle, AlertTriangle, Plus } from 'lucide-react'
import * as api from '../api'

const EMPTY = { patente: '', kilometraje: '' }

export default function Kilometraje() {
  const [historial, setHistorial] = useState([])
  const [camiones, setCamiones] = useState([])
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState(null) // { alerta, total, patente }
  const [loading, setLoading] = useState(true)

  const load = () => {
    Promise.all([api.getKilometraje(), api.getCamiones()])
      .then(([km, c]) => { setHistorial(km); setCamiones(c) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.patente || !form.kilometraje) return
    setSaving(true)
    setFeedback(null)
    try {
      const res = await api.createKilometraje({
        patente: form.patente,
        kilometraje: parseInt(form.kilometraje),
      })
      const data = await res.json()
      setFeedback({ ...data, patente: form.patente })
      setForm(EMPTY)
      load()
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="max-w-5xl">
      <div className="flex items-center gap-2 mb-6">
        <Gauge size={22} className="text-purple-400" />
        <h1 className="text-2xl font-bold text-white">Kilometraje</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Formulario de registro */}
        <div className="lg:col-span-1">
          <div className="bg-slate-800 rounded-xl border border-slate-700 p-5">
            <div className="flex items-center gap-2 mb-4">
              <Plus size={16} className="text-blue-400" />
              <h2 className="text-sm font-semibold text-white uppercase tracking-wide">Registrar Recorrido</h2>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Patente */}
              <div>
                <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                  Patente <span className="text-red-400">*</span>
                </label>
                <select
                  value={form.patente}
                  onChange={e => setForm({ ...form, patente: e.target.value })}
                  required
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                             focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
                >
                  <option value="">Seleccionar camión...</option>
                  {camiones.map(c => (
                    <option key={c.id} value={c.patente}>{c.patente} — {c.marca} {c.modelo}</option>
                  ))}
                </select>
              </div>

              {/* Km recorridos */}
              <div>
                <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                  Km recorridos <span className="text-red-400">*</span>
                </label>
                <input
                  type="number"
                  value={form.kilometraje}
                  onChange={e => setForm({ ...form, kilometraje: e.target.value })}
                  required
                  min="1"
                  placeholder="Ej: 250"
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                             placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
                />
              </div>

              <button
                type="submit"
                disabled={saving}
                className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-60 rounded-lg text-sm font-medium text-white transition-colors"
              >
                {saving ? 'Registrando...' : 'Registrar Kilometraje'}
              </button>
            </form>

            {/* Feedback post-registro */}
            {feedback && (
              <div className={`mt-4 p-3 rounded-lg border text-sm ${
                feedback.alerta
                  ? 'bg-red-900/30 border-red-500/30 text-red-300'
                  : 'bg-green-900/20 border-green-500/30 text-green-300'
              }`}>
                {feedback.alerta ? (
                  <div className="flex items-start gap-2">
                    <AlertTriangle size={16} className="shrink-0 mt-0.5" />
                    <div>
                      <p className="font-semibold">¡Alerta de mantenimiento!</p>
                      <p className="text-xs mt-1 opacity-80">
                        {feedback.patente} acumula {feedback.total.toLocaleString('es-CL')} km.
                        Se requiere mantenimiento preventivo.
                      </p>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <CheckCircle size={16} />
                    <div>
                      <p className="font-semibold">Registrado correctamente</p>
                      <p className="text-xs mt-0.5 opacity-80">
                        Total acumulado de {feedback.patente}: {feedback.total.toLocaleString('es-CL')} km
                      </p>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Historial */}
        <div className="lg:col-span-2">
          <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden h-full">
            <div className="px-4 py-3 border-b border-slate-700 bg-slate-900/40">
              <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wide">
                Historial de registros
              </h2>
            </div>
            {loading ? (
              <div className="text-center py-12 text-slate-400">Cargando...</div>
            ) : historial.length === 0 ? (
              <div className="text-center py-12 text-slate-500">
                No hay registros de kilometraje aún.
              </div>
            ) : (
              <div className="overflow-auto max-h-[500px]">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-slate-700 sticky top-0 bg-slate-800">
                      <th className="text-left px-4 py-3 text-slate-400 font-medium">Patente</th>
                      <th className="text-right px-4 py-3 text-slate-400 font-medium">Km recorridos</th>
                      <th className="text-left px-4 py-3 text-slate-400 font-medium">Fecha y hora</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historial.map(h => (
                      <tr key={h.id} className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors">
                        <td className="px-4 py-3">
                          <span className="font-bold text-blue-300 font-mono tracking-wider text-sm">{h.patente}</span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <span className="text-white font-medium">{h.kilometraje.toLocaleString('es-CL')}</span>
                          <span className="text-slate-500 ml-1 text-xs">km</span>
                        </td>
                        <td className="px-4 py-3 text-slate-400 text-xs">{h.fecha}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
