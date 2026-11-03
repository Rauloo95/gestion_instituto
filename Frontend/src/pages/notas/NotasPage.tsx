import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { notaService } from '../../api/notaService';
import { matriculaService } from '../../api/matriculaService';
import { getApiErrorMessage } from '../../api/client';
import type { Nota, NotaRequest } from '../../types/nota';
import type { Matricula } from '../../types/matricula';
import { NotaModal } from './NotaModal';
import {
  Award,
  BookOpen,
  Calendar,
  User,
  GraduationCap,
  Plus,
  Search,
  Pencil,
  Trash2,
  ArrowLeft,
  Loader2,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Sparkles,
  TrendingUp,
  FileText,
  XCircle,
  X
} from 'lucide-react';

export function NotasPage() {
  const { user, hasRole } = useAuth();
  const isProfesor = hasRole('PROFESOR');
  const isAlumno = hasRole('ALUMNO');

  // Datos
  const [notas, setNotas] = useState<Nota[]>([]);
  const [matriculas, setMatriculas] = useState<Matricula[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [calificacionFiltro, setCalificacionFiltro] = useState<
    'ALL' | 'APROBADOS' | 'SUSPENSOS' | 'NOTABLES' | 'SOBRESALIENTES'
  >('ALL');
  const [selectedAsignatura, setSelectedAsignatura] = useState<string | 'ALL'>('ALL');

  // Modales
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingNota, setEditingNota] = useState<Nota | null>(null);
  const [deletingNota, setDeletingNota] = useState<Nota | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchNotas = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      if (isAlumno) {
        const misNotasData = await notaService.getMisNotas();
        setNotas(misNotasData);
      } else {
        const [notasData, matsData] = await Promise.all([
          notaService.getNotas(),
          matriculaService.getMatriculas().catch(() => [] as Matricula[])
        ]);
        setNotas(notasData);
        setMatriculas(matsData);
      }
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudieron sincronizar las calificaciones.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchNotas();
  }, [user?.rol]);

  // Manejo de Modal (Creación / Edición)
  const handleOpenCreateModal = () => {
    setEditingNota(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (n: Nota) => {
    setEditingNota(n);
    setIsModalOpen(true);
  };

  const handleSaveNota = async (data: NotaRequest) => {
    if (editingNota) {
      await notaService.updateNota(editingNota.id, data);
      setSuccessMessage('Calificación actualizada con éxito.');
    } else {
      await notaService.createNota(data);
      setSuccessMessage('Calificación emitida y guardada en el expediente.');
    }
    await fetchNotas();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDelete = async () => {
    if (!deletingNota) return;
    setIsDeleting(true);
    try {
      await notaService.deleteNota(deletingNota.id);
      setSuccessMessage('Calificación eliminada correctamente.');
      setDeletingNota(null);
      await fetchNotas();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar la calificación.'));
    } finally {
      setIsDeleting(false);
    }
  };

  // Asignaturas únicas para filtro
  const uniqueAsignaturas = useMemo(() => {
    const set = new Set<string>();
    for (const n of notas) {
      if (n.asignaturaNombre) {
        set.add(n.asignaturaNombre);
      }
    }
    return Array.from(set).sort();
  }, [notas]);

  // Cálculos de estadísticas y métricas
  const stats = useMemo(() => {
    if (notas.length === 0) {
      return { media: '0.0', aprobados: 0, suspensos: 0, sobresalientes: 0, tasaAprobados: 0 };
    }

    let suma = 0;
    let aprobados = 0;
    let suspensos = 0;
    let sobresalientes = 0;

    for (const n of notas) {
      const v = n.valor || 0;
      suma += v;
      if (v >= 5.0) aprobados++;
      else suspensos++;
      if (v >= 9.0) sobresalientes++;
    }

    const media = (suma / notas.length).toFixed(2);
    const tasaAprobados = Math.round((aprobados / notas.length) * 100);

    return { media, aprobados, suspensos, sobresalientes, tasaAprobados };
  }, [notas]);

  // Filtrado reactivo de notas
  const filteredNotas = useMemo(() => {
    return notas.filter((n) => {
      const term = searchTerm.toLowerCase();
      const matchSearch =
        (n.alumnoNombre && n.alumnoNombre.toLowerCase().includes(term)) ||
        (n.asignaturaNombre && n.asignaturaNombre.toLowerCase().includes(term)) ||
        (n.claseNombre && n.claseNombre.toLowerCase().includes(term)) ||
        (n.profesorNombre && n.profesorNombre.toLowerCase().includes(term)) ||
        (n.observaciones && n.observaciones.toLowerCase().includes(term));

      const matchAsignatura =
        selectedAsignatura === 'ALL' || n.asignaturaNombre === selectedAsignatura;

      let matchRango = true;
      const v = n.valor || 0;
      if (calificacionFiltro === 'APROBADOS') matchRango = v >= 5.0;
      else if (calificacionFiltro === 'SUSPENSOS') matchRango = v < 5.0;
      else if (calificacionFiltro === 'NOTABLES') matchRango = v >= 7.0 && v < 9.0;
      else if (calificacionFiltro === 'SOBRESALIENTES') matchRango = v >= 9.0;

      return matchSearch && matchAsignatura && matchRango;
    });
  }, [notas, searchTerm, selectedAsignatura, calificacionFiltro]);

  const hasActiveFilters =
    searchTerm !== '' || selectedAsignatura !== 'ALL' || calificacionFiltro !== 'ALL';

  const getBadgeStyle = (valor: number) => {
    if (valor < 5.0) {
      return {
        bg: 'bg-rose-500/10 text-rose-300 border-rose-500/30',
        label: 'Suspenso',
        boxBg: 'bg-rose-500/20 text-rose-300 border-rose-500/40'
      };
    }
    if (valor < 7.0) {
      return {
        bg: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
        label: 'Aprobado',
        boxBg: 'bg-amber-500/20 text-amber-300 border-amber-500/40'
      };
    }
    if (valor < 9.0) {
      return {
        bg: 'bg-blue-500/10 text-blue-300 border-blue-500/30',
        label: 'Notable',
        boxBg: 'bg-blue-500/20 text-blue-300 border-blue-500/40'
      };
    }
    return {
      bg: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30',
      label: 'Sobresaliente',
      boxBg: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
    };
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Barra Superior */}
      <header className="bg-slate-900 border-b border-slate-800 sticky top-0 z-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link
              to="/dashboard"
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition flex items-center gap-1.5 text-xs font-semibold"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Volver</span>
            </Link>
            <div className="h-6 w-px bg-slate-800" />
            <div className="flex items-center gap-2.5">
              <div className="p-2 bg-gradient-to-tr from-cyan-600 to-blue-600 rounded-xl shadow-md shadow-cyan-500/20">
                <Award className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">
                {isAlumno
                  ? 'Mis Calificaciones'
                  : isProfesor
                  ? 'Evaluación y Calificaciones'
                  : 'Auditoría General de Notas'}
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchNotas}
              disabled={isLoading}
              title="Refrescar notas"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isProfesor && (
              <button
                onClick={handleOpenCreateModal}
                className="flex items-center gap-2 px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-cyan-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Calificar Alumno</span>
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Contenido Principal */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">
        {/* Notificaciones */}
        {successMessage && (
          <div className="mb-6 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-3 animate-in fade-in">
            <CheckCircle2 className="w-5 h-5 shrink-0 text-emerald-400" />
            <span>{successMessage}</span>
          </div>
        )}

        {errorMessage && (
          <div className="mb-6 p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center justify-between gap-3 animate-in fade-in">
            <div className="flex items-center gap-3">
              <AlertCircle className="w-5 h-5 shrink-0 text-rose-400" />
              <span>{errorMessage}</span>
            </div>
            <button
              onClick={fetchNotas}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Tarjetas de Métricas de Rendimiento */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800/80 flex items-center gap-3.5">
            <div className="p-3 rounded-xl bg-cyan-500/10 text-cyan-400">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Nota Media</p>
              <div className="flex items-baseline gap-1">
                <span className="text-xl font-extrabold text-white">{stats.media}</span>
                <span className="text-xs text-slate-500">/ 10</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setCalificacionFiltro('APROBADOS')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              calificacionFiltro === 'APROBADOS'
                ? 'bg-emerald-950/40 border-emerald-500/40 ring-1 ring-emerald-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Aprobados</p>
              <div className="flex items-baseline gap-1.5">
                <span className="text-xl font-bold text-emerald-300">{stats.aprobados}</span>
                <span className="text-xs text-emerald-400/80 font-medium">({stats.tasaAprobados}%)</span>
              </div>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setCalificacionFiltro('SUSPENSOS')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              calificacionFiltro === 'SUSPENSOS'
                ? 'bg-rose-950/40 border-rose-500/40 ring-1 ring-rose-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400">
              <XCircle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Suspensos</p>
              <span className="text-xl font-bold text-rose-300">{stats.suspensos}</span>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setCalificacionFiltro('SOBRESALIENTES')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              calificacionFiltro === 'SOBRESALIENTES'
                ? 'bg-amber-950/40 border-amber-500/40 ring-1 ring-amber-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Sobresalientes</p>
              <span className="text-xl font-bold text-amber-300">{stats.sobresalientes}</span>
            </div>
          </button>
        </div>

        {/* Filtros y Búsqueda */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-6">
          <div className="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto flex-1">
            <div className="relative w-full sm:w-80">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Search className="h-4 w-4" />
              </div>
              <input
                type="text"
                placeholder={
                  isAlumno
                    ? 'Buscar asignatura o comentarios...'
                    : 'Buscar alumno, asignatura, clase o docente...'
                }
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 transition"
              />
            </div>

            {/* Filtro por Asignatura */}
            <div className="relative w-full sm:w-56">
              <select
                value={selectedAsignatura}
                onChange={(e) => setSelectedAsignatura(e.target.value)}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 transition cursor-pointer"
              >
                <option value="ALL">Todas las Asignaturas</option>
                {uniqueAsignaturas.map((asig) => (
                  <option key={asig} value={asig}>
                    {asig}
                  </option>
                ))}
              </select>
            </div>

            {/* Filtro por Rango */}
            <div className="relative w-full sm:w-48">
              <select
                value={calificacionFiltro}
                onChange={(e) =>
                  setCalificacionFiltro(e.target.value as typeof calificacionFiltro)
                }
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 transition cursor-pointer"
              >
                <option value="ALL">Todas las Calificaciones</option>
                <option value="APROBADOS">Aprobados (&gt;= 5.0)</option>
                <option value="NOTABLES">Notables (7.0 - 8.9)</option>
                <option value="SOBRESALIENTES">Sobresalientes (&gt;= 9.0)</option>
                <option value="SUSPENSOS">Suspensos (&lt; 5.0)</option>
              </select>
            </div>

            {hasActiveFilters && (
              <button
                onClick={() => {
                  setSearchTerm('');
                  setSelectedAsignatura('ALL');
                  setCalificacionFiltro('ALL');
                }}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition cursor-pointer"
              >
                <X className="w-3.5 h-3.5" />
                <span>Limpiar</span>
              </button>
            )}
          </div>

          <div className="text-xs text-slate-400 self-end sm:self-center">
            Mostrando <strong className="text-white">{filteredNotas.length}</strong> de{' '}
            <strong className="text-white">{notas.length}</strong> calificaciones
          </div>
        </div>

        {/* Listado de Calificaciones */}
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-cyan-500" />
            <p className="text-sm font-medium">Cargando expediente de calificaciones...</p>
          </div>
        ) : filteredNotas.length === 0 ? (
          <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
            <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center mx-auto mb-3">
              <Award className="w-6 h-6" />
            </div>
            <h3 className="text-base font-semibold text-white mb-1">
              No se encontraron calificaciones
            </h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
              {hasActiveFilters
                ? 'No hay notas que coincidan con los criterios de filtrado seleccionados.'
                : isAlumno
                ? 'Aún no se han emitido notas para tus asignaturas matriculadas.'
                : 'Aún no se ha registrado ninguna evaluación en el sistema.'}
            </p>
            {isProfesor && (
              <button
                onClick={handleOpenCreateModal}
                className="inline-flex items-center gap-2 px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Emitir la primera nota</span>
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredNotas.map((n) => {
              const badge = getBadgeStyle(n.valor);
              const formattedDate = n.fechaEvaluacion
                ? new Date(n.fechaEvaluacion).toLocaleDateString()
                : 'Fecha no registrada';

              const canEditThisNota =
                isProfesor && (user?.id === n.profesorId || user?.nombre === n.profesorNombre);

              return (
                <div
                  key={n.id}
                  className="bg-slate-900 border border-slate-800/90 hover:border-cyan-500/40 rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl hover:shadow-cyan-900/10 group"
                >
                  <div>
                    {/* Fila Superior: Nota Grande, Badge y Acciones */}
                    <div className="flex items-start justify-between gap-3 mb-4">
                      <div className="flex items-center gap-3">
                        {/* Bloque Nota Numérica */}
                        <div
                          className={`w-14 h-14 rounded-2xl border flex flex-col items-center justify-center font-extrabold ${badge.boxBg}`}
                        >
                          <span className="text-xl leading-none">
                            {n.valor !== undefined ? n.valor.toFixed(1) : '-'}
                          </span>
                          <span className="text-[10px] opacity-75 font-semibold">/10</span>
                        </div>

                        <div>
                          <span
                            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg border text-[11px] font-bold ${badge.bg}`}
                          >
                            <Sparkles className="w-3 h-3" />
                            <span>{badge.label}</span>
                          </span>
                          <p className="text-[11px] text-slate-500 mt-1">{formattedDate}</p>
                        </div>
                      </div>

                      {canEditThisNota && (
                        <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                          <button
                            onClick={() => handleOpenEditModal(n)}
                            title="Editar nota"
                            className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-300 hover:bg-cyan-500/10 transition cursor-pointer"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setDeletingNota(n)}
                            title="Eliminar calificación"
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      )}
                    </div>

                    {/* Alumno (si la vista no es de alumno personal) */}
                    {!isAlumno && (
                      <div className="flex items-center gap-2 text-xs text-white font-bold mb-2">
                        <User className="w-4 h-4 text-blue-400 shrink-0" />
                        <span className="truncate">{n.alumnoNombre}</span>
                      </div>
                    )}

                    {/* Asignatura y Clase */}
                    <div className="space-y-1.5 text-xs text-slate-300 mb-3 bg-slate-950/50 p-3 rounded-xl border border-slate-800/80">
                      <div className="flex items-center gap-2">
                        <BookOpen className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
                        <span className="truncate">
                          Asignatura: <strong className="text-white">{n.asignaturaNombre}</strong>
                        </span>
                      </div>

                      <div className="flex items-center gap-2 text-slate-400">
                        <Calendar className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                        <span className="truncate">Clase: {n.claseNombre}</span>
                      </div>

                      <div className="flex items-center gap-2 text-slate-400">
                        <GraduationCap className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span className="truncate">Profesor: {n.profesorNombre}</span>
                      </div>
                    </div>

                    {/* Observaciones */}
                    {n.observaciones && (
                      <div className="p-3 rounded-xl bg-slate-800/40 border border-slate-800 text-xs text-slate-300 italic mb-2 flex items-start gap-2">
                        <FileText className="w-3.5 h-3.5 text-slate-500 shrink-0 mt-0.5" />
                        <p className="line-clamp-3 leading-relaxed">{n.observaciones}</p>
                      </div>
                    )}
                  </div>

                  {/* Pie de la tarjeta */}
                  <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500 mt-2">
                    <span className="font-mono">Ref. #{n.id}</span>
                    <span className="text-slate-400">Matrícula #{n.matriculaId}</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* Modal de Calificación */}
      <NotaModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveNota}
        initialNota={editingNota}
        matriculasList={matriculas}
      />

      {/* Modal Confirmación de Eliminación */}
      {deletingNota && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar Calificación?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de borrar la calificación de{' '}
              <strong className="text-white">{deletingNota.valor.toFixed(1)}</strong> para{' '}
              <strong className="text-white">"{deletingNota.alumnoNombre}"</strong> en la
              asignatura <strong className="text-white">"{deletingNota.asignaturaNombre}"</strong>.
              El expediente quedará sin calificar. ¿Deseas continuar?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingNota(null)}
                disabled={isDeleting}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmDelete}
                disabled={isDeleting}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 transition cursor-pointer disabled:opacity-50"
              >
                {isDeleting && <Loader2 className="w-4 h-4 animate-spin" />}
                <span>Eliminar Calificación</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
