import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, Wrench } from 'lucide-react'
import Modal from '../components/Modal'
import * as api from '../api'

const TIPOS = ['Preventivo', 'Correctivo', 'Revisión']

const hoy = () => new Date().toISOString().split('T')[0]
const EMPTY = { patente: '', tipo: 'Preventivo', descripcion: '', fecha: hoy() }

const TIPO_COLORS = {
  'Preventivo': 'bg-blue-900/40 text-blue-300 border-blue-500/30',
  'Correctivo': 'bg-red-900/40 text-red-300 border-red-500/30',
  'Revisión':   'bg-amber-900/40 text-amber-300 border-amber-500/30',
}

export default function Mantenimiento() {
  const [registros, setRegistros] = useState([])
  const [camiones, setCamiones] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState({ open: false, editing: null })
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([api.getMantenimiento(), api.getCamiones()])
      .then(([m, c]) => { setRegistros(m); setCamiones(c) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setForm(EMPTY)
    setModal({ open: true, editing: null })
  }

  const openEdit = (r) => {
    setForm({
      patente:     r.patente,
      tipo:        r.tipo,
      descripcion: r.descripcion,
      fecha:       r.fecha,
    })
    setModal({ open: true, editing: r })
  }

  const closeModal = () => setModal({ open: false, editing: null })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (modal.editing) {
        await api.updateMantenimiento(modal.editing.id, form)
      } else {
        await api.createMantenimiento(form)
      }
      closeModal()
      load()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (r) => {
    if (!confirm(`¿Eliminar el registro de mantenimiento de "${r.patente}" (${r.fecha})?`)) return
    const res = await api.deleteMantenimiento(r.id)
    if (!res.ok) {
      const data = await res.json()
      alert('Error: ' + data.error)
    } else {
      load()
    }
  }

  return (
    <div className="max-w-6xl">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Wrench size={22} className="text-amber-400" />
          <h1 className="text-2xl font-bold text-white">Mantenimiento</h1>
        </div>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium text-white transition-colors"
        >
          <Plus size={16} />
          Registrar Mantenimiento
        </button>
      </div>

      <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden">
        {loading ? (
          <div className="text-center py-12 text-slate-400">Cargando...</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 bg-slate-900/40">
                <th className="text-left px-4 py-3 text-slate-400 font-medium w-12">ID</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Patente</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium w-32">Tipo</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Descripción</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium w-28">Fecha</th>
                <th className="text-right px-4 py-3 text-slate-400 font-medium w-24">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {registros.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-12 text-slate-500">
                    No hay registros de mantenimiento.
                  </td>
                </tr>
              ) : (
                registros.map(r => (
                  <tr key={r.id} className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors">
                    <td className="px-4 py-3 text-slate-500 text-xs">{r.id}</td>
                    <td className="px-4 py-3">
                      <span className="font-bold text-blue-300 font-mono tracking-wider text-sm">{r.patente}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-md text-xs font-medium border ${TIPO_COLORS[r.tipo] || 'bg-slate-700 text-slate-300'}`}>
                        {r.tipo}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-300 max-w-xs truncate" title={r.descripcion}>
                      {r.descripcion || <span className="text-slate-600">—</span>}
                    </td>
                    <td className="px-4 py-3 text-slate-400 text-xs">{r.fecha}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(r)}
                        className="text-blue-400 hover:text-blue-300 transition-colors mr-3 p-1 rounded hover:bg-blue-900/20"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => handleDelete(r)}
                        className="text-red-400 hover:text-red-300 transition-colors p-1 rounded hover:bg-red-900/20"
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
      <p className="text-xs text-slate-500 mt-2">{registros.length} registro(s) de mantenimiento</p>

      {/* Modal */}
      <Modal
        isOpen={modal.open}
        onClose={closeModal}
        title={modal.editing ? 'Editar Mantenimiento' : 'Registrar Mantenimiento'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
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

            {/* Tipo */}
            <div>
              <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                Tipo <span className="text-red-400">*</span>
              </label>
              <select
                value={form.tipo}
                onChange={e => setForm({ ...form, tipo: e.target.value })}
                required
                className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                           focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
              >
                {TIPOS.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
          </div>

          {/* Fecha */}
          <div>
            <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
              Fecha <span className="text-red-400">*</span>
            </label>
            <input
              type="date"
              value={form.fecha}
              onChange={e => setForm({ ...form, fecha: e.target.value })}
              required
              className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                         focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
            />
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
              Descripción
            </label>
            <textarea
              value={form.descripcion}
              onChange={e => setForm({ ...form, descripcion: e.target.value })}
              rows={3}
              placeholder="Describe el trabajo realizado o a realizar..."
              className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                         placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30
                         resize-none"
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={closeModal}
              className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors rounded-lg hover:bg-slate-700"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-60 rounded-lg text-sm font-medium text-white transition-colors"
            >
              {saving ? 'Guardando...' : modal.editing ? 'Actualizar' : 'Registrar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
