package com.assistant.core.services

import android.content.Context
import java.io.File

class CodingService(private val context: Context) {

    private val fileService = FileService(context)

    fun createProject(name: String): File {
        val projectsDir = fileService.getProjectsDir()
        val projectDir = File(projectsDir, name)
        projectDir.mkdirs()

        val mainPy = File(projectDir, "main.py")
        val content = """
def main():
    print("Hello from Assistant Platform")

if __name__ == "__main__":
    main()
        """.trimIndent()

        fileService.writeTextFile(mainPy, content)
        return projectDir
    }
}
