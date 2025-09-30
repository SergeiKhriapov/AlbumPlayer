package ru.netology.albumplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.albumplayer.model.Album
import ru.netology.albumplayer.repository.AlbumRepository

class AlbumViewModel : ViewModel() {
    private val _album = MutableLiveData<Album?>()
    val album: LiveData<Album?> = _album

    fun loadAlbum() {
        AlbumRepository.loadAlbum {
            _album.postValue(it)
        }
    }
}