package com.assistant.core.services

import java.io.File

class CodingService(private val projectsRoot: File) {

    fun createProject(name: String): File {
        val projectDir = File(projectsRoot, name)
        projectDir.mkdirs()

        val mainPy = File(projectDir, "main.py")
        mainPy.writeText(
            """
            def main():
                print("Hello from Assistant Platform")

            if __name__ == "__main__":
                main()
            """.trimIndent()
        )

        return projectDir
    }
}
