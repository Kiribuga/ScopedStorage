package com.example.scopedstorage.data

import android.net.Uri

interface DetailVideo {

    fun detailVideo(title: String, uri: String)
    fun detailVideoWithDir(url: String)
}