package com.buildfgu.guardbiu.ui.screens.furniture

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.FurnitureEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import com.buildfgu.guardbiu.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private data class FurnitureTemplate(val name: String, val width: Float, val depth: Float)

private val templates = listOf(
    FurnitureTemplate("Sofa", 2.0f, 0.9f),
    FurnitureTemplate("Table", 1.2f, 0.8f),
    FurnitureTemplate("Bed", 2.0f, 1.6f),
    FurnitureTemplate("Chair", 0.5f, 0.5f),
    FurnitureTemplate("Wardrobe", 1.8f, 0.6f),
    FurnitureTemplate("Desk", 1.2f, 0.6f)
)

private fun furnitureIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        "sofa" in lower || "couch" in lower -> Icons.Rounded.Weekend
        "table" in lower -> Icons.Rounded.TableBar
        "bed" in lower -> Icons.Rounded.Bed
        "chair" in lower -> Icons.Rounded.Chair
        "wardrobe" in lower || "closet" in lower -> Icons.Rounded.Inventory2
        "desk" in lower -> Icons.Rounded.Laptop
        else -> Icons.Rounded.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureScreen(roomId: Long, navController: NavHostController) {
    val viewModel: FurnitureViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val furniture by viewModel.furniture.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Furniture") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Furniture")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Button(
                onClick = { navController.navigate(Screen.FurniturePlacement.createRoute(roomId)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Rounded.GridView, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Furniture Placement View", fontWeight = FontWeight.Medium)
            }

            Text(
                "Quick Add",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { template ->
                    AssistChip(
                        onClick = { viewModel.addFromTemplate(template.name, template.width, template.depth) },
                        label = { Text("${template.name} (${template.width}×${template.depth})") },
                        leadingIcon = {
                            Icon(
                                furnitureIcon(template.name),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (furniture.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Weekend,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No furniture added yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Use quick add chips or tap + to add furniture",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(furniture, key = { it.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteFurniture(item)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                furnitureIcon(item.name),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "%.1f × %.1f m".format(item.width, item.depth),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
        }
    }

    if (showDialog) {
        AddFurnitureDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, width, depth ->
                viewModel.addFurniture(name, width, depth)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AddFurnitureDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var depth by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Add Furniture", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Sofa") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it },
                        label = { Text("Width (m)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = depth,
                        onValueChange = { depth = it },
                        label = { Text("Depth (m)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = width.toFloatOrNull()
                    val d = depth.toFloatOrNull()
                    if (w != null && d != null) onConfirm(name, w, d)
                },
                enabled = name.isNotBlank() && width.toFloatOrNull() != null && depth.toFloatOrNull() != null,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
        }
    )
}

class FurnitureViewModel(
    private val roomId: Long,
    private val repository: AppRepository
) : ViewModel() {

    private val _furniture = MutableStateFlow<List<FurnitureEntity>>(emptyList())
    val furniture: StateFlow<List<FurnitureEntity>> = _furniture.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFurnitureByRoom(roomId).collect { _furniture.value = it }
        }
    }

    fun addFurniture(name: String, width: Float, depth: Float) {
        viewModelScope.launch {
            repository.insertFurniture(
                FurnitureEntity(roomId = roomId, name = name, width = width, depth = depth)
            )
        }
    }

    fun addFromTemplate(name: String, width: Float, depth: Float) {
        addFurniture(name, width, depth)
    }

    fun deleteFurniture(furniture: FurnitureEntity) {
        viewModelScope.launch {
            repository.deleteFurniture(furniture)
        }
    }
}
