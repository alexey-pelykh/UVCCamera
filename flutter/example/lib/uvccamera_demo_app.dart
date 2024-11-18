import 'package:flutter/material.dart';

import 'uvccamera_devices_screen.dart';

class UvcCameraDemoApp extends StatefulWidget {
  const UvcCameraDemoApp({super.key});

  @override
  State<UvcCameraDemoApp> createState() => _UvcCameraDemoAppState();
}

class _UvcCameraDemoAppState extends State<UvcCameraDemoApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'UVC Camera Example',
      home: Scaffold(
        appBar: AppBar(
          title: const Text('UVC Camera Example'),
        ),
        body: UvcCameraDevicesScreen(),
      ),
    );
  }
}
