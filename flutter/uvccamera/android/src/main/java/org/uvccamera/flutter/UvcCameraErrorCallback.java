package org.uvccamera.flutter;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.view.TextureRegistry;

/**
 * An error callback implementing
 */
/* package-private */ class UvcCameraErrorCallback {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraErrorCallback.class.getCanonicalName();

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
     * A callback for the {@link TextureRegistry.SurfaceProducer#setCallback(TextureRegistry.SurfaceProducer.Callback)}
     */
    public final TextureRegistry.SurfaceProducer.Callback textureRegistrySurfaceProducerCallback =
            new TextureRegistrySurfaceProducerCallback();

    /**
     * Constructs a new {@link UvcCameraErrorCallback} instance
     *
     * @param uvcCameraPlatform the UVC camera platform
     * @param cameraId          the camera ID
     */
    public UvcCameraErrorCallback(final UvcCameraPlatform uvcCameraPlatform, final int cameraId) {
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

    /**
     * {@link TextureRegistry.SurfaceProducer.Callback}'s implementation
     */
    private class TextureRegistrySurfaceProducerCallback implements TextureRegistry.SurfaceProducer.Callback {
        /**
         * Log tag
         */
        private static final String TAG = TextureRegistrySurfaceProducerCallback.class.getCanonicalName();

        @Override
        public void onSurfaceDestroyed() {
            Log.v(TAG, "onSurfaceDestroyed");

            if (UvcCameraErrorCallback.this.castEvents.get()) {
                UvcCameraErrorCallback.this.uvcCameraPlatform.castCameraErrorEvent(
                        cameraId,
                        "previewInterrupted",
                        "The surface was destroyed"
                );
            }
        }
    }

}
