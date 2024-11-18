package org.uvccamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

/**
 * UvcCameraPlugin
 */
public class UvcCameraPlugin implements FlutterPlugin, ActivityAware {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraPlugin.class.getCanonicalName();

    /**
     * libuvc's {@code uvc_status_class} to {@code UvcCameraStatusClass} enum mapping.
     */
    private static final Map<Integer, String> STATUS_CLASS_LIBUVC_VALUE_TO_ENUM_NAME = Map.of(
            /* UVC_STATUS_CLASS_CONTROL */ 0x10, "control",
            /* UVC_STATUS_CLASS_CONTROL_CAMERA */ 0x11, "controlCamera",
            /* UVC_STATUS_CLASS_CONTROL_PROCESSING */ 0x12, "controlProcessing"
    );

    /**
     * libuvc's {@code uvc_status_attribute} to {@code UvcCameraStatusAttribute} enum mapping.
     */
    private static final Map<Integer, String> STATUS_ATTRIBUTE_LIBUVC_VALUE_TO_ENUM_NAME = Map.of(
            /* UVC_STATUS_ATTRIBUTE_VALUE_CHANGE */ 0x00, "valueChange",
            /* UVC_STATUS_ATTRIBUTE_INFO_CHANGE */ 0x01, "infoChange",
            /* UVC_STATUS_ATTRIBUTE_FAILURE_CHANGE */ 0x02, "errorChange",
            /* UVC_STATUS_ATTRIBUTE_UNKNOWN */ 0xff, "unknown"
    );

    /**
     * libuvc's {@code uvc_frame_format} to {@code UvcCameraFrameFormat} enum mapping.
     */
    private static final Map<Integer, String> FRAME_FORMAT_LIBUVC_VALUE_TO_ENUM_NAME = Map.of(
            /* UVC_FRAME_FORMAT_YUYV */ 4, "yuyv",
            /* UVC_FRAME_FORMAT_MJPEG */ 6, "mjpeg"
    );

    /**
     * {@code UvcCameraFrameFormat} enum to libuvc's {@code uvc_frame_format} mapping.
     */
    private static final Map<String, Integer> FRAME_FORMAT_ENUM_NAME_TO_LIBUVC_VALUE = Map.of(
            "yuyv", /* UVC_FRAME_FORMAT_YUYV */ 4,
            "mjpeg", /* UVC_FRAME_FORMAT_MJPEG */ 6
    );

    /**
     * Resolution preset to frame area mapping.
     */
    private static final Map<String, Integer> RESOLUTION_PRESET_TO_FRAME_AREA = Map.of(
            "min", Integer.MIN_VALUE,
            "low", 640 * 480,
            "medium", 1280 * 720,
            "high", 1920 * 1080,
            "max", Integer.MAX_VALUE
    );

    /**
     * Binary messenger
     */
    private WeakReference<BinaryMessenger> binaryMessenger;

    /**
     * Application context
     */
    private WeakReference<Context> applicationContext;

    /**
     * Main looper handler
     */
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    /**
     * USB monitor
     */
    private USBMonitor usbMonitor;

    /**
     * USB monitor listener
     */
    private final UsbMonitorListener usbMonitorListener = new UsbMonitorListener();

    /**
     * "uvccamera/native" method channel
     */
    private MethodChannel nativeMethodChannel;

    /**
     * "uvccamera/native" method call handler
     */
    private final NativeMethodCallHandler nativeMethodCallHandler = new NativeMethodCallHandler();

    /**
     * "uvccamera/flutter" method channel
     */
    private MethodChannel flutterMethodChannel;

    /**
     * "uvccamera/device_events" event channel
     */
    private EventChannel deviceEventChannel;

    /**
     * "uvccamera/device_events" event sink
     */
    private EventChannel.EventSink deviceEventChannelSink;

    /**
     * "uvccamera/device_events" event stream handler
     */
    private final DeviceEventStreamHandler deviceEventStreamHandler = new DeviceEventStreamHandler();

    /**
     * Texture registry
     */
    private TextureRegistry textureRegistry;

    /**
     * Pending device permission request device name
     */
    private String pendingDevicePermissionRequestDeviceName;

    /**
     * Pending device permission request result handler
     */
    private DevicePermissionRequestResultHandler pendingDevicePermissionRequestResultHandler;

    /**
     * Lock for {@link #pendingDevicePermissionRequestDeviceName} and
     * {@link #pendingDevicePermissionRequestResultHandler}
     */
    private final Object pendingDevicePermissionRequestLock = new Object();

    /**
     * Cameras that are currently opened.
     */
    private final Map<Integer, ActiveCamera> activeCameras = new HashMap<>();

    /**
     * Lock for {@link #activeCameras}.
     */
    private final Object activeCamerasLock = new Object();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.v(TAG, "onAttachedToEngine");

        final var binaryMessenger = flutterPluginBinding.getBinaryMessenger();
        this.binaryMessenger = new WeakReference<>(binaryMessenger);

        final var applicationContext = flutterPluginBinding.getApplicationContext();
        this.applicationContext = new WeakReference<>(applicationContext);

        usbMonitor = new USBMonitor(applicationContext, usbMonitorListener);

        nativeMethodChannel = new MethodChannel(binaryMessenger, "uvccamera/native");
        nativeMethodChannel.setMethodCallHandler(nativeMethodCallHandler);

        flutterMethodChannel = new MethodChannel(binaryMessenger, "uvccamera/flutter");

        deviceEventChannel = new EventChannel(binaryMessenger, "uvccamera/device_events");
        deviceEventChannel.setStreamHandler(deviceEventStreamHandler);

        textureRegistry = flutterPluginBinding.getTextureRegistry();
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        Log.v(TAG, "onAttachedToActivity");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        Log.v(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.v(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.v(TAG, "onDetachedFromActivity");
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.v(TAG, "onDetachedFromEngine");

        textureRegistry = null;

        if (deviceEventChannel != null) {
            deviceEventChannel.setStreamHandler(null);
            deviceEventChannel = null;
        }

        if (flutterMethodChannel != null) {
            flutterMethodChannel.setMethodCallHandler(null);
            flutterMethodChannel = null;
        }

        if (nativeMethodChannel != null) {
            nativeMethodChannel.setMethodCallHandler(null);
            nativeMethodChannel = null;
        }

        if (usbMonitor != null) {
            usbMonitor.destroy();
            usbMonitor = null;
        }

        this.applicationContext = null;
        this.binaryMessenger = null;
    }

    /**
     * Checks if the device supports UVC camera
     *
     * @return true if the device supports UVC camera, false otherwise
     */
    public boolean isSupported() {
        final var applicationContext = this.applicationContext.get();
        if (applicationContext == null) {
            throw new IllegalStateException("applicationContext reference has expired");
        }

        return applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
    }

    /**
     * Requests permission to access the specified UVC camera device
     *
     * @param deviceName    the name of the UVC camera device
     * @param resultHandler the handler to be notified when the device permission request result is available
     */
    public void requestDevicePermission(
            final @NonNull String deviceName,
            final @NonNull DevicePermissionRequestResultHandler resultHandler
    ) {
        Log.v(TAG, "requestDevicePermission: deviceName=" + deviceName + ", resultHandler=" + resultHandler);

        final var device = findDeviceByName(deviceName);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceName);
        }

        synchronized (pendingDevicePermissionRequestLock) {
            if (pendingDevicePermissionRequestResultHandler != null) {
                throw new IllegalStateException("A device permission request is already pending");
            }

            pendingDevicePermissionRequestResultHandler = resultHandler;
            pendingDevicePermissionRequestDeviceName = deviceName;
        }

        Log.d(TAG, "requestDevicePermission: device=" + device);
        usbMonitor.requestPermission(device);
    }

    /**
     * Handle to be notified when the device permission request result is available.
     */
    @FunctionalInterface
    public interface DevicePermissionRequestResultHandler {

        /**
         * Called when the device permission request result is available
         *
         * @param granted true if the device permission is granted
         */
        void onResult(boolean granted);

    }

    /**
     * Opens the specified UVC camera device
     *
     * @param deviceName       the name of the UVC camera device
     * @param desiredFrameArea the desired frame area
     * @return camera ID
     */
    public int openCamera(final @NonNull String deviceName, final int desiredFrameArea) {
        Log.v(TAG, "openCamera: deviceName=" + deviceName + ", desiredFrameArea=" + desiredFrameArea);

        final var device = findDeviceByName(deviceName);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceName);
        }

        final var binaryMessenger = this.binaryMessenger.get();
        if (binaryMessenger == null) {
            throw new IllegalStateException("binaryMessenger reference has expired");
        }

        // NOTE: The device is already connected, this should just retrieve the device control block
        final var deviceCtrlBlock = usbMonitor.openDevice(device);

        final var camera = new UVCCamera();
        final var cameraId = deviceCtrlBlock.getConnection().getFileDescriptor();

        Log.d(TAG, "openCamera: opening camera");
        try {
            camera.open(deviceCtrlBlock);
        } catch (final Exception e) {
            camera.destroy();
            throw new IllegalStateException("Failed to open camera", e);
        }
        Log.d(TAG, "openCamera: camera opened");

        Log.d(TAG, "openCamera: looking for matching frame size");
        final List<Size> supportedSizes;
        try {
            supportedSizes = camera.getSupportedSizeList();
        } catch (final Exception e) {
            camera.close();
            camera.destroy();
            throw new IllegalStateException("Failed to get supported sizes", e);
        }
        final var supportedSizesWithAreaDelta = new ArrayList<Pair<Size, Integer>>(supportedSizes.size());
        for (final var size : supportedSizes) {
            final var areaDelta = size.width * size.height - desiredFrameArea;
            supportedSizesWithAreaDelta.add(new Pair<>(size, areaDelta));
        }
        Collections.sort(supportedSizesWithAreaDelta, (l, r) -> Integer.compare(r.second, l.second));
        final var desiredFrameSize = supportedSizesWithAreaDelta.get(0).first;
        Log.d(TAG, "openCamera: best size found: " + desiredFrameSize);

        // Set the status callback
        Log.d(TAG, "openCamera: setting status callback");
        final var statusCallback = new CameraStatusCallback(cameraId);
        try {
            camera.setStatusCallback(statusCallback);
        } catch (final Exception e) {
            camera.close();
            camera.destroy();
            throw new IllegalStateException("Failed to set status callback", e);
        }
        Log.d(TAG, "openCamera: status callback set");

        // Set the button callback
        Log.d(TAG, "openCamera: setting button callback");
        final var buttonCallback = new CameraButtonCallback(cameraId);
        try {
            camera.setButtonCallback(buttonCallback);
        } catch (final Exception e) {
            camera.close();
            camera.destroy();
            throw new IllegalStateException("Failed to set button callback", e);
        }

        // Set the preview size and the frame format
        Log.d(TAG, "openCamera: setting preview size and frame format");
        Integer frameFormat = null;
        for (final var desiredFrameFormat : List.of(UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.FRAME_FORMAT_YUYV)) {
            try {
                camera.setPreviewSize(
                        desiredFrameSize.width,
                        desiredFrameSize.height,
                        desiredFrameFormat
                );
                frameFormat = desiredFrameFormat;
                break;
            } catch (final IllegalArgumentException e) {
                Log.w(TAG, "Unsupported frame format: " + desiredFrameFormat);
            }
        }
        if (frameFormat == null) {
            camera.close();
            camera.destroy();
            throw new IllegalStateException("No supported frame format found");
        }
        Log.d(TAG, "openCamera: preview size and frame format set: frameFormat=" + frameFormat);

        // Set the preview display surface and start the preview
        Log.d(TAG, "openCamera: setting preview texture and starting preview");
        final var cameraTexture = textureRegistry.createSurfaceTexture();
        final var cameraSurface = new Surface(cameraTexture.surfaceTexture());
        try {
            camera.setPreviewDisplay(cameraSurface);
            camera.startPreview();
        } catch (final Exception e) {
            camera.close();
            camera.destroy();
            cameraSurface.release();
            cameraTexture.release();
            throw new IllegalStateException("Failed to start preview", e);
        }

        // Create the status event channel
        final var statusEventChannel = new EventChannel(
                binaryMessenger,
                "uvccamera/camera@" + cameraId + "/status_events"
        );
        final var statusEventStreamHandler = new CameraStatusEventStreamHandler();
        statusEventChannel.setStreamHandler(statusEventStreamHandler);

        // Create the button event channel
        final var buttonEventChannel = new EventChannel(
                binaryMessenger,
                "uvccamera/camera@" + cameraId + "/button_events"
        );
        final var buttonEventStreamHandler = new CameraButtonEventStreamHandler();
        buttonEventChannel.setStreamHandler(buttonEventStreamHandler);

        synchronized (activeCamerasLock) {
            activeCameras.put(cameraId, new ActiveCamera(
                    cameraId,
                    cameraTexture,
                    cameraSurface,
                    camera,
                    statusEventChannel,
                    statusEventStreamHandler,
                    statusCallback,
                    buttonEventChannel,
                    buttonEventStreamHandler,
                    buttonCallback
            ));
        }

        return cameraId;
    }

    /**
     * Closes the specified camera
     *
     * @param cameraId the camera ID
     */
    public void closeCamera(final int cameraId) {
        Log.v(TAG, "closeCamera: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.remove(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.statusEventChannel.setStreamHandler(null);
        activeCamera.buttonEventChannel.setStreamHandler(null);

        Log.d(TAG, "closeCamera: stopping preview");
        try {
            activeCamera.camera.stopPreview();
            Log.d(TAG, "closeCamera: preview stopped");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to stop preview", e);
        }

        Log.d(TAG, "closeCamera: unsetting button callback");
        try {
            activeCamera.camera.setButtonCallback(null);
            Log.d(TAG, "closeCamera: button callback unset");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to unset button callback", e);
        }

        Log.d(TAG, "closeCamera: unsetting status callback");
        try {
            activeCamera.camera.setStatusCallback(null);
            Log.d(TAG, "closeCamera: status callback unset");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to unset status callback", e);
        }

        Log.d(TAG, "closeCamera: closing camera");
        try {
            activeCamera.camera.close();
            Log.d(TAG, "closeCamera: camera closed");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to close camera", e);
        }

        Log.d(TAG, "closeCamera: destroying camera");
        try {
            activeCamera.camera.destroy();
            Log.d(TAG, "closeCamera: camera destroyed");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to destroy camera", e);
        }

        Log.d(TAG, "closeCamera: releasing camera surface");
        try {
            activeCamera.surface.release();
            Log.d(TAG, "closeCamera: camera surface released");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to release camera surface", e);
        }

        Log.d(TAG, "closeCamera: releasing camera texture");
        try {
            activeCamera.surfaceTextureEntry.release();
            Log.d(TAG, "closeCamera: camera texture released");
        } catch (final Exception e) {
            Log.w(TAG, "closeCamera: failed to release camera texture", e);
        }
    }

    /**
     * Gets the camera texture ID
     *
     * @param cameraId the camera ID
     * @return the camera texture ID
     */
    public long getCameraTextureId(final int cameraId) {
        Log.v(TAG, "getCameraTextureId: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        return activeCamera.surfaceTextureEntry.id();
    }

    /**
     * Attaches to the camera button callback
     *
     * @param cameraId the camera ID
     */
    public void attachToCameraStatusCallback(final int cameraId) {
        Log.v(TAG, "attachToCameraStatusCallback: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.statusCallback.attachToStreamHandler(activeCamera.statusEventStreamHandler);
    }

    /**
     * Detaches from the camera button callback
     *
     * @param cameraId the camera ID
     */
    public void detachFromCameraStatusCallback(final int cameraId) {
        Log.v(TAG, "detachFromCameraStatusCallback: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.statusCallback.detachFromStreamHandler();
    }

    /**
     * Attaches to the camera button callback
     *
     * @param cameraId the camera ID
     */
    public void attachToCameraButtonCallback(final int cameraId) {
        Log.v(TAG, "attachToCameraButtonCallback: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.buttonCallback.attachToStreamHandler(activeCamera.buttonEventStreamHandler);
    }

    /**
     * Detaches from the camera button callback
     *
     * @param cameraId the camera ID
     */
    public void detachFromCameraButtonCallback(final int cameraId) {
        Log.v(TAG, "detachFromCameraButtonCallback: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.buttonCallback.detachFromStreamHandler();
    }

    /**
     * Gets the supported sizes for the specified camera
     *
     * @param cameraId the camera ID
     * @return the supported sizes
     */
    public List<Size> getSupportedSizes(final int cameraId) {
        Log.v(TAG, "getSupportedSizes: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        return UVCCamera.getSupportedSize(-1, activeCamera.camera.getSupportedSize());
    }

    /**
     * Gets the preview size for the specified camera
     *
     * @param cameraId the camera ID
     * @return the preview size
     */
    public Size getPreviewSize(final int cameraId) {
        Log.v(TAG, "getPreviewSize: cameraId=" + cameraId);

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        return activeCamera.camera.getPreviewSize();
    }

    /**
     * Sets the preview size for the specified camera
     *
     * @param cameraId    the camera ID
     * @param frameWidth  the frame width
     * @param frameHeight the frame height
     * @param frameFormat the frame format
     */
    public void setPreviewSize(
            final int cameraId,
            final int frameWidth,
            final int frameHeight,
            final int frameFormat
    ) {
        Log.v(TAG, "setPreviewSize"
                + ": cameraId=" + cameraId
                + ", frameWidth=" + frameWidth
                + ", frameHeight=" + frameHeight
                + ", frameFormat=" + frameFormat
        );

        final ActiveCamera activeCamera;
        synchronized (activeCamerasLock) {
            activeCamera = activeCameras.get(cameraId);
        }
        if (activeCamera == null) {
            throw new IllegalArgumentException("Camera not found: " + cameraId);
        }

        activeCamera.camera.setPreviewSize(
                frameWidth,
                frameHeight,
                frameFormat == 4 ? UVCCamera.FRAME_FORMAT_YUYV : UVCCamera.FRAME_FORMAT_MJPEG
        );
    }

    /**
     * Finds the UVC camera device by name
     *
     * @param deviceName the name of the UVC camera device
     * @return the UVC camera device, or null if not found
     */
    private UsbDevice findDeviceByName(final @NonNull String deviceName) {
        for (final var device : usbMonitor.getDeviceList()) {
            if (device.getDeviceName().equals(deviceName)) {
                return device;
            }
        }

        return null;
    }

    /**
     * {@link #usbMonitor} listener
     */
    private class UsbMonitorListener implements USBMonitor.OnDeviceConnectListener {

        /**
         * Log tag
         */
        private static final String TAG = UsbMonitorListener.class.getCanonicalName();

        @Override
        public void onAttach(@NonNull UsbDevice device) {
            Log.v(TAG, "onAttach: device=" + device);

            if (UvcCameraPlugin.this.deviceEventChannelSink != null) {
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

                UvcCameraPlugin.this.mainLooperHandler.post(
                        () -> UvcCameraPlugin.this.deviceEventChannelSink.success(event)
                );
            }
        }

        @Override
        public void onDettach(@NonNull UsbDevice device) {
            Log.v(TAG, "onDettach: device=" + device);

            if (UvcCameraPlugin.this.deviceEventChannelSink != null) {
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

                UvcCameraPlugin.this.mainLooperHandler.post(
                        () -> UvcCameraPlugin.this.deviceEventChannelSink.success(event)
                );
            }
        }

        @Override
        public void onConnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            Log.v(TAG, "onConnect: device=" + device + ", ctrlBlock=" + ctrlBlock + ", createNew=" + createNew);

            // NOTE: UVCCamera automatically connects on permission being granted, so handling permission request result
            synchronized (UvcCameraPlugin.this.pendingDevicePermissionRequestLock) {
                if (UvcCameraPlugin.this.pendingDevicePermissionRequestResultHandler == null) {
                    Log.w(TAG, "No pending device permission request");
                    return;
                }

                if (!UvcCameraPlugin.this.pendingDevicePermissionRequestDeviceName.equals(device.getDeviceName())) {
                    Log.w(TAG, "Pending device permission request device name mismatch");
                    return;
                }

                pendingDevicePermissionRequestResultHandler.onResult(true);
                pendingDevicePermissionRequestResultHandler = null;
                pendingDevicePermissionRequestDeviceName = null;
            }

            if (UvcCameraPlugin.this.deviceEventChannelSink != null) {
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

                UvcCameraPlugin.this.mainLooperHandler.post(
                        () -> UvcCameraPlugin.this.deviceEventChannelSink.success(event)
                );
            }
        }

        @Override
        public void onDisconnect(@NonNull UsbDevice device, @NonNull USBMonitor.UsbControlBlock ctrlBlock) {
            Log.v(TAG, "onDisconnect: device=" + device + ", ctrlBlock=" + ctrlBlock);

            if (UvcCameraPlugin.this.deviceEventChannelSink != null) {
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

                UvcCameraPlugin.this.mainLooperHandler.post(
                        () -> UvcCameraPlugin.this.deviceEventChannelSink.success(event)
                );
            }
        }

        @Override
        public void onCancel(@NonNull UsbDevice device) {
            Log.v(TAG, "onCancel: device=" + device);

            synchronized (UvcCameraPlugin.this.pendingDevicePermissionRequestLock) {
                if (UvcCameraPlugin.this.pendingDevicePermissionRequestResultHandler == null) {
                    Log.w(TAG, "No pending device permission request");
                    return;
                }

                if (!UvcCameraPlugin.this.pendingDevicePermissionRequestDeviceName.equals(device.getDeviceName())) {
                    Log.w(TAG, "Pending device permission request device name mismatch");
                    return;
                }

                pendingDevicePermissionRequestResultHandler.onResult(false);
                pendingDevicePermissionRequestResultHandler = null;
                pendingDevicePermissionRequestDeviceName = null;
            }
        }

    }

    /**
     * Camera status callback
     */
    private class CameraStatusCallback implements IStatusCallback {

        /**
         * Log tag
         */
        private static final String TAG = CameraStatusCallback.class.getCanonicalName();

        /**
         * The camera ID
         */
        private final int cameraId;

        /**
         * Event stream handler
         */
        private CameraStatusEventStreamHandler eventStreamHandler;

        /**
         * Lock for {@link #eventStreamHandler}
         */
        private final Object eventStreamHandlerLock = new Object();

        /**
         * Constructor
         *
         * @param cameraId the camera ID
         */
        public CameraStatusCallback(final int cameraId) {
            this.cameraId = cameraId;
        }

        /**
         * Attaches to the event stream handler
         *
         * @param eventStreamHandler the event stream handler
         */
        public void attachToStreamHandler(final CameraStatusEventStreamHandler eventStreamHandler) {
            synchronized (eventStreamHandlerLock) {
                if (this.eventStreamHandler != null) {
                    throw new IllegalStateException("Already attached to an event stream handler");
                }
                this.eventStreamHandler = eventStreamHandler;
            }
        }

        /**
         * Detaches from the event stream handler
         */
        public void detachFromStreamHandler() {
            synchronized (eventStreamHandlerLock) {
                if (this.eventStreamHandler == null) {
                    throw new IllegalStateException("Not attached to an event stream handler");
                }
                this.eventStreamHandler = null;
            }
        }

        @Override
        public void onStatus(int statusClass, int event, int selector, int statusAttribute, ByteBuffer data) {
            Log.v(TAG, "onStatus"
                    + ": statusClass=" + statusClass
                    + ", event=" + event
                    + ", selector=" + selector
                    + ", statusAttribute=" + statusAttribute
                    + ", data=" + data
            );

            CameraStatusEventStreamHandler eventStreamHandler;
            synchronized (eventStreamHandlerLock) {
                eventStreamHandler = this.eventStreamHandler;
            }
            if (eventStreamHandler == null) {
                return;
            }

            final var eventMap = Map.of(
                    "cameraId", this.cameraId,
                    "payload", Map.of(
                            "statusClass", STATUS_CLASS_LIBUVC_VALUE_TO_ENUM_NAME.get(statusClass),
                            "event", event,
                            "selector", selector,
                            "statusAttribute", STATUS_ATTRIBUTE_LIBUVC_VALUE_TO_ENUM_NAME.get(statusAttribute)
                    )
            );

            UvcCameraPlugin.this.mainLooperHandler.post(
                    () -> eventStreamHandler.getEventSink().success(eventMap)
            );
        }

    }

    /**
     * Camera button event stream handler
     */
    private static class CameraStatusEventStreamHandler implements EventChannel.StreamHandler {

        /**
         * Log tag
         */
        private static final String TAG = CameraStatusEventStreamHandler.class.getCanonicalName();

        /**
         * The event sink
         */
        private EventChannel.EventSink eventSink;

        /**
         * Lock for {@link #eventSink}
         */
        private final Object eventSinkLock = new Object();

        /**
         * Returns the event sink
         *
         * @return the event sink
         */
        public EventChannel.EventSink getEventSink() {
            synchronized (eventSinkLock) {
                return eventSink;
            }
        }

        @Override
        public void onListen(Object arguments, EventChannel.EventSink eventSink) {
            Log.v(TAG, "onListen: arguments=" + arguments + ", eventSink=" + eventSink);

            synchronized (eventSinkLock) {
                this.eventSink = eventSink;
            }
        }

        @Override
        public void onCancel(Object arguments) {
            Log.v(TAG, "onCancel: arguments=" + arguments);

            synchronized (eventSinkLock) {
                this.eventSink = null;
            }
        }

    }

    /**
     * Camera button callback
     */
    private class CameraButtonCallback implements IButtonCallback {

        /**
         * Log tag
         */
        private static final String TAG = CameraButtonCallback.class.getCanonicalName();

        /**
         * The camera ID
         */
        private final int cameraId;

        /**
         * Event stream handler
         */
        private CameraButtonEventStreamHandler eventStreamHandler;

        /**
         * Lock for {@link #eventStreamHandler}
         */
        private final Object eventStreamHandlerLock = new Object();

        /**
         * Constructor
         *
         * @param cameraId the camera ID
         */
        public CameraButtonCallback(final int cameraId) {
            this.cameraId = cameraId;
        }

        /**
         * Attaches to the event stream handler
         *
         * @param eventStreamHandler the event stream handler
         */
        public void attachToStreamHandler(final CameraButtonEventStreamHandler eventStreamHandler) {
            synchronized (eventStreamHandlerLock) {
                if (this.eventStreamHandler != null) {
                    throw new IllegalStateException("Already attached to an event stream handler");
                }
                this.eventStreamHandler = eventStreamHandler;
            }
        }

        /**
         * Detaches from the event stream handler
         */
        public void detachFromStreamHandler() {
            synchronized (eventStreamHandlerLock) {
                if (this.eventStreamHandler == null) {
                    throw new IllegalStateException("Not attached to an event stream handler");
                }
                this.eventStreamHandler = null;
            }
        }

        @Override
        public void onButton(int button, int state) {
            Log.v(TAG, "onButton: button=" + button + ", state=" + state);

            CameraButtonEventStreamHandler eventStreamHandler;
            synchronized (eventStreamHandlerLock) {
                eventStreamHandler = this.eventStreamHandler;
            }
            if (eventStreamHandler == null) {
                return;
            }

            final var eventMap = Map.of(
                    "cameraId", this.cameraId,
                    "button", button,
                    "state", state
            );

            UvcCameraPlugin.this.mainLooperHandler.post(
                    () -> eventStreamHandler.getEventSink().success(eventMap)
            );
        }

    }

    /**
     * Camera button event stream handler
     */
    private static class CameraButtonEventStreamHandler implements EventChannel.StreamHandler {

        /**
         * Log tag
         */
        private static final String TAG = CameraButtonEventStreamHandler.class.getCanonicalName();

        /**
         * The event sink
         */
        private EventChannel.EventSink eventSink;

        /**
         * Lock for {@link #eventSink}
         */
        private final Object eventSinkLock = new Object();

        /**
         * Returns the event sink
         *
         * @return the event sink
         */
        public EventChannel.EventSink getEventSink() {
            synchronized (eventSinkLock) {
                return eventSink;
            }
        }

        @Override
        public void onListen(Object arguments, EventChannel.EventSink eventSink) {
            Log.v(TAG, "onListen: arguments=" + arguments + ", eventSink=" + eventSink);

            synchronized (eventSinkLock) {
                this.eventSink = eventSink;
            }
        }

        @Override
        public void onCancel(Object arguments) {
            Log.v(TAG, "onCancel: arguments=" + arguments);

            synchronized (eventSinkLock) {
                this.eventSink = null;
            }
        }

    }

    /**
     * {@link #deviceEventChannel} stream handler
     */
    private class DeviceEventStreamHandler implements EventChannel.StreamHandler {

        /**
         * Log tag
         */
        private static final String TAG = DeviceEventStreamHandler.class.getCanonicalName();

        @Override
        public void onListen(Object arguments, EventChannel.EventSink eventSink) {
            Log.v(TAG, "onListen: arguments=" + arguments + ", eventSink=" + eventSink);

            UvcCameraPlugin.this.deviceEventChannelSink = eventSink;

            UvcCameraPlugin.this.usbMonitor.register();
        }

        @Override
        public void onCancel(Object arguments) {
            Log.v(TAG, "onCancel: arguments=" + arguments);

            UvcCameraPlugin.this.usbMonitor.unregister();

            UvcCameraPlugin.this.deviceEventChannelSink = null;
        }

    }

    /**
     * {@link #nativeMethodChannel} call handler
     */
    private class NativeMethodCallHandler implements MethodChannel.MethodCallHandler {

        /**
         * Log tag
         */
        private static final String TAG = NativeMethodCallHandler.class.getCanonicalName();

        @Override
        public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
            Log.v(TAG, "onMethodCall: " + call + ", result=" + result);

            switch (call.method) {
                case "isSupported" -> {
                    try {
                        final var isSupported = UvcCameraPlugin.this.isSupported();
                        result.success(isSupported);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    }
                }
                case "getDevices" -> {
                    List<UsbDevice> devicesList;
                    try {
                        devicesList = UvcCameraPlugin.this.usbMonitor.getDeviceList();
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    final var devices = new HashMap<String, Object>();
                    for (final var device : devicesList) {
                        devices.put(
                                device.getDeviceName(),
                                Map.of(
                                        "name", device.getDeviceName(),
                                        "deviceClass", device.getDeviceClass(),
                                        "deviceSubclass", device.getDeviceSubclass(),
                                        "vendorId", device.getVendorId(),
                                        "productId", device.getProductId()
                                )
                        );
                    }

                    result.success(devices);
                }
                case "requestDevicePermission" -> {
                    final var deviceName = call.<String>argument("deviceName");
                    if (deviceName == null) {
                        result.error("InvalidArgument", "deviceName is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.requestDevicePermission(
                                deviceName,
                                granted -> UvcCameraPlugin.this.mainLooperHandler.post(() -> {
                                    if (granted) {
                                        result.success(true);
                                    } else {
                                        result.error("PermissionNotGranted", "Permission not granted", null);
                                    }
                                })
                        );
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    }
                }
                case "openCamera" -> {
                    final var deviceName = call.<String>argument("deviceName");
                    if (deviceName == null) {
                        result.error("InvalidArgument", "deviceName is required", null);
                        return;
                    }

                    final var resolutionPreset = call.<String>argument("resolutionPreset");
                    if (resolutionPreset == null) {
                        result.error("InvalidArgument", "resolutionPreset is required", null);
                        return;
                    }

                    final var desiredFrameArea = RESOLUTION_PRESET_TO_FRAME_AREA.get(resolutionPreset);

                    long cameraId;
                    try {
                        cameraId = UvcCameraPlugin.this.openCamera(deviceName, desiredFrameArea);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(cameraId);
                }
                case "closeCamera" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.closeCamera(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                case "getCameraTextureId" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    long textureId;
                    try {
                        textureId = UvcCameraPlugin.this.getCameraTextureId(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(textureId);
                }
                case "attachToCameraStatusCallback" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.attachToCameraStatusCallback(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                case "detachFromCameraStatusCallback" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.detachFromCameraStatusCallback(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                case "attachToCameraButtonCallback" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.attachToCameraButtonCallback(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                case "detachFromCameraButtonCallback" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.detachFromCameraButtonCallback(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                case "getSupportedModes" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    List<Size> supportedSizes;
                    try {
                        supportedSizes = UvcCameraPlugin.this.getSupportedSizes(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    final var modes = new ArrayList<HashMap<String, Object>>(supportedSizes.size());
                    for (final var size : supportedSizes) {
                        final var mode = new HashMap<String, Object>();
                        mode.put("frameWidth", size.width);
                        mode.put("frameHeight", size.height);
                        mode.put("frameFormat", FRAME_FORMAT_LIBUVC_VALUE_TO_ENUM_NAME.get(size.type));
                        modes.add(mode);
                    }

                    result.success(modes);
                }
                case "getPreviewMode" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    Size previewSize;
                    try {
                        previewSize = UvcCameraPlugin.this.getPreviewSize(cameraId);
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    final var mode = new HashMap<String, Object>();
                    mode.put("frameWidth", previewSize.width);
                    mode.put("frameHeight", previewSize.height);
                    mode.put("frameFormat", FRAME_FORMAT_LIBUVC_VALUE_TO_ENUM_NAME.get(previewSize.type));
                    result.success(mode);
                }
                case "setPreviewMode" -> {
                    final var cameraId = call.<Integer>argument("cameraId");
                    if (cameraId == null) {
                        result.error("InvalidArgument", "cameraId is required", null);
                        return;
                    }

                    final var mode = call.<Map<String, Object>>argument("mode");
                    if (mode == null) {
                        result.error("InvalidArgument", "mode is required", null);
                        return;
                    }

                    final var frameWidth = (Integer)mode.get("frameWidth");
                    if (frameWidth == null) {
                        result.error("InvalidArgument", "mode.frameWidth is required", null);
                        return;
                    }

                    final var frameHeight = (Integer)mode.get("frameHeight");
                    if (frameHeight == null) {
                        result.error("InvalidArgument", "mode.frameHeight is required", null);
                        return;
                    }

                    final var frameFormat = (String)mode.get("frameFormat");
                    if (frameFormat == null) {
                        result.error("InvalidArgument", "mode.frameFormat is required", null);
                        return;
                    }

                    try {
                        UvcCameraPlugin.this.setPreviewSize(
                                cameraId,
                                frameWidth,
                                frameHeight,
                                FRAME_FORMAT_ENUM_NAME_TO_LIBUVC_VALUE.get(frameFormat)
                        );
                    } catch (final Exception e) {
                        result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                        return;
                    }

                    result.success(null);
                }
                default -> result.notImplemented();
            }
        }

    }

    /**
     * Active camera
     */
    private record ActiveCamera(
            int cameraId,
            TextureRegistry.SurfaceTextureEntry surfaceTextureEntry,
            Surface surface,
            UVCCamera camera,
            EventChannel statusEventChannel,
            CameraStatusEventStreamHandler statusEventStreamHandler,
            CameraStatusCallback statusCallback,
            EventChannel buttonEventChannel,
            CameraButtonEventStreamHandler buttonEventStreamHandler,
            CameraButtonCallback buttonCallback
    ) {
    }

}
