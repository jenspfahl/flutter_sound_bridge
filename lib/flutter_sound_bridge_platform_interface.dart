import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_sound_bridge_method_channel.dart';

abstract class FlutterSoundBridgePlatform extends PlatformInterface {
  /// Constructs a FlutterSoundBridgePlatform.
  FlutterSoundBridgePlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterSoundBridgePlatform _instance = MethodChannelFlutterSoundBridge();

  /// The default instance of [FlutterSoundBridgePlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterSoundBridge].
  static FlutterSoundBridgePlatform get instance => _instance;
  
  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterSoundBridgePlatform] when
  /// they register themselves.
  static set instance(FlutterSoundBridgePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
