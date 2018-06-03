package fp.cookcorder.recorder

import android.content.Context
import android.media.MediaRecorder
import fp.cookcorder.model.Task
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class TaskRecorder @Inject constructor(private val context: Context) {

    private class CurrentRecord(
            val mediaRecorder: MediaRecorder,
            val fileName: String,
            val recordStart: Long)

    private var currentRecord: CurrentRecord? = null

    @Throws(Exception::class)
    fun startRecording(taskCount: Int) {
        val fileName = "task$taskCount"
        val recordStart = System.currentTimeMillis()
        try {
            val file = File(context.filesDir, fileName)
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.path)
                prepare()
                start()
            }
            currentRecord = CurrentRecord(recorder, fileName, recordStart)
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }

    @Throws(Exception::class)
    fun finishRecording(): Task {
        val tempCurrRecord = currentRecord
        with(tempCurrRecord) {
            if (this != null) {
                mediaRecorder.stop()
                mediaRecorder.release()
                val duration = System.currentTimeMillis() - recordStart
                return Task(null, fileName, duration)
            } else {
                val e = IllegalStateException("Can not finish recording, currentRecordIsNull")
                Timber.e(e)
                throw e
            }
        }
    }
}