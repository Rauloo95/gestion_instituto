import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authService } from '../api/authService';
import type { AuthResponse, LoginRequest, RegisterRequest, Rol, Usuario } from '../types/auth';

interface AuthContextType {
  user: Usuario | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<AuthResponse>;
  register: (data: RegisterRequest) => Promise<Usuario>;
  logout: () => void;
  hasRole: (role: Rol | Rol[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'baleras_token';
const USER_KEY = 'baleras_user';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Inicialización: recuperar sesión de localStorage al cargar la app
  useEffect(() => {
    try {
      const storedToken = localStorage.getItem(TOKEN_KEY);
      const storedUser = localStorage.getItem(USER_KEY);

      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      }
    } catch (e) {
      console.error('Error al recuperar sesión guardada:', e);
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    const data = await authService.login(credentials);

    const loggedUser: Usuario = {
      id: data.id,
      nombre: data.nombre,
      email: data.email,
      rol: data.rol,
    };

    setToken(data.token);
    setUser(loggedUser);

    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(loggedUser));

    return data;
  };

  const register = async (data: RegisterRequest): Promise<Usuario> => {
    return await authService.register(data);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  };

  const hasRole = (role: Rol | Rol[]): boolean => {
    if (!user) return false;
    if (Array.isArray(role)) {
      return role.includes(user.rol);
    }
    return user.rol === role;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// Hook personalizado para consumir el contexto fácilmente
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un <AuthProvider>');
  }
  return context;
}

