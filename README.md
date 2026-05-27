# Dedalo

Aplicación mobile para gestión de obras de construcción con mapas, etapas y chat IA.

Proyecto universitario — **UADE**, Desarrollo de Aplicaciones I  
**Alumno:** Matias Gabriel Audine

---

## Stack

- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + DI manual (AppContainer)
- **Base de datos local:** Room (offline-first)
- **Backend:** Firebase Auth, Firestore, Storage
- **Mapas:** MapLibre GL
- **Geocoding:** LocationIQ (Retrofit)
- **Chat IA:** Gemini API

## Features

- Autenticación con Google
- CRUD de obras con datos urbanísticos (FOS, FOT)
- Etapas de obra con estados (ESPERA / EN_PROGRESO / FINALIZADA)
- Galería de imágenes con carga a Firebase Storage
- Geocoding automático al ingresar dirección
- Mapa interactivo con ubicación de obras
- Chat con IA (Gemini)
- Perfil de usuario
- Persistencia offline (Room sincronizado con Firestore)

## Cómo ejecutar

```bash
./gradlew assembleDebug    # Build
./gradlew installDebug     # Instalar en dispositivo
```

Requiere archivo `google-services.json` de Firebase (no incluido en el repo).

---

# Dedalo

Mobile app for construction project management with maps, stages, and AI chat.

University project — **UADE**, Desarrollo de Aplicaciones I  
**Student:** Matias Gabriel Audine

---

## Stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + manual DI (AppContainer)
- **Local database:** Room (offline-first)
- **Backend:** Firebase Auth, Firestore, Storage
- **Maps:** MapLibre GL
- **Geocoding:** LocationIQ (Retrofit)
- **AI Chat:** Gemini API

## Features

- Google authentication
- Project CRUD with urban planning data (FOS, FOT)
- Project stages with status (PENDING / IN_PROGRESS / FINISHED)
- Image gallery with Firebase Storage upload
- Automatic geocoding on address input
- Interactive map with project locations
- AI chat (Gemini)
- User profile
- Offline persistence (Room synced with Firestore)

## How to run

```bash
./gradlew assembleDebug    # Build
./gradlew installDebug     # Install on device
```

Requires `google-services.json` from Firebase (not included in the repo).
