package com.buildfgu.guardbiu.ui.screens.materials

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.MaterialEntity
import com.buildfgu.guardbiu.data.local.entity.RoomEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import com.buildfgu.guardbiu.ui.navigation.Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(navController: NavHostController) {
    val viewModel: MaterialsViewModel = koinViewModel()
    val materials by viewModel.materials.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materials", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Material")
            }
        }
    ) { padding ->
        val filtered = if (selectedCategory == null) materials
        else materials.filter { it.category == selectedCategory }

        if (materials.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No materials yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap + to add your first material",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") },
                            leadingIcon = if (selectedCategory == null) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) },
                            leadingIcon = if (selectedCategory == category) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                val grouped = filtered.groupBy { it.category }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(items, key = { it.id }) { material ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteMaterial(material)
                                        true
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                            MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.surface,
                                        label = "bg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, MaterialTheme.shapes.medium)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                MaterialCard(material)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMaterialDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, unit, price, category ->
                viewModel.addMaterial(name, unit, price, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MaterialCard(material: MaterialEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", material.pricePerUnit)} / ${material.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(material.category, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Float, String) -> Unit
) {
    val units = listOf("m²", "m", "L", "kg", "pcs")
    val categoriesList = listOf("Flooring", "Paint", "Tiles", "Wood", "Plumbing", "Electrical", "Other")

    var name by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(units[0]) }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categoriesList[0]) }
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { selectedUnit = it; unitExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per Unit") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categoriesList.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { selectedCategory = it; categoryExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = price.toFloatOrNull() ?: 0f
                    if (name.isNotBlank() && p > 0f) onAdd(name, selectedUnit, p, selectedCategory)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCalculatorScreen(roomId: Long, navController: NavHostController) {
    val viewModel: MaterialsViewModel = koinViewModel()
    val materials by viewModel.materials.collectAsState()
    val room by viewModel.room.collectAsState()

    LaunchedEffect(roomId) { viewModel.loadRoom(roomId) }

    var selectedMaterial by remember { mutableStateOf<MaterialEntity?>(null) }
    var wastage by remember { mutableStateOf("10") }
    var materialExpanded by remember { mutableStateOf(false) }

    val roomArea = room?.let { it.width * it.length } ?: 0f
    val wastagePercent = wastage.toFloatOrNull() ?: 0f
    val materialNeeded = roomArea * (1 + wastagePercent / 100f)
    val estimatedCost = selectedMaterial?.let { materialNeeded * it.pricePerUnit } ?: 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Room Info", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = room?.name ?: "Loading…",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (room != null) {
                            Text(
                                text = "Area: ${String.format("%.1f", roomArea)} m²  (${room!!.width} × ${room!!.length})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item {
                Text("Select Material", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = materialExpanded,
                    onExpandedChange = { materialExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMaterial?.name ?: "Choose material",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = materialExpanded, onDismissRequest = { materialExpanded = false }) {
                        materials.forEach { mat ->
                            DropdownMenuItem(
                                text = { Text("${mat.name} — $${String.format("%.2f", mat.pricePerUnit)}/${mat.unit}") },
                                onClick = { selectedMaterial = mat; materialExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = wastage,
                    onValueChange = { wastage = it },
                    label = { Text("Wastage %") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Calculation Results", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        HorizontalDivider()
                        ResultRow("Room Area", "${String.format("%.1f", roomArea)} m²")
                        ResultRow("Material Needed", "${String.format("%.1f", materialNeeded)} ${selectedMaterial?.unit ?: "m²"}")
                        ResultRow("Estimated Cost", "$${String.format("%.2f", estimatedCost)}")
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Shopping List")
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

class MaterialsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val materials: StateFlow<List<MaterialEntity>> = repository.getAllMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = materials.map { list ->
        list.map { it.category }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _room = MutableStateFlow<RoomEntity?>(null)
    val room: StateFlow<RoomEntity?> = _room.asStateFlow()

    init {
        seedDefaults()
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addMaterial(name: String, unit: String, pricePerUnit: Float, category: String) {
        viewModelScope.launch {
            repository.insertMaterial(
                MaterialEntity(name = name, unit = unit, pricePerUnit = pricePerUnit, category = category)
            )
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }

    fun loadRoom(roomId: Long) {
        viewModelScope.launch {
            _room.value = repository.getRoom(roomId)
        }
    }

    private fun seedDefaults() {
        viewModelScope.launch {
            val current = repository.getAllMaterials().first()
            if (current.isEmpty()) {
                val defaults = listOf(
                    MaterialEntity(name = "Paint", unit = "L", pricePerUnit = 25f, category = "Paint"),
                    MaterialEntity(name = "Laminate", unit = "m²", pricePerUnit = 30f, category = "Flooring"),
                    MaterialEntity(name = "Tiles", unit = "m²", pricePerUnit = 20f, category = "Tiles"),
                    MaterialEntity(name = "Wallpaper", unit = "m²", pricePerUnit = 15f, category = "Other"),
                    MaterialEntity(name = "Plywood", unit = "m²", pricePerUnit = 18f, category = "Wood")
                )
                defaults.forEach { repository.insertMaterial(it) }
            }
        }
    }
}
