import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_sound_bridge_platform_interface.dart';

/// An implementation of [FlutterSoundBridgePlatform] that uses method channels.
class MethodChannelFlutterSoundBridge extends FlutterSoundBridgePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_sound_bridge');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
