import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Rol } from '../types/auth';
import { Loader2 } from 'lucide-react';

interface ProtectedRouteProps {
  allowedRoles?: Rol[];
  children?: React.ReactNode;
}

export function ProtectedRoute({ allowedRoles, children }: ProtectedRouteProps) {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-slate-300">
        <Loader2 className="w-10 h-10 text-blue-500 animate-spin mb-4" />
        <p className="text-sm font-medium">Verificando sesión...</p>
      </div>
    );
  }

  // Si no está autenticado, redirigir al login guardando la ubicación intentada
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Si se requieren roles específicos y el usuario no lo tiene
  if (allowedRoles && !allowedRoles.includes(user.rol)) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center p-6 text-center">
        <div className="w-16 h-16 rounded-full bg-rose-500/10 text-rose-400 flex items-center justify-center mb-4">
          <span className="text-2xl font-bold">403</span>
        </div>
        <h2 className="text-2xl font-bold text-white mb-2">Acceso No Autorizado</h2>
        <p className="text-sm text-slate-400 max-w-md mb-6">
          Tu rol actual ({user.rol}) no tiene los permisos suficientes para acceder a este módulo.
        </p>
        <Navigate to="/dashboard" replace />
      </div>
    );
  }

  return children ? <>{children}</> : <Outlet />;
}

