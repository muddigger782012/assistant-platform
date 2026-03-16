package com.assistant.core.services

import android.content.Context
import java.io.File

class CodingService(private val context: Context, private val fileService: FileService) {

    fun createProject(name: String): File {
        val projectsRoot = fileService.getProjectsRoot()
        val projectDir = File(projectsRoot, name)
        fileService.ensureDirectoryExists(projectDir.absolutePath)

        val mainPy = File(projectDir, "main.py")
        val content = """def main():
    print("Hello from Assistant Platform")

if __name__ == "__main__":
    main()
"""
        fileService.writeTextFile(mainPy.absolutePath, content)

        return projectDir
    }
}
