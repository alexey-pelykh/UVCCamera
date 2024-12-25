import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

import 'uvccamera_error.dart';

/// UVC camera error event.
@immutable
class UvcCameraErrorEvent extends Equatable {
  final int cameraId;
  final UvcCameraError error;

  const UvcCameraErrorEvent({
    required this.cameraId,
    required this.error,
  });

  factory UvcCameraErrorEvent.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraErrorEvent(
      cameraId: map['cameraId'] as int,
      error: UvcCameraError.fromMap(map['error'] as Map<dynamic, dynamic>),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'cameraId': cameraId,
      'error': error.toMap(),
    };
  }

  @override
  List<Object?> get props => [
        cameraId,
        error,
      ];
}
