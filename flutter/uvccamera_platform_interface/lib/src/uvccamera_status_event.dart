import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

import 'uvccamera_status.dart';

@immutable
class UvcCameraStatusEvent extends Equatable {
  final int cameraId;
  final UvcCameraStatus payload;

  const UvcCameraStatusEvent({required this.cameraId, required this.payload});

  factory UvcCameraStatusEvent.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraStatusEvent(
      cameraId: map['cameraId'] as int,
      payload: UvcCameraStatus.fromMap(map['payload'] as Map<dynamic, dynamic>),
    );
  }

  Map<String, dynamic> toMap() {
    return {'cameraId': cameraId, 'payload': payload.toMap()};
  }

  @override
  List<Object?> get props => [cameraId, payload];
}
