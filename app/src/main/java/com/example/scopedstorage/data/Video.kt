package com.example.scopedstorage.data

import android.net.Uri

data class Video(
    val id: Long,
    val title: String,
    val size: Int,
    val uri: Uri
)