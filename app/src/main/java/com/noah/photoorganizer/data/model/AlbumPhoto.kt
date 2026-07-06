package com.noah.photoorganizer.data.model

import androidx.room.Entity

@Entity(tableName = "album_photos", primaryKeys = ["albumId", "photoUri"])
data class AlbumPhoto(
    val albumId: Long,
    val photoUri: String,
    val dateAjout: Long = System.currentTimeMillis()
)