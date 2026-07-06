package com.noah.photoorganizer.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNomCoverDialog(
    titre: String,
    nomInitial: String,
    coverUriInitial: String?,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, coverUri: String?) -> Unit
) {
    var nom by remember { mutableStateOf(nomInitial) }
    var coverUri by remember { mutableStateOf(coverUriInitial) }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        PhotoPickerScreen(
            onCancel = { showPicker = false },
            onConfirm = { selected ->
                coverUri = selected.firstOrNull()?.uri?.toString()
                showPicker = false
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titre) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri != null) {
                        AsyncImage(
                            model = Uri.parse(coverUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = "Changer la couverture")
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nom, coverUri) }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}