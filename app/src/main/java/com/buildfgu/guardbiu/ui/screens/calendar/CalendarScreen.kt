package com.buildfgu.guardbiu.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.buildfgu.guardbiu.data.local.entity.TaskEntity
import com.buildfgu.guardbiu.data.repository.AppRepository
import com.buildfgu.guardbiu.ui.navigation.Screen
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavHostController) {
    val viewModel: CalendarViewModel = koinViewModel()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val taskDates by viewModel.taskDates.collectAsState()

    val (year, month) = currentMonth
    val monthName = remember(year, month) {
        val cal = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month) }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val today = remember {
        val cal = Calendar.getInstance()
        Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }

    val selectedCal = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }
    val selectedDay = selectedCal.get(Calendar.DAY_OF_MONTH)
    val selectedMonth = selectedCal.get(Calendar.MONTH)
    val selectedYear = selectedCal.get(Calendar.YEAR)

    val calendarDays = remember(year, month) { buildCalendarGrid(year, month) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Tasks.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Task, contentDescription = "Tasks")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousMonth() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                            }
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            val dayHeaders = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            dayHeaders.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        calendarDays.chunked(7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                week.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .then(
                                                if (day != null) Modifier.clip(CircleShape).clickable {
                                                    val cal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, year)
                                                        set(Calendar.MONTH, month)
                                                        set(Calendar.DAY_OF_MONTH, day)
                                                        set(Calendar.HOUR_OF_DAY, 0)
                                                        set(Calendar.MINUTE, 0)
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    viewModel.selectDate(cal.timeInMillis)
                                                } else Modifier
                                            )
                                            .then(
                                                when {
                                                    day != null && day == today.third && month == today.second && year == today.first ->
                                                        Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                                    day != null && day == selectedDay && month == selectedMonth && year == selectedYear ->
                                                        Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                                    else -> Modifier
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day != null) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                val isToday = day == today.third && month == today.second && year == today.first
                                                Text(
                                                    text = "$day",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isToday -> MaterialTheme.colorScheme.onPrimary
                                                        day == selectedDay && month == selectedMonth && year == selectedYear -> MaterialTheme.colorScheme.onPrimaryContainer
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                if (day in taskDates) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(
                                                                if (isToday) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                val sdf = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
                Text(
                    text = "Tasks for ${sdf.format(Date(selectedDate))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (tasksForSelectedDate.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Text(
                            "No tasks for this day",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(tasksForSelectedDate) { task ->
                    TaskDayItem(task)
                }
            }
        }
    }
}

@Composable
private fun TaskDayItem(task: TaskEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun buildCalendarGrid(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val mondayOffset = when (firstDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = 42
    val days = mutableListOf<Int?>()
    for (i in 0 until mondayOffset) days.add(null)
    for (d in 1..daysInMonth) days.add(d)
    while (days.size < totalCells) days.add(null)
    return days
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(todayMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(currentYearMonth())
    val currentMonth: StateFlow<Pair<Int, Int>> = _currentMonth.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<TaskEntity>> = _selectedDate.flatMapLatest { millis ->
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1
        repository.getTasksByDateRange(startOfDay, endOfDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskDates: StateFlow<Set<Int>> = _currentMonth.flatMapLatest { (year, month) ->
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, year); set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        repository.getTasksByDateRange(start.timeInMillis, end.timeInMillis).map { tasks ->
            tasks.map { task ->
                Calendar.getInstance().apply { timeInMillis = task.date }.get(Calendar.DAY_OF_MONTH)
            }.toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectDate(millis: Long) {
        _selectedDate.value = millis
    }

    fun nextMonth() {
        _currentMonth.update { (y, m) ->
            if (m == 11) Pair(y + 1, 0) else Pair(y, m + 1)
        }
    }

    fun previousMonth() {
        _currentMonth.update { (y, m) ->
            if (m == 0) Pair(y - 1, 11) else Pair(y, m - 1)
        }
    }

    private fun todayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun currentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }
}
