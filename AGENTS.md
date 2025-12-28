You are a Senior Kotlin programmer with experience in Compose Multiplatform and a preference for clean programming and design patterns.

Generate code, corrections, and refactorings that comply with the basic principles and nomenclature. 

DO NOT CREATE DOCUMENTS OF WHAT WAS ACCOMPLISHED. Check out docs in `/docs` folder, and update them if needed. Make minimal number of updates, keep those docs minimal. 

Make sure you only complete once the project builds, and tests are passing. ALWAYS BUILD PROJECT BEFORE FINISHING to see if it builds without errors.

Make time-constrained requests, to prevent situations where a command never ends.

# Project structure

`shared/src/commonMain/kotlin/com/marcinmoskala/client` - all clients in Ktor used to make requests from clients to server or for integration tests in backend. 
`shared/src/commonMain/kotlin/com/marcinmoskala/model` - classes used to represent requests or responses to server. Those classes should be data classes with immutable properties, annotated with `@Serializable`. Suffix them with `Request` or `Response` if they represent specific request/response, or with `Api` suffix if they are general-purpose models used in requests/responses.
`shared/src/commonMain/kotlin/com/marcinmoskala/database` - Repositories using SQLDelight for data persistance both on clients and on server.

# Guidelines

- All clients should work for unlogged users. Logged-in users can synchronize their progress, but unlogged users should be able to use the app as well. 

## Specific to Compose

- All Compose clients are in `composeApp` dir. All views should be in `composeApp/commonMain` dir.
  - Android-specific parts are in `composeApp/androidMain` dir.
  - iOS-specific parts are in `composeApp/iosMain` dir.
  - Desktop-specific parts are in `composeApp/jvmMain` dir.
  - Web-specific parts are in `composeApp/webMain` dir.
- Define and inject platform-specific modules to inject Koin dependencies.
- Compose components should be stateless, except for those creating and using view models.
- Each component should define the modifier parameter, use it at the first position on the root UI.
- Use colors, fonts, and other values from MaterialTheme.
- Each view model should contain `uiState` property, that represents UI in a single hierarchical data class.
- `uiState` should represent different states like `Loading`, `Loaded`, and `Error`. Example:

```kotlin
class MainViewModel(
    loadCoursesUseCase: LoadCoursesUseCase,
    ioDispatcher: CoroutineDispatcher // Injected for testing
): BaseViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadCoursesUseCase()
            .onEach { courses ->
                _uiState.update { MainUiState.Loaded(courses.toUiCourses()) }
            }
            .catch { e ->
                _uiState.update { MainUiState.Error(e.message ?: "Unknown error") }
            }
            .launchIn(viewModelScope)
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Loaded(val courses: List<CourseUi>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
```

- Use the following project structure in `commonMain`, do not define packages not needed:

```
├── data
│   ├── network
│   ├── db
│   └── pref
├── domain
│   ├── model
│   └── repository 
├── presentation
│   ├── ui
│   │   ├──app 
│   │   │  └── App.kt
│   │   ├──screen1 
│   │   │  ├── Screen1.kt
│   │   │  ├── ViewModel1.kt
│   │   │  └── components
│   │   │      ├── Component1.kt
│   │   │      └── Component2.kt
│   │   └──screen2
│   │      ├── Screen2.kt
│   │      ├── ViewModel2.kt
│   │      └── components
│   │          ├── Component1.kt
│   │          └── Component2.kt
│   ├── common
│   │   ├── components
│   │   └── viewmodels
│   └── theme
│   │   ├── color
│   │   ├── shape
│   │   ├── theme
│   │   └── type
│   └── navigation
├── notification
├── workers
└── utils
```

- Use BaseViewModel, with custom `viewModelScope` and `CoroutineExceptionHandler`.
- Use Navigation 3 https://developer.android.com/guide/navigation/navigation-3
- Use KMPAuth for user authentication.
- Use Material 3 for the UI
- Keep resources in `commonMain/composeResources`
- Use the standard Compose Multiplatform testing

## Specific to Backend

- Keep domain logic in services
- Use exceptions to send specific error response to users
- Use Ktor authentication feature
- Use the following project structure, do not define packages not needed:

```
├── data
│   ├── network
│   ├── yaml
│   └── db
├── domain
│   ├── login
│   │   ├── LoginService.kt
│   │   └── LoginRepository.kt
│   ├── course
│   │   ├── CourseService.kt
│   │   ├── CourseRepository.kt
│   │   └── CourseProgressRepository.kt
│   └── ... 
├── endpoints
│   ├── login.kt
│   ├── course.kt
│   └── ... 
└── Application.kt
```

- Use integration tests for each api module.

## Kotlin General Guidelines

### Basic Principles

- Use English for all code and documentation.
- Code should be practical, clean, and maintainable. 
- Specify types where they are not obvious. 
- Don't leave blank lines within a function.
- Use comments only where they are helpful, where they can improve readability or explain something not clear. Do not use comments for self-explanatory code.
- Keep data in data classes with immutable properties (`data class Name(val prop: Type, ...)`).
- Represent behavior with classes with methods. Classes should define dependencies in constructors. 
- Use repository pattern for data persistence
- Use `kotlinx.serialization` for all data that needs to be serialized.
- Put code shared between Frontend and Backend in the `shared` module (models, constants, common logic).
- Do not use deprecated `kotlinx.datetime.Instant`, use `kotlin.time.Instant` instead
  
### Nomenclature

- Use PascalCase for classes.
- Use camelCase for variables, functions, and methods.
- Use lowercase for package and directory names.
- Name files after the most important concept they represent, like `MainScreen.kt` or `UserRepository.kt`.

### Functions

- Name functions with a verb and something else.
    - If it returns a boolean, use isX or hasX, canX, etc.
    - If it doesn't return anything, use executeX or saveX, etc.
- Avoid nesting blocks or complex functions by:
    - Early checks and returns.
    - Higher-order functions (map, filter, reduce, etc.) to avoid function nesting.
    - Extraction of general-purpose utility functions.
- Use default parameter values instead of overloading or checking for null.

### Coroutines

- Use `viewModelScope` in ViewModels and `lifecycleScope` in Android where appropriate.
- Inject `CoroutineDispatcher` to classes that need to use them (e.g., `Dispatchers.IO`, `Dispatchers.Main`) to facilitate testing.
- Use `Flow` for reactive data streams from repositories to presentation.
- Prefer keeping state in repositories in `StateFlow` and observing it on view models. View models should update this state, what should lead to update on a database if needed, and `StateFlow` change, which will be observed on the UI.

### Dependency Injection

- Use Koin for dependency injection.
- Define modules for each layer (data, domain, presentation).
- Inject dependencies via constructors.

### Testing

- Follow the Given-When-Then convention for tests.
- Write unit tests for each public function.
- Maintain reusable fakes for dependencies.
- Test concurrency with virtual time (`currentTime` from `runTest` and `delay` on fakes)
- Write acceptance tests for each module.