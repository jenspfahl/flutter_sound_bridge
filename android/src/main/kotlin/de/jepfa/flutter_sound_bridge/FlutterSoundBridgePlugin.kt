package de.jepfa.flutter_sound_bridge

import android.media.AudioManager
import android.media.ToneGenerator
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** adapted from FlutterBeepPlugin https://pub.dev/packages/flutter_beep  */
class FlutterSoundBridgePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private val DEFAULT_VOLUME = 100

    private var toneGen: ToneGenerator? = null
    private var channel: MethodChannel? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        setVolumeAndReInit(DEFAULT_VOLUME, AudioManager.STREAM_SYSTEM)
        channel = MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_sound_bridge")
        channel?.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        CoroutineScope(Dispatchers.IO).launch {
            perform(call, result)
        }
    }

    private fun perform(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        when (call.method) {
            "setVolume" -> {
                val volume: Int = call.argument("volume") ?: DEFAULT_VOLUME
                val audioStream: Int = call.argument("audioStream") ?: AudioManager.STREAM_SYSTEM
                setVolumeAndReInit(volume, audioStream)
                result.success(true)
            }

            "playPattern" -> {
                val soundId: Int? = call.argument("soundId")
                if (soundId == null) {
                    result.error("1", "soundId must be set", null)
                    return
                }
                val pattern: String? = call.argument("pattern")
                if (pattern == null) {
                    result.error("2", "pattern must be set", null)
                    return
                }
                playPattern(soundId, pattern)
                result.success(true)
            }

            "playSysSound" -> {
                val soundId: Int? = call.argument("soundId")
                if (soundId == null) {
                    result.error("1", "soundId must be set", null)
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

            "pause" -> {
                val durationMs: Int? = call.argument("durationMs")
                pause(durationMs ?: 0)
                result.success(true)
            }

            "release" -> {
                release()
                result.success(true)
            }

            else -> result.notImplemented()
        }
    }

    private fun playPattern(soundId: Int, pattern: String) {
        pattern.forEach {
            when (it) {
                '|' -> {
                    playSysSound(soundId, 50)
                    pause(300)
                }
                '-' -> {
                    playSysSound(soundId, 400)
                    pause(400)
                }
                '_' -> {
                    playSysSound(soundId, 1000)
                    pause(400)
                }
                ' ' -> pause(1000)
            }

        }

    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        release()
        channel?.setMethodCallHandler(null)
    }

    private fun playSysSound(soundID: Int, durationMs: Int) {
        toneGen?.startTone(soundID, durationMs)
        accurateSleep(durationMs.toLong())
    }

    private fun pause(durationMs: Int) {
        stopSysSound()
        accurateSleep(durationMs.toLong())
    }

    private fun stopSysSound() {
        toneGen?.stopTone()
    }

    private fun setVolumeAndReInit(volume: Int, audioStream: Int) {
        toneGen?.release() // release old toneGen
        toneGen = null
        if (volume > 0) {
            toneGen = ToneGenerator(audioStream, volume)
        }
    }

    private fun release() {
        toneGen?.stopTone()
        toneGen?.release()
        toneGen = null
    }

    private fun accurateSleep(durationMillis: Long) {
        if (durationMillis >= 400) {
            Thread.sleep(durationMillis);
        }
        else {
            val startTime = System.nanoTime()
            val durationNanos = durationMillis * 1_000_000
            while ((System.nanoTime() - startTime) < durationNanos) {
                Thread.yield()
            }
        }
    }



}