import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

/// UVC camera button event.
@immutable
class UvcCameraButtonEvent extends Equatable {
  final int cameraId;
  final int button;
  final int state;

  const UvcCameraButtonEvent({required this.cameraId, required this.button, required this.state});

  factory UvcCameraButtonEvent.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraButtonEvent(
      cameraId: map['cameraId'] as int,
      button: map['button'] as int,
      state: map['state'] as int,
    );
  }

  Map<String, dynamic> toMap() {
    return {'cameraId': cameraId, 'button': button, 'state': state};
  }

  @override
  List<Object?> get props => [cameraId, button, state];
}
