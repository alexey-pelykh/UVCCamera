package org.uvccamera.flutter;

import android.media.MediaRecorder;
import android.view.Surface;

import com.serenegiant.usb.UVCCamera;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

/**
 * UVC camera resources
 */
/* package-private */ record UvcCameraResources(
        int cameraId,
        TextureRegistry.SurfaceTextureEntry surfaceTextureEntry,
        Surface surface,
        UVCCamera camera,
        EventChannel statusEventChannel,
        UvcCameraStatusEventStreamHandler statusEventStreamHandler,
        UvcCameraStatusCallback statusCallback,
        EventChannel buttonEventChannel,
        UvcCameraButtonEventStreamHandler buttonEventStreamHandler,
        UvcCameraButtonCallback buttonCallback,
        MediaRecorder mediaRecorder
) {
}
