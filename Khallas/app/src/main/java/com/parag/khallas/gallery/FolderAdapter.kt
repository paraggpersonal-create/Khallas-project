package com.parag.khallas.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parag.khallas.databinding.ItemFolderBinding
import com.parag.khallas.util.FileUtils
import java.io.File

class FolderAdapter(
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<FolderAdapter.VH>() {

    private var items: List<File> = emptyList()

    fun submitList(folders: List<File>) {
        items = folders
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val folder = items[position]
        holder.binding.icon.text = "📁"
        holder.binding.name.text = folder.name
        val count = FileUtils.mediaCount(folder)
        holder.binding.subtitle.text = if (count > 0) "$count media" else ""
        holder.itemView.setOnClickListener { onClick(folder) }
    }

    override fun getItemCount() = items.size
}
