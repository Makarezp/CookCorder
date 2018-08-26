package fp.cookcorder.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.support.annotation.RequiresApi
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


interface Player {
    fun play(fileName: String, doAfterComplete: (() -> Unit)? = null): Observable<Pair<Int, Int>>
}

private const val STREAM_TYPE = AudioManager.STREAM_VOICE_CALL
private const val AUDIO_FOCUS_TYPE = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE

class PlayerImpl @Inject constructor(private val context: Context) : Player {

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun play(fileName: String, doAfterComplete: (() -> Unit)?): Observable<Pair<Int, Int>> {
//        val currentVolume = audioManager.getStreamVolume(STREAM_TYPE)
//        val maxVolume = audioManager.getStreamMaxVolume(STREAM_TYPE)
//        audioManager.setStreamVolume(STREAM_TYPE, maxVolume, 0)

        val focusRequest = if (SDK_INT >= 26) FocusRequestAPI26(audioManager) else
            FocusRequestBelowAPI26(audioManager)

        return focusRequest.requestAudioFocus(startPlaying(fileName))
    }


    private fun startPlaying(fileName: String): Observable<Pair<Int, Int>> {
        val mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(File(context.filesDir, fileName).path)
                prepareAsync()
                setOnPreparedListener { start() }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return Observable.just(mediaPlayer)
                .flatMap { ticks(it) }
                .takeUntil(complete(mediaPlayer))
                .doOnComplete { mediaPlayer.release() }
    }

    private fun ticks(mediaPlayer: MediaPlayer): Observable<Pair<Int, Int>> {
        return Observable.interval(16, TimeUnit.MILLISECONDS)
                .map {
                    val currentPositionInSeconds = mediaPlayer.currentPosition
                    val durationInSeconds = mediaPlayer.duration
                    Pair(currentPositionInSeconds, durationInSeconds)
                }
    }

    private fun complete(mediaPlayer: MediaPlayer): Observable<MediaPlayer> {
        return Observable.create {
            mediaPlayer.setOnCompletionListener { player ->
                it.onNext(player)
                it.onComplete()
            }
        }
    }
}

private interface FocusRequest {

    fun requestAudioFocus(startPlaying: Observable<Pair<Int, Int>>): Observable<Pair<Int, Int>>
}

private class FocusRequestAPI26(private val audioManager: AudioManager) : FocusRequest {
    @RequiresApi(26)
    override fun requestAudioFocus(startPlaying: Observable<Pair<Int, Int>>): Observable<Pair<Int, Int>> {
        requestFocusApi26().let { audioFocusRequest ->
            audioManager.requestAudioFocus(audioFocusRequest).let {
                return startPlaying.doOnComplete {
                    abandonFocusApi26(audioFocusRequest)
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

    override fun requestAudioFocus(startPlaying: Observable<Pair<Int, Int>>): Observable<Pair<Int, Int>> {
        audioManager.requestAudioFocus(audioFocusChangeListener,
                STREAM_TYPE,
                AUDIO_FOCUS_TYPE)
        return startPlaying.doOnComplete { abandonAudioFocus() }
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
}