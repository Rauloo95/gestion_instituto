import { useState, useEffect, type FormEvent } from 'react';
import type { Matricula, MatriculaRequest, EstadoMatricula } from '../../types/matricula';
import type { UsuarioResponse } from '../../types/usuario';
import type { Clase } from '../../types/clase';
import { usuarioService } from '../../api/usuarioService';
import { claseService } from '../../api/claseService';
import { getApiErrorMessage } from '../../api/client';
import {
  X,
  ClipboardList,
  User,
  Calendar,
  CheckCircle2,
  Clock,
  XCircle,
  Loader2,
  AlertCircle
} from 'lucide-react';

interface MatriculaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: MatriculaRequest) => Promise<void>;
  initialMatricula?: Matricula | null;
  alumnosList?: UsuarioResponse[];
  clasesList?: Clase[];
  defaultClaseId?: number | null;
}

export function MatriculaModal({
  isOpen,
  onClose,
  onSave,
  initialMatricula,
  alumnosList,
  clasesList,
  defaultClaseId
}: MatriculaModalProps) {
  const [alumnoId, setAlumnoId] = useState<number | ''>('');
  const [claseId, setClaseId] = useState<number | ''>('');
  const [estado, setEstado] = useState<EstadoMatricula>('ACTIVA');

  const [alumnos, setAlumnos] = useState<UsuarioResponse[]>(alumnosList || []);
  const [clases, setClases] = useState<Clase[]>(clasesList || []);
  const [isLoadingDependencies, setIsLoadingDependencies] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialMatricula;

  useEffect(() => {
    if (isOpen) {
      const loadData = async () => {
        setIsLoadingDependencies(true);
        try {
          const promises: [Promise<UsuarioResponse[]>, Promise<Clase[]>] = [
            alumnosList && alumnosList.length > 0
              ? Promise.resolve(alumnosList)
              : usuarioService.getUsuarios(),
            clasesList && clasesList.length > 0
              ? Promise.resolve(clasesList)
              : claseService.getClases()
          ];

          const [usuariosData, clasesData] = await Promise.all(promises);
          setAlumnos(usuariosData.filter((u) => u.rol === 'ALUMNO'));
          setClases(clasesData);
        } catch (err) {
          setErrorMessage(getApiErrorMessage(err, 'Error al cargar alumnos o clases disponibles.'));
        } finally {
          setIsLoadingDependencies(false);
        }
      };

      loadData();
    }
  }, [isOpen, alumnosList, clasesList]);

  useEffect(() => {
    if (initialMatricula) {
      setAlumnoId(initialMatricula.alumnoId || '');
      setClaseId(initialMatricula.claseId || '');
      setEstado(initialMatricula.estado || 'ACTIVA');
    } else {
      if (alumnos.length > 0 && alumnoId === '') {
        setAlumnoId(alumnos[0].id);
      } else {
        setAlumnoId('');
      }

      if (defaultClaseId) {
        setClaseId(defaultClaseId);
      } else if (clases.length > 0 && claseId === '') {
        setClaseId(clases[0].id);
      } else {
        setClaseId('');
      }

      setEstado('ACTIVA');
    }
    setErrorMessage('');
  }, [initialMatricula, isOpen, alumnos, clases, defaultClaseId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!alumnoId) {
      setErrorMessage('Debes seleccionar un alumno con perfil de estudiante.');
      return;
    }

    if (!claseId) {
      setErrorMessage('Debes seleccionar la clase en la que matricular al alumno.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        alumnoId: Number(alumnoId),
        claseId: Number(claseId),
        estado: estado
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo registrar la matrícula.'));
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
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <ClipboardList className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Matrícula' : 'Nueva Matrícula'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? 'Modifica el estado o vinculación de la matrícula.'
                  : 'Inscribe a un alumno en una clase lectiva activa.'}
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

          {/* Selector de Alumno */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-emerald-400" />
              Alumno Estudiante <span className="text-rose-400">*</span>
            </label>
            {isLoadingDependencies ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-emerald-400" />
                <span>Cargando lista de alumnos...</span>
              </div>
            ) : (
              <select
                required
                value={alumnoId}
                onChange={(e) => setAlumnoId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona el Alumno --</option>
                {alumnos.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.nombre} {a.apellidos} ({a.email})
                  </option>
                ))}
              </select>
            )}
            <p className="mt-1 text-[11px] text-slate-400">
              Solo se listan usuarios con rol activo de <strong>ALUMNO</strong>.
            </p>
          </div>

          {/* Selector de Clase */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-emerald-400" />
              Clase y Convocatoria <span className="text-rose-400">*</span>
            </label>
            {isLoadingDependencies ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-emerald-400" />
                <span>Cargando clases activas...</span>
              </div>
            ) : (
              <select
                required
                value={claseId}
                onChange={(e) => setClaseId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona la Clase --</option>
                {clases.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre} {c.grupo ? `[${c.grupo}]` : ''} {c.asignaturaNombre ? `(${c.asignaturaNombre})` : ''}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Selector de Estado */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-2">
              Estado de la Matrícula <span className="text-rose-400">*</span>
            </label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setEstado('ACTIVA')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  estado === 'ACTIVA'
                    ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300 shadow-sm'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span className="text-xs font-bold">ACTIVA</span>
                <span className="text-[10px] opacity-75">En curso</span>
              </button>

              <button
                type="button"
                onClick={() => setEstado('FINALIZADA')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  estado === 'FINALIZADA'
                    ? 'bg-blue-500/15 border-blue-500/40 text-blue-300 shadow-sm'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <Clock className="w-4 h-4 text-blue-400" />
                <span className="text-xs font-bold">FINALIZADA</span>
                <span className="text-[10px] opacity-75">Completada</span>
              </button>

              <button
                type="button"
                onClick={() => setEstado('CANCELADA')}
                className={`p-3 rounded-2xl border text-center transition flex flex-col items-center gap-1.5 cursor-pointer ${
                  estado === 'CANCELADA'
                    ? 'bg-rose-500/15 border-rose-500/40 text-rose-300 shadow-sm'
                    : 'bg-slate-800/40 border-slate-800 text-slate-400 hover:bg-slate-800/80 hover:text-slate-300'
                }`}
              >
                <XCircle className="w-4 h-4 text-rose-400" />
                <span className="text-xs font-bold">CANCELADA</span>
                <span className="text-[10px] opacity-75">Anulada</span>
              </button>
            </div>
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
              className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-emerald-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Confirmar Matrícula'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

