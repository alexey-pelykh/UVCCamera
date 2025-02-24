import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

@immutable
class UvcCameraDevice extends Equatable {
  final String name;
  final int deviceClass;
  final int deviceSubclass;
  final int vendorId;
  final int productId;

  const UvcCameraDevice({
    required this.name,
    required this.deviceClass,
    required this.deviceSubclass,
    required this.vendorId,
    required this.productId,
  });

  factory UvcCameraDevice.fromMap(Map<dynamic, dynamic> map) {
    return UvcCameraDevice(
      name: map['name'] as String,
      deviceClass: map['deviceClass'] as int,
      deviceSubclass: map['deviceSubclass'] as int,
      vendorId: map['vendorId'] as int,
      productId: map['productId'] as int,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'name': name,
      'deviceClass': deviceClass,
      'deviceSubclass': deviceSubclass,
      'vendorId': vendorId,
      'productId': productId,
    };
  }

  @override
  List<Object?> get props => [name, deviceClass, deviceSubclass, vendorId, productId];
}
