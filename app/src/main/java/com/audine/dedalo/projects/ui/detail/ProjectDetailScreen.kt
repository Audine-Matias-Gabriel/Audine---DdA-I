package com.audine.dedalo.projects.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.audine.dedalo.projects.data.Project
import com.audine.dedalo.projects.data.Stage
import com.audine.dedalo.projects.data.StageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    uiState: ProjectDetailUiState,
    onNavigateBack: () -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = uiState) {
                            is ProjectDetailUiState.Success -> state.project.nombre
                            else -> "Detalle de Obra"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProjectDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProjectDetailUiState.Error -> {
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
            is ProjectDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        GeneralInfoSection(state.project)
                    }

                    if (state.project.stages.isNotEmpty()) {
                        item {
                            Text(
                                text = "Etapas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(state.project.stages, key = { it.id }) { stage ->
                            StageCard(stage)
                        }
                    }

                    if (state.project.imageUrls.isNotEmpty()) {
                        item {
                            GallerySection(
                                imageUrls = state.project.imageUrls,
                                onImageClick = onNavigateToImageViewer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneralInfoSection(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow("Dirección", project.direccion)
            Spacer(Modifier.height(4.dp))
            InfoRow("Latitud", project.latitud.toString())
            Spacer(Modifier.height(4.dp))
            InfoRow("Longitud", project.longitud.toString())
            if (project.fos.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                InfoRow("FOS", project.fos)
            }
            if (project.fot.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                InfoRow("FOT", project.fot)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StageCard(stage: Stage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                stage.fechaInicio?.let { inicio ->
                    Text(
                        text = "Inicio: ${dateFormat.format(Date(inicio))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                stage.fechaFin?.let { fin ->
                    Text(
                        text = "Fin: ${dateFormat.format(Date(fin))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            StageBadge(stage.estado)
        }
    }
}

@Composable
private fun StageBadge(status: StageStatus) {
    val (text, color) = when (status) {
        StageStatus.ESPERA -> "Espera" to MaterialTheme.colorScheme.outline
        StageStatus.EN_PROGRESO -> "En Progreso" to MaterialTheme.colorScheme.primary
        StageStatus.FINALIZADA -> "Finalizada" to MaterialTheme.colorScheme.tertiary
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GallerySection(
    imageUrls: List<String>,
    onImageClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Galería",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imageUrls, key = { it }) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Imagen de obra",
                    modifier = Modifier
                        .size(width = 150.dp, height = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(url) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
