package com.noah.photoorganizer.data.dao

import androidx.room.*
import com.noah.photoorganizer.data.model.Favori
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriDao {
    @Query("SELECT * FROM favoris ORDER BY dateAjout DESC")
    fun getAllFavoris(): Flow<List<Favori>>

    @Query("SELECT EXISTS(SELECT 1 FROM favoris WHERE photoUri = :photoUri)")
    fun isFavori(photoUri: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favori: Favori)

    @Query("DELETE FROM favoris WHERE photoUri = :photoUri")
    suspend fun removeFavori(photoUri: String)
}