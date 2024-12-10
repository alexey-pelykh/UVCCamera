package org.uvccamera.flutter;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.serenegiant.usb.USBMonitor;

import java.util.Map;

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
     * Main looper handler
     */
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

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

        final var eventSink = uvcCameraPlatform.getDeviceEventSink();
        if (eventSink != null) {
            final var event = Map.of(
                    "device", Map.of(
                            "name", device.getDeviceName(),
                            "deviceClass", device.getDeviceClass(),
                            "deviceSubclass", device.getDeviceSubclass(),
                            "vendorId", device.getVendorId(),
                            "productId", device.getProductId()
                    ),
                    "type", "attached"
            );

            mainLooperHandler.post(
                    () -> eventSink.success(event)
            );
        }
    }

    @Override
    public void onDettach(@NonNull UsbDevice device) {
        Log.v(TAG, "onDettach: device=" + device);

        final var eventSink = uvcCameraPlatform.getDeviceEventSink();
        if (eventSink != null) {
            final var event = Map.of(
                    "device", Map.of(
                            "name", device.getDeviceName(),
                            "deviceClass", device.getDeviceClass(),
                            "deviceSubclass", device.getDeviceSubclass(),
                            "vendorId", device.getVendorId(),
                            "productId", device.getProductId()
                    ),
                    "type", "detached"
            );

            mainLooperHandler.post(
                    () -> eventSink.success(event)
            );
        }
    }

    @Override
    public void onConnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
        Log.v(TAG, "onConnect: device=" + device + ", ctrlBlock=" + ctrlBlock + ", createNew=" + createNew);

        // NOTE: UVCCamera automatically connects on permission being granted, so handling permission request result
        uvcCameraPlatform.fulfillDevicePermissionRequest(device);

        final var eventSink = uvcCameraPlatform.getDeviceEventSink();
        if (eventSink != null) {
            final var event = Map.of(
                    "device", Map.of(
                            "name", device.getDeviceName(),
                            "deviceClass", device.getDeviceClass(),
                            "deviceSubclass", device.getDeviceSubclass(),
                            "vendorId", device.getVendorId(),
                            "productId", device.getProductId()
                    ),
                    "type", "connected"
            );

            mainLooperHandler.post(
                    () -> eventSink.success(event)
            );
        }
    }

    @Override
    public void onDisconnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock) {
        Log.v(TAG, "onDisconnect: device=" + device + ", ctrlBlock=" + ctrlBlock);

        final var eventSink = uvcCameraPlatform.getDeviceEventSink();
        if (eventSink != null) {
            final var event = Map.of(
                    "device", Map.of(
                            "name", device.getDeviceName(),
                            "deviceClass", device.getDeviceClass(),
                            "deviceSubclass", device.getDeviceSubclass(),
                            "vendorId", device.getVendorId(),
                            "productId", device.getProductId()
                    ),
                    "type", "disconnected"
            );

            mainLooperHandler.post(
                    () -> eventSink.success(event)
            );
        }
    }

    @Override
    public void onCancel(@NonNull UsbDevice device) {
        Log.v(TAG, "onCancel: device=" + device);

        uvcCameraPlatform.rejectDevicePermissionRequest(device);
    }

}
