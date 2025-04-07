/// UVC status attribute
///
/// Corresponds to the uvc_status_attribute from libuvc.
enum UvcCameraStatusAttribute {
  /// Corresponds to the UVC_STATUS_ATTRIBUTE_VALUE_CHANGE
  valueChange,

  /// Corresponds to the UVC_STATUS_ATTRIBUTE_INFO_CHANGE
  infoChange,

  /// Corresponds to the UVC_STATUS_ATTRIBUTE_FAILURE_CHANGE
  failureChange,

  /// Corresponds to the UVC_STATUS_ATTRIBUTE_UNKNOWN
  unknown,
}
