import 'uvccamera_exception.dart';

/// Exception thrown when the [UvcCameraController] is already initialized.
class UvcCameraControllerInitializedException extends UvcCameraException {
  const UvcCameraControllerInitializedException() : super('UvcCameraController is already initialized');
}
