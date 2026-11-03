import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { usuarioService } from '../../api/usuarioService';
import { getApiErrorMessage } from '../../api/client';
import type { UsuarioResponse, UsuarioRequest } from '../../types/usuario';
import type { Rol } from '../../types/auth';
import { UsuarioModal } from './UsuarioModal';
import {
  Users,
  User,
  Shield,
  GraduationCap,
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
  Mail,
  X
} from 'lucide-react';

export function UsuariosPage() {
  const { user: currentUser, hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  // Datos de usuarios
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRol, setSelectedRol] = useState<Rol | 'ALL'>('ALL');

  // Modales
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUsuario, setEditingUsuario] = useState<UsuarioResponse | null>(null);
  const [deletingUsuario, setDeletingUsuario] = useState<UsuarioResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchUsuarios = async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await usuarioService.getUsuarios();
      setUsuarios(data);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo cargar la lista de usuarios.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const handleOpenCreateModal = () => {
    setEditingUsuario(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (u: UsuarioResponse) => {
    setEditingUsuario(u);
    setIsModalOpen(true);
  };

  const handleSaveUsuario = async (data: UsuarioRequest) => {
    if (editingUsuario) {
      await usuarioService.updateUsuario(editingUsuario.id, data);
      setSuccessMessage(`Usuario "${data.nombre} ${data.apellidos}" actualizado correctamente.`);
    } else {
      await usuarioService.createUsuario(data);
      setSuccessMessage(`Usuario "${data.nombre} ${data.apellidos}" creado con éxito.`);
    }
    await fetchUsuarios();
    setTimeout(() => setSuccessMessage(''), 4000);
  };

  const handleConfirmDelete = async () => {
    if (!deletingUsuario) return;

    // Protección para no auto-eliminarse
    if (currentUser?.email === deletingUsuario.email || currentUser?.id === deletingUsuario.id) {
      setErrorMessage('No puedes eliminar tu propia cuenta de usuario en sesión.');
      setDeletingUsuario(null);
      return;
    }

    setIsDeleting(true);
    try {
      await usuarioService.deleteUsuario(deletingUsuario.id);
      setSuccessMessage(`Usuario "${deletingUsuario.nombre} ${deletingUsuario.apellidos}" eliminado correctamente.`);
      setDeletingUsuario(null);
      await fetchUsuarios();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo eliminar el usuario seleccionado.'));
    } finally {
      setIsDeleting(false);
    }
  };

  // Conteo por roles
  const counts = useMemo(() => {
    let alumnos = 0;
    let profesores = 0;
    let admins = 0;

    for (const u of usuarios) {
      if (u.rol === 'ALUMNO') alumnos++;
      else if (u.rol === 'PROFESOR') profesores++;
      else if (u.rol === 'ADMIN') admins++;
    }

    return { total: usuarios.length, alumnos, profesores, admins };
  }, [usuarios]);

  // Filtrado reactivo
  const filteredUsuarios = useMemo(() => {
    return usuarios.filter((u) => {
      const matchRol = selectedRol === 'ALL' || u.rol === selectedRol;
      const term = searchTerm.toLowerCase();
      const matchSearch =
        u.nombre.toLowerCase().includes(term) ||
        u.apellidos.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.rol.toLowerCase().includes(term);

      return matchRol && matchSearch;
    });
  }, [usuarios, selectedRol, searchTerm]);

  const hasActiveFilters = searchTerm !== '' || selectedRol !== 'ALL';

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
              <div className="p-2 bg-gradient-to-tr from-rose-600 to-pink-600 rounded-xl shadow-md shadow-rose-500/20">
                <Users className="w-5 h-5 text-white" />
              </div>
              <h1 className="text-lg font-bold text-white tracking-tight">Gestión de Usuarios</h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchUsuarios}
              disabled={isLoading}
              title="Refrescar usuarios"
              className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            </button>

            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 transition active:scale-[0.98] cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Nuevo Usuario</span>
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
              onClick={fetchUsuarios}
              className="text-xs bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 px-3 py-1 rounded-lg transition"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Tarjetas de Métricas por Rol */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <button
            type="button"
            onClick={() => setSelectedRol('ALL')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedRol === 'ALL'
                ? 'bg-slate-800/90 border-slate-700 shadow-md ring-1 ring-slate-600'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-slate-800 text-slate-300">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Total Usuarios</p>
              <p className="text-xl font-bold text-white">{counts.total}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedRol('ALUMNO')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedRol === 'ALUMNO'
                ? 'bg-blue-950/40 border-blue-500/40 shadow-md ring-1 ring-blue-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-blue-500/10 text-blue-400">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Alumnos</p>
              <p className="text-xl font-bold text-blue-300">{counts.alumnos}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedRol('PROFESOR')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedRol === 'PROFESOR'
                ? 'bg-emerald-950/40 border-emerald-500/40 shadow-md ring-1 ring-emerald-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400">
              <GraduationCap className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Profesores</p>
              <p className="text-xl font-bold text-emerald-300">{counts.profesores}</p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => setSelectedRol('ADMIN')}
            className={`p-4 rounded-2xl border text-left transition cursor-pointer flex items-center gap-3.5 ${
              selectedRol === 'ADMIN'
                ? 'bg-rose-950/40 border-rose-500/40 shadow-md ring-1 ring-rose-500/50'
                : 'bg-slate-900 border-slate-800/80 hover:bg-slate-800/40'
            }`}
          >
            <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-medium text-slate-400">Administradores</p>
              <p className="text-xl font-bold text-rose-300">{counts.admins}</p>
            </div>
          </button>
        </div>

        {/* Barra de Filtros y Búsqueda */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-6">
          <div className="flex flex-col sm:flex-row items-center gap-3 w-full sm:w-auto flex-1">
            <div className="relative w-full sm:w-80">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Search className="h-4 w-4" />
              </div>
              <input
                type="text"
                placeholder="Buscar por nombre, apellidos o email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500 transition"
              />
            </div>

            <div className="relative w-full sm:w-48">
              <select
                value={selectedRol}
                onChange={(e) => setSelectedRol(e.target.value as Rol | 'ALL')}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 transition cursor-pointer"
              >
                <option value="ALL">Todos los Perfiles</option>
                <option value="ALUMNO">Solo Alumnos</option>
                <option value="PROFESOR">Solo Profesores</option>
                <option value="ADMIN">Solo Administradores</option>
              </select>
            </div>

            {hasActiveFilters && (
              <button
                onClick={() => {
                  setSearchTerm('');
                  setSelectedRol('ALL');
                }}
                className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 transition cursor-pointer"
              >
                <X className="w-3.5 h-3.5" />
                <span>Limpiar filtros</span>
              </button>
            )}
          </div>

          <div className="text-xs text-slate-400 self-end sm:self-center">
            Mostrando <strong className="text-white">{filteredUsuarios.length}</strong> de{' '}
            <strong className="text-white">{usuarios.length}</strong> cuentas
          </div>
        </div>

        {/* Grid de Usuarios */}
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-rose-500" />
            <p className="text-sm font-medium">Cargando directorio de usuarios...</p>
          </div>
        ) : filteredUsuarios.length === 0 ? (
          <div className="py-16 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 p-8">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mx-auto mb-3">
              <Users className="w-6 h-6" />
            </div>
            <h3 className="text-base font-semibold text-white mb-1">
              No se encontraron usuarios
            </h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mb-4">
              {hasActiveFilters
                ? 'No hay ningún usuario que cumpla con los filtros de búsqueda aplicados.'
                : 'Aún no se han registrado cuentas en la base de datos.'}
            </p>
            {isAdmin && (
              <button
                onClick={handleOpenCreateModal}
                className="inline-flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl transition cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Dar de alta un usuario</span>
              </button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredUsuarios.map((u) => {
              const isCurrentSessionUser =
                currentUser?.id === u.id || currentUser?.email === u.email;

              return (
                <div
                  key={u.id}
                  className={`bg-slate-900 border rounded-2xl p-5 flex flex-col justify-between transition-all hover:shadow-xl group ${
                    isCurrentSessionUser
                      ? 'border-blue-500/40 shadow-blue-500/5'
                      : 'border-slate-800/90 hover:border-slate-700'
                  }`}
                >
                  <div>
                    {/* Fila Superior: Rol Badge & Acciones */}
                    <div className="flex items-start justify-between gap-2 mb-4">
                      <div className="flex items-center gap-2">
                        {u.rol === 'ADMIN' && (
                          <span className="px-2.5 py-1 rounded-lg bg-rose-500/15 border border-rose-500/30 text-rose-300 text-[11px] font-bold flex items-center gap-1.5">
                            <Shield className="w-3.5 h-3.5" />
                            <span>ADMINISTRADOR</span>
                          </span>
                        )}
                        {u.rol === 'PROFESOR' && (
                          <span className="px-2.5 py-1 rounded-lg bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-[11px] font-bold flex items-center gap-1.5">
                            <GraduationCap className="w-3.5 h-3.5" />
                            <span>PROFESOR</span>
                          </span>
                        )}
                        {u.rol === 'ALUMNO' && (
                          <span className="px-2.5 py-1 rounded-lg bg-blue-500/15 border border-blue-500/30 text-blue-300 text-[11px] font-bold flex items-center gap-1.5">
                            <BookOpen className="w-3.5 h-3.5" />
                            <span>ALUMNO</span>
                          </span>
                        )}

                        {isCurrentSessionUser && (
                          <span className="px-2 py-0.5 rounded-md bg-blue-500 text-white text-[10px] font-bold uppercase tracking-wider">
                            Tú
                          </span>
                        )}
                      </div>

                      {isAdmin && (
                        <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition">
                          <button
                            onClick={() => handleOpenEditModal(u)}
                            title="Modificar usuario"
                            className="p-1.5 rounded-lg text-slate-400 hover:text-rose-300 hover:bg-rose-500/10 transition cursor-pointer"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setDeletingUsuario(u)}
                            disabled={isCurrentSessionUser}
                            title={
                              isCurrentSessionUser
                                ? 'No puedes eliminar tu propia cuenta'
                                : 'Dar de baja usuario'
                            }
                            className={`p-1.5 rounded-lg transition ${
                              isCurrentSessionUser
                                ? 'text-slate-600 cursor-not-allowed'
                                : 'text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 cursor-pointer'
                            }`}
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      )}
                    </div>

                    {/* Información del Usuario */}
                    <div className="flex items-center gap-3.5 mb-3">
                      <div className="w-11 h-11 rounded-2xl bg-slate-800 border border-slate-700/80 flex items-center justify-center text-white font-bold text-sm shrink-0">
                        {u.nombre.charAt(0).toUpperCase()}
                        {u.apellidos ? u.apellidos.charAt(0).toUpperCase() : ''}
                      </div>
                      <div className="min-w-0">
                        <h3 className="text-base font-bold text-white leading-snug truncate group-hover:text-rose-300 transition-colors">
                          {u.nombre} {u.apellidos}
                        </h3>
                        <p className="text-xs text-slate-400 truncate flex items-center gap-1.5 mt-0.5">
                          <Mail className="w-3 h-3 text-slate-500 shrink-0" />
                          <span>{u.email}</span>
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Pie de la tarjeta */}
                  <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500 mt-2">
                    <span className="font-mono">ID #{u.id}</span>
                    <span className="text-slate-400 flex items-center gap-1">
                      <User className="w-3 h-3 text-slate-500" />
                      <span>Cuenta Activa</span>
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* Modal de Creación / Edición */}
      <UsuarioModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveUsuario}
        initialUsuario={editingUsuario}
      />

      {/* Modal Confirmación de Eliminación */}
      {deletingUsuario && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">¿Dar de baja a este usuario?</h3>
            <p className="text-xs text-slate-300 leading-relaxed mb-6">
              Estás a punto de eliminar a{' '}
              <strong className="text-white">
                "{deletingUsuario.nombre} {deletingUsuario.apellidos}" ({deletingUsuario.email})
              </strong>
              . Se revocarán todos sus accesos al sistema. Esta acción no se puede deshacer. ¿Deseas confirmar la eliminación?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setDeletingUsuario(null)}
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
                <span>Eliminar Usuario</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

