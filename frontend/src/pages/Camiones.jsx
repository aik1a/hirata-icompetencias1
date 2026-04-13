import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, Truck } from 'lucide-react'
import Modal from '../components/Modal'
import * as api from '../api'

const EMPTY = { patente: '', marca: '', modelo: '', anio: new Date().getFullYear().toString(), conductorId: '' }

export default function Camiones() {
  const [camiones, setCamiones] = useState([])
  const [conductores, setConductores] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState({ open: false, editing: null })
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([api.getCamiones(), api.getConductores()])
      .then(([c, d]) => { setCamiones(c); setConductores(d) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setForm(EMPTY)
    setModal({ open: true, editing: null })
  }

  const openEdit = (c) => {
    setForm({
      patente:     c.patente,
      marca:       c.marca,
      modelo:      c.modelo,
      anio:        String(c.anio),
      conductorId: c.conductorId != null ? String(c.conductorId) : '',
    })
    setModal({ open: true, editing: c })
  }

  const closeModal = () => setModal({ open: false, editing: null })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = { ...form, anio: parseInt(form.anio) }
      if (modal.editing) {
        await api.updateCamion(modal.editing.id, payload)
      } else {
        await api.createCamion(payload)
      }
      closeModal()
      load()
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (c) => {
    if (!confirm(`¿Eliminar el camión "${c.patente}"?`)) return
    const res = await api.deleteCamion(c.id)
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
          <Truck size={22} className="text-blue-400" />
          <h1 className="text-2xl font-bold text-white">Camiones</h1>
        </div>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium text-white transition-colors"
        >
          <Plus size={16} />
          Agregar Camión
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
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Marca</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Modelo</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Año</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Conductor</th>
                <th className="text-right px-4 py-3 text-slate-400 font-medium w-24">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {camiones.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-500">
                    No hay camiones registrados.
                  </td>
                </tr>
              ) : (
                camiones.map(c => (
                  <tr key={c.id} className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors">
                    <td className="px-4 py-3 text-slate-500 text-xs">{c.id}</td>
                    <td className="px-4 py-3">
                      <span className="font-bold text-blue-300 font-mono tracking-wider text-sm">{c.patente}</span>
                    </td>
                    <td className="px-4 py-3 text-white font-medium">{c.marca}</td>
                    <td className="px-4 py-3 text-slate-300">{c.modelo}</td>
                    <td className="px-4 py-3 text-slate-300">{c.anio}</td>
                    <td className="px-4 py-3">
                      {c.conductorNombre === 'Sin asignar'
                        ? <span className="text-slate-500 text-xs">Sin asignar</span>
                        : <span className="text-green-300 text-sm">{c.conductorNombre}</span>
                      }
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(c)}
                        className="text-blue-400 hover:text-blue-300 transition-colors mr-3 p-1 rounded hover:bg-blue-900/20"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => handleDelete(c)}
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
      <p className="text-xs text-slate-500 mt-2">{camiones.length} camión(es) registrado(s)</p>

      {/* Modal */}
      <Modal
        isOpen={modal.open}
        onClose={closeModal}
        title={modal.editing ? `Editar: ${modal.editing.patente}` : 'Agregar Camión'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Patente */}
            <div>
              <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                Patente <span className="text-red-400">*</span>
              </label>
              <input
                type="text"
                value={form.patente}
                onChange={e => setForm({ ...form, patente: e.target.value.toUpperCase() })}
                required
                placeholder="ABCD12"
                className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm font-mono
                           placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
              />
            </div>
            {/* Año */}
            <div>
              <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                Año <span className="text-red-400">*</span>
              </label>
              <input
                type="number"
                value={form.anio}
                onChange={e => setForm({ ...form, anio: e.target.value })}
                required
                min="1990"
                max="2030"
                className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                           focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
              />
            </div>
          </div>

          {/* Marca */}
          <div>
            <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
              Marca <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={form.marca}
              onChange={e => setForm({ ...form, marca: e.target.value })}
              required
              placeholder="Ej: Volvo, Mercedes-Benz"
              className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                         placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
            />
          </div>

          {/* Modelo */}
          <div>
            <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
              Modelo <span className="text-red-400">*</span>
            </label>
            <input
              type="text"
              value={form.modelo}
              onChange={e => setForm({ ...form, modelo: e.target.value })}
              required
              placeholder="Ej: FH16, Actros"
              className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                         placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
            />
          </div>

          {/* Conductor */}
          <div>
            <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
              Conductor asignado
            </label>
            <select
              value={form.conductorId}
              onChange={e => setForm({ ...form, conductorId: e.target.value })}
              className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                         focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
            >
              <option value="">Sin conductor asignado</option>
              {conductores.map(c => (
                <option key={c.id} value={c.id}>{c.nombre} — {c.rut}</option>
              ))}
            </select>
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
              {saving ? 'Guardando...' : modal.editing ? 'Actualizar' : 'Agregar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
