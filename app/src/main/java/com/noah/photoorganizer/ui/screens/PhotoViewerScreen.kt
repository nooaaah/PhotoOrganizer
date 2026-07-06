package com.noah.photoorganizer.ui.screens

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.noah.photoorganizer.ui.viewmodel.PhotoViewerViewModel
import com.noah.photoorganizer.ui.viewmodel.PhotoViewerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    photos: List<String>,
    startIndex: Int,
    albumId: Long?,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = startIndex) { photos.size }
    val currentUriString = photos.getOrNull(pagerState.currentPage) ?: photos.first()
    val photoUri = Uri.parse(currentUriString)

    val viewModel: PhotoViewerViewModel = viewModel(
        key = "viewer_${currentUriString}_$albumId",
        factory = PhotoViewerViewModelFactory(
            context.applicationContext as android.app.Application, currentUriString, albumId
        )
    )
    val isFavori by viewModel.isFavori.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun onDeleteSuccess() {
        viewModel.cleanupAfterDelete()
        onDeleted()
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onDeleteSuccess()
    }

    fun deletePhoto() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val pendingIntent = MediaStore.createTrashRequest(
                    context.contentResolver, listOf(photoUri), true
                )
                deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                try {
                    context.contentResolver.delete(photoUri, null, null)
                    onDeleteSuccess()
                } catch (e: RecoverableSecurityException) {
                    val intentSender = e.userAction.actionIntent.intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }
            else -> {
                try {
                    context.contentResolver.delete(photoUri, null, null)
                    onDeleteSuccess()
                } catch (e: SecurityException) { /* pas de permission suffisante */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${pagerState.currentPage + 1} / ${photos.size}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavori() }) {
                        Icon(
                            imageVector = if (isFavori) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori"
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, photoUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager la photo"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Partager")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            val uriString = photos[page]
            val uri = Uri.parse(uriString)
            val isVideo = uriString.contains("/video/")

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isVideo) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                val controller = android.widget.MediaController(ctx)
                                controller.setAnchorView(this)
                                setMediaController(controller)
                                setVideoURI(uri)
                                setOnPreparedListener {
                                    if (page == pagerState.currentPage) start()
                                    controller.show(0)
                                }
                                setOnErrorListener { _, what, extra ->
                                    android.util.Log.e("PhotoOrganizerDebug", "Erreur vidéo: what=$what extra=$extra")
                                    true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la photo") },
            text = { Text("Retirer uniquement de cet album, ou supprimer la photo du téléphone (corbeille) ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    deletePhoto()
                }) { Text("Supprimer du téléphone") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
                    if (albumId != null) {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            viewModel.removeFromAlbumOnly(onDone = onBack)
                        }) { Text("Retirer de l'album") }
                    }
                }
            }
        )
    }
}