package com.assistant.core.storage

import android.content.ContentValues
import com.assistant.core.models.Project

class ProjectRepository(private val database: Database) {

    fun insertProject(project: Project) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", project.id)
            put("name", project.name)
            put("root_path", project.rootPath)
            put("created_at", project.createdAt)
        }
        db.insertWithOnConflict("projects", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllProjects(): List<Project> {
        val db = database.readableDatabase
        val cursor = db.rawQuery("SELECT id, name, root_path, created_at FROM projects ORDER BY created_at DESC", null)
        val projects = mutableListOf<Project>()
        cursor.use {
            while (it.moveToNext()) {
                projects.add(
                    Project(
                        id = it.getString(0),
                        name = it.getString(1),
                        rootPath = it.getString(2),
                        createdAt = it.getLong(3)
                    )
                )
            }
        }
        return projects
    }

    fun insertProjectFile(id: String, projectId: String, path: String, hash: String?, lastModified: Long) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("project_id", projectId)
            put("path", path)
            put("hash", hash)
            put("last_modified", lastModified)
        }
        db.insertWithOnConflict("project_files", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }
}
