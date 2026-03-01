# UVCCamera

> USB Video Class (UVC) camera library for Android + Flutter plugin

## Project Context

| Attribute | Value |
|-----------|-------|
| **License** | Apache-2.0 |
| **Min SDK** | 21 (Android 5.0) |
| **Compile SDK** | 34 (Android 14) |
| **Java Compatibility** | 11 (build requires JDK 17) |
| **Target ABIs** | armeabi-v7a, arm64-v8a |
| **Gradle** | 8.13, Kotlin DSL, version catalog |
| **Flutter** | >=3.29.2, Dart SDK >=3.7.0 |
| **Distribution** | Maven Central (`org.uvccamera:lib`), pub.dev (`uvccamera`), GitHub Packages |

## Structure

| Path | Purpose |
|------|---------|
| `lib/` | Android library module (`org.uvccamera.lib`) |
| `lib/src/main/java/` | Java API (UVCCamera, USBMonitor, DeviceFilter) |
| `lib/src/main/jni/` | Native C/C++ (libuvc, libusb, libjpeg, UVCCamera JNI) |
| `flutter/` | Flutter plugin (`uvccamera`) |
| `flutter/lib/` | Dart API |
| `flutter/android/` | Flutter Android platform implementation (Java) |
| `flutter/example/` | Flutter example app |
| `usbCameraCommon/` | Shared Android UI utilities for test apps |
| `usbCameraTest*/` | Android test/demo applications |
| `upstreams/` | Git submodules referencing upstream forks |
| `gh-pages/` | GitHub Pages build assets |
| `gradle/libs.versions.toml` | Dependency version catalog |

## Conventions

### Commits

Format: `(type) scope: description`

| Type | Meaning |
|------|---------|
| `fix` | Bug fix |
| `imp` | Improvement/enhancement |
| `chore` | Maintenance (deps, CI, tooling) |
| `docs` | Documentation |

Scope prefix when targeting a specific module: `flutter:`, `ci:`, `lib:`

Cherry-picked commits use trailers:
```
Cherry-picked-from: source/repo@sha (or source/repo#PR)
Co-authored-by: Original Author <email>
```

### Branches

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feat/description` | `feat/flutter/pause-resume-preview` |
| Fix | `fix/description` | `fix/preview-size-comparison` |
| Cherry-pick | `cherry-pick/source-description` | `cherry-pick/hthetiot-fix-rotation` |

### Naming

- Java packages: `com.serenegiant.usb` / `com.serenegiant.utils` (legacy upstream)
- Library namespace: `org.uvccamera.lib`
- Flutter plugin package: `org.uvccamera.flutter`
- Dart files: `uvccamera_*.dart` (snake_case with prefix)
- Gradle modules: camelCase (`usbCameraTest`, `usbCameraCommon`)

### Code Style

Per `.editorconfig`: 4-space indent, 120 char max, LF line endings, UTF-8. YAML uses 2-space indent.

## Development

### Build Android library

```shell
./gradlew :lib:assembleRelease
```

### Publish to local Maven (required before Flutter build)

```shell
./gradlew :lib:publishToMavenLocal
```

### Build Flutter example

```shell
cd flutter/example
flutter build apk
```

### Run full build chain

```shell
./gradlew assembleRelease publishToMavenLocal && cd flutter/example && flutter build apk
```

## Release

Tag-triggered: push a semantic version tag to trigger the release workflow.

```shell
git tag X.Y.Z
git push origin X.Y.Z
```

The CI workflow automatically publishes to Maven Central, pub.dev, and GitHub Packages.

## CI

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `ci.yaml` | Push to main, PRs to main | Build lib, publish snapshots, build GitHub Pages |
| `release.yaml` | Version tag push | Full release: build, sign, publish all artifacts |

Snapshot versions: `main-SNAPSHOT` (main branch), `PR-{N}-SNAPSHOT` (pull requests).

## Native Layer

The native code uses ndk-build (not CMake). Entry point: `lib/src/main/jni/Android.mk`.

Key native libraries:
- **libuvc** — USB Video Class protocol implementation
- **libusb** — USB device access
- **libjpeg** — JPEG encoding/decoding
- **UVCCamera** — JNI bridge and pipeline system

The pipeline architecture (`lib/src/main/jni/UVCCamera/pipeline/`) handles frame processing with buffered, preview, capture, and callback pipelines.

## Fork Context

Hard fork of `saki4510t/UVCCamera`. Upstream submodules in `upstreams/` for reference. Community improvements are cherry-picked with attribution trailers.
