package com.parag.khallas.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parag.khallas.databinding.FragmentGalleryBinding
import com.parag.khallas.util.FileUtils
import com.parag.khallas.viewer.FullScreenViewerActivity
import java.io.File

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FolderAdapter
    private var currentDir: File = FileUtils.ROOT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FolderAdapter { folder -> openFolder(folder) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.upButton.setOnClickListener { goUp() }

        binding.viewMediaButton.setOnClickListener {
            val media = FileUtils.listMediaInFolder(currentDir)
            if (media.isNotEmpty()) openViewer(media, 0)
        }

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

    private fun refresh() {
        binding.pathText.text = currentDir.path
        binding.upButton.visibility = if (currentDir != FileUtils.ROOT) View.VISIBLE else View.GONE
        adapter.submitList(FileUtils.listSubfolders(currentDir))
        val mediaCount = FileUtils.mediaCount(currentDir)
        binding.viewMediaButton.visibility = if (mediaCount > 0) View.VISIBLE else View.GONE
        binding.viewMediaButton.text = "View $mediaCount media file(s) in this folder"
    }

    private fun openViewer(files: List<File>, startIndex: Int) {
        val paths = ArrayList(files.map { it.absolutePath })
        val intent = Intent(requireContext(), FullScreenViewerActivity::class.java)
        intent.putStringArrayListExtra(FullScreenViewerActivity.EXTRA_PATHS, paths)
        intent.putExtra(FullScreenViewerActivity.EXTRA_START_INDEX, startIndex)
        startActivity(intent)
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
