package com.assistant.core.services

import java.io.File

class FileService {

    fun writeFile(path: String, content: String) {
        val file = File(path)
        ensureDirectoryExists(file.parentFile)
        file.writeText(content)
    }

    fun readFile(path: String): String? {
        val file = File(path)
        return if (file.exists()) file.readText() else null
    }

    fun ensureDirectoryExists(dir: File?) {
        dir?.let { if (!it.exists()) it.mkdirs() }
    }

    fun listProjectFiles(rootPath: String): List<String> {
        val root = File(rootPath)
        if (!root.exists()) return emptyList()
        return root.walkTopDown()
            .filter { it.isFile }
            .map { it.absolutePath }
            .toList()
    }
}
