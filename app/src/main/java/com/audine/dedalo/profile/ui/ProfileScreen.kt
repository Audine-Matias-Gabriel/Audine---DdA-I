package com.audine.dedalo.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.audine.dedalo.auth.data.UserEntity
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.audine.dedalo.profile.data.GalleryImageEntity
import com.audine.dedalo.profile.domain.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onUploadGalleryImage: (Uri) -> Unit,
    onUploadAvatar: (Uri) -> Unit,
    onClearUploadError: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    isSyncing: Boolean = false,
    onSyncProjects: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.UploadError) {
            snackbarHostState.showSnackbar((uiState as ProfileUiState.UploadError).message)
            onClearUploadError()
        }
    }

    when (val s = uiState) {
        is ProfileUiState.Loading -> LoadingState(modifier)
        is ProfileUiState.Error -> ErrorState(s.message, modifier)
        is ProfileUiState.UploadError -> ProfileContent(
            user = s.user,
            galleryImages = s.galleryImages,
            onUploadGalleryImage = onUploadGalleryImage,
            onUploadAvatar = onUploadAvatar,
            onSignOut = onSignOut,
            onNavigateToImageViewer = onNavigateToImageViewer,
            isSyncing = isSyncing,
            onSyncProjects = onSyncProjects,
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
        is ProfileUiState.Success -> ProfileContent(
            user = s.user,
            galleryImages = s.galleryImages,
            onUploadGalleryImage = onUploadGalleryImage,
            onUploadAvatar = onUploadAvatar,
            onSignOut = onSignOut,
            onNavigateToImageViewer = onNavigateToImageViewer,
            isSyncing = isSyncing,
            onSyncProjects = onSyncProjects,
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Cargando...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ProfileContent(
    user: UserEntity,
    galleryImages: List<GalleryImageEntity>,
    onUploadGalleryImage: (Uri) -> Unit,
    onUploadAvatar: (Uri) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    isSyncing: Boolean = false,
    onSyncProjects: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            avatarUri = it
            onUploadAvatar(it)
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            galleryUri = it
            onUploadGalleryImage(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { galleryPicker.launch("image/*") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar imagen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { avatarPicker.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { avatarPicker.launch("image/*") },
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = user.displayName ?: "Sin nombre",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = user.email ?: "Sin email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onSyncProjects,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (isSyncing) "Sincronizando..." else "Sincronizar obras")
                }
            }

            if (galleryImages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Galería vacía — toca + para agregar imágenes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(galleryImages, key = { it.id }) { image ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onNavigateToImageViewer(image.imageUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ProfileScreenPreview() {
    val user = UserEntity(
        id = "test",
        displayName = "Matías Rodríguez",
        email = "matias@example.com",
        photoUrl = null
    )
    val gallery = listOf(
        GalleryImageEntity(id = "1", userId = "test", imageUrl = "https://picsum.photos/400/400?1"),
        GalleryImageEntity(id = "2", userId = "test", imageUrl = "https://picsum.photos/400/400?2"),
        GalleryImageEntity(id = "3", userId = "test", imageUrl = "https://picsum.photos/400/400?3"),
        GalleryImageEntity(id = "4", userId = "test", imageUrl = "https://picsum.photos/400/400?4"),
        GalleryImageEntity(id = "5", userId = "test", imageUrl = "https://picsum.photos/400/400?5"),
    )
    DedaloTheme {
        ProfileScreen(
            uiState = ProfileUiState.Success(user = user, galleryImages = gallery),
            onUploadGalleryImage = {},
            onUploadAvatar = {},
            onClearUploadError = {},
            onSignOut = {},
            onNavigateToImageViewer = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_6")
@Composable
private fun ProfileScreenWithNavPreview() {
    val user = UserEntity(
        id = "test",
        displayName = "Matías Rodríguez",
        email = "matias@example.com",
        photoUrl = null
    )
    DedaloTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = false, onClick = {},
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Obras") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = false, onClick = {},
                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                        label = { Text("Chat") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = true, onClick = {},
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            ProfileScreen(
                uiState = ProfileUiState.Success(user = user, galleryImages = emptyList()),
                onUploadGalleryImage = {},
                onUploadAvatar = {},
                onClearUploadError = {},
                onSignOut = {},
                onNavigateToImageViewer = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
