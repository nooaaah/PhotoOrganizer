package com.noah.photoorganizer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noah.photoorganizer.ui.screens.AlbumScreen
import com.noah.photoorganizer.ui.screens.FavorisScreen
import com.noah.photoorganizer.ui.screens.PhotoViewerScreen
import com.noah.photoorganizer.ui.screens.RootScreen
import com.noah.photoorganizer.ui.theme.PhotoOrganizerTheme
import com.noah.photoorganizer.ui.tryOpenPhotoExternally
import com.noah.photoorganizer.ui.viewmodel.RootViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoOrganizerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val rootViewModel: RootViewModel = viewModel()
                    val context = LocalContext.current

                    // État partagé pour la visionneuse : la liste de photos ne passe pas
                    // proprement par une route Navigation Compose (trop volumineux), donc
                    // on la garde hors du graphe de navigation, au niveau de l'écran parent.
                    var viewerPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
                    var viewerAlbumId by remember { mutableStateOf<Long?>(null) }

                    NavHost(navController = navController, startDestination = "main") {

                        composable("main") {
                            var currentTab by remember { mutableStateOf(0) }

                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        NavigationBarItem(
                                            selected = currentTab == 0,
                                            onClick = { currentTab = 0 },
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                                            label = { Text("Accueil") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == 1,
                                            onClick = { currentTab = 1 },
                                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoris") },
                                            label = { Text("Favoris") }
                                        )
                                    }
                                }
                            ) { padding ->
                                Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                                    when (currentTab) {
                                        0 -> RootScreen(
                                            viewModel = rootViewModel,
                                            onAlbumClick = { albumId -> navController.navigate("album/$albumId") }
                                        )
                                        1 -> FavorisScreen(
                                            onPhotoClick = { list, index ->
                                                viewerPhotos = list
                                                viewerAlbumId = null
                                                navController.navigate("viewer/$index")
                                            },
                                            onAlbumClick = { albumId -> navController.navigate("album/$albumId") },
                                            onGroupeClick = { groupeId ->
                                                rootViewModel.navigateToGroupeDirect(groupeId)
                                                currentTab = 0
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        composable(
                            route = "album/{albumId}",
                            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
                            AlbumScreen(
                                albumId = albumId,
                                onBack = { navController.popBackStack() },
                                onPhotoClick = { list, index ->
                                    val opened = tryOpenPhotoExternally(context, Uri.parse(list[index]))
                                    if (!opened) {
                                        viewerPhotos = list
                                        viewerAlbumId = albumId
                                        navController.navigate("viewer/$index")
                                    }
                                }
                            )
                        }

                        composable(
                            route = "viewer/{startIndex}",
                            arguments = listOf(navArgument("startIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: 0
                            PhotoViewerScreen(
                                photos = viewerPhotos,
                                startIndex = startIndex,
                                albumId = viewerAlbumId,
                                onBack = { navController.popBackStack() },
                                onDeleted = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}