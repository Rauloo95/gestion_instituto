import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { claseService } from '../../api/claseService';
import { asignaturaService } from '../../api/asignaturaService';
import { usuarioService } from '../../api/usuarioService';
import { getApiErrorMessage } from '../../api/client';
import type { Clase, ClaseRequest } from '../../types/clase';
import type { Asignatura } from '../../types/asignatura';
import type { UsuarioResponse } from '../../types/usuario';
import { ClaseModal } from './ClaseModal';
import {
  Calendar,
  DoorOpen,
  Users,
  UserCheck,
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
  X,
  Sparkles
} from 'lucide-react';

export function ClasesPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');
  const isProfesor = hasRole('PROFESOR');

  // Datos
  const [clases, setClases] = useState<Clase[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [profesores, setProfesores] = useState<UsuarioResponse[]>([]);

  // Estados de vista y filtros
  const [viewScope, setViewScope] = useState<'all' | 'mine'>(isProfesor && !isAdmin ? 'mine' : 'all');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedAsignatura, setSelectedAsignatura] = useState<number | 'ALL'>('ALL');
  const [selectedAula, setSelectedAula] = useState<string | 'ALL'>('ALL');
  const [selectedProfesor, setSelectedProfesor] = useState<number | 'ALL'>('ALL');

  // Estados de carga y mensajes
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Modales
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingClase, setEditingClase] = useState<Clase | null>(null);
  const [deletingClase, setDeletingClase] = useState<Clase | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Carga de datos
  const fetchClases = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const promises: [Promise<Clase[]>, Promise<Asignatura[]>, Promise<UsuarioResponse[]>] = [
        viewScope === 'mine' ? claseService.getMisClases() : claseService.getClases(),
        asignaturaService.getAsignaturas().catch(() => [] as Asignatura[]),
        usuarioService.getProfesores().catch(() => [] as UsuarioResponse[])
      ];

      const [clasesData, asigsData, profsData] = await Promise.all(promises);
      setClases(clasesData);
      setAsignaturas(asigsData);
      setProfesores(profsData);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudieron sincronizar las clases y aulas.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchClases();
  }, [viewScope]);

  // Manejo de Modal Creación / Edición
  const handleOpenCreateModal = () => {
    setEditingClase(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (clase: Clase) => {
    setEditingClase(clase);
    setIsModalOpen(true);
  };

  const handleSaveClase = async (data: ClaseRequest) => {
    if (editingClase) {
      await claseService.updateClase(editingClase.id, data);
      setSuccessMessage(`La clase "${data.nombre}" se ha actualizado correctamente.`);
    } else {
      await claseService.createClase(data);
      setSuccessMessage(`La clase "${data.nombre}" se ha creado con éxito.`);
    }
    await fetchClases();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  // Manejo de Eliminación
  const handleConfirmDelete = async () => {
    if (!deletingClase) return;
    setIsDeleting(true);
    try {
      await claseService.deleteClase(deletingClase.id);
      setSuccessMessage(`Clase "${deletingClase.nombre}" eliminada correctamente.`);
      setDeletingClase(null);
      await fetchClases();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar la clase.'));
    } finally {
      setIsDeleting(false);
    }
  };

  // Lista de aulas únicas presentes en las clases
  const uniqueAulas = useMemo(() => {
    const set = new Set<string>();
    for (const c of clases) {
      if (c.aula && c.aula.trim()) {
        set.add(c.aula.trim());
      }
    }
    return Array.from(set).sort();
  }, [clases]);

  // Lista de grupos únicos
  const uniqueGrupos = useMemo(() => {
    const set = new Set<string>();
    for (const c of clases) {
      if (c.grupo && c.grupo.trim()) {
        set.add(c.grupo.trim());
      }
    }
    return Array.from(set).sort();
  }, [clases]);

  // Filtrado reactivo de clases
  const filteredClases = useMemo(() => {
    return clases.filter((c) => {
      const term = searchTerm.toLowerCase();
      const matchSearch =
        c.nombre.toLowerCase().includes(term) ||
        (c.grupo && c.grupo.toLowerCase().includes(term)) ||
        (c.aula && c.aula.toLowerCase().includes(term)) ||
        (c.asignaturaNombre && c.asignaturaNombre.toLowerCase().includes(term)) ||
        (c.profesorNombre && c.profesorNombre.toLowerCase().includes(term));

      const matchAsignatura =
        selectedAsignatura === 'ALL' || c.asignaturaId === selectedAsignatura;

      const matchAula = selectedAula === 'ALL' || c.aula === selectedAula;

      const matchProfesor =
        selectedProfesor === 'ALL' || c.profesorId === selectedProfesor;

      return matchSearch && matchAsignatura && matchAula && matchProfesor;
    });
  }, [clases, searchTerm, selectedAsignatura, selectedAula, selectedProfesor]);

  const hasActiveFilters =
    searchTerm !== '' ||
    selectedAsignatura !== 'ALL' ||
    selectedAula !== 'ALL' ||
    selectedProfesor !== 'ALL';

  const handleResetFilters = () => {
    setSearchTerm('');
    setSelectedAsignatura('ALL');
    setSelectedAula('ALL');
    setSelectedProfesor('ALL');
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
              <div className="p-2 bg-gradient-to-tr from-amber-600 to-orange-600 rounded-xl shadow-md shadow-amber-500/20">
                <Calendar className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">Clases & Aulas</h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchClases}
              disabled={isLoading}
              title="Refrescar listado"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="flex items-center gap-2 px-4 py-2 bg-amber-600 hover:bg-amber-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-amber-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Nueva Clase</span>
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
              onClick={fetchClases}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Tarjetas de Métricas Rápidas */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800/80 flex items-center gap-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Total Clases</p>
              <p className="text-xl font-bold text-white">{clases.length}</p>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800/80 flex items-center gap-3.5">
            <div className="p-3 rounded-xl bg-blue-500/10 text-blue-400">
              <DoorOpen className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Aulas en Uso</p>
              <p className="text-xl font-bold text-white">{uniqueAulas.length}</p>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800/80 flex items-center gap-3.5">
            <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Grupos Activos</p>
              <p className="text-xl font-bold text-white">{uniqueGrupos.length}</p>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800/80 flex items-center gap-3.5">
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400">
              <UserCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Profesores</p>
              <p className="text-xl font-bold text-white">{profesores.length}</p>
            </div>
          </div>
        </div>

        {/* Selector de Ámbito para Profesores (Mis Clases vs Todas) */}
        {isProfesor && (
          <div className="flex items-center gap-2 mb-6 p-1.5 rounded-2xl bg-slate-900 border border-slate-800 max-w-fit">
            <button
              onClick={() => setViewScope('mine')}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition cursor-pointer ${
                viewScope === 'mine'
                  ? 'bg-amber-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Mis Clases Asignadas
            </button>
            <button
              onClick={() => setViewScope('all')}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition cursor-pointer ${
                viewScope === 'all'
                  ? 'bg-amber-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Todas las Clases del Centro
            </button>
          </div>
        )}

        {/* Barra de Filtros y Búsqueda */}
        <div className="flex flex-col lg:flex-row items-center justify-between gap-4 mb-6">
          <div className="flex flex-col sm:flex-row flex-wrap items-center gap-3 w-full lg:w-auto flex-1">
            {/* Buscador */}
            <div className="relative w-full sm:w-72">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Search className="h-4 w-4" />
              </div>
              <input
                type="text"
                placeholder="Buscar clase, aula, grupo o profesor..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500 transition"
              />
            </div>

            {/* Filtro por Asignatura */}
            <div className="relative w-full sm:w-56">
              <select
                value={selectedAsignatura}
                onChange={(e) =>
                  setSelectedAsignatura(
                    e.target.value === 'ALL' ? 'ALL' : Number(e.target.value)
                  )
                }
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 transition cursor-pointer"
              >
                <option value="ALL">Todas las Asignaturas</option>
                {asignaturas.map((asig) => (
                  <option key={asig.id} value={asig.id}>
                    {asig.nombre} {asig.codigo ? `[${asig.codigo}]` : ''}
                  </option>
                ))}
              </select>
            </div>

            {/* Filtro por Aula */}
            <div className="relative w-full sm:w-48">
              <select
                value={selectedAula}
                onChange={(e) => setSelectedAula(e.target.value)}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 transition cursor-pointer"
              >
                <option value="ALL">Todas las Aulas</option>
                {uniqueAulas.map((a) => (
                  <option key={a} value={a}>
                    {a}
                  </option>
                ))}
              </select>
            </div>

            {/* Filtro por Profesor (Solo visible si es ADMIN) */}
            {isAdmin && (
              <div className="relative w-full sm:w-52">
                <select
                  value={selectedProfesor}
                  onChange={(e) =>
                    setSelectedProfesor(
                      e.target.value === 'ALL' ? 'ALL' : Number(e.target.value)
                    )
                  }
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 transition cursor-pointer"
                >
                  <option value="ALL">Todos los Profesores</option>
                  {profesores.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombre} {p.apellidos}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Botón Reset Filtros */}
            {hasActiveFilters && (
              <button
                onClick={handleResetFilters}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition cursor-pointer self-start sm:self-auto"
              >
                <X className="w-3.5 h-3.5" />
                <span>Limpiar filtros</span>
              </button>
            )}
          </div>

          <div className="text-xs text-slate-400 self-end lg:self-center shrink-0">
            Mostrando <strong className="text-white">{filteredClases.length}</strong> de{' '}
            <strong className="text-white">{clases.length}</strong> clases
          </div>
        </div>

        {/* Grid de Clases */}
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-amber-500" />
            <p className="text-sm font-medium">Cargando clases y aulas...</p>
          </div>
        ) : filteredClases.length === 0 ? (
          <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
            <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-400 flex items-center justify-center mx-auto mb-3">
              <Calendar className="w-6 h-6" />
            </div>
            <h3 className="text-base font-semibold text-white mb-1">
              No se encontraron clases
            </h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
              {hasActiveFilters
                ? 'No hay clases que coincidan con los criterios de búsqueda aplicados.'
                : viewScope === 'mine'
                ? 'Actualmente no tienes asignada ninguna clase como profesor titular.'
                : 'Aún no se ha registrado ninguna clase en la institución.'}
            </p>
            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="inline-flex items-center gap-2 px-4 py-2 bg-amber-600 hover:bg-amber-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Crear la primera clase</span>
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredClases.map((c) => (
              <div
                key={c.id}
                className="bg-slate-900 border border-slate-800/90 hover:border-amber-500/40 rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl hover:shadow-amber-900/10 group"
              >
                <div>
                  {/* Fila Superior: Badges y Acciones */}
                  <div className="flex items-start justify-between gap-2 mb-3">
                    <div className="flex flex-wrap items-center gap-1.5">
                      {c.grupo ? (
                        <span className="px-2.5 py-0.5 rounded-md bg-amber-500/15 border border-amber-500/30 text-amber-300 font-mono text-[11px] font-bold flex items-center gap-1">
                          <Users className="w-3 h-3" />
                          <span>{c.grupo}</span>
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700 text-slate-400 text-[11px]">
                          Sin grupo
                        </span>
                      )}

                      <span className="px-2 py-0.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-[10px] font-semibold flex items-center gap-1">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                        <span>Activa</span>
                      </span>
                    </div>

                    {isAdmin && (
                      <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                        <button
                          onClick={() => handleOpenEditModal(c)}
                          title="Editar clase"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-amber-300 hover:bg-amber-500/10 transition cursor-pointer"
                        >
                          <Pencil className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => setDeletingClase(c)}
                          title="Eliminar clase"
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Nombre de la Clase */}
                  <h3 className="text-base font-bold text-white mb-2 leading-snug group-hover:text-amber-300 transition-colors">
                    {c.nombre}
                  </h3>

                  {/* Asignatura */}
                  <div className="flex items-center gap-2 text-xs text-slate-300 mb-2">
                    <BookOpen className="w-4 h-4 text-indigo-400 shrink-0" />
                    <span className="font-medium truncate">
                      {c.asignaturaNombre || `Asignatura #${c.asignaturaId}`}
                    </span>
                  </div>

                  {/* Aula o Espacio Físico */}
                  <div className="flex items-center gap-2 text-xs text-slate-400 mb-2">
                    <DoorOpen className="w-4 h-4 text-blue-400 shrink-0" />
                    <span className="truncate">
                      {c.aula ? (
                        <strong className="text-slate-300">{c.aula}</strong>
                      ) : (
                        <span className="italic text-slate-500">Aula pendiente de asignar</span>
                      )}
                    </span>
                  </div>

                  {/* Profesor Titular */}
                  <div className="flex items-center gap-2 text-xs text-slate-400 mb-4">
                    <UserCheck className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span className="truncate">
                      Docente:{' '}
                      <strong className="text-slate-300">
                        {c.profesorNombre || `Profesor #${c.profesorId}`}
                      </strong>
                    </span>
                  </div>
                </div>

                {/* Pie de la tarjeta */}
                <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
                  <span className="font-mono">ID #{c.id}</span>
                  <span className="text-slate-400 flex items-center gap-1">
                    <Sparkles className="w-3 h-3 text-amber-400" />
                    <span>Convocatoria lectiva</span>
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal de Creación / Edición */}
      <ClaseModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveClase}
        initialClase={editingClase}
        asignaturasList={asignaturas}
        profesoresList={profesores}
        defaultAsignaturaId={
          selectedAsignatura !== 'ALL' ? selectedAsignatura : null
        }
      />

      {/* Modal Confirmación de Eliminación */}
      {deletingClase && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar Clase?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de eliminar la clase{' '}
              <strong className="text-white">"{deletingClase.nombre}"</strong>. Si existen alumnos
              matriculados en esta clase, se verán desvinculados de su horario y calificaciones. ¿Deseas continuar?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingClase(null)}
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
                <span>Eliminar Clase</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
