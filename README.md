# Learning Dashboard

Kotlin Multiplatform + Compose Multiplatform app: Login → Course Dashboard → Course Details with lesson completion and offline support. **Android is the primary deliverable**; the same code also runs on iOS.

**Run:** open the root in Android Studio and run `androidApp`, or open `iosApp/iosApp.xcodeproj` in Xcode. **Tests:** `./gradlew :shared:testAndroidHostTest`. **Demo login:** `demo@learning.com` / `password123`.

Stack: Kotlin 2.4, Compose Multiplatform 1.12, Navigation Compose, Koin, Ktor, Room (KMP), DataStore, coroutines/Flow.

## 1. Architecture
Clean Architecture with MVVM, split by feature (`auth`, `course`), each with `presentation → domain → data` layers:
- **UI (Compose)** renders one immutable `UiState` per screen and only sends events to the ViewModel.
- **ViewModel** combines a repository `Flow` with a `RefreshState` into a sealed state: `Loading / Empty / Error / Content`.
- **Domain** holds the models, the repository interfaces and pure logic (`ProgressCalculator`, `LoginValidator`). Use cases exist only where they add logic (`LogoutUseCase`); I skipped pass-through use cases on purpose.
- **Data** contains repositories plus `Remote` (Ktor) and `Local` (Room) data sources behind interfaces. Errors are mapped once to a domain `AppError`.

Why: each layer can be tested on its own, and the UI never sees transport or database types. Sharing everything except the platform entry points keeps Android and iOS behaviour identical.

## 2. Offline Support
The repository is **offline-first**. The UI only observes Room, and the network only writes into Room. With no internet, the cached list still renders with an "offline" banner. If nothing is cached yet, the app shows an error with Retry.
- Lesson completion is saved locally first, in a single transaction that also recomputes course progress.
- Completion is **monotonic** (a lesson is never un-completed), so a server refresh merges as `local OR remote`. The merge is conflict-free and a refresh never wipes progress the user made offline.
- The API is a Ktor `MockEngine` that checks real device connectivity (`ConnectivityManager` on Android, `NWPathMonitor` on iOS), so airplane mode makes requests fail like a real backend would. Swapping in OkHttp/Darwin is a one-line DI change.

## 3. Security
Today the token sits in plain DataStore, which is acceptable only because it is a mock. In production:
- **Android:** store the token in Keystore-backed encrypted storage (Tink-encrypted DataStore), with `allowBackup=false`.
- **iOS:** store it in the **Keychain** (`kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`).
- Use short-lived access tokens with a refresh token, sent through Ktor's `Auth` bearer plugin.
- Add certificate pinning and enable R8 obfuscation.
- Logout already clears both the token and the cached learner data.

## 4. Scale (1M users, hundreds of courses)
1. **Pagination:** use cursor-based paging for the course list with Paging 3 (Room as the source of truth), plus ETag/`If-None-Match` caching.
2. **Sync completions to the server:** queue completions in an outbox table and send them with idempotent requests from WorkManager / `BGTaskScheduler`, with retry and backoff. Then add delta sync (`updatedSince`).
3. **Observability:** add crash reporting, performance traces, structured logging and analytics on API error rates. Use feature flags for staged rollouts.
4. **Backend friendliness:** batch calls, add a CDN for static course content, use exponential backoff with jitter on the client, and set request timeouts (already configured).
5. **Engineering scale:** move to a multi-module Gradle setup per feature, CI with tests and lint on every PR, baseline profiles, and screenshot tests.

## 5. Second Platform (iOS)
iOS already runs from this codebase: `shared` compiles to an iOS framework, `iosApp` hosts `MainViewController()`, and Koin is started from Swift (`KoinIosKt.doInitKoinIos()`). Platform-specific code is limited to the DB path, the DataStore path and `NWPathMonitor`.

If the team wanted a fully native UI, I would keep `shared` (domain, data, ViewModels) and rebuild only the screens in SwiftUI, observing the `StateFlow`s through SKIE or KMP-NativeCoroutines. A fully native rewrite would follow the same layers with SwiftUI, `@Observable` view models, async/await, URLSession, SwiftData and the Keychain.
