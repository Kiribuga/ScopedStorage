package com.example.scopedstorage.data

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.scopedstorage.R
import com.example.scopedstorage.utill.inflate

class AdapterVideo(
    private val onItemClick: (video: Video) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var video: List<Video> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoHolder(
            parent.inflate(R.layout.item_video)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoHolder -> {
                val video = video[position]
                holder.bind(video)
                holder.itemView.findViewById<Button>(R.id.removeVideo).setOnClickListener {
                    onItemClick.invoke(video)
                }
            }
        }
    }

    override fun getItemCount(): Int = video.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Video>) {
        video = newList
        notifyDataSetChanged()
    }

    abstract class BaseHolder(
        private val containerView: View
    ) : RecyclerView.ViewHolder(containerView) {

        private val titleVideo: TextView = itemView.findViewById(R.id.titleVideo)
        private val sizeVideo: TextView = itemView.findViewById(R.id.sizeVideo)
        private val previewVideo: ImageView = itemView.findViewById(R.id.previewVideo)

        @SuppressLint("SetTextI18n")
        protected fun bindMainInfo(
            title: String,
            size: Int,
            preview: Uri
        ) {
            titleVideo.text = title
            sizeVideo.text = "$size bytes"

            Glide.with(itemView)
                .load(preview)
                .placeholder(R.drawable.video_24)
                .into(previewVideo)
        }
    }

    class VideoHolder(
        containerView: View
    ) : BaseHolder(containerView) {
        fun bind(video: Video) {
            bindMainInfo(video.title, video.size, video.uri)
        }
    }
}