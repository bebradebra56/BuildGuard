package com.buildfgu.guardbiu.ui.screens.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.RoomEntity
import com.buildfgu.guardbiu.data.local.entity.WallPointEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import com.buildfgu.guardbiu.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScanScreen(roomId: Long, navController: NavHostController) {
    val viewModel: ScanViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    var points by remember { mutableStateOf(listOf<Offset>()) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Scan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Tap to place corner points of your room",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }

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
                        .background(surfaceColor)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                points = points + offset
                            }
                        }
                ) {
                    val gridSpacing = 50.dp.toPx()
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += gridSpacing
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += gridSpacing
                    }

                    if (points.size >= 2) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = primaryColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    if (points.size >= 3) {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = points.last(),
                            end = points.first(),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    points.forEach { point ->
                        drawCircle(
                            color = primaryColor,
                            radius = 8.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            AnimatedVisibility(visible = points.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "${points.size} point${if (points.size != 1) "s" else ""} placed",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (points.isNotEmpty()) points = points.dropLast(1) },
                    enabled = points.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Rounded.Undo, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Undo", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = { points = emptyList() },
                    enabled = points.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Clear, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Clear", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Button(
                    onClick = {
                        viewModel.saveWallPoints(points, 1f, 1f)
                        navController.navigate(Screen.ScanResult.createRoute(roomId))
                    },
                    enabled = points.size >= 3,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Done", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(roomId: Long, navController: NavHostController) {
    val viewModel: ScanViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val wallPoints by viewModel.wallPoints.collectAsState()
    val room by viewModel.room.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val offsets = remember(wallPoints) {
        wallPoints.sortedBy { it.orderIndex }.map { Offset(it.x, it.y) }
    }
    val area = remember(offsets) { if (offsets.size >= 3) viewModel.calculateArea(offsets) else 0f }
    val perimeter = remember(offsets) { if (offsets.size >= 3) viewModel.calculatePerimeter(offsets) else 0f }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (offsets.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
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

                        val minX = offsets.minOf { it.x }
                        val maxX = offsets.maxOf { it.x }
                        val minY = offsets.minOf { it.y }
                        val maxY = offsets.maxOf { it.y }
                        val shapeW = maxX - minX
                        val shapeH = maxY - minY
                        val scale = if (shapeW > 0 && shapeH > 0) {
                            minOf(size.width / shapeW, size.height / shapeH) * 0.8f
                        } else 1f
                        val offsetX = (size.width - shapeW * scale) / 2f
                        val offsetY = (size.height - shapeH * scale) / 2f

                        val path = Path().apply {
                            val first = offsets.first()
                            moveTo((first.x - minX) * scale + offsetX, (first.y - minY) * scale + offsetY)
                            offsets.drop(1).forEach { pt ->
                                lineTo((pt.x - minX) * scale + offsetX, (pt.y - minY) * scale + offsetY)
                            }
                            close()
                        }
                        drawPath(path, fillColor, style = Fill)
                        drawPath(path, primaryColor, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))

                        offsets.forEach { pt ->
                            drawCircle(
                                primaryColor,
                                radius = 5.dp.toPx(),
                                center = Offset((pt.x - minX) * scale + offsetX, (pt.y - minY) * scale + offsetY)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = room?.name ?: "Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Area", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.1f sq units".format(area), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Perimeter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.1f units".format(perimeter), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }
                    Text(
                        "${offsets.size} corners",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.LayoutEditor.createRoute(roomId)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Layout", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Measurements.createRoute(roomId)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Straighten, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Measure", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Furniture.createRoute(roomId)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Weekend, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("Furnish", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

class ScanViewModel(
    private val roomId: Long,
    private val repository: AppRepository
) : ViewModel() {

    private val _wallPoints = MutableStateFlow<List<WallPointEntity>>(emptyList())
    val wallPoints: StateFlow<List<WallPointEntity>> = _wallPoints.asStateFlow()

    private val _room = MutableStateFlow<RoomEntity?>(null)
    val room: StateFlow<RoomEntity?> = _room.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getWallPoints(roomId).collect { _wallPoints.value = it }
        }
        viewModelScope.launch {
            _room.value = repository.getRoom(roomId)
        }
    }

    fun saveWallPoints(points: List<Offset>, canvasWidth: Float, canvasHeight: Float) {
        viewModelScope.launch {
            repository.deleteWallPoints(roomId)
            val entities = points.mapIndexed { index, offset ->
                WallPointEntity(
                    roomId = roomId,
                    x = offset.x,
                    y = offset.y,
                    orderIndex = index
                )
            }
            repository.insertWallPoints(entities)
        }
    }

    fun calculateArea(points: List<Offset>): Float {
        if (points.size < 3) return 0f
        var sum = 0f
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y
            sum -= points[j].x * points[i].y
        }
        return abs(sum) / 2f
    }

    fun calculatePerimeter(points: List<Offset>): Float {
        if (points.size < 2) return 0f
        var perimeter = 0f
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val dx = points[j].x - points[i].x
            val dy = points[j].y - points[i].y
            perimeter += sqrt(dx * dx + dy * dy)
        }
        return perimeter
    }
}
