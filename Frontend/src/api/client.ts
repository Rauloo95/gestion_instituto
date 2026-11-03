import axios, { AxiosError } from 'axios';
import type { ApiError } from '../types/auth';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor de Petición: adjunta el token JWT si está almacenado
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('baleras_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor de Respuesta: gestiona sesiones expiradas (401)
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    // Si el backend devuelve 401 y no proviene del endpoint de login/registro
    const isAuthEndpoint = error.config?.url?.includes('/auth/login') || error.config?.url?.includes('/auth/register');

    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('baleras_token');
      localStorage.removeItem('baleras_user');
      // Redirige al login si estamos en el navegador
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        window.location.href = '/login?expired=true';
      }
    }

    return Promise.reject(error);
  }
);

// Función auxiliar para extraer el mensaje de error de la API de Spring Boot
export const getApiErrorMessage = (error: unknown, defaultMessage = 'Ha ocurrido un error inesperado'): string => {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError | undefined;
    if (apiError) {
      // Si hay errores de validación por campo, los mostramos ordenadamente
      if (apiError.errors && Object.keys(apiError.errors).length > 0) {
        return Object.values(apiError.errors).join('. ');
      }
      return apiError.message || apiError.error || defaultMessage;
    }
    if (error.message === 'Network Error') {
      return 'No se pudo conectar con el servidor backend. Comprueba que Spring Boot está ejecutándose.';
    }
    return error.message || defaultMessage;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return defaultMessage;
};

