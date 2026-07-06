package com.noah.photoorganizer.data.repository

import com.noah.photoorganizer.data.dao.AlbumDao
import com.noah.photoorganizer.data.dao.AlbumPhotoDao
import com.noah.photoorganizer.data.dao.FavoriDao
import com.noah.photoorganizer.data.dao.GroupeDao
import com.noah.photoorganizer.data.model.*
import kotlinx.coroutines.flow.Flow

class PhotoOrganizerRepository(
    private val groupeDao: GroupeDao,
    private val albumDao: AlbumDao,
    private val albumPhotoDao: AlbumPhotoDao,
    private val favoriDao: FavoriDao
) {
    fun getGroupesByParent(parentId: Long?, sortByName: Boolean): Flow<List<Groupe>> =
        if (sortByName) groupeDao.getGroupesByParentSortedByName(parentId)
        else groupeDao.getGroupesByParentSortedByDate(parentId)

    suspend fun insertGroupe(groupe: Groupe): Long = groupeDao.insert(groupe)
    suspend fun updateGroupe(groupe: Groupe) = groupeDao.update(groupe)
    suspend fun deleteGroupe(groupe: Groupe) = groupeDao.delete(groupe)
    suspend fun moveGroupeToGroupe(groupeId: Long, newParentId: Long?) =
        groupeDao.moveToGroupe(groupeId, newParentId)
    fun getFavoriGroupes(): Flow<List<Groupe>> = groupeDao.getFavoriGroupes()
    suspend fun setGroupeFavori(id: Long, isFavori: Boolean) = groupeDao.setFavori(id, isFavori)
    suspend fun getGroupeById(id: Long) = groupeDao.getGroupeById(id)

    fun getAlbumsByGroupe(groupeId: Long?, sortByName: Boolean): Flow<List<Album>> =
        if (sortByName) albumDao.getAlbumsByGroupeSortedByName(groupeId)
        else albumDao.getAlbumsByGroupeSortedByDate(groupeId)

    suspend fun getAlbumById(id: Long): Album? = albumDao.getAlbumById(id)
    suspend fun insertAlbum(album: Album): Long = albumDao.insert(album)
    suspend fun updateAlbum(album: Album) = albumDao.update(album)
    suspend fun deleteAlbum(album: Album) = albumDao.delete(album)
    suspend fun moveAlbumToGroupe(albumId: Long, groupeId: Long?) =
        albumDao.moveToGroupe(albumId, groupeId)
    fun getFavoriAlbums(): Flow<List<Album>> = albumDao.getFavoriAlbums()
    suspend fun setAlbumFavori(id: Long, isFavori: Boolean) = albumDao.setFavori(id, isFavori)

    fun getPhotoUrisByAlbum(albumId: Long): Flow<List<String>> =
        albumPhotoDao.getPhotoUrisByAlbum(albumId)

    suspend fun addPhotoToAlbum(albumId: Long, photoUri: String) =
        albumPhotoDao.insert(AlbumPhoto(albumId, photoUri))

    suspend fun removePhotoFromAlbum(albumId: Long, photoUri: String) =
        albumPhotoDao.removePhotoFromAlbum(albumId, photoUri)

    fun getAllFavoris(): Flow<List<Favori>> = favoriDao.getAllFavoris()
    fun isFavori(photoUri: String): Flow<Boolean> = favoriDao.isFavori(photoUri)
    suspend fun addFavori(photoUri: String) = favoriDao.insert(Favori(photoUri))
    suspend fun removeFavori(photoUri: String) = favoriDao.removeFavori(photoUri)

    suspend fun getAlbumCoverUri(albumId: Long): String? = albumPhotoDao.getFirstPhotoUriOnce(albumId)
    suspend fun getAlbumPhotoCount(albumId: Long): Int = albumPhotoDao.getPhotoCount(albumId)

    suspend fun getGroupeCoverUris(groupeId: Long): List<String> {
        val albums = albumDao.getAlbumsByGroupeOnceLimited(groupeId)
        return albums.mapNotNull { albumPhotoDao.getFirstPhotoUriOnce(it.id) }
    }

    suspend fun getGroupeContentCounts(groupeId: Long): Pair<Int, Int> {
        val groupeCount = groupeDao.getGroupeCount(groupeId)
        val albumCount = albumDao.getAlbumCountInGroupe(groupeId)
        return groupeCount to albumCount
    }
}