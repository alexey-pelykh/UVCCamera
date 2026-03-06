# Real-Time Image Stream Feature

## Overview

Fitur ini menambahkan kemampuan untuk mendapatkan data image secara real-time dari kamera UVC dalam bentuk stream. Fitur ini memungkinkan developer untuk melakukan image processing, analisis real-time, atau menyimpan frame individual dari stream kamera.

## Komponen Utama

### 1. UvcCameraFrameEvent

Class yang merepresentasikan event frame dari kamera UVC yang berisi data image.

```dart
class UvcCameraFrameEvent {
  final Uint8List imageData;     // Raw image data dalam bytes
  final int width;               // Lebar image dalam pixels
  final int height;              // Tinggi image dalam pixels  
  final int timestamp;           // Timestamp capture (milliseconds since epoch)
  final String format;           // Format image (yuv420, nv21, jpeg, dll)
}
```

### 2. Stream API

Tambahan method di `UvcCameraController`:

```dart
// Mendapatkan stream frame events
Stream<UvcCameraFrameEvent> get cameraFrameEvents
```

## Cara Penggunaan

### 1. Basic Usage

```dart
// Initialize camera controller
final controller = UvcCameraController(device: device);
await controller.initialize();

// Listen to frame events
StreamSubscription<UvcCameraFrameEvent>? subscription;
subscription = controller.cameraFrameEvents.listen((frameEvent) {
  // Process image data
  processImageData(
    frameEvent.imageData,
    frameEvent.width,
    frameEvent.height,
    frameEvent.format,
  );
});

// Stop listening
subscription?.cancel();
```

### 2. Example Implementation

Lihat `example/lib/uvccamera_image_stream_demo.dart` untuk implementasi lengkap yang mendemonstrasikan:

- Setup camera controller
- Start/stop image streaming
- Menampilkan informasi frame real-time
- Calculating FPS
- Example image processing

### 3. Image Processing

```dart
void processImageData(Uint8List imageData, int width, int height, String format) {
  // Example processing:
  
  // 1. Convert to different formats
  // 2. Apply filters
  // 3. Save to file
  // 4. Send to ML model
  // 5. Analyze image content
  // dll.
}
```

## Platform Support

### Android

- Menggunakan EventChannel `uvccamera/frame_events_{cameraId}`
- Raw image data dikirim melalui platform channel
- Format yang didukung: YUV420, NV21, MJPEG

### iOS

- Belum diimplementasikan (future work)

## Performance Considerations

1. **Memory Usage**: Frame data bisa cukup besar (terutama untuk resolusi tinggi), pastikan untuk memproses dan dispose data dengan tepat
2. **Processing Time**: Hindari operasi berat di dalam stream listener untuk mencegah blocking UI
3. **Frame Rate**: Stream rate tergantung pada capability kamera dan processing speed

## Example App

Untuk melihat demo:

1. Buka example app
2. Pilih device kamera
3. Tap icon kamera di AppBar untuk membuka "Image Stream Demo"
4. Tap "Start Stream" untuk memulai streaming
5. Lihat informasi frame real-time dan FPS

## Architecture

```
UvcCameraController
    ↓
UvcCameraPlatformInterface  
    ↓
UvcCameraPlatform (Android)
    ↓  
EventChannel (uvccamera/frame_events_{id})
    ↓
Native Android Code (UVCCamera library)
```

## Future Enhancements

1. **iOS Support**: Implementasi untuk platform iOS
2. **Frame Format Options**: Pilihan format output (RGB, grayscale, dll)
3. **Resolution Control**: Kontrol resolusi stream terpisah dari preview
4. **Frame Rate Control**: Kontrol frame rate streaming
5. **Buffer Management**: Advanced buffering untuk high-performance scenarios

## Breaking Changes

Tidak ada breaking changes - fitur ini adalah penambahan baru yang backward compatible.
