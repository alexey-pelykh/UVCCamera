package org.uvccamera.flutter;

/**
 * Handler to be notified when the device permission request result is available.
 */
@FunctionalInterface
/* package-private */ interface UvcCameraDevicePermissionRequestResultHandler {

    /**
     * Called when the device permission request result is available
     *
     * @param granted true if the device permission is granted
     */
    void onResult(boolean granted);

}
