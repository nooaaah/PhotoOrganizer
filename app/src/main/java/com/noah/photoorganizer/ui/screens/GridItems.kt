package com.noah.photoorganizer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.model.Groupe
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository
import kotlinx.coroutines.launch

@Composable
private fun rememberRepository(): PhotoOrganizerRepository {
    val context = LocalContext.current
    return remember {
        val db = AppDatabase.getDatabase(context)
        PhotoOrganizerRepository(db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumGridItem(album: Album, onClick: () -> Unit, onLongClick: () -> Unit) {
    val repository = rememberRepository()
    val scope = rememberCoroutineScope()
    var autoCoverUri by remember(album.id) { mutableStateOf<String?>(null) }
    var count by remember(album.id) { mutableStateOf(0) }

    LaunchedEffect(album.id) {
        autoCoverUri = repository.getAlbumCoverUri(album.id)
        count = repository.getAlbumPhotoCount(album.id)
    }

    val displayCoverUri = album.coverUri ?: autoCoverUri

    Column(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (displayCoverUri != null) {
                AsyncImage(
                    model = android.net.Uri.parse(displayCoverUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.PhotoLibrary, contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        scope.launch { repository.setAlbumFavori(album.id, !album.isFavori) }
                    }
            ) {
                Icon(
                    imageVector = if (album.isFavori) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (album.isFavori) Color.Red else Color.White,
                    modifier = Modifier.padding(4.dp).size(18.dp)
                )
            }
        }
        Text(album.nom, style = MaterialTheme.typography.bodyMedium, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
        Text("$count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupeGridItem(groupe: Groupe, onClick: () -> Unit, onLongClick: () -> Unit) {
    val repository = rememberRepository()
    val scope = rememberCoroutineScope()
    var coverUris by remember(groupe.id) { mutableStateOf<List<String>>(emptyList()) }
    var counts by remember(groupe.id) { mutableStateOf(0 to 0) }

    LaunchedEffect(groupe.id) {
        coverUris = repository.getGroupeCoverUris(groupe.id)
        counts = repository.getGroupeContentCounts(groupe.id)
    }

    Column(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (groupe.coverUri != null) {
                AsyncImage(
                    model = android.net.Uri.parse(groupe.coverUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (coverUris.isEmpty()) {
                Icon(
                    Icons.Default.Folder, contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        CollageCell(coverUris.getOrNull(0), Modifier.weight(1f).fillMaxHeight())
                        Spacer(Modifier.width(2.dp))
                        CollageCell(coverUris.getOrNull(1), Modifier.weight(1f).fillMaxHeight())
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        CollageCell(coverUris.getOrNull(2), Modifier.weight(1f).fillMaxHeight())
                        Spacer(Modifier.width(2.dp))
                        CollageCell(coverUris.getOrNull(3), Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
                    .clickable {
                        scope.launch { repository.setGroupeFavori(groupe.id, !groupe.isFavori) }
                    }
            ) {
                Icon(
                    imageVector = if (groupe.isFavori) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (groupe.isFavori) Color.Red else Color.White,
                    modifier = Modifier.padding(4.dp).size(18.dp)
                )
            }
        }
        Text(groupe.nom, style = MaterialTheme.typography.bodyMedium, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
        Text(
            "${counts.first} groupe(s), ${counts.second} album(s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CollageCell(uri: String?, modifier: Modifier) {
    Box(modifier = modifier.background(Color.DarkGray.copy(alpha = 0.15f))) {
        if (uri != null) {
            AsyncImage(
                model = android.net.Uri.parse(uri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}