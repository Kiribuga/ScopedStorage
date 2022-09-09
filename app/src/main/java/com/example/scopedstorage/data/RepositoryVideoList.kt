package com.example.scopedstorage.data

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.scopedstorage.data.networking.Network
import com.example.scopedstorage.utill.haveQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryVideoList(
    private val context: Context
) {

    private var observer: ContentObserver? = null

    fun observeVideo(onChange: () -> Unit) {
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                onChange()
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )
    }

    fun unregisterObserver() {
        observer?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
    }

    @SuppressLint("Range")
    suspend fun getVideo(): List<Video> {
        val video = mutableListOf<Video>()
        withContext(Dispatchers.IO) {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                    val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    video += Video(id, title, size, uri)
                }
            }
        }
        return video
    }

    suspend fun deleteVideo(idVideo: Long) {
        withContext(Dispatchers.IO) {
            val uri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, idVideo)
            context.contentResolver.delete(uri, null, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveVideo(title: String, url: String): Long? {
        var result: Long?
        withContext(Dispatchers.IO) {
            val videoUri = saveVideoDetails(title)
            result = downloadVideo(url, videoUri)
            makeVideoVisible(videoUri)
        }
        return result
    }

    suspend fun saveDownloadVideo(url: String, uri: Uri): Long? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)
                ?.use {
                    downloadVideo(url, uri)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveVideoDetails(title: String): Uri {
        val volume = if (haveQ()) {
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        } else {
            MediaStore.VOLUME_EXTERNAL
        }
        val videoCollectionUri = MediaStore.Video.Media.getContentUri(volume)
        val videoDetails = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, title)
            put(MediaStore.Video.Media.MIME_TYPE, "video/*")
            if (haveQ()) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        return context.contentResolver.insert(videoCollectionUri, videoDetails)!!
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun makeVideoVisible(videoUri: Uri) {
        if (haveQ().not()) return
        val videoDetails = ContentValues().apply {
            put(MediaStore.Video.Media.IS_PENDING, 0)
        }
        context.contentResolver.update(videoUri, videoDetails, null, null)
    }

    private suspend fun downloadVideo(url: String, uri: Uri): Long? {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                Network.api
                    .getFile(url)
                    .byteStream()
                    .use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
            }
        } catch (t: Throwable) {
            context.contentResolver.delete(uri, null, null)
            0
        }
    }
}