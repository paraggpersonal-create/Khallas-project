package com.parag.khallas.viewer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.parag.khallas.databinding.ActivityFullscreenViewerBinding
import java.io.File

class FullScreenViewerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATHS = "extra_paths"
        const val EXTRA_START_INDEX = "extra_start_index"
        private const val FAST_NAV_INTERVAL_MS = 150L
        private const val HOLD_THRESHOLD_MS = 400L
    }

    private lateinit var binding: ActivityFullscreenViewerBinding
    private lateinit var adapter: MediaPagerAdapter
    private val paths: MutableList<String> = mutableListOf()
    private val selected: MutableSet<String> = mutableSetOf()

    private val handler = Handler(Looper.getMainLooper())
    private var isHolding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val incoming = intent.getStringArrayListExtra(EXTRA_PATHS) ?: arrayListOf()
        paths.addAll(incoming)

        if (paths.isEmpty()) {
            Toast.makeText(this, "No media found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0).coerceIn(0, paths.size - 1)

        adapter = MediaPagerAdapter(paths)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.setCurrentItem(startIndex, false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUi()
            }
        })

        setupRepeatButton(binding.prevButton, -1)
        setupRepeatButton(binding.nextButton, 1)

        binding.selectButton.setOnClickListener { toggleSelectCurrent() }
        binding.deleteButton.setOnClickListener { deleteSelected() }

        updateUi()
    }

    private fun currentPath(): String = paths[binding.viewPager.currentItem]

    private fun navigate(direction: Int) {
        if (paths.isEmpty()) return
        val next = (binding.viewPager.currentItem + direction).coerceIn(0, paths.size - 1)
        binding.viewPager.setCurrentItem(next, true)
    }

    private fun setupRepeatButton(button: android.view.View, direction: Int) {
        var starter: Runnable? = null
        var repeater: Runnable? = null

        button.setOnClickListener { navigate(direction) }

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding = false
                    starter = Runnable {
                        isHolding = true
                        repeater = object : Runnable {
                            override fun run() {
                                navigate(direction)
                                if (isHolding) handler.postDelayed(this, FAST_NAV_INTERVAL_MS)
                            }
                        }
                        handler.post(repeater!!)
                    }
                    handler.postDelayed(starter!!, HOLD_THRESHOLD_MS)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    starter?.let { handler.removeCallbacks(it) }
                    val wasHolding = isHolding
                    isHolding = false
                    repeater?.let { handler.removeCallbacks(it) }
                    if (wasHolding) {
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun toggleSelectCurrent() {
        val path = currentPath()
        if (path in selected) {
            selected.remove(path)
            updateUi()
        } else {
            selected.add(path)
            updateUi()
            if (binding.viewPager.currentItem < paths.size - 1) {
                handler.postDelayed({ navigate(1) }, 150L)
            }
        }
    }

    private fun deleteSelected() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            return
        }

        val toDelete = selected.toList()
            .mapNotNull { p -> val idx = paths.indexOf(p); if (idx >= 0) idx to p else null }
            .sortedByDescending { it.first }

        var deletedCount = 0
        for ((index, path) in toDelete) {
            val file = File(path)
            if (file.exists() && file.delete()) {
                deletedCount++
                adapter.removeAt(index)
            }
        }
        selected.clear()

        Toast.makeText(this, "Deleted $deletedCount file(s)", Toast.LENGTH_SHORT).show()

        if (paths.isEmpty()) {
            finish()
        } else {
            val safeIndex = binding.viewPager.currentItem.coerceIn(0, paths.size - 1)
            binding.viewPager.setCurrentItem(safeIndex, false)
            updateUi()
        }
    }

    private fun updateUi() {
        if (paths.isEmpty()) return
        val pos = binding.viewPager.currentItem
        binding.counterText.text = "${pos + 1} / ${paths.size}"
        val isSelected = currentPath() in selected
        binding.selectButton.text = if (isSelected) "☑ Selected" else "☐ Select"
        binding.deleteButton.text = "🗑 Delete (${selected.size})"
        binding.prevButton.isEnabled = pos > 0
        binding.nextButton.isEnabled = pos < paths.size - 1
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
