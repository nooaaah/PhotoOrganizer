package com.noah.photoorganizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noah.photoorganizer.data.mediastore.MediaStoreHelper
import com.noah.photoorganizer.data.mediastore.PhotoItem
import com.noah.photoorganizer.ui.MediaThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerScreen(
    onCancel: () -> Unit,
    onConfirm: (List<PhotoItem>) -> Unit
) {
    val context = LocalContext.current
    val allPhotos = remember { MediaStoreHelper(context).getAllPhotos() }
    val selectedIds = remember { mutableStateListOf<Long>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${selectedIds.size} sélectionnée(s)") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Annuler")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onConfirm(allPhotos.filter { it.id in selectedIds }) },
                        enabled = selectedIds.isNotEmpty()
                    ) { Text("Ajouter") }
                }
            )
        }
    ) { padding ->
        if (allPhotos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucune photo trouvée sur ce téléphone.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allPhotos, key = { it.id }) { photo ->
                    val isSelected = photo.id in selectedIds
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                if (isSelected) selectedIds.remove(photo.id) else selectedIds.add(photo.id)
                            }
                    ) {
                        MediaThumbnail(
                            uri = photo.uri,
                            isVideo = photo.isVideo,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                            Box(
                                Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    Icons.Default.Check, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}