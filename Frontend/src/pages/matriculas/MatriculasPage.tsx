import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { matriculaService } from '../../api/matriculaService';
import { claseService } from '../../api/claseService';
import { getApiErrorMessage } from '../../api/client';
import type { Matricula, MatriculaRequest, EstadoMatricula } from '../../types/matricula';
import type { Clase } from '../../types/clase';
import { MatriculaModal } from './MatriculaModal';
import {
  ClipboardList,
  User,
  Calendar,
  BookOpen,
  Plus,
  Search,
  Pencil,
  Trash2,
  ArrowLeft,
  Loader2,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Clock,
  XCircle,
  X,
  FileCheck
} from 'lucide-react';

export function MatriculasPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  // Datos
  const [matriculas, setMatriculas] = useState<Matricula[]>([]);
  const [clases, setClases] = useState<Clase[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedEstado, setSelectedEstado] = useState<EstadoMatricula | 'ALL'>('ALL');
  const [selectedClase, setSelectedClase] = useState<number | 'ALL'>('ALL');

  // Modales
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingMatricula, setEditingMatricula] = useState<Matricula | null>(null);
  const [deletingMatricula, setDeletingMatricula] = useState<Matricula | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchMatriculas = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const [matsData, clasesData] = await Promise.all([
        matriculaService.getMatriculas(),
        claseService.getClases().catch(() => [] as Clase[])
      ]);
      setMatriculas(matsData);
      setClases(clasesData);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudieron sincronizar las matrículas de alumnos.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMatriculas();
  }, []);

  const handleOpenCreateModal = () => {
    setEditingMatricula(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (m: Matricula) => {
    setEditingMatricula(m);
    setIsModalOpen(true);
  };

  const handleSaveMatricula = async (data: MatriculaRequest) => {
    if (editingMatricula) {
      await matriculaService.updateMatricula(editingMatricula.id, data);
      setSuccessMessage('Matrícula actualizada correctamente.');
    } else {
      await matriculaService.createMatricula(data);
      setSuccessMessage('Alumno matriculado con éxito en la clase seleccionada.');
    }
    await fetchMatriculas();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDelete = async () => {
    if (!deletingMatricula) return;
    setIsDeleting(true);
    try {
      await matriculaService.deleteMatricula(deletingMatricula.id);
      setSuccessMessage(
        `Matrícula de "${deletingMatricula.alumnoNombre}" eliminada correctamente.`
      );
      setDeletingMatricula(null);
      await fetchMatriculas();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar la matrícula.'));
    } finally {
      setIsDeleting(false);
    }
  };

  // Contadores por estado
  const counts = useMemo(() => {
    let activas = 0;
    let finalizadas = 0;
    let canceladas = 0;

    for (const m of matriculas) {
      if (m.estado === 'ACTIVA') activas++;
      else if (m.estado === 'FINALIZADA') finalizadas++;
      else if (m.estado === 'CANCELADA') canceladas++;
    }

    return { total: matriculas.length, activas, finalizadas, canceladas };
  }, [matriculas]);

  // Filtrado reactivo
  const filteredMatriculas = useMemo(() => {
    return matriculas.filter((m) => {
      const matchEstado = selectedEstado === 'ALL' || m.estado === selectedEstado;
      const matchClase = selectedClase === 'ALL' || m.claseId === selectedClase;
      const term = searchTerm.toLowerCase();
      const matchSearch =
        (m.alumnoNombre && m.alumnoNombre.toLowerCase().includes(term)) ||
        (m.claseNombre && m.claseNombre.toLowerCase().includes(term)) ||
        (m.asignaturaNombre && m.asignaturaNombre.toLowerCase().includes(term)) ||
        m.estado.toLowerCase().includes(term);

      return matchEstado && matchClase && matchSearch;
    });
  }, [matriculas, selectedEstado, selectedClase, searchTerm]);

  const hasActiveFilters =
    searchTerm !== '' || selectedEstado !== 'ALL' || selectedClase !== 'ALL';

  const handleResetFilters = () => {
    setSearchTerm('');
    setSelectedEstado('ALL');
    setSelectedClase('ALL');
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
              <div className="p-2 bg-gradient-to-tr from-emerald-600 to-teal-600 rounded-xl shadow-md shadow-emerald-500/20">
                <ClipboardList className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">Gestión de Matrículas</h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchMatriculas}
              disabled={isLoading}
              title="Refrescar listado"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-emerald-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Nueva Matrícula</span>
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
              onClick={fetchMatriculas}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Tarjetas de Métricas Rápidas por Estado */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <button
            type="button"
            onClick={() => setSelectedEstado('ALL')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedEstado === 'ALL'
                ? 'bg-slate-800/90 border-slate-700 shadow-md ring-1 ring-slate-600'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-slate-800 text-slate-300">
              <ClipboardList className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Total Matrículas</p>
              <p className="text-xl font-bold text-white">{counts.total}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedEstado('ACTIVA')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedEstado === 'ACTIVA'
                ? 'bg-emerald-950/40 border-emerald-500/40 shadow-md ring-1 ring-emerald-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Activas</p>
              <p className="text-xl font-bold text-emerald-300">{counts.activas}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedEstado('FINALIZADA')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedEstado === 'FINALIZADA'
                ? 'bg-blue-950/40 border-blue-500/40 shadow-md ring-1 ring-blue-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-blue-500/10 text-blue-400">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Finalizadas</p>
              <p className="text-xl font-bold text-blue-300">{counts.finalizadas}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedEstado('CANCELADA')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedEstado === 'CANCELADA'
                ? 'bg-rose-950/40 border-rose-500/40 shadow-md ring-1 ring-rose-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400">
              <XCircle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Canceladas</p>
              <p className="text-xl font-bold text-rose-300">{counts.canceladas}</p>
            </div>
          </button>
        </div>

        {/* Filtros y Buscador */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-6">
          <div className="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto flex-1">
            {/* Buscador */}
            <div className="relative w-full sm:w-80">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Search className="h-4 w-4" />
              </div>
              <input
                type="text"
                placeholder="Buscar por alumno, clase o asignatura..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition"
              />
            </div>

            {/* Filtro por Clase */}
            <div className="relative w-full sm:w-60">
              <select
                value={selectedClase}
                onChange={(e) =>
                  setSelectedClase(
                    e.target.value === 'ALL' ? 'ALL' : Number(e.target.value)
                  )
                }
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 transition cursor-pointer"
              >
                <option value="ALL">Todas las Clases</option>
                {clases.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre} {c.grupo ? `[${c.grupo}]` : ''}
                  </option>
                ))}
              </select>
            </div>

            {/* Filtro por Estado */}
            <div className="relative w-full sm:w-44">
              <select
                value={selectedEstado}
                onChange={(e) =>
                  setSelectedEstado(e.target.value as EstadoMatricula | 'ALL')
                }
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 transition cursor-pointer"
              >
                <option value="ALL">Todos los Estados</option>
                <option value="ACTIVA">Solo Activas</option>
                <option value="FINALIZADA">Solo Finalizadas</option>
                <option value="CANCELADA">Solo Canceladas</option>
              </select>
            </div>

            {/* Reset Filtros */}
            {hasActiveFilters && (
              <button
                onClick={handleResetFilters}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition cursor-pointer"
              >
                <X className="w-3.5 h-3.5" />
                <span>Limpiar</span>
              </button>
            )}
          </div>

          <div className="text-xs text-slate-400 self-end sm:self-center">
            Mostrando <strong className="text-white">{filteredMatriculas.length}</strong> de{' '}
            <strong className="text-white">{matriculas.length}</strong> matrículas
          </div>
        </div>

        {/* Grid de Matrículas */}
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-emerald-500" />
            <p className="text-sm font-medium">Cargando registros de matrícula...</p>
          </div>
        ) : filteredMatriculas.length === 0 ? (
          <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center mx-auto mb-3">
              <ClipboardList className="w-6 h-6" />
            </div>
            <h3 className="text-base font-semibold text-white mb-1">
              No se encontraron matrículas
            </h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
              {hasActiveFilters
                ? 'No hay matrículas que coincidan con los criterios de filtrado seleccionados.'
                : 'Aún no se ha realizado ninguna inscripción de alumnos en las clases.'}
            </p>
            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Matricular el primer alumno</span>
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredMatriculas.map((m) => (
              <div
                key={m.id}
                className="bg-slate-900 border border-slate-800/90 hover:border-emerald-500/40 rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl hover:shadow-emerald-900/10 group"
              >
                <div>
                  {/* Fila Superior: Estado Badge y Acciones */}
                  <div className="flex items-start justify-between gap-2 mb-3">
                    <div className="flex items-center gap-2">
                      {m.estado === 'ACTIVA' && (
                        <span className="px-2.5 py-1 rounded-lg bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-[11px] font-bold flex items-center gap-1.5">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          <span>ACTIVA</span>
                        </span>
                      )}
                      {m.estado === 'FINALIZADA' && (
                        <span className="px-2.5 py-1 rounded-lg bg-blue-500/15 border border-blue-500/30 text-blue-300 text-[11px] font-bold flex items-center gap-1.5">
                          <Clock className="w-3.5 h-3.5" />
                          <span>FINALIZADA</span>
                        </span>
                      )}
                      {m.estado === 'CANCELADA' && (
                        <span className="px-2.5 py-1 rounded-lg bg-rose-500/15 border border-rose-500/30 text-rose-300 text-[11px] font-bold flex items-center gap-1.5">
                          <XCircle className="w-3.5 h-3.5" />
                          <span>CANCELADA</span>
                        </span>
                      )}

                      <span className="text-[11px] text-slate-500">
                        {m.fechaMatricula}
                      </span>
                    </div>

                    {isAdmin && (
                      <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                        <button
                          onClick={() => handleOpenEditModal(m)}
                          title="Modificar matrícula"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-300 hover:bg-emerald-500/10 transition cursor-pointer"
                        >
                          <Pencil className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => setDeletingMatricula(m)}
                          title="Anular / Eliminar matrícula"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Nombre del Alumno */}
                  <div className="flex items-center gap-2.5 mb-3">
                    <div className="p-2 rounded-xl bg-blue-500/10 text-blue-400">
                      <User className="w-4 h-4" />
                    </div>
                    <div className="min-w-0">
                      <h3 className="text-base font-bold text-white leading-snug truncate group-hover:text-emerald-300 transition-colors">
                        {m.alumnoNombre || `Alumno #${m.alumnoId}`}
                      </h3>
                      <p className="text-[11px] text-slate-400">Alumno Matriculado</p>
                    </div>
                  </div>

                  {/* Clase y Asignatura */}
                  <div className="space-y-1.5 text-xs text-slate-300 mb-3 bg-slate-950/50 p-3 rounded-xl border border-slate-800/80">
                    <div className="flex items-center gap-2">
                      <Calendar className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                      <span className="truncate">
                        Clase: <strong className="text-white">{m.claseNombre || `#${m.claseId}`}</strong>
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <BookOpen className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
                      <span className="truncate">
                        Asignatura: <span className="text-slate-300">{m.asignaturaNombre || `#${m.asignaturaId}`}</span>
                      </span>
                    </div>
                  </div>
                </div>

                {/* Pie de la tarjeta */}
                <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
                  <span className="font-mono">Expediente #{m.id}</span>
                  <span className="text-slate-400 flex items-center gap-1">
                    <FileCheck className="w-3 h-3 text-emerald-400" />
                    <span>Inscripción Registrada</span>
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal de Creación / Edición */}
      <MatriculaModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveMatricula}
        initialMatricula={editingMatricula}
        clasesList={clases}
        defaultClaseId={selectedClase !== 'ALL' ? selectedClase : null}
      />

      {/* Modal Confirmación de Eliminación */}
      {deletingMatricula && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar Matrícula?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de desmatricular a{' '}
              <strong className="text-white">"{deletingMatricula.alumnoNombre}"</strong> de la clase{' '}
              <strong className="text-white">"{deletingMatricula.claseNombre}"</strong>. El alumno ya no figurará en las listas de calificaciones de esta clase. ¿Deseas continuar?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingMatricula(null)}
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
                <span>Eliminar Matrícula</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

