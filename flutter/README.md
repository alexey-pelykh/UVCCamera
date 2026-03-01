# UVC Camera plugin for Flutter

[![GitHub Repo stars](https://img.shields.io/github/stars/alexey-pelykh/UVCCamera?style=flat&logo=github)](https://github.com/alexey-pelykh/UVCCamera)
[![GitHub License](https://img.shields.io/github/license/alexey-pelykh/UVCCamera)](./LICENSE)
[![Pub Version](https://img.shields.io/pub/v/uvccamera)](https://pub.dev/packages/uvccamera)

A USB Video Class (UVC) camera plugin for Flutter based on [UVCCamera](https://uvccamera.org) library for Android,
brought to you by [Alexey Pelykh](https://alexey-pelykh.com).

## Compatibility

**Flutter 3.29.2+ is strongly recommended.** Flutter 3.27–3.28 enabled Impeller by default on Android, which causes
black screens on some devices (MediaTek/PowerVR GPUs). This was fixed in Flutter 3.29.0 with automatic GLES fallback
(and a crash fix in 3.29.2). If you use Flutter 3.27.x–3.28.x and experience a black screen, disable Impeller:

```xml
<meta-data android:name="io.flutter.embedding.android.EnableImpeller" android:value="false" />
```

Flutter 3.24–3.26 users are unaffected (Impeller was not default on Android).

## Usage

The plugin's API is intentionally kept similar to the [camera](https://pub.dev/packages/camera) package. See the
[example](https://pub.dev/packages/uvccamera/example) for an app that uses the plugin.
