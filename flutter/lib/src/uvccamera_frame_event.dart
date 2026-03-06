import 'dart:typed_data';

import 'package:equatable/equatable.dart';

/// Represents a frame event from the UVC camera containing image data.
class UvcCameraFrameEvent extends Equatable {
  /// The raw image data in bytes.
  final Uint8List imageData;

  /// The width of the image in pixels.
  final int width;

  /// The height of the image in pixels.
  final int height;

  /// The timestamp when the frame was captured (in milliseconds since epoch).
  final int timestamp;

  /// The format of the image data (e.g., 'yuv420', 'nv21', 'jpeg').
  final String format;

  /// Creates a new [UvcCameraFrameEvent].
  const UvcCameraFrameEvent({
    required this.imageData,
    required this.width,
    required this.height,
    required this.timestamp,
    required this.format,
  });

  /// Creates a [UvcCameraFrameEvent] from a map.
  factory UvcCameraFrameEvent.fromMap(Map<String, dynamic> map) {
    return UvcCameraFrameEvent(
      imageData: map['imageData'] as Uint8List,
      width: map['width'] as int,
      height: map['height'] as int,
      timestamp: map['timestamp'] as int,
      format: map['format'] as String,
    );
  }

  /// Converts this [UvcCameraFrameEvent] to a map.
  Map<String, dynamic> toMap() {
    return {
      'imageData': imageData,
      'width': width,
      'height': height,
      'timestamp': timestamp,
      'format': format,
    };
  }

  /// The size of the image data in bytes.
  int get dataSize => imageData.length;

  /// The aspect ratio of the image.
  double get aspectRatio => width / height;

  @override
  List<Object?> get props => [imageData, width, height, timestamp, format];

  @override
  String toString() {
    return 'UvcCameraFrameEvent(width: $width, height: $height, format: $format, dataSize: $dataSize, timestamp: $timestamp)';
  }
}
