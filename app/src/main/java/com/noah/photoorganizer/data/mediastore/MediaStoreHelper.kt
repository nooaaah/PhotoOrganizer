package com.noah.photoorganizer.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size

data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val nom: String,
    val dateAjout: Long,
    val isVideo: Boolean = false
)

class MediaStoreHelper(private val context: Context) {

    fun getAllPhotos(sortByName: Boolean = false): List<PhotoItem> {
        val items = mutableListOf<PhotoItem>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val sortOrder = if (sortByName) {
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"
        } else {
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        }

        val queryUri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val nom = cursor.getString(nameCol) ?: ""
                val dateAjout = cursor.getLong(dateCol)
                val mediaType = cursor.getInt(typeCol)
                val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

                val uri = if (isVideo) {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                } else {
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                }
                items.add(PhotoItem(id, uri, nom, dateAjout, isVideo))
            }
        }

        return items
    }

    fun getPhotoUri(mediaStoreId: Long): Uri {
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaStoreId)
    }

    fun getThumbnail(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(300, 300), null)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Récupère les infos (dont la date) d'une photo ou vidéo à partir de son URI complet
    fun getPhotoInfo(uri: Uri): PhotoItem? {
        val isVideo = uri.toString().contains("/video/")
        val projection = if (isVideo) {
            arrayOf(MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_ADDED)
        } else {
            arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED)
        }
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameCol = cursor.getColumnIndex(projection[0])
                val dateCol = cursor.getColumnIndex(projection[1])
                val nom = if (nameCol >= 0) cursor.getString(nameCol) ?: "" else ""
                val date = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                return PhotoItem(id = 0, uri = uri, nom = nom, dateAjout = date, isVideo = isVideo)
            }
        }
        return null
    }
}