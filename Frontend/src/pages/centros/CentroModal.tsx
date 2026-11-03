import { useState, useEffect, type FormEvent } from 'react';
import type { Centro, CentroRequest } from '../../types/centro';
import { X, Building2, MapPin, Phone, Mail, Loader2, AlertCircle } from 'lucide-react';

interface CentroModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: CentroRequest) => Promise<void>;
  initialCentro?: Centro | null;
}

export function CentroModal({ isOpen, onClose, onSave, initialCentro }: CentroModalProps) {
  const [nombre, setNombre] = useState('');
  const [tipo, setTipo] = useState('');
  const [direccion, setDireccion] = useState('');
  const [ciudad, setCiudad] = useState('');
  const [codigoPostal, setCodigoPostal] = useState('');
  const [telefono, setTelefono] = useState('');
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const isEditing = !!initialCentro;

  useEffect(() => {
    if (initialCentro) {
      setNombre(initialCentro.nombre || '');
      setTipo(initialCentro.tipo || '');
      setDireccion(initialCentro.direccion || '');
      setCiudad(initialCentro.ciudad || '');
      setCodigoPostal(initialCentro.codigoPostal || '');
      setTelefono(initialCentro.telefono || '');
      setEmail(initialCentro.email || '');
    } else {
      setNombre('');
      setTipo('');
      setDireccion('');
      setCiudad('');
      setCodigoPostal('');
      setTelefono('');
      setEmail('');
    }
    setErrorMessage('');
  }, [initialCentro, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!nombre.trim() || !tipo.trim()) {
      setErrorMessage('El nombre y el tipo de centro son obligatorios.');
      return;
    }

    setIsSubmitting(true);
    try {
      await onSave({
        nombre: nombre.trim(),
        tipo: tipo.trim(),
        direccion: direccion.trim() || undefined,
        ciudad: ciudad.trim() || undefined,
        codigoPostal: codigoPostal.trim() || undefined,
        telefono: telefono.trim() || undefined,
        email: email.trim() || undefined,
      });
      onClose();
    } catch (err: unknown) {
      setErrorMessage(err instanceof Error ? err.message : 'Error al guardar el centro');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl shadow-2xl overflow-hidden">
        {/* Cabecera del Modal */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-600/10 text-blue-400 rounded-xl border border-blue-500/20">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">
                {isEditing ? 'Editar Centro Educativo' : 'Nuevo Centro Educativo'}
              </h3>
              <p className="text-xs text-slate-400">
                {isEditing ? 'Modifica los datos del centro' : 'Introduce la información para registrar la sede'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Contenido del Formulario */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {errorMessage && (
            <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Nombre */}
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Nombre del Centro *
              </label>
              <input
                type="text"
                required
                maxLength={100}
                placeholder="Ej. Campus Baleras Central"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Tipo */}
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Tipo de Centro *
              </label>
              <input
                type="text"
                required
                maxLength={50}
                placeholder="Ej. Centro Integrado de FP / Instituto Tecnológico"
                value={tipo}
                onChange={(e) => setTipo(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Dirección */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5 text-slate-500" />
                Dirección
              </label>
              <input
                type="text"
                maxLength={150}
                placeholder="Calle Mayor 12"
                value={direccion}
                onChange={(e) => setDireccion(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Ciudad */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Ciudad
              </label>
              <input
                type="text"
                maxLength={100}
                placeholder="Palma de Mallorca"
                value={ciudad}
                onChange={(e) => setCiudad(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Código Postal */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Código Postal
              </label>
              <input
                type="text"
                maxLength={10}
                placeholder="07002"
                value={codigoPostal}
                onChange={(e) => setCodigoPostal(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Teléfono */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <Phone className="w-3.5 h-3.5 text-slate-500" />
                Teléfono
              </label>
              <input
                type="text"
                maxLength={20}
                placeholder="971000111"
                value={telefono}
                onChange={(e) => setTelefono(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>

            {/* Email */}
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5 flex items-center gap-1">
                <Mail className="w-3.5 h-3.5 text-slate-500" />
                Correo de Contacto
              </label>
              <input
                type="email"
                maxLength={150}
                placeholder="contacto@baleras-palma.edu"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3.5 py-2.5 bg-slate-800/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
            </div>
          </div>

          {/* Botones de acción */}
          <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2.5 rounded-xl text-sm font-medium text-slate-400 hover:text-white hover:bg-slate-800 transition"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold rounded-xl shadow-lg shadow-blue-600/30 flex items-center gap-2 transition disabled:opacity-50 cursor-pointer"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isEditing ? 'Actualizar Centro' : 'Crear Centro'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

