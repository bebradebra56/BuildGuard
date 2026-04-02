package com.buildfgu.guardbiu.ui.screens.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoorBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Window
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.RoomElementEntity
import com.buildfgu.guardbiu.data.local.entity.RoomEntity
import com.buildfgu.guardbiu.data.local.entity.WallPointEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.sqrt

private enum class EditorMode { SELECT, DOOR, WINDOW, DELETE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutEditorScreen(roomId: Long, navController: NavHostController) {
    val viewModel: LayoutEditorViewModel = koinViewModel(parameters = { parametersOf(roomId) })
    val wallPoints by viewModel.wallPoints.collectAsState()
    val roomElements by viewModel.roomElements.collectAsState()
    val room by viewModel.room.collectAsState()
    var editorMode by remember { mutableStateOf(EditorMode.SELECT) }

    val sortedPoints = remember(wallPoints) {
        wallPoints.sortedBy { it.orderIndex }.map { Offset(it.x, it.y) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    val doorColor = Color(0xFFE67E22)
    val windowColor = Color(0xFF3498DB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layout Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save")
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
            room?.let { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text("Room", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(r.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text("Dimensions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.1f × %.1f m".format(r.width, r.length), style = MaterialTheme.typography.bodyMedium)
                        }
                        Column {
                            Text("Elements", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${roomElements.size}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .pointerInput(editorMode, sortedPoints, roomElements) {
                            detectTapGestures { tapOffset ->
                                if (sortedPoints.size < 2) return@detectTapGestures

                                val minX = sortedPoints.minOf { it.x }
                                val maxX = sortedPoints.maxOf { it.x }
                                val minY = sortedPoints.minOf { it.y }
                                val maxY = sortedPoints.maxOf { it.y }
                                val shapeW = maxX - minX
                                val shapeH = maxY - minY
                                val scale = if (shapeW > 0 && shapeH > 0) {
                                    minOf(size.width / shapeW, size.height / shapeH) * 0.75f
                                } else 1f
                                val oX = (size.width - shapeW * scale) / 2f
                                val oY = (size.height - shapeH * scale) / 2f

                                fun mapPt(pt: Offset) = Offset(
                                    (pt.x - minX) * scale + oX,
                                    (pt.y - minY) * scale + oY
                                )

                                if (editorMode == EditorMode.DELETE) {
                                    var nearestElem: RoomElementEntity? = null
                                    var nearestDist = Float.MAX_VALUE
                                    roomElements.forEach { elem ->
                                        val i = elem.wallIndex.coerceIn(0, sortedPoints.size - 1)
                                        val j = (i + 1) % sortedPoints.size
                                        val a = mapPt(sortedPoints[i])
                                        val b = mapPt(sortedPoints[j])
                                        val pos = elem.positionOnWall.coerceIn(0f, 1f)
                                        val cx = a.x + (b.x - a.x) * pos
                                        val cy = a.y + (b.y - a.y) * pos
                                        val dx = tapOffset.x - cx
                                        val dy = tapOffset.y - cy
                                        val dist = sqrt(dx * dx + dy * dy)
                                        if (dist < nearestDist) {
                                            nearestDist = dist
                                            nearestElem = elem
                                        }
                                    }
                                    if (nearestDist < 60f && nearestElem != null) {
                                        viewModel.removeElement(nearestElem!!)
                                    }
                                } else if (editorMode == EditorMode.DOOR || editorMode == EditorMode.WINDOW) {
                                    var bestDist = Float.MAX_VALUE
                                    var bestWall = 0
                                    var bestPos = 0.5f

                                    for (i in sortedPoints.indices) {
                                        val j = (i + 1) % sortedPoints.size
                                        val a = mapPt(sortedPoints[i])
                                        val b = mapPt(sortedPoints[j])
                                        val ab = b - a
                                        val ap = tapOffset - a
                                        val t = ((ap.x * ab.x + ap.y * ab.y) / (ab.x * ab.x + ab.y * ab.y)).coerceIn(0f, 1f)
                                        val closest = Offset(a.x + t * ab.x, a.y + t * ab.y)
                                        val dx = tapOffset.x - closest.x
                                        val dy = tapOffset.y - closest.y
                                        val dist = sqrt(dx * dx + dy * dy)
                                        if (dist < bestDist) {
                                            bestDist = dist
                                            bestWall = i
                                            bestPos = t
                                        }
                                    }
                                    if (bestDist < 60f) {
                                        val type = if (editorMode == EditorMode.DOOR) "door" else "window"
                                        viewModel.addElement(type, bestWall, bestPos)
                                    }
                                }
                            }
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
                        val minX = sortedPoints.minOf { it.x }
                        val maxX = sortedPoints.maxOf { it.x }
                        val minY = sortedPoints.minOf { it.y }
                        val maxY = sortedPoints.maxOf { it.y }
                        val shapeW = maxX - minX
                        val shapeH = maxY - minY
                        val scale = if (shapeW > 0 && shapeH > 0) {
                            minOf(size.width / shapeW, size.height / shapeH) * 0.75f
                        } else 1f
                        val oX = (size.width - shapeW * scale) / 2f
                        val oY = (size.height - shapeH * scale) / 2f

                        fun mapped(pt: Offset) = Offset((pt.x - minX) * scale + oX, (pt.y - minY) * scale + oY)

                        val roomPath = Path().apply {
                            val first = mapped(sortedPoints.first())
                            moveTo(first.x, first.y)
                            sortedPoints.drop(1).forEach { lineTo(mapped(it).x, mapped(it).y) }
                            close()
                        }
                        drawPath(roomPath, fillColor, style = Fill)
                        drawPath(roomPath, primaryColor, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))

                        sortedPoints.forEach { pt ->
                            drawCircle(primaryColor, radius = 4.dp.toPx(), center = mapped(pt))
                        }

                        roomElements.forEach { elem ->
                            val i = elem.wallIndex.coerceIn(0, sortedPoints.size - 1)
                            val j = (i + 1) % sortedPoints.size
                            val a = mapped(sortedPoints[i])
                            val b = mapped(sortedPoints[j])
                            val pos = elem.positionOnWall.coerceIn(0f, 1f)
                            val cx = a.x + (b.x - a.x) * pos
                            val cy = a.y + (b.y - a.y) * pos

                            if (elem.type == "door") {
                                drawCircle(doorColor, radius = 12.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))
                                drawCircle(doorColor.copy(alpha = 0.2f), radius = 12.dp.toPx(), center = Offset(cx, cy), style = Fill)
                            } else {
                                val halfW = 10.dp.toPx()
                                val halfH = 5.dp.toPx()
                                drawRect(
                                    windowColor,
                                    topLeft = Offset(cx - halfW, cy - halfH),
                                    size = androidx.compose.ui.geometry.Size(halfW * 2, halfH * 2),
                                    style = Fill
                                )
                                drawRect(
                                    windowColor,
                                    topLeft = Offset(cx - halfW, cy - halfH),
                                    size = androidx.compose.ui.geometry.Size(halfW * 2, halfH * 2),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EditorToolButton(
                        icon = Icons.Rounded.TouchApp,
                        label = "Select",
                        selected = editorMode == EditorMode.SELECT,
                        onClick = { editorMode = EditorMode.SELECT }
                    )
                    EditorToolButton(
                        icon = Icons.Rounded.DoorBack,
                        label = "Door",
                        selected = editorMode == EditorMode.DOOR,
                        onClick = { editorMode = EditorMode.DOOR }
                    )
                    EditorToolButton(
                        icon = Icons.Rounded.Window,
                        label = "Window",
                        selected = editorMode == EditorMode.WINDOW,
                        onClick = { editorMode = EditorMode.WINDOW }
                    )
                    EditorToolButton(
                        icon = Icons.Rounded.Delete,
                        label = "Delete",
                        selected = editorMode == EditorMode.DELETE,
                        onClick = { editorMode = EditorMode.DELETE }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorToolButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "toolBg"
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "toolContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        contentColor = contentColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

class LayoutEditorViewModel(
    private val roomId: Long,
    private val repository: AppRepository
) : ViewModel() {

    private val _wallPoints = MutableStateFlow<List<WallPointEntity>>(emptyList())
    val wallPoints: StateFlow<List<WallPointEntity>> = _wallPoints.asStateFlow()

    private val _roomElements = MutableStateFlow<List<RoomElementEntity>>(emptyList())
    val roomElements: StateFlow<List<RoomElementEntity>> = _roomElements.asStateFlow()

    private val _room = MutableStateFlow<RoomEntity?>(null)
    val room: StateFlow<RoomEntity?> = _room.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getWallPoints(roomId).collect { _wallPoints.value = it }
        }
        viewModelScope.launch {
            repository.getRoomElements(roomId).collect { _roomElements.value = it }
        }
        viewModelScope.launch {
            _room.value = repository.getRoom(roomId)
        }
    }

    fun addElement(type: String, wallIndex: Int, position: Float) {
        viewModelScope.launch {
            repository.insertRoomElement(
                RoomElementEntity(
                    roomId = roomId,
                    type = type,
                    wallIndex = wallIndex,
                    positionOnWall = position
                )
            )
        }
    }

    fun removeElement(element: RoomElementEntity) {
        viewModelScope.launch {
            repository.deleteRoomElement(element)
        }
    }
}
