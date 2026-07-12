package com.noah.photoorganizer.data.mediastore

import android.content.Context
import android.os.Build
import android.provider.MediaStore

data class BucketInfo(val bucketId: Long, val displayName: String, val relativePath: String)

class BucketHelper(private val context: Context) {

    // Liste tous les dossiers réels contenant des photos/vidéos sur le téléphone
    // Récupère toutes les photos/vidéos d'un dossier réel donné (par son bucket)
    fun getPhotosInBucket(relativePath: String, sortByName: Boolean = false): List<PhotoItem> {
        val items = mutableListOf<PhotoItem>()
        val useRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        val selection = if (useRelativePath) {
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) AND ${MediaStore.Files.FileColumns.RELATIVE_PATH} = ?"
        } else {
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) AND ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        }
        val selectionArgs = if (useRelativePath) {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                relativePath
            )
        } else {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                "%/$relativePath%"
            )
        }
        val sortOrder = if (sortByName) {
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"
        } else {
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        }

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val nom = cursor.getString(nameCol) ?: ""
                val dateAjout = cursor.getLong(dateCol)
                val isVideo = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val uri = if (isVideo) {
                    android.content.ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                } else {
                    android.content.ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                }
                items.add(PhotoItem(id, uri, nom, dateAjout, isVideo))
            }
        }
        return items
    }
    fun getAllBuckets(): List<BucketInfo> {
        val result = mutableListOf<BucketInfo>()
        val seen = mutableSetOf<Long>()

        val useRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val projection = if (useRelativePath) {
            arrayOf(
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            )
        } else {
            arrayOf(
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA
            )
        }

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection, selection, selectionArgs, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val pathCol = cursor.getColumnIndex(projection[2])

            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(idCol)
                if (bucketId in seen) continue
                val name = cursor.getString(nameCol) ?: continue
                val rawPath = if (pathCol >= 0) cursor.getString(pathCol) else null
                val relativePath = if (useRelativePath) {
                    rawPath ?: continue
                } else {
                    // Sur les vieilles versions, on déduit le dossier depuis le chemin complet du fichier
                    val fullPath = rawPath ?: continue
                    val dir = fullPath.substringBeforeLast('/')
                    val relative = dir.substringAfter("/storage/emulated/0/", "")
                    if (relative.isEmpty()) continue
                    "$relative/"
                }
                seen.add(bucketId)
                result.add(BucketInfo(bucketId, name, relativePath))
            }
        }
        return result
    }
}