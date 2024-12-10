package org.uvccamera.flutter;

import static org.uvccamera.flutter.UvcCameraPlatform.STATUS_ATTRIBUTE_LIBUVC_VALUE_TO_ENUM_NAME;
import static org.uvccamera.flutter.UvcCameraPlatform.STATUS_CLASS_LIBUVC_VALUE_TO_ENUM_NAME;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.serenegiant.usb.IStatusCallback;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link com.serenegiant.usb.UVCCamera}'s {@link IStatusCallback} implementation
 */
/* package-private */ class UvcCameraStatusCallback implements IStatusCallback {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraStatusCallback.class.getCanonicalName();

    /**
     * The UVC camera platform
     */
    private final UvcCameraPlatform uvcCameraPlatform;

    /**
     * The camera ID
     */
    private final int cameraId;

    /**
     * Flag that controls whether or not the events are casted to the sink
     */
    private final AtomicBoolean castEvents = new AtomicBoolean(false);

    /**
     * Main looper handler
     */
    private final Handler mainLooperHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructs a new {@link UvcCameraStatusCallback} instance
     *
     * @param uvcCameraPlatform the UVC camera platform
     * @param cameraId          the camera ID
     */
    public UvcCameraStatusCallback(final UvcCameraPlatform uvcCameraPlatform, final int cameraId) {
        this.uvcCameraPlatform = uvcCameraPlatform;
        this.cameraId = cameraId;
    }

    /**
     * Enables casting of events to the sink
     */
    public void enableEventsCasting() {
        castEvents.set(true);
    }

    /**
     * Disables casting of events to the sink
     */
    public void disableEventsCasting() {
        castEvents.set(false);
    }

    @Override
    public void onStatus(int statusClass, int event, int selector, int statusAttribute, ByteBuffer data) {
        Log.v(TAG, "onStatus"
                + ": cameraId=" + cameraId
                + ", statusClass=" + statusClass
                + ", event=" + event
                + ", selector=" + selector
                + ", statusAttribute=" + statusAttribute
                + ", data=" + data
        );

        if (!castEvents.get()) {
            return;
        }

        final var eventSink = uvcCameraPlatform.getCameraStatusEventSink(cameraId);
        if (eventSink != null) {
            final var eventMap = Map.of(
                    "cameraId", this.cameraId,
                    "payload", Map.of(
                            "statusClass", STATUS_CLASS_LIBUVC_VALUE_TO_ENUM_NAME.get(statusClass),
                            "event", event,
                            "selector", selector,
                            "statusAttribute", STATUS_ATTRIBUTE_LIBUVC_VALUE_TO_ENUM_NAME.get(statusAttribute)
                    )
            );

            mainLooperHandler.post(
                    () -> eventSink.success(eventMap)
            );
        }
    }

}
