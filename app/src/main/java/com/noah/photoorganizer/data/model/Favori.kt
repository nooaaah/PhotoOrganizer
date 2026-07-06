package com.noah.photoorganizer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoris")
data class Favori(
    @PrimaryKey
    val photoUri: String,
    val dateAjout: Long = System.currentTimeMillis()
)