import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_sound_bridge/flutter_sound_bridge.dart';
import 'package:flutter_sound_bridge/flutter_sound_bridge_platform_interface.dart';
import 'package:flutter_sound_bridge/flutter_sound_bridge_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterSoundBridgePlatform 
    with MockPlatformInterfaceMixin
    implements FlutterSoundBridgePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterSoundBridgePlatform initialPlatform = FlutterSoundBridgePlatform.instance;

  test('$MethodChannelFlutterSoundBridge is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterSoundBridge>());
  });

  test('getPlatformVersion', () async {
    FlutterSoundBridge flutterSoundBridgePlugin = FlutterSoundBridge();
    MockFlutterSoundBridgePlatform fakePlatform = MockFlutterSoundBridgePlatform();
    FlutterSoundBridgePlatform.instance = fakePlatform;
  
    expect(await flutterSoundBridgePlugin.getPlatformVersion(), '42');
  });
}
