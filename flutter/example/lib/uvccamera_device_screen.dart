import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

import 'uvccamera_image_stream_demo.dart';
import 'uvccamera_widget.dart';

class UvcCameraDeviceScreen extends StatelessWidget {
  final UvcCameraDevice device;

  const UvcCameraDeviceScreen({super.key, required this.device});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(device.name),
        actions: [
          IconButton(
            icon: const Icon(Icons.camera_alt),
            tooltip: 'Image Stream Demo',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => UvcCameraImageStreamDemo(device: device),
                ),
              );
            },
          ),
        ],
      ),
      body: Center(child: UvcCameraWidget(device: device)),
    );
  }
}
