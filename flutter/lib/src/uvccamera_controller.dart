import 'dart:async';

import 'package:cross_file/cross_file.dart';
import 'package:flutter/widgets.dart';

import 'uvccamera_button_event.dart';
import 'uvccamera_controller_disposed_exception.dart';
import 'uvccamera_controller_illegal_state_exception.dart';
import 'uvccamera_controller_initialized_exception.dart';
import 'uvccamera_controller_not_initialized_exception.dart';
import 'uvccamera_controller_state.dart';
import 'uvccamera_device.dart';
import 'uvccamera_error_event.dart';
import 'uvccamera_frame_event.dart';
import 'uvccamera_mode.dart';
import 'uvccamera_platform_interface.dart';
import 'uvccamera_resolution_preset.dart';
import 'uvccamera_status_event.dart';

/// A controller for a connected [UvcCameraDevice].
class UvcCameraController extends ValueNotifier<UvcCameraControllerState> {
  /// The camera device controlled by this controller.
  final UvcCameraDevice device;

  /// The resolution preset requested for the camera.
  final UvcCameraResolutionPreset resolutionPreset;

  bool _isDisposed = false;
  Future<void>? _initializeFuture;

  /// Camera ID
  int? _cameraId;

  /// Texture ID
  int? _textureId;

  /// Stream of camera error events.
  Stream<UvcCameraErrorEvent>? _cameraErrorEventStream;

  /// Stream of camera status events.
  Stream<UvcCameraStatusEvent>? _cameraStatusEventStream;

  /// Stream of camera button events.
  Stream<UvcCameraButtonEvent>? _cameraButtonEventStream;

  /// Stream of camera frame events.
  Stream<UvcCameraFrameEvent>? _cameraFrameEventStream;

  /// Frame event subscription for image streaming.
  StreamSubscription<UvcCameraFrameEvent>? _frameEventSubscription;

  /// Callback function for image streaming.
  void Function(UvcCameraFrameEvent)? _onImageAvailable;

  /// Creates a new [UvcCameraController] object.
  UvcCameraController({required this.device, this.resolutionPreset = UvcCameraResolutionPreset.max})
    : super(UvcCameraControllerState.uninitialized(device));

  /// Initializes the controller on the device.
  Future<void> initialize() => _initialize(device);

  /// Initializes the controller on the specified device.
  Future<void> _initialize(UvcCameraDevice device) async {
    if (_initializeFuture != null) {
      throw UvcCameraControllerInitializedException();
    }
    if (_isDisposed) {
      throw UvcCameraControllerDisposedException();
    }

    final Completer<void> initializeCompleter = Completer<void>();
    _initializeFuture = initializeCompleter.future;

    try {
      _cameraId = await UvcCameraPlatformInterface.instance.openCamera(device, resolutionPreset);

      _textureId = await UvcCameraPlatformInterface.instance.getCameraTextureId(_cameraId!);
      final previewMode = await UvcCameraPlatformInterface.instance.getPreviewMode(_cameraId!);

      _cameraErrorEventStream = await UvcCameraPlatformInterface.instance.attachToCameraErrorCallback(_cameraId!);
      _cameraStatusEventStream = await UvcCameraPlatformInterface.instance.attachToCameraStatusCallback(_cameraId!);
      _cameraButtonEventStream = await UvcCameraPlatformInterface.instance.attachToCameraButtonCallback(_cameraId!);
      _cameraFrameEventStream = await UvcCameraPlatformInterface.instance.attachToCameraFrameCallback(_cameraId!);

      value = value.copyWith(isInitialized: true, device: device, previewMode: previewMode);

      initializeCompleter.complete();
    } catch (e) {
      initializeCompleter.completeError(e);
    }
  }

  @override
  Future<void> dispose() async {
    if (_isDisposed) {
      return;
    }
    super.dispose();

    _isDisposed = true;

    if (_initializeFuture != null) {
      await _initializeFuture;
      _initializeFuture = null;
    }

    if (_cameraButtonEventStream != null) {
      if (_cameraId != null) {
        await UvcCameraPlatformInterface.instance.detachFromCameraButtonCallback(_cameraId!);
      }
      _cameraButtonEventStream = null;
    }

    // Clean up image streaming if active
    if (_frameEventSubscription != null) {
      await _frameEventSubscription!.cancel();
      _frameEventSubscription = null;
      _onImageAvailable = null;
    }

    if (_cameraFrameEventStream != null) {
      await UvcCameraPlatformInterface.instance.detachFromCameraFrameCallback(_cameraId!);
      _cameraFrameEventStream = null;
    }

    if (_cameraStatusEventStream != null) {
      if (_cameraId != null) {
        await UvcCameraPlatformInterface.instance.detachFromCameraStatusCallback(_cameraId!);
      }
      _cameraStatusEventStream = null;
    }

    if (_cameraErrorEventStream != null) {
      if (_cameraId != null) {
        await UvcCameraPlatformInterface.instance.detachFromCameraErrorCallback(_cameraId!);
      }
      _cameraErrorEventStream = null;
    }

    _textureId = null;

    if (_cameraId != null) {
      await UvcCameraPlatformInterface.instance.closeCamera(_cameraId!);
      _cameraId = null;
    }
  }

  /// Returns the camera ID.
  int get cameraId {
    _ensureInitializedNotDisposed();
    return _cameraId!;
  }

  /// Returns the texture ID.
  int get textureId {
    _ensureInitializedNotDisposed();
    return _textureId!;
  }

  /// Returns a stream of camera error events.
  Stream<UvcCameraErrorEvent> get cameraErrorEvents {
    _ensureInitializedNotDisposed();
    return _cameraErrorEventStream!;
  }

  /// Returns a stream of camera status events.
  Stream<UvcCameraStatusEvent> get cameraStatusEvents {
    _ensureInitializedNotDisposed();
    return _cameraStatusEventStream!;
  }

  /// Returns a stream of camera button events.
  Stream<UvcCameraButtonEvent> get cameraButtonEvents {
    _ensureInitializedNotDisposed();
    return _cameraButtonEventStream!;
  }

  /// Returns a stream of camera frame events containing real-time image data.
  Stream<UvcCameraFrameEvent> get cameraFrameEvents {
    _ensureInitializedNotDisposed();
    return _cameraFrameEventStream!;
  }

  /// Takes a picture.
  Future<XFile> takePicture() async {
    _ensureInitializedNotDisposed();

    if (value.isTakingPicture) {
      throw UvcCameraControllerIllegalStateException('UvcCameraController is already taking a picture');
    }

    value = value.copyWith(isTakingPicture: true);
    try {
      final XFile pictureFile = await UvcCameraPlatformInterface.instance.takePicture(_cameraId!);
      return pictureFile;
    } catch (e) {
      rethrow;
    } finally {
      value = value.copyWith(isTakingPicture: false);
    }
  }

  /// Starts video recording.
  Future<void> startVideoRecording(UvcCameraMode videoRecordingMode) async {
    _ensureInitializedNotDisposed();

    if (value.isRecordingVideo) {
      throw UvcCameraControllerIllegalStateException('UvcCameraController is already recording video');
    }

    value = value.copyWith(isRecordingVideo: true, videoRecordingMode: videoRecordingMode, videoRecordingFile: null);
    try {
      final XFile videoRecordingFile = await UvcCameraPlatformInterface.instance.startVideoRecording(
        _cameraId!,
        videoRecordingMode,
      );
      value = value.copyWith(videoRecordingFile: videoRecordingFile);
    } catch (e) {
      value = value.copyWith(isRecordingVideo: false, videoRecordingMode: null, videoRecordingFile: null);
      rethrow;
    }
  }

  /// Stops video recording.
  Future<XFile> stopVideoRecording() async {
    _ensureInitializedNotDisposed();

    if (!value.isRecordingVideo) {
      throw UvcCameraControllerIllegalStateException('UvcCameraController is not recording video');
    }

    try {
      await UvcCameraPlatformInterface.instance.stopVideoRecording(_cameraId!);

      final XFile videoRecordingFile = value.videoRecordingFile!;

      return videoRecordingFile;
    } catch (e) {
      rethrow;
    } finally {
      value = value.copyWith(isRecordingVideo: false, videoRecordingMode: null, videoRecordingFile: null);
    }
  }

  /// Starts streaming raw image data from the camera.
  ///
  /// The provided [onImageAvailable] callback will be called for each frame
  /// captured by the camera. The callback receives a [UvcCameraFrameEvent]
  /// containing the raw image data and metadata.
  ///
  /// Throws [UvcCameraControllerIllegalStateException] if the controller is
  /// already streaming images.
  Future<void> startImageStream(void Function(UvcCameraFrameEvent) onImageAvailable) async {
    _ensureInitializedNotDisposed();

    if (value.isStreamingImages) {
      throw UvcCameraControllerIllegalStateException('UvcCameraController is already streaming images');
    }

    try {
      // Store the callback
      _onImageAvailable = onImageAvailable;
      
      // Start native frame streaming
      await UvcCameraPlatformInterface.instance.attachToCameraFrameCallback(_cameraId!);
      
      // Subscribe to frame events and call the callback
      _frameEventSubscription = cameraFrameEvents.listen((frameEvent) {
        _onImageAvailable?.call(frameEvent);
      });
      
      // Update state
      value = value.copyWith(isStreamingImages: true);
    } catch (e) {
      // Clean up on error
      _onImageAvailable = null;
      await _frameEventSubscription?.cancel();
      _frameEventSubscription = null;
      rethrow;
    }
  }

  /// Stops streaming raw image data from the camera.
  ///
  /// Throws [UvcCameraControllerIllegalStateException] if the controller is
  /// not streaming images.
  Future<void> stopImageStream() async {
    _ensureInitializedNotDisposed();

    if (!value.isStreamingImages) {
      throw UvcCameraControllerIllegalStateException('UvcCameraController is not streaming images');
    }

    try {
      // Stop native frame streaming
      await UvcCameraPlatformInterface.instance.detachFromCameraFrameCallback(_cameraId!);
      
      // Cancel subscription
      await _frameEventSubscription?.cancel();
      _frameEventSubscription = null;
      
      // Clear callback
      _onImageAvailable = null;
    } catch (e) {
      rethrow;
    } finally {
      // Update state
      value = value.copyWith(isStreamingImages: false);
    }
  }

  /// Returns a widget showing a live camera preview.
  Widget buildPreview() {
    _ensureInitializedNotDisposed();

    return Texture(textureId: _textureId!);
  }

  /// Ensures that the controller is initialized and not disposed.
  void _ensureInitializedNotDisposed() {
    if (_isDisposed) {
      throw UvcCameraControllerDisposedException();
    }
    if (_initializeFuture == null) {
      throw UvcCameraControllerNotInitializedException();
    }
  }
}
