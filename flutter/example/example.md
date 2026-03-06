# [UVCCamera](https://uvccamera.org) Example

The example demonstrates how to use the [UVCCamera](https://uvccamera.org) plugin in a Flutter app.

## [pubspec.yaml](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/pubspec.yaml)
```yaml
name: uvccamera_example
description: "Demonstrates how to use the uvccamera plugin."
publish_to: none

environment:
  sdk: ^3.7.0

dependencies:
  flutter:
    sdk: flutter
  uvccamera: 0.0.13
  cross_file: ^0.3.4+2
  cupertino_icons: ^1.0.8
  permission_handler: ^11.3.1

dev_dependencies:
  integration_test:
    sdk: flutter
  flutter_test:
    sdk: flutter
  flutter_lints: ^5.0.0

flutter:
  uses-material-design: true
```

## [lib/main.dart](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/lib/main.dart)
```dart
import 'package:flutter/material.dart';

import 'uvccamera_demo_app.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const UvcCameraDemoApp());
}
```

## [lib/uvccamera_demo_app.dart](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/lib/uvccamera_demo_app.dart)
```dart
import 'package:flutter/material.dart';

import 'uvccamera_devices_screen.dart';

class UvcCameraDemoApp extends StatefulWidget {
  const UvcCameraDemoApp({super.key});

  @override
  State<UvcCameraDemoApp> createState() => _UvcCameraDemoAppState();
}

class _UvcCameraDemoAppState extends State<UvcCameraDemoApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'UVC Camera Example',
      home: Scaffold(appBar: AppBar(title: const Text('UVC Camera Example')), body: UvcCameraDevicesScreen()),
    );
  }
}
```

## [lib/uvccamera_device_screen.dart](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/lib/uvccamera_device_screen.dart)
```dart
import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

import 'uvccamera_widget.dart';

class UvcCameraDeviceScreen extends StatelessWidget {
  final UvcCameraDevice device;

  const UvcCameraDeviceScreen({super.key, required this.device});

  @override
  Widget build(BuildContext context) {
    return Scaffold(appBar: AppBar(title: Text(device.name)), body: Center(child: UvcCameraWidget(device: device)));
  }
}
```

## [lib/uvccamera_devices_screen.dart](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/lib/uvccamera_devices_screen.dart)
```dart
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

import 'uvccamera_device_screen.dart';

class UvcCameraDevicesScreen extends StatefulWidget {
  const UvcCameraDevicesScreen({super.key});

  @override
  State<UvcCameraDevicesScreen> createState() => _UvcCameraDevicesScreenState();
}

class _UvcCameraDevicesScreenState extends State<UvcCameraDevicesScreen> {
  bool _isSupported = false;
  StreamSubscription<UvcCameraDeviceEvent>? _deviceEventSubscription;
  final Map<String, UvcCameraDevice> _devices = {};

  @override
  void initState() {
    super.initState();

    UvcCamera.isSupported().then((value) {
      setState(() {
        _isSupported = value;
      });
    });

    _deviceEventSubscription = UvcCamera.deviceEventStream.listen((event) {
      setState(() {
        if (event.type == UvcCameraDeviceEventType.attached) {
          _devices[event.device.name] = event.device;
        } else if (event.type == UvcCameraDeviceEventType.detached) {
          _devices.remove(event.device.name);
        }
      });
    });

    UvcCamera.getDevices().then((devices) {
      setState(() {
        _devices.addAll(devices);
      });
    });
  }

  @override
  void dispose() {
    _deviceEventSubscription?.cancel();
    _deviceEventSubscription = null;

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isSupported) {
      return const Center(child: Text('UVC Camera is not supported on this device.', style: TextStyle(fontSize: 18)));
    }

    if (_devices.isEmpty) {
      return const Center(child: Text('No UVC devices connected.', style: TextStyle(fontSize: 18)));
    }

    return ListView(
      children:
          _devices.values.map((device) {
            return ListTile(
              leading: const Icon(Icons.videocam),
              title: Text(device.name),
              subtitle: Text('Vendor ID: ${device.vendorId}, Product ID: ${device.productId}'),
              onTap: () {
                Navigator.push(context, MaterialPageRoute(builder: (context) => UvcCameraDeviceScreen(device: device)));
              },
            );
          }).toList(),
    );
  }
}
```

## [lib/uvccamera_widget.dart](https://github.com/alexey-pelykh/UVCCamera/blob/main/flutter/example/lib/uvccamera_widget.dart)
```dart
import 'dart:async';

import 'package:cross_file/cross_file.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:uvccamera/uvccamera.dart';

class UvcCameraWidget extends StatefulWidget {
  final UvcCameraDevice device;

  const UvcCameraWidget({super.key, required this.device});

  @override
  State<UvcCameraWidget> createState() => _UvcCameraWidgetState();
}

class _UvcCameraWidgetState extends State<UvcCameraWidget> with WidgetsBindingObserver {
  bool _isAttached = false;
  bool _hasDevicePermission = false;
  bool _hasCameraPermission = false;
  bool _isDeviceAttached = false;
  bool _isDeviceConnected = false;
  UvcCameraController? _cameraController;
  Future<void>? _cameraControllerInitializeFuture;
  StreamSubscription<UvcCameraErrorEvent>? _errorEventSubscription;
  StreamSubscription<UvcCameraStatusEvent>? _statusEventSubscription;
  StreamSubscription<UvcCameraButtonEvent>? _buttonEventSubscription;
  StreamSubscription<UvcCameraDeviceEvent>? _deviceEventSubscription;
  String _log = '';

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addObserver(this);

    _attach();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);

    _detach(force: true);

    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _attach();
    } else if (state == AppLifecycleState.paused) {
      _detach();
    }
  }

  void _attach({bool force = false}) {
    if (_isAttached && !force) {
      return;
    }

    UvcCamera.getDevices().then((devices) {
      if (!devices.containsKey(widget.device.name)) {
        return;
      }

      setState(() {
        _isDeviceAttached = true;
      });

      _requestPermissions();
    });

    _deviceEventSubscription = UvcCamera.deviceEventStream.listen((event) {
      if (event.device.name != widget.device.name) {
        return;
      }

      if (event.type == UvcCameraDeviceEventType.attached && !_isDeviceAttached) {
        // NOTE: Requesting UVC device permission will trigger connection request
        _requestPermissions();
      }

      setState(() {
        if (event.type == UvcCameraDeviceEventType.attached) {
          // _hasCameraPermission - maybe
          // _hasDevicePermission - maybe
          _isDeviceAttached = true;
          _isDeviceConnected = false;
        } else if (event.type == UvcCameraDeviceEventType.detached) {
          _hasCameraPermission = false;
          _hasDevicePermission = false;
          _isDeviceAttached = false;
          _isDeviceConnected = false;
        } else if (event.type == UvcCameraDeviceEventType.connected) {
          _hasCameraPermission = true;
          _hasDevicePermission = true;
          _isDeviceAttached = true;
          _isDeviceConnected = true;

          _log = '';

          _cameraController = UvcCameraController(device: widget.device);
          _cameraControllerInitializeFuture = _cameraController!.initialize().then((_) async {
            _errorEventSubscription = _cameraController!.cameraErrorEvents.listen((event) {
              setState(() {
                _log = 'error: ${event.error}\n$_log';
              });

              if (event.error.type == UvcCameraErrorType.previewInterrupted) {
                _detach();
                _attach();
              }
            });

            _statusEventSubscription = _cameraController!.cameraStatusEvents.listen((event) {
              setState(() {
                _log = 'status: ${event.payload}\n$_log';
              });
            });

            _buttonEventSubscription = _cameraController!.cameraButtonEvents.listen((event) {
              setState(() {
                _log = 'btn(${event.button}): ${event.state}\n$_log';
              });
            });
          });
        } else if (event.type == UvcCameraDeviceEventType.disconnected) {
          _hasCameraPermission = false;
          _hasDevicePermission = false;
          // _isDeviceAttached - maybe?
          _isDeviceConnected = false;

          _buttonEventSubscription?.cancel();
          _buttonEventSubscription = null;

          _statusEventSubscription?.cancel();
          _statusEventSubscription = null;

          _errorEventSubscription?.cancel();
          _errorEventSubscription = null;

          _cameraController?.dispose();
          _cameraController = null;
          _cameraControllerInitializeFuture = null;

          _log = '';
        }
      });
    });

    _isAttached = true;
  }

  void _detach({bool force = false}) {
    if (!_isAttached && !force) {
      return;
    }

    _hasDevicePermission = false;
    _hasCameraPermission = false;
    _isDeviceAttached = false;
    _isDeviceConnected = false;

    _buttonEventSubscription?.cancel();
    _buttonEventSubscription = null;

    _statusEventSubscription?.cancel();
    _statusEventSubscription = null;

    _cameraController?.dispose();
    _cameraController = null;
    _cameraControllerInitializeFuture = null;

    _deviceEventSubscription?.cancel();
    _deviceEventSubscription = null;

    _isAttached = false;
  }

  Future<void> _requestPermissions() async {
    final hasCameraPermission = await _requestCameraPermission().then((value) {
      setState(() {
        _hasCameraPermission = value;
      });

      return value;
    });

    // NOTE: Requesting UVC device permission can be made only after camera permission is granted
    if (!hasCameraPermission) {
      return;
    }

    _requestDevicePermission().then((value) {
      setState(() {
        _hasDevicePermission = value;
      });

      return value;
    });
  }

  Future<bool> _requestDevicePermission() async {
    final devicePermissionStatus = await UvcCamera.requestDevicePermission(widget.device);
    return devicePermissionStatus;
  }

  Future<bool> _requestCameraPermission() async {
    var cameraPermissionStatus = await Permission.camera.status;
    if (cameraPermissionStatus.isGranted) {
      return true;
    } else if (cameraPermissionStatus.isDenied || cameraPermissionStatus.isRestricted) {
      cameraPermissionStatus = await Permission.camera.request();
      return cameraPermissionStatus.isGranted;
    } else {
      // NOTE: Permission is permanently denied
      return false;
    }
  }

  Future<void> _startVideoRecording(UvcCameraMode videoRecordingMode) async {
    await _cameraController!.startVideoRecording(videoRecordingMode);
  }

  Future<void> _takePicture() async {
    final XFile outputFile = await _cameraController!.takePicture();

    outputFile.length().then((length) {
      setState(() {
        _log = 'image file: ${outputFile.path} ($length bytes)\n$_log';
      });
    });
  }

  Future<void> _stopVideoRecording() async {
    final XFile outputFile = await _cameraController!.stopVideoRecording();

    outputFile.length().then((length) {
      setState(() {
        _log = 'video file: ${outputFile.path} ($length bytes)\n$_log';
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!_isDeviceAttached) {
      return Center(child: Text('Device is not attached', style: TextStyle(fontSize: 18)));
    }

    if (!_hasCameraPermission) {
      return Center(child: Text('Camera permission is not granted', style: TextStyle(fontSize: 18)));
    }

    if (!_hasDevicePermission) {
      return Center(child: Text('Device permission is not granted', style: TextStyle(fontSize: 18)));
    }

    if (!_isDeviceConnected) {
      return Center(child: Text('Device is not connected', style: TextStyle(fontSize: 18)));
    }

    return FutureBuilder<void>(
      future: _cameraControllerInitializeFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return Stack(
            children: [
              Align(
                alignment: Alignment.topCenter,
                child: UvcCameraPreview(
                  _cameraController!,
                  child: Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: SingleChildScrollView(
                      child: SelectableText(
                        _log,
                        style: TextStyle(color: Colors.red, fontFamily: 'Courier', fontSize: 10.0),
                      ),
                    ),
                  ),
                ),
              ),
              Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 50.0),
                  child: ValueListenableBuilder<UvcCameraControllerState>(
                    valueListenable: _cameraController!,
                    builder: (context, value, child) {
                      return Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          FloatingActionButton(
                            backgroundColor: Colors.white,
                            onPressed:
                                value.isTakingPicture
                                    ? null
                                    : () async {
                                      await _takePicture();
                                    },
                            child: Icon(Icons.camera_alt, color: Colors.black),
                          ),
                          FloatingActionButton(
                            backgroundColor: value.isRecordingVideo ? Colors.red : Colors.white,
                            onPressed: () async {
                              if (value.isRecordingVideo) {
                                await _stopVideoRecording();
                              } else {
                                await _startVideoRecording(value.previewMode!);
                              }
                            },
                            child: Icon(
                              value.isRecordingVideo ? Icons.stop : Icons.videocam,
                              color: value.isRecordingVideo ? Colors.white : Colors.black,
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                ),
              ),
            ],
          );
        } else {
          return const Center(child: CircularProgressIndicator());
        }
      },
    );
  }
}
```
