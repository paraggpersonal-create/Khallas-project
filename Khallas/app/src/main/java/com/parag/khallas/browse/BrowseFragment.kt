package com.parag.khallas.browse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parag.khallas.databinding.FragmentBrowseBinding
import com.parag.khallas.util.FileUtils
import com.parag.khallas.viewer.FullScreenViewerActivity
import java.io.File

class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BrowseAdapter
    private var currentDir: File = FileUtils.ROOT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BrowseAdapter(
            onFolderClick = { folder -> openFolder(folder) },
            onMediaClick = { file -> openViewerForFolder(file) },
            onSelectionChanged = { updateDeleteBar() }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.upButton.setOnClickListener { goUp() }
        binding.deleteSelectedButton.setOnClickListener { deleteSelected() }

        refresh()
    }

    private fun openFolder(folder: File) {
        currentDir = folder
        refresh()
    }

    private fun goUp() {
        val parent = currentDir.parentFile
        if (currentDir != FileUtils.ROOT && parent != null) {
            currentDir = parent
            refresh()
        }
    }

    private fun openViewerForFolder(tappedFile: File) {
        val media = FileUtils.listMediaInFolder(currentDir)
        val startIndex = media.indexOfFirst { it.absolutePath == tappedFile.absolutePath }.coerceAtLeast(0)
        val paths = ArrayList(media.map { it.absolutePath })
        val intent = Intent(requireContext(), FullScreenViewerActivity::class.java)
        intent.putStringArrayListExtra(FullScreenViewerActivity.EXTRA_PATHS, paths)
        intent.putExtra(FullScreenViewerActivity.EXTRA_START_INDEX, startIndex)
        startActivity(intent)
    }

    private fun updateDeleteBar() {
        val count = adapter.selected.size
        binding.deleteBar.visibility = if (count > 0) View.VISIBLE else View.GONE
        binding.selectedCountText.text = "$count selected"
    }

    private fun deleteSelected() {
        val toDelete = adapter.selected.toList()
        var deletedCount = 0
        for (path in toDelete) {
            val f = File(path)
            if (f.exists() && f.delete()) deletedCount++
        }
        adapter.removeSelected()
        updateDeleteBar()
        Toast.makeText(requireContext(), "Deleted $deletedCount file(s)", Toast.LENGTH_SHORT).show()
    }

    private fun refresh() {
        binding.pathText.text = currentDir.path
        binding.upButton.visibility = if (currentDir != FileUtils.ROOT) View.VISIBLE else View.GONE
        adapter.submitList(FileUtils.listAllEntries(currentDir))
        updateDeleteBar()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
