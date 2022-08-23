package de.jepfa.flutter_sound_bridge

import android.media.AudioManager
import android.media.ToneGenerator
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** adapted from FlutterBeepPlugin https://pub.dev/packages/flutter_beep  */
class FlutterSoundBridgePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private val DEFAULT_VOLUME = 100

    private var toneGen: ToneGenerator? = null
    private var channel: MethodChannel? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        setVolumeAndReInit(DEFAULT_VOLUME)
        channel = MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_sound_bridge")
        channel?.setMethodCallHandler(this)
    }
    
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "setVolume" -> {
                val volume: Int? = call.argument("volume")
                setVolumeAndReInit(volume ?: DEFAULT_VOLUME)
                result.success(true)
            }
            "playSysSound" -> {
                val soundId: Int? = call.argument("soundId")
                if (soundId == null) {
                    result.error("1", "soundId must not be set", null)
                    return
                }
                val durationMs: Int? = call.argument("durationMs")
                playSysSound(soundId, durationMs ?: -1)
                result.success(true)
            }
            "stopSysSound" -> {
                stopSysSound()
                result.success(true)
            }
            "release" -> {
                release()
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

    private fun playSysSound(soundID: Int, durationMs: Int) {
        toneGen?.startTone(soundID, durationMs)
    }

    private fun stopSysSound() {
        toneGen?.stopTone()
    }

    private fun setVolumeAndReInit(volume: Int) {
        toneGen?.release() // release old toneGen
        toneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, volume)
    }

    private fun release() {
        toneGen?.stopTone()
        toneGen?.release()
    }

}