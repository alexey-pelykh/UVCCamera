import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

import 'uvccamera_status_attribute.dart';
import 'uvccamera_status_class.dart';

/// UVC status.
///
/// Corresponds to the uvc_status_callback_t arguments from libuvc.
@immutable
class UvcCameraStatus extends Equatable {
  final UvcCameraStatusClass statusClass;
  final int event;
  final int selector;
  final UvcCameraStatusAttribute statusAttribute;

  const UvcCameraStatus({
    required this.statusClass,
    required this.event,
    required this.selector,
    required this.statusAttribute,
  });

  factory UvcCameraStatus.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraStatus(
      statusClass: UvcCameraStatusClass.values.byName(map['statusClass'] as String),
      event: map['event'] as int,
      selector: map['selector'] as int,
      statusAttribute: UvcCameraStatusAttribute.values.byName(map['statusAttribute'] as String),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'statusClass': statusClass.name,
      'event': event,
      'selector': selector,
      'statusAttribute': statusAttribute.name,
    };
  }

  @override
  List<Object?> get props => [statusClass, event, selector, statusAttribute];
}
