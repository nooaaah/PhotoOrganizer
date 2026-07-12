package com.noah.photoorganizer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.mediastore.BucketHelper
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BucketSyncViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PhotoOrganizerRepository
    private val bucketHelper = BucketHelper(application)

    private val _syncDone = MutableStateFlow(false)
    val syncDone: StateFlow<Boolean> = _syncDone

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PhotoOrganizerRepository(db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao())
        syncBuckets()
    }

    private fun syncBuckets() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val buckets = bucketHelper.getAllBuckets()
                buckets.forEach { bucket ->
                    repository.getOrCreateAlbumForBucket(bucket)
                }
            }
            _syncDone.value = true
        }
    }
}