package com.audine.dedalo.projects.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.audine.dedalo.core.data.remote.LocationiqResponse
import com.audine.dedalo.core.ui.theme.DedaloTheme
import com.audine.dedalo.projects.data.ImageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onNavigateBack: () -> Unit
) {
    val nombre by viewModel.nombre.collectAsStateWithLifecycle()
    val direccion by viewModel.direccion.collectAsStateWithLifecycle()
    val fos by viewModel.fos.collectAsStateWithLifecycle()
    val fot by viewModel.fot.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { viewModel.addImageUri(it) }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onNavigateBack()
    }

    val isFormValid = nombre.isNotBlank() && direccion.isNotBlank()

    CreateProjectContent(
        nombre = nombre,
        onNombreChange = viewModel::onNombreChange,
        direccion = direccion,
        onDireccionChange = viewModel::onDireccionChange,
        fos = fos,
        onFosChange = viewModel::onFosChange,
        fot = fot,
        onFotChange = viewModel::onFotChange,
        selectedImages = selectedImages,
        suggestions = suggestions,
        onAddImage = { imagePickerLauncher.launch("image/*") },
        onRemoveImage = viewModel::removeImageUri,
        onSetImageType = viewModel::setImageType,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        isSaving = isSaving,
        saveError = saveError,
        isFormValid = isFormValid,
        onSave = viewModel::createProject,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectContent(
    nombre: String,
    onNombreChange: (String) -> Unit,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    fos: String,
    onFosChange: (String) -> Unit,
    fot: String,
    onFotChange: (String) -> Unit,
    selectedImages: List<SelectedImage>,
    suggestions: List<LocationiqResponse>,
    onAddImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSetImageType: (Uri, ImageType) -> Unit,
    onSuggestionSelected: (LocationiqResponse) -> Unit,
    isSaving: Boolean,
    saveError: String?,
    isFormValid: Boolean,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva obra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Información general")
            }

            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Box {
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { onDireccionChange(it); showSuggestions = true },
                        label = { Text("Dirección") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (showSuggestions && suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                suggestions.forEachIndexed { index, suggestion ->
                                    Text(
                                        text = suggestion.displayName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSuggestionSelected(suggestion)
                                                showSuggestions = false
                                            }
                                            .padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )
                                    if (index < suggestions.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Parámetros urbanísticos")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fos,
                        onValueChange = onFosChange,
                        label = { Text("FOS") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = fot,
                        onValueChange = onFotChange,
                        label = { Text("FOT") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                SectionHeader("Imágenes")
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImages, key = { it.uri.toString() }) { selected ->
                        Column(modifier = Modifier.width(100.dp)) {
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(selected.uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onRemoveImage(selected.uri) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = selected.type == ImageType.PHOTO,
                                    onClick = { onSetImageType(selected.uri, ImageType.PHOTO) },
                                    label = { Text("Foto", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selected.type == ImageType.PLAN,
                                    onClick = { onSetImageType(selected.uri, ImageType.PLAN) },
                                    label = { Text("Plano", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = !isSaving) { onAddImage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar imagen",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                saveError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Guardando..." else "Guardar obra")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun CreateProjectContentEmptyPreview() {
    DedaloTheme {
        CreateProjectContent(
            nombre = "", onNombreChange = {},
            direccion = "", onDireccionChange = {},
            fos = "", onFosChange = {},
            fot = "", onFotChange = {},
            selectedImages = emptyList(), suggestions = emptyList(),
            onAddImage = {}, onRemoveImage = {}, onSetImageType = { _, _ -> },
            onSuggestionSelected = {},
            isSaving = false, saveError = null, isFormValid = false,
            onSave = {}, onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun CreateProjectContentFilledPreview() {
    DedaloTheme {
        CreateProjectContent(
            nombre = "Edificio Central", onNombreChange = {},
            direccion = "Av. Siempre Viva 742", onDireccionChange = {},
            fos = "0.6", onFosChange = {},
            fot = "2.5", onFotChange = {},
            selectedImages = emptyList(), suggestions = emptyList(),
            onAddImage = {}, onRemoveImage = {}, onSetImageType = { _, _ -> },
            onSuggestionSelected = {},
            isSaving = false, saveError = null, isFormValid = true,
            onSave = {}, onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun CreateProjectContentSavingPreview() {
    DedaloTheme {
        CreateProjectContent(
            nombre = "Edificio Central", onNombreChange = {},
            direccion = "Av. Siempre Viva 742", onDireccionChange = {},
            fos = "0.6", onFosChange = {},
            fot = "2.5", onFotChange = {},
            selectedImages = emptyList(), suggestions = emptyList(),
            onAddImage = {}, onRemoveImage = {}, onSetImageType = { _, _ -> },
            onSuggestionSelected = {},
            isSaving = true, saveError = null, isFormValid = true,
            onSave = {}, onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun CreateProjectContentSuggestionsPreview() {
    DedaloTheme {
        CreateProjectContent(
            nombre = "", onNombreChange = {},
            direccion = "Av.", onDireccionChange = {},
            fos = "", onFosChange = {},
            fot = "", onFotChange = {},
            selectedImages = emptyList(),
            suggestions = listOf(
                LocationiqResponse(lat = "-34.6037", lon = "-58.3816", displayName = "Av. Siempre Viva 742, CABA"),
                LocationiqResponse(lat = "-34.6020", lon = "-58.3800", displayName = "Av. Siempre Viva 750, CABA")
            ),
            onAddImage = {}, onRemoveImage = {}, onSetImageType = { _, _ -> },
            onSuggestionSelected = {},
            isSaving = false, saveError = null, isFormValid = false,
            onSave = {}, onNavigateBack = {}
        )
    }
}
