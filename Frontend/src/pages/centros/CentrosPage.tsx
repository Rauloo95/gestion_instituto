import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { centroService } from '../../api/centroService';
import { getApiErrorMessage } from '../../api/client';
import type { Centro, CentroRequest } from '../../types/centro';
import { CentroModal } from './CentroModal';
import {
  Building2,
  Plus,
  Search,
  MapPin,
  Phone,
  Mail,
  Pencil,
  Trash2,
  ArrowLeft,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Building,
  RefreshCw
} from 'lucide-react';

export function CentrosPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [centros, setCentros] = useState<Centro[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Estados del modal de creación / edición
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCentro, setEditingCentro] = useState<Centro | null>(null);

  // Estados del modal de confirmación de eliminación
  const [deletingCentro, setDeletingCentro] = useState<Centro | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchCentros = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await centroService.getCentros();
      setCentros(data);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudieron cargar los centros educativos.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCentros();
  }, []);

  const handleOpenCreateModal = () => {
    setEditingCentro(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (centro: Centro) => {
    setEditingCentro(centro);
    setIsModalOpen(true);
  };

  const handleSaveCentro = async (data: CentroRequest) => {
    if (editingCentro) {
      await centroService.updateCentro(editingCentro.id, data);
      setSuccessMessage(`El centro "${data.nombre}" se ha actualizado correctamente.`);
    } else {
      await centroService.createCentro(data);
      setSuccessMessage(`El centro "${data.nombre}" se ha creado con éxito.`);
    }
    await fetchCentros();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDelete = async () => {
    if (!deletingCentro) return;

    setIsDeleting(true);
    try {
      await centroService.deleteCentro(deletingCentro.id);
      setSuccessMessage(`Centro "${deletingCentro.nombre}" eliminado correctamente.`);
      setDeletingCentro(null);
      await fetchCentros();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar el centro educativo.'));
    } finally {
      setIsDeleting(false);
    }
  };

  // Filtrado reactivo por nombre, tipo o ciudad
  const filteredCentros = centros.filter((c) => {
    const term = searchTerm.toLowerCase();
    return (
      c.nombre.toLowerCase().includes(term) ||
      c.tipo.toLowerCase().includes(term) ||
      (c.ciudad && c.ciudad.toLowerCase().includes(term))
    );
  });

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Barra Superior */}
      <header className="bg-slate-900 border-b border-slate-800 sticky top-0 z-10">
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
              <div className="p-2 bg-blue-600 rounded-xl shadow-md shadow-blue-500/20">
                <Building2 className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">Centros Educativos</h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchCentros}
              disabled={isLoading}
              title="Refrescar listado"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-blue-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Nuevo Centro</span>
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
              onClick={fetchCentros}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Barra de Filtros y Búsqueda */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-6">
          <div className="relative w-full sm:w-96">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
              <Search className="h-4 w-4" />
            </div>
            <input
              type="text"
              placeholder="Buscar por nombre, tipo o ciudad..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
            />
          </div>

          <div className="text-xs text-slate-400 self-end sm:self-center">
            Mostrando <strong className="text-white">{filteredCentros.length}</strong> de{' '}
            <strong className="text-white">{centros.length}</strong> centros
          </div>
        </div>

        {/* Estado de Carga */}
        {isLoading && (
          <div className="py-24 flex flex-col items-center justify-center text-slate-400">
            <Loader2 className="w-8 h-8 text-blue-500 animate-spin mb-3" />
            <p className="text-sm">Cargando centros educativos...</p>
          </div>
        )}

        {/* Estado Vacío */}
        {!isLoading && filteredCentros.length === 0 && (
          <div className="p-12 text-center rounded-3xl bg-slate-900/40 border border-slate-800 max-w-lg mx-auto my-12">
            <div className="w-14 h-14 rounded-2xl bg-slate-800/80 text-slate-400 flex items-center justify-center mx-auto mb-4">
              <Building className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-white mb-1">No se encontraron centros</h3>
            <p className="text-xs text-slate-400 mb-6">
              {searchTerm
                ? 'Ningún centro coincide con los términos de búsqueda.'
                : 'Aún no hay centros educativos registrados en el sistema.'}
            </p>
            {isAdmin && !searchTerm && (
              <button
                onClick={handleOpenCreateModal}
                className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold rounded-xl shadow-lg transition"
              >
                <Plus className="w-4 h-4" />
                <span>Crear primer centro</span>
              </button>
            )}
          </div>
        )}

        {/* Listado en Tarjetas Responsivas */}
        {!isLoading && filteredCentros.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredCentros.map((centro) => (
              <div
                key={centro.id}
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 hover:border-slate-700/80 flex flex-col justify-between transition-all shadow-md group"
              >
                <div>
                  {/* Encabezado de la tarjeta */}
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <span className="px-2.5 py-1 rounded-lg text-[10px] font-bold tracking-wider uppercase bg-blue-500/10 text-blue-400 border border-blue-500/20">
                      {centro.tipo}
                    </span>
                    {centro.activo !== undefined && (
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                          centro.activo
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : 'bg-slate-800 text-slate-500 border-slate-700'
                        }`}
                      >
                        {centro.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    )}
                  </div>

                  <h3 className="text-lg font-bold text-white group-hover:text-blue-400 transition-colors mb-2">
                    {centro.nombre}
                  </h3>

                  {/* Detalles de contacto y ubicación */}
                  <div className="space-y-2 text-xs text-slate-400 mb-6">
                    {(centro.direccion || centro.ciudad) && (
                      <div className="flex items-center gap-2">
                        <MapPin className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                        <span className="truncate">
                          {[centro.direccion, centro.ciudad, centro.codigoPostal].filter(Boolean).join(', ')}
                        </span>
                      </div>
                    )}

                    {centro.telefono && (
                      <div className="flex items-center gap-2">
                        <Phone className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                        <a href={`tel:${centro.telefono}`} className="hover:text-slate-200 transition">
                          {centro.telefono}
                        </a>
                      </div>
                    )}

                    {centro.email && (
                      <div className="flex items-center gap-2">
                        <Mail className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                        <a href={`mailto:${centro.email}`} className="hover:text-slate-200 transition truncate">
                          {centro.email}
                        </a>
                      </div>
                    )}
                  </div>
                </div>

                {/* Acciones de administración */}
                {isAdmin && (
                  <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-2">
                    <button
                      onClick={() => handleOpenEditModal(centro)}
                      className="p-2 rounded-lg text-slate-400 hover:text-blue-400 hover:bg-slate-800 transition cursor-pointer"
                      title="Editar centro"
                    >
                      <Pencil className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => setDeletingCentro(centro)}
                      className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition cursor-pointer"
                      title="Eliminar centro"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal de Crear / Editar */}
      <CentroModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveCentro}
        initialCentro={editingCentro}
      />

      {/* Modal de Confirmación de Borrado */}
      {deletingCentro && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <Trash2 className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Eliminar centro educativo?</h3>
            <p className="text-xs text-slate-400 mb-6">
              Estás a punto de eliminar el centro <strong className="text-white">"{deletingCentro.nombre}"</strong>. Esta acción no se puede deshacer y puede afectar a formaciones vinculadas.
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingCentro(null)}
                disabled={isDeleting}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white hover:bg-slate-800 transition"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmDelete}
                disabled={isDeleting}
                className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 flex items-center gap-1.5 transition disabled:opacity-50 cursor-pointer"
              >
                {isDeleting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                <span>{isDeleting ? 'Eliminando...' : 'Sí, eliminar'}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

