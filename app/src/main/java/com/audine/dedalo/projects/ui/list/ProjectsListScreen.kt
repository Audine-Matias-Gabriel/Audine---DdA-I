package com.audine.dedalo.projects.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.audine.dedalo.projects.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    uiState: ProjectsUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateProject: () -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    isSyncing: Boolean = false,
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Buscar obras...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            focusedBorderColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSync, enabled = !isSyncing) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProject,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear obra")
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProjectsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProjectsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ProjectsUiState.Success -> {
                if (state.projects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Construction,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Todavía no hay obras" else "No se encontraron obras",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.projects, key = { it.id }) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { onNavigateToProjectDetail(project.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    val lastImage = project.images.lastOrNull()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Text(
                    text = project.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (project.fos.isNotBlank() || project.fot.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (project.fos.isNotBlank()) {
                            Text(
                                text = "FOS: ${project.fos}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (project.fot.isNotBlank()) {
                            Text(
                                text = "FOT: ${project.fot}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (lastImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(lastImage.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Última imagen",
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

private val previewProject1 = Project(
    id = "1", userId = "", nombre = "Edificio Central",
    direccion = "Av. Siempre Viva 742", latitud = -34.6037, longitud = -58.3816,
    fos = "0.6", fot = "2.5"
)
private val previewProject2 = Project(
    id = "2", userId = "", nombre = "Torre Empresarial",
    direccion = "Calle Principal 123", latitud = -34.6020, longitud = -58.3800,
    fos = "0.8", fot = "3.0"
)

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ProjectsListLoadingPreview() {
    DedaloTheme { ProjectsListScreen(uiState = ProjectsUiState.Loading, searchQuery = "", onSearchQueryChange = {}, onCreateProject = {}, onNavigateToProjectDetail = {}) }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ProjectsListEmptyPreview() {
    DedaloTheme { ProjectsListScreen(uiState = ProjectsUiState.Success(emptyList()), searchQuery = "", onSearchQueryChange = {}, onCreateProject = {}, onNavigateToProjectDetail = {}) }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ProjectsListSuccessPreview() {
    DedaloTheme { ProjectsListScreen(uiState = ProjectsUiState.Success(listOf(previewProject1, previewProject2)), searchQuery = "", onSearchQueryChange = {}, onCreateProject = {}, onNavigateToProjectDetail = {}) }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_6")
@Composable
private fun ProjectsListWithNavPreview() {
    DedaloTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = true, onClick = {},
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Obras") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                        selected = false, onClick = {},
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        ) { innerPadding ->
            ProjectsListScreen(
                uiState = ProjectsUiState.Success(listOf(previewProject1, previewProject2)),
                searchQuery = "",
                onSearchQueryChange = {},
                onCreateProject = {},
                onNavigateToProjectDetail = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
