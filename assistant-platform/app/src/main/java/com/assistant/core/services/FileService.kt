package com.assistant.core.services

import android.content.Context
import java.io.File

class FileService(private val context: Context) {

    fun writeTextFile(path: String, content: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun readTextFile(path: String): String {
        val file = File(path)
        return if (file.exists()) file.readText() else ""
    }

    fun ensureDirectoryExists(path: String): File {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun listProjectFiles(projectDir: File): List<File> {
        return if (projectDir.exists() && projectDir.isDirectory) {
            projectDir.walkTopDown().filter { it.isFile }.toList()
        } else {
            emptyList()
        }
    }

    fun getProjectsRoot(): File {
        return ensureDirectoryExists(context.filesDir.absolutePath + "/projects")
    }
}
