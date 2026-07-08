package com.noah.photoorganizer.data.mediastore

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class FolderHelper(private val context: Context) {

    private val baseFolder = "Pictures/PhotoOrganizer"

    fun folderPathFor(albumNom: String): String = "$baseFolder/$albumNom/"

    // true si ce dossier a été créé par notre app (donc supprimable), false si c'est un vrai dossier système
    fun isAppManagedFolder(path: String): Boolean = path.startsWith("$baseFolder/")

    fun baseFolderFor(isVideo: Boolean): String = if (isVideo) "Movies/" else "Pictures/"

    fun ensureFolderExists(relativePath: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, ".placeholder")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }
        try {
            context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
        } catch (e: Exception) { /* Non bloquant */ }
    }

    fun requestMovePermission(uris: List<Uri>): PendingIntent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createWriteRequest(context.contentResolver, uris)
        } else null
    }

    fun moveToFolder(uri: Uri, targetRelativePath: String): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, targetRelativePath)
        }
        return try {
            context.contentResolver.update(uri, values, null, null) > 0
        } catch (e: Exception) {
            false
        }
    }
}