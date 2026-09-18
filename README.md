# Gestor Personal de Tareas - Solución de Taller Académico

Esta aplicación es una solución de software móvil nativa para Android, desarrollada bajo estándares de calidad corporativa como **Arquitectura Limpia (Clean Architecture)** y el patrón de diseño **MVVM (Model-View-ViewModel)**. El objetivo principal es proporcionar un sistema de administración y sincronización híbrida de tareas personales que combina persistencia local y almacenamiento en la nube en tiempo real.

## 👥 Integrantes y Autoría
* **Desarrollador / Estudiante:** [Tu Nombre Completo Aquí]
* **Institución:** Servicio Nacional de Aprendizaje (SENA)
* **Curso / Ficha:** [Tu Ficha Aquí]

---

## 🛠️ Tecnologías y Frameworks Implementados
* **Lenguaje:** Kotlin 2.0.21 con Kotlin Coroutines y Flow reactivo.
* **UI Toolkit:** Jetpack Compose (Diseño Declarativo) integrado con Material Design 3.
* **Inyección de Dependencias:** Google Hilt (Dagger) para la gestión desacoplada del ciclo de vida de los componentes.
* **Persistencia Local (Modo Offline):** Room Database con KSP (Kotlin Symbol Processing) para almacenamiento de borradores.
* **Persistencia Remota (Modo Online):** Firebase Cloud Firestore con sincronización activa y oyentes en tiempo real (`addSnapshotListener`).
* **Autenticación de Usuarios:** Firebase Authentication (Manejo seguro de tokens, registro validado y sesiones persistentes).
* **Navegación:** Jetpack Navigation Compose para el control seguro de flujos y pantallas de la UI.

---

## 🏛️ Arquitectura de Software Implementada
El proyecto está rigurosamente desacoplado en tres capas fundamentales, aislando por completo las reglas del negocio de cualquier infraestructura externa o framework:

1. **Capa Domain (Dominio):** Contiene las entidades puras del negocio (`Task`, `User`), interfaces de los repositorios y la definición modular de casos de uso agrupados (`AuthUseCases`, `TaskUseCases`). **Cero dependencias de Android, Room o Firebase.**
2. **Capa Data (Datos):** Implementa las interfaces de repositorios del dominio. Integra los Data Sources: `FirebaseAuth` para la sesión, `FirebaseFirestore` para las colecciones y `TaskDraftDao` de Room para los borradores en local. Incluye mappers puros bidireccionales.
3. **Capa UI (Presentación):** Estructura visual reactiva con Jetpack Compose y patrones de estado explícitos administrados por `ViewModel` que capturan el estado asíncrono y lo exponen mediante `StateFlow`.

---

## 📂 Estructura de Paquetes
```text
com.sena.crud/
├── BaseApplication.kt               # Entrada global de la App, inicialización de Hilt
├── MainActivity.kt                  # Actividad Única, anfitrión del NavHost reactivo
├── di/                              # Capa de Inyección de Dependencias (Hilt Modules)
│   ├── DatabaseModule.kt            # Proveedor de Room Database y DAOs de borradores
│   ├── FirebaseModule.kt            # Proveedor de instancias de FirebaseAuth y Firestore
│   └── RepositoryModule.kt          # Binds semánticos entre interfaces de dominio e impls
├── domain/                          # Capa de Dominio (Reglas de Negocio Puras)
│   ├── model/                       # Entidades de datos básicas (Task, User)
│   ├── repository/                  # Contratos e interfaces de persistencia de datos
│   └── usecase/                     # Casos de uso atómicos (auth/*, task/*)
├── data/                            # Capa de Datos (Infraestructura y Persistencia)
│   ├── local/                       # Componentes locales de Room (entity, dao, database)
│   ├── remote/                      # Modelos o integraciones de red si aplican
│   ├── mapper/                      # Funciones de extensión para transformación limpia de datos
│   └── repository/                  # Implementación de repositorios (Lógica de publicación segura)
└── ui/                              # Capa de Presentación (Interfaz de Usuario)
    ├── auth/                        # Componentes de Autenticación (ViewModels y Screens)
    └── task/                        # Componentes de Gestión de Tareas (ViewModels y Screens)
```

---

## ⚙️ Instrucciones de Configuración y Ejecución

### Requisitos Previos
1. **Android Studio** instalado (Versión Ladybug o superior).
2. **JDK 17** configurado en el sistema y en Gradle.
3. Dispositivo físico Android conectado por cable USB con la **Depuración por USB** activada.

### Pasos para Configurar e Instalar:
1. **Clonar o descargar** este repositorio en tu máquina.
2. Abre la consola de [Firebase Console](https://console.firebase.google.com/) y crea un proyecto llamado `MiAppCompose`.
3. Registra una aplicación Android en el proyecto con el paquete exacto: `com.sena.crud`.
4. Descarga el archivo `google-services.json` proporcionado por Firebase y colócalo dentro de la carpeta del proyecto en la ruta raíz del módulo: `app/google-services.json`.
5. En la consola de Firebase, activa los servicios de **Authentication** (Habilitar proveedor Correo/Contraseña) y **Cloud Firestore** en modo de prueba.
6. Abre el proyecto en Android Studio, espera a que la sincronización de Gradle finalice con éxito.
7. Selecciona tu teléfono móvil en la barra superior de dispositivos y presiona el botón **Run** (índice ▷). La aplicación se instalará automáticamente por USB.

---

## 🚀 Funcionalidades Terminadas (Requerimientos Cumplidos)
* **RF01 - Registro de Usuarios:** Formulario completo con validación interactiva que exige correo en formato correcto, clave mayor o igual a 6 caracteres y coincidencia exacta de la confirmación.
* **RF02 - Inicio de Sesión:** Autenticación fluida conectada a Firebase Auth con manejo explícito de errores y cargadores visuales.
* **RF03 - Conservación de Sesión:** Al abrir la aplicación, el sistema intercepta si el token de usuario está activo y lo desvía directamente a sus tareas sin pedir credenciales nuevamente.
* **RF04 - Filtrado por Usuario:** Las tareas de Firestore se aíslan por completo mediante consultas estructuradas vinculadas exclusivamente al `ownerId` del usuario autenticado.
* **RF05/RF06/RF07 - CRUD Completo de Tareas:** Crear, editar y eliminar tareas remotas en la nube de forma asíncrona con sincronización reactiva en tiempo real.
* **RF08 - Flujo de Publicación Segura (Borradores):** El usuario puede elegir guardar localmente como borrador (Room). Al intentar publicar, si el guardado en la nube tiene éxito se borra de Room de inmediato; si Firestore falla por falta de red, el borrador se preserva localmente e informa al usuario.
* **RF09 - Seguridad Perimetral:** Reglas de escritura y lectura (`firestore.rules`) aplicadas en el servidor para evitar accesos no autorizados a datos ajenos.

### Errores Conocidos:
* *Ninguno:* El sistema compila y ejecuta con 0 errores de sintaxis y manejo de hilos optimizado.

---

## 📸 Guía para Adjuntar Capturas de Pantalla Principales
*(Para tu entrega final, reemplaza los archivos de imagen dentro de la carpeta de recursos de tu repositorio con las capturas de tu celular)*
* **Pantalla de Login:** `docs/screenshots/login.png`
* **Pantalla de Registro:** `docs/screenshots/register.png`
* **Lista de Tareas (Cloud Firestore):** `docs/screenshots/tasks_list.png`
* **Formulario de Tareas:** `docs/screenshots/task_form.png`
* **Panel de Borradores Locales (Room):** `docs/screenshots/drafts_panel.png`
