package ru.netology.albumplayer.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.netology.albumplayer.R
import ru.netology.albumplayer.adapter.TracksAdapter
import ru.netology.albumplayer.model.Track
import ru.netology.albumplayer.repository.PlayerManager
import ru.netology.albumplayer.viewmodel.AlbumViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AlbumViewModel
    private lateinit var adapter: TracksAdapter
    private val playerManager = PlayerManager()

    private lateinit var albumTitle: TextView
    private lateinit var albumArtist: TextView
    private lateinit var albumPublishedGenre: TextView
    private lateinit var albumSubtitle: TextView

    private lateinit var currentTrackTitle: TextView
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseMainButton: ImageView

    private var tracks: List<Track> = emptyList()
    private var currentTrackIndex = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Инициализация Views
        albumTitle = findViewById(R.id.albumTitle)
        albumArtist = findViewById(R.id.albumArtist)
        albumPublishedGenre = findViewById(R.id.albumPublishedGenre)
        albumSubtitle = findViewById(R.id.albumSubtitle)

        currentTrackTitle = findViewById(R.id.currentTrackTitle)
        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)
        seekBar = findViewById(R.id.seekBar)
        playPauseMainButton = findViewById(R.id.playPauseMainButton)

        // RecyclerView
        adapter = TracksAdapter { track -> playerManager.playTrack(track) }
        val recyclerView = findViewById<RecyclerView>(R.id.tracksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ViewModel
        viewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
        viewModel.album.observe(this) { album ->
            album?.let {
                tracks = it.tracks
                adapter.submitList(it.tracks)

                albumTitle.text = it.title
                albumArtist.text = it.artist
                albumPublishedGenre.text = "${it.published} • ${it.genre} • ${it.tracks.size} треков"
                albumSubtitle.text = it.subtitle
            }
        }
        viewModel.loadAlbum()

        playPauseMainButton.setOnClickListener {
            playerManager.currentTrack?.let {
                playerManager.playTrack(it)
            } ?: run {
                playerManager.playTrack(tracks.getOrNull(currentTrackIndex) ?: return@setOnClickListener)
            }
        }

        // Слушатели PlayerManager
        playerManager.onProgressUpdate = { current, total ->
            runOnUiThread {
                seekBar.max = total
                seekBar.progress = current
                currentTime.text = formatTime(current)
                totalTime.text = formatTime(total)
            }
        }

        playerManager.onPlayPauseChanged = { playing ->
            runOnUiThread {
                val icon = if (playing) R.drawable.ic_pause else R.drawable.ic_play
                playPauseMainButton.setImageResource(icon)
                adapter.isPlaying = playing
                adapter.notifyDataSetChanged()
            }
        }

        playerManager.onTrackChanged = { track ->
            runOnUiThread {
                currentTrackTitle.text = track.file
                currentTrackIndex = tracks.indexOf(track)
                adapter.currentTrackId = track.id
                adapter.notifyDataSetChanged()
            }
        }

        playerManager.onCompletion = {
            playNext()
        }

        setupSeekBar()
    }

    private fun playNext() {
        if (tracks.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % tracks.size
            playerManager.playTrack(tracks[currentTrackIndex])
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) playerManager.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
