package com.assistant.core.services

import android.content.Context
import java.io.File

class CodingService(
    private val context: Context,
    private val fileService: FileService,
) {

    fun createProject(name: String): File {
        val rootDirectory = File(context.filesDir, "projects/$name")
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs()
        }
        val mainFile = File(rootDirectory, "main.py")
        val content = """
            def main():
                print("Hello from Assistant Platform")

            if __name__ == "__main__":
                main()
        """.trimIndent()
        fileService.writeTextFile(mainFile, content)
        return rootDirectory
    }
}
