# Dedalo

Aplicación mobile para gestión de obras de construcción con etapas, chat IA y perfil de usuario.

Proyecto universitario — **UADE**, Desarrollo de Aplicaciones I  
**Alumno:** Matias Gabriel Audine

---

## Stack

- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + Hilt DI (KSP)
- **Base de datos local:** Room 2.7.1 (offline-first, SSOT)
- **Backend:** Firebase Auth + Firestore (metadata/URLs), Supabase Storage (archivos de imagen)
- **Geocoding:** LocationIQ (Retrofit)
- **Chat IA:** Gemini API (Retrofit)
- **Mapa:** Google Maps vía geo intent desde detalle de obra
- **Tests:** MockK + Turbine + StandardTestDispatcher

## Features

- [x] Autenticación con Google
- [x] CRUD de obras con datos urbanísticos (FOS, FOT)
- [x] Edición de obra desde pantalla de detalle
- [x] Etapas de obra con estados (ESPERA / EN_PROGRESO / FINALIZADA)
- [x] Cambio de estado de etapas desde el detalle
- [x] Imágenes en proyectos y etapas con selector de destino
- [x] Galería de imágenes con carga a Supabase Storage
- [x] Geocoding automático al ingresar dirección (LocationIQ)
- [x] Chat con IA (Gemini)
- [x] Perfil de usuario con galería estilo Instagram
- [x] Persistencia offline (Room sincronizado con Firestore)
- [x] DI con Hilt (migrado desde DI manual)
- [x] Apertura en Google Maps desde detalle de obra

## Movimiento de Datos

Dedalo sigue una arquitectura **offline-first** donde **Room es la fuente única de verdad (SSOT)**.

### Pipeline general

```
Firestore ──(Snapshot Listener)──> Room (SSOT) ──(Flow DAO)──> Repository ──(Flow)──> ViewModel ──(StateFlow)──> UI (collectAsStateWithLifecycle)
```

### Lectura de datos
1. Room expone `Flow<>` a través de los DAOs (`observeByUserId`, `observeById`, etc.)
2. Los repositorios combinan y mapean entidades Room a modelos de dominio
3. Los ViewModels transforman los dominios en `UiState` (sealed interface: `Loading | Success | Error`)
4. La UI observa con `collectAsStateWithLifecycle()` y renderiza según el estado
5. **La UI nunca observa Firestore directamente**

### Escritura de datos (write-through)
1. La mutación se envía primero a Firestore
2. `FirebaseSyncManager` recibe el cambio vía snapshot listener
3. Escribe en Room (upsert)
4. Room propaga el cambio vía `Flow` al Repository → ViewModel → UI

### Sincronización en tiempo real
- `FirebaseSyncManager` (singleton, iniciado en `AppModule`) escucha `AuthStateListener`
- Al iniciar sesión, registra listeners sobre la colección `projects` y el collection group `stages` (filtrados por `userId`)
- Cada `ADDED`/`MODIFIED` → `projectDao.upsert()` o `stageDao.upsertAll()`
- Cada `REMOVED` → `projectDao.deleteById()`
- `syncMyProjectsToFirebase()` permite sincronización manual local → remota desde el perfil

### Flujo de imágenes
1. Usuario selecciona imagen → se sube a **Supabase Storage** (bucket `images`)
2. Se obtiene la URL pública → se guarda en **Firestore** (campo `images` como JSON)
3. El snapshot listener la persiste en **Room** (tipo `List<ImageData>` serializado con Gson)
4. Coil carga la imagen desde la URL pública de Supabase

### Flujo del Chat IA
1. Usuario envía mensaje → se inserta en **Room** (`ChatMessageDao.insert()`)
2. Se envía request a **Gemini API** (`GeminiApiService`)
3. La respuesta del modelo se inserta en **Room**
4. Room propaga ambos mensajes a la UI vía `Flow`

### Flujo de autenticación
1. `AuthViewModel` escucha `FirebaseAuth.AuthStateListener` mediante `callbackFlow`
2. Al iniciar sesión con Google → `AuthRepository.signInWithGoogle()` autentica en Firebase
3. El usuario se persiste en **Room** (`userDao.upsert()` con flag `isCurrentUser`)
4. `FirebaseSyncManager` reacciona al cambio de auth y reinicia los listeners
5. Al cerrar sesión → se limpian Room y los listeners

### Flujo de geocoding (LocationIQ)
1. Usuario escribe dirección en pantalla de crear/editar obra
2. `CreateProjectViewModel.queryFlow` aplica `debounce(2500)` y `filter { it.length >= 3 }`
3. Se llama a `LocationiqService` vía Retrofit
4. Se actualiza el estado del UI con `lat`, `lon`, `display_name`

### Diagrama de capas por feature

```
┌─────────────────────────────────────────────────┐
│  UI Layer (Composables)                         │
│  collectAsStateWithLifecycle()                  │
├─────────────────────────────────────────────────┤
│  ViewModel Layer                                │
│  StateFlow<UiState>                             │
├─────────────────────────────────────────────────┤
│  Domain Layer (modelos, UiState sealed)         │
├─────────────────────────────────────────────────┤
│  Repository Layer                               │
│  Flow<> desde DAOs + lógica de negocio         │
├─────────────────────────────────────────────────┤
│  Data Layer                                     │
│  ┌──────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ Room     │  │ Firebase     │  │ Retrofit  │ │
│  │ (SSOT)   │  │ Auth/Firest. │  │ (APIs)    │ │
│  └──────────┘  └──────────────┘  └───────────┘ │
│  ┌──────────────────────┐                       │
│  │ Supabase Storage     │                       │
│  │ (archivos imagen)    │                       │
│  └──────────────────────┘                       │
└─────────────────────────────────────────────────┘
```

## Testing

```bash
./gradlew test    # Tests unitarios (26 tests)
```

## Cómo ejecutar

```bash
./gradlew assembleDebug    # Build
./gradlew installDebug     # Instalar en dispositivo
```

Requiere archivo `google-services.json` de Firebase (no incluido en el repo)  
y `secrets.properties` con las API keys.

---

# Dedalo

Mobile app for construction project management with stages, AI chat and user profile.

University project — **UADE**, Desarrollo de Aplicaciones I  
**Student:** Matias Gabriel Audine

---

## Stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Hilt DI (KSP)
- **Local database:** Room 2.7.1 (offline-first, SSOT)
- **Backend:** Firebase Auth + Firestore (metadata/URLs), Supabase Storage (image files)
- **Geocoding:** LocationIQ (Retrofit)
- **AI Chat:** Gemini API (Retrofit)
- **Maps:** Google Maps via geo intent from project detail
- **Tests:** MockK + Turbine + StandardTestDispatcher

## Features

- [x] Google authentication
- [x] Project CRUD with urban planning data (FOS, FOT)
- [x] Edit project from detail screen
- [x] Project stages with status (PENDING / IN_PROGRESS / FINISHED)
- [x] Change stage status from detail screen
- [x] Images on projects and stages with destination picker
- [x] Image gallery with Supabase Storage upload
- [x] Automatic geocoding on address input (LocationIQ)
- [x] AI chat (Gemini)
- [x] User profile with Instagram-style gallery
- [x] Offline persistence (Room synced with Firestore)
- [x] Hilt DI (migrated from manual DI)
- [x] Open in Google Maps from project detail

## Data Flow

Dedalo follows an **offline-first** architecture where **Room is the single source of truth (SSOT)**.

### General Pipeline

```
Firestore ──(Snapshot Listener)──> Room (SSOT) ──(Flow DAO)──> Repository ──(Flow)──> ViewModel ──(StateFlow)──> UI (collectAsStateWithLifecycle)
```

### Reading data
1. Room exposes `Flow<>` through DAOs (`observeByUserId`, `observeById`, etc.)
2. Repositories combine and map Room entities to domain models
3. ViewModels transform domain models into `UiState` (sealed interface: `Loading | Success | Error`)
4. The UI observes with `collectAsStateWithLifecycle()` and renders based on state
5. **The UI never observes Firestore directly**

### Writing data (write-through)
1. Mutations are sent to Firestore first
2. `FirebaseSyncManager` receives the change via snapshot listener
3. Writes to Room (upsert)
4. Room propagates the change via `Flow` to Repository → ViewModel → UI

### Real-time sync
- `FirebaseSyncManager` (singleton, started in `AppModule`) listens to `AuthStateListener`
- On login, registers listeners on the `projects` collection and `stages` collection group (filtered by `userId`)
- Each `ADDED`/`MODIFIED` → `projectDao.upsert()` or `stageDao.upsertAll()`
- Each `REMOVED` → `projectDao.deleteById()`
- `syncMyProjectsToFirebase()` provides manual local → remote sync from the profile screen

### Image flow
1. User picks an image → uploaded to **Supabase Storage** (bucket `images`)
2. Public URL is obtained → saved to **Firestore** (`images` field as JSON)
3. The snapshot listener persists it in **Room** (as `List<ImageData>` serialized with Gson)
4. Coil loads the image from the Supabase public URL

### AI Chat flow
1. User sends a message → inserted in **Room** (`ChatMessageDao.insert()`)
2. Request sent to **Gemini API** (`GeminiApiService`)
3. Model response is inserted in **Room**
4. Room propagates both messages to the UI via `Flow`

### Authentication flow
1. `AuthViewModel` listens to `FirebaseAuth.AuthStateListener` via `callbackFlow`
2. On Google sign-in → `AuthRepository.signInWithGoogle()` authenticates with Firebase
3. User is persisted in **Room** (`userDao.upsert()` with `isCurrentUser` flag)
4. `FirebaseSyncManager` reacts to auth changes and restarts listeners
5. On sign-out → Room and listeners are cleared

### Geocoding flow (LocationIQ)
1. User types an address on the create/edit project screen
2. `CreateProjectViewModel.queryFlow` applies `debounce(2500)` and `filter { it.length >= 3 }`
3. Calls `LocationiqService` via Retrofit
4. UI state is updated with `lat`, `lon`, `display_name`

### Layer diagram per feature

```
┌─────────────────────────────────────────────────┐
│  UI Layer (Composables)                         │
│  collectAsStateWithLifecycle()                  │
├─────────────────────────────────────────────────┤
│  ViewModel Layer                                │
│  StateFlow<UiState>                             │
├─────────────────────────────────────────────────┤
│  Domain Layer (models, sealed UiState)          │
├─────────────────────────────────────────────────┤
│  Repository Layer                               │
│  Flow<> from DAOs + business logic             │
├─────────────────────────────────────────────────┤
│  Data Layer                                     │
│  ┌──────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ Room     │  │ Firebase     │  │ Retrofit  │ │
│  │ (SSOT)   │  │ Auth/Firest. │  │ (APIs)    │ │
│  └──────────┘  └──────────────┘  └───────────┘ │
│  ┌──────────────────────┐                       │
│  │ Supabase Storage     │                       │
│  │ (image files)        │                       │
│  └──────────────────────┘                       │
└─────────────────────────────────────────────────┘
```

## Testing

```bash
./gradlew test    # Unit tests (26 tests)
```

## How to run

```bash
./gradlew assembleDebug    # Build
./gradlew installDebug     # Install on device
```

Requires `google-services.json` from Firebase (not included in the repo)  
and `secrets.properties` with API keys.
