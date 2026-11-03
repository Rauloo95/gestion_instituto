import { useState, useEffect, type FormEvent } from 'react';
import type { UsuarioResponse, UsuarioRequest } from '../../types/usuario';
import type { Rol } from '../../types/auth';
import { getApiErrorMessage } from '../../api/client';
import {
  X,
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  Shield,
  GraduationCap,
  BookOpen,
  Loader2,
  AlertCircle
} from 'lucide-react';

interface UsuarioModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: UsuarioRequest) => Promise<void>;
  initialUsuario?: UsuarioResponse | null;
}

export function UsuarioModal({
  isOpen,
  onClose,
  onSave,
  initialUsuario
}: UsuarioModalProps) {
  const [nombre, setNombre] = useState('');
  const [apellidos, setApellidos] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rol, setRol] = useState<Rol>('ALUMNO');

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialUsuario;

  useEffect(() => {
    if (initialUsuario) {
      setNombre(initialUsuario.nombre || '');
      setApellidos(initialUsuario.apellidos || '');
      setEmail(initialUsuario.email || '');
      setRol(initialUsuario.rol || 'ALUMNO');
      setPassword(''); // Por seguridad la contraseña no viaja desde el backend
    } else {
      setNombre('');
      setApellidos('');
      setEmail('');
      setPassword('');
      setRol('ALUMNO');
    }
    setErrorMessage('');
    setShowPassword(false);
  }, [initialUsuario, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!nombre.trim() || !apellidos.trim()) {
      setErrorMessage('El nombre y los apellidos son obligatorios.');
      return;
    }

    if (!email.trim() || !email.includes('@')) {
      setErrorMessage('Debes indicar un correo electrónico válido.');
      return;
    }

    if (!password || password.length < 6) {
      setErrorMessage(
        isEditing
          ? 'Para actualizar los datos del usuario debes especificar una contraseña válida (mínimo 6 caracteres).'
          : 'La contraseña es obligatoria y debe tener al menos 6 caracteres.'
      );
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        nombre: nombre.trim(),
        apellidos: apellidos.trim(),
        email: email.trim().toLowerCase(),
        password: password,
        rol: rol
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo guardar el usuario.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Cabecera */}
        <div className="px-6 py-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-rose-500/10 text-rose-400 border border-rose-500/20">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Cuenta de Usuario' : 'Nuevo Usuario'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? `Modificando los datos y credenciales de ${initialUsuario.nombre}`
                  : 'Crea un nuevo Administrador, Profesor o Alumno en el sistema.'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto flex-1">
          {errorMessage && (
            <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2.5">
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Selector de Rol */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-2">
              Perfil / Rol del Usuario <span className="text-rose-400">*</span>
            </label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setRol('ALUMNO')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  rol === 'ALUMNO'
                    ? 'bg-blue-500/15 border-blue-500/40 text-blue-300 shadow-sm shadow-blue-500/10'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <BookOpen className="w-4 h-4" />
                <span className="text-xs font-bold">ALUMNO</span>
                <span className="text-[10px] opacity-75">Estudiante</span>
              </button>

              <button
                type="button"
                onClick={() => setRol('PROFESOR')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  rol === 'PROFESOR'
                    ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300 shadow-sm shadow-emerald-500/10'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <GraduationCap className="w-4 h-4" />
                <span className="text-xs font-bold">PROFESOR</span>
                <span className="text-[10px] opacity-75">Docente</span>
              </button>

              <button
                type="button"
                onClick={() => setRol('ADMIN')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  rol === 'ADMIN'
                    ? 'bg-rose-500/15 border-rose-500/40 text-rose-300 shadow-sm shadow-rose-500/10'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <Shield className="w-4 h-4" />
                <span className="text-xs font-bold">ADMIN</span>
                <span className="text-[10px] opacity-75">Gestor total</span>
              </button>
            </div>
          </div>

          {/* Grid: Nombre y Apellidos */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Nombre <span className="text-rose-400">*</span>
              </label>
              <input
                type="text"
                required
                maxLength={50}
                placeholder="Ej. Carlos"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500 transition"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Apellidos <span className="text-rose-400">*</span>
              </label>
              <input
                type="text"
                required
                maxLength={100}
                placeholder="Ej. García López"
                value={apellidos}
                onChange={(e) => setApellidos(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500 transition"
              />
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <Mail className="w-3.5 h-3.5 text-rose-400" />
              Correo Electrónico <span className="text-rose-400">*</span>
            </label>
            <input
              type="email"
              required
              placeholder="nombre.apellido@baleras.edu"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500 transition"
            />
          </div>

          {/* Contraseña */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5 text-rose-400" />
                {isEditing ? 'Nueva Contraseña' : 'Contraseña de Acceso'}{' '}
                <span className="text-rose-400">*</span>
              </span>
              <span className="text-[11px] text-slate-500 font-normal">Mínimo 6 caracteres</span>
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                minLength={6}
                placeholder={isEditing ? 'Introduce la nueva contraseña...' : '••••••••'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-3.5 pr-10 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500 transition"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-200 transition cursor-pointer"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {isEditing && (
              <p className="mt-1 text-[11px] text-slate-400 leading-normal">
                Nota: El backend encriptará y actualizará la contraseña del usuario con el valor introducido.
              </p>
            )}
          </div>

          {/* Acciones */}
          <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2.5 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 px-5 py-2.5 bg-rose-600 hover:bg-rose-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-rose-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Crear Usuario'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

