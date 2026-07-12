package com.noah.photoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.model.Groupe
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortMode { NOM, DATE }

class RootViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoOrganizerRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PhotoOrganizerRepository(
            db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao()
        )
    }

    private val _groupeStack = MutableStateFlow<List<Long>>(emptyList())

    val currentGroupeId: StateFlow<Long?> = _groupeStack
        .map { it.lastOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val canGoBack: StateFlow<Boolean> = _groupeStack
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _sortMode = MutableStateFlow(SortMode.DATE)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    val groupes: StateFlow<List<Groupe>> = combine(currentGroupeId, _sortMode) { parentId, sort ->
        parentId to sort
    }.flatMapLatest { (parentId, sort) ->
        repository.getGroupesByParent(parentId, sort == SortMode.NOM)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = combine(currentGroupeId, _sortMode) { parentId, sort ->
        parentId to sort
    }.flatMapLatest { (parentId, sort) ->
        repository.getAlbumsByGroupe(parentId, sort == SortMode.NOM)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSortMode() {
        _sortMode.value = if (_sortMode.value == SortMode.NOM) SortMode.DATE else SortMode.NOM
    }

    fun navigateInto(groupeId: Long) {
        _groupeStack.value = _groupeStack.value + groupeId
    }

    fun navigateBack() {
        _groupeStack.value = _groupeStack.value.dropLast(1)
    }

    fun createGroupe(nom: String) {
        if (nom.isBlank()) return
        viewModelScope.launch {
            repository.insertGroupe(Groupe(nom = nom, parentGroupeId = currentGroupeId.value))
        }
    }

    fun createAlbum(nom: String) {
        if (nom.isBlank()) return
        viewModelScope.launch {
            val folderHelper = com.noah.photoorganizer.data.mediastore.FolderHelper(getApplication())
            val folderPath = folderHelper.folderPathFor(nom)
            folderHelper.ensureFolderExists(folderPath)
            repository.insertAlbum(Album(nom = nom, groupeId = currentGroupeId.value, folderPath = folderPath))
        }
    }

    fun deleteGroupe(groupe: Groupe) {
        viewModelScope.launch { repository.deleteGroupe(groupe) }
    }

    fun deleteAlbum(album: Album) {
        viewModelScope.launch { repository.deleteAlbum(album) }
    }

    fun moveAlbum(albumId: Long, destGroupeId: Long?) {
        viewModelScope.launch { repository.moveAlbumToGroupe(albumId, destGroupeId) }
    }

    fun moveGroupe(groupeId: Long, destGroupeId: Long?) {
        viewModelScope.launch { repository.moveGroupeToGroupe(groupeId, destGroupeId) }
    }

    fun navigateToGroupeDirect(groupeId: Long) {
        viewModelScope.launch {
            val chain = mutableListOf<Long>()
            var currentId: Long? = groupeId
            while (currentId != null) {
                chain.add(0, currentId)
                currentId = repository.getGroupeById(currentId)?.parentGroupeId
            }
            _groupeStack.value = chain
        }
    }
    fun updateGroupe(groupe: Groupe, nom: String, coverUri: String?) {
        viewModelScope.launch {
            repository.updateGroupe(groupe.copy(nom = nom, coverUri = coverUri))
        }
    }

    fun updateAlbum(album: Album, nom: String, coverUri: String?) {
        viewModelScope.launch {
            repository.updateAlbum(album.copy(nom = nom, coverUri = coverUri))
        }
    }
}