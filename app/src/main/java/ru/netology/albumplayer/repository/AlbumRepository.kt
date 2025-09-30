package ru.netology.albumplayer.repository

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.netology.albumplayer.model.Album
import java.io.IOException

object AlbumRepository {
    private const val BASE_URL = "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"

    fun loadAlbum(callback: (Album?) -> Unit) {
        val request = Request.Builder()
            .url(BASE_URL + "album.json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val album = Gson().fromJson(it, Album::class.java)
                    callback(album)
                }
            }
        })
    }

    fun getTrackUrl(file: String): String = BASE_URL + file
}