import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

import 'uvccamera_error_type.dart';

@immutable
class UvcCameraError extends Equatable {
  final UvcCameraErrorType type;
  final String? reason;

  const UvcCameraError({
    required this.type,
    this.reason,
  });

  factory UvcCameraError.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraError(
      type: UvcCameraErrorType.values.byName(map['type'] as String),
      reason: map['reason'] as String?,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'type': type.name,
    };
  }

  @override
  List<Object?> get props => [
        type,
      ];
}
