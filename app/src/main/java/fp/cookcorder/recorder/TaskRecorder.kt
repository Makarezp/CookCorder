package fp.cookcorder.recorder

import android.content.Context
import android.media.MediaRecorder
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class TaskRecorder @Inject constructor(private val context: Context) {

    private val mediaRecorder = MediaRecorder()
    private class CurrentRecord(
            val fileName: String,
            val recordStart: Long)

    private var currentRecord: CurrentRecord? = null

    @Throws(Exception::class)
    fun startRecording(taskCount: Int) {
        val fileName = "task$taskCount"
        val recordStart = System.currentTimeMillis()
        try {
            val file = File(context.filesDir, fileName)
            with(mediaRecorder){
                reset()
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
            throw e
        }
    }

    fun cancelRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        currentRecord?.let {
            File(context.filesDir, it.fileName).delete()
        }
    }

    class FilenameToDuration(val fileName: String, val duration: Long)

    @Throws(Exception::class)
    fun finishRecording(): FilenameToDuration {
        val tempCurrRecord = currentRecord
        with(tempCurrRecord) {
            if (this != null) {
                mediaRecorder.stop()
                mediaRecorder.release()
                val duration = System.currentTimeMillis() - recordStart
                return FilenameToDuration(fileName, duration)
            } else {
                val e = IllegalStateException("Can not finish recording, currentRecordIsNull")
                Timber.e(e)
                throw e
            }
        }
    }
}