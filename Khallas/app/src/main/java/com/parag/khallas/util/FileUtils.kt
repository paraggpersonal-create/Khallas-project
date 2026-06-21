package com.parag.khallas.util

import android.os.Environment
import java.io.File

object FileUtils {

    val ROOT: File = Environment.getExternalStorageDirectory()

    private val IMAGE_EXT = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
    private val VIDEO_EXT = setOf("mp4", "3gp", "mkv", "webm", "avi", "mov", "m4v")

    fun isImage(file: File): Boolean = file.extension.lowercase() in IMAGE_EXT
    fun isVideo(file: File): Boolean = file.extension.lowercase() in VIDEO_EXT
    fun isMedia(file: File): Boolean = isImage(file) || isVideo(file)

    fun listSubfolders(dir: File): List<File> {
        return dir.listFiles { f -> f.isDirectory && !f.name.startsWith(".") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    fun listMediaInFolder(dir: File): List<File> {
        return dir.listFiles { f -> f.isFile && isMedia(f) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    fun listAllEntries(dir: File): List<File> {
        val folders = listSubfolders(dir)
        val files = dir.listFiles { f -> f.isFile && !f.name.startsWith(".") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
        return folders + files
    }

    fun mediaCount(dir: File): Int {
        return dir.listFiles { f -> f.isFile && isMedia(f) }?.size ?: 0
    }

    fun readableSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format("%.1f %s", size, units[unitIndex])
    }
}
