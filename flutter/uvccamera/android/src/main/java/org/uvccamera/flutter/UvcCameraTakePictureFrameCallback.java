package org.uvccamera.flutter;

import android.util.Log;

import com.serenegiant.usb.IFrameCallback;

import java.io.File;
import java.nio.ByteBuffer;

/* package-private */ class UvcCameraTakePictureFrameCallback implements IFrameCallback {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraTakePictureFrameCallback.class.getCanonicalName();

    /**
     * The UVC camera platform
     */
    private final UvcCameraPlatform uvcCameraPlatform;

    /**
     * The camera ID
     */
    private final int cameraId;

    /**
     * Output file to which the picture is saved.
     */
    private final File outputFile;

    /**
     * The result handler
     */
    private final UvcCameraTakePictureResultHandler resultHandler;

    /**
     * Whether the frame has been captured
     */
    private boolean frameCaptured;

    /**
     * Creates a new instance of {@link UvcCameraTakePictureFrameCallback}.
     *
     * @param uvcCameraPlatform the UVC camera platform
     * @param cameraId          the camera ID
     * @param outputFile        the output file
     * @param resultHandler     the result handler
     */
    public UvcCameraTakePictureFrameCallback(
            final UvcCameraPlatform uvcCameraPlatform,
            final int cameraId,
            final File outputFile,
            final UvcCameraTakePictureResultHandler resultHandler
    ) {
        this.uvcCameraPlatform = uvcCameraPlatform;
        this.cameraId = cameraId;
        this.outputFile = outputFile;
        this.resultHandler = resultHandler;
    }

    @Override
    public void onFrame(ByteBuffer frame) {
        Log.v(TAG, "onFrame"
                + ": frame=" + frame
        );

        if (frameCaptured) {
            Log.w(TAG, "onFrame: frame already captured");
            return;
        }

        frameCaptured = true;

        uvcCameraPlatform.handleTakenPicture(cameraId, outputFile, frame, resultHandler);
    }

}
