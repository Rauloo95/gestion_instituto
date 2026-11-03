import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  GraduationCap,
  LogOut,
  Building2,
  BookOpen,
  Calendar,
  Users,
  Award,
  ClipboardList,
  Shield,
  UserCheck,
  Sparkles
} from 'lucide-react';
import type { Rol } from '../types/auth';

export function Dashboard() {
  const { user, logout, token } = useAuth();

  const getRoleBadge = (rol?: Rol) => {
    switch (rol) {
      case 'ADMIN':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-purple-500/10 text-purple-400 border border-purple-500/30">
            <Shield className="w-3.5 h-3.5" />
            ADMINISTRADOR
          </span>
        );
      case 'PROFESOR':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/30">
            <UserCheck className="w-3.5 h-3.5" />
            PROFESOR
          </span>
        );
      case 'ALUMNO':
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <GraduationCap className="w-3.5 h-3.5" />
            ALUMNO
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Barra de navegación superior */}
      <header className="bg-slate-900 border-b border-slate-800 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-600 rounded-xl shadow-md shadow-blue-500/20">
              <GraduationCap className="w-6 h-6 text-white" />
            </div>
            <div>
              <span className="text-lg font-bold text-white tracking-tight">Gestión Baleras</span>
              <span className="hidden sm:inline-block ml-2 text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
                Portal v1.0
              </span>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-sm font-semibold text-white">{user?.nombre}</span>
              <span className="text-xs text-slate-400">{user?.email}</span>
            </div>
            {getRoleBadge(user?.rol)}
            <button
              onClick={logout}
              title="Cerrar sesión"
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 border border-transparent hover:border-slate-700 transition cursor-pointer"
            >
              <LogOut className="w-5 h-5 text-rose-400" />
            </button>
          </div>
        </div>
      </header>

      {/* Contenido principal */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">
        {/* Banner de Bienvenida */}
        <div className="p-6 md:p-8 rounded-3xl bg-gradient-to-r from-blue-900/40 via-slate-900 to-slate-900 border border-blue-500/20 mb-8 shadow-xl">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-400 mb-2">
                <Sparkles className="w-4 h-4" />
                Sesión Iniciada Correctamente
              </div>
              <h1 className="text-2xl md:text-3xl font-extrabold text-white">
                ¡Bienvenido/a de nuevo, {user?.nombre}!
              </h1>
              <p className="mt-1 text-sm text-slate-400">
                Has accedido con el rol de <strong className="text-slate-200">{user?.rol}</strong>. Tienes acceso a los módulos correspondientes a tu perfil.
              </p>
            </div>
          </div>
        </div>

        {/* Sección de módulos según el rol */}
        <div className="mb-8">
          <h2 className="text-lg font-bold text-white mb-4">Tus Módulos Activos</h2>

          {user?.rol === 'ADMIN' && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              <Link
                to="/centros"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-blue-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Building2 className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-blue-400 transition-colors">Centros Educativos</h3>
                  <span className="text-[10px] text-blue-400 bg-blue-500/10 border border-blue-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Gestión de sedes, creación, actualización y consulta de centros activos.</p>
              </Link>

              <Link
                to="/formaciones"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-purple-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-purple-400 transition-colors">Formaciones & Asignaturas</h3>
                  <span className="text-[10px] text-purple-400 bg-purple-500/10 border border-purple-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Catálogo de titulaciones académicas y planes de estudios.</p>
              </Link>

              <Link
                to="/clases"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-amber-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Calendar className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-amber-400 transition-colors">Clases & Aulas</h3>
                  <span className="text-[10px] text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Asignación de aulas, grupos y profesores titulares por asignatura.</p>
              </Link>

              <Link
                to="/matriculas"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-emerald-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <ClipboardList className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-emerald-400 transition-colors">Matrículas</h3>
                  <span className="text-[10px] text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Inscripción y gestión de estados de alumnos en las diferentes clases.</p>
              </Link>

              <Link
                to="/usuarios"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-rose-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Users className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-rose-400 transition-colors">Gestión de Usuarios</h3>
                  <span className="text-[10px] text-rose-400 bg-rose-500/10 border border-rose-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Altas, bajas y modificación de cuentas (Administradores, Profesores, Alumnos).</p>
              </Link>

              <Link
                to="/notas"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-cyan-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Award className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-cyan-400 transition-colors">Auditoría de Notas</h3>
                  <span className="text-[10px] text-cyan-400 bg-cyan-500/10 border border-cyan-500/20 px-2 py-0.5 rounded-full font-medium">Ver módulo →</span>
                </div>
                <p className="text-xs text-slate-400">Consulta global de expedientes y calificaciones emitidas.</p>
              </Link>
            </div>
          )}

          {user?.rol === 'PROFESOR' && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              <Link
                to="/centros"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-blue-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Building2 className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-blue-400 transition-colors">Centros Educativos</h3>
                  <span className="text-[10px] text-blue-400 bg-blue-500/10 border border-blue-500/20 px-2 py-0.5 rounded-full font-medium">Consultar →</span>
                </div>
                <p className="text-xs text-slate-400">Consulta de sedes educativas y centros adscritos.</p>
              </Link>

              <Link
                to="/formaciones"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-purple-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <BookOpen className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-purple-400 transition-colors">Formaciones & Asignaturas</h3>
                  <span className="text-[10px] text-purple-400 bg-purple-500/10 border border-purple-500/20 px-2 py-0.5 rounded-full font-medium">Consultar →</span>
                </div>
                <p className="text-xs text-slate-400">Catálogo de titulaciones académicas y asignaturas impartidas.</p>
              </Link>

              <Link
                to="/clases"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-amber-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Calendar className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-amber-400 transition-colors">Mis Clases Asignadas</h3>
                  <span className="text-[10px] text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-full font-medium">Gestionar →</span>
                </div>
                <p className="text-xs text-slate-400">Consulta los grupos, aulas y horarios donde figuras como docente titular.</p>
              </Link>

              <Link
                to="/notas"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-emerald-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Award className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-emerald-400 transition-colors">Calificar Alumnos</h3>
                  <span className="text-[10px] text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full font-medium">Evaluar →</span>
                </div>
                <p className="text-xs text-slate-400">Emite y actualiza las notas y observaciones de tus alumnos matriculados.</p>
              </Link>
            </div>
          )}

          {user?.rol === 'ALUMNO' && (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <Link
                to="/notas"
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-cyan-500/50 hover:bg-slate-800/50 transition-all shadow-md group block"
              >
                <div className="w-10 h-10 rounded-xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                  <Award className="w-5 h-5" />
                </div>
                <div className="flex items-center justify-between mb-1">
                  <h3 className="font-semibold text-white group-hover:text-cyan-400 transition-colors">Mis Calificaciones</h3>
                  <span className="text-[10px] text-cyan-400 bg-cyan-500/10 border border-cyan-500/20 px-2 py-0.5 rounded-full font-medium">Consultar →</span>
                </div>
                <p className="text-xs text-slate-400">Consulta en tiempo real las notas y comentarios de tus profesores.</p>
              </Link>

              <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-blue-500/50 transition-all shadow-md">
                <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center mb-4">
                  <ClipboardList className="w-5 h-5" />
                </div>
                <h3 className="font-semibold text-white mb-1">Mis Matrículas</h3>
                <p className="text-xs text-slate-400">Revisa las asignaturas y clases en las que estás actualmente matriculado.</p>
              </div>
            </div>
          )}
        </div>

        {/* Diagnóstico de Sesión JWT */}
        <div className="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 text-xs text-slate-400">
          <div className="flex items-center justify-between mb-2">
            <span className="font-semibold text-slate-300">Detalles de Autenticación JWT</span>
            <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Token Activo
            </span>
          </div>
          <p className="font-mono truncate bg-slate-950 p-2.5 rounded-lg border border-slate-800/80 text-slate-300">
            {token}
          </p>
        </div>
      </main>

      <footer className="border-t border-slate-900 py-4 text-center text-xs text-slate-500">
        Gestión Baleras © 2026 - Sesión iniciada como {user?.email}
      </footer>
    </div>
  );
}

