package com.example.scopedstorage.ui.listvideo

import android.app.Application
import android.app.RecoverableSecurityException
import android.app.RemoteAction
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scopedstorage.data.RepositoryVideoList
import com.example.scopedstorage.data.Video
import com.example.scopedstorage.utill.haveQ
import kotlinx.coroutines.launch

class ViewModelVideoList(application: Application) : AndroidViewModel(application) {

    private val repository = RepositoryVideoList(application)

    private val allVideoMLD = MutableLiveData<List<Video>>()
    private val permissionsGrantedMLD = MutableLiveData<Boolean>()
    private val recoverableActionMLD = MutableLiveData<RemoteAction>()
    private val downloadVideoMLD = MutableLiveData<Long?>()
    private val saveUriMLD = MutableLiveData<Uri>()
    private val loadMLD = MutableLiveData<Boolean>()

    private var isObservingStarted: Boolean = false
    private var pendingDeleteId: Long? = null

    val allVideoLD: LiveData<List<Video>>
        get() = allVideoMLD

    val loadLD: LiveData<Boolean>
        get() = loadMLD

    val recoverableActionLD: LiveData<RemoteAction>
        get() = recoverableActionMLD

    val downloadVideoLD: LiveData<Long?>
        get() = downloadVideoMLD

    private fun getAllVideo() {
        loadMLD.postValue(true)
        viewModelScope.launch {
            try {
                allVideoMLD.postValue(repository.getVideo())
            } catch (t: Throwable) {
                Log.d("ViewModel", "error get all video")
                allVideoMLD.postValue(emptyList())
            } finally {
                loadMLD.postValue(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveVideo(title: String, url: String) {
        loadMLD.postValue(true)
        viewModelScope.launch {
            try {
                downloadVideoMLD.postValue(repository.saveVideo(title, url))
            } catch (t: Throwable) {
                Log.d("ViewModel", "error save video", t)
            } finally {
                loadMLD.postValue(false)
            }
        }
    }

    fun saveDownloadVideo(url: String) {
        loadMLD.postValue(true)
        viewModelScope.launch {
            try {
                downloadVideoMLD.postValue(repository.saveDownloadVideo(url, saveUriMLD.value!!))
            } catch (t: Throwable) {
                Log.d("ViewModel", "error download and save video", t)
            } finally {
                loadMLD.postValue(false)
            }
        }
    }

    fun saveUri(uri: Uri) {
        loadMLD.postValue(true)
        viewModelScope.launch {
            try {
                saveUriMLD.value = uri
                Log.d("ViewModel", "save uri = ${saveUriMLD.value}")
            } catch (t: Throwable) {
                Log.d("ViewModel", "error save uri")
            } finally {
                loadMLD.postValue(false)
            }
        }
    }

    fun deleteVideo(idVideo: Long) {
        loadMLD.postValue(true)
        viewModelScope.launch {
            try {
                repository.deleteVideo(idVideo)
                pendingDeleteId = null
            } catch (t: Throwable) {
                Log.d("ViewModel", "error delete video", t)
                if (haveQ() && t is RecoverableSecurityException) {
                    pendingDeleteId = idVideo
                    recoverableActionMLD.postValue(t.userAction)
                }
            } finally {
                loadMLD.postValue(false)
            }
        }
    }

    fun confirmDelete() {
        pendingDeleteId?.let {
            deleteVideo(it)
        }
    }

    fun declineDelete() {
        pendingDeleteId = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.unregisterObserver()
    }

    fun updatePermissionState(isGranted: Boolean) {
        if (isGranted) {
            permissionGranted()
        } else {
            permissionDenied()
        }
    }

    fun permissionGranted() {
        getAllVideo()
        if (isObservingStarted.not()) {
            repository.observeVideo { getAllVideo() }
            isObservingStarted = true
        }
        permissionsGrantedMLD.postValue(true)
    }

    fun permissionDenied() {
        permissionsGrantedMLD.postValue(false)
    }
}