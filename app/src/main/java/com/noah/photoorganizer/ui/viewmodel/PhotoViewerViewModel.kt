package com.noah.photoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoViewerViewModel(
    application: Application,
    private val photoUri: String,
    private val albumId: Long?
) : AndroidViewModel(application) {
    private val repository: PhotoOrganizerRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PhotoOrganizerRepository(db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao())
    }

    val isFavori: StateFlow<Boolean> = repository.isFavori(photoUri)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleFavori() {
        viewModelScope.launch {
            if (isFavori.value) repository.removeFavori(photoUri) else repository.addFavori(photoUri)
        }
    }

    // Appelé après une suppression système réussie : nettoie nos propres références locales
    fun cleanupAfterDelete() {
        viewModelScope.launch {
            albumId?.let { repository.removePhotoFromAlbum(it, photoUri) }
            repository.removeFavori(photoUri)
        }
    }

    // Retire uniquement le lien avec cet album, la photo reste sur le téléphone
    fun removeFromAlbumOnly(onDone: () -> Unit) {
        viewModelScope.launch {
            albumId?.let { repository.removePhotoFromAlbum(it, photoUri) }
            onDone()
        }
    }
}

class PhotoViewerViewModelFactory(
    private val application: Application,
    private val photoUri: String,
    private val albumId: Long?
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return PhotoViewerViewModel(application, photoUri, albumId) as T
    }
}