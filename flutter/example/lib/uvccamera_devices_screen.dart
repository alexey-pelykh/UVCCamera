import 'dart:async';

import 'package:flutter/material.dart';
import 'package:uvccamera/uvccamera.dart';

import 'uvccamera_device_screen.dart';

class UvcCameraDevicesScreen extends StatefulWidget {
  const UvcCameraDevicesScreen({super.key});

  @override
  State<UvcCameraDevicesScreen> createState() => _UvcCameraDevicesScreenState();
}

class _UvcCameraDevicesScreenState extends State<UvcCameraDevicesScreen> {
  bool _isSupported = false;
  StreamSubscription<UvcCameraDeviceEvent>? _deviceEventSubscription;
  final Map<String, UvcCameraDevice> _devices = {};

  @override
  void initState() {
    super.initState();

    UvcCamera.isSupported().then((value) {
      setState(() {
        _isSupported = value;
      });
    });

    _deviceEventSubscription = UvcCamera.deviceEventStream.listen((event) {
      setState(() {
        if (event.type == UvcCameraDeviceEventType.attached) {
          _devices[event.device.name] = event.device;
        } else if (event.type == UvcCameraDeviceEventType.detached) {
          _devices.remove(event.device.name);
        }
      });
    });

    UvcCamera.getDevices().then((devices) {
      setState(() {
        _devices.addAll(devices);
      });
    });
  }

  @override
  void dispose() {
    _deviceEventSubscription?.cancel();
    _deviceEventSubscription = null;

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isSupported) {
      return const Center(
        child: Text(
          'UVC Camera is not supported on this device.',
          style: TextStyle(fontSize: 18),
        ),
      );
    }

    if (_devices.isEmpty) {
      return const Center(
        child: Text(
          'No UVC devices connected.',
          style: TextStyle(fontSize: 18),
        ),
      );
    }

    return ListView(
      children: _devices.values.map((device) {
        return ListTile(
          leading: const Icon(Icons.videocam),
          title: Text(device.name),
          subtitle: Text('Vendor ID: ${device.vendorId}, Product ID: ${device.productId}'),
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => UvcCameraDeviceScreen(device: device),
              ),
            );
          },
        );
      }).toList(),
    );
  }
}
