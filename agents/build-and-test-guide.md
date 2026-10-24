# Build and Test Guide

## Before building
1. Run `git status --short --branch` and inspect diffs. This checkout already contained user modifications across many Kotlin files during guide creation; preserve them.
2. Verify Java with `java -version`. Existing GitHub workflows use Temurin JDK 21; Java 8 is insufficient for the current toolchain.
3. Use the checked-in Gradle wrapper. Do not upgrade Gradle, Kotlin, AGP or dependencies just to work around a source error.
4. Discover tasks first: Windows `.gradlew.bat tasks --all`; Unix `./gradlew tasks --all`.

## Suggested validation ladder
Run from repository root; confirm task availability before running:
1. Formatting/lint check: `.gradlew.bat spotlessCheck` (if the task is available).
2. Wasm compiler diagnostics: `.gradlew.bat :composeApp:compileKotlinWasmJs` (confirm exact task name with `tasks --all`).
3. Browser distribution: `.gradlew.bat :composeApp:wasmJsBrowserDistribution`.
4. Desktop unit tests: `.gradlew.bat :composeApp:desktopTest`.
5. Android FOSS debug compile/package: discover the available variant task; do not assume a flavor/build type exists without checking `:androidApp` configuration.
6. Run targeted common tests and screenshot tests only when relevant and when baseline screenshots are protected.

## Existing CI behavior
- `.github/workflows/test.yml` runs on pushes and pull requests to `main`.
- It runs `spotlessApply`, screenshot updates, WebP conversion and desktop tests. If generated changes exist, it can commit and push them for non-fork runs.
- `.github/workflows/apk.yml` supports `workflow_dispatch` and `workflow_call`, uses Java 21 and builds `:androidApp:assembleFossRelease`.
- CI behavior is not automatically read-only. Do not trigger workflows or push changes without the user's explicit authorization.

## Error triage
- Fix the first meaningful compiler error first; later errors may be cascades.
- Separate Kotlin syntax/brace errors from unresolved imports, wrong source set, API signature changes, missing resources, dependency resolution and JDK/Gradle incompatibility.
- Save the complete command, exit code and relevant error context. Never report success based on a task starting or partial output.
- Avoid `clean` until incremental build/cache issues are demonstrated; it can cost time and erase useful diagnostic state.
