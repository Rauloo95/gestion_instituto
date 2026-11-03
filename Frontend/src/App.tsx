import { useState } from 'react'
import { GraduationCap, Server, ShieldCheck, CheckCircle2, AlertCircle, ArrowRight } from 'lucide-react'

function App() {
  const [backendStatus, setBackendStatus] = useState<'idle' | 'checking' | 'connected' | 'error'>('idle')
  const [errorMessage, setErrorMessage] = useState<string>('')

  const checkBackend = async () => {
    setBackendStatus('checking')
    setErrorMessage('')
    try {
      // Intentamos una petición al endpoint público de login con credenciales de prueba para verificar proxy y conectividad
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'test@baleras.com', password: 'test' })
      })

      // Si responde (incluso 401 Unauthorized por contraseña incorrecta), significa que el backend y el proxy funcionan
      if (res.status === 401 || res.status === 200 || res.status === 400) {
        setBackendStatus('connected')
      } else {
        setBackendStatus('error')
        setErrorMessage(`El servidor respondió con código HTTP ${res.status}`)
      }
    } catch (err: unknown) {
      setBackendStatus('error')
      setErrorMessage(err instanceof Error ? err.message : 'No se pudo conectar con el backend en http://localhost:8080')
    }
  }

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col justify-between p-6 md:p-12 font-sans">
      <header className="max-w-5xl mx-auto w-full flex items-center justify-between py-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-blue-600 rounded-xl shadow-lg shadow-blue-500/20">
            <GraduationCap className="w-7 h-7 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight">Gestión Baleras</h1>
            <p className="text-xs text-slate-400">Portal Académico & API Frontend</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
            Vite + React OK
          </span>
        </div>
      </header>

      <main className="max-w-5xl mx-auto w-full my-auto py-12">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 mb-4">
            🚀 Frontend Inicializado con Éxito
          </div>
          <h2 className="text-4xl md:text-5xl font-extrabold tracking-tight text-white mb-4">
            Ecosistema Frontend <span className="text-blue-500">Gestión Baleras</span>
          </h2>
          <p className="text-slate-400 text-base md:text-lg">
            Stack moderno con <span className="text-slate-200 font-semibold">React 19</span>,{' '}
            <span className="text-slate-200 font-semibold">TypeScript</span>,{' '}
            <span className="text-slate-200 font-semibold">Vite</span> y{' '}
            <span className="text-slate-200 font-semibold">Tailwind CSS v4</span>.
          </p>
        </div>

        {/* Tarjetas de estado */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 hover:border-blue-500/50 transition-all">
            <div className="w-10 h-10 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center mb-4">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">Seguridad JWT</h3>
            <p className="text-sm text-slate-400">
              Integración con tokens de autenticación stateless y roles RBAC (Admin, Profesor, Alumno).
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 hover:border-blue-500/50 transition-all">
            <div className="w-10 h-10 rounded-lg bg-purple-500/10 text-purple-400 flex items-center justify-center mb-4">
              <Server className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">Proxy Vite Configurado</h3>
            <p className="text-sm text-slate-400">
              Las llamadas a <code className="text-xs bg-slate-700 px-1.5 py-0.5 rounded text-purple-300">/api/*</code> se redirigen automáticamente a <code className="text-xs bg-slate-700 px-1.5 py-0.5 rounded text-purple-300">localhost:8080</code> sin CORS.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 hover:border-blue-500/50 transition-all">
            <div className="w-10 h-10 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-4">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">Tailwind CSS v4</h3>
            <p className="text-sm text-slate-400">
              Estilos optimizados con la nueva arquitectura nativa de Tailwind y Vite sin configuración extra.
            </p>
          </div>
        </div>

        {/* Comprobador de conexión con Backend */}
        <div className="max-w-xl mx-auto p-6 rounded-2xl bg-slate-800/90 border border-slate-700 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <span className="text-sm font-semibold text-slate-300">Test de Conexión Backend</span>
            <span className="text-xs text-slate-400">Target: http://localhost:8080</span>
          </div>

          <button
            onClick={checkBackend}
            disabled={backendStatus === 'checking'}
            className="w-full flex items-center justify-center gap-2 py-3 px-4 bg-blue-600 hover:bg-blue-500 active:scale-[0.99] disabled:opacity-50 text-white font-medium rounded-xl transition-all shadow-md shadow-blue-600/30 cursor-pointer"
          >
            {backendStatus === 'checking' ? 'Comprobando conexión...' : 'Probar conexión con Spring Boot (/api/auth)'}
            <ArrowRight className="w-4 h-4" />
          </button>

          {backendStatus === 'connected' && (
            <div className="mt-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>¡Conexión establecida con éxito! El backend en Spring Boot y el proxy responden correctamente.</span>
            </div>
          )}

          {backendStatus === 'error' && (
            <div className="mt-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMessage || 'Asegúrate de que la aplicación Spring Boot está corriendo en http://localhost:8080'}</span>
            </div>
          )}
        </div>
      </main>

      <footer className="max-w-5xl mx-auto w-full text-center py-4 border-t border-slate-800 text-xs text-slate-500">
        Gestión Baleras © 2026 - Frontend React + Tailwind + Vite + TypeScript
      </footer>
    </div>
  )
}

export default App
