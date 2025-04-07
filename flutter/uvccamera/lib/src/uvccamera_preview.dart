import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'uvccamera_controller.dart';
import 'uvccamera_controller_state.dart';

class UvcCameraPreview extends StatelessWidget {
  final UvcCameraController controller;
  final Widget? child;

  const UvcCameraPreview(this.controller, {super.key, this.child});

  @override
  Widget build(BuildContext context) {
    if (!controller.value.isInitialized) {
      return Container();
    }

    return ValueListenableBuilder<UvcCameraControllerState>(
      valueListenable: controller,
      builder: (BuildContext context, Object? value, Widget? child) {
        return AspectRatio(
          aspectRatio: controller.value.previewMode!.aspectRatio,
          child: Stack(fit: StackFit.expand, children: <Widget>[controller.buildPreview(), child ?? Container()]),
        );
      },
      child: child,
    );
  }
}
