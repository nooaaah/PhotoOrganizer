package com.noah.photoorganizer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val coverUri: String? = null,
    val groupeId: Long? = null,
    val dateCreation: Long = System.currentTimeMillis(),
    val isFavori: Boolean = false
)