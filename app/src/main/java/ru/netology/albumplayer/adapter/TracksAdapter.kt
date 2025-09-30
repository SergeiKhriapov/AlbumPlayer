package ru.netology.albumplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.albumplayer.R
import ru.netology.albumplayer.model.Track

class TracksAdapter(
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, TracksAdapter.TrackViewHolder>(DiffCallback()) {

    var currentTrackId: Int? = null
    var isPlaying: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view, onTrackClick)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track, track.id == currentTrackId && isPlaying)
    }

    class TrackViewHolder(
        itemView: View,
        private val onTrackClick: (Track) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val trackTitle: TextView = itemView.findViewById(R.id.trackTitle)
        private val playPauseBtn: ImageView = itemView.findViewById(R.id.playPauseButton)

        fun bind(track: Track, isPlaying: Boolean) {
            trackTitle.text = track.file
            playPauseBtn.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            itemView.setOnClickListener { onTrackClick(track) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}
