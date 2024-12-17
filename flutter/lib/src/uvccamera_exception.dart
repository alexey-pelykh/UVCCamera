/// UVC Camera Exception
class UvcCameraException implements Exception {
  final dynamic message;

  const UvcCameraException([this.message]);

  @override
  String toString() {
    if (message == null) return runtimeType.toString();
    return "$runtimeType: $message";
  }
}
