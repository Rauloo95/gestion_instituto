import { useState, useEffect, type FormEvent } from 'react';
import type { Formacion, FormacionRequest } from '../../types/formacion';
import type { Centro } from '../../types/centro';
import { centroService } from '../../api/centroService';
import { getApiErrorMessage } from '../../api/client';
import { X, GraduationCap, Building2, Code, FileText, Loader2, AlertCircle } from 'lucide-react';

interface FormacionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: FormacionRequest) => Promise<void>;
  initialFormacion?: Formacion | null;
  centrosList?: Centro[];
}

const NIVELES_SUGERIDOS = [
  'Grado Superior (FP)',
  'Grado Medio (FP)',
  'Curso de Especialización',
  'Grado Básico',
  'Grado Universitario',
  'Máster'
];

export function FormacionModal({
  isOpen,
  onClose,
  onSave,
  initialFormacion,
  centrosList
}: FormacionModalProps) {
  const [nombre, setNombre] = useState('');
  const [codigo, setCodigo] = useState('');
  const [nivel, setNivel] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [centroId, setCentroId] = useState<number | ''>('');

  const [centros, setCentros] = useState<Centro[]>(centrosList || []);
  const [loadingCentros, setLoadingCentros] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialFormacion;

  // Cargar centros si no vienen provistos
  useEffect(() => {
    if (isOpen) {
      if (centrosList && centrosList.length > 0) {
        setCentros(centrosList);
      } else {
        setLoadingCentros(true);
        centroService
          .getCentros()
          .then((data) => setCentros(data))
          .catch((err) => {
            setErrorMessage(getApiErrorMessage(err, 'No se pudieron cargar los centros disponibles.'));
          })
          .finally(() => setLoadingCentros(false));
      }
    }
  }, [isOpen, centrosList]);

  // Rellenar formulario al editar o resetear al crear
  useEffect(() => {
    if (initialFormacion) {
      setNombre(initialFormacion.nombre || '');
      setCodigo(initialFormacion.codigo || '');
      setNivel(initialFormacion.nivel || '');
      setDescripcion(initialFormacion.descripcion || '');
      setCentroId(initialFormacion.centroId || '');
    } else {
      setNombre('');
      setCodigo('');
      setNivel('');
      setDescripcion('');
      // Si hay centros disponibles, seleccionar el primero por defecto
      if (centros.length > 0 && centroId === '') {
        setCentroId(centros[0].id);
      } else {
        setCentroId('');
      }
    }
    setErrorMessage('');
  }, [initialFormacion, isOpen, centros]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!nombre.trim()) {
      setErrorMessage('El nombre del plan de formación es obligatorio.');
      return;
    }

    if (!centroId) {
      setErrorMessage('Debes asociar la formación a un centro educativo.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        nombre: nombre.trim(),
        codigo: codigo.trim() || undefined,
        nivel: nivel.trim() || undefined,
        descripcion: descripcion.trim() || undefined,
        centroId: Number(centroId),
      });
      onClose();
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Ocurrió un error al guardar la formación académica.'));
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
            <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <GraduationCap className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Formación' : 'Nueva Formación Académica'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing
                  ? 'Modifica los datos del ciclo o plan formativo.'
                  : 'Crea una nueva titulación asociada a un centro.'}
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
              Nombre de la Formación <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={100}
              placeholder="Ej. Desarrollo de Aplicaciones Multiplataforma (DAM)"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 transition"
            />
          </div>

          {/* Grid de 2 columnas: Código y Nivel */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Code className="w-3.5 h-3.5 text-purple-400" />
                Código Oficial
              </label>
              <input
                type="text"
                maxLength={20}
                placeholder="Ej. DAM, DAW, ASIR"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value.toUpperCase())}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 uppercase tracking-wider focus:outline-none focus:ring-2 focus:ring-purple-500 transition"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Nivel Formativo
              </label>
              <input
                type="text"
                list="niveles-sugeridos"
                maxLength={50}
                placeholder="Ej. Grado Superior (FP)"
                value={nivel}
                onChange={(e) => setNivel(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 transition"
              />
              <datalist id="niveles-sugeridos">
                {NIVELES_SUGERIDOS.map((niv) => (
                  <option key={niv} value={niv} />
                ))}
              </datalist>
            </div>
          </div>

          {/* Centro Educativo */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-purple-400" />
              Centro Educativo <span className="text-rose-400">*</span>
            </label>
            {loadingCentros ? (
              <div className="flex items-center gap-2 text-xs text-slate-400 py-2">
                <Loader2 className="w-4 h-4 animate-spin text-purple-400" />
                <span>Cargando centros disponibles...</span>
              </div>
            ) : (
              <select
                required
                value={centroId}
                onChange={(e) => setCentroId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 transition cursor-pointer"
              >
                <option value="">-- Selecciona el Centro Sede --</option>
                {centros.map((centro) => (
                  <option key={centro.id} value={centro.id}>
                    {centro.nombre} {centro.ciudad ? `(${centro.ciudad})` : ''}
                  </option>
                ))}
              </select>
            )}
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-purple-400" />
              Descripción o Perfil Profesional
            </label>
            <textarea
              rows={3}
              maxLength={500}
              placeholder="Competencias generales, salidas profesionales o detalles de la titulación..."
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-purple-500 transition resize-none"
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
              className="flex items-center gap-2 px-5 py-2.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-purple-600/30 transition active:scale-[0.98] disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Guardar Cambios' : 'Crear Formación'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

