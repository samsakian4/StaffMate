package com.staffmate.app.util

import android.content.Context
import android.net.Uri
import com.staffmate.app.data.StaffMateDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtil {

    fun suggestedBackupFileName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US)
        return "StaffMate_Backup_${sdf.format(Date())}.db"
    }

    private fun dbFile(context: Context): File =
        context.getDatabasePath(StaffMateDatabase.DB_NAME)

    /** Copies the live database file to the destination Uri chosen by the user (SAF). */
    fun exportBackup(context: Context, destUri: Uri): Boolean {
        return try {
            StaffMateDatabase.closeAndReset()
            val source = dbFile(context)
            context.contentResolver.openOutputStream(destUri)?.use { out ->
                source.inputStream().use { input -> input.copyTo(out) }
            } ?: return false
            true
        } catch (e: Exception) {
            false
        } finally {
            StaffMateDatabase.getInstance(context)
        }
    }

    /**
     * Validates that a file at [srcUri] is a readable SQLite database with the expected tables.
     */
    fun isValidBackup(context: Context, srcUri: Uri): Boolean {
        val tmp = File(context.cacheDir, "validate_check.db")
        return try {
            context.contentResolver.openInputStream(srcUri)?.use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            } ?: return false

            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                tmp.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='employees'", null
            )
            val valid = cursor.moveToFirst()
            cursor.close()
            db.close()
            valid
        } catch (e: Exception) {
            false
        } finally {
            tmp.delete()
        }
    }

    /** Replaces the live database with the content at [srcUri]. Caller must confirm with the user first. */
    fun importBackup(context: Context, srcUri: Uri): Boolean {
        return try {
            StaffMateDatabase.closeAndReset()
            val dest = dbFile(context)
            context.contentResolver.openInputStream(srcUri)?.use { input ->
                dest.outputStream().use { out -> input.copyTo(out) }
            } ?: return false
            // Drop -wal/-shm sidecar files from previous session to avoid stale state
            File(dest.path + "-wal").delete()
            File(dest.path + "-shm").delete()
            true
        } catch (e: Exception) {
            false
        } finally {
            StaffMateDatabase.getInstance(context)
        }
    }

    /** Creates an automatic safety backup before a restore, inside app-private storage. */
    fun autoBackupBeforeRestore(context: Context): File? {
        return try {
            val source = dbFile(context)
            if (!source.exists()) return null
            val dir = File(context.filesDir, "backups").apply { mkdirs() }
            val sdf = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
            val dest = File(dir, "AutoBackup_before_restore_${sdf.format(Date())}.db")
            source.copyTo(dest, overwrite = true)
            dest
        } catch (e: Exception) {
            null
        }
    }

    fun approxDbSizeKb(context: Context): Long = dbFile(context).length() / 1024
}
