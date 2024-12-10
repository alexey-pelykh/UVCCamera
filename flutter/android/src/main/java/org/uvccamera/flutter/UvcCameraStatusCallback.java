package org.uvccamera.flutter;

import android.util.Log;

import com.serenegiant.usb.IStatusCallback;

import java.nio.ByteBuffer;
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

        if (castEvents.get()) {
            uvcCameraPlatform.castCameraStatusEvent(cameraId, statusClass, event, selector, statusAttribute, data);
        }
    }

}
