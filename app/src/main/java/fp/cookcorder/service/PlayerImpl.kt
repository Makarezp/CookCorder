package fp.cookcorder.service

import android.content.Context
import android.media.MediaPlayer
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface Player {
    fun startPlaying(fileName: String)
}

class PlayerImpl @Inject constructor(private val context: Context) : Player {

    private val mediaPlayer = MediaPlayer()

    override fun startPlaying(fileName: String) {
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(File(context.filesDir, fileName).path)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            mediaPlayer.setOnCompletionListener { mediaPlayer.stop() }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}