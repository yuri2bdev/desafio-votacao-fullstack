import { Link, NavLink, Outlet } from 'react-router-dom'

function Layout() {
  const activeMainLink =
    'text-emerald-700 dark:text-emerald-400 border-b-2 border-emerald-700 pb-1 font-manrope tracking-tight font-bold text-sm transition-all duration-200 hover:opacity-80'
  const inactiveMainLink =
    'text-slate-500 dark:text-slate-400 hover:text-emerald-900 dark:hover:text-emerald-100 font-manrope tracking-tight font-bold text-sm transition-all duration-200 hover:opacity-80'

  const activeSideLink =
    'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-900 dark:text-emerald-400 font-bold rounded-lg px-4 py-3 flex items-center gap-3 transition-colors hover:translate-x-1 transition-transform'
  const inactiveSideLink =
    'text-slate-600 dark:text-slate-400 px-4 py-3 hover:bg-slate-100 dark:hover:bg-slate-900 rounded-lg flex items-center gap-3 transition-colors hover:translate-x-1 transition-transform'

  return (
    <div className="bg-surface text-on-surface min-h-screen">
      <header className="sticky top-0 z-50 w-full bg-white/80 dark:bg-slate-900/80 backdrop-blur-md shadow-sm dark:shadow-none">
        <div className="flex items-center justify-between px-6 py-4 max-w-7xl mx-auto">
          <div className="flex items-center gap-8">
            <span className="text-2xl font-black tracking-tighter text-emerald-900 dark:text-emerald-50">
              Cooperativa
            </span>
            <nav className="hidden md:flex gap-6">
              <NavLink to="/" className={({ isActive }) => (isActive ? activeMainLink : inactiveMainLink)} end>
                Dashboard
              </NavLink>
              <Link to="/" className={inactiveMainLink}>
                Historico
              </Link>
              <Link to="/" className={inactiveMainLink}>
                Relatorios
              </Link>
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden md:flex gap-2">
              <button className="p-2 text-on-surface-variant hover:bg-surface-container rounded-full transition-all active:scale-95" type="button">
                <span className="material-symbols-outlined">notifications</span>
              </button>
              <button className="p-2 text-on-surface-variant hover:bg-surface-container rounded-full transition-all active:scale-95" type="button">
                <span className="material-symbols-outlined">account_circle</span>
              </button>
            </div>
            <Link
              to="/pautas/nova"
              className="bg-primary text-on-primary px-5 py-2.5 rounded-xl font-bold text-sm transition-all duration-200 hover:opacity-90 active:scale-95 flex items-center gap-2"
            >
              <span className="material-symbols-outlined text-sm">add</span>
              Nova Pauta
            </Link>
          </div>
        </div>
        <div className="bg-slate-100 dark:bg-slate-800 h-[1px] w-full" />
      </header>

      <div className="flex max-w-7xl mx-auto">
        <aside className="fixed left-0 top-0 h-screen w-64 hidden lg:flex flex-col p-4 bg-slate-50 dark:bg-slate-950 border-r border-slate-200 dark:border-slate-800 pt-24">
          <div className="mb-8 px-4">
            <h3 className="text-emerald-900 font-bold text-lg">Portal do Cooperado</h3>
            <p className="text-xs text-on-surface-variant">Unidade Central</p>
          </div>

          <nav className="flex-1 space-y-1">
            <NavLink to="/" className={({ isActive }) => (isActive ? activeSideLink : inactiveSideLink)} end>
              <span className="material-symbols-outlined">how_to_vote</span>
              <span className="font-manrope font-medium text-sm">Votacoes Ativas</span>
            </NavLink>
            <NavLink to="/pautas/encerradas" className={({ isActive }) => (isActive ? activeSideLink : inactiveSideLink)}>
              <span className="material-symbols-outlined">archive</span>
              <span className="font-manrope font-medium text-sm">Encerradas</span>
            </NavLink>
            <Link to="/" className={inactiveSideLink}>
              <span className="material-symbols-outlined">group</span>
              <span className="font-manrope font-medium text-sm">Membros</span>
            </Link>
            <Link to="/" className={inactiveSideLink}>
              <span className="material-symbols-outlined">settings</span>
              <span className="font-manrope font-medium text-sm">Configuracoes</span>
            </Link>
          </nav>

          <div className="mt-auto">
            <Link to="/" className={inactiveSideLink}>
              <span className="material-symbols-outlined">help_outline</span>
              <span className="font-manrope font-medium text-sm">Suporte</span>
            </Link>
          </div>
        </aside>

        <main className="flex-1 lg:ml-64 p-6 md:p-12 pb-28 lg:pb-12">
          <Outlet />
        </main>
      </div>

      <nav className="fixed bottom-0 left-0 w-full z-50 flex justify-around items-center px-4 pb-6 pt-2 bg-white/90 dark:bg-slate-900/90 backdrop-blur-xl lg:hidden rounded-t-2xl shadow-[0_-4px_20px_rgba(0,0,0,0.05)] border-t border-slate-200 dark:border-slate-800">
        <NavLink
          to="/"
          className={({ isActive }) =>
            isActive
              ? 'flex flex-col items-center justify-center bg-emerald-100 dark:bg-emerald-900/40 text-emerald-900 dark:text-emerald-300 rounded-xl px-4 py-2 tap-highlight-transparent active:bg-slate-100 transition-all'
              : 'flex flex-col items-center justify-center text-slate-400 dark:text-slate-500 px-4 py-2 tap-highlight-transparent active:bg-slate-100 transition-all'
          }
          end
        >
          <span className="material-symbols-outlined">home</span>
          <span className="font-manrope text-[10px] uppercase tracking-widest font-bold mt-1">Inicio</span>
        </NavLink>
        <NavLink
          to="/pautas/1"
          className={({ isActive }) =>
            isActive
              ? 'flex flex-col items-center justify-center bg-emerald-100 dark:bg-emerald-900/40 text-emerald-900 dark:text-emerald-300 rounded-xl px-4 py-2 tap-highlight-transparent active:bg-slate-100 transition-all'
              : 'flex flex-col items-center justify-center text-slate-400 dark:text-slate-500 px-4 py-2 tap-highlight-transparent active:bg-slate-100 transition-all'
          }
        >
          <span className="material-symbols-outlined">ballot</span>
          <span className="font-manrope text-[10px] uppercase tracking-widest font-bold mt-1">Votar</span>
        </NavLink>
        <Link
          to="/"
          className="flex flex-col items-center justify-center text-slate-400 dark:text-slate-500 px-4 py-2 tap-highlight-transparent active:bg-slate-100 transition-all"
        >
          <span className="material-symbols-outlined">person</span>
          <span className="font-manrope text-[10px] uppercase tracking-widest font-bold mt-1">Perfil</span>
        </Link>
      </nav>
    </div>
  )
}

export default Layout

