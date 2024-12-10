package org.uvccamera.flutter;

import android.hardware.usb.UsbDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.serenegiant.usb.USBMonitor;

/**
 * {@link USBMonitor}'s {@link USBMonitor.OnDeviceConnectListener} implementation
 */
/* package-private */ class UvcCameraDeviceMonitorListener implements USBMonitor.OnDeviceConnectListener {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraDeviceMonitorListener.class.getCanonicalName();

    /**
     * The UVC camera platform
     */
    private final UvcCameraPlatform uvcCameraPlatform;

    /**
     * Constructs a new {@link UvcCameraDeviceMonitorListener} instance
     *
     * @param uvcCameraPlatform the UVC camera platform
     */
    public UvcCameraDeviceMonitorListener(final UvcCameraPlatform uvcCameraPlatform) {
        this.uvcCameraPlatform = uvcCameraPlatform;
    }

    @Override
    public void onAttach(@NonNull UsbDevice device) {
        Log.v(TAG, "onAttach: device=" + device);

        uvcCameraPlatform.castDeviceAttachedEvent(device);
    }

    @Override
    public void onDettach(@NonNull UsbDevice device) {
        Log.v(TAG, "onDettach: device=" + device);

        uvcCameraPlatform.castDeviceDetachedEvent(device);
    }

    @Override
    public void onConnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
        Log.v(TAG, "onConnect"
                + ": device=" + device
                + ", ctrlBlock=" + ctrlBlock
                + ", createNew=" + createNew
        );

        // NOTE: UVCCamera automatically connects on permission being granted, so handling permission request result
        uvcCameraPlatform.fulfillDevicePermissionRequest(device);

        uvcCameraPlatform.castDeviceConnectedEvent(device);
    }

    @Override
    public void onDisconnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock) {
        Log.v(TAG, "onDisconnect: device=" + device + ", ctrlBlock=" + ctrlBlock);

        uvcCameraPlatform.castDeviceDisconnectedEvent(device);
    }

    @Override
    public void onCancel(@NonNull UsbDevice device) {
        Log.v(TAG, "onCancel: device=" + device);

        uvcCameraPlatform.rejectDevicePermissionRequest(device);
    }

}
