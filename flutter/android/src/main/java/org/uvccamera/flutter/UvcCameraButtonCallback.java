package org.uvccamera.flutter;

import android.util.Log;

import com.serenegiant.usb.IButtonCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link com.serenegiant.usb.UVCCamera}'s {@link IButtonCallback} implementation
 */
/* package-private */ class UvcCameraButtonCallback implements IButtonCallback {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraButtonCallback.class.getCanonicalName();

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
     * Constructs a new {@link UvcCameraButtonCallback} instance
     *
     * @param uvcCameraPlatform the UVC camera platform
     * @param cameraId          the camera ID
     */
    public UvcCameraButtonCallback(final UvcCameraPlatform uvcCameraPlatform, final int cameraId) {
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
    public void onButton(int button, int state) {
        Log.v(TAG, "onButton"
                + ": cameraId=" + cameraId
                + ", button=" + button
                + ", state=" + state
        );

        if (castEvents.get()) {
            uvcCameraPlatform.castCameraButtonEvent(cameraId, button, state);
        }
    }

}
