package pe.kusicred.app.features.vaccines.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.database.dao.IronRecordDao
import pe.kusicred.app.core.database.entity.IronRecordEntity
import pe.kusicred.app.core.util.*
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.vaccines.data.repository.VaccineRepository
import pe.kusicred.app.features.vaccines.data.repository.VaccineWithRecord
import pe.kusicred.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

// ===== ViewModels =====

@HiltViewModel
class VaccineViewModel @Inject constructor(
    private val vaccineRepository: VaccineRepository,
    private val childRepository: ChildRepository
) : ViewModel() {
    private val _childId = MutableStateFlow("")

    val child = _childId.flatMapLatest { if (it.isBlank()) flowOf(null) else childRepository.getChildById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val schedule = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else vaccineRepository.getVaccineSchedule(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setChildId(id: String) { _childId.value = id }

    fun markApplied(recordId: String) {
        viewModelScope.launch {
            vaccineRepository.markVaccineApplied(recordId, LocalDate.now())
        }
    }

    fun reschedule(recordId: String, newDate: LocalDate, childId: String) {
        viewModelScope.launch {
            vaccineRepository.rescheduleVaccine(recordId, newDate, childId)
        }
    }
}

@HiltViewModel
class IronTrackerViewModel @Inject constructor(
    private val ironRecordDao: IronRecordDao,
    private val childRepository: ChildRepository
) : ViewModel() {
    private val _childId = MutableStateFlow("")

    val child = _childId.flatMapLatest { if (it.isBlank()) flowOf(null) else childRepository.getChildById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ironRecords = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else ironRecordDao.getRecordsByChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setChildId(id: String) { _childId.value = id }

    fun toggleIron(date: LocalDate, taken: Boolean) {
        val id = _childId.value
        if (id.isBlank()) return
        viewModelScope.launch {
            val dayStart = date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000L
            val dayEnd = date.plusDays(1).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000L
            val existing = ironRecordDao.getRecordForDay(id, dayStart, dayEnd)
            if (existing != null) {
                // Update existing
                ironRecordDao.insertRecord(existing.copy(taken = taken))
            } else {
                ironRecordDao.insertRecord(
                    IronRecordEntity(
                        id = UUID.randomUUID().toString(),
                        childId = id,
                        dateMillis = dayStart,
                        taken = taken
                    )
                )
            }
        }
    }
}

// ===== Screens =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineCalendarScreen(
    childId: String,
    onBack: () -> Unit,
    onVaccineClick: (String) -> Unit,
    onIronTracker: () -> Unit,
    viewModel: VaccineViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val schedule by viewModel.schedule.collectAsState()

    val pending = schedule.filter { it.status == VaccineStatus.PENDING || it.status == VaccineStatus.RESCHEDULED }
    val applied = schedule.filter { it.status == VaccineStatus.APPLIED }
    val overdue = schedule.filter { it.status == VaccineStatus.OVERDUE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vacunas", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onIronTracker) {
                        Icon(Icons.Default.WaterDrop, "Hierro", tint = Color(0xFFAB47BC))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Iron tracker banner
            item {
                Card(
                    onClick = onIronTracker,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💊", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Suplementación de Hierro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Registro diario de gotas de hierro", style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFAB47BC))
                    }
                }
            }

            if (overdue.isNotEmpty()) {
                item { VaccineGroupHeader("⚠️ Vencidas (${overdue.size})", KusiError) }
                items(overdue) { VaccineTimelineItem(it, onVaccineClick, viewModel, childId) }
            }

            item { VaccineGroupHeader("⏳ Pendientes (${pending.size})", KusiBlue40) }
            if (pending.isEmpty()) {
                item { Text("¡Sin vacunas pendientes próximas! 🎉", color = KusiOnSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp)) }
            } else {
                items(pending) { VaccineTimelineItem(it, onVaccineClick, viewModel, childId) }
            }

            if (applied.isNotEmpty()) {
                item { VaccineGroupHeader("✅ Aplicadas (${applied.size})", KusiStatusNormal) }
                items(applied) { VaccineTimelineItem(it, onVaccineClick, viewModel, childId) }
            }
        }
    }
}

@Composable
fun VaccineGroupHeader(title: String, color: Color) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
        color = color, modifier = Modifier.padding(vertical = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineTimelineItem(
    item: VaccineWithRecord,
    onClick: (String) -> Unit,
    viewModel: VaccineViewModel,
    childId: String
) {
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    val daysUntil = today.until(item.scheduledDate, ChronoUnit.DAYS)

    val statusColor = when (item.status) {
        VaccineStatus.APPLIED -> KusiStatusNormal
        VaccineStatus.OVERDUE -> KusiError
        VaccineStatus.RESCHEDULED -> KusiOrange40
        VaccineStatus.PENDING -> if (daysUntil <= 7) KusiStatusRisk else KusiBlue40
    }

    val statusEmoji = when (item.status) {
        VaccineStatus.APPLIED -> "✅"
        VaccineStatus.OVERDUE -> "⚠️"
        VaccineStatus.RESCHEDULED -> "📅"
        VaccineStatus.PENDING -> if (daysUntil <= 3) "🔔" else "💉"
    }

    var showRescheduleDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { onClick(item.record.id) },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(statusColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(statusEmoji, style = MaterialTheme.typography.titleMedium) }

            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.vaccine.shortName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val dateLabel = if (item.status == VaccineStatus.APPLIED)
                    "Aplicada: ${item.appliedDate?.format(fmt) ?: "-"}"
                else
                    "Fecha: ${(item.rescheduledDate ?: item.scheduledDate).format(fmt)}"
                Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                if (item.status == VaccineStatus.PENDING && daysUntil >= 0) {
                    Text("En $daysUntil días", style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }

            if (item.status == VaccineStatus.PENDING || item.status == VaccineStatus.OVERDUE) {
                Column {
                    IconButton(onClick = { viewModel.markApplied(item.record.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CheckCircle, "Marcar aplicada", tint = KusiStatusNormal, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { showRescheduleDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.EventAvailable, "Reprogramar", tint = KusiOrange40, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showRescheduleDialog) {
        var newDate by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reprogramar vacuna", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ingresa la nueva fecha (DD/MM/AAAA):", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("Nueva fecha") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        val date = LocalDate.parse(newDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        viewModel.reschedule(item.record.id, date, childId)
                        showRescheduleDialog = false
                    } catch (e: Exception) { }
                }) { Text("Reprogramar") }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineDetailScreen(
    vaccineRecordId: String,
    childId: String,
    onBack: () -> Unit,
    viewModel: VaccineViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val schedule by viewModel.schedule.collectAsState()
    val item = schedule.find { it.record.id == vaccineRecordId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.vaccine?.shortName ?: "Vacuna", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        item?.let { vax ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = KusiBlue40.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(vax.vaccine.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(vax.vaccine.description, style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                    }
                }

                // Contextual tip
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Text("💡", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Consejo para el día", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(vax.vaccine.contextualTip, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5C4000))
                        }
                    }
                }

                // Side effects
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Posibles efectos secundarios", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(vax.vaccine.sideEffects, style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                    }
                }

                // Status and date
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Estado de la vacuna", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        Row {
                            Column(Modifier.weight(1f)) {
                                Text("Fecha programada", style = MaterialTheme.typography.labelSmall, color = KusiOnSurfaceVariant)
                                Text(vax.scheduledDate.format(fmt), fontWeight = FontWeight.Bold)
                            }
                            vax.appliedDate?.let {
                                Column(Modifier.weight(1f)) {
                                    Text("Fecha aplicada", style = MaterialTheme.typography.labelSmall, color = KusiOnSurfaceVariant)
                                    Text(it.format(fmt), fontWeight = FontWeight.Bold, color = KusiStatusNormal)
                                }
                            }
                        }
                    }
                }

                if (vax.status == VaccineStatus.PENDING || vax.status == VaccineStatus.OVERDUE) {
                    Button(
                        onClick = { viewModel.markApplied(vax.record.id) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KusiStatusNormal)
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como Aplicada ✓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IronTrackerScreen(
    childId: String,
    onBack: () -> Unit,
    viewModel: IronTrackerViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val child by viewModel.child.collectAsState()
    val ironRecords by viewModel.ironRecords.collectAsState()

    val today = LocalDate.now()
    val last30Days = (0..29).map { today.minusDays(it.toLong()) }.reversed()

    // Build map of date → taken
    val recordMap = ironRecords.associate { rec ->
        LocalDate.ofEpochDay(rec.dateMillis / (24 * 60 * 60 * 1000L)) to rec.taken
    }

    val takenCount = recordMap.values.count { it }
    val streak = calculateStreak(recordMap, today)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gotas de Hierro 💊", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Header card
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFAB47BC), Color(0xFFE040FB)))
                ).padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("💊", style = MaterialTheme.typography.displaySmall)
                    Text("Suplementación de Hierro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Previene la anemia y protege el desarrollo cerebral",
                        style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$takenCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("días este mes", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$streak", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("días seguidos 🔥", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Today's check
            val todayTaken = recordMap[today] ?: false
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (todayTaken) KusiStatusNormal.copy(alpha = 0.1f) else KusiSurface)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("¿Le diste las gotas hoy?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(today.format(DateTimeFormatter.ofPattern("EEEE dd/MM", java.util.Locale("es", "PE"))).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                    }
                    Switch(
                        checked = todayTaken,
                        onCheckedChange = { viewModel.toggleIron(today, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = KusiStatusNormal, checkedTrackColor = KusiStatusNormal.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Últimos 30 días", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            // Calendar grid (5 weeks × 7 days)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                last30Days.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                        week.forEach { date ->
                            val taken = recordMap[date]
                            val bgColor = when {
                                taken == true -> KusiStatusNormal
                                taken == false -> KusiError.copy(alpha = 0.3f)
                                date == today -> KusiBlue40.copy(alpha = 0.3f)
                                date > today -> Color.Transparent
                                else -> KusiDivider
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(bgColor, RoundedCornerShape(8.dp))
                                    .clickable { if (date <= today) viewModel.toggleIron(date, !(recordMap[date] ?: false)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall,
                                    color = if (taken == true) Color.White else KusiOnBackground,
                                    fontWeight = if (date == today) FontWeight.ExtraBold else FontWeight.Normal)
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("💡 ¿Por qué es importante?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("La anemia por deficiencia de hierro afecta el desarrollo cerebral en los primeros 2 años de vida. Los efectos son irreversibles. ¡La constancia diaria hace la diferencia!",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C4000))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun calculateStreak(recordMap: Map<LocalDate, Boolean>, today: LocalDate): Int {
    var streak = 0
    var date = today
    while (recordMap[date] == true) {
        streak++
        date = date.minusDays(1)
    }
    return streak
}
