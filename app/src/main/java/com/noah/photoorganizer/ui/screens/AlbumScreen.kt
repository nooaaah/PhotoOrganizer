package com.noah.photoorganizer.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noah.photoorganizer.data.mediastore.groupUrisByDate
import com.noah.photoorganizer.ui.MediaThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    onBack: () -> Unit,
    onPhotoClick: (List<String>, Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: com.noah.photoorganizer.ui.viewmodel.AlbumViewModel = viewModel(
        key = "album_$albumId",
        factory = com.noah.photoorganizer.ui.viewmodel.AlbumViewModelFactory(
            context.applicationContext as android.app.Application, albumId
        )
    )

    val album by viewModel.album.collectAsState()
    val photoUris by viewModel.photoUris.collectAsState()
    val sortByName by viewModel.sortByName.collectAsState()
    var groupByDateOn by remember { mutableStateOf(false) }

    var showDeleteAlbumConfirm by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        PhotoPickerScreen(
            onCancel = { showPicker = false },
            onConfirm = { selected ->
                viewModel.addPhotos(selected.map { it.uri })
                showPicker = false
            }
        )
        return
    }

    val dateGroups = remember(photoUris, groupByDateOn) {
        if (groupByDateOn) groupUrisByDate(context, photoUris) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album?.nom ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { groupByDateOn = !groupByDateOn }) {
                        Icon(
                            imageVector = if (groupByDateOn) Icons.Default.CalendarViewDay else Icons.Default.CalendarMonth,
                            contentDescription = "Regrouper par date"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSort() }) {
                        Icon(
                            imageVector = if (sortByName) Icons.Default.SortByAlpha else Icons.Default.Schedule,
                            contentDescription = "Trier"
                        )
                    }
                    IconButton(onClick = { showDeleteAlbumConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer l'album")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter des photos")
            }
        }
    ) { padding ->
        if (photoUris.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucune photo. Utilise le + pour en ajouter.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (dateGroups != null) {
                    dateGroups.forEach { group ->
                        item(span = { GridItemSpan(3) }) {
                            Text(
                                group.label,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(8.dp, 12.dp, 8.dp, 4.dp)
                            )
                        }
                        items(group.items, key = { it }) { uriString ->
                            PhotoGridCell(uriString, photoUris, onPhotoClick)
                        }
                    }
                } else {
                    items(photoUris, key = { it }) { uriString ->
                        PhotoGridCell(uriString, photoUris, onPhotoClick)
                    }
                }
            }
        }
    }

    if (showDeleteAlbumConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAlbumConfirm = false },
            title = { Text("Supprimer l'album ?") },
            text = { Text("L'album \"${album?.nom}\" sera supprimé. Les photos qu'il contient resteront sur le téléphone, seul l'album disparaît.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAlbumConfirm = false
                    viewModel.deleteAlbum(onDone = onBack)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlbumConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun PhotoGridCell(uriString: String, allUris: List<String>, onPhotoClick: (List<String>, Int) -> Unit) {
    val uri = Uri.parse(uriString)
    val isVideo = uriString.contains("/video/")
    MediaThumbnail(
        uri = uri,
        isVideo = isVideo,
        modifier = Modifier.aspectRatio(1f).clickable {
            onPhotoClick(allUris, allUris.indexOf(uriString))
        }
    )
}