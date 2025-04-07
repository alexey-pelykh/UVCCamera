package org.uvccamera.flutter;

import android.util.Log;

import io.flutter.plugin.common.EventChannel;

/**
 * Camera button event stream handler
 */
/* package-private */ class UvcCameraButtonEventStreamHandler implements EventChannel.StreamHandler {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraButtonEventStreamHandler.class.getCanonicalName();

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
