import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

import 'uvccamera_device.dart';
import 'uvccamera_device_event_type.dart';

@immutable
class UvcCameraDeviceEvent extends Equatable {
  final UvcCameraDevice device;
  final UvcCameraDeviceEventType type;

  const UvcCameraDeviceEvent({required this.device, required this.type});

  factory UvcCameraDeviceEvent.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraDeviceEvent(
      device: UvcCameraDevice.fromMap(map['device'] as Map<dynamic, dynamic>),
      type: UvcCameraDeviceEventType.values.byName(map['type'] as String),
    );
  }

  Map<String, dynamic> toMap() {
    return {'device': device.toMap(), 'type': type.name};
  }

  @override
  List<Object?> get props => [device, type];
}
