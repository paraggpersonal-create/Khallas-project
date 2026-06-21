package com.parag.khallas.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parag.khallas.databinding.ItemBrowseBinding
import com.parag.khallas.util.FileUtils
import java.io.File

class BrowseAdapter(
    private val onFolderClick: (File) -> Unit,
    private val onMediaClick: (File) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<BrowseAdapter.VH>() {

    private var items: List<File> = emptyList()
    val selected: MutableSet<String> = mutableSetOf()

    fun submitList(entries: List<File>) {
        items = entries
        selected.clear()
        notifyDataSetChanged()
    }

    fun removeSelected() {
        items = items.filter { it.absolutePath !in selected }
        selected.clear()
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemBrowseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBrowseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        val b = holder.binding

        b.checkbox.setOnCheckedChangeListener(null)

        if (entry.isDirectory) {
            b.icon.text = "📁"
            b.name.text = entry.name
            val count = FileUtils.listAllEntries(entry).size
            b.subtitle.text = if (count > 0) "$count items" else "empty"
            b.checkbox.visibility = View.GONE
            holder.itemView.setOnClickListener { onFolderClick(entry) }
        } else {
            b.icon.text = when {
                FileUtils.isImage(entry) -> "🖼"
                FileUtils.isVideo(entry) -> "🎬"
                else -> "📄"
            }
            b.name.text = entry.name
            b.subtitle.text = FileUtils.readableSize(entry.length())
            b.checkbox.visibility = View.VISIBLE
            b.checkbox.isChecked = entry.absolutePath in selected

            b.checkbox.setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(entry.absolutePath) else selected.remove(entry.absolutePath)
                onSelectionChanged()
            }

            holder.itemView.setOnClickListener {
                if (FileUtils.isMedia(entry)) {
                    onMediaClick(entry)
                } else {
                    b.checkbox.isChecked = !b.checkbox.isChecked
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
