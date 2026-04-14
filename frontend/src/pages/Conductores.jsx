import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, Users } from 'lucide-react'
import Modal from '../components/Modal'
import * as api from '../api'

const EMPTY = { nombre: '', rut: '', telefono: '', email: '' }

const FIELDS = [
  { key: 'nombre',   label: 'Nombre completo', required: true,  type: 'text' },
  { key: 'rut',      label: 'RUT',              required: true,  type: 'text',  placeholder: '12345678-9' },
  { key: 'telefono', label: 'Teléfono',         required: false, type: 'tel' },
  { key: 'email',    label: 'Email',             required: false, type: 'email' },
]

export default function Conductores() {
  const [conductores, setConductores] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState({ open: false, editing: null })
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const load = () => {
    setLoading(true)
    api.getConductores().then(setConductores).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setForm(EMPTY)
    setError(null)
    setModal({ open: true, editing: null })
  }

  const openEdit = (c) => {
    setForm({ nombre: c.nombre, rut: c.rut, telefono: c.telefono || '', email: c.email || '' })
    setError(null)
    setModal({ open: true, editing: c })
  }

  const closeModal = () => setModal({ open: false, editing: null })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      if (modal.editing) {
        await api.updateConductor(modal.editing.id, form)
      } else {
        await api.createConductor(form)
      }
      closeModal()
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (c) => {
    if (!confirm(`¿Eliminar al conductor "${c.nombre}"?`)) return
    const res = await api.deleteConductor(c.id)
    if (res.status === 409) {
      const data = await res.json()
      alert('Error: ' + data.error)
    } else {
      load()
    }
  }

  return (
    <div className="max-w-5xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Users size={22} className="text-green-400" />
          <h1 className="text-2xl font-bold text-white">Conductores</h1>
        </div>
        <button
          onClick={openAdd}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg text-sm font-medium text-white transition-colors"
        >
          <Plus size={16} />
          Agregar Conductor
        </button>
      </div>

      {/* Tabla */}
      <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden">
        {loading ? (
          <div className="text-center py-12 text-slate-400">Cargando...</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700 bg-slate-900/40">
                <th className="text-left px-4 py-3 text-slate-400 font-medium w-12">ID</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Nombre</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">RUT</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Teléfono</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Email</th>
                <th className="text-right px-4 py-3 text-slate-400 font-medium w-24">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {conductores.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-12 text-slate-500">
                    No hay conductores registrados.
                  </td>
                </tr>
              ) : (
                conductores.map(c => (
                  <tr
                    key={c.id}
                    className="border-b border-slate-700/50 hover:bg-slate-700/20 transition-colors"
                  >
                    <td className="px-4 py-3 text-slate-500 text-xs">{c.id}</td>
                    <td className="px-4 py-3 font-medium text-white">{c.nombre}</td>
                    <td className="px-4 py-3 text-slate-300 font-mono text-xs">{c.rut}</td>
                    <td className="px-4 py-3 text-slate-300">{c.telefono || <span className="text-slate-600">—</span>}</td>
                    <td className="px-4 py-3 text-slate-300">{c.email || <span className="text-slate-600">—</span>}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(c)}
                        className="text-blue-400 hover:text-blue-300 transition-colors mr-3 p-1 rounded hover:bg-blue-900/20"
                        title="Editar"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        onClick={() => handleDelete(c)}
                        className="text-red-400 hover:text-red-300 transition-colors p-1 rounded hover:bg-red-900/20"
                        title="Eliminar"
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
      <p className="text-xs text-slate-500 mt-2">{conductores.length} conductor(es) registrado(s)</p>

      {/* Modal agregar/editar */}
      <Modal
        isOpen={modal.open}
        onClose={closeModal}
        title={modal.editing ? `Editar: ${modal.editing.nombre}` : 'Agregar Conductor'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {FIELDS.map(({ key, label, required, type, placeholder }) => (
            <div key={key}>
              <label className="block text-xs text-slate-400 mb-1.5 font-medium uppercase tracking-wide">
                {label} {required && <span className="text-red-400">*</span>}
              </label>
              <input
                type={type}
                value={form[key]}
                onChange={e => setForm({ ...form, [key]: e.target.value })}
                required={required}
                placeholder={placeholder}
                className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2.5 text-white text-sm
                           placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30
                           transition-colors"
              />
            </div>
          ))}
          {error && (
            <p className="text-sm text-red-400 bg-red-900/20 border border-red-500/30 rounded-lg px-3 py-2">
              {error}
            </p>
          )}
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
