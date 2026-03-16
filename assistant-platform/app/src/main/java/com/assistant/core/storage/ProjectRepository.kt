package com.assistant.core.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.assistant.core.models.Project

class ProjectRepository(private val database: Database) {

    fun insert(project: Project) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("id", project.id)
            put("name", project.name)
            put("root_path", project.rootPath)
            put("created_at", project.createdAt)
        }
        db.insert("projects", null, values)
    }

    fun getById(id: String): Project? {
        val db = database.readableDatabase
        val cursor = db.query(
            "projects",
            arrayOf("id", "name", "root_path", "created_at"),
            "id = ?",
            arrayOf(id),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Project(
                    id = it.getString(0),
                    name = it.getString(1),
                    rootPath = it.getString(2),
                    createdAt = it.getLong(3)
                )
            } else null
        }
    }

    fun getAll(): List<Project> {
        val db = database.readableDatabase
        val cursor = db.query(
            "projects",
            arrayOf("id", "name", "root_path", "created_at"),
            null, null, null, null, "created_at DESC"
        )
        return cursor.use {
            val list = mutableListOf<Project>()
            while (it.moveToNext()) {
                list.add(Project(
                    id = it.getString(0),
                    name = it.getString(1),
                    rootPath = it.getString(2),
                    createdAt = it.getLong(3)
                ))
            }
            list
        }
    }
}
