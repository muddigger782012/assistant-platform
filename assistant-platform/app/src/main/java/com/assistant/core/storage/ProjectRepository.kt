package com.assistant.core.storage

import android.content.ContentValues
import android.content.Context
import com.assistant.core.models.Project

class ProjectRepository(context: Context) {

    private val db = Database(context).writableDatabase

    fun insert(project: Project) {
        val values = ContentValues().apply {
            put("id", project.id)
            put("name", project.name)
            put("root_path", project.rootPath)
            put("created_at", project.createdAt)
        }
        db.insertWithOnConflict(
            Database.TABLE_PROJECTS, null, values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getAll(): List<Project> {
        val projects = mutableListOf<Project>()
        val cursor = db.query(Database.TABLE_PROJECTS, null, null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                projects.add(
                    Project(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        rootPath = it.getString(it.getColumnIndexOrThrow("root_path")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return projects
    }
}
