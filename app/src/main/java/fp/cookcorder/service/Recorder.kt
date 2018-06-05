package fp.cookcorder.service

import android.content.Context
import android.media.MediaRecorder
import io.reactivex.Maybe
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface Recorder {
    fun startRecording(fileName: String)

    fun cancelRecording()

    fun finishRecording(): Maybe<FilenameToDuration>

    data class FilenameToDuration(val fileName: String, val duration: Long)
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


    override fun startRecording(fileName: String) {
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
            } catch (e: Exception) {
                Timber.e(e)
            }
        } else {
            Timber.d("couldn't start new recording, there is already ongoing recording")
        }
    }

    override fun cancelRecording() {
        currentRecord?.let {
            Timber.d("Cancelling recording $currentRecord")
            try {
                stopMediaRecorder()
            } catch (e: Exception) {
                Timber.e(e)
            }
            File(context.filesDir, it.fileName).delete()
        }
    }

    override fun finishRecording(): Maybe<Recorder.FilenameToDuration> {
        return Maybe.create<Recorder.FilenameToDuration> { emitter ->
            currentRecord?.let {
                Timber.d("Finishing recording $currentRecord")
                try {
                    stopMediaRecorder()
                    val duration = System.currentTimeMillis() - it.recordStart
                    Timber.d("Record is finished successfully")
                    emitter.onSuccess(Recorder.FilenameToDuration(it.fileName, duration))
                    return@create
                } catch (e: Exception) {
                    Timber.d(e)
                    File(context.filesDir, it.fileName).delete()
                    emitter.onComplete()
                    return@create
                }
            }
            Timber.d("Cannot finish recording, there is no on going recording")
            emitter.onComplete()
        }.delaySubscription(1, TimeUnit.SECONDS)
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