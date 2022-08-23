#import "FlutterSoundBridgePlugin.h"
#if __has_include(<flutter_sound_bridge/flutter_sound_bridge-Swift.h>)
#import <flutter_sound_bridge/flutter_sound_bridge-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_sound_bridge-Swift.h"
#endif

@implementation FlutterSoundBridgePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterSoundBridgePlugin registerWithRegistrar:registrar];
}
@end
