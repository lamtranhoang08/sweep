# Sweep

A lightweight, native Android file manager — browse files and folders, view media, monitor storage usage, and clean up space, all in a small, focused app built entirely with modern Android tooling.

Sweep is a personal portfolio project inspired by the UX of apps like *Files by Google*, deliberately scoped down to a size one developer can design, build, and ship end-to-end while demonstrating real architectural decisions rather than following a tutorial. It is not affiliated with or endorsed by Google.

---

## Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Permissions](#permissions)
- [Roadmap](#roadmap)
- [Engineering Notes](#engineering-notes)
- [License](#license)

---

## Features

**Implemented**
- [x] Runtime permission handling for media access (images, video, audio)
- [x] Bottom-navigation app shell with four sections: Browse, Gallery, Storage, Cleanup
- [x] Browse screen with real-time device media listing
- [x] Folder browsing via the Storage Access Framework (SAF), with persistent folder access and recursive navigation into subfolders

**In progress / planned**
- [ ] Storage overview — visual breakdown of space used by category (images, video, audio, documents)
- [ ] Media gallery with thumbnail grid and full-screen viewer
- [ ] Cleanup tools — largest files finder and duplicate file detection
- [ ] File actions — rename, move, delete, and share from a shared bottom sheet
- [ ] Search across files and folders
- [ ] Polished empty/error states, app icon, and release build

See [Roadmap](#roadmap) for the day-by-day build plan this project follows.

---

## Screenshots

*Screenshots will be added here as each screen is completed.*

| Browse | Storage | Gallery | Cleanup |
|---|---|---|---|
| _coming soon_ | _coming soon_ | _coming soon_ | _coming soon_ |

---

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture:** MVVM (Model–View–ViewModel)
- **Async / reactive state:** Kotlin Coroutines & Flow
- **Navigation:** Jetpack Navigation for Compose
- **File access:**
  - [`MediaStore`](https://developer.android.com/training/data-storage/shared/media) for indexed media (images, video, audio)
  - [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files) (`DocumentFile`) for general folder browsing
- **Dependency injection:** manual constructor injection (no DI framework yet, by design — see [Engineering Notes](#engineering-notes))

---

## Architecture

Sweep follows MVVM with a strict one-directional dependency rule: each layer depends only on the layer directly beneath it.

```
┌─────────────────────────────────────────┐
│                UI Layer                  │
│     Jetpack Compose screens & nav        │
└───────────────────┬───────────────────────┘
                    │
┌───────────────────▼───────────────────────┐
│              ViewModel Layer               │
│   Holds UI state, exposes StateFlow        │
└───────────────────┬───────────────────────┘
                    │
┌───────────────────▼───────────────────────┐
│             Repository Layer               │
│  Combines & exposes data as Flow           │
└──────────┬────────────────────┬────────────┘
           │                    │
┌──────────▼──────────┐ ┌──────▼───────────┐
│     MediaStore        │ │   File system    │
│   (media queries)      │ │  (SAF browsing) │
└────────────────────────┘ └─────────────────┘
```

**Why this shape:** a `Screen` composable never talks to `MediaStore` or the file system directly — it only observes a `ViewModel`'s `StateFlow`. A `ViewModel` never queries data directly — it only calls a `FileRepository` interface. This means the underlying data source can change, or be replaced with a fake for testing, without touching the UI at all (dependency inversion).

---

## Project Structure

```
app/src/main/java/com/example/sweep/

MainActivity.kt          entry point; hosts the permission gate and app shell
PermissionRequester.kt   reusable runtime permission request composable
Screen.kt                navigation route + bottom tab definitions
AppShell.kt              Scaffold, bottom navigation, and NavHost

data/
    FileItem.kt              domain model shared across all screens
    FileRepository.kt        repository interface (the contract)
    FileRepositoryImpl.kt    combines data sources behind the interface
    MediaStoreDataSource.kt  raw MediaStore queries
    FileSystemDataSource.kt  raw SAF / DocumentFile browsing

screens/
    BrowseScreen.kt + BrowseViewModel.kt
    GalleryScreen.kt + GalleryViewModel.kt
    StorageScreen.kt + StorageViewModel.kt
    CleanupScreen.kt + CleanupViewModel.kt

ui/theme/                Compose theme (colors, typography)
```

A file's folder tells you its role: `data/` never imports Compose, `screens/` never queries data sources directly, and a screen's ViewModel always lives beside its screen.

---

## Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (recent stable version)
- Android SDK with API level 33+ installed
- An emulator or physical device running Android 13 (API 33) or newer

### Setup
```bash
git clone https://github.com/lamtranhoang08/sweep.git
cd sweep
```

1. Open the project in Android Studio.
2. Let Gradle sync automatically (first sync may take a few minutes).
3. Run the app on an emulator or connected device via the Run button.
4. Grant media permissions when prompted, or use the "Pick a folder to browse" button to grant access to a specific folder via the system picker.

No API keys, backend services, or additional configuration are required — Sweep is fully local and works entirely offline.

---

## Permissions

Sweep requests the minimum permissions needed for each feature, rather than broad storage access:

| Permission | Why it's needed |
|---|---|
| `READ_MEDIA_IMAGES` | List and display images via MediaStore |
| `READ_MEDIA_VIDEO` | List and display videos via MediaStore |
| `READ_MEDIA_AUDIO` | List and display audio files via MediaStore |
| Storage Access Framework (no manifest permission) | Browse a specific folder the user explicitly picks |

Sweep deliberately does **not** request `MANAGE_EXTERNAL_STORAGE` (all-files access), since it isn't necessary for the app's scope and would require additional Play Store justification for a real release.

---

## Roadmap

This project is being built on a 7-day milestone plan:

| Day | Focus |
|---|---|
| 1 | Project setup, permissions, navigation shell |
| 2 | File repository layer, real Browse screen |
| 3 | Storage overview screen |
| 4 | Media gallery + viewer |
| 5 | Cleanup tools (large files, duplicates) |
| 6 | File actions (rename/move/delete/share) + search |
| 7 | Polish, documentation, and release |

---

## Engineering Notes

A few deliberate decisions worth calling out:

- **Manual DI before Hilt:** dependencies are currently passed through constructors by hand rather than via a DI framework. This keeps the object graph easy to reason about while the codebase is small; introducing Hilt once the graph grows is a natural next step.
- **Scoped storage, not legacy file access:** rather than requesting broad storage permissions, Sweep uses MediaStore for indexed media and the Storage Access Framework for general folders — the approach Android expects modern apps to use.
- **No system cache cleanup:** unlike apps with system-level privileges, Sweep's cleanup tools focus on large-file and duplicate-file detection rather than clearing app caches, which requires privileged access outside the scope of a standard app.

---

## License

This project is available under the [MIT License](LICENSE).
