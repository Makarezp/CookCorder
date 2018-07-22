package fp.cookcorder.infrastructure

import android.content.Context
import android.media.MediaPlayer
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface Player {
    fun startPlaying(fileName: String)
}

class PlayerImpl @Inject constructor(private val context: Context) : Player {

    override fun startPlaying(fileName: String) {
        MediaPlayer().apply {
            try {
                setDataSource(File(context.filesDir, fileName).path)
                prepareAsync()
                setOnPreparedListener { it.start() }
                setOnCompletionListener { it.release() }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}