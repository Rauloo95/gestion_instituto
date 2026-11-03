import { useState, useEffect, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { formacionService } from '../../api/formacionService';
import { asignaturaService } from '../../api/asignaturaService';
import { centroService } from '../../api/centroService';
import { getApiErrorMessage } from '../../api/client';
import type { Formacion, FormacionRequest } from '../../types/formacion';
import type { Asignatura, AsignaturaRequest } from '../../types/asignatura';
import type { Centro } from '../../types/centro';
import { FormacionModal } from './FormacionModal';
import { AsignaturaModal } from '../asignaturas/AsignaturaModal';
import {
  GraduationCap,
  BookOpen,
  Building2,
  Plus,
  Search,
  Pencil,
  Trash2,
  ArrowLeft,
  Loader2,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Calendar,
  Layers,
  ChevronRight,
  X
} from 'lucide-react';

export function FormacionesAsignaturasPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [searchParams, setSearchParams] = useSearchParams();
  const currentTab = searchParams.get('tab') === 'asignaturas' ? 'asignaturas' : 'formaciones';
  const initialFormacionFilter = searchParams.get('formacionId')
    ? Number(searchParams.get('formacionId'))
    : null;

  // Estados de datos
  const [formaciones, setFormaciones] = useState<Formacion[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [centros, setCentros] = useState<Centro[]>([]);

  // Estados de interfaz y filtrado
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [searchFormacion, setSearchFormacion] = useState('');
  const [selectedCentroFilter, setSelectedCentroFilter] = useState<number | 'ALL'>('ALL');

  const [searchAsignatura, setSearchAsignatura] = useState('');
  const [selectedFormacionFilter, setSelectedFormacionFilter] = useState<number | 'ALL'>(
    initialFormacionFilter || 'ALL'
  );

  // Modales de Formación
  const [isFormacionModalOpen, setIsFormacionModalOpen] = useState(false);
  const [editingFormacion, setEditingFormacion] = useState<Formacion | null>(null);
  const [deletingFormacion, setDeletingFormacion] = useState<Formacion | null>(null);
  const [isDeletingFormacion, setIsDeletingFormacion] = useState(false);

  // Modales de Asignatura
  const [isAsignaturaModalOpen, setIsAsignaturaModalOpen] = useState(false);
  const [editingAsignatura, setEditingAsignatura] = useState<Asignatura | null>(null);
  const [deletingAsignatura, setDeletingAsignatura] = useState<Asignatura | null>(null);
  const [isDeletingAsignatura, setIsDeletingAsignatura] = useState(false);

  // Carga inicial y refresco de todos los datos necesarios
  const fetchAllData = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const [formacionesData, asignaturasData, centrosData] = await Promise.all([
        formacionService.getFormaciones(),
        asignaturaService.getAsignaturas(),
        centroService.getCentros().catch(() => [] as Centro[]),
      ]);
      setFormaciones(formacionesData);
      setAsignaturas(asignaturasData);
      setCentros(centrosData);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Error al sincronizar datos del catálogo académico.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAllData();
  }, []);

  // Cambio de pestañas
  const handleTabChange = (tab: 'formaciones' | 'asignaturas') => {
    const params = new URLSearchParams(searchParams);
    params.set('tab', tab);
    setSearchParams(params);
  };

  // Atajo para ver asignaturas de una formación
  const handleViewAsignaturasOfFormacion = (formacionId: number) => {
    setSelectedFormacionFilter(formacionId);
    setSearchAsignatura('');
    const params = new URLSearchParams();
    params.set('tab', 'asignaturas');
    params.set('formacionId', String(formacionId));
    setSearchParams(params);
  };

  // ---------------- Handlers de Formación ----------------
  const handleOpenCreateFormacion = () => {
    setEditingFormacion(null);
    setIsFormacionModalOpen(true);
  };

  const handleOpenEditFormacion = (f: Formacion) => {
    setEditingFormacion(f);
    setIsFormacionModalOpen(true);
  };

  const handleSaveFormacion = async (data: FormacionRequest) => {
    if (editingFormacion) {
      await formacionService.updateFormacion(editingFormacion.id, data);
      setSuccessMessage(`Formación "${data.nombre}" actualizada con éxito.`);
    } else {
      await formacionService.createFormacion(data);
      setSuccessMessage(`Formación "${data.nombre}" creada con éxito.`);
    }
    await fetchAllData();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDeleteFormacion = async () => {
    if (!deletingFormacion) return;
    setIsDeletingFormacion(true);
    try {
      await formacionService.deleteFormacion(deletingFormacion.id);
      setSuccessMessage(`Formación "${deletingFormacion.nombre}" eliminada correctamente.`);
      setDeletingFormacion(null);
      await fetchAllData();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar la formación académica.'));
    } finally {
      setIsDeletingFormacion(false);
    }
  };

  // ---------------- Handlers de Asignatura ----------------
  const handleOpenCreateAsignatura = () => {
    setEditingAsignatura(null);
    setIsAsignaturaModalOpen(true);
  };

  const handleOpenEditAsignatura = (a: Asignatura) => {
    setEditingAsignatura(a);
    setIsAsignaturaModalOpen(true);
  };

  const handleSaveAsignatura = async (data: AsignaturaRequest) => {
    if (editingAsignatura) {
      await asignaturaService.updateAsignatura(editingAsignatura.id, data);
      setSuccessMessage(`Asignatura "${data.nombre}" actualizada con éxito.`);
    } else {
      await asignaturaService.createAsignatura(data);
      setSuccessMessage(`Asignatura "${data.nombre}" registrada con éxito.`);
    }
    await fetchAllData();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDeleteAsignatura = async () => {
    if (!deletingAsignatura) return;
    setIsDeletingAsignatura(true);
    try {
      await asignaturaService.deleteAsignatura(deletingAsignatura.id);
      setSuccessMessage(`Asignatura "${deletingAsignatura.nombre}" eliminada correctamente.`);
      setDeletingAsignatura(null);
      await fetchAllData();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar la asignatura.'));
    } finally {
      setIsDeletingAsignatura(false);
    }
  };

  // ---------------- Filtrados reactivos ----------------
  const filteredFormaciones = useMemo(() => {
    return formaciones.filter((f) => {
      const matchCentro = selectedCentroFilter === 'ALL' || f.centroId === selectedCentroFilter;
      const term = searchFormacion.toLowerCase();
      const matchSearch =
        f.nombre.toLowerCase().includes(term) ||
        (f.codigo && f.codigo.toLowerCase().includes(term)) ||
        (f.nivel && f.nivel.toLowerCase().includes(term)) ||
        (f.centroNombre && f.centroNombre.toLowerCase().includes(term));
      return matchCentro && matchSearch;
    });
  }, [formaciones, selectedCentroFilter, searchFormacion]);

  const filteredAsignaturas = useMemo(() => {
    return asignaturas.filter((a) => {
      const matchFormacion =
        selectedFormacionFilter === 'ALL' || a.formacionId === selectedFormacionFilter;
      const term = searchAsignatura.toLowerCase();
      const matchSearch =
        a.nombre.toLowerCase().includes(term) ||
        (a.codigo && a.codigo.toLowerCase().includes(term)) ||
        (a.curso && a.curso.toLowerCase().includes(term)) ||
        (a.formacionNombre && a.formacionNombre.toLowerCase().includes(term));
      return matchFormacion && matchSearch;
    });
  }, [asignaturas, selectedFormacionFilter, searchAsignatura]);

  // Contar asignaturas por formación
  const asignaturasCountByFormacion = useMemo(() => {
    const map = new Map<number, number>();
    for (const asig of asignaturas) {
      map.set(asig.formacionId, (map.get(asig.formacionId) || 0) + 1);
    }
    return map;
  }, [asignaturas]);

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
              <div className="p-2 bg-gradient-to-tr from-purple-600 to-indigo-600 rounded-xl shadow-md shadow-indigo-500/20">
                <GraduationCap className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">Formaciones & Asignaturas</h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchAllData}
              disabled={isLoading}
              title="Refrescar catálogo"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isAdmin && (
              <button
                onClick={
                  currentTab === 'formaciones' ? handleOpenCreateFormacion : handleOpenCreateAsignatura
                }
                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-purple-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>{currentTab === 'formaciones' ? 'Nueva Formación' : 'Nueva Asignatura'}</span>
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
              onClick={fetchAllData}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Selector de Pestañas (Tabs) */}
        <div className="flex border-b border-slate-800 mb-6 gap-2">
          <button
            onClick={() => handleTabChange('formaciones')}
            className={`flex items-center gap-2.5 px-5 py-3 border-b-2 text-sm font-semibold transition cursor-pointer ${
              currentTab === 'formaciones'
                ? 'border-purple-500 text-purple-400 bg-purple-500/5'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
            }`}
          >
            <GraduationCap className="w-4 h-4" />
            <span>Formaciones Académicas</span>
            <span
              className={`px-2 py-0.5 rounded-full text-xs ${
                currentTab === 'formaciones'
                  ? 'bg-purple-500/20 text-purple-300'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              {formaciones.length}
            </span>
          </button>

          <button
            onClick={() => handleTabChange('asignaturas')}
            className={`flex items-center gap-2.5 px-5 py-3 border-b-2 text-sm font-semibold transition cursor-pointer ${
              currentTab === 'asignaturas'
                ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
                : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
            }`}
          >
            <BookOpen className="w-4 h-4" />
            <span>Asignaturas & Módulos</span>
            <span
              className={`px-2 py-0.5 rounded-full text-xs ${
                currentTab === 'asignaturas'
                  ? 'bg-indigo-500/20 text-indigo-300'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              {asignaturas.length}
            </span>
          </button>
        </div>

        {/* ---------------- PESTAÑA 1: FORMACIONES ---------------- */}
        {currentTab === 'formaciones' && (
          <div className="space-y-6">
            {/* Filtros de Formaciones */}
            <div className="flex flex-col md:flex-row items-center justify-between gap-4">
              <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto flex-1">
                {/* Búsqueda */}
                <div className="relative w-full sm:w-80">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    <Search className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    placeholder="Buscar formación o titulación..."
                    value={searchFormacion}
                    onChange={(e) => setSearchFormacion(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 transition"
                  />
                </div>

                {/* Filtro por Centro */}
                <div className="relative w-full sm:w-64">
                  <select
                    value={selectedCentroFilter}
                    onChange={(e) =>
                      setSelectedCentroFilter(
                        e.target.value === 'ALL' ? 'ALL' : Number(e.target.value)
                      )
                    }
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 transition cursor-pointer"
                  >
                    <option value="ALL">Todos los Centros</option>
                    {centros.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="text-xs text-slate-400 self-end md:self-center">
                Mostrando <strong className="text-white">{filteredFormaciones.length}</strong> de{' '}
                <strong className="text-white">{formaciones.length}</strong> formaciones
              </div>
            </div>

            {/* Grid de Formaciones */}
            {isLoading ? (
              <div className="py-20 flex flex-col items-center justify-center gap-3 text-slate-400">
                <Loader2 className="w-8 h-8 animate-spin text-purple-500" />
                <p className="text-sm font-medium">Cargando formaciones académicas...</p>
              </div>
            ) : filteredFormaciones.length === 0 ? (
              <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
                <div className="w-12 h-12 rounded-2xl bg-purple-500/10 text-purple-400 flex items-center justify-center mx-auto mb-3">
                  <GraduationCap className="w-6 h-6" />
                </div>
                <h3 className="text-base font-semibold text-white mb-1">
                  No se encontraron formaciones
                </h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
                  {searchFormacion || selectedCentroFilter !== 'ALL'
                    ? 'No hay formaciones que coincidan con los filtros aplicados.'
                    : 'Aún no se ha registrado ninguna formación académica en el sistema.'}
                </p>
                {isAdmin && (
                  <button
                    onClick={handleOpenCreateFormacion}
                    className="inline-flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Crear la primera formación</span>
                  </button>
                )}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {filteredFormaciones.map((f) => {
                  const asigCount = asignaturasCountByFormacion.get(f.id) || 0;

                  return (
                    <div
                      key={f.id}
                      className="bg-slate-900 border border-slate-800/90 hover:border-purple-500/40 rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl hover:shadow-purple-900/10 group"
                    >
                      <div>
                        {/* Cabecera de la tarjeta: Badges y Acciones */}
                        <div className="flex items-start justify-between gap-2 mb-3">
                          <div className="flex flex-wrap items-center gap-1.5">
                            {f.codigo && (
                              <span className="px-2.5 py-0.5 rounded-md bg-purple-500/15 border border-purple-500/30 text-purple-300 font-mono text-[11px] font-bold">
                                {f.codigo}
                              </span>
                            )}
                            {f.nivel && (
                              <span className="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700 text-slate-300 text-[11px] font-medium">
                                {f.nivel}
                              </span>
                            )}
                          </div>

                          {isAdmin && (
                            <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                              <button
                                onClick={() => handleOpenEditFormacion(f)}
                                title="Editar formación"
                                className="p-1.5 rounded-lg text-slate-400 hover:text-purple-300 hover:bg-purple-500/10 transition cursor-pointer"
                              >
                                <Pencil className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => setDeletingFormacion(f)}
                                title="Eliminar formación"
                                className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          )}
                        </div>

                        {/* Nombre */}
                        <h3 className="text-base font-bold text-white mb-2 leading-snug group-hover:text-purple-300 transition-colors">
                          {f.nombre}
                        </h3>

                        {/* Centro Asociado */}
                        <div className="flex items-center gap-2 text-xs text-slate-400 mb-3">
                          <Building2 className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                          <span className="truncate">{f.centroNombre || `Centro ID #${f.centroId}`}</span>
                        </div>

                        {/* Descripción */}
                        {f.descripcion && (
                          <p className="text-xs text-slate-400 line-clamp-3 mb-4 leading-relaxed">
                            {f.descripcion}
                          </p>
                        )}
                      </div>

                      {/* Botón inferior: Ver Asignaturas */}
                      <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between mt-2">
                        <span className="text-[11px] text-slate-500 flex items-center gap-1.5">
                          <Layers className="w-3.5 h-3.5 text-indigo-400" />
                          <span>
                            <strong>{asigCount}</strong> {asigCount === 1 ? 'asignatura' : 'asignaturas'}
                          </span>
                        </span>

                        <button
                          onClick={() => handleViewAsignaturasOfFormacion(f.id)}
                          className="inline-flex items-center gap-1 text-xs font-semibold text-purple-400 hover:text-purple-300 transition cursor-pointer"
                        >
                          <span>Ver asignaturas</span>
                          <ChevronRight className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ---------------- PESTAÑA 2: ASIGNATURAS ---------------- */}
        {currentTab === 'asignaturas' && (
          <div className="space-y-6">
            {/* Filtros de Asignaturas */}
            <div className="flex flex-col md:flex-row items-center justify-between gap-4">
              <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto flex-1">
                {/* Búsqueda */}
                <div className="relative w-full sm:w-80">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    <Search className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    placeholder="Buscar por asignatura, código o curso..."
                    value={searchAsignatura}
                    onChange={(e) => setSearchAsignatura(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
                  />
                </div>

                {/* Filtro por Formación */}
                <div className="relative w-full sm:w-72">
                  <select
                    value={selectedFormacionFilter}
                    onChange={(e) =>
                      setSelectedFormacionFilter(
                        e.target.value === 'ALL' ? 'ALL' : Number(e.target.value)
                      )
                    }
                    className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition cursor-pointer"
                  >
                    <option value="ALL">Todas las Formaciones</option>
                    {formaciones.map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.nombre} {f.codigo ? `[${f.codigo}]` : ''}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Botón reset filtro si hay uno activo */}
                {selectedFormacionFilter !== 'ALL' && (
                  <button
                    onClick={() => {
                      setSelectedFormacionFilter('ALL');
                      const params = new URLSearchParams(searchParams);
                      params.delete('formacionId');
                      setSearchParams(params);
                    }}
                    className="flex items-center gap-1 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition cursor-pointer"
                  >
                    <X className="w-3.5 h-3.5" />
                    <span>Quitar filtro</span>
                  </button>
                )}
              </div>

              <div className="text-xs text-slate-400 self-end md:self-center">
                Mostrando <strong className="text-white">{filteredAsignaturas.length}</strong> de{' '}
                <strong className="text-white">{asignaturas.length}</strong> asignaturas
              </div>
            </div>

            {/* Grid de Asignaturas */}
            {isLoading ? (
              <div className="py-20 flex flex-col items-center justify-center gap-3 text-slate-400">
                <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                <p className="text-sm font-medium">Cargando asignaturas...</p>
              </div>
            ) : filteredAsignaturas.length === 0 ? (
              <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
                <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mx-auto mb-3">
                  <BookOpen className="w-6 h-6" />
                </div>
                <h3 className="text-base font-semibold text-white mb-1">
                  No se encontraron asignaturas
                </h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
                  {searchAsignatura || selectedFormacionFilter !== 'ALL'
                    ? 'No hay asignaturas que coincidan con la búsqueda o la formación seleccionada.'
                    : 'Aún no hay asignaturas registradas en el catálogo.'}
                </p>
                {isAdmin && (
                  <button
                    onClick={handleOpenCreateAsignatura}
                    className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Crear la primera asignatura</span>
                  </button>
                )}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                {filteredAsignaturas.map((a) => (
                  <div
                    key={a.id}
                    className="bg-slate-900 border border-slate-800/90 hover:border-indigo-500/40 rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl hover:shadow-indigo-900/10 group"
                  >
                    <div>
                      {/* Badges y Acciones */}
                      <div className="flex items-start justify-between gap-2 mb-3">
                        <div className="flex flex-wrap items-center gap-1.5">
                          {a.codigo && (
                            <span className="px-2.5 py-0.5 rounded-md bg-indigo-500/15 border border-indigo-500/30 text-indigo-300 font-mono text-[11px] font-bold">
                              {a.codigo}
                            </span>
                          )}
                          {a.curso && (
                            <span className="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700 text-slate-300 text-[11px] font-medium flex items-center gap-1">
                              <Calendar className="w-3 h-3 text-slate-400" />
                              <span>{a.curso}</span>
                            </span>
                          )}
                        </div>

                        {isAdmin && (
                          <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                            <button
                              onClick={() => handleOpenEditAsignatura(a)}
                              title="Editar asignatura"
                              className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-300 hover:bg-indigo-500/10 transition cursor-pointer"
                            >
                              <Pencil className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => setDeletingAsignatura(a)}
                              title="Eliminar asignatura"
                              className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        )}
                      </div>

                      {/* Nombre */}
                      <h3 className="text-base font-bold text-white mb-2 leading-snug group-hover:text-indigo-300 transition-colors">
                        {a.nombre}
                      </h3>

                      {/* Formación a la que pertenece */}
                      <div className="flex items-center gap-2 text-xs text-slate-400 mb-3">
                        <GraduationCap className="w-3.5 h-3.5 text-purple-400 shrink-0" />
                        <span className="truncate">
                          {a.formacionNombre || `Formación ID #${a.formacionId}`}
                        </span>
                      </div>

                      {/* Descripción */}
                      {a.descripcion && (
                        <p className="text-xs text-slate-400 line-clamp-3 mb-2 leading-relaxed">
                          {a.descripcion}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* Modal de Formación (Creación / Edición) */}
      <FormacionModal
        isOpen={isFormacionModalOpen}
        onClose={() => setIsFormacionModalOpen(false)}
        onSave={handleSaveFormacion}
        initialFormacion={editingFormacion}
        centrosList={centros}
      />

      {/* Modal de Asignatura (Creación / Edición) */}
      <AsignaturaModal
        isOpen={isAsignaturaModalOpen}
        onClose={() => setIsAsignaturaModalOpen(false)}
        onSave={handleSaveAsignatura}
        initialAsignatura={editingAsignatura}
        formacionesList={formaciones}
        defaultFormacionId={
          selectedFormacionFilter !== 'ALL' ? selectedFormacionFilter : null
        }
      />

      {/* Modal Confirmación de Eliminación de Formación */}
      {deletingFormacion && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar Formación?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de eliminar permanentemente la formación{' '}
              <strong className="text-white">"{deletingFormacion.nombre}"</strong>. Esta acción
              puede afectar a las asignaturas y clases vinculadas. ¿Deseas continuar?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingFormacion(null)}
                disabled={isDeletingFormacion}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmDeleteFormacion}
                disabled={isDeletingFormacion}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 transition cursor-pointer disabled:opacity-50"
              >
                {isDeletingFormacion && <Loader2 className="w-4 h-4 animate-spin" />}
                <span>Eliminar Formación</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Confirmación de Eliminación de Asignatura */}
      {deletingAsignatura && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar Asignatura?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de eliminar la asignatura{' '}
              <strong className="text-white">"{deletingAsignatura.nombre}"</strong>. Esta acción
              no se puede deshacer. ¿Deseas continuar?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingAsignatura(null)}
                disabled={isDeletingAsignatura}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmDeleteAsignatura}
                disabled={isDeletingAsignatura}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 transition cursor-pointer disabled:opacity-50"
              >
                {isDeletingAsignatura && <Loader2 className="w-4 h-4 animate-spin" />}
                <span>Eliminar Asignatura</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
