import 'uvccamera_exception.dart';

/// Exception thrown when the [UvcCameraController] is in an illegal state.
class UvcCameraControllerIllegalStateException extends UvcCameraException {
  const UvcCameraControllerIllegalStateException([
    super.message,
  ]);
}
