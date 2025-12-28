# Tech Stack

- Kotlin 2.2.21 used as the main language
- Multiplatform Kotlin project, with shared part between Frontend and Backend in `shared` module
- Ktor Client for network requests
- kotlinx.serialization for serialization/deserialization
- Dependency Injection with Koin

# Frontend - Compose Multiplatform

- Compose Multiplatform, supporting Android, iOS, Web in Kotlin/Wasm, with all views in `composeApp\src\commonMain\kotlin` and only platform-specific starters and definitions in platform modules. 
- ViewModels from Compose Multiplatform for state management
- KMPAuth for authentication
- SQLDelight for local storage

# Backend - Ktor

- Ktor framework for building the server
- SQLDelight for local storage

# Testing
 
- Unit test services with JUnit5 and fakes
- Frontend: Compose testing
- Backend: Ktor test framework for endpoint testing, with mocked database
- Multiplatform: Tests run on JVM, Android, iOS, and JS (browser and Node.js) platforms
- JS browser tests use Karma test runner with headless Chrome

