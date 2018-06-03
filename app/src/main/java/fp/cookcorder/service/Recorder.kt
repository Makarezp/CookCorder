package fp.cookcorder.service

import android.content.Context
import android.media.MediaRecorder
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface Recorder {
    fun startRecording(fileName: String)

    fun cancelRecording()

    fun finishRecording(): FilenameToDuration?

    class FilenameToDuration(val fileName: String, val duration: Long)
}

class RecorderImpl @Inject constructor(private val context: Context) : Recorder {

    private var mediaRecorder: MediaRecorder? = null

    private data class CurrentRecord(
            val fileName: String,
            val recordStart: Long)

    private var currentRecord: CurrentRecord? = null

    override fun startRecording(fileName: String) {
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
    }

    override fun cancelRecording() {
        currentRecord?.let {
            try {
                Timber.d("Cancelling recording $currentRecord")
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                File(context.filesDir, it.fileName).delete()
                currentRecord = null
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    @Throws(Exception::class)
    override fun finishRecording(): Recorder.FilenameToDuration? {
        currentRecord?.let {
            Timber.d("Finishing recording $currentRecord")
            mediaRecorder?.stop()
            mediaRecorder?.release()
            val duration = System.currentTimeMillis() - it.recordStart
            currentRecord = null
            mediaRecorder = null
            return Recorder.FilenameToDuration(it.fileName, duration)
        }
        return null
    }
}