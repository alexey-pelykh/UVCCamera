package org.uvccamera.flutter;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.serenegiant.usb.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * "uvccamera/native" method channel call handler
 * <p>
 * It routes method calls from Flutter to {@link UvcCameraPlatform}.
 */
/* package-private */ class UvcCameraNativeMethodCallHandler implements MethodChannel.MethodCallHandler {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraNativeMethodCallHandler.class.getSimpleName();

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
     * UvcCameraPlatform instance.
     */
    private final UvcCameraPlatform uvcCameraPlatform;

    /**
     * Main looper handler
     */
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor
     */
    public UvcCameraNativeMethodCallHandler(final UvcCameraPlatform uvcCameraPlatform) {
        this.uvcCameraPlatform = uvcCameraPlatform;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        Log.v(TAG, "onMethodCall"
                + ": call=" + call
                + ", result=" + result
        );

        switch (call.method) {
            case "isSupported" -> {
                try {
                    final var isSupported = uvcCameraPlatform.isSupported();
                    result.success(isSupported);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                }
            }
            case "getDevices" -> {
                List<UsbDevice> devicesList;
                try {
                    devicesList = uvcCameraPlatform.getDevices();
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                final var devicesMap = new HashMap<String, Object>();
                for (final var device : devicesList) {
                    devicesMap.put(
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

                result.success(devicesMap);
            }
            case "requestDevicePermission" -> {
                final var deviceName = call.<String>argument("deviceName");
                if (deviceName == null) {
                    result.error("InvalidArgument", "deviceName is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.requestDevicePermission(
                            deviceName,
                            granted -> mainLooperHandler.post(() -> {
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
                if (desiredFrameArea == null) {
                    result.error("InvalidArgument", "Unknown resolutionPreset: " + resolutionPreset, null);
                    return;
                }

                long cameraId;
                try {
                    cameraId = uvcCameraPlatform.openCamera(deviceName, desiredFrameArea);
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
                    uvcCameraPlatform.closeCamera(cameraId);
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
                    textureId = uvcCameraPlatform.getCameraTextureId(cameraId);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                result.success(textureId);
            }
            case "attachToCameraErrorCallback" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.attachToCameraErrorCallback(cameraId);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                result.success(null);
            }
            case "detachFromCameraErrorCallback" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.detachFromCameraErrorCallback(cameraId);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                result.success(null);
            }
            case "attachToCameraStatusCallback" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.attachToCameraStatusCallback(cameraId);
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
                    uvcCameraPlatform.detachFromCameraStatusCallback(cameraId);
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
                    uvcCameraPlatform.attachToCameraButtonCallback(cameraId);
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
                    uvcCameraPlatform.detachFromCameraButtonCallback(cameraId);
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
                    supportedSizes = uvcCameraPlatform.getSupportedSizes(cameraId);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                final var modes = new ArrayList<HashMap<String, Object>>(supportedSizes.size());
                for (final var size : supportedSizes) {
                    final var frameFormatEnumName = FRAME_FORMAT_LIBUVC_VALUE_TO_ENUM_NAME.get(size.type);
                    if (frameFormatEnumName == null) {
                        Log.w(TAG, "Unknown frame format: " + size.type);
                        continue;
                    }

                    final var mode = new HashMap<String, Object>();
                    mode.put("frameWidth", size.width);
                    mode.put("frameHeight", size.height);
                    mode.put("frameFormat", frameFormatEnumName);
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
                    previewSize = uvcCameraPlatform.getPreviewSize(cameraId);
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                final var frameFormatEnumName = FRAME_FORMAT_LIBUVC_VALUE_TO_ENUM_NAME.get(previewSize.type);
                if (frameFormatEnumName == null) {
                    result.error("UnknownFrameFormat", "Unknown frame format: " + previewSize.type, null);
                    return;
                }

                final var mode = new HashMap<String, Object>();
                mode.put("frameWidth", previewSize.width);
                mode.put("frameHeight", previewSize.height);
                mode.put("frameFormat", frameFormatEnumName);
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

                final var frameWidth = (Integer) mode.get("frameWidth");
                if (frameWidth == null) {
                    result.error("InvalidArgument", "mode.frameWidth is required", null);
                    return;
                }

                final var frameHeight = (Integer) mode.get("frameHeight");
                if (frameHeight == null) {
                    result.error("InvalidArgument", "mode.frameHeight is required", null);
                    return;
                }

                final var frameFormat = (String) mode.get("frameFormat");
                if (frameFormat == null) {
                    result.error("InvalidArgument", "mode.frameFormat is required", null);
                    return;
                }

                final var frameFormatValue = FRAME_FORMAT_ENUM_NAME_TO_LIBUVC_VALUE.get(frameFormat);
                if (frameFormatValue == null) {
                    result.error("InvalidArgument", "Unknown frameFormat: " + frameFormat, null);
                    return;
                }

                try {
                    uvcCameraPlatform.setPreviewSize(
                            cameraId,
                            frameWidth,
                            frameHeight,
                            frameFormatValue
                    );
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                result.success(null);
            }
            case "takePicture" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.takePicture(
                            cameraId,
                            (pictureFile, error) -> mainLooperHandler.post(() -> {
                                if (error != null) {
                                    result.error(error.getClass().getSimpleName(), error.getMessage(), null);
                                } else {
                                    result.success(pictureFile.getAbsolutePath());
                                }
                            })
                    );
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                }
            }
            case "startVideoRecording" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                final var videoRecordingMode = call.<Map<String, Object>>argument("videoRecordingMode");
                if (videoRecordingMode == null) {
                    result.error("InvalidArgument", "videoRecordingMode is required", null);
                    return;
                }

                final var frameWidth = (Integer)videoRecordingMode.get("frameWidth");
                if (frameWidth == null) {
                    result.error("InvalidArgument", "videoRecordingMode.frameWidth is required", null);
                    return;
                }

                final var frameHeight = (Integer)videoRecordingMode.get("frameHeight");
                if (frameHeight == null) {
                    result.error("InvalidArgument", "videoRecordingMode.frameHeight is required", null);
                    return;
                }

                final File videoRecordingFile;
                try {
                    videoRecordingFile = uvcCameraPlatform.startVideoRecording(
                            cameraId,
                            frameWidth,
                            frameHeight
                    );
                } catch (final Exception e) {
                    result.error(e.getClass().getSimpleName(), e.getMessage(), null);
                    return;
                }

                result.success(videoRecordingFile.getAbsolutePath());
            }
            case "stopVideoRecording" -> {
                final var cameraId = call.<Integer>argument("cameraId");
                if (cameraId == null) {
                    result.error("InvalidArgument", "cameraId is required", null);
                    return;
                }

                try {
                    uvcCameraPlatform.stopVideoRecording(cameraId);
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
