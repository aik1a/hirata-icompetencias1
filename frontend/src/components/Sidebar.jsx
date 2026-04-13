import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Truck, Users, Gauge, Wrench } from 'lucide-react'

const navItems = [
  { to: '/dashboard',    label: 'Dashboard',       icon: LayoutDashboard },
  { to: '/camiones',     label: 'Camiones',         icon: Truck },
  { to: '/conductores',  label: 'Conductores',      icon: Users },
  { to: '/kilometraje',  label: 'Kilometraje',      icon: Gauge },
  { to: '/mantenimiento', label: 'Mantenimiento',   icon: Wrench },
]

export default function Sidebar() {
  return (
    <aside className="w-60 bg-slate-800 border-r border-slate-700 flex flex-col shrink-0">
      {/* Logo */}
      <div className="px-5 py-4 border-b border-slate-700">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-blue-600 rounded-lg flex items-center justify-center">
            <Truck size={20} className="text-white" />
          </div>
          <div>
            <p className="font-bold text-white text-sm leading-tight">Hirata Flota</p>
            <p className="text-xs text-slate-400">Sistema de Gestión</p>
          </div>
        </div>
      </div>

      {/* Navegación */}
      <nav className="flex-1 p-3 space-y-0.5">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-700'
              }`
            }
          >
            <Icon size={17} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-4 py-3 border-t border-slate-700">
        <p className="text-xs text-slate-500">Empresa de Transporte Hirata</p>
      </div>
    </aside>
  )
}
