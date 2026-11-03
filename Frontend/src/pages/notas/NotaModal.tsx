import { useState, useEffect, type FormEvent } from 'react';
import type { Nota, NotaRequest } from '../../types/nota';
import type { Matricula } from '../../types/matricula';
import { matriculaService } from '../../api/matriculaService';
import { getApiErrorMessage } from '../../api/client';
import {
  X,
  Award,
  User,
  FileText,
  Loader2,
  AlertCircle,
  Sparkles
} from 'lucide-react';

interface NotaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: NotaRequest) => Promise<void>;
  initialNota?: Nota | null;
  matriculasList?: Matricula[];
  defaultMatriculaId?: number | null;
}

export function NotaModal({
  isOpen,
  onClose,
  onSave,
  initialNota,
  matriculasList,
  defaultMatriculaId
}: NotaModalProps) {
  const [matriculaId, setMatriculaId] = useState<number | ''>('');
  const [valor, setValor] = useState<number | ''>('');
  const [observaciones, setObservaciones] = useState('');

  const [matriculas, setMatriculas] = useState<Matricula[]>(matriculasList || []);
  const [loadingMatriculas, setLoadingMatriculas] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialNota;

  useEffect(() => {
    if (isOpen) {
      if (matriculasList && matriculasList.length > 0) {
        setMatriculas(matriculasList);
      } else {
        setLoadingMatriculas(true);
        matriculaService
          .getMatriculas()
          .then((data) => setMatriculas(data))
          .catch((err) => {
            setErrorMessage(getApiErrorMessage(err, 'Error al cargar las matrículas de alumnos.'));
          })
          .finally(() => setLoadingMatriculas(false));
      }
    }
  }, [isOpen, matriculasList]);

  useEffect(() => {
    if (initialNota) {
      setMatriculaId(initialNota.matriculaId || '');
      setValor(initialNota.valor !== undefined ? initialNota.valor : '');
      setObservaciones(initialNota.observaciones || '');
    } else {
      if (defaultMatriculaId) {
        setMatriculaId(defaultMatriculaId);
      } else if (matriculas.length > 0 && matriculaId === '') {
        setMatriculaId(matriculas[0].id);
      } else {
        setMatriculaId('');
      }

      setValor(5.0);
      setObservaciones('');
    }
    setErrorMessage('');
  }, [initialNota, isOpen, matriculas, defaultMatriculaId]);

  if (!isOpen) return null;

  const numValor = typeof valor === 'number' ? valor : 0;
  const getCalificacionQualitative = (v: number) => {
    if (v < 5) return { label: 'Suspenso', color: 'text-rose-400 bg-rose-500/10 border-rose-500/30' };
    if (v < 7) return { label: 'Aprobado / Bien', color: 'text-amber-400 bg-amber-500/10 border-amber-500/30' };
    if (v < 9) return { label: 'Notable', color: 'text-blue-400 bg-blue-500/10 border-blue-500/30' };
    return { label: 'Sobresaliente', color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' };
  };

  const calificacionStatus = getCalificacionQualitative(numValor);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!matriculaId) {
      setErrorMessage('Debes seleccionar el alumno y matrícula a calificar.');
      return;
    }

    if (valor === '' || isNaN(Number(valor)) || Number(valor) < 0 || Number(valor) > 10) {
      setErrorMessage('La nota debe ser una calificación válida entre 0.0 y 10.0.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        matriculaId: Number(matriculaId),
        valor: Number(Number(valor).toFixed(2)),
        observaciones: observaciones.trim() || undefined
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo guardar la calificación.'));
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
            <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Calificación' : 'Emitir Calificación'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? 'Modifica la nota u observaciones registradas.'
                  : 'Asigna una nota y observaciones cualitativas al alumno.'}
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

          {/* Selector de Matrícula */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-cyan-400" />
              Alumno y Matrícula <span className="text-rose-400">*</span>
            </label>
            {loadingMatriculas ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-cyan-400" />
                <span>Cargando matrículas disponibles...</span>
              </div>
            ) : (
              <select
                required
                disabled={isEditing}
                value={matriculaId}
                onChange={(e) => setMatriculaId(e.target.value ? Number(e.target.value) : '')}
                className={`w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 transition ${
                  isEditing ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'
                }`}
              >
                <option value="">-- Selecciona el Alumno y Clase --</option>
                {matriculas.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.alumnoNombre} - {m.claseNombre} ({m.asignaturaNombre})
                  </option>
                ))}
              </select>
            )}
            {isEditing && (
              <p className="mt-1 text-[11px] text-slate-400">
                La matrícula asociada no se puede modificar durante la edición de nota.
              </p>
            )}
          </div>

          {/* Valor de la Nota y Calificación Cualitativa */}
          <div className="p-4 rounded-2xl bg-slate-800/40 border border-slate-700/60 space-y-3">
            <div className="flex items-center justify-between">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                <Award className="w-3.5 h-3.5 text-cyan-400" />
                Calificación Numérica (0.0 - 10.0) <span className="text-rose-400">*</span>
              </label>

              {/* Distintivo Cualitativo en tiempo real */}
              <span
                className={`px-2.5 py-0.5 rounded-lg border text-xs font-bold transition flex items-center gap-1 ${calificacionStatus.color}`}
              >
                <Sparkles className="w-3 h-3" />
                <span>{calificacionStatus.label}</span>
              </span>
            </div>

            <div className="flex items-center gap-4">
              <input
                type="number"
                step="0.1"
                min="0"
                max="10"
                required
                placeholder="5.0"
                value={valor}
                onChange={(e) =>
                  setValor(e.target.value === '' ? '' : parseFloat(e.target.value))
                }
                className="w-32 px-3.5 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white font-bold text-lg text-center focus:outline-none focus:ring-2 focus:ring-cyan-500 transition"
              />

              {/* Control de rango visual deslizante */}
              <input
                type="range"
                min="0"
                max="10"
                step="0.1"
                value={numValor}
                onChange={(e) => setValor(parseFloat(e.target.value))}
                className="w-full accent-cyan-500 cursor-pointer"
              />
            </div>
          </div>

          {/* Observaciones / Feedback del Profesor */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-cyan-400" />
              Observaciones / Comentarios Pedagógicos
            </label>
            <textarea
              rows={3}
              maxLength={500}
              placeholder="Detalla las fortalezas demostradas, áreas de mejora o detalles de la evaluación continua..."
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 transition resize-none"
            />
            <div className="flex justify-end mt-1 text-[11px] text-slate-400">
              {observaciones.length} / 500 caracteres
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
              className="flex items-center gap-2 px-5 py-2.5 bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-cyan-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Registrar Calificación'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

