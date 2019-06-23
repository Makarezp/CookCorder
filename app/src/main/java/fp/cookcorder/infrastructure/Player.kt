package fp.cookcorder.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.support.annotation.RequiresApi
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

typealias Progress = Int

interface Player {
    fun play(fileName: String, repeats: Int): Observable<Progress>
    fun stopPlaying(fileName: String): Single<Any>
}

private const val STREAM_TYPE = AudioManager.STREAM_MUSIC
private const val AUDIO_FOCUS_TYPE = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE

class PlayerImpl @Inject constructor(private val context: Context) : Player {

    private var mediaPlayerCache = emptyMap<String, MediaPlayer>()

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun play(fileName: String, repeats: Int): Observable<Progress> {
//        val currentVolume = audioManager.getStreamVolume(STREAM_TYPE)
//        val maxVolume = audioManager.getStreamMaxVolume(STREAM_TYPE)
//        audioManager.setStreamVolume(STREAM_TYPE, maxVolume, 0)

        val focusRequest = if (SDK_INT >= 26) FocusRequestAPI26(audioManager) else
            FocusRequestBelowAPI26(audioManager)

        return focusRequest.requestAudioFocus(startPlaying(fileName, repeats))
    }


    private fun startPlaying(fileName: String, repeats: Int): Observable<Int> {
        val mediaPlayer = MediaPlayer()

        return Observable.just(mediaPlayer)
                .doAfterNext { mediaPlayerCache = mediaPlayerCache.plus(fileName to it) }
                .flatMap {
                    var counter = 0
                    var proceed = true
                    Observable.create<MediaPlayer> {
                        mediaPlayer.setOnCompletionListener { player ->
                            counter++
                            if (counter < repeats) {
                                it.onNext(player)
                            } else {
                                proceed = false
                                it.onComplete()
                            }
                        }
                        it.onNext(mediaPlayer)
                    }.flatMap { mediaPlayer ->
                        startPlaying(mediaPlayer, fileName)
                    }
                            .flatMap { ticks(it) }
                            .takeWhile { proceed }
                }
                .doFinally {
                    mediaPlayerCache = mediaPlayerCache.minus(fileName)
                    mediaPlayer.release()
                }

    }

    private fun startPlaying(mediaPlayer: MediaPlayer, fileName: String): Observable<MediaPlayer>? {
        return Observable.create<MediaPlayer> {
            with(mediaPlayer) {
                reset()
                setDataSource(File(context.filesDir, fileName).path)
                prepareAsync()
                setOnPreparedListener { mediaPlayer ->
                    start()
                    it.onNext(mediaPlayer)
                    it.onComplete()
                }
            }
        }
    }

    private fun ticks(mediaPlayer: MediaPlayer): Observable<Int> {
        var previousPosition = 0
        return Observable.interval(16, TimeUnit.MILLISECONDS)
                .map {
                    try {
                        if (mediaPlayer.isPlaying) {
                            val currentPositionInSeconds = mediaPlayer.currentPosition
                            previousPosition = currentPositionInSeconds
                        }
                        previousPosition
                    } catch (e: Exception) {
                        previousPosition
                    }
                }
    }

    override fun stopPlaying(fileName: String): Single<Any> {
        return try {
            val mediaPlayer = mediaPlayerCache[fileName] ?: throw NullPointerException()
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayerCache = mediaPlayerCache.minus(fileName)
            Single.just(true)
        } catch (e: Exception) {
            Single.just(e)
        }
    }
}

private interface FocusRequest {

    fun requestAudioFocus(startPlaying: Observable<Int>): Observable<Int>
}

private class FocusRequestAPI26(private val audioManager: AudioManager) : FocusRequest {
    @RequiresApi(26)
    override fun requestAudioFocus(startPlaying: Observable<Int>): Observable<Int> {
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

    override fun requestAudioFocus(startPlaying: Observable<Int>): Observable<Int> {
        audioManager.requestAudioFocus(audioFocusChangeListener,
                STREAM_TYPE,
                AUDIO_FOCUS_TYPE)
        return startPlaying.doOnComplete { abandonAudioFocus() }
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
}