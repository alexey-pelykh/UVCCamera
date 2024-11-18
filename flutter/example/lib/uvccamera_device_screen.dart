import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

import 'uvccamera_widget.dart';

class UvcCameraDeviceScreen extends StatelessWidget {
  final UvcCameraDevice device;

  const UvcCameraDeviceScreen({super.key, required this.device});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(device.name),
      ),
      body: Center(
        child: UvcCameraWidget(device: device),
      ),
    );
  }
}
