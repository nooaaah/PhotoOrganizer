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
import com.noah.photoorganizer.data.mediastore.MediaStoreHelper
import com.noah.photoorganizer.data.mediastore.groupUrisByDate
import com.noah.photoorganizer.ui.MediaThumbnail
import com.noah.photoorganizer.ui.groupByDate
import com.noah.photoorganizer.ui.viewmodel.FavorisViewModel
import com.noah.photoorganizer.ui.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavorisScreen(
    viewModel: FavorisViewModel = viewModel(),
    onPhotoClick: (List<String>, Int) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onGroupeClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val favoriUris by viewModel.favoriUris.collectAsState()
    val favoriGroupes by viewModel.favoriGroupes.collectAsState()
    val favoriAlbums by viewModel.favoriAlbums.collectAsState()

    var sortMode by remember { mutableStateOf(SortMode.DATE) }
    var groupByDateOn by remember { mutableStateOf(false) }

    val displayedGroupes = remember(favoriGroupes, sortMode) {
        if (sortMode == SortMode.NOM) favoriGroupes.sortedBy { it.nom }
        else favoriGroupes.sortedByDescending { it.dateCreation }
    }
    val displayedAlbums = remember(favoriAlbums, sortMode) {
        if (sortMode == SortMode.NOM) favoriAlbums.sortedBy { it.nom }
        else favoriAlbums.sortedByDescending { it.dateCreation }
    }
    val displayedPhotoUris = remember(favoriUris, sortMode) {
        if (sortMode == SortMode.NOM) {
            val helper = MediaStoreHelper(context)
            favoriUris.sortedBy { helper.getPhotoInfo(Uri.parse(it))?.nom?.lowercase() ?: "" }
        } else favoriUris
    }

    val groupesDateGroups = remember(displayedGroupes, groupByDateOn) {
        if (groupByDateOn) groupByDate(displayedGroupes) { it.dateCreation } else null
    }
    val albumsDateGroups = remember(displayedAlbums, groupByDateOn) {
        if (groupByDateOn) groupByDate(displayedAlbums) { it.dateCreation } else null
    }
    val photosDateGroups = remember(displayedPhotoUris, groupByDateOn) {
        if (groupByDateOn) groupUrisByDate(context, displayedPhotoUris) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoris") },
                actions = {
                    IconButton(onClick = { groupByDateOn = !groupByDateOn }) {
                        Icon(
                            imageVector = if (groupByDateOn) Icons.Default.CalendarViewDay else Icons.Default.CalendarMonth,
                            contentDescription = "Regrouper par date"
                        )
                    }
                    IconButton(onClick = {
                        sortMode = if (sortMode == SortMode.NOM) SortMode.DATE else SortMode.NOM
                    }) {
                        Icon(
                            imageVector = if (sortMode == SortMode.NOM) Icons.Default.SortByAlpha else Icons.Default.Schedule,
                            contentDescription = "Changer le tri"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (favoriUris.isEmpty() && favoriGroupes.isEmpty() && favoriAlbums.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucun favori pour l'instant.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (displayedGroupes.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) { SectionTitle("Groupes") }
                    if (groupesDateGroups != null) {
                        groupesDateGroups.forEach { dg ->
                            item(span = { GridItemSpan(3) }) { SubDateTitle(dg.label) }
                            items(dg.items, key = { "g_${it.id}" }) { groupe ->
                                GroupeGridItem(groupe = groupe, onClick = { onGroupeClick(groupe.id) }, onLongClick = {})
                            }
                        }
                    } else {
                        items(displayedGroupes, key = { "g_${it.id}" }) { groupe ->
                            GroupeGridItem(groupe = groupe, onClick = { onGroupeClick(groupe.id) }, onLongClick = {})
                        }
                    }
                }
                if (displayedAlbums.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) { SectionTitle("Albums") }
                    if (albumsDateGroups != null) {
                        albumsDateGroups.forEach { dg ->
                            item(span = { GridItemSpan(3) }) { SubDateTitle(dg.label) }
                            items(dg.items, key = { "a_${it.id}" }) { album ->
                                AlbumGridItem(album = album, onClick = { onAlbumClick(album.id) }, onLongClick = {})
                            }
                        }
                    } else {
                        items(displayedAlbums, key = { "a_${it.id}" }) { album ->
                            AlbumGridItem(album = album, onClick = { onAlbumClick(album.id) }, onLongClick = {})
                        }
                    }
                }
                if (displayedPhotoUris.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) { SectionTitle("Photos") }
                    if (photosDateGroups != null) {
                        photosDateGroups.forEach { dg ->
                            item(span = { GridItemSpan(3) }) { SubDateTitle(dg.label) }
                            items(dg.items, key = { it }) { uriString -> PhotoCell(uriString, displayedPhotoUris, onPhotoClick) }
                        }
                    } else {
                        items(displayedPhotoUris, key = { it }) { uriString -> PhotoCell(uriString, displayedPhotoUris, onPhotoClick) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(uriString: String, allUris: List<String>, onPhotoClick: (List<String>, Int) -> Unit) {
    val uri = Uri.parse(uriString)
    val isVideo = uriString.contains("/video/")
    MediaThumbnail(
        uri = uri,
        isVideo = isVideo,
        modifier = Modifier.aspectRatio(1f).clickable { onPhotoClick(allUris, allUris.indexOf(uriString)) }
    )
}

@Composable
private fun SectionTitle(titre: String) {
    Text(text = titre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(4.dp, 8.dp, 4.dp, 0.dp))
}

@Composable
private fun SubDateTitle(titre: String) {
    Text(
        text = titre, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(4.dp, 4.dp, 4.dp, 0.dp)
    )
}