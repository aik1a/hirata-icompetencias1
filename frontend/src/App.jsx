import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Dashboard from './pages/Dashboard'
import Conductores from './pages/Conductores'
import Camiones from './pages/Camiones'
import Kilometraje from './pages/Kilometraje'
import Mantenimiento from './pages/Mantenimiento'

function App() {
  return (
    <BrowserRouter>
      <div style={{ display: 'flex', height: '100vh', width: '100%' }}>
        <Sidebar />
        <main
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: '24px',
            backgroundColor: '#0f172a',
          }}
        >
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard"     element={<Dashboard />} />
            <Route path="/conductores"   element={<Conductores />} />
            <Route path="/camiones"      element={<Camiones />} />
            <Route path="/kilometraje"   element={<Kilometraje />} />
            <Route path="/mantenimiento" element={<Mantenimiento />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App
