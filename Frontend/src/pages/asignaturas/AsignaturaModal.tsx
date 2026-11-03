import { useState, useEffect, type FormEvent } from 'react';
import type { Asignatura, AsignaturaRequest } from '../../types/asignatura';
import type { Formacion } from '../../types/formacion';
import { formacionService } from '../../api/formacionService';
import { getApiErrorMessage } from '../../api/client';
import { X, BookOpen, GraduationCap, Code, Calendar, FileText, Loader2, AlertCircle } from 'lucide-react';

interface AsignaturaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: AsignaturaRequest) => Promise<void>;
  initialAsignatura?: Asignatura | null;
  formacionesList?: Formacion[];
  defaultFormacionId?: number | null;
}

const CURSOS_SUGERIDOS = [
  '1º Curso',
  '2º Curso',
  '3º Curso',
  '4º Curso',
  '1º Semestre',
  '2º Semestre'
];

export function AsignaturaModal({
  isOpen,
  onClose,
  onSave,
  initialAsignatura,
  formacionesList,
  defaultFormacionId
}: AsignaturaModalProps) {
  const [nombre, setNombre] = useState('');
  const [codigo, setCodigo] = useState('');
  const [curso, setCurso] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [formacionId, setFormacionId] = useState<number | ''>('');

  const [formaciones, setFormaciones] = useState<Formacion[]>(formacionesList || []);
  const [loadingFormaciones, setLoadingFormaciones] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialAsignatura;

  // Cargar lista de formaciones si no vienen pasadas
  useEffect(() => {
    if (isOpen) {
      if (formacionesList && formacionesList.length > 0) {
        setFormaciones(formacionesList);
      } else {
        setLoadingFormaciones(true);
        formacionService
          .getFormaciones()
          .then((data) => setFormaciones(data))
          .catch((err) => {
            setErrorMessage(getApiErrorMessage(err, 'No se pudieron cargar las formaciones disponibles.'));
          })
          .finally(() => setLoadingFormaciones(false));
      }
    }
  }, [isOpen, formacionesList]);

  // Actualizar formulario al abrir / cambiar asignatura
  useEffect(() => {
    if (initialAsignatura) {
      setNombre(initialAsignatura.nombre || '');
      setCodigo(initialAsignatura.codigo || '');
      setCurso(initialAsignatura.curso || '');
      setDescripcion(initialAsignatura.descripcion || '');
      setFormacionId(initialAsignatura.formacionId || '');
    } else {
      setNombre('');
      setCodigo('');
      setCurso('1º Curso');
      setDescripcion('');

      if (defaultFormacionId) {
        setFormacionId(defaultFormacionId);
      } else if (formaciones.length > 0 && formacionId === '') {
        setFormacionId(formaciones[0].id);
      } else {
        setFormacionId('');
      }
    }
    setErrorMessage('');
  }, [initialAsignatura, isOpen, formaciones, defaultFormacionId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!nombre.trim()) {
      setErrorMessage('El nombre de la asignatura es obligatorio.');
      return;
    }

    if (!formacionId) {
      setErrorMessage('Debes asociar la asignatura a una titulación o formación.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        nombre: nombre.trim(),
        codigo: codigo.trim() || undefined,
        curso: curso.trim() || undefined,
        descripcion: descripcion.trim() || undefined,
        formacionId: Number(formacionId),
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Ocurrió un error al guardar la asignatura.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Cabecera del modal */}
        <div className="px-6 py-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Asignatura' : 'Nueva Asignatura'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? 'Modifica los datos del módulo lectivo.'
                  : 'Añade una asignatura al plan formativo correspondiente.'}
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

          {/* Nombre */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Nombre de la Asignatura <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={100}
              placeholder="Ej. Programación Orientada a Objetos"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
            />
          </div>

          {/* Grid de 2 columnas: Código y Curso */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Code className="w-3.5 h-3.5 text-indigo-400" />
                Código
              </label>
              <input
                type="text"
                maxLength={20}
                placeholder="Ej. PROG, BBDD, LMD"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value.toUpperCase())}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 uppercase tracking-wider focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                Curso o Período
              </label>
              <input
                type="text"
                list="cursos-sugeridos"
                maxLength={50}
                placeholder="Ej. 1º Curso"
                value={curso}
                onChange={(e) => setCurso(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
              />
              <datalist id="cursos-sugeridos">
                {CURSOS_SUGERIDOS.map((c) => (
                  <option key={c} value={c} />
                ))}
              </datalist>
            </div>
          </div>

          {/* Formación a la que pertenece */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <GraduationCap className="w-3.5 h-3.5 text-indigo-400" />
              Formación / Titulación <span className="text-rose-400">*</span>
            </label>
            {loadingFormaciones ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-indigo-400" />
                <span>Cargando formaciones disponibles...</span>
              </div>
            ) : (
              <select
                required
                value={formacionId}
                onChange={(e) => setFormacionId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona la Formación --</option>
                {formaciones.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.nombre} {f.codigo ? `[${f.codigo}]` : ''} {f.centroNombre ? `- ${f.centroNombre}` : ''}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-indigo-400" />
              Descripción o Temario General
            </label>
            <textarea
              rows={3}
              maxLength={500}
              placeholder="Contenidos clave, objetivos didácticos, horas lectivas..."
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition resize-none"
            />
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
              className="flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-indigo-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Crear Asignatura'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

