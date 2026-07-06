package com.noah.photoorganizer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noah.photoorganizer.data.database.AppDatabase
import com.noah.photoorganizer.data.repository.PhotoOrganizerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupePickerScreen(
    excludeGroupeId: Long?,
    onCancel: () -> Unit,
    onDestinationChosen: (Long?) -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getDatabase(context)
        PhotoOrganizerRepository(db.groupeDao(), db.albumDao(), db.albumPhotoDao(), db.favoriDao())
    }

    var stack by remember { mutableStateOf<List<Long>>(emptyList()) }
    val currentParentId = stack.lastOrNull()

    val groupesFlow = remember(currentParentId) {
        repository.getGroupesByParent(currentParentId, true)
    }
    val groupes by groupesFlow.collectAsState(initial = emptyList())
    val visibleGroupes = groupes.filter { it.id != excludeGroupeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choisir un groupe") },
                navigationIcon = {
                    if (stack.isNotEmpty()) {
                        IconButton(onClick = { stack = stack.dropLast(1) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onCancel) { Text("Annuler") }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { onDestinationChosen(currentParentId) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(if (currentParentId == null) "Déplacer ici (racine)" else "Déplacer ici")
            }
        }
    ) { padding ->
        if (visibleGroupes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucun sous-groupe ici.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(visibleGroupes, key = { it.id }) { groupe ->
                    ListItem(
                        headlineContent = { Text(groupe.nom) },
                        leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                        modifier = Modifier.clickable { stack = stack + groupe.id }
                    )
                }
            }
        }
    }
}