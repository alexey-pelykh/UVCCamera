import 'uvccamera_device.dart';
import 'uvccamera_device_event.dart';
import 'uvccamera_platform_interface.dart';

/// UVC Camera plugin
class UvcCamera {
  /// Checks if UVC camera is supported on the current device.
  static Future<bool> isSupported() {
    return UvcCameraPlatformInterface.instance.isSupported();
  }

  /// Gets a list of all UVC camera devices connected to the device.
  static Future<Map<String, UvcCameraDevice>> getDevices() {
    return UvcCameraPlatformInterface.instance.getDevices();
  }

  /// Requests permission to access the UVC camera device.
  static Future<bool> requestDevicePermission(UvcCameraDevice uvcCameraDevice) {
    return UvcCameraPlatformInterface.instance.requestDevicePermission(uvcCameraDevice);
  }

  /// Device event stream.
  static Stream<UvcCameraDeviceEvent> get deviceEventStream {
    return UvcCameraPlatformInterface.instance.deviceEventStream;
  }
}
