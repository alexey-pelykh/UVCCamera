import 'uvccamera_exception.dart';

/// Exception thrown when the [UvcCameraController] is disposed yet an operation is attempted.
class UvcCameraControllerDisposedException extends UvcCameraException {
  const UvcCameraControllerDisposedException() : super('UvcCameraController is disposed');
}
