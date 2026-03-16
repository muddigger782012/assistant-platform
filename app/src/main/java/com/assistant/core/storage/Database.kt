package com.assistant.core.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${TABLE_PROJECTS} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                root_path TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_PROJECT_FILES} (
                id TEXT PRIMARY KEY,
                project_id TEXT NOT NULL,
                path TEXT NOT NULL,
                hash TEXT,
                last_modified INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_ACTIONS} (
                id TEXT PRIMARY KEY,
                action_type TEXT NOT NULL,
                parameters TEXT,
                required_capability TEXT,
                risk_level INTEGER,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_ACTION_QUEUE} (
                id TEXT PRIMARY KEY,
                action_id TEXT NOT NULL,
                status TEXT NOT NULL,
                scheduled_time INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_WORKFLOWS} (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                trigger_type TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_TASKS} (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                status TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE ${TABLE_AUDIT_LOGS} (
                id TEXT PRIMARY KEY,
                action_id TEXT,
                adapter_used TEXT,
                status TEXT,
                message TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUDIT_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKFLOWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTION_QUEUE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECT_FILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "assistant_platform.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_PROJECTS = "projects"
        const val TABLE_PROJECT_FILES = "project_files"
        const val TABLE_ACTIONS = "actions"
        const val TABLE_ACTION_QUEUE = "action_queue"
        const val TABLE_WORKFLOWS = "workflows"
        const val TABLE_TASKS = "tasks"
        const val TABLE_AUDIT_LOGS = "audit_logs"
    }
}
