const BASE = '/api'

const headers = { 'Content-Type': 'application/json' }

// Verifica que la respuesta sea exitosa; si no, lanza un Error con el mensaje del servidor.
const checkOk = async (res) => {
  if (!res.ok) {
    let msg = `Error HTTP ${res.status}`
    try { const d = await res.json(); if (d.error) msg = d.error } catch { /* sin body */ }
    throw new Error(msg)
  }
  try { return await res.json() } catch { return {} }
}

// ── Dashboard ────────────────────────────────────────────────────────────────
export const getDashboard = () =>
  fetch(`${BASE}/dashboard`).then(r => r.json())

// ── Conductores ──────────────────────────────────────────────────────────────
export const getConductores = () =>
  fetch(`${BASE}/conductores`).then(r => r.json())

export const createConductor = (data) =>
  fetch(`${BASE}/conductores`, { method: 'POST', headers, body: JSON.stringify(data) }).then(checkOk)

export const updateConductor = (id, data) =>
  fetch(`${BASE}/conductores/${id}`, { method: 'PUT', headers, body: JSON.stringify(data) }).then(checkOk)

export const deleteConductor = (id) =>
  fetch(`${BASE}/conductores/${id}`, { method: 'DELETE' })

// ── Camiones ─────────────────────────────────────────────────────────────────
export const getCamiones = () =>
  fetch(`${BASE}/camiones`).then(r => r.json())

export const createCamion = (data) =>
  fetch(`${BASE}/camiones`, { method: 'POST', headers, body: JSON.stringify(data) }).then(checkOk)

export const updateCamion = (id, data) =>
  fetch(`${BASE}/camiones/${id}`, { method: 'PUT', headers, body: JSON.stringify(data) }).then(checkOk)

export const deleteCamion = (id) =>
  fetch(`${BASE}/camiones/${id}`, { method: 'DELETE' })

// ── Kilometraje ───────────────────────────────────────────────────────────────
export const getKilometraje = () =>
  fetch(`${BASE}/kilometraje`).then(r => r.json())

export const createKilometraje = (data) =>
  fetch(`${BASE}/kilometraje`, { method: 'POST', headers, body: JSON.stringify(data) }).then(checkOk)

export const getKilometrajeTotalPorPatente = (patente) =>
  fetch(`${BASE}/kilometraje/total/${encodeURIComponent(patente)}`).then(r => r.json())

// ── Mantenimiento ─────────────────────────────────────────────────────────────
export const getMantenimiento = () =>
  fetch(`${BASE}/mantenimiento`).then(r => r.json())

export const createMantenimiento = (data) =>
  fetch(`${BASE}/mantenimiento`, { method: 'POST', headers, body: JSON.stringify(data) }).then(checkOk)

export const updateMantenimiento = (id, data) =>
  fetch(`${BASE}/mantenimiento/${id}`, { method: 'PUT', headers, body: JSON.stringify(data) }).then(checkOk)

export const deleteMantenimiento = (id) =>
  fetch(`${BASE}/mantenimiento/${id}`, { method: 'DELETE' })
