# kmp-shopping-route-planner
A PoC multiplatform route planner app for ideal traverse of a shop, based on a shopping list

---

This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - `commonMain` is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Local service ports

Copy `.env.example` to `.env`, then load it into the environment before launching the desktop app:

```sh
set -a
source .env
set +a
./gradlew :composeApp:run
```

- `SHOPMAP_BACKEND_PORT` sets the backend API port. The default is `8082`.
- `SHOPMAP_OAUTH_CALLBACK_PORT` sets the local desktop Google OAuth callback port. The default is `8083`. It must be free, and the matching `http://localhost:<port>/callback` URI must be registered on the Google OAuth client.

The `.env` file is not loaded automatically by the app. For Android and iOS, set these in the app process environment through the platform's launch or build configuration. Invalid port values fail with a configuration error.
