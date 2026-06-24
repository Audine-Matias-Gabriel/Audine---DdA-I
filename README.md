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
- **Backend:** Firebase Auth, Firestore, Storage
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
- [x] Galería de imágenes con carga a Firebase Storage
- [x] Geocoding automático al ingresar dirección (LocationIQ)
- [x] Chat con IA (Gemini)
- [x] Perfil de usuario con galería estilo Instagram
- [x] Persistencia offline (Room sincronizado con Firestore)
- [x] DI con Hilt (migrado desde DI manual)
- [x] Apertura en Google Maps desde detalle de obra

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
- **Backend:** Firebase Auth, Firestore, Storage
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
- [x] Image gallery with Firebase Storage upload
- [x] Automatic geocoding on address input (LocationIQ)
- [x] AI chat (Gemini)
- [x] User profile with Instagram-style gallery
- [x] Offline persistence (Room synced with Firestore)
- [x] Hilt DI (migrated from manual DI)
- [x] Open in Google Maps from project detail

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
