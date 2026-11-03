# 🖥️ Gestión Baleras - Frontend SPA

[![React](https://img.shields.io/badge/React-19.0.0-blue.svg?logo=react&logoColor=white)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7.2-blue.svg?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8.3.5-646CFF.svg?logo=vite&logoColor=white)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4.0.0-38B2AC.svg?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![React Router](https://img.shields.io/badge/React%20Router-v7.3.0-CA4245.svg?logo=reactrouter&logoColor=white)](https://reactrouter.com/)
[![Axios](https://img.shields.io/badge/Axios-1.8.2-5A29E4.svg?logo=axios&logoColor=white)](https://axios-http.com/)
[![Lucide Icons](https://img.shields.io/badge/Lucide%20Icons-0.475.0-F56565.svg)](https://lucide.dev/)
[![Status](https://img.shields.io/badge/Production-Ready-brightgreen.svg)](#)

Frontend moderno, reactivo y de alto rendimiento para el sistema integral de administración académica **Gestión Baleras**. Construido como una Single Page Application (SPA) con **React 19**, **TypeScript**, **Vite** y **Tailwind CSS v4**, diseñado para interactuar de forma segura con la API REST de Spring Boot mediante tokens criptográficos **JWT** y control de acceso basado en roles (**RBAC**).

---

## 📋 Tabla de Contenidos

1. [Visión General y Principios de Diseño](#-visión-general-y-principios-de-diseño)
2. [Stack Tecnológico](#-stack-tecnológico)
3. [Estructura del Proyecto](#-estructura-del-proyecto)
4. [Seguridad y Control de Acceso (RBAC)](#-seguridad-y-control-de-acceso-rbac)
   - [Contexto de Autenticación (`AuthContext`)](#1-contexto-de-autenticación-authcontext)
   - [Cliente HTTP e Interceptores (`client.ts`)](#2-cliente-http-e-interceptores-clientts)
   - [Rutas Protegidas (`ProtectedRoute`)](#3-rutas-protegidas-protectedroute)
5. [Módulos Implementados en Detalle](#-módulos-implementados-en-detalle)
   - [1. Autenticación y Registro](#1-autenticación-y-registro-login-y-register)
   - [2. Panel de Control Adaptativo (Dashboard)](#2-panel-de-control-adaptativo-dashboard)
   - [3. Centros Educativos](#3-centros-educativos-centrospage)
   - [4. Formaciones y Asignaturas](#4-formaciones-y-asignaturas-formacionesasignaturaspage)
   - [5. Clases y Aulas](#5-clases-y-aulas-clasespage)
   - [6. Gestión de Usuarios](#6-gestión-de-usuarios-usuariospage)
   - [7. Matrículas de Alumnos](#7-matrículas-de-alumnos-matriculaspage)
   - [8. Notas y Calificaciones](#8-notas-y-calificaciones-notaspage)
6. [Matriz de Control de Acceso Frontend](#-matriz-de-control-de-acceso-frontend)
7. [Configuración y Proxy de Desarrollo](#-configuración-y-proxy-de-desarrollo)
8. [Instalación y Puesta en Marcha](#-instalación-y-puesta-en-marcha)
9. [Scripts Disponibles](#-scripts-disponibles)

---

## 💡 Visión General y Principios de Diseño

El frontend de **Gestión Baleras** está diseñado para ofrecer una experiencia de usuario fluida, accesible y profesional:

- **Tema Oscuro Premium (Dark Mode):** Interfaz estilizada en tonalidades `slate-900`/`slate-950` con contraste accesible y código de color semántico (azul para centros, morado para formaciones, ámbar para clases, esmeralda para matrículas, cian para calificaciones y carmesí para usuarios/administración).
- **Tipado Estricto de Extremo a Extremo:** Modelos e interfaces TypeScript que reflejan con total precisión los DTOs y Entities del backend Spring Boot.
- **Feedback Visual Inmediato:** Notificaciones contextuales de éxito y error, estados de carga con *spinners* optimizados, diálogos modales accesibles y confirmaciones destructivas para prevenir pérdidas de datos accidentales.
- **Diseño 100% Responsivo:** Adaptado para móviles, tabletas y monitores de escritorio mediante rejillas dinámicas de Tailwind CSS.
- **Navegación Profunda y Reactiva:** Uso de parámetros URL (`useSearchParams`) para sincronizar filtros, pestañas y expedientes en el historial del navegador.

---

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Rol en el Proyecto |
|---|---|---|
| **React** | `19.0.0` | Biblioteca declarativa de componentes de interfaz. |
| **TypeScript** | `5.7.2` | Tipado estático robusto, prevención de errores en compilación y autocompletado. |
| **Vite** | `8.3.5` | Bundler y entorno de desarrollo ultra-rápido con HMR y soporte nativo de módulos ES. |
| **Tailwind CSS** | `4.0.0` | Framework de diseño utilitario integrado directamente con `@tailwindcss/vite`. |
| **React Router DOM** | `7.3.0` | Enrutamiento del lado del cliente, rutas anidadas y guardias de navegación. |
| **Axios** | `1.8.2` | Cliente HTTP para consumo de API REST con interceptores de solicitud y respuesta. |
| **Lucide React** | `0.475.0` | Colección consistente y ligera de iconos vectoriales para interfaces modernas. |

---

## 📂 Estructura del Proyecto

```text
Frontend/
├── public/                       # Activos estáticos públicos
├── src/
│   ├── api/                      # Clientes de consumo API REST (Axios)
│   │   ├── client.ts             # Instancia configurada, interceptores JWT y extractor de errores
│   │   ├── authService.ts        # Métodos de Login y Register
│   │   ├── centroService.ts      # CRUD de Centros Educativos
│   │   ├── formacionService.ts   # CRUD de Formaciones y Titulaciones
│   │   ├── asignaturaService.ts  # CRUD de Asignaturas y Módulos
│   │   ├── claseService.ts       # CRUD de Clases, Aulas y Mis Clases
│   │   ├── usuarioService.ts     # CRUD de Usuarios y filtro de Profesores
│   │   ├── matriculaService.ts   # CRUD de Matrículas e inscripciones
│   │   └── notaService.ts        # CRUD de Calificaciones y expediente estudiantil
│   │
│   ├── components/               # Componentes reutilizables y estructurales
│   │   └── ProtectedRoute.tsx    # Guardia de rutas autenticadas y verificación RBAC
│   │
│   ├── context/                  # Estados globales de React Context
│   │   └── AuthContext.tsx       # Proveedor de sesión, token JWT, rol y persistencia
│   │
│   ├── pages/                    # Vistas principales del sistema
│   │   ├── Login.tsx             # Inicio de sesión con toggle de contraseña y validación
│   │   ├── Register.tsx          # Alta de nuevo estudiante (ALUMNO)
│   │   ├── Dashboard.tsx         # Panel central adaptativo según el perfil activo
│   │   ├── centros/
│   │   │   ├── CentrosPage.tsx   # Directorio, métricas y búsqueda de centros
│   │   │   └── CentroModal.tsx   # Modal de creación y edición de sedes
│   │   ├── formaciones/
│   │   │   ├── FormacionesAsignaturasPage.tsx # Vista unificada con pestañas dinámicas
│   │   │   └── FormacionModal.tsx             # Modal de creación/edición de formaciones
│   │   ├── asignaturas/
│   │   │   └── AsignaturaModal.tsx            # Modal de creación/edición de asignaturas
│   │   ├── clases/
│   │   │   ├── ClasesPage.tsx    # Listado, aulas en uso, turnos y mis clases
│   │   │   └── ClaseModal.tsx    # Asignación de aula, grupo y profesor titular
│   │   ├── usuarios/
│   │   │   ├── UsuariosPage.tsx  # Directorio con métricas y tarjetas interactivas
│   │   │   └── UsuarioModal.tsx  # Selector de perfil (Admin, Profesor, Alumno)
│   │   ├── matriculas/
│   │   │   ├── MatriculasPage.tsx# Control de estados (Activa, Finalizada, Cancelada)
│   │   │   └── MatriculaModal.tsx# Formulario de matriculación de alumnos en clases
│   │   └── notas/
│   │       ├── NotasPage.tsx     # Expediente de calificaciones y auditoría general
│   │       └── NotaModal.tsx     # Registro de nota con slider y feedback cualitativo
│   │
│   ├── types/                    # Definiciones TypeScript de entidades y contratos API
│   │   ├── auth.ts               # Rol, Usuario, AuthResponse, LoginRequest, RegisterRequest
│   │   ├── centro.ts             # Centro, CentroRequest
│   │   ├── formacion.ts          # Formacion, FormacionRequest
│   │   ├── asignatura.ts         # Asignatura, AsignaturaRequest
│   │   ├── clase.ts              # Clase, ClaseRequest
│   │   ├── usuario.ts            # UsuarioResponse, UsuarioRequest
│   │   ├── matricula.ts          # Matricula, MatriculaRequest, EstadoMatricula
│   │   └── nota.ts               # Nota, NotaRequest
│   │
│   ├── App.tsx                   # Declaración del Router y mapeo de rutas
│   ├── main.tsx                  # Punto de entrada de la aplicación React
│   └── index.css                 # Importación de Tailwind CSS
│
├── index.html                    # Plantilla base HTML5
├── package.json                  # Dependencias y scripts del frontend
├── tsconfig.json                 # Configuración del compilador TypeScript
└── vite.config.ts                # Configuración de Vite, plugins y Proxy API
```

---

## 🔒 Seguridad y Control de Acceso (RBAC)

### 1. Contexto de Autenticación (`AuthContext`)
Ubicación: [`Frontend/src/context/AuthContext.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/context/AuthContext.tsx)

El estado global de autenticación gestiona la identidad del usuario conectado:
- **Persistencia Segura:** Guarda el token JWT y los datos del usuario en `localStorage` bajo las claves `baleras_jwt_token` y `baleras_user_data`.
- **Detección de Carga Inicial:** Evita parpadeos o redirecciones en falso durante la hidratación de la sesión.
- **Helper `hasRole(rol)`:** Permite a los componentes consultar de forma declarativa si el usuario tiene un rol determinado (`ADMIN`, `PROFESOR`, `ALUMNO`).
- **Métodos expuestos:** `login(credentials)`, `register(data)`, `logout()`.

### 2. Cliente HTTP e Interceptores (`client.ts`)
Ubicación: [`Frontend/src/api/client.ts`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/api/client.ts)

Instancia centralizada de Axios configurada con:
- **Prefijo base:** `/api` (redirigido por el proxy de Vite hacia `http://localhost:8080/api`).
- **Interceptor de Petición (Request):** Inyecta automáticamente la cabecera `Authorization: Bearer <token>` en todas las solicitudes salientes si existe una sesión activa.
- **Interceptor de Respuesta (Response):** Detecta códigos de estado `401 Unauthorized`. Si el token ha expirado o no es válido, limpia el almacenamiento local y redirige al usuario a `/login`.
- **Extractor Unificado de Errores (`getApiErrorMessage`):** Extrae mensajes detallados del payload JSON del backend (`message`, `error`, o `errors`) con un mensaje de respaldo amigable.

### 3. Rutas Protegidas (`ProtectedRoute`)
Ubicación: [`Frontend/src/components/ProtectedRoute.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/components/ProtectedRoute.tsx)

Componente guardián que envuelve las páginas protegidas:
1. **Verificación de Carga:** Si la sesión aún se está sincronizando, renderiza un indicador de carga centrado.
2. **Verificación de Autenticación:** Si no hay token, redirige inmediatamente a `/login`.
3. **Verificación de Autorización:** Si se define la propiedad `allowedRoles={['ADMIN']}` y el usuario activo no posee uno de los roles permitidos, muestra una pantalla de **Acceso Restringido (403)** con botón para regresar a su panel correspondiente.

---

## 📦 Módulos Implementados en Detalle

### 1. Autenticación y Registro (`Login` y `Register`)

- **Login (`/login`):**
  - Formulario con campos de Email y Contraseña.
  - Botón de alternancia para ver/ocultar la contraseña.
  - Banner interactivo de credenciales de demostración (`ADMIN`, `PROFESOR`, `ALUMNO`) que permite autorellenar el formulario con un solo clic.
  - Notificaciones flotantes de error ante credenciales inválidas.
  - Redirección automática al `/dashboard` tras autenticación satisfactoria.
- **Registro (`/register`):**
  - Registro público de nuevos usuarios con selector interactivo de rol (**ALUMNO**, **PROFESOR** o **ADMIN**).
  - Validación estricta de campos obligatorios (nombre, apellidos, email válido y contraseña mínima de 6 caracteres).
  - Redirección con mensaje de confirmación hacia la pantalla de inicio de sesión.

---

### 2. Panel de Control Adaptativo (`Dashboard`)

Ubicación: [`Frontend/src/pages/Dashboard.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/Dashboard.tsx)

El panel principal adapta dinámicamente sus módulos según el rol del usuario conectado:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        DASHBOARD GESTIÓN BALERAS                       │
├────────────────────────────────────────────────────────────────────────┤
│ ROL: ADMIN                                                             │
│ ├── [Centros Educativos]       ├── [Formaciones & Asignaturas]         │
│ ├── [Clases & Aulas]           ├── [Matrículas de Alumnos]             │
│ ├── [Gestión de Usuarios]      └── [Auditoría General de Notas]        │
├────────────────────────────────────────────────────────────────────────┤
│ ROL: PROFESOR                                                          │
│ ├── [Centros Educativos]       ├── [Formaciones & Asignaturas]         │
│ ├── [Mis Clases Asignadas]     └── [Calificar Alumnos]                 │
├────────────────────────────────────────────────────────────────────────┤
│ ROL: ALUMNO                                                            │
│ ├── [Mis Calificaciones]       └── [Mis Matrículas]                    │
└────────────────────────────────────────────────────────────────────────┘
```

- **Banner de Bienvenida:** Muestra el nombre completo del usuario, su distintivo de rol y estado de la conexión.
- **Visor de Diagnóstico JWT:** Módulo inferior que inspecciona el token activo, formato de clave y emisor para facilitar auditorías técnicas.

---

### 3. Centros Educativos (`CentrosPage`)

Ubicación: [`Frontend/src/pages/centros/CentrosPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/centros/CentrosPage.tsx)

- **Funcionalidades:**
  - Consulta de sedes formativas adscritas.
  - Creación y edición mediante [`CentroModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/centros/CentroModal.tsx): Nombre, tipo de centro (IES, CIFP, Universidad), dirección, ciudad, código postal, teléfono y correo electrónico.
  - Buscador reactivo por nombre, tipo o ciudad.
  - Diálogo de confirmación destructiva antes de eliminar una sede.
- **Permisos:**
  - `ADMIN`: Creación, edición, eliminación y consulta.
  - `PROFESOR`: Consulta del catálogo de centros.

---

### 4. Formaciones y Asignaturas (`FormacionesAsignaturasPage`)

Ubicación: [`Frontend/src/pages/formaciones/FormacionesAsignaturasPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/formaciones/FormacionesAsignaturasPage.tsx)

- **Diseño con Pestañas Dinámicas (Tabs):**
  - **Pestaña 1 - Formaciones Académicas:**
    - Tarjetas con código oficial (ej. `DAM`, `DAW`, `ASIR`), nivel formativo (Grado Superior, Grado Medio, etc.) y centro educativo sede.
    - Contador de asignaturas pertenecientes a cada plan formativo.
    - Botón interactivo **"Ver asignaturas"**: Cambia a la Pestaña 2 y pre-filtra automáticamente las asignaturas de dicho ciclo formativo.
  - **Pestaña 2 - Asignaturas & Módulos:**
    - Código de módulo (ej. `PROG`, `BBDD`, `LMD`), curso lectivo (`1º Curso`, `2º Curso`) y formación matriz.
    - Filtro por titulación con botón de restablecimiento rápido.
- **Modales Asociados:**
  - [`FormacionModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/formaciones/FormacionModal.tsx): Selector de centro adscrito, lista de sugerencias de niveles y descripción curricular.
  - [`AsignaturaModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/asignaturas/AsignaturaModal.tsx): Selector de formación y sugerencias de curso lectivo.

---

### 5. Clases y Aulas (`ClasesPage`)

Ubicación: [`Frontend/src/pages/clases/ClasesPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/clases/ClasesPage.tsx)

- **Funcionalidades:**
  - Asignación de grupos de estudiantes a espacios físicos (aulas, talleres, laboratorios).
  - Vinculación obligatoria con una asignatura y un profesor titular.
  - **Cumplimiento de Regla de Negocio:** El modal de asignación ([`ClaseModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/clases/ClaseModal.tsx)) filtra estrictamente a los usuarios con rol activo de `PROFESOR`.
  - **Vista Específica para Profesores:** Selector que permite alternar entre *"Mis Clases Asignadas"* (consumiendo `/api/clases/mis-clases`) y *"Todas las Clases del Centro"*.
  - **Métricas Superiores:** Total de clases, aulas físicas en uso, grupos activos y plantilla de profesores.
  - Sugerencias automáticas de espacios (*Aula 101 - Informática*, *Laboratorio de Redes*, *Aula Magna*) y grupos (*Grupo A*, *1º DAM*, etc.).

---

### 6. Gestión de Usuarios (`UsuariosPage`)

Ubicación: [`Frontend/src/pages/usuarios/UsuariosPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/usuarios/UsuariosPage.tsx)

- **Funcionalidades:**
  - Directorio completo de usuarios registrados en el sistema.
  - Tarjetas de métricas interactivas por perfil (**Total**, **Alumnos**, **Profesores**, **Administradores**) que funcionan como filtro rápido al hacer clic.
  - Tarjetas de usuario con avatar generado por iniciales, distintivo del rol y badge especial **(Tú)** para la cuenta en sesión.
  - **Protección contra Auto-Bloqueo:** Deshabilita el botón de borrado en la cuenta del propio administrador conectado para evitar que se elimine a sí mismo por accidente.
- **Modal de Creación/Edición ([`UsuarioModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/usuarios/UsuarioModal.tsx)):**
  - Selector gráfico de perfiles con descripciones pedagógicas.
  - Encriptación de contraseña delegada al servidor mediante BCrypt (mínimo 6 caracteres).

---

### 7. Matrículas de Alumnos (`MatriculasPage`)

Ubicación: [`Frontend/src/pages/matriculas/MatriculasPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/matriculas/MatriculasPage.tsx)

- **Funcionalidades:**
  - Inscripción de estudiantes en clases activas.
  - Control visual de estados mediante badges de color:
    - `ACTIVA` (verde esmeralda): Matrícula en curso.
    - `FINALIZADA` (azul): Curso lectivo completado.
    - `CANCELADA` (rojo carmesí): Anulación de inscripción.
  - Tarjetas de estadísticas por estado con filtrado con un solo clic.
  - Desplegable de selección de clase y buscador en tiempo real por nombre de alumno, clase o asignatura.
- **Modal de Matriculación ([`MatriculaModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/matriculas/MatriculaModal.tsx)):**
  - Selector de alumno limitado a usuarios con perfil `ALUMNO`.
  - Selector de clase con previsualización del grupo y asignatura vinculada.
  - Prevención de duplicados informada por excepciones de la API (`409 Conflict`).

---

### 8. Notas y Calificaciones (`NotasPage`)

Ubicación: [`Frontend/src/pages/notas/NotasPage.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/notas/NotasPage.tsx)

- **Experiencia Adaptada por Rol:**
  - **Para el ALUMNO:** Vista *"Mis Calificaciones"* que calcula en tiempo real su nota media global (GPA), número de asignaturas aprobadas, comentarios pedagógicos y distinciones.
  - **Para el PROFESOR:** Botón `+ Calificar Alumno`, edición y borrado de notas de sus clases asignadas. Incluye métricas de rendimiento del grupo (% de aprobados, sobresalientes, etc.).
  - **Para el ADMINISTRADOR:** *"Auditoría General de Notas"*, con supervisión institucional completa de todos los expedientes y rendimiento por asignatura.
- **Modal de Calificación ([`NotaModal.tsx`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/src/pages/notas/NotaModal.tsx)):**
  - Calificación numérica en escala continua de `0.0` a `10.0` con paso decimal de `0.1`.
  - Control numérico sincronizado con una barra deslizante interactiva.
  - **Etiquetado Cualitativo Dinámico en Vivo:**
    - `0.0 - 4.9`: **Suspenso**
    - `5.0 - 6.9`: **Aprobado / Bien**
    - `7.0 - 8.9`: **Notable**
    - `9.0 - 10.0`: **Sobresaliente**
  - Caja de texto para feedback y observaciones pedagógicas (hasta 500 caracteres).

---

## 🛡️ Matriz de Control de Acceso Frontend

| Ruta Frontend | Componente Vista | `ADMIN` | `PROFESOR` | `ALUMNO` | Observaciones |
|---|---|:---:|:---:|:---:|---|
| `/login` | `Login.tsx` | Público | Público | Público | Redirige a `/dashboard` si ya hay sesión activa. |
| `/register` | `Register.tsx` | Público | Público | Público | Registro de usuarios con selector de rol (`ALUMNO`, `PROFESOR`, `ADMIN`). |
| `/dashboard` | `Dashboard.tsx` | ✅ Total | ✅ Docente | ✅ Alumno | Tarjetas y módulos adaptados al perfil activo. |
| `/centros` | `CentrosPage.tsx` | ✅ CRUD | 👁️ Lectura | ❌ | Gestión de sedes educativas. |
| `/formaciones` | `FormacionesAsignaturasPage.tsx` | ✅ CRUD | 👁️ Lectura | ❌ | Pestañas de formaciones y asignaturas. |
| `/asignaturas` | *Alias / Redirección* | ✅ CRUD | 👁️ Lectura | ❌ | Redirige a `/formaciones?tab=asignaturas`. |
| `/clases` | `ClasesPage.tsx` | ✅ CRUD | 👁️ Lectura + Mis Clases | ❌ | Asignación de aulas y turnos docentes. |
| `/aulas` | *Alias / Redirección* | ✅ CRUD | 👁️ Lectura | ❌ | Redirige a `/clases`. |
| `/usuarios` | `UsuariosPage.tsx` | ✅ CRUD | 👁️ Lectura | ❌ | Directorio institucional (con auto-bloqueo activo). |
| `/matriculas` | `MatriculasPage.tsx` | ✅ CRUD | 👁️ Lectura | ❌ | Inscripción y estados de matrícula. |
| `/notas` | `NotasPage.tsx` | 👁️ Auditoría | ✅ CRUD Docente | 👁️ Mis Notas | Calificación y consulta de expedientes. |
| `/calificaciones` | *Alias / Redirección* | 👁️ Auditoría | ✅ CRUD Docente | 👁️ Mis Notas | Redirige a `/notas`. |

---

## ⚙️ Configuración y Proxy de Desarrollo

En [`Frontend/vite.config.ts`](file:///c:/Users/raulo/IdeaProjects/Gestion%20Baleras/Frontend/vite.config.ts) se ha configurado un proxy transparente para evitar problemas de CORS y permitir el desarrollo fluido:

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss()
  ],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
});
```

Cualquier petición emitida por el cliente hacia `/api/...` es reenviada automáticamente a `http://localhost:8080/api/...`.

---

## 🚀 Instalación y Puesta en Marcha

### Requisitos Previos

- **Node.js**: Versión `18.0.0` o superior (Recomendado Node `20+` o `24+`).
- **npm**: Versión `9.0.0` o superior.
- **Backend Activo**: La API REST de Spring Boot debe estar ejecutándose en `http://localhost:8080`.

### 1. Instalación de Dependencias

Desde la raíz del repositorio o dentro del directorio `Frontend/`:

```bash
# Opción A: Desde la carpeta Frontend
cd Frontend
npm install

# Opción B: Desde la raíz del proyecto
npm install --prefix Frontend
```

### 2. Ejecución en Modo Desarrollo

```bash
# Desde la carpeta Frontend
npm run dev

# O desde la raíz del proyecto
npm run dev
```

El servidor Vite se iniciará en `http://localhost:5173` y abrirá automáticamente tu navegador web predeterminado.

### 3. Compilación para Producción

```bash
# Compilación TypeScript + bundle de producción Vite
npm run build
```

Los artefactos optimizados y minificados se generarán en la carpeta `Frontend/dist/`.

---

## 📜 Scripts Disponibles

En `Frontend/package.json`:

| Comando | Descripción |
|---|---|
| `npm run dev` | Inicia el servidor de desarrollo local con Vite y HMR en el puerto `5173`. |
| `npm run build` | Valida los tipos TypeScript (`tsc -b`) y empaqueta la aplicación para producción con Vite (`dist/`). |
| `npm run preview` | Levanta un servidor estático local para previsualizar los archivos compilados de producción en `dist/`. |
| `npm run lint` | Ejecuta Oxlint para análisis estático de código y detección temprana de errores. |

---

> **Gestión Baleras Frontend** © 2026. Diseñado para ofrecer la máxima eficiencia pedagógica y administrativa.
