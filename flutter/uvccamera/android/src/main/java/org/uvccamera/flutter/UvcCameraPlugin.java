package org.uvccamera.flutter;

import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

/**
 * UvcCameraPlugin
 */
public class UvcCameraPlugin implements FlutterPlugin, ActivityAware {

    /**
     * Log tag
     */
    private static final String TAG = UvcCameraPlugin.class.getCanonicalName();

    /**
     * "uvccamera/native" method channel
     */
    private MethodChannel nativeMethodChannel;

    /**
     * "uvccamera/device_events" event channel
     */
    private EventChannel deviceEventChannel;

    /**
     * {@link UvcCameraPlatform} instance.
     */
    private UvcCameraPlatform uvcCameraPlatform;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.v(TAG, "onAttachedToEngine");

        final var applicationContext = flutterPluginBinding.getApplicationContext();
        final var binaryMessenger = flutterPluginBinding.getBinaryMessenger();
        final var textureRegistry = flutterPluginBinding.getTextureRegistry();

        nativeMethodChannel = new MethodChannel(binaryMessenger, "uvccamera/native");
        deviceEventChannel = new EventChannel(binaryMessenger, "uvccamera/device_events");

        final var deviceEventChannelStreamHandler = new UvcCameraDeviceEventStreamHandler();

        uvcCameraPlatform = new UvcCameraPlatform(
                applicationContext,
                binaryMessenger,
                textureRegistry,
                deviceEventChannelStreamHandler
        );

        nativeMethodChannel.setMethodCallHandler(new UvcCameraNativeMethodCallHandler(uvcCameraPlatform));
        deviceEventChannel.setStreamHandler(deviceEventChannelStreamHandler);
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

        if (uvcCameraPlatform != null) {
            uvcCameraPlatform.release();
            uvcCameraPlatform = null;
        }

        if (deviceEventChannel != null) {
            deviceEventChannel.setStreamHandler(null);
            deviceEventChannel = null;
        }

        if (nativeMethodChannel != null) {
            nativeMethodChannel.setMethodCallHandler(null);
            nativeMethodChannel = null;
        }
    }

}
