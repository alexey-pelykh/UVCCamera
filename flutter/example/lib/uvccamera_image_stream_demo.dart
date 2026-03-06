import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

/// Demo widget that shows how to use real-time image stream from UVC camera
class UvcCameraImageStreamDemo extends StatefulWidget {
  final UvcCameraDevice device;

  const UvcCameraImageStreamDemo({super.key, required this.device});

  @override
  State<UvcCameraImageStreamDemo> createState() => _UvcCameraImageStreamDemoState();
}

class _UvcCameraImageStreamDemoState extends State<UvcCameraImageStreamDemo> {
  UvcCameraController? _cameraController;
  StreamSubscription<UvcCameraFrameEvent>? _frameEventSubscription;
  bool _isStreamingImages = false;
  int _frameCount = 0;
  String _lastFrameInfo = '';
  DateTime? _lastFrameTime;
  double _fps = 0.0;
  bool _isInitialized = false;

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  @override
  void dispose() {
    _stopImageStream();
    _cameraController?.dispose();
    super.dispose();
  }

  Future<void> _initializeCamera() async {
    try {
      _cameraController = UvcCameraController(device: widget.device);
      await _cameraController!.initialize();
      
      setState(() {
        _isInitialized = true;
      });
    } catch (e) {
      debugPrint('Error initializing camera: $e');
      setState(() {
        _lastFrameInfo = 'Error initializing camera: $e';
      });
    }
  }

  void _startImageStream() {
    if (_cameraController == null || !_isInitialized) {
      return;
    }

    setState(() {
      _isStreamingImages = true;
      _frameCount = 0;
      _lastFrameTime = DateTime.now();
    });

    _frameEventSubscription = _cameraController!.cameraFrameEvents.listen(
      (UvcCameraFrameEvent frameEvent) {
        _handleFrameEvent(frameEvent);
      },
      onError: (error) {
        debugPrint('Frame stream error: $error');
        setState(() {
          _lastFrameInfo = 'Frame stream error: $error';
        });
      },
    );
  }

  void _stopImageStream() {
    _frameEventSubscription?.cancel();
    _frameEventSubscription = null;
    
    setState(() {
      _isStreamingImages = false;
      _fps = 0.0;
    });
  }

  void _handleFrameEvent(UvcCameraFrameEvent frameEvent) {
    setState(() {
      _frameCount++;
      
      // Calculate FPS
      final now = DateTime.now();
      if (_lastFrameTime != null) {
        final timeDiff = now.difference(_lastFrameTime!).inMilliseconds;
        if (timeDiff > 0) {
          _fps = 1000.0 / timeDiff;
        }
      }
      _lastFrameTime = now;

      // Update frame information
      _lastFrameInfo = 'Frame #$_frameCount\n'
          'Size: ${frameEvent.width}x${frameEvent.height}\n'
          'Format: ${frameEvent.format}\n'
          'Data Size: ${frameEvent.dataSize} bytes\n'
          'Aspect Ratio: ${frameEvent.aspectRatio.toStringAsFixed(2)}\n'
          'FPS: ${_fps.toStringAsFixed(1)}\n'
          'Timestamp: ${DateTime.fromMillisecondsSinceEpoch(frameEvent.timestamp)}';
    });

    // Example: Process image data here
    _processImageData(frameEvent.imageData, frameEvent.width, frameEvent.height, frameEvent.format);
  }

  void _processImageData(Uint8List imageData, int width, int height, String format) {
    // Example processing - you can implement your own image processing here
    // For example:
    // - Convert to different formats
    // - Apply filters
    // - Save to file
    // - Send to ML model
    // - etc.
    
    debugPrint('Processing image: ${width}x${height}, format: $format, size: ${imageData.length} bytes');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Image Stream Demo - ${widget.device.name}'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Camera Preview
            if (_isInitialized && _cameraController != null)
              Container(
                height: 200,
                width: double.infinity,
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: UvcCameraPreview(_cameraController!),
                ),
              ),
            
            const SizedBox(height: 16),
            
            // Control Buttons
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _isInitialized && !_isStreamingImages ? _startImageStream : null,
                  child: const Text('Start Stream'),
                ),
                ElevatedButton(
                  onPressed: _isStreamingImages ? _stopImageStream : null,
                  child: const Text('Stop Stream'),
                ),
              ],
            ),
            
            const SizedBox(height: 16),
            
            // Stream Status
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: _isStreamingImages ? Colors.green.withOpacity(0.1) : Colors.grey.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: _isStreamingImages ? Colors.green : Colors.grey,
                ),
              ),
              child: Row(
                children: [
                  Icon(
                    _isStreamingImages ? Icons.play_circle_filled : Icons.pause_circle_filled,
                    color: _isStreamingImages ? Colors.green : Colors.grey,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    _isStreamingImages ? 'Streaming Active' : 'Streaming Stopped',
                    style: TextStyle(
                      color: _isStreamingImages ? Colors.green : Colors.grey,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Frame Information
            Expanded(
              child: Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.grey.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.grey),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Frame Information:',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                    ),
                    const SizedBox(height: 8),
                    Expanded(
                      child: SingleChildScrollView(
                        child: Text(
                          _lastFrameInfo.isEmpty ? 'No frame data yet...' : _lastFrameInfo,
                          style: const TextStyle(fontFamily: 'monospace', fontSize: 12),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Instructions
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue),
              ),
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Instructions:',
                    style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue),
                  ),
                  SizedBox(height: 4),
                  Text(
                    '1. Click "Start Stream" to begin receiving real-time image data\n'
                    '2. Frame information will update in real-time\n'
                    '3. Use _processImageData() method to process the image data\n'
                    '4. Click "Stop Stream" to stop receiving frames',
                    style: TextStyle(fontSize: 12, color: Colors.blue),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
