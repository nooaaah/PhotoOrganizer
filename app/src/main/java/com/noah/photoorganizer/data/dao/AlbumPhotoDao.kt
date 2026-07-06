package com.noah.photoorganizer.data.dao

import androidx.room.*
import com.noah.photoorganizer.data.model.AlbumPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumPhotoDao {
    @Query("SELECT photoUri FROM album_photos WHERE albumId = :albumId ORDER BY dateAjout DESC")
    fun getPhotoUrisByAlbum(albumId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(albumPhoto: AlbumPhoto)

    @Query("DELETE FROM album_photos WHERE albumId = :albumId AND photoUri = :photoUri")
    suspend fun removePhotoFromAlbum(albumId: Long, photoUri: String)

    @Query("SELECT photoUri FROM album_photos WHERE albumId = :albumId ORDER BY dateAjout DESC LIMIT 1")
    suspend fun getFirstPhotoUriOnce(albumId: Long): String?

    @Query("SELECT COUNT(*) FROM album_photos WHERE albumId = :albumId")
    suspend fun getPhotoCount(albumId: Long): Int
}