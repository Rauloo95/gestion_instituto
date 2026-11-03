import { useState, useEffect, type FormEvent } from 'react';
import type { Clase, ClaseRequest } from '../../types/clase';
import type { Asignatura } from '../../types/asignatura';
import type { UsuarioResponse } from '../../types/usuario';
import { asignaturaService } from '../../api/asignaturaService';
import { usuarioService } from '../../api/usuarioService';
import { getApiErrorMessage } from '../../api/client';
import {
  X,
  Calendar,
  BookOpen,
  UserCheck,
  DoorOpen,
  Users,
  Loader2,
  AlertCircle
} from 'lucide-react';

interface ClaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: ClaseRequest) => Promise<void>;
  initialClase?: Clase | null;
  asignaturasList?: Asignatura[];
  profesoresList?: UsuarioResponse[];
  defaultAsignaturaId?: number | null;
}

const AULAS_SUGERIDAS = [
  'Aula 101 - Informática',
  'Aula 102 - Informática',
  'Aula 201 - Teoría',
  'Aula 202 - Teoría',
  'Laboratorio de Redes',
  'Laboratorio de Hardware',
  'Aula Magna',
  'Taller Multimedia'
];

const GRUPOS_SUGERIDOS = [
  'Grupo A (Mañanas)',
  'Grupo B (Tardes)',
  'Grupo Único',
  '1º DAM - Mañana',
  '2º DAM - Mañana',
  '1º DAW - Mañana',
  '2º DAW - Tarde'
];

export function ClaseModal({
  isOpen,
  onClose,
  onSave,
  initialClase,
  asignaturasList,
  profesoresList,
  defaultAsignaturaId
}: ClaseModalProps) {
  const [nombre, setNombre] = useState('');
  const [grupo, setGrupo] = useState('');
  const [aula, setAula] = useState('');
  const [asignaturaId, setAsignaturaId] = useState<number | ''>('');
  const [profesorId, setProfesorId] = useState<number | ''>('');

  const [asignaturas, setAsignaturas] = useState<Asignatura[]>(asignaturasList || []);
  const [profesores, setProfesores] = useState<UsuarioResponse[]>(profesoresList || []);
  const [isLoadingDependencies, setIsLoadingDependencies] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialClase;

  // Cargar asignaturas y profesores si no vienen proporcionados
  useEffect(() => {
    if (isOpen) {
      const loadData = async () => {
        setIsLoadingDependencies(true);
        try {
          const promises: [Promise<Asignatura[]>, Promise<UsuarioResponse[]>] = [
            asignaturasList && asignaturasList.length > 0
              ? Promise.resolve(asignaturasList)
              : asignaturaService.getAsignaturas(),
            profesoresList && profesoresList.length > 0
              ? Promise.resolve(profesoresList)
              : usuarioService.getProfesores()
          ];

          const [asigs, profs] = await Promise.all(promises);
          setAsignaturas(asigs);
          setProfesores(profs.filter((u) => u.rol === 'PROFESOR'));
        } catch (err) {
          setErrorMessage(getApiErrorMessage(err, 'Error al cargar asignaturas o profesores disponibles.'));
        } finally {
          setIsLoadingDependencies(false);
        }
      };

      loadData();
    }
  }, [isOpen, asignaturasList, profesoresList]);

  // Sincronizar datos al editar o abrir
  useEffect(() => {
    if (initialClase) {
      setNombre(initialClase.nombre || '');
      setGrupo(initialClase.grupo || '');
      setAula(initialClase.aula || '');
      setAsignaturaId(initialClase.asignaturaId || '');
      setProfesorId(initialClase.profesorId || '');
    } else {
      setNombre('');
      setGrupo('');
      setAula('');

      if (defaultAsignaturaId) {
        setAsignaturaId(defaultAsignaturaId);
      } else if (asignaturas.length > 0 && asignaturaId === '') {
        setAsignaturaId(asignaturas[0].id);
      } else {
        setAsignaturaId('');
      }

      if (profesores.length > 0 && profesorId === '') {
        setProfesorId(profesores[0].id);
      } else {
        setProfesorId('');
      }
    }
    setErrorMessage('');
  }, [initialClase, isOpen, asignaturas, profesores, defaultAsignaturaId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!nombre.trim()) {
      setErrorMessage('El nombre o identificador de la clase es obligatorio.');
      return;
    }

    if (!asignaturaId) {
      setErrorMessage('Debes seleccionar una asignatura para la clase.');
      return;
    }

    if (!profesorId) {
      setErrorMessage('Debes asignar un profesor titular con rol de PROFESOR.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        nombre: nombre.trim(),
        grupo: grupo.trim() || undefined,
        aula: aula.trim() || undefined,
        asignaturaId: Number(asignaturaId),
        profesorId: Number(profesorId)
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'No se pudo guardar la clase.'));
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
            <div className="p-2.5 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Clase & Aula' : 'Nueva Clase & Asignación'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? 'Modifica el grupo, aula o docente de esta clase.'
                  : 'Asigna un grupo de alumnos y un aula al profesor titular.'}
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

          {/* Nombre de la Clase */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Nombre de la Clase <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={100}
              placeholder="Ej. Programación I - Mañanas A"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500 transition"
            />
          </div>

          {/* Grid de 2 columnas: Grupo y Aula */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Users className="w-3.5 h-3.5 text-amber-400" />
                Grupo de Alumnos
              </label>
              <input
                type="text"
                list="grupos-sugeridos"
                maxLength={50}
                placeholder="Ej. Grupo A (Mañanas)"
                value={grupo}
                onChange={(e) => setGrupo(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500 transition"
              />
              <datalist id="grupos-sugeridos">
                {GRUPOS_SUGERIDOS.map((g) => (
                  <option key={g} value={g} />
                ))}
              </datalist>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <DoorOpen className="w-3.5 h-3.5 text-amber-400" />
                Aula o Espacio Físico
              </label>
              <input
                type="text"
                list="aulas-sugeridas"
                maxLength={50}
                placeholder="Ej. Aula 102 - Informática"
                value={aula}
                onChange={(e) => setAula(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500 transition"
              />
              <datalist id="aulas-sugeridas">
                {AULAS_SUGERIDAS.map((a) => (
                  <option key={a} value={a} />
                ))}
              </datalist>
            </div>
          </div>

          {/* Asignatura */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <BookOpen className="w-3.5 h-3.5 text-amber-400" />
              Asignatura <span className="text-rose-400">*</span>
            </label>
            {isLoadingDependencies ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-amber-400" />
                <span>Cargando catálogo de asignaturas...</span>
              </div>
            ) : (
              <select
                required
                value={asignaturaId}
                onChange={(e) => setAsignaturaId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona la Asignatura --</option>
                {asignaturas.map((asig) => (
                  <option key={asig.id} value={asig.id}>
                    {asig.nombre} {asig.codigo ? `[${asig.codigo}]` : ''} {asig.formacionNombre ? `(${asig.formacionNombre})` : ''}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Profesor Titular */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <UserCheck className="w-3.5 h-3.5 text-amber-400" />
              Profesor Titular <span className="text-rose-400">*</span>
            </label>
            {isLoadingDependencies ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-amber-400" />
                <span>Cargando plantilla de profesores...</span>
              </div>
            ) : (
              <select
                required
                value={profesorId}
                onChange={(e) => setProfesorId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona el Profesor Titular --</option>
                {profesores.map((prof) => (
                  <option key={prof.id} value={prof.id}>
                    {prof.nombre} {prof.apellidos} ({prof.email})
                  </option>
                ))}
              </select>
            )}
            <p className="mt-1 text-[11px] text-slate-400">
              Solo se listan usuarios con rol activo de <strong>PROFESOR</strong>.
            </p>
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
              className="flex items-center gap-2 px-5 py-2.5 bg-amber-600 hover:bg-amber-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-amber-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Crear Clase'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

