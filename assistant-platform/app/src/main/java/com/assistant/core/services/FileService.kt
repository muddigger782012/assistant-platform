package com.assistant.core.services

import android.content.Context
import java.io.File

class FileService(private val context: Context) {

    fun writeTextFile(file: File, content: String) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun readTextFile(file: File): String = file.readText()

    fun ensureDirectoriesExist(dir: File) {
        dir.mkdirs()
    }

    fun listProjectFiles(projectDir: File): List<File> {
        if (!projectDir.exists() || !projectDir.isDirectory) return emptyList()
        return projectDir.walkTopDown().filter { it.isFile }.toList()
    }

    fun getProjectsDir(): File {
        val dir = File(context.filesDir, "projects")
        ensureDirectoriesExist(dir)
        return dir
    }
}
