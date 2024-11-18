import 'package:flutter/foundation.dart';

import 'uvccamera_device.dart';
import 'uvccamera_mode.dart';

/// The state of a [UvcCameraController].
@immutable
class UvcCameraControllerState {

  /// True after [UvcCameraController.initialize] has completed successfully.
  final bool isInitialized;

  /// The UVC device controlled by the controller.
  final UvcCameraDevice device;

  /// The current preview mode of the camera.
  ///
  /// Is `null` until [isInitialized] is `true`.
  final UvcCameraMode? previewMode;

  /// Creates a new [UvcCameraControllerState] object.
  const UvcCameraControllerState({
    required this.isInitialized,
    required this.device,
    this.previewMode,
  });

  /// Creates a [UvcCameraControllerState] object for an uninitialized controller.
  const UvcCameraControllerState.uninitialized(UvcCameraDevice device)
      : this(
    isInitialized: false,
    device: device,
  );

  /// Creates a modified copy of this object.
  ///
  /// Explicitly specified fields get the specified value, all other fields get the same value of the current object.
  UvcCameraControllerState copyWith({
    bool? isInitialized,
    UvcCameraDevice? device,
    UvcCameraMode? previewMode,
  }) {
    return UvcCameraControllerState(
      isInitialized: isInitialized ?? this.isInitialized,
      device: device ?? this.device,
      previewMode: previewMode ?? this.previewMode,
    );
  }

  @override
  String toString() {
    return '${objectRuntimeType(this, 'UvcCameraControllerState')}('
        'isInitialized: $isInitialized, '
        'device: $device, '
        'previewMode: $previewMode'
        ')';
  }

}
