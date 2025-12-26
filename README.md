# Albert

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.21-blue.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blue?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.3-blue?logo=ktor)](https://ktor.io/)

Albert is a modern learning application designed to help self-learners and students master complex
technology-related topics. By combining bite-sized lessons with interactive questions and immediate
feedback, Albert ensures an effective learning experience. The core of Albert's effectiveness lies
in its integrated **Spaced Repetition System (SRS)**, which schedules reviews at optimal intervals
to guarantee long-term knowledge retention.

---

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

---

## Project Description

Albert addresses the common challenges of self-studying technical subjects: information overload,
lack of structured review, and inconsistent feedback. It breaks down complex curricula into a
manageable hierarchy:

**Course → Lesson → Lesson Step**

### Key Features

- **Bite-Sized Learning:** Lessons are divided into small, sequential steps that allow for focused
  study.
- **Interactive Feedback:** Immediate evaluation of answers with detailed explanations for every
  question.
- **Spaced Repetition (SRS):** Intelligent scheduling of repeatable elements (1, 2, 4, 8... day
  intervals) to reinforce memory.
- **Progressive Unlocking:** Lessons become available only after the prerequisite material is
  mastered.
- **Cross-Platform Sync:** Your progress is tied to your account and synchronized across Android,
  iOS, Web, and Desktop.

---

## Tech Stack

Albert is built using the latest Kotlin Multiplatform technologies, sharing logic between the
frontend and backend to ensure consistency and development efficiency.

### Core

- **Language:** Kotlin 2.2.21
- **Dependency Injection:** Koin
- **Serialization:** kotlinx.serialization

### Frontend (Compose Multiplatform)

- **UI Framework:** Compose Multiplatform (Android, iOS, Web/Wasm, Desktop)
- **State Management:** Compose Multiplatform ViewModels
- **Authentication:** KMPAuth
- **Local Storage:** SQLDelight
- **Networking:** Ktor Client

### Backend (Ktor)

- **Framework:** Ktor Server
- **Database:** MongoDB (Native Kotlin Driver)

### Testing

- **Unit Testing:** JUnit5 with fakes
- **Frontend:** Compose testing framework
- **Backend:** Ktor test framework with mocked database

---

## Getting Started Locally

### Prerequisites

- **JDK 17 or higher**
- **Android Studio / IntelliJ IDEA**
- **Xcode** (for iOS development)
- **MongoDB** (running locally on default port 27017 or configured via environment)

### Setup Steps

1. **Clone the repository:**

```bash
git clone https://github.com/your-username/albert.git
cd albert
```

2. **Build the project:**

```bash
./gradlew build
```

3. **Run the Backend:**

```bash
./gradlew :server:run
```

4. **Run the Frontend (Choose your platform):**
    - **Desktop:** `./gradlew :composeApp:run`
    - **Android:** Open in Android Studio and run `composeApp`.
    - **Web (Wasm):** `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
    - **iOS:** Open `iosApp/iosApp.xcodeproj` in Xcode.

---

## Available Scripts

The project uses Gradle to manage all build and execution tasks.

| Command                                             | Description                            |
|-----------------------------------------------------|----------------------------------------|
| `./gradlew :server:run`                             | Starts the Ktor backend server         |
| `./gradlew :composeApp:run`                         | Launches the Desktop (JVM) application |
| `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` | Launches the Web application (Wasm)    |
| `./gradlew :composeApp:jsBrowserDevelopmentRun`     | Launches the Web application (JS)      |
| `./gradlew :composeApp:assembleDebug`               | Builds the Android Debug APK           |
| `./gradlew check`                                   | Runs all tests across the project      |
| `./gradlew build`                                   | Full build of the project              |

---

## Project Scope

### MVP Functionalities

- **Secure Authentication:** User login via KMPAuth what allows progress synchronization.
- **Course Catalog:** Browsing the hierarchy of Courses and Lessons.
- **Lesson Flow:** Sequential presentation of lesson elements.
- **Supported Element Types:**
    - **Text:** Informational content with manual completion.
    - **Single-Choice:** Select one correct answer.
    - **Multiple-Choice:** Select one or more correct answers.
    - **Exact Word:** Free-text matching for specific terms.
- **Spaced Repetition System:** Automated scheduling for repeatable items and same-day retries for
  mistakes.