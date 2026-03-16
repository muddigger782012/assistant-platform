package com.assistant.core.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.assistant.core.models.Project
import java.util.UUID

data class ProjectFileRecord(
    val id: String,
    val projectId: String,
    val path: String,
    val hash: String? = null,
    val lastModified: Long? = null,
)

class ProjectRepository(private val database: Database) {

    fun insertProject(project: Project) {
        val values = ContentValues().apply {
            put("id", project.id)
            put("name", project.name)
            put("root_path", project.rootPath)
            put("created_at", project.createdAt)
        }
        database.writableDatabase.insertWithOnConflict(
            Database.TABLE_PROJECTS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun insertProjectFile(projectId: String, path: String, hash: String? = null, lastModified: Long? = null) {
        val values = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("project_id", projectId)
            put("path", path)
            put("hash", hash)
            put("last_modified", lastModified)
        }
        database.writableDatabase.insertWithOnConflict(
            Database.TABLE_PROJECT_FILES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun getAllProjects(): List<Project> {
        val projects = mutableListOf<Project>()
        database.readableDatabase.query(
            Database.TABLE_PROJECTS,
            arrayOf("id", "name", "root_path", "created_at"),
            null,
            null,
            null,
            null,
            "created_at DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                projects += Project(
                    id = cursor.getString(0),
                    name = cursor.getString(1),
                    rootPath = cursor.getString(2),
                    createdAt = cursor.getLong(3),
                )
            }
        }
        return projects
    }
}
