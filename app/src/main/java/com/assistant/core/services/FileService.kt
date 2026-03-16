package com.assistant.core.services

import android.content.Context
import java.io.File

class FileService(private val context: Context) {

    fun ensureDirectoriesExist(relativePath: String): File {
        val directory = File(context.filesDir, relativePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    fun writeTextFile(file: File, content: String): File {
        file.parentFile?.mkdirs()
        file.writeText(content)
        return file
    }

    fun readTextFile(file: File): String = file.takeIf { it.exists() }?.readText().orEmpty()

    fun listProjectFiles(projectRoot: File): List<File> {
        if (!projectRoot.exists()) {
            return emptyList()
        }
        return projectRoot.walkTopDown().filter { it.isFile }.toList()
    }
}
