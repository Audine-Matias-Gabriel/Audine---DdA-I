package com.audine.dedalo.projects.ui.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.audine.dedalo.projects.data.ImageData
import com.audine.dedalo.projects.data.ImageType
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
    viewModel: ProjectDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    onOpenInMap: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingImageUri by viewModel.pendingImageUri.collectAsStateWithLifecycle()
    val showAddStageDialog by viewModel.showAddStageDialog.collectAsStateWithLifecycle()
    val stageStatusDialogStage by viewModel.stageStatusDialog.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImagePicked(it) }
    }

    if (pendingImageUri != null) {
        ImageDestinationDialog(
            stages = (uiState as? ProjectDetailUiState.Success)?.project?.stages ?: emptyList(),
            onConfirm = { destination -> viewModel.confirmAddImage(destination) },
            onDismiss = { viewModel.cancelImageDestination() }
        )
    }

    if (showAddStageDialog) {
        AddStageDialog(
            onConfirm = { name -> viewModel.addStage(name) },
            onDismiss = { viewModel.hideAddStageDialog() }
        )
    }

    stageStatusDialogStage?.let { stage ->
        StageStatusDialog(
            stage = stage,
            onSelectStatus = { status -> viewModel.updateStageStatus(stage, status) },
            onDismiss = { viewModel.hideStageStatusDialog() }
        )
    }

    Scaffold(
        modifier = modifier,
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
                },
                actions = {
                    if (uiState is ProjectDetailUiState.Success) {
                        IconButton(onClick = onNavigateToEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar obra")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                        GeneralInfoSection(
                            project = state.project,
                            onOpenInMap = onOpenInMap
                        )
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Galería",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar")
                            }
                        }
                    }
                    if (state.project.images.isNotEmpty()) {
                        item {
                            GallerySection(
                                images = state.project.images,
                                onImageClick = onNavigateToImageViewer
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Sin imágenes aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Etapas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.showAddStageDialog() }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar")
                            }
                        }
                    }

                    if (state.project.stages.isEmpty()) {
                        item {
                            Text(
                                text = "Sin etapas aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.project.stages, key = { it.id }) { stage ->
                            StageCard(
                                stage = stage,
                                onClick = { viewModel.showStageStatusDialog(stage) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageDestinationDialog(
    stages: List<Stage>,
    onConfirm: (ImageDestination) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDestination by remember { mutableStateOf<ImageDestination>(ImageDestination.Project) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar imagen a...") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { selectedDestination = ImageDestination.Project }
                ) {
                    RadioButton(
                        selected = selectedDestination is ImageDestination.Project,
                        onClick = { selectedDestination = ImageDestination.Project }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Galería de la obra", style = MaterialTheme.typography.bodyLarge)
                }
                if (stages.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Etapas:", style = MaterialTheme.typography.labelMedium)
                    stages.forEach { stage ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                selectedDestination = ImageDestination.Stage(stage.id, stage.nombre)
                            }
                        ) {
                            RadioButton(
                                selected = selectedDestination is ImageDestination.Stage &&
                                    (selectedDestination as ImageDestination.Stage).stageId == stage.id,
                                onClick = {
                                    selectedDestination = ImageDestination.Stage(stage.id, stage.nombre)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stage.nombre, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDestination) }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AddStageDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar etapa") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la etapa") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun StageStatusDialog(
    stage: Stage,
    onSelectStatus: (StageStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stage.nombre) },
        text = {
            Column {
                StageStatus.entries.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStatus(status) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = stage.estado == status,
                            onClick = { onSelectStatus(status) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (status) {
                                    StageStatus.ESPERA -> "Espera"
                                    StageStatus.EN_PROGRESO -> "En Progreso"
                                    StageStatus.FINALIZADA -> "Finalizada"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            stage.fechaInicio?.let { inicio ->
                                Text(
                                    text = "Inicio: ${dateFormat.format(Date(inicio))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun GeneralInfoSection(
    project: Project,
    onOpenInMap: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(Icons.Outlined.LocationOn, "Dirección", project.direccion)
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Outlined.Straighten, "Latitud", project.latitud.toString())
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Outlined.Straighten, "Longitud", project.longitud.toString())
            if (project.fos.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                InfoRow(Icons.Outlined.Image, "FOS", project.fos)
            }
            if (project.fot.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                InfoRow(Icons.Outlined.Image, "FOT", project.fot)
            }
            if (project.latitud != 0.0 || project.longitud != 0.0) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Surface(
                    onClick = { onOpenInMap(project.latitud, project.longitud) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ver en mapa",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
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
}

@Composable
private fun StageCard(
    stage: Stage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            if (stage.images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(stage.images.take(5)) { image ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagen de etapa",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageBadge(status: StageStatus) {
    val (text, color) = when (status) {
        StageStatus.ESPERA -> "Espera" to MaterialTheme.colorScheme.outline
        StageStatus.EN_PROGRESO -> "En Progreso" to MaterialTheme.colorScheme.secondary
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
    images: List<ImageData>,
    onImageClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images, key = { it.url }) { image ->
            Column {
                Box(modifier = Modifier.size(width = 150.dp, height = 120.dp)) {
                    AsyncImage(
                        model = image.url,
                        contentDescription = "Imagen de obra",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(image.url) },
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (image.type == ImageType.PLAN)
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (image.type == ImageType.PLAN) "Plano" else "Foto",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}




