import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:cross_file/cross_file.dart';

import 'uvccamera_button_event.dart';
import 'uvccamera_device.dart';
import 'uvccamera_device_event.dart';
import 'uvccamera_error_event.dart';
import 'uvccamera_platform.dart';
import 'uvccamera_mode.dart';
import 'uvccamera_resolution_preset.dart';
import 'uvccamera_status_event.dart';

abstract class UvcCameraPlatformInterface extends PlatformInterface {
  UvcCameraPlatformInterface() : super(token: _token);

  static final Object _token = Object();

  static UvcCameraPlatformInterface _instance = UvcCameraPlatform();
  static UvcCameraPlatformInterface get instance => _instance;

  static set instance(UvcCameraPlatformInterface instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> isSupported() {
    throw UnimplementedError('isSupported() has not been implemented.');
  }

  Future<Map<String, UvcCameraDevice>> getDevices() {
    throw UnimplementedError('getDevices() has not been implemented.');
  }

  Future<bool> requestDevicePermission(UvcCameraDevice device) {
    throw UnimplementedError('requestDevicePermission() has not been implemented.');
  }

  Future<int> openCamera(UvcCameraDevice device, UvcCameraResolutionPreset resolutionPreset) {
    throw UnimplementedError('openCamera() has not been implemented.');
  }

  Future<void> closeCamera(int cameraId) {
    throw UnimplementedError('closeCamera() has not been implemented.');
  }

  Future<int> getCameraTextureId(int cameraId) {
    throw UnimplementedError('getCameraTextureId() has not been implemented.');
  }

  Future<Stream<UvcCameraErrorEvent>> attachToCameraErrorCallback(int cameraId) {
    throw UnimplementedError('attachToCameraErrorCallback() has not been implemented.');
  }

  Future<void> detachFromCameraErrorCallback(int cameraId) {
    throw UnimplementedError('detachFromCameraErrorCallback() has not been implemented.');
  }

  Future<Stream<UvcCameraStatusEvent>> attachToCameraStatusCallback(int cameraId) {
    throw UnimplementedError('attachToCameraStatusCallback() has not been implemented.');
  }

  Future<void> detachFromCameraStatusCallback(int cameraId) {
    throw UnimplementedError('detachFromCameraStatusCallback() has not been implemented.');
  }

  Future<Stream<UvcCameraButtonEvent>> attachToCameraButtonCallback(int cameraId) {
    throw UnimplementedError('attachToCameraButtonCallback() has not been implemented.');
  }

  Future<void> detachFromCameraButtonCallback(int cameraId) {
    throw UnimplementedError('detachFromCameraButtonCallback() has not been implemented.');
  }

  Future<List<UvcCameraMode>> getSupportedModes(int cameraId) {
    throw UnimplementedError('getSupportedModes() has not been implemented.');
  }

  Future<UvcCameraMode> getPreviewMode(int cameraId) {
    throw UnimplementedError('getPreviewMode() has not been implemented.');
  }

  Future<void> setPreviewMode(int cameraId, UvcCameraMode previewMode) {
    throw UnimplementedError('setPreviewMode() has not been implemented.');
  }

  Future<XFile> takePicture(int cameraId) {
    throw UnimplementedError('takePicture() has not been implemented.');
  }

  Future<XFile> startVideoRecording(int cameraId, UvcCameraMode videoRecordingMode) {
    throw UnimplementedError('startVideoRecording() has not been implemented.');
  }

  Future<void> stopVideoRecording(int cameraId) {
    throw UnimplementedError('stopVideoRecording() has not been implemented.');
  }

  Stream<UvcCameraDeviceEvent> get deviceEventStream {
    throw UnimplementedError('deviceEventStream has not been implemented.');
  }
}
