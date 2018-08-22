package fp.cookcorder.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.support.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface Player {
    fun play(fileName: String)
}

private const val STREAM_TYPE = AudioManager.STREAM_VOICE_CALL
private const val AUDIO_FOCUS_TYPE = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE

class PlayerImpl @Inject constructor(private val context: Context) : Player {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun play(fileName: String) {
        val currentVolume = audioManager.getStreamVolume(STREAM_TYPE)
        val maxVolume = audioManager.getStreamMaxVolume(STREAM_TYPE)
        audioManager.setStreamVolume(STREAM_TYPE, maxVolume, 0)

        val focusRequest = if (SDK_INT >= 26) FocusRequestAPI26(audioManager) else
            FocusRequestBelowAPI26(audioManager)

        focusRequest.requestAudioFocus { onComplete: () -> Unit ->
            startPlaying(fileName) {
                onComplete()
                audioManager.setStreamVolume(STREAM_TYPE, currentVolume, 0)
            }
        }
    }


    private fun startPlaying(fileName: String, onComplete: () -> Unit) {
        MediaPlayer().apply {
            try {
                setDataSource(File(context.filesDir, fileName).path)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    it.release()
                    onComplete()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

private interface FocusRequest {

    fun requestAudioFocus(startPlaying: (() -> Unit) -> Unit)
}

private class FocusRequestAPI26(private val audioManager: AudioManager) : FocusRequest {
    @RequiresApi(26)
    override fun requestAudioFocus(startPlaying: (() -> Unit) -> Unit) {
        requestFocusApi26().let { audioFocusRequest ->
            audioManager.requestAudioFocus(audioFocusRequest).let {
                if (it == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    startPlaying {
                        abandonFocusApi26(audioFocusRequest)
                    }
                }
            }
        }
    }

    @RequiresApi(26)
    private fun requestFocusApi26(): AudioFocusRequest {
        return AudioAttributes.Builder().run {
            setLegacyStreamType(STREAM_TYPE)
            build()
        }.let { audioAttributes ->
            AudioFocusRequest.Builder(AUDIO_FOCUS_TYPE).run {
                setAudioAttributes(audioAttributes)

                build()
            }
        }
    }

    @RequiresApi(26)
    private fun abandonFocusApi26(audioFocusRequest: AudioFocusRequest) {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }
}

private class FocusRequestBelowAPI26(private val audioManager: AudioManager) : FocusRequest {

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { }

    override fun requestAudioFocus(startPlaying: (() -> Unit) -> Unit) {
        audioManager.requestAudioFocus(audioFocusChangeListener,
                STREAM_TYPE,
                AUDIO_FOCUS_TYPE)
        startPlaying { abandonAudioFocus() }
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
}