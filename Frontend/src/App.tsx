import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { CentrosPage } from './pages/centros/CentrosPage';
import { FormacionesAsignaturasPage } from './pages/formaciones/FormacionesAsignaturasPage';
import { ClasesPage } from './pages/clases/ClasesPage';
import { UsuariosPage } from './pages/usuarios/UsuariosPage';
import { MatriculasPage } from './pages/matriculas/MatriculasPage';
import { NotasPage } from './pages/notas/NotasPage';
import { ProtectedRoute } from './components/ProtectedRoute';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rutas Públicas */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Rutas Protegidas (requieren token JWT válido) */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Módulo de Centros Educativos (ADMIN y PROFESOR) */}
        <Route
          path="/centros"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR']}>
              <CentrosPage />
            </ProtectedRoute>
          }
        />

        {/* Módulo de Formaciones y Asignaturas (ADMIN y PROFESOR) */}
        <Route
          path="/formaciones"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR']}>
              <FormacionesAsignaturasPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/asignaturas"
          element={<Navigate to="/formaciones?tab=asignaturas" replace />}
        />
        <Route
          path="/formaciones-asignaturas"
          element={<Navigate to="/formaciones" replace />}
        />

        {/* Módulo de Clases & Aulas (ADMIN y PROFESOR) */}
        <Route
          path="/clases"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR']}>
              <ClasesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/aulas"
          element={<Navigate to="/clases" replace />}
        />

        {/* Módulo de Gestión de Usuarios (ADMIN y PROFESOR) */}
        <Route
          path="/usuarios"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR']}>
              <UsuariosPage />
            </ProtectedRoute>
          }
        />

        {/* Módulo de Matrículas (ADMIN y PROFESOR) */}
        <Route
          path="/matriculas"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR']}>
              <MatriculasPage />
            </ProtectedRoute>
          }
        />

        {/* Módulo de Notas & Calificaciones (ADMIN, PROFESOR y ALUMNO) */}
        <Route
          path="/notas"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'PROFESOR', 'ALUMNO']}>
              <NotasPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/calificaciones"
          element={<Navigate to="/notas" replace />}
        />

        {/* Redirección raíz y rutas desconocidas */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
