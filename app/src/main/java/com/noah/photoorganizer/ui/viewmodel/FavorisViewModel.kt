package com.noah.photoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.model.Groupe
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FavorisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PhotoOrganizerRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PhotoOrganizerRepository(db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao())
    }

    val favoriUris: StateFlow<List<String>> = repository.getAllFavoris()
        .map { list -> list.map { it.photoUri } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriGroupes: StateFlow<List<Groupe>> = repository.getFavoriGroupes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriAlbums: StateFlow<List<Album>> = repository.getFavoriAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}