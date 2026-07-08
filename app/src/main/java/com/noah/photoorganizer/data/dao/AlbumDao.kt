package com.noah.photoorganizer.data.dao

import androidx.room.*
import com.noah.photoorganizer.data.model.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums WHERE groupeId IS :groupeId ORDER BY nom ASC")
    fun getAlbumsByGroupeSortedByName(groupeId: Long?): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE groupeId IS :groupeId ORDER BY dateCreation DESC")
    fun getAlbumsByGroupeSortedByDate(groupeId: Long?): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): Album?

    @Insert
    suspend fun insert(album: Album): Long

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("UPDATE albums SET groupeId = :groupeId WHERE id = :albumId")
    suspend fun moveToGroupe(albumId: Long, groupeId: Long?)

    @Query("SELECT * FROM albums WHERE groupeId IS :groupeId ORDER BY dateCreation DESC LIMIT 4")
    suspend fun getAlbumsByGroupeOnceLimited(groupeId: Long?): List<Album>

    @Query("SELECT COUNT(*) FROM albums WHERE groupeId IS :groupeId")
    suspend fun getAlbumCountInGroupe(groupeId: Long?): Int

    @Query("SELECT * FROM albums WHERE isFavori = 1 ORDER BY nom ASC")
    fun getFavoriAlbums(): Flow<List<Album>>

    @Query("UPDATE albums SET isFavori = :isFavori WHERE id = :id")
    suspend fun setFavori(id: Long, isFavori: Boolean)

    @Query("SELECT * FROM albums WHERE folderPath = :path LIMIT 1")
    suspend fun getAlbumByFolderPath(path: String): Album?
}