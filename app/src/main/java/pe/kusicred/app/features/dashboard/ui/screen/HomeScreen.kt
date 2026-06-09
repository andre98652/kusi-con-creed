package pe.kusicred.app.features.dashboard.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.util.*
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.growth.data.repository.GrowthRepository
import pe.kusicred.app.features.vaccines.data.repository.VaccineRepository
import pe.kusicred.app.features.vaccines.data.repository.VaccineWithRecord
import pe.kusicred.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ===== ViewModel =====
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val growthRepository: GrowthRepository,
    private val vaccineRepository: VaccineRepository
) : ViewModel() {

    private val _childId = MutableStateFlow("")
    val child = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else childRepository.getChildById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestGrowthRecord = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null)
        else growthRepository.getRecordsByChild(id).map { it.firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val nextVaccine = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null)
        else vaccineRepository.getVaccineSchedule(id).map { schedule ->
            schedule.filter { it.status == VaccineStatus.PENDING || it.status == VaccineStatus.RESCHEDULED }
                .minByOrNull { it.scheduledDate }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setChildId(id: String) { _childId.value = id }
}

// ===== Screen =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    childId: String,
    onNavigateToGrowthChart: () -> Unit,
    onNavigateToAddGrowth: () -> Unit,
    onNavigateToVaccines: () -> Unit,
    onNavigateToMilestones: () -> Unit,
    onNavigateToIron: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToPdfExport: () -> Unit,
    onChangeChild: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }

    val child by viewModel.child.collectAsState()
    val latestRecord by viewModel.latestGrowthRecord.collectAsState()
    val nextVaccine by viewModel.nextVaccine.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    child?.let { c ->
                        val age = AgeCalculator.calculate(c.birthDate)
                        Column {
                            Text("Hola! 👋", style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                            Text(c.fullName.split(" ").first(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    } ?: Text("Kusi-CRED")
                },
                actions = {
                    IconButton(onClick = onChangeChild) {
                        Icon(Icons.Default.SwitchAccount, "Cambiar perfil", tint = KusiGreen40)
                    }
                    IconButton(onClick = onNavigateToPremium) {
                        Icon(Icons.Default.Star, "Premium", tint = KusiOrange40)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Hero Card: Edad y perfil ----
            child?.let { c ->
                val age = AgeCalculator.calculate(c.birthDate)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(KusiGreen40, KusiGreenLight)),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (c.sex == Sex.MALE) "👦" else "👧",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(c.fullName.split(" ").take(2).joinToString(" "),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("🎂 ${age.asReadableString()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.9f))
                                    if (c.isPremature) {
                                        Text("⚡ Prematuro — Edad corregida activa",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---- Último control de crecimiento ----
            latestRecord?.let { record ->
                StatusCard(
                    title = "Último Control de Crecimiento",
                    status = record.nutritionalStatus.displayName,
                    statusColor = Color(record.nutritionalStatus.color),
                    message = record.messageForMother,
                    actionLabel = "Ver curvas OMS",
                    onAction = onNavigateToGrowthChart
                )
            } ?: run {
                ActionPromptCard(
                    emoji = "📏",
                    title = "Registrar primer control",
                    subtitle = "Agrega el peso y talla de tu bebé para ver las curvas de crecimiento",
                    buttonLabel = "Agregar registro",
                    onClick = onNavigateToAddGrowth,
                    color = KusiGreen40
                )
            }

            // ---- Próxima vacuna ----
            nextVaccine?.let { vax ->
                val daysUntil = LocalDate.now().until(vax.scheduledDate, java.time.temporal.ChronoUnit.DAYS)
                val dateStr = vax.scheduledDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val urgencyColor = when {
                    daysUntil < 0 -> KusiError
                    daysUntil <= 7 -> KusiStatusRisk
                    else -> KusiBlue40
                }
                StatusCard(
                    title = "Próxima Vacuna",
                    status = if (daysUntil < 0) "VENCIDA" else if (daysUntil == 0L) "¡HOY!" else "En $daysUntil días",
                    statusColor = urgencyColor,
                    message = "${vax.vaccine.shortName} — $dateStr\n${vax.vaccine.contextualTip}",
                    actionLabel = "Ver calendario",
                    onAction = onNavigateToVaccines
                )
            } ?: ActionPromptCard(
                emoji = "💉",
                title = "Sin vacunas pendientes",
                subtitle = "Revisa el calendario de vacunas",
                buttonLabel = "Ver calendario",
                onClick = onNavigateToVaccines,
                color = KusiBlue40
            )

            // ---- Módulos de acceso rápido ----
            Spacer(Modifier.height(8.dp))
            Text("Módulos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard("📈", "Crecimiento", KusiGreen40, Modifier.weight(1f), onNavigateToGrowthChart)
                QuickAccessCard("💉", "Vacunas", KusiBlue40, Modifier.weight(1f), onNavigateToVaccines)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard("🧩", "Hitos", KusiOrange40, Modifier.weight(1f), onNavigateToMilestones)
                QuickAccessCard("💊", "Hierro", Color(0xFFAB47BC), Modifier.weight(1f), onNavigateToIron)
            }
            Spacer(Modifier.height(24.dp))

            // Premium banner / PDF Export banner
            val isPremium = child?.isPremium == true
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onClick = {
                    if (isPremium) onNavigateToPdfExport()
                    else onNavigateToPremium()
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                if (isPremium) listOf(KusiGreen40, KusiGreenLight)
                                else listOf(Color(0xFFF9A825), Color(0xFFFF8C69))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (isPremium) {
                                Text("📄 Reporte Clínico CRED", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Exportar historial de crecimiento, vacunas e hitos en PDF", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            } else {
                                Text("⭐ Kusi Premium", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("PDF clínico, alertas de riesgo\ny modo multi-perfil", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatusCard(title: String, status: String, statusColor: Color, message: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(actionLabel, color = KusiGreen40, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ArrowForward, null, tint = KusiGreen40, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ActionPromptCard(emoji: String, title: String, subtitle: String, buttonLabel: String, onClick: () -> Unit, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                Text(buttonLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickAccessCard(emoji: String, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = KusiOnBackground)
        }
    }
}
