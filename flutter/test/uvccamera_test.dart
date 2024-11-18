import 'package:flutter_test/flutter_test.dart';
import 'package:uvccamera/src/uvccamera_platform.dart';
import 'package:uvccamera/src/uvccamera_platform_interface.dart';

void main() {
  final UvcCameraPlatformInterface initialPlatform = UvcCameraPlatformInterface.instance;

  test('$UvcCameraPlatform is the default instance', () {
    expect(initialPlatform, isInstanceOf<UvcCameraPlatform>());
  });
}
