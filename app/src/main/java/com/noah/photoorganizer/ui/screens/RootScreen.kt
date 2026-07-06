package com.noah.photoorganizer.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.model.Groupe
import com.noah.photoorganizer.ui.groupByDate
import com.noah.photoorganizer.ui.rememberPhotoPermissionState
import com.noah.photoorganizer.ui.viewmodel.RootViewModel
import com.noah.photoorganizer.ui.viewmodel.SortMode
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(viewModel: RootViewModel = viewModel(), onAlbumClick: (Long) -> Unit) {
    val hasPermission = rememberPhotoPermissionState()

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Autorisation d'accès aux photos requise pour utiliser l'application.")
        }
        return
    }

    val groupes by viewModel.groupes.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    BackHandler(enabled = canGoBack) { viewModel.navigateBack() }
    val sortMode by viewModel.sortMode.collectAsState()
    var groupByDateOn by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    var groupeActionTarget by remember { mutableStateOf<Groupe?>(null) }
    var albumActionTarget by remember { mutableStateOf<Album?>(null) }
    var groupeToDelete by remember { mutableStateOf<Groupe?>(null) }
    var albumToDelete by remember { mutableStateOf<Album?>(null) }
    var groupeToMove by remember { mutableStateOf<Groupe?>(null) }
    var albumToMove by remember { mutableStateOf<Album?>(null) }
    var groupeToEdit by remember { mutableStateOf<Groupe?>(null) }
    var albumToEdit by remember { mutableStateOf<Album?>(null) }

    groupeToMove?.let { groupe ->
        GroupePickerScreen(
            excludeGroupeId = groupe.id,
            onCancel = { groupeToMove = null },
            onDestinationChosen = { dest ->
                viewModel.moveGroupe(groupe.id, dest)
                groupeToMove = null
            }
        )
        return
    }
    albumToMove?.let { album ->
        GroupePickerScreen(
            excludeGroupeId = null,
            onCancel = { albumToMove = null },
            onDestinationChosen = { dest ->
                viewModel.moveAlbum(album.id, dest)
                albumToMove = null
            }
        )
        return
    }

    val groupesDateGroups = remember(groupes, groupByDateOn) {
        if (groupByDateOn) groupByDate(groupes) { it.dateCreation } else null
    }
    val albumsDateGroups = remember(albums, groupByDateOn) {
        if (groupByDateOn) groupByDate(albums) { it.dateCreation } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PhotoOrganizer") },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { groupByDateOn = !groupByDateOn }) {
                        Icon(
                            imageVector = if (groupByDateOn) Icons.Default.CalendarViewDay else Icons.Default.CalendarMonth,
                            contentDescription = "Regrouper par date"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSortMode() }) {
                        Icon(
                            imageVector = if (sortMode == SortMode.NOM) Icons.Default.SortByAlpha else Icons.Default.Schedule,
                            contentDescription = "Changer le tri"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
                DropdownMenu(expanded = showFabMenu, onDismissRequest = { showFabMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Nouveau groupe") },
                        onClick = { showFabMenu = false; showCreateDialog = "groupe" }
                    )
                    DropdownMenuItem(
                        text = { Text("Nouvel album") },
                        onClick = { showFabMenu = false; showCreateDialog = "album" }
                    )
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (groupes.isEmpty() && albums.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Vide pour l'instant. Utilise le bouton + pour créer un groupe ou un album.")
                    }
                }
            }
            if (groupes.isNotEmpty()) {
                item(span = { GridItemSpan(3) }) { SectionTitle("Groupes") }
                if (groupesDateGroups != null) {
                    groupesDateGroups.forEach { dateGroup ->
                        item(span = { GridItemSpan(3) }) { SubDateTitle(dateGroup.label) }
                        items(dateGroup.items, key = { "g_${it.id}" }) { groupe ->
                            GroupeGridItem(
                                groupe = groupe,
                                onClick = { viewModel.navigateInto(groupe.id) },
                                onLongClick = { groupeActionTarget = groupe }
                            )
                        }
                    }
                } else {
                    items(groupes, key = { "g_${it.id}" }) { groupe ->
                        GroupeGridItem(
                            groupe = groupe,
                            onClick = { viewModel.navigateInto(groupe.id) },
                            onLongClick = { groupeActionTarget = groupe }
                        )
                    }
                }
            }
            if (albums.isNotEmpty()) {
                item(span = { GridItemSpan(3) }) { SectionTitle("Albums") }
                if (albumsDateGroups != null) {
                    albumsDateGroups.forEach { dateGroup ->
                        item(span = { GridItemSpan(3) }) { SubDateTitle(dateGroup.label) }
                        items(dateGroup.items, key = { "a_${it.id}" }) { album ->
                            AlbumGridItem(
                                album = album,
                                onClick = { onAlbumClick(album.id) },
                                onLongClick = { albumActionTarget = album }
                            )
                        }
                    }
                } else {
                    items(albums, key = { "a_${it.id}" }) { album ->
                        AlbumGridItem(
                            album = album,
                            onClick = { onAlbumClick(album.id) },
                            onLongClick = { albumActionTarget = album }
                        )
                    }
                }
            }
        }
    }

    showCreateDialog?.let { type ->
        var nomSaisi by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = null },
            title = { Text(if (type == "groupe") "Nouveau groupe" else "Nouvel album") },
            text = {
                OutlinedTextField(
                    value = nomSaisi, onValueChange = { nomSaisi = it },
                    label = { Text("Nom") }, singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (type == "groupe") viewModel.createGroupe(nomSaisi) else viewModel.createAlbum(nomSaisi)
                    showCreateDialog = null
                }) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = null }) { Text("Annuler") } }
        )
    }

    groupeActionTarget?.let { groupe ->
        AlertDialog(
            onDismissRequest = { groupeActionTarget = null },
            title = { Text(groupe.nom) },
            text = { Text("Que veux-tu faire avec ce groupe ?") },
            confirmButton = {
                TextButton(onClick = {
                    groupeToMove = groupe
                    groupeActionTarget = null
                }) { Text("Déplacer") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { groupeActionTarget = null }) { Text("Annuler") }
                    TextButton(onClick = {
                        groupeToEdit = groupe
                        groupeActionTarget = null
                    }) { Text("Modifier") }
                    TextButton(onClick = {
                        groupeToDelete = groupe
                        groupeActionTarget = null
                    }) { Text("Supprimer") }
                }
            }
        )
    }

    albumActionTarget?.let { album ->
        AlertDialog(
            onDismissRequest = { albumActionTarget = null },
            title = { Text(album.nom) },
            text = { Text("Que veux-tu faire avec cet album ?") },
            confirmButton = {
                TextButton(onClick = {
                    albumToMove = album
                    albumActionTarget = null
                }) { Text("Déplacer") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { albumActionTarget = null }) { Text("Annuler") }
                    TextButton(onClick = {
                        albumToEdit = album
                        albumActionTarget = null
                    }) { Text("Modifier") }
                    TextButton(onClick = {
                        albumToDelete = album
                        albumActionTarget = null
                    }) { Text("Supprimer") }
                }
            }
        )
    }

    groupeToEdit?.let { groupe ->
        EditNomCoverDialog(
            titre = "Modifier le groupe",
            nomInitial = groupe.nom,
            coverUriInitial = groupe.coverUri,
            onDismiss = { groupeToEdit = null },
            onConfirm = { nom, cover ->
                viewModel.updateGroupe(groupe, nom, cover)
                groupeToEdit = null
            }
        )
    }

    albumToEdit?.let { album ->
        EditNomCoverDialog(
            titre = "Modifier l'album",
            nomInitial = album.nom,
            coverUriInitial = album.coverUri,
            onDismiss = { albumToEdit = null },
            onConfirm = { nom, cover ->
                viewModel.updateAlbum(album, nom, cover)
                albumToEdit = null
            }
        )
    }

    groupeToDelete?.let { groupe ->
        AlertDialog(
            onDismissRequest = { groupeToDelete = null },
            title = { Text("Supprimer le groupe ?") },
            text = { Text("Le groupe \"${groupe.nom}\" sera supprimé. ⚠️ Les groupes ou albums qu'il contient ne seront plus accessibles depuis l'accueil.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteGroupe(groupe); groupeToDelete = null }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { groupeToDelete = null }) { Text("Annuler") } }
        )
    }

    albumToDelete?.let { album ->
        AlertDialog(
            onDismissRequest = { albumToDelete = null },
            title = { Text("Supprimer l'album ?") },
            text = { Text("L'album \"${album.nom}\" sera supprimé. Les photos qu'il contient resteront sur le téléphone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAlbum(album); albumToDelete = null }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { albumToDelete = null }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun SectionTitle(titre: String) {
    Text(
        text = titre,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(4.dp, 8.dp, 4.dp, 0.dp)
    )
}

@Composable
private fun SubDateTitle(titre: String) {
    Text(
        text = titre,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(4.dp, 4.dp, 4.dp, 0.dp)
    )
}