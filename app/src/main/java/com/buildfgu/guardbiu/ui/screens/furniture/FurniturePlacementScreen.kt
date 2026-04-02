package com.buildfgu.guardbiu.ui.screens.furniture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.FurnitureEntity
import com.buildfgu.guardbiu.data.local.entity.WallPointEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.sqrt

private data class RoomMapping(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val minX: Float,
    val minY: Float
)

private fun computeMapping(
    sortedPoints: List<Offset>,
    canvasSize: Size
): RoomMapping? {
    if (sortedPoints.size < 3 || canvasSize.width <= 0) return null
    val minX = sortedPoints.minOf { it.x }
    val maxX = sortedPoints.maxOf { it.x }
    val minY = sortedPoints.minOf { it.y }
    val maxY = sortedPoints.maxOf { it.y }
    val shapeW = maxX - minX
    val shapeH = maxY - minY
    val margin = 48f
    val availW = canvasSize.width - margin * 2
    val availH = canvasSize.height - margin * 2
    val scale = if (shapeW > 0 && shapeH > 0) minOf(availW / shapeW, availH / shapeH) else 1f
    val oX = (canvasSize.width - shapeW * scale) / 2f
    val oY = (canvasSize.height - shapeH * scale) / 2f
    return RoomMapping(scale, oX, oY, minX, minY)
}

private fun isInsidePolygon(px: Float, py: Float, polygon: List<Offset>): Boolean {
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val yi = polygon[i].y; val yj = polygon[j].y
        val xi = polygon[i].x; val xj = polygon[j].x
        if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
            inside = !inside
        }
        j = i
    }
    return inside
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurniturePlacementScreen(roomId: Long, navController: NavHostController) {
    val viewModel: FurniturePlacementViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val wallPoints by viewModel.wallPoints.collectAsState()
    val furniture by viewModel.furniture.collectAsState()

    val sortedPoints = remember(wallPoints) {
        wallPoints.sortedBy { it.orderIndex }.map { Offset(it.x, it.y) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    val roomFillColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val fitsColor = Color(0xFF00B894)
    val noFitColor = Color(0xFFD63031)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)

    val furnitureColors = remember {
        listOf(
            Color(0xFF6C5CE7), Color(0xFF00B894), Color(0xFFE17055),
            Color(0xFF0984E3), Color(0xFFFDAA5B), Color(0xFFD63031)
        )
    }

    var dragFurnitureId by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val mapping = remember(sortedPoints, canvasSize) {
        computeMapping(sortedPoints, canvasSize)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Furniture Placement") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.autoArrange() }) {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Auto Arrange")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .onSizeChanged { canvasSize = it.toSize() }
                        .pointerInput(sortedPoints, furniture, mapping) {
                            val m = mapping ?: return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    furniture.forEach { item ->
                                        val fx = (item.posX - m.minX) * m.scale + m.offsetX
                                        val fy = (item.posY - m.minY) * m.scale + m.offsetY
                                        val fw = item.width * m.scale
                                        val fd = item.depth * m.scale
                                        if (offset.x in fx..(fx + fw) && offset.y in fy..(fy + fd)) {
                                            dragFurnitureId = item.id
                                            dragOffset = Offset(offset.x - fx, offset.y - fy)
                                        }
                                    }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val id = dragFurnitureId ?: return@detectDragGestures
                                    val newX = (change.position.x - dragOffset.x - m.offsetX) / m.scale + m.minX
                                    val newY = (change.position.y - dragOffset.y - m.offsetY) / m.scale + m.minY
                                    viewModel.updateFurniturePosition(id, newX, newY)
                                },
                                onDragEnd = { dragFurnitureId = null },
                                onDragCancel = { dragFurnitureId = null }
                            )
                        }
                ) {
                    val gridSpacing = 50.dp.toPx()
                    var gx = 0f
                    while (gx <= size.width) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 0.5f)
                        gx += gridSpacing
                    }
                    var gy = 0f
                    while (gy <= size.height) {
                        drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 0.5f)
                        gy += gridSpacing
                    }

                    if (sortedPoints.size >= 3) {
                        val m = mapping ?: return@Canvas
                        fun mapped(pt: Offset) = Offset(
                            (pt.x - m.minX) * m.scale + m.offsetX,
                            (pt.y - m.minY) * m.scale + m.offsetY
                        )

                        val roomPath = Path().apply {
                            val first = mapped(sortedPoints.first())
                            moveTo(first.x, first.y)
                            sortedPoints.drop(1).forEach { lineTo(mapped(it).x, mapped(it).y) }
                            close()
                        }
                        drawPath(roomPath, roomFillColor, style = Fill)
                        drawPath(roomPath, outlineColor, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))

                        furniture.forEachIndexed { idx, item ->
                            val fx = (item.posX - m.minX) * m.scale + m.offsetX
                            val fy = (item.posY - m.minY) * m.scale + m.offsetY
                            val fw = item.width * m.scale
                            val fd = item.depth * m.scale
                            val baseColor = furnitureColors[idx % furnitureColors.size]

                            val corners = listOf(
                                Offset(item.posX, item.posY),
                                Offset(item.posX + item.width, item.posY),
                                Offset(item.posX, item.posY + item.depth),
                                Offset(item.posX + item.width, item.posY + item.depth)
                            )
                            val fits = corners.all { isInsidePolygon(it.x, it.y, sortedPoints) }
                            val borderColor = if (fits) fitsColor else noFitColor

                            drawRect(
                                color = baseColor.copy(alpha = 0.3f),
                                topLeft = Offset(fx, fy),
                                size = Size(fw, fd),
                                style = Fill
                            )
                            drawRect(
                                color = borderColor,
                                topLeft = Offset(fx, fy),
                                size = Size(fw, fd),
                                style = Stroke(width = 2.5.dp.toPx())
                            )

                            val textResult = textMeasurer.measure(item.name, labelStyle)
                            val textX = fx + (fw - textResult.size.width) / 2f
                            val textY = fy + (fd - textResult.size.height) / 2f

                            drawRect(
                                color = baseColor.copy(alpha = 0.7f),
                                topLeft = Offset(textX - 4, textY - 2),
                                size = Size(textResult.size.width + 8f, textResult.size.height + 4f)
                            )
                            drawText(textResult, topLeft = Offset(textX, textY))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Furniture Items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = fitsColor,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Text("Fits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = noFitColor,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Text("Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (furniture.isEmpty()) {
                        Text(
                            "No furniture to place. Add some first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(furniture, key = { it.id }) { item ->
                                val idx = furniture.indexOf(item)
                                val color = furnitureColors[idx % furnitureColors.size]
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            "${item.name} (%.1f×%.1f)".format(item.width, item.depth),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    icon = {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = color,
                                            modifier = Modifier.size(12.dp)
                                        ) {}
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class FurniturePlacementViewModel(
    private val roomId: Long,
    private val repository: AppRepository
) : ViewModel() {

    private val _wallPoints = MutableStateFlow<List<WallPointEntity>>(emptyList())
    val wallPoints: StateFlow<List<WallPointEntity>> = _wallPoints.asStateFlow()

    private val _furniture = MutableStateFlow<List<FurnitureEntity>>(emptyList())
    val furniture: StateFlow<List<FurnitureEntity>> = _furniture.asStateFlow()

    private var hasAutoPlaced = false

    init {
        viewModelScope.launch {
            combine(
                repository.getWallPoints(roomId),
                repository.getFurnitureByRoom(roomId)
            ) { points, items -> Pair(points, items) }
                .collect { (points, items) ->
                    _wallPoints.value = points
                    _furniture.value = items
                    if (!hasAutoPlaced) {
                        autoPlaceUnpositioned(points, items)
                    }
                }
        }
    }

    private fun autoPlaceUnpositioned(points: List<WallPointEntity>, items: List<FurnitureEntity>) {
        if (points.isEmpty() || items.isEmpty()) return
        val unpositioned = items.filter { it.posX == 0f && it.posY == 0f }
        if (unpositioned.isEmpty()) return

        hasAutoPlaced = true
        val sorted = points.sortedBy { it.orderIndex }
        val minX = sorted.minOf { it.x }
        val maxX = sorted.maxOf { it.x }
        val minY = sorted.minOf { it.y }
        val maxY = sorted.maxOf { it.y }
        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2
        val spacing = (maxX - minX) * 0.15f

        viewModelScope.launch {
            unpositioned.forEachIndexed { idx, item ->
                val col = idx % 3 - 1
                val row = idx / 3
                repository.updateFurniture(
                    item.copy(
                        posX = centerX + col * spacing - item.width / 2,
                        posY = centerY + row * spacing - item.depth / 2
                    )
                )
            }
        }
    }

    fun autoArrange() {
        val points = _wallPoints.value
        val items = _furniture.value
        if (points.isEmpty() || items.isEmpty()) return

        val sorted = points.sortedBy { it.orderIndex }
        val minX = sorted.minOf { it.x }
        val maxX = sorted.maxOf { it.x }
        val minY = sorted.minOf { it.y }
        val maxY = sorted.maxOf { it.y }
        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2
        val spacing = (maxX - minX) * 0.15f

        viewModelScope.launch {
            items.forEachIndexed { idx, item ->
                val col = idx % 3 - 1
                val row = idx / 3
                repository.updateFurniture(
                    item.copy(
                        posX = centerX + col * spacing - item.width / 2,
                        posY = centerY + row * spacing - item.depth / 2
                    )
                )
            }
        }
    }

    fun updateFurniturePosition(id: Long, x: Float, y: Float) {
        viewModelScope.launch {
            val item = repository.getFurniture(id)
            if (item != null) {
                repository.updateFurniture(item.copy(posX = x, posY = y))
            }
        }
    }
}
