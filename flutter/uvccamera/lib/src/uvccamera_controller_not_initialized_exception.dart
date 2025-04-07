import 'uvccamera_exception.dart';

/// Exception thrown when the [UvcCameraController] is not initialized yet an operation is attempted.
class UvcCameraControllerNotInitializedException extends UvcCameraException {
  const UvcCameraControllerNotInitializedException() : super('UvcCameraController is not initialized');
}
