package com.noah.photoorganizer.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.mediastore.BucketHelper
import com.noah.photoorganizer.data.mediastore.MediaStoreHelper
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlbumViewModel(application: Application, private val albumId: Long) : AndroidViewModel(application) {

    private val repository: PhotoOrganizerRepository
    private val mediaStoreHelper = MediaStoreHelper(application)
    private val bucketHelper = BucketHelper(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PhotoOrganizerRepository(
            db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao()
        )
    }

    private val _album = MutableStateFlow<Album?>(null)
    val album: StateFlow<Album?> = _album.asStateFlow()

    private val _sortByName = MutableStateFlow(false)
    val sortByName: StateFlow<Boolean> = _sortByName.asStateFlow()

    // Incrémenté manuellement après un déplacement de fichier réussi, pour forcer un nouveau scan MediaStore
    private val _folderRefreshTrigger = MutableStateFlow(0)
    fun refreshFolder() { _folderRefreshTrigger.value++ }

    @OptIn(ExperimentalCoroutinesApi::class)
    val photoUris: StateFlow<List<String>> = combine(
        _album.filterNotNull(), _sortByName, _folderRefreshTrigger
    ) { album, byName, _ ->
        album to byName
    }.flatMapLatest { (album, byName) ->
        if (album.folderPath != null) {
            flow {
                emit(bucketHelper.getPhotosInBucket(album.folderPath, byName).map { it.uri.toString() })
            }
        } else {
            repository.getPhotoUrisByAlbum(albumId).map { uris ->
                if (byName) {
                    uris.sortedBy { uriString -> mediaStoreHelper.getPhotoInfo(Uri.parse(uriString))?.nom?.lowercase() ?: "" }
                } else uris
            }
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { _album.value = repository.getAlbumById(albumId) }
    }

    fun toggleSort() { _sortByName.value = !_sortByName.value }

    fun addPhotos(uris: List<Uri>) {
        viewModelScope.launch { uris.forEach { uri -> repository.addPhotoToAlbum(albumId, uri.toString()) } }
    }

    fun removePhoto(uriString: String) {
        viewModelScope.launch { repository.removePhotoFromAlbum(albumId, uriString) }
    }

    fun deleteAlbum(onDone: () -> Unit) {
        viewModelScope.launch {
            _album.value?.let { repository.deleteAlbum(it) }
            onDone()
        }
    }
}

class AlbumViewModelFactory(
    private val application: Application,
    private val albumId: Long
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return AlbumViewModel(application, albumId) as T
    }
}