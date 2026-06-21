package com.parag.khallas.viewer

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parag.khallas.databinding.PageImageBinding
import com.parag.khallas.databinding.PageVideoBinding
import com.parag.khallas.util.FileUtils
import java.io.File

private const val TYPE_IMAGE = 0
private const val TYPE_VIDEO = 1

class MediaPagerAdapter(
    private val paths: MutableList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ImageVH(val binding: PageImageBinding) : RecyclerView.ViewHolder(binding.root)
    inner class VideoVH(val binding: PageVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val file = File(paths[position])
        return if (FileUtils.isVideo(file)) TYPE_VIDEO else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_VIDEO) {
            VideoVH(PageVideoBinding.inflate(inflater, parent, false))
        } else {
            ImageVH(PageImageBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val path = paths[position]
        when (holder) {
            is ImageVH -> {
                Glide.with(holder.itemView.context)
                    .load(File(path))
                    .fitCenter()
                    .into(holder.binding.imageView)
            }
            is VideoVH -> {
                val videoView = holder.binding.videoView
                val hint = holder.binding.tapHint
                videoView.setVideoURI(Uri.fromFile(File(path)))
                videoView.setOnPreparedListener { mp -> mp.isLooping = true }
                var playing = false
                videoView.setOnClickListener {
                    if (playing) {
                        videoView.pause()
                        hint.visibility = View.VISIBLE
                    } else {
                        videoView.start()
                        hint.visibility = View.GONE
                    }
                    playing = !playing
                }
            }
        }
    }

    override fun getItemCount() = paths.size

    fun removeAt(index: Int) {
        paths.removeAt(index)
        notifyItemRemoved(index)
    }
}
