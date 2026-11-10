package de.jepfa.flutter_sound_bridge

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
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
    private var vibrator: Vibrator? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        setVolumeAndReInit(DEFAULT_VOLUME, AudioManager.STREAM_SYSTEM)
        channel = MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_sound_bridge")
        channel?.setMethodCallHandler(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = flutterPluginBinding.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            flutterPluginBinding.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

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
                val vibrate: Boolean? = call.argument("vibrate")
                playPattern(soundId, pattern, vibrate ?: false)
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

    private fun playPattern(soundId: Int, pattern: String, vibrate: Boolean) {
        pattern.forEach {
            when (it) {
                '|' -> {
                    if (vibrate) startVibrating(50)
                    playSysSound(soundId, 50)
                    pause(300)
                }
                '-' -> {
                    if (vibrate) startVibrating(400)
                    playSysSound(soundId, 400)
                    pause(400)
                }
                '_' -> {
                    if (vibrate) startVibrating(1000)
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
        val instance = toneGen
        if (instance != null) {
            toneGen = null
            instance.stopTone()
            instance.release()
        }
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

    private fun startVibrating(durationMillis: Long) {
        if (vibrator?.hasVibrator() == true) {
            vibrator?.vibrate(durationMillis)
        }
    }




}