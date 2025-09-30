package ru.netology.albumplayer.repository

import android.media.MediaPlayer
import ru.netology.albumplayer.model.Track
import java.util.Timer
import java.util.TimerTask

class PlayerManager {

    private var mediaPlayer: MediaPlayer? = null
    private var seekBarTimer: Timer? = null

    var currentTrack: Track? = null
        private set
    var isPlaying: Boolean = false
        private set

    var onProgressUpdate: ((current: Int, total: Int) -> Unit)? = null
    var onTrackChanged: ((track: Track) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null
    var onPlayPauseChanged: ((isPlaying: Boolean) -> Unit)? = null

    fun playTrack(track: Track) {
        if (currentTrack?.id == track.id) {
            if (isPlaying) pause() else start()
            return
        }

        mediaPlayer?.release()
        seekBarTimer?.cancel()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(AlbumRepository.getTrackUrl(track.file))
            prepareAsync()
            setOnPreparedListener {
                start()
                currentTrack = track
                this@PlayerManager.isPlaying = true
                onTrackChanged?.invoke(track)
                onPlayPauseChanged?.invoke(true)
                setupSeekBar()
            }
            setOnCompletionListener {
                this@PlayerManager.isPlaying = false
                onPlayPauseChanged?.invoke(false)
                onCompletion?.invoke()
            }
        }
    }

    private fun start() {
        mediaPlayer?.start()
        isPlaying = true
        onPlayPauseChanged?.invoke(true)
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlaying = false
        onPlayPauseChanged?.invoke(false)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun setupSeekBar() {
        mediaPlayer?.let { player ->
            seekBarTimer?.cancel()
            seekBarTimer = Timer()
            seekBarTimer?.schedule(object : TimerTask() {
                override fun run() {
                    onProgressUpdate?.invoke(player.currentPosition, player.duration)
                }
            }, 0, 500)
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        seekBarTimer?.cancel()
        seekBarTimer = null
        currentTrack = null
        isPlaying = false
    }
}
