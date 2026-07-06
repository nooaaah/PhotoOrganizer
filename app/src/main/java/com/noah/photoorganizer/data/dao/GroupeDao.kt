package com.noah.photoorganizer.data.dao

import androidx.room.*
import com.noah.photoorganizer.data.model.Groupe
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupeDao {
    @Query("SELECT * FROM groupes WHERE parentGroupeId IS :parentId ORDER BY nom ASC")
    fun getGroupesByParentSortedByName(parentId: Long?): Flow<List<Groupe>>

    @Query("SELECT * FROM groupes WHERE parentGroupeId IS :parentId ORDER BY dateCreation DESC")
    fun getGroupesByParentSortedByDate(parentId: Long?): Flow<List<Groupe>>

    @Query("SELECT * FROM groupes WHERE id = :id")
    suspend fun getGroupeById(id: Long): Groupe?

    @Insert
    suspend fun insert(groupe: Groupe): Long

    @Update
    suspend fun update(groupe: Groupe)

    @Delete
    suspend fun delete(groupe: Groupe)

    @Query("SELECT COUNT(*) FROM groupes WHERE parentGroupeId IS :parentId")
    suspend fun getGroupeCount(parentId: Long?): Int

    @Query("UPDATE groupes SET parentGroupeId = :newParentId WHERE id = :groupeId")
    suspend fun moveToGroupe(groupeId: Long, newParentId: Long?)

    @Query("SELECT * FROM groupes WHERE isFavori = 1 ORDER BY nom ASC")
    fun getFavoriGroupes(): Flow<List<Groupe>>

    @Query("UPDATE groupes SET isFavori = :isFavori WHERE id = :id")
    suspend fun setFavori(id: Long, isFavori: Boolean)
}