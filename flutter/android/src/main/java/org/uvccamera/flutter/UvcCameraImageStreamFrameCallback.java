package org.uvccamera.flutter;
import android.os.Handler;
import android.os.Looper;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import io.flutter.plugin.common.EventChannel;
/**
 * A callback that receives frames from the UVC camera and continuously
 * sends them to Flutter via EventChannel.
 */
public class UvcCameraImageStreamFrameCallback implements IFrameCallback {
    /// Event sink to send data back to Flutter
    private final EventChannel.EventSink eventSink;

    /// Handler to ensure events are sent on the main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /// Frame width and height
    private final int width;
    private final int height;
    /**
     * Constructor
     *
     * @param sink   EventSink to send frame data
     * @param width  Width of the preview frame
     * @param height Height of the preview frame
     */
    public UvcCameraImageStreamFrameCallback(EventChannel.EventSink sink, int width, int height) {
        this.eventSink = sink;
        this.width = width;
        this.height = height;
    }
    @Override
    public void onFrame(final ByteBuffer frame) {
        if (eventSink == null) return;
        // Create a byte array with the remaining size of the ByteBuffer
        final byte[] bytes = new byte[frame.remaining()];

        // Copy the NV21 frame data into our byte array
        frame.get(bytes);
        // Package the frame data with its metadata
        final Map<String, Object> frameData = new HashMap<>();
        frameData.put("bytes", bytes);
        frameData.put("width", width);
        frameData.put("height", height);
        // Explicitly indicate that the format is NV21
        frameData.put("format", UVCCamera.PIXEL_FORMAT_NV21);
        // MethodChannel and EventChannel require events to be fired on the UI (main) thread.
        // Post the success event back to the main looper.
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                eventSink.success(frameData);
            }
        });
    }
}
