package fp.cookcorder.infrastructure

import android.content.Context
import android.media.MediaRecorder
import io.reactivex.Maybe
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface Recorder {

    fun startRecording(fileName: String): Maybe<Any>

    fun cancelRecording(): Maybe<Any>

    fun finishRecording(): Maybe<FilenameToDuration>

    data class FilenameToDuration(val fileName: String, val duration: Int)
}

class RecorderImpl @Inject constructor(private val context: Context) : Recorder {

    private var mediaRecorder: MediaRecorder? = null
    /**
     * When current record is not null, it means that there is on going recording
     */
    private var currentRecord: CurrentRecord? = null

    private data class CurrentRecord(
            val fileName: String,
            val recordStart: Long)

    override fun startRecording(fileName: String): Maybe<Any> {
        return Maybe.create { emitter ->
            synchronized(this) {
                if (currentRecord == null) {
                    val recordStart = System.currentTimeMillis()
                    try {
                        val file = File(context.filesDir, fileName)

                        mediaRecorder = MediaRecorder().apply {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                            setOutputFile(file.path)
                            prepare()
                            start()
                        }
                        currentRecord = CurrentRecord(fileName, recordStart)

                        emitter.onSuccess(Any())
                    } catch (e: Exception) {
                        Timber.e(e)
                        emitter.onComplete()
                    }
                } else {
                    Timber.d("couldn't start new recording, there is already ongoing recording")
                    emitter.onComplete()
                }
            }
        }
    }

    override fun cancelRecording(): Maybe<Any> {
        return Maybe.create { emitter ->
            synchronized(this) {
                currentRecord?.let {
                    Timber.d("Cancelling recording $currentRecord")
                    try {
                        stopMediaRecorder()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    File(context.filesDir, it.fileName).delete()
                    emitter.onSuccess(Any())
                    return@create
                }
                emitter.onComplete()
            }
        }
    }

    override fun finishRecording(): Maybe<Recorder.FilenameToDuration> {
        val maybe = Maybe.create<Recorder.FilenameToDuration> { emitter ->
            synchronized(this) {
                currentRecord?.let {
                    Timber.d("Finishing recording $currentRecord")
                    try {
                        stopMediaRecorder()
                        val duration = (System.currentTimeMillis() - it.recordStart).toInt()
                        Timber.d("Record finished successfully")
                        emitter.onSuccess(Recorder.FilenameToDuration(it.fileName, duration))
                        return@create
                    } catch (e: Exception) {
                        Timber.e(e)
                        File(context.filesDir, it.fileName).delete()
                        emitter.onError(IllegalStateException("recording hasn't been saved"))
                        return@create
                    }
                }
            }
            Timber.d("Cannot finish recording, there is no on going recording")
            emitter.onComplete()
        }

        //there a bug in media recorder, if recording is too short it crashes recording
        var shouldDelay = false
        currentRecord?.let {
            shouldDelay = System.currentTimeMillis() - it.recordStart < 1000
        }

        return if (shouldDelay) maybe.delaySubscription(1, TimeUnit.SECONDS) else maybe
    }

    private fun stopMediaRecorder() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            throw e
        } finally {
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            currentRecord = null
        }
    }
}