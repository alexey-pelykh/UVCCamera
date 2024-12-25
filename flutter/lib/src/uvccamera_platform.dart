import 'package:flutter/services.dart';
import 'package:cross_file/cross_file.dart';

import 'uvccamera_button_event.dart';
import 'uvccamera_device.dart';
import 'uvccamera_device_event.dart';
import 'uvccamera_error_event.dart';
import 'uvccamera_mode.dart';
import 'uvccamera_platform_interface.dart';
import 'uvccamera_resolution_preset.dart';
import 'uvccamera_status_event.dart';

class UvcCameraPlatform extends UvcCameraPlatformInterface {
  final _nativeMethodChannel = const MethodChannel('uvccamera/native');

  final EventChannel _deviceEventChannel = EventChannel('uvccamera/device_events');
  Stream<UvcCameraDeviceEvent>? _deviceEventStream;

  final Map<int, EventChannel> _errorEventChannels = {};
  final Map<int, Stream<UvcCameraErrorEvent>> _errorEventStreams = {};

  final Map<int, EventChannel> _statusEventChannels = {};
  final Map<int, Stream<UvcCameraStatusEvent>> _statusEventStreams = {};

  final Map<int, EventChannel> _buttonEventChannels = {};
  final Map<int, Stream<UvcCameraButtonEvent>> _buttonEventStreams = {};

  @override
  Future<bool> isSupported() async {
    final result = await _nativeMethodChannel.invokeMethod<bool>('isSupported');
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to determine if UVC camera is supported',
      );
    }
    return result;
  }

  @override
  Future<Map<String, UvcCameraDevice>> getDevices() async {
    final result = _nativeMethodChannel.invokeMethod<Map>('getDevices');
    return result.then((value) {
      if (value == null) {
        throw PlatformException(
          code: 'UNKNOWN',
          message: 'Unable to get UVC camera devices',
        );
      }
      return value.map((key, value) {
        return MapEntry(key, UvcCameraDevice.fromMap(value));
      });
    });
  }

  @override
  Future<bool> requestDevicePermission(UvcCameraDevice device) async {
    final result = await _nativeMethodChannel.invokeMethod<bool>('requestDevicePermission', {
      'deviceName': device.name,
    });
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to request device permission for device: $device',
      );
    }
    return result;
  }

  @override
  Future<int> openCamera(UvcCameraDevice device, UvcCameraResolutionPreset resolutionPreset) async {
    final result = await _nativeMethodChannel.invokeMethod<int>('openCamera', {
      'deviceName': device.name,
      'resolutionPreset': resolutionPreset.name,
    });
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to open camera for device: $device',
      );
    }
    return result;
  }

  @override
  Future<void> closeCamera(int cameraId) async {
    _statusEventChannels.remove(cameraId);
    _statusEventStreams.remove(cameraId);

    _buttonEventChannels.remove(cameraId);
    _buttonEventStreams.remove(cameraId);

    await _nativeMethodChannel.invokeMethod<void>('closeCamera', {
      'cameraId': cameraId,
    });
  }

  @override
  Future<int> getCameraTextureId(int cameraId) async {
    final result = await _nativeMethodChannel.invokeMethod<int>('getCameraTextureId', {
      'cameraId': cameraId,
    });
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to get camera texture id for camera: $cameraId',
      );
    }
    return result;
  }

  @override
  Future<Stream<UvcCameraErrorEvent>> attachToCameraErrorCallback(int cameraId) async {
    final errorEventChannel = EventChannel('uvccamera/camera@$cameraId/error_events');
    final errorEventStream = errorEventChannel.receiveBroadcastStream().map((event) {
      return UvcCameraErrorEvent.fromMap(event);
    });

    await _nativeMethodChannel.invokeMethod<void>('attachToCameraErrorCallback', {
      'cameraId': cameraId,
    });

    _errorEventChannels[cameraId] = errorEventChannel;
    _errorEventStreams[cameraId] = errorEventStream;

    return errorEventStream;
  }

  @override
  Future<void> detachFromCameraErrorCallback(int cameraId) async {
    await _nativeMethodChannel.invokeMethod<void>('detachFromCameraErrorCallback', {
      'cameraId': cameraId,
    });

    _errorEventChannels.remove(cameraId);
    _errorEventStreams.remove(cameraId);
  }

  @override
  Future<Stream<UvcCameraStatusEvent>> attachToCameraStatusCallback(int cameraId) async {
    final statusEventChannel = EventChannel('uvccamera/camera@$cameraId/status_events');
    final statusEventStream = statusEventChannel.receiveBroadcastStream().map((event) {
      return UvcCameraStatusEvent.fromMap(event);
    });

    await _nativeMethodChannel.invokeMethod<void>('attachToCameraStatusCallback', {
      'cameraId': cameraId,
    });

    _statusEventChannels[cameraId] = statusEventChannel;
    _statusEventStreams[cameraId] = statusEventStream;

    return statusEventStream;
  }

  @override
  Future<void> detachFromCameraStatusCallback(int cameraId) async {
    await _nativeMethodChannel.invokeMethod<void>('detachFromCameraStatusCallback', {
      'cameraId': cameraId,
    });

    _statusEventChannels.remove(cameraId);
    _statusEventStreams.remove(cameraId);
  }

  @override
  Future<Stream<UvcCameraButtonEvent>> attachToCameraButtonCallback(int cameraId) async {
    final buttonEventChannel = EventChannel('uvccamera/camera@$cameraId/button_events');
    final buttonEventStream = buttonEventChannel.receiveBroadcastStream().map((event) {
      return UvcCameraButtonEvent.fromMap(event);
    });

    await _nativeMethodChannel.invokeMethod<void>('attachToCameraButtonCallback', {
      'cameraId': cameraId,
    });

    _buttonEventChannels[cameraId] = buttonEventChannel;
    _buttonEventStreams[cameraId] = buttonEventStream;

    return buttonEventStream;
  }

  @override
  Future<void> detachFromCameraButtonCallback(int cameraId) async {
    await _nativeMethodChannel.invokeMethod<void>('detachFromCameraButtonCallback', {
      'cameraId': cameraId,
    });

    _buttonEventChannels.remove(cameraId);
    _buttonEventStreams.remove(cameraId);
  }

  @override
  Future<List<UvcCameraMode>> getSupportedModes(int cameraId) async {
    final result = await _nativeMethodChannel.invokeMethod<List>('getSupportedModes', {
      'cameraId': cameraId,
    });
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to get supported modes for camera: $cameraId',
      );
    }
    return result.map((value) {
      return UvcCameraMode.fromMap(value);
    }).toList();
  }

  @override
  Future<UvcCameraMode> getPreviewMode(int cameraId) async {
    final result = await _nativeMethodChannel.invokeMethod<Map>('getPreviewMode', {
      'cameraId': cameraId,
    });
    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to get preview mode for camera: $cameraId',
      );
    }
    return UvcCameraMode.fromMap(result);
  }

  @override
  Future<void> setPreviewMode(int cameraId, UvcCameraMode previewMode) async {
    await _nativeMethodChannel.invokeMethod<void>('setPreviewMode', {
      'cameraId': cameraId,
      'previewMode': previewMode.toMap(),
    });
  }

  @override
  Future<XFile> takePicture(int cameraId) async {
    final result = await _nativeMethodChannel.invokeMethod<String>('takePicture', {
      'cameraId': cameraId,
    });

    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to take picture for camera: $cameraId',
      );
    }

    return XFile(result);
  }

  @override
  Future<XFile> startVideoRecording(int cameraId, UvcCameraMode videoRecordingMode) async {
    final result = await _nativeMethodChannel.invokeMethod<String>('startVideoRecording', {
      'cameraId': cameraId,
      'videoRecordingMode': videoRecordingMode.toMap(),
    });

    if (result == null) {
      throw PlatformException(
        code: 'UNKNOWN',
        message: 'Unable to start video recording for camera: $cameraId',
      );
    }

    return XFile(result);
  }

  @override
  Future<void> stopVideoRecording(int cameraId) async {
    await _nativeMethodChannel.invokeMethod<void>('stopVideoRecording', {
      'cameraId': cameraId,
    });
  }

  @override
  Stream<UvcCameraDeviceEvent> get deviceEventStream {
    return _deviceEventStream ??= _deviceEventChannel.receiveBroadcastStream().map((event) {
      return UvcCameraDeviceEvent.fromMap(event);
    });
  }
}
